# AI Chatbot Floating Widget for Backend Management System

A comprehensive solution for embedding an AI-powered Q&A chatbot floating widget in a Vue.js + Spring Boot backend management system.

## 🚀 Features

- **Global Accessibility**: Available across all pages of the management system
- **API Integration**: Retrieves system data through RESTful API calls
- **Dynamic Rendering**: Personalized displays based on content formats
- **UI Consistency**: Seamlessly integrates with Element-UI components
- **Cross-page Context**: Maintains conversation context across navigation
- **Security**: JWT-based authentication and secure API communication
- **Responsive Design**: Optimized for desktop and mobile interfaces

## 🏗️ Architecture

### Frontend (Vue.js)
- **Floating Widget Component**: Draggable, resizable chat interface
- **Vuex State Management**: Centralized chat state and API management
- **Element-UI Integration**: Consistent styling with existing UI framework
- **Dynamic Content Renderer**: Supports text, cards, lists, and rich media

### Backend (Spring Boot)
- **RESTful API Endpoints**: Chat processing and system integration
- **AI Service Integration**: Configurable AI provider support
- **Security Layer**: JWT authentication and authorization
- **Context Management**: Session-based conversation tracking

## 📁 Project Structure

```
ai-chatbot-widget/
├── backend/                    # Spring Boot application
│   ├── src/main/java/
│   │   └── com/chatbot/
│   │       ├── controller/     # REST controllers
│   │       ├── service/        # Business logic
│   │       ├── model/          # Data models
│   │       ├── config/         # Configuration
│   │       └── security/       # Security implementation
│   └── pom.xml
├── frontend/                   # Vue.js components
│   ├── components/
│   │   ├── ChatWidget/         # Main widget components
│   │   └── ChatRenderer/       # Dynamic content renderers
│   ├── store/                  # Vuex store modules
│   ├── services/               # API services
│   └── styles/                 # Widget-specific styles
├── docs/                       # Documentation
└── examples/                   # Integration examples
```

## 🛠️ Technology Stack

- **Backend**: Spring Boot 2.7+, Spring Security, JPA
- **Frontend**: Vue.js 2.x/3.x, Vuex, Element-UI
- **Database**: MySQL/PostgreSQL (configurable)
- **Security**: JWT tokens, CORS configuration
- **AI Integration**: OpenAI API, Azure OpenAI, or custom providers

## 📖 Quick Start

1. **Backend Setup**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Frontend Integration**
   ```javascript
   // Import and register the widget
   import ChatWidget from './components/ChatWidget'
   Vue.component('ChatWidget', ChatWidget)
   ```

3. **Add to your Vue app**
   ```vue
   <template>
     <div id="app">
       <!-- Your existing content -->
       <ChatWidget />
     </div>
   </template>
   ```

## 🔧 Configuration

See the `docs/` directory for detailed configuration options, API documentation, and integration guides.

## 📝 License

MIT License - See LICENSE file for details
