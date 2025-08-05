<template>
  <div id="app">
    <!-- Your existing application layout -->
    <el-container>
      <!-- Header -->
      <el-header>
        <el-menu
          :default-active="activeIndex"
          class="el-menu-demo"
          mode="horizontal"
          @select="handleSelect"
        >
          <el-menu-item index="1">Dashboard</el-menu-item>
          <el-menu-item index="2">Users</el-menu-item>
          <el-menu-item index="3">Orders</el-menu-item>
          <el-menu-item index="4">Products</el-menu-item>
          <el-menu-item index="5">Reports</el-menu-item>
        </el-menu>
      </el-header>

      <!-- Main Content -->
      <el-container>
        <el-aside width="200px">
          <el-menu
            default-active="1-1"
            class="el-menu-vertical-demo"
            @open="handleOpen"
            @close="handleClose"
          >
            <el-submenu index="1">
              <template slot="title">
                <i class="el-icon-location"></i>
                <span>Navigation One</span>
              </template>
              <el-menu-item index="1-1">Option 1</el-menu-item>
              <el-menu-item index="1-2">Option 2</el-menu-item>
            </el-submenu>
          </el-menu>
        </el-aside>

        <el-main>
          <!-- Your page content -->
          <el-card class="box-card">
            <div slot="header" class="clearfix">
              <span>Management Dashboard</span>
              <el-button style="float: right; padding: 3px 0" type="text">
                Operation button
              </el-button>
            </div>
            
            <div class="content">
              <p>This is your existing Vue.js application content.</p>
              <p>The AI chatbot widget will appear as a floating button in the bottom-right corner.</p>
              
              <!-- Example data that the chatbot can access -->
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card>
                    <div slot="header">Users</div>
                    <div>Total: {{ stats.users }}</div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card>
                    <div slot="header">Orders</div>
                    <div>Total: {{ stats.orders }}</div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card>
                    <div slot="header">Revenue</div>
                    <div>${{ stats.revenue.toLocaleString() }}</div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <!-- AI Chatbot Widget - This is the key integration -->
    <ChatWidget
      @chat-action="handleChatAction"
      @chat-function="handleChatFunction"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import ChatWidget from '../frontend/components/ChatWidget/ChatWidget.vue'

export default {
  name: 'App',
  
  components: {
    ChatWidget
  },

  data() {
    return {
      activeIndex: '1',
      stats: {
        users: 1234,
        orders: 567,
        revenue: 89012
      }
    }
  },

  computed: {
    ...mapState('user', ['currentUser']),
    
    // System context that will be passed to the chatbot
    systemContext() {
      return {
        currentPage: this.$route.name,
        userRole: this.currentUser?.role,
        stats: this.stats,
        availableActions: this.getAvailableActions(),
        timestamp: new Date().toISOString()
      }
    }
  },

  mounted() {
    // Initialize the chatbot with system context
    this.initializeChatbot()
  },

  methods: {
    handleSelect(key, keyPath) {
      console.log(key, keyPath)
    },

    handleOpen(key, keyPath) {
      console.log(key, keyPath)
    },

    handleClose(key, keyPath) {
      console.log(key, keyPath)
    },

    // Initialize chatbot with system context
    initializeChatbot() {
      // Update chat store with current system context
      this.$store.dispatch('chat/updateConfig', {
        systemContext: this.systemContext
      })
    },

    // Handle actions triggered by the chatbot
    handleChatAction(action) {
      console.log('Chat action triggered:', action)
      
      switch (action.type) {
        case 'navigation':
          this.handleNavigation(action)
          break
        case 'data_request':
          this.handleDataRequest(action)
          break
        case 'system_action':
          this.handleSystemAction(action)
          break
        default:
          console.log('Unknown chat action:', action)
      }
    },

    // Handle function calls from the chatbot
    handleChatFunction(functionCall) {
      console.log('Chat function called:', functionCall)
      
      switch (functionCall.action) {
        case 'get_user_data':
          return this.getUserData(functionCall.parameters)
        case 'get_order_data':
          return this.getOrderData(functionCall.parameters)
        case 'create_report':
          return this.createReport(functionCall.parameters)
        case 'send_notification':
          return this.sendNotification(functionCall.parameters)
        default:
          console.log('Unknown function call:', functionCall)
          return null
      }
    },

    // Navigation handler
    handleNavigation(action) {
      const { path, params } = action.parameters
      this.$router.push({ path, params })
    },

    // Data request handler
    async handleDataRequest(action) {
      const { dataType, filters } = action.parameters
      
      try {
        let data
        switch (dataType) {
          case 'users':
            data = await this.fetchUsers(filters)
            break
          case 'orders':
            data = await this.fetchOrders(filters)
            break
          case 'products':
            data = await this.fetchProducts(filters)
            break
          default:
            throw new Error(`Unknown data type: ${dataType}`)
        }
        
        // Send data back to chatbot
        this.$store.dispatch('chat/sendChatMessage', {
          message: `Here's the ${dataType} data you requested:`,
          systemContext: { requestedData: data }
        })
        
      } catch (error) {
        this.$message.error(`Failed to fetch ${dataType}: ${error.message}`)
      }
    },

    // System action handler
    handleSystemAction(action) {
      const { actionType, parameters } = action.parameters
      
      switch (actionType) {
        case 'refresh_data':
          this.refreshDashboardData()
          break
        case 'export_data':
          this.exportData(parameters)
          break
        case 'send_email':
          this.sendEmail(parameters)
          break
        default:
          console.log('Unknown system action:', actionType)
      }
    },

    // Get available actions based on current context
    getAvailableActions() {
      return [
        { action: 'view_users', label: 'View Users', permission: 'users.read' },
        { action: 'view_orders', label: 'View Orders', permission: 'orders.read' },
        { action: 'create_report', label: 'Create Report', permission: 'reports.create' },
        { action: 'export_data', label: 'Export Data', permission: 'data.export' }
      ].filter(action => this.hasPermission(action.permission))
    },

    // Permission check
    hasPermission(permission) {
      return this.currentUser?.permissions?.includes(permission) || 
             this.currentUser?.role === 'admin'
    },

    // Data fetching methods
    async fetchUsers(filters = {}) {
      // Simulate API call
      return new Promise(resolve => {
        setTimeout(() => {
          resolve([
            { id: 1, name: 'John Doe', email: 'john@example.com', status: 'active' },
            { id: 2, name: 'Jane Smith', email: 'jane@example.com', status: 'active' },
            { id: 3, name: 'Bob Johnson', email: 'bob@example.com', status: 'inactive' }
          ])
        }, 500)
      })
    },

    async fetchOrders(filters = {}) {
      return new Promise(resolve => {
        setTimeout(() => {
          resolve([
            { id: 1, customer: 'John Doe', amount: 150.00, status: 'completed' },
            { id: 2, customer: 'Jane Smith', amount: 75.50, status: 'pending' },
            { id: 3, customer: 'Bob Johnson', amount: 200.00, status: 'shipped' }
          ])
        }, 500)
      })
    },

    async fetchProducts(filters = {}) {
      return new Promise(resolve => {
        setTimeout(() => {
          resolve([
            { id: 1, name: 'Product A', price: 29.99, stock: 100 },
            { id: 2, name: 'Product B', price: 49.99, stock: 50 },
            { id: 3, name: 'Product C', price: 19.99, stock: 200 }
          ])
        }, 500)
      })
    },

    // Utility methods
    refreshDashboardData() {
      // Refresh dashboard statistics
      this.stats = {
        users: Math.floor(Math.random() * 2000) + 1000,
        orders: Math.floor(Math.random() * 1000) + 500,
        revenue: Math.floor(Math.random() * 100000) + 50000
      }
      
      this.$message.success('Dashboard data refreshed')
    },

    exportData(parameters) {
      const { format, dataType } = parameters
      this.$message.info(`Exporting ${dataType} data in ${format} format...`)
      
      // Simulate export process
      setTimeout(() => {
        this.$message.success(`${dataType} data exported successfully`)
      }, 2000)
    },

    sendEmail(parameters) {
      const { to, subject, body } = parameters
      this.$message.info(`Sending email to ${to}...`)
      
      // Simulate email sending
      setTimeout(() => {
        this.$message.success('Email sent successfully')
      }, 1500)
    }
  },

  // Watch for route changes to update chatbot context
  watch: {
    '$route'() {
      this.initializeChatbot()
    },
    
    stats: {
      handler() {
        this.initializeChatbot()
      },
      deep: true
    }
  }
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100vh;
}

.el-header {
  background-color: #545c64;
  color: #fff;
  text-align: center;
  line-height: 60px;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  line-height: 200px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.content {
  padding: 20px 0;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
}
</style>