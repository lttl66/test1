# AI Chatbot Widget Integration Guide

This guide provides step-by-step instructions for integrating the AI chatbot floating widget into your existing Vue.js + Spring Boot backend management system.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Backend Setup](#backend-setup)
3. [Frontend Integration](#frontend-integration)
4. [Configuration](#configuration)
5. [Customization](#customization)
6. [Security](#security)
7. [Deployment](#deployment)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### Backend Requirements
- Java 11 or higher
- Spring Boot 2.7+
- Maven 3.6+
- MySQL 8.0+ or PostgreSQL 12+ (optional, H2 included for development)

### Frontend Requirements
- Node.js 14+
- Vue.js 2.6+ or 3.x
- Element-UI 2.15+
- Vuex 3.x (Vue 2) or 4.x (Vue 3)
- Axios for HTTP requests

### AI Service Requirements
- OpenAI API key (or Azure OpenAI credentials)
- Internet connection for AI API calls

## Backend Setup

### 1. Add Dependencies

Add the provided `pom.xml` dependencies to your existing Spring Boot project:

```xml
<!-- Add these to your existing pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### 2. Copy Backend Files

Copy the following files to your Spring Boot project:

```
src/main/java/com/chatbot/
├── controller/ChatController.java
├── service/ChatService.java
├── service/AIService.java
├── service/impl/OpenAIServiceImpl.java
├── model/
│   ├── ChatMessage.java
│   ├── ChatSession.java
│   └── dto/
│       ├── ChatRequest.java
│       └── ChatResponse.java
├── repository/
│   ├── ChatMessageRepository.java
│   └── ChatSessionRepository.java
├── config/SecurityConfig.java
└── security/
    ├── JwtUtils.java
    ├── JwtAuthenticationFilter.java
    └── JwtAuthenticationEntryPoint.java
```

### 3. Update Application Configuration

Add the following to your `application.yml`:

```yaml
# AI Chatbot Configuration
jwt:
  secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
  expiration: 86400000 # 24 hours

ai:
  provider: openai
  openai:
    api-key: ${OPENAI_API_KEY:your-openai-api-key}
    model: gpt-3.5-turbo
    max-tokens: 1000
    temperature: 0.7

cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:8080
    - http://localhost:8081
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
  allowed-headers:
    - "*"
  allow-credentials: true
```

### 4. Environment Variables

Set the following environment variables:

```bash
export OPENAI_API_KEY="your-openai-api-key"
export JWT_SECRET="your-jwt-secret-key-minimum-32-characters"
```

### 5. Database Setup

The system uses H2 for development by default. For production, update your database configuration:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate # Use 'create' for first run, then 'validate'
```

## Frontend Integration

### 1. Install Dependencies

```bash
npm install axios vuex element-ui
```

### 2. Copy Frontend Files

Copy the following files to your Vue.js project:

```
src/
├── components/
│   ├── ChatWidget/
│   │   └── ChatWidget.vue
│   └── ChatRenderer/
│       └── ChatRenderer.vue
├── store/
│   └── chat.js
└── services/
    └── chatApi.js
```

### 3. Register Components

In your main Vue application file:

```javascript
// main.js
import Vue from 'vue'
import App from './App.vue'
import store from './store'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// Import chatbot components
import ChatWidget from './components/ChatWidget/ChatWidget.vue'

Vue.use(ElementUI)

// Register chatbot component globally
Vue.component('ChatWidget', ChatWidget)

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
```

### 4. Update Vuex Store

Add the chat module to your existing Vuex store:

```javascript
// store/index.js
import Vue from 'vue'
import Vuex from 'vuex'
import chat from './chat'
// ... your existing modules

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    chat,
    // ... your existing modules
  }
})
```

### 5. Add Widget to Your Application

Add the ChatWidget component to your main application template:

```vue
<template>
  <div id="app">
    <!-- Your existing application content -->
    <router-view />
    
    <!-- Add the chatbot widget -->
    <ChatWidget
      @chat-action="handleChatAction"
      @chat-function="handleChatFunction"
    />
  </div>
</template>

<script>
export default {
  name: 'App',
  methods: {
    handleChatAction(action) {
      // Handle actions triggered by the chatbot
      console.log('Chat action:', action)
    },
    
    handleChatFunction(functionCall) {
      // Handle function calls from the chatbot
      console.log('Chat function:', functionCall)
    }
  }
}
</script>
```

## Configuration

### Environment Variables

Create a `.env` file in your Vue.js project root:

```env
VUE_APP_API_URL=http://localhost:8080/api
VUE_APP_WS_URL=ws://localhost:8080/api/ws
```

### API Configuration

Update the API base URL in `services/chatApi.js` if needed:

```javascript
// services/chatApi.js
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'
```

### Widget Configuration

Configure the chatbot widget in your component:

```javascript
// In your Vue component
mounted() {
  // Configure chatbot
  this.$store.dispatch('chat/updateConfig', {
    apiUrl: process.env.VUE_APP_API_URL,
    autoReconnect: true,
    typingTimeout: 3000,
    messageRetention: 100
  })
  
  // Set user preferences
  this.$store.dispatch('chat/updatePreferences', {
    theme: 'default',
    language: 'en',
    notifications: true,
    soundEnabled: true
  })
}
```

## Customization

### Styling

Customize the widget appearance by overriding CSS variables:

```css
/* In your global CSS */
:root {
  --chat-primary-color: #409eff;
  --chat-secondary-color: #67c23a;
  --chat-background-color: #ffffff;
  --chat-border-radius: 12px;
  --chat-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}
```

### Custom Content Renderers

Add custom content renderers for specific data types:

```javascript
// In ChatRenderer.vue
computed: {
  customComponent() {
    const componentMap = {
      'user-profile': 'UserProfileRenderer',
      'order-details': 'OrderDetailsRenderer',
      'product-catalog': 'ProductCatalogRenderer'
    }
    
    return componentMap[this.metadata.type] || null
  }
}
```

### AI Response Formatting

Customize AI response formatting in the backend:

```java
// In OpenAIServiceImpl.java
private ChatResponse.CardContent createCustomCardContent(String response, Map<String, Object> analysis) {
    // Your custom card formatting logic
    return ChatResponse.CardContent.builder()
            .title("Custom Title")
            .description(response)
            .imageUrl("custom-image-url")
            .build();
}
```

## Security

### Authentication

The system uses JWT tokens for authentication. Ensure your existing authentication system provides JWT tokens:

```javascript
// Set authentication token
import chatApiService from './services/chatApi'

// After successful login
chatApiService.setAuthToken(token, persistent)
```

### CORS Configuration

Update CORS settings in your Spring Boot application:

```yaml
cors:
  allowed-origins:
    - https://yourdomain.com
    - https://admin.yourdomain.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
```

### Content Security Policy

Add CSP headers to allow the chatbot to function:

```javascript
// In your index.html or security configuration
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; 
               connect-src 'self' https://api.openai.com ws://localhost:8080 wss://localhost:8080; 
               script-src 'self' 'unsafe-inline'; 
               style-src 'self' 'unsafe-inline';">
```

## Deployment

### Backend Deployment

1. Build the Spring Boot application:
```bash
mvn clean package
```

2. Deploy the JAR file to your server
3. Set production environment variables:
```bash
export SPRING_PROFILES_ACTIVE=production
export OPENAI_API_KEY="your-production-api-key"
export JWT_SECRET="your-production-jwt-secret"
export DB_URL="your-production-database-url"
```

### Frontend Deployment

1. Build the Vue.js application:
```bash
npm run build
```

2. Update environment variables for production:
```env
VUE_APP_API_URL=https://api.yourdomain.com/api
VUE_APP_WS_URL=wss://api.yourdomain.com/api/ws
```

3. Deploy the built files to your web server

### Docker Deployment

Use the provided Docker configuration:

```dockerfile
# Backend Dockerfile
FROM openjdk:11-jre-slim
COPY target/ai-chatbot-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```dockerfile
# Frontend Dockerfile
FROM node:16-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

## Troubleshooting

### Common Issues

#### 1. CORS Errors
**Problem**: Browser blocks requests due to CORS policy.
**Solution**: Update CORS configuration in `application.yml` and ensure your frontend URL is included in `allowed-origins`.

#### 2. Authentication Failures
**Problem**: 401 Unauthorized errors when sending messages.
**Solution**: Ensure JWT token is properly set and not expired. Check token format and signing key.

#### 3. WebSocket Connection Issues
**Problem**: Real-time features not working.
**Solution**: Verify WebSocket URL is correct and server supports WebSocket connections. Check firewall settings.

#### 4. AI API Errors
**Problem**: OpenAI API calls failing.
**Solution**: Verify API key is correct and account has sufficient credits. Check rate limiting.

#### 5. Component Not Rendering
**Problem**: ChatWidget component not appearing.
**Solution**: Ensure component is properly registered and imported. Check console for JavaScript errors.

### Debug Mode

Enable debug logging:

```yaml
# application.yml
logging:
  level:
    com.chatbot: DEBUG
    org.springframework.security: DEBUG
```

```javascript
// In Vue.js
localStorage.setItem('debug', 'chat:*')
```

### Performance Optimization

1. **Lazy Loading**: Load chatbot components only when needed
2. **Message Pagination**: Implement pagination for long chat histories
3. **Caching**: Cache frequently accessed data
4. **Compression**: Enable gzip compression for API responses

### Support

For additional support:

1. Check the console logs for detailed error messages
2. Review the API documentation
3. Test with the demo endpoints first
4. Verify all dependencies are correctly installed

## Next Steps

After successful integration:

1. **Customize AI Prompts**: Tailor the AI responses to your domain
2. **Add Analytics**: Track chatbot usage and effectiveness
3. **Implement Feedback**: Allow users to rate responses
4. **Extend Functionality**: Add file upload, voice input, etc.
5. **Monitor Performance**: Set up monitoring and alerting

For advanced features and custom implementations, refer to the API documentation and component source code.