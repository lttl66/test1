import axios from 'axios'

class ChatApiService {
  constructor() {
    this.baseURL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'
    this.timeout = 30000
    
    // Create axios instance
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: this.timeout,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    // Setup interceptors
    this.setupInterceptors()
  }
  
  setupInterceptors() {
    // Request interceptor for authentication
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getAuthToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        
        // Add request timestamp for debugging
        config.metadata = { startTime: new Date() }
        
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )
    
    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response) => {
        // Calculate request duration
        const duration = new Date() - response.config.metadata.startTime
        console.log(`API Request completed in ${duration}ms:`, {
          url: response.config.url,
          method: response.config.method,
          status: response.status,
          duration
        })
        
        return response
      },
      (error) => {
        // Handle different types of errors
        if (error.response) {
          // Server responded with error status
          const { status, data } = error.response
          
          switch (status) {
            case 401:
              this.handleUnauthorized()
              break
            case 403:
              this.handleForbidden()
              break
            case 429:
              this.handleRateLimit(error.response)
              break
            case 500:
              this.handleServerError(error.response)
              break
          }
          
          // Enhance error with user-friendly message
          error.userMessage = this.getUserFriendlyErrorMessage(status, data)
        } else if (error.request) {
          // Network error
          error.userMessage = 'Network error. Please check your connection and try again.'
        } else {
          // Request setup error
          error.userMessage = 'An unexpected error occurred. Please try again.'
        }
        
        return Promise.reject(error)
      }
    )
  }
  
  // Authentication methods
  getAuthToken() {
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken')
  }
  
  setAuthToken(token, persistent = false) {
    if (persistent) {
      localStorage.setItem('authToken', token)
    } else {
      sessionStorage.setItem('authToken', token)
    }
  }
  
  clearAuthToken() {
    localStorage.removeItem('authToken')
    sessionStorage.removeItem('authToken')
  }
  
  // Error handlers
  handleUnauthorized() {
    this.clearAuthToken()
    // Emit event for app to handle
    window.dispatchEvent(new CustomEvent('auth:unauthorized'))
  }
  
  handleForbidden() {
    window.dispatchEvent(new CustomEvent('auth:forbidden'))
  }
  
  handleRateLimit(response) {
    const retryAfter = response.headers['retry-after']
    window.dispatchEvent(new CustomEvent('api:rateLimit', { 
      detail: { retryAfter } 
    }))
  }
  
  handleServerError(response) {
    console.error('Server error:', response.data)
    window.dispatchEvent(new CustomEvent('api:serverError', { 
      detail: response.data 
    }))
  }
  
  getUserFriendlyErrorMessage(status, data) {
    const messages = {
      400: 'Invalid request. Please check your input and try again.',
      401: 'Authentication required. Please log in and try again.',
      403: 'You do not have permission to perform this action.',
      404: 'The requested resource was not found.',
      429: 'Too many requests. Please wait a moment and try again.',
      500: 'Server error. Please try again later.',
      502: 'Service temporarily unavailable. Please try again later.',
      503: 'Service temporarily unavailable. Please try again later.'
    }
    
    return data?.message || messages[status] || 'An unexpected error occurred.'
  }
  
  // Chat API methods
  async sendMessage(messageData) {
    try {
      const response = await this.client.post('/chat/message', messageData)
      return response.data
    } catch (error) {
      console.error('Failed to send message:', error)
      throw error
    }
  }
  
  async getChatHistory(sessionId) {
    try {
      const response = await this.client.get(`/chat/history/${sessionId}`)
      return response.data
    } catch (error) {
      console.error('Failed to get chat history:', error)
      throw error
    }
  }
  
  async getUserSessions() {
    try {
      const response = await this.client.get('/chat/sessions')
      return response.data
    } catch (error) {
      console.error('Failed to get user sessions:', error)
      throw error
    }
  }
  
  async endSession(sessionId) {
    try {
      const response = await this.client.post(`/chat/session/${sessionId}/end`)
      return response.data
    } catch (error) {
      console.error('Failed to end session:', error)
      throw error
    }
  }
  
  async clearHistory(sessionId) {
    try {
      const response = await this.client.delete(`/chat/session/${sessionId}/history`)
      return response.data
    } catch (error) {
      console.error('Failed to clear history:', error)
      throw error
    }
  }
  
  // Demo/public methods (no auth required)
  async sendDemoMessage(messageData) {
    try {
      const response = await this.client.post('/chat/public/demo', messageData)
      return response.data
    } catch (error) {
      console.error('Failed to send demo message:', error)
      throw error
    }
  }
  
  async healthCheck() {
    try {
      const response = await this.client.get('/chat/health')
      return response.data
    } catch (error) {
      console.error('Health check failed:', error)
      throw error
    }
  }
  
  // Utility methods
  async testConnection() {
    try {
      await this.healthCheck()
      return true
    } catch (error) {
      return false
    }
  }
  
  // File upload for media messages
  async uploadFile(file, onProgress) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await this.client.post('/chat/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          if (onProgress) {
            const percentCompleted = Math.round(
              (progressEvent.loaded * 100) / progressEvent.total
            )
            onProgress(percentCompleted)
          }
        }
      })
      
      return response.data
    } catch (error) {
      console.error('Failed to upload file:', error)
      throw error
    }
  }
  
  // WebSocket connection helper
  createWebSocketUrl(path = '/ws') {
    const wsProtocol = this.baseURL.startsWith('https') ? 'wss' : 'ws'
    const baseUrl = this.baseURL.replace(/^https?/, wsProtocol)
    return `${baseUrl}${path}`
  }
  
  // Request cancellation
  createCancelToken() {
    return axios.CancelToken.source()
  }
  
  isRequestCanceled(error) {
    return axios.isCancel(error)
  }
  
  // Retry mechanism
  async retryRequest(requestFn, maxRetries = 3, delay = 1000) {
    let lastError
    
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        return await requestFn()
      } catch (error) {
        lastError = error
        
        // Don't retry on client errors (4xx)
        if (error.response && error.response.status >= 400 && error.response.status < 500) {
          throw error
        }
        
        // Don't retry on last attempt
        if (attempt === maxRetries) {
          break
        }
        
        // Wait before retrying
        await new Promise(resolve => setTimeout(resolve, delay * attempt))
      }
    }
    
    throw lastError
  }
  
  // Batch requests
  async batchRequests(requests) {
    try {
      const responses = await Promise.allSettled(requests.map(req => 
        typeof req === 'function' ? req() : req
      ))
      
      return responses.map((result, index) => ({
        index,
        success: result.status === 'fulfilled',
        data: result.status === 'fulfilled' ? result.value : null,
        error: result.status === 'rejected' ? result.reason : null
      }))
    } catch (error) {
      console.error('Batch request failed:', error)
      throw error
    }
  }
}

// Create singleton instance
const chatApiService = new ChatApiService()

export default chatApiService

// Named exports for specific methods
export const {
  sendMessage,
  getChatHistory,
  getUserSessions,
  endSession,
  clearHistory,
  sendDemoMessage,
  healthCheck,
  testConnection,
  uploadFile,
  createWebSocketUrl,
  retryRequest,
  batchRequests
} = chatApiService