import axios from 'axios'

// API Configuration
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'
const WS_URL = process.env.VUE_APP_WS_URL || 'ws://localhost:8080/api/ws'

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add request interceptor for authentication
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Add response interceptor for error handling
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken')
      // Optionally redirect to login
    }
    return Promise.reject(error)
  }
)

const state = {
  // Messages and conversation
  messages: [],
  currentSession: null,
  sessionHistory: [],
  
  // UI State
  isLoading: false,
  isTyping: false,
  isConnected: false,
  error: null,
  
  // WebSocket connection
  websocket: null,
  reconnectAttempts: 0,
  maxReconnectAttempts: 5,
  
  // Configuration
  config: {
    apiUrl: API_BASE_URL,
    wsUrl: WS_URL,
    autoReconnect: true,
    typingTimeout: 3000,
    messageRetention: 100 // Max messages to keep in memory
  },
  
  // User preferences
  preferences: {
    theme: 'default',
    language: 'en',
    notifications: true,
    soundEnabled: true
  }
}

const getters = {
  // Get messages for current session
  currentMessages: state => {
    return state.messages.filter(msg => 
      !state.currentSession || msg.sessionId === state.currentSession.sessionId
    )
  },
  
  // Check if there are unread messages
  hasUnreadMessages: state => {
    return state.messages.some(msg => !msg.isUser && !msg.read)
  },
  
  // Get unread message count
  unreadCount: state => {
    return state.messages.filter(msg => !msg.isUser && !msg.read).length
  },
  
  // Check if chat is ready
  isReady: state => {
    return !state.isLoading && state.isConnected
  },
  
  // Get last message
  lastMessage: state => {
    return state.messages[state.messages.length - 1] || null
  },
  
  // Get session by ID
  getSessionById: state => sessionId => {
    return state.sessionHistory.find(session => session.sessionId === sessionId)
  }
}

const mutations = {
  // Message mutations
  ADD_MESSAGE(state, message) {
    const messageWithDefaults = {
      id: Date.now() + Math.random(),
      timestamp: new Date().toISOString(),
      read: false,
      ...message
    }
    
    state.messages.push(messageWithDefaults)
    
    // Limit message history
    if (state.messages.length > state.config.messageRetention) {
      state.messages.splice(0, state.messages.length - state.config.messageRetention)
    }
  },
  
  UPDATE_MESSAGE(state, { id, updates }) {
    const messageIndex = state.messages.findIndex(msg => msg.id === id)
    if (messageIndex !== -1) {
      Object.assign(state.messages[messageIndex], updates)
    }
  },
  
  CLEAR_MESSAGES(state) {
    state.messages = []
  },
  
  MARK_MESSAGES_READ(state) {
    state.messages.forEach(msg => {
      if (!msg.isUser) {
        msg.read = true
      }
    })
  },
  
  // Session mutations
  SET_CURRENT_SESSION(state, session) {
    state.currentSession = session
    
    // Add to session history if not exists
    const existingIndex = state.sessionHistory.findIndex(s => s.sessionId === session.sessionId)
    if (existingIndex === -1) {
      state.sessionHistory.unshift(session)
    } else {
      // Move to front
      const [existingSession] = state.sessionHistory.splice(existingIndex, 1)
      state.sessionHistory.unshift({ ...existingSession, ...session })
    }
    
    // Limit session history
    if (state.sessionHistory.length > 10) {
      state.sessionHistory = state.sessionHistory.slice(0, 10)
    }
  },
  
  UPDATE_SESSION(state, { sessionId, updates }) {
    if (state.currentSession?.sessionId === sessionId) {
      Object.assign(state.currentSession, updates)
    }
    
    const sessionIndex = state.sessionHistory.findIndex(s => s.sessionId === sessionId)
    if (sessionIndex !== -1) {
      Object.assign(state.sessionHistory[sessionIndex], updates)
    }
  },
  
  // UI State mutations
  SET_LOADING(state, loading) {
    state.isLoading = loading
  },
  
  SET_TYPING(state, typing) {
    state.isTyping = typing
  },
  
  SET_CONNECTED(state, connected) {
    state.isConnected = connected
    if (connected) {
      state.reconnectAttempts = 0
    }
  },
  
  SET_ERROR(state, error) {
    state.error = error
  },
  
  // WebSocket mutations
  SET_WEBSOCKET(state, websocket) {
    state.websocket = websocket
  },
  
  INCREMENT_RECONNECT_ATTEMPTS(state) {
    state.reconnectAttempts++
  },
  
  RESET_RECONNECT_ATTEMPTS(state) {
    state.reconnectAttempts = 0
  },
  
  // Configuration mutations
  UPDATE_CONFIG(state, config) {
    Object.assign(state.config, config)
  },
  
  UPDATE_PREFERENCES(state, preferences) {
    Object.assign(state.preferences, preferences)
    localStorage.setItem('chatPreferences', JSON.stringify(state.preferences))
  }
}

const actions = {
  // Initialize chat module
  async initialize({ commit, dispatch }) {
    try {
      // Load preferences from localStorage
      const savedPreferences = localStorage.getItem('chatPreferences')
      if (savedPreferences) {
        commit('UPDATE_PREFERENCES', JSON.parse(savedPreferences))
      }
      
      // Load session history from localStorage
      const savedSessions = localStorage.getItem('chatSessions')
      if (savedSessions) {
        const sessions = JSON.parse(savedSessions)
        sessions.forEach(session => {
          commit('SET_CURRENT_SESSION', session)
        })
      }
      
      // Connect WebSocket
      await dispatch('connectWebSocket')
      
    } catch (error) {
      console.error('Failed to initialize chat:', error)
      commit('SET_ERROR', 'Failed to initialize chat system')
    }
  },
  
  // Send chat message
  async sendChatMessage({ commit, state, dispatch }, payload) {
    const { message, currentPage, systemContext, userPreferences } = payload
    
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      // Add user message to UI immediately
      const userMessage = {
        content: message,
        format: 'TEXT',
        isUser: true,
        sessionId: state.currentSession?.sessionId
      }
      commit('ADD_MESSAGE', userMessage)
      
      // Prepare request
      const requestData = {
        message,
        sessionId: state.currentSession?.sessionId,
        userId: state.currentSession?.userId || 'anonymous',
        currentPage,
        systemContext: {
          ...systemContext,
          timestamp: new Date().toISOString(),
          sessionId: state.currentSession?.sessionId
        },
        userPreferences: {
          ...state.preferences,
          ...userPreferences
        }
      }
      
      // Send to API
      const response = await apiClient.post('/chat/message', requestData)
      const aiResponse = response.data
      
      // Update session if new one was created
      if (aiResponse.sessionId && aiResponse.sessionId !== state.currentSession?.sessionId) {
        commit('SET_CURRENT_SESSION', {
          sessionId: aiResponse.sessionId,
          userId: requestData.userId,
          createdAt: new Date().toISOString(),
          lastActivity: new Date().toISOString()
        })
      }
      
      // Add AI response to UI
      const botMessage = {
        content: aiResponse.content || aiResponse.message,
        format: aiResponse.responseFormat || 'TEXT',
        metadata: aiResponse.metadata,
        suggestedActions: aiResponse.suggestedActions,
        isUser: false,
        sessionId: aiResponse.sessionId,
        timestamp: aiResponse.timestamp
      }
      commit('ADD_MESSAGE', botMessage)
      
      // Save session to localStorage
      dispatch('saveSessionToStorage')
      
    } catch (error) {
      console.error('Failed to send message:', error)
      
      // Add error message
      const errorMessage = {
        content: 'Sorry, I encountered an error processing your message. Please try again.',
        format: 'TEXT',
        isUser: false,
        error: true,
        sessionId: state.currentSession?.sessionId
      }
      commit('ADD_MESSAGE', errorMessage)
      
      commit('SET_ERROR', error.response?.data?.message || 'Failed to send message')
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // Clear chat history
  async clearChatHistory({ commit, state }) {
    try {
      if (state.currentSession?.sessionId) {
        await apiClient.delete(`/chat/session/${state.currentSession.sessionId}/history`)
      }
      
      commit('CLEAR_MESSAGES')
      localStorage.removeItem('chatMessages')
      
    } catch (error) {
      console.error('Failed to clear history:', error)
      throw error
    }
  },
  
  // Load chat history
  async loadChatHistory({ commit, state }, sessionId) {
    try {
      commit('SET_LOADING', true)
      
      const response = await apiClient.get(`/chat/history/${sessionId}`)
      const history = response.data
      
      // Clear current messages and load history
      commit('CLEAR_MESSAGES')
      
      history.forEach(msg => {
        // Add user message
        commit('ADD_MESSAGE', {
          content: msg.message,
          format: 'TEXT',
          isUser: true,
          sessionId: msg.sessionId,
          timestamp: msg.timestamp
        })
        
        // Add AI response
        commit('ADD_MESSAGE', {
          content: msg.response,
          format: msg.responseFormat,
          metadata: msg.metadata ? JSON.parse(msg.metadata) : {},
          isUser: false,
          sessionId: msg.sessionId,
          timestamp: msg.timestamp
        })
      })
      
    } catch (error) {
      console.error('Failed to load history:', error)
      commit('SET_ERROR', 'Failed to load chat history')
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  // Mark messages as read
  markMessagesAsRead({ commit }) {
    commit('MARK_MESSAGES_READ')
  },
  
  // WebSocket actions
  connectWebSocket({ commit, dispatch, state }) {
    return new Promise((resolve, reject) => {
      try {
        const ws = new WebSocket(state.config.wsUrl)
        
        ws.onopen = () => {
          console.log('WebSocket connected')
          commit('SET_WEBSOCKET', ws)
          commit('SET_CONNECTED', true)
          commit('RESET_RECONNECT_ATTEMPTS')
          resolve()
        }
        
        ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data)
            dispatch('handleWebSocketMessage', data)
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error)
          }
        }
        
        ws.onclose = () => {
          console.log('WebSocket disconnected')
          commit('SET_CONNECTED', false)
          commit('SET_WEBSOCKET', null)
          
          // Auto-reconnect
          if (state.config.autoReconnect && state.reconnectAttempts < state.maxReconnectAttempts) {
            setTimeout(() => {
              commit('INCREMENT_RECONNECT_ATTEMPTS')
              dispatch('connectWebSocket')
            }, 2000 * state.reconnectAttempts)
          }
        }
        
        ws.onerror = (error) => {
          console.error('WebSocket error:', error)
          commit('SET_ERROR', 'WebSocket connection error')
          reject(error)
        }
        
      } catch (error) {
        console.error('Failed to create WebSocket:', error)
        reject(error)
      }
    })
  },
  
  disconnectWebSocket({ commit, state }) {
    if (state.websocket) {
      state.websocket.close()
      commit('SET_WEBSOCKET', null)
      commit('SET_CONNECTED', false)
    }
  },
  
  handleWebSocketMessage({ commit }, data) {
    switch (data.type) {
      case 'typing':
        commit('SET_TYPING', data.isTyping)
        if (data.isTyping) {
          // Auto-clear typing indicator after timeout
          setTimeout(() => {
            commit('SET_TYPING', false)
          }, state.config.typingTimeout)
        }
        break
        
      case 'message':
        commit('ADD_MESSAGE', {
          content: data.content,
          format: data.format || 'TEXT',
          metadata: data.metadata,
          isUser: false,
          sessionId: data.sessionId
        })
        break
        
      case 'session_update':
        commit('UPDATE_SESSION', {
          sessionId: data.sessionId,
          updates: data.updates
        })
        break
        
      default:
        console.log('Unknown WebSocket message type:', data.type)
    }
  },
  
  // Utility actions
  saveSessionToStorage({ state }) {
    if (state.sessionHistory.length > 0) {
      localStorage.setItem('chatSessions', JSON.stringify(state.sessionHistory))
    }
  },
  
  updatePreferences({ commit }, preferences) {
    commit('UPDATE_PREFERENCES', preferences)
  },
  
  updateConfig({ commit }, config) {
    commit('UPDATE_CONFIG', config)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}