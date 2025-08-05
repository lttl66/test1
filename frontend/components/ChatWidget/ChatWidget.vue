<template>
  <div class="chat-widget-container">
    <!-- Chat Toggle Button -->
    <el-button
      v-if="!isOpen"
      type="primary"
      circle
      size="large"
      class="chat-toggle-button"
      @click="toggleChat"
      :style="{ bottom: position.bottom + 'px', right: position.right + 'px' }"
    >
      <i class="el-icon-chat-dot-round"></i>
      <el-badge
        v-if="unreadCount > 0"
        :value="unreadCount"
        class="unread-badge"
      />
    </el-button>

    <!-- Chat Window -->
    <el-card
      v-if="isOpen"
      class="chat-window"
      :class="{ 'chat-window-mobile': isMobile }"
      :style="chatWindowStyle"
      shadow="always"
    >
      <!-- Header -->
      <div slot="header" class="chat-header">
        <div class="chat-header-content">
          <div class="chat-title">
            <i class="el-icon-service"></i>
            <span>AI Assistant</span>
            <el-badge
              v-if="isConnected"
              is-dot
              class="status-badge online"
            />
            <el-badge
              v-else
              is-dot
              class="status-badge offline"
            />
          </div>
          <div class="chat-controls">
            <el-button
              type="text"
              size="mini"
              @click="minimizeChat"
              title="Minimize"
            >
              <i class="el-icon-minus"></i>
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click="toggleFullscreen"
              title="Toggle Fullscreen"
            >
              <i :class="isFullscreen ? 'el-icon-copy-document' : 'el-icon-full-screen'"></i>
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click="closeChat"
              title="Close"
            >
              <i class="el-icon-close"></i>
            </el-button>
          </div>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="welcome-message">
          <el-alert
            title="Welcome to AI Assistant!"
            description="I'm here to help you with your management system. Ask me anything!"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message-item"
          :class="{
            'user-message': message.isUser,
            'bot-message': !message.isUser,
            'message-error': message.error
          }"
        >
          <div class="message-avatar">
            <el-avatar
              v-if="message.isUser"
              :size="32"
              :src="userAvatar"
              icon="el-icon-user"
            />
            <el-avatar
              v-else
              :size="32"
              class="bot-avatar"
              icon="el-icon-service"
            />
          </div>

          <div class="message-content">
            <div class="message-meta">
              <span class="message-sender">
                {{ message.isUser ? 'You' : 'AI Assistant' }}
              </span>
              <span class="message-time">
                {{ formatTime(message.timestamp) }}
              </span>
            </div>

            <!-- Dynamic Content Renderer -->
            <ChatRenderer
              :content="message.content"
              :format="message.format"
              :metadata="message.metadata"
              @action="handleAction"
            />

            <!-- Suggested Actions -->
            <div
              v-if="message.suggestedActions && message.suggestedActions.length > 0"
              class="suggested-actions"
            >
              <el-button
                v-for="action in message.suggestedActions"
                :key="action.action"
                size="mini"
                type="primary"
                plain
                @click="handleSuggestedAction(action)"
              >
                {{ action.label }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="isTyping" class="typing-indicator">
          <div class="message-avatar">
            <el-avatar :size="32" class="bot-avatar" icon="el-icon-service" />
          </div>
          <div class="typing-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="inputRows"
          placeholder="Type your message here..."
          :disabled="isLoading"
          @keydown.enter.exact="handleSend"
          @keydown.enter.shift.exact="handleNewLine"
          @input="handleInput"
          ref="messageInput"
          class="message-input"
        />
        
        <div class="input-controls">
          <div class="input-tools">
            <el-button
              type="text"
              size="mini"
              @click="clearHistory"
              title="Clear History"
              :disabled="messages.length === 0"
            >
              <i class="el-icon-delete"></i>
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click="exportHistory"
              title="Export History"
              :disabled="messages.length === 0"
            >
              <i class="el-icon-download"></i>
            </el-button>
          </div>
          
          <el-button
            type="primary"
            size="small"
            @click="sendMessage"
            :loading="isLoading"
            :disabled="!inputMessage.trim()"
            class="send-button"
          >
            <i v-if="!isLoading" class="el-icon-s-promotion"></i>
            Send
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Resize Handle -->
    <div
      v-if="isOpen && !isMobile && !isFullscreen"
      class="resize-handle"
      @mousedown="startResize"
      :style="resizeHandleStyle"
    ></div>
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex'
import ChatRenderer from '../ChatRenderer/ChatRenderer.vue'

export default {
  name: 'ChatWidget',
  components: {
    ChatRenderer
  },
  
  data() {
    return {
      isOpen: false,
      isFullscreen: false,
      inputMessage: '',
      inputRows: 1,
      position: {
        bottom: 20,
        right: 20
      },
      size: {
        width: 380,
        height: 500
      },
      isDragging: false,
      isResizing: false,
      dragOffset: { x: 0, y: 0 },
      unreadCount: 0
    }
  },

  computed: {
    ...mapState('chat', [
      'messages',
      'isLoading',
      'isTyping',
      'isConnected',
      'currentSession'
    ]),
    
    ...mapGetters('chat', [
      'hasUnreadMessages'
    ]),

    isMobile() {
      return window.innerWidth <= 768
    },

    userAvatar() {
      return this.$store.state.user?.avatar || null
    },

    chatWindowStyle() {
      if (this.isMobile) {
        return {
          position: 'fixed',
          top: '0',
          left: '0',
          right: '0',
          bottom: '0',
          width: '100%',
          height: '100%',
          zIndex: 2000
        }
      }

      if (this.isFullscreen) {
        return {
          position: 'fixed',
          top: '20px',
          left: '20px',
          right: '20px',
          bottom: '20px',
          width: 'auto',
          height: 'auto',
          zIndex: 2000
        }
      }

      return {
        position: 'fixed',
        bottom: this.position.bottom + 'px',
        right: this.position.right + 'px',
        width: this.size.width + 'px',
        height: this.size.height + 'px',
        zIndex: 1000
      }
    },

    resizeHandleStyle() {
      return {
        position: 'fixed',
        bottom: this.position.bottom + 'px',
        right: this.position.right + 'px',
        width: '20px',
        height: '20px',
        cursor: 'nw-resize',
        zIndex: 1001
      }
    }
  },

  mounted() {
    this.initializeWidget()
    this.setupEventListeners()
  },

  beforeDestroy() {
    this.removeEventListeners()
  },

  methods: {
    ...mapActions('chat', [
      'sendChatMessage',
      'clearChatHistory',
      'connectWebSocket',
      'disconnectWebSocket',
      'markMessagesAsRead'
    ]),

    initializeWidget() {
      // Initialize chat store
      this.$store.dispatch('chat/initialize')
      
      // Connect WebSocket for real-time updates
      this.connectWebSocket()
      
      // Load position and size from localStorage
      this.loadWidgetState()
    },

    setupEventListeners() {
      window.addEventListener('resize', this.handleWindowResize)
      document.addEventListener('mousemove', this.handleMouseMove)
      document.addEventListener('mouseup', this.handleMouseUp)
    },

    removeEventListeners() {
      window.removeEventListener('resize', this.handleWindowResize)
      document.removeEventListener('mousemove', this.handleMouseMove)
      document.removeEventListener('mouseup', this.handleMouseUp)
    },

    toggleChat() {
      this.isOpen = !this.isOpen
      if (this.isOpen) {
        this.markMessagesAsRead()
        this.unreadCount = 0
        this.$nextTick(() => {
          this.scrollToBottom()
          this.focusInput()
        })
      }
      this.saveWidgetState()
    },

    minimizeChat() {
      this.isOpen = false
      this.saveWidgetState()
    },

    closeChat() {
      this.isOpen = false
      this.isFullscreen = false
      this.saveWidgetState()
    },

    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return

      const message = this.inputMessage.trim()
      this.inputMessage = ''
      this.inputRows = 1

      try {
        await this.sendChatMessage({
          message,
          currentPage: this.$route?.path || window.location.pathname,
          systemContext: this.getSystemContext(),
          userPreferences: this.getUserPreferences()
        })
        
        this.$nextTick(() => {
          this.scrollToBottom()
          this.focusInput()
        })
      } catch (error) {
        this.$message.error('Failed to send message. Please try again.')
        this.inputMessage = message // Restore message on error
      }
    },

    handleSend(event) {
      if (!event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    },

    handleNewLine() {
      // Allow default behavior for Shift+Enter
    },

    handleInput(value) {
      // Auto-resize textarea
      const lines = value.split('\n').length
      this.inputRows = Math.min(Math.max(lines, 1), 4)
    },

    handleAction(action) {
      // Handle actions from dynamic content renderer
      this.$emit('chat-action', action)
    },

    handleSuggestedAction(action) {
      if (action.type === 'message') {
        this.inputMessage = action.parameters?.message || action.label
        this.sendMessage()
      } else if (action.type === 'function') {
        this.$emit('chat-function', action)
      } else if (action.type === 'navigation') {
        this.$router?.push(action.parameters?.path || '/')
      }
    },

    async clearHistory() {
      try {
        await this.$confirm('Are you sure you want to clear the chat history?', 'Confirm', {
          type: 'warning'
        })
        await this.clearChatHistory()
        this.$message.success('Chat history cleared')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('Failed to clear history')
        }
      }
    },

    exportHistory() {
      const history = this.messages.map(msg => ({
        sender: msg.isUser ? 'User' : 'AI Assistant',
        message: msg.content,
        timestamp: msg.timestamp
      }))
      
      const dataStr = JSON.stringify(history, null, 2)
      const dataBlob = new Blob([dataStr], { type: 'application/json' })
      
      const link = document.createElement('a')
      link.href = URL.createObjectURL(dataBlob)
      link.download = `chat-history-${new Date().toISOString().split('T')[0]}.json`
      link.click()
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    focusInput() {
      this.$nextTick(() => {
        if (this.$refs.messageInput) {
          this.$refs.messageInput.focus()
        }
      })
    },

    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getSystemContext() {
      return {
        currentPage: this.$route?.name || 'unknown',
        userAgent: navigator.userAgent,
        timestamp: new Date().toISOString(),
        // Add more system context as needed
      }
    },

    getUserPreferences() {
      return {
        theme: 'default',
        language: 'en',
        // Add user preferences from store or localStorage
      }
    },

    // Drag and resize functionality
    startResize(event) {
      this.isResizing = true
      this.dragOffset = {
        x: event.clientX,
        y: event.clientY
      }
      event.preventDefault()
    },

    handleMouseMove(event) {
      if (this.isResizing) {
        const deltaX = this.dragOffset.x - event.clientX
        const deltaY = this.dragOffset.y - event.clientY
        
        this.size.width = Math.max(300, this.size.width + deltaX)
        this.size.height = Math.max(400, this.size.height + deltaY)
        
        this.dragOffset = {
          x: event.clientX,
          y: event.clientY
        }
      }
    },

    handleMouseUp() {
      if (this.isResizing) {
        this.isResizing = false
        this.saveWidgetState()
      }
    },

    handleWindowResize() {
      // Adjust position if widget goes off-screen
      const maxRight = window.innerWidth - this.size.width
      const maxBottom = window.innerHeight - this.size.height
      
      this.position.right = Math.min(this.position.right, maxRight)
      this.position.bottom = Math.min(this.position.bottom, maxBottom)
    },

    loadWidgetState() {
      const savedState = localStorage.getItem('chatWidgetState')
      if (savedState) {
        const state = JSON.parse(savedState)
        this.position = { ...this.position, ...state.position }
        this.size = { ...this.size, ...state.size }
        this.isOpen = state.isOpen || false
      }
    },

    saveWidgetState() {
      const state = {
        position: this.position,
        size: this.size,
        isOpen: this.isOpen
      }
      localStorage.setItem('chatWidgetState', JSON.stringify(state))
    }
  },

  watch: {
    hasUnreadMessages(hasUnread) {
      if (hasUnread && !this.isOpen) {
        this.unreadCount++
      }
    }
  }
}
</script>

<style scoped>
.chat-widget-container {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.chat-toggle-button {
  position: fixed;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: all 0.3s ease;
}

.chat-toggle-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
}

.chat-window {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
}

.chat-window-mobile {
  border-radius: 0;
}

.chat-header {
  padding: 0;
  border-bottom: 1px solid #e4e7ed;
}

.chat-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.chat-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.chat-title i {
  margin-right: 8px;
  font-size: 18px;
  color: #409eff;
}

.status-badge {
  margin-left: 8px;
}

.status-badge.online ::v-deep .el-badge__content {
  background-color: #67c23a;
}

.status-badge.offline ::v-deep .el-badge__content {
  background-color: #f56c6c;
}

.chat-controls {
  display: flex;
  gap: 4px;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  max-height: 400px;
  background: #fafafa;
}

.welcome-message {
  margin-bottom: 16px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: slideIn 0.3s ease;
}

.message-item.user-message {
  flex-direction: row-reverse;
}

.message-item.user-message .message-content {
  background: #409eff;
  color: white;
  margin-right: 8px;
  margin-left: 0;
}

.message-item.bot-message .message-content {
  background: white;
  margin-left: 8px;
  border: 1px solid #e4e7ed;
}

.message-item.message-error .message-content {
  background: #fef0f0;
  border-color: #f56c6c;
}

.message-avatar {
  flex-shrink: 0;
}

.bot-avatar {
  background: #409eff;
  color: white;
}

.message-content {
  max-width: 70%;
  padding: 12px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
  opacity: 0.7;
}

.message-sender {
  font-weight: 600;
}

.suggested-actions {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.typing-dots {
  display: flex;
  align-items: center;
  margin-left: 8px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c0c4cc;
  margin: 0 2px;
  animation: typing 1.4s infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

.chat-input-area {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background: white;
}

.message-input {
  margin-bottom: 8px;
}

.input-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-tools {
  display: flex;
  gap: 4px;
}

.send-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.resize-handle {
  background: #409eff;
  opacity: 0.3;
  transition: opacity 0.2s;
}

.resize-handle:hover {
  opacity: 0.6;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .chat-window-mobile .chat-messages {
    max-height: calc(100vh - 200px);
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>