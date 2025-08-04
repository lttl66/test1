# AI Chatbot API Documentation

This document provides comprehensive documentation for the AI Chatbot backend API endpoints.

## Base URL

```
http://localhost:8080/api
```

## Authentication

The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### Chat Endpoints

#### Send Message

Send a message to the AI chatbot and receive a response.

**Endpoint:** `POST /chat/message`

**Authentication:** Required

**Request Body:**
```json
{
  "message": "Hello, can you help me with user management?",
  "sessionId": "optional-session-id",
  "userId": "user-123",
  "currentPage": "/users",
  "systemContext": {
    "currentPage": "users",
    "userRole": "admin",
    "stats": {
      "totalUsers": 1234,
      "activeUsers": 987
    }
  },
  "userPreferences": {
    "theme": "default",
    "language": "en"
  }
}
```

**Response:**
```json
{
  "sessionId": "session-uuid",
  "message": "I can help you with user management. Here are the current statistics...",
  "responseFormat": "CARD",
  "content": {
    "title": "User Management Overview",
    "description": "Current user statistics and available actions",
    "data": {
      "totalUsers": 1234,
      "activeUsers": 987,
      "inactiveUsers": 247
    },
    "actions": [
      {
        "label": "View All Users",
        "action": "view_users",
        "type": "navigation",
        "parameters": {
          "path": "/users"
        }
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z",
  "success": true,
  "metadata": {
    "intent": "list",
    "dataType": "user",
    "suggestedFormat": "CARD"
  },
  "suggestedActions": [
    {
      "label": "View Users",
      "action": "view_users",
      "type": "navigation"
    }
  ]
}
```

**Error Response:**
```json
{
  "sessionId": "session-uuid",
  "message": "I apologize, but I encountered an error processing your request.",
  "responseFormat": "TEXT",
  "timestamp": "2024-01-15T10:30:00Z",
  "success": false,
  "error": "AI service temporarily unavailable"
}
```

#### Get Chat History

Retrieve chat history for a specific session.

**Endpoint:** `GET /chat/history/{sessionId}`

**Authentication:** Required

**Path Parameters:**
- `sessionId` (string): The session ID to retrieve history for

**Response:**
```json
[
  {
    "id": 1,
    "sessionId": "session-uuid",
    "userId": "user-123",
    "message": "Hello, can you help me?",
    "response": "Of course! I'm here to help you with your management system.",
    "messageType": "USER_QUERY",
    "responseFormat": "TEXT",
    "metadata": "{\"intent\":\"greeting\"}",
    "timestamp": "2024-01-15T10:25:00Z"
  },
  {
    "id": 2,
    "sessionId": "session-uuid",
    "userId": "user-123",
    "message": "Show me user statistics",
    "response": "Here are the current user statistics...",
    "messageType": "USER_QUERY",
    "responseFormat": "CARD",
    "metadata": "{\"intent\":\"list\",\"dataType\":\"user\"}",
    "timestamp": "2024-01-15T10:30:00Z"
  }
]
```

#### Get User Sessions

Retrieve all active sessions for the authenticated user.

**Endpoint:** `GET /chat/sessions`

**Authentication:** Required

**Response:**
```json
[
  {
    "sessionId": "session-uuid-1",
    "userId": "user-123",
    "context": "{\"currentPage\":\"dashboard\"}",
    "createdAt": "2024-01-15T09:00:00Z",
    "lastActivity": "2024-01-15T10:30:00Z",
    "active": true
  },
  {
    "sessionId": "session-uuid-2",
    "userId": "user-123",
    "context": "{\"currentPage\":\"users\"}",
    "createdAt": "2024-01-14T14:00:00Z",
    "lastActivity": "2024-01-14T16:45:00Z",
    "active": true
  }
]
```

#### End Session

End a specific chat session.

**Endpoint:** `POST /chat/session/{sessionId}/end`

**Authentication:** Required

**Path Parameters:**
- `sessionId` (string): The session ID to end

**Response:**
```json
{
  "message": "Session ended successfully"
}
```

#### Clear History

Clear chat history for a specific session.

**Endpoint:** `DELETE /chat/session/{sessionId}/history`

**Authentication:** Required

**Path Parameters:**
- `sessionId` (string): The session ID to clear history for

**Response:**
```json
{
  "message": "History cleared successfully"
}
```

### Public Endpoints

#### Demo Message

Send a demo message without authentication (for testing purposes).

**Endpoint:** `POST /chat/public/demo`

**Authentication:** Not required

**Request Body:**
```json
{
  "message": "Hello, this is a demo message",
  "currentPage": "/demo",
  "systemContext": {
    "demo": true
  }
}
```

**Response:** Same format as the authenticated message endpoint.

#### Health Check

Check the health status of the chat service.

**Endpoint:** `GET /chat/health`

**Authentication:** Not required

**Response:**
```json
{
  "status": "healthy",
  "service": "AI Chatbot API",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Response Formats

The API supports different response formats based on the content type:

### TEXT Format

Simple text responses.

```json
{
  "responseFormat": "TEXT",
  "content": "This is a simple text response."
}
```

### CARD Format

Structured card with title, description, and actions.

```json
{
  "responseFormat": "CARD",
  "content": {
    "title": "Card Title",
    "subtitle": "Optional subtitle",
    "description": "Card description with **markdown** support",
    "imageUrl": "https://example.com/image.jpg",
    "data": {
      "key1": "value1",
      "key2": "value2"
    },
    "actions": [
      {
        "label": "Action Button",
        "action": "action_name",
        "type": "primary",
        "parameters": {}
      }
    ]
  }
}
```

### LIST Format

List of items with optional metadata.

```json
{
  "responseFormat": "LIST",
  "content": {
    "title": "List Title",
    "items": [
      {
        "title": "Item 1",
        "subtitle": "Item subtitle",
        "description": "Item description",
        "iconUrl": "https://example.com/icon.png",
        "data": {
          "id": 1,
          "status": "active"
        }
      }
    ]
  }
}
```

### TABLE Format

Tabular data with headers and rows.

```json
{
  "responseFormat": "TABLE",
  "content": {
    "title": "Table Title",
    "headers": ["Name", "Email", "Status"],
    "rows": [
      ["John Doe", "john@example.com", "Active"],
      ["Jane Smith", "jane@example.com", "Inactive"]
    ],
    "options": {
      "sortable": true,
      "filterable": true
    }
  }
}
```

### RICH_MEDIA Format

Rich media content like images, videos, or charts.

```json
{
  "responseFormat": "RICH_MEDIA",
  "content": {
    "type": "image",
    "url": "https://example.com/chart.png",
    "caption": "User growth chart",
    "thumbnail": "https://example.com/thumb.png"
  }
}
```

## Error Handling

### HTTP Status Codes

- `200` - Success
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (missing or invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource not found)
- `429` - Too Many Requests (rate limit exceeded)
- `500` - Internal Server Error

### Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Message cannot be blank",
  "path": "/chat/message",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Rate Limiting

The API implements rate limiting to prevent abuse:

- **Authenticated users**: 100 requests per minute
- **Demo endpoint**: 10 requests per minute per IP

Rate limit headers are included in responses:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642248600
```

## WebSocket Support

Real-time features are supported via WebSocket connections:

**WebSocket URL:** `ws://localhost:8080/api/ws`

### WebSocket Message Types

#### Typing Indicator

```json
{
  "type": "typing",
  "sessionId": "session-uuid",
  "isTyping": true
}
```

#### Real-time Message

```json
{
  "type": "message",
  "sessionId": "session-uuid",
  "content": "Real-time message content",
  "format": "TEXT",
  "metadata": {}
}
```

#### Session Update

```json
{
  "type": "session_update",
  "sessionId": "session-uuid",
  "updates": {
    "lastActivity": "2024-01-15T10:30:00Z"
  }
}
```

## SDK Examples

### JavaScript/Axios

```javascript
import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Authorization': 'Bearer your-jwt-token',
    'Content-Type': 'application/json'
  }
});

// Send message
const response = await client.post('/chat/message', {
  message: 'Hello, AI assistant!',
  currentPage: '/dashboard',
  systemContext: { userRole: 'admin' }
});

console.log(response.data);
```

### cURL

```bash
# Send message
curl -X POST http://localhost:8080/api/chat/message \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, AI assistant!",
    "currentPage": "/dashboard",
    "systemContext": {"userRole": "admin"}
  }'

# Get chat history
curl -X GET http://localhost:8080/api/chat/history/session-uuid \
  -H "Authorization: Bearer your-jwt-token"

# Health check
curl -X GET http://localhost:8080/api/chat/health
```

### Python/Requests

```python
import requests

headers = {
    'Authorization': 'Bearer your-jwt-token',
    'Content-Type': 'application/json'
}

# Send message
response = requests.post(
    'http://localhost:8080/api/chat/message',
    headers=headers,
    json={
        'message': 'Hello, AI assistant!',
        'currentPage': '/dashboard',
        'systemContext': {'userRole': 'admin'}
    }
)

print(response.json())
```

## Configuration

### Environment Variables

Set these environment variables for proper API configuration:

```bash
# Required
OPENAI_API_KEY=your-openai-api-key
JWT_SECRET=your-jwt-secret-key

# Optional
AI_PROVIDER=openai
AI_MODEL=gpt-3.5-turbo
AI_MAX_TOKENS=1000
AI_TEMPERATURE=0.7
```

### Application Properties

```yaml
# AI Configuration
ai:
  provider: openai
  openai:
    api-key: ${OPENAI_API_KEY}
    model: ${AI_MODEL:gpt-3.5-turbo}
    max-tokens: ${AI_MAX_TOKENS:1000}
    temperature: ${AI_TEMPERATURE:0.7}

# Security
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

# CORS
cors:
  allowed-origins:
    - http://localhost:3000
    - https://yourdomain.com
```

## Testing

### Unit Tests

Run the backend unit tests:

```bash
mvn test
```

### Integration Tests

Test the API endpoints:

```bash
mvn test -Dtest=ChatControllerIntegrationTest
```

### Load Testing

Use tools like Apache Bench or JMeter to test API performance:

```bash
ab -n 1000 -c 10 -H "Authorization: Bearer token" \
   -p message.json -T application/json \
   http://localhost:8080/api/chat/message
```

## Monitoring

### Health Endpoints

- `/chat/health` - Service health check
- `/actuator/health` - Spring Boot actuator health
- `/actuator/metrics` - Application metrics

### Logging

Configure logging levels in `application.yml`:

```yaml
logging:
  level:
    com.chatbot: DEBUG
    org.springframework.security: INFO
    org.springframework.web: INFO
```

### Metrics

Key metrics to monitor:

- Request rate and response time
- AI API call success rate
- Database connection pool usage
- Memory and CPU usage
- Active WebSocket connections

## Security Considerations

1. **Authentication**: Always validate JWT tokens
2. **Authorization**: Implement role-based access control
3. **Input Validation**: Sanitize all user inputs
4. **Rate Limiting**: Prevent API abuse
5. **CORS**: Configure appropriate CORS policies
6. **HTTPS**: Use HTTPS in production
7. **API Keys**: Secure AI service API keys
8. **Logging**: Don't log sensitive information

## Versioning

The API follows semantic versioning. Current version: `v1`

Future versions will be available at:
- `/api/v2/chat/message`
- `/api/v3/chat/message`

## Support

For API support and questions:

1. Check the integration guide
2. Review error logs
3. Test with demo endpoints
4. Verify authentication tokens
5. Check rate limits and quotas

## Changelog

### v1.0.0 (2024-01-15)
- Initial API release
- Basic chat functionality
- JWT authentication
- Multiple response formats
- WebSocket support
- Rate limiting