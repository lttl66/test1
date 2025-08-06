# Qwen3 AI Integration with System Data Processing

## Overview

This Java application provides a comprehensive integration with Qwen3's AI question-answering system, enabling intelligent analysis of system data. The integration can process system information and format the output in various formats (text, table, list, card) based on user queries.

## 🚀 Key Features

- **🤖 AI-Powered Analysis**: Qwen3 AI processes system data and provides intelligent insights
- **📊 System Data Collection**: Comprehensive collection of performance metrics, network configuration, JVM details, and process information
- **🎨 Multiple Response Formats**: Support for text, table, list, and card response formats
- **⏱️ Real-time Monitoring**: Continuous system monitoring with AI analysis
- **⚡ Command Execution**: Execute system commands and analyze output with AI
- **🌐 RESTful API**: Complete REST API for integration with other systems
- **🔒 Security**: Secure API key management and input validation
- **📈 Performance**: Optimized for high-performance system data processing

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Qwen3 API     │    │  System Data     │    │   REST API      │
│   Integration   │◄──►│   Processor      │◄──►│   Endpoints     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   AI Response   │    │   System Info    │    │   Client Apps   │
│   Formatter     │    │   Collector      │    │   & Frontend    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 📁 Project Structure

```
backend/
├── src/main/java/com/chatbot/
│   ├── service/
│   │   ├── impl/
│   │   │   └── Qwen3ServiceImpl.java          # Qwen3 AI integration
│   │   └── SystemDataProcessor.java           # System data collection
│   ├── controller/
│   │   └── Qwen3Controller.java               # REST API endpoints
│   ├── config/
│   │   └── Qwen3Config.java                   # Configuration
│   └── example/
│       └── Qwen3IntegrationExample.java       # Usage examples
├── src/test/java/com/chatbot/service/
│   └── Qwen3IntegrationTest.java              # Integration tests
├── src/main/resources/
│   └── application-qwen3.yml                  # Configuration
├── run-qwen3-integration.sh                   # Startup script
└── test-api.sh                               # API test script
```

## 🚀 Quick Start

### 1. Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Qwen3 API key

### 2. Setup

```bash
# Clone the repository
git clone <repository-url>
cd <repository-name>

# Set your Qwen3 API key
export QWEN3_API_KEY="your-qwen3-api-key"

# Make scripts executable
chmod +x backend/run-qwen3-integration.sh
chmod +x backend/test-api.sh
```

### 3. Run the Application

```bash
# Option 1: Use the startup script
./backend/run-qwen3-integration.sh

# Option 2: Manual startup
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=qwen3
```

### 4. Test the API

```bash
# Run the API test script
./backend/test-api.sh

# Or test individual endpoints
curl -X GET http://localhost:8080/api/qwen3/health
curl -X POST http://localhost:8080/api/qwen3/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Analyze system performance"}'
```

## 📚 API Endpoints

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/qwen3/chat` | Process chat messages with AI |
| `POST` | `/api/qwen3/system-info` | Get system information with AI analysis |
| `GET` | `/api/qwen3/performance` | Performance analysis |
| `GET` | `/api/qwen3/network` | Network analysis |
| `GET` | `/api/qwen3/jvm` | JVM analysis |
| `POST` | `/api/qwen3/execute-command` | Execute system commands |
| `GET` | `/api/qwen3/system-data` | Get raw system data |
| `GET` | `/api/qwen3/summary` | System summary |
| `GET` | `/api/qwen3/health` | Health check |

### Example Usage

#### 1. Basic Chat
```bash
curl -X POST http://localhost:8080/api/qwen3/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Analyze the system performance",
    "sessionId": "user-session-1"
  }'
```

#### 2. System Information Analysis
```bash
curl -X POST http://localhost:8080/api/qwen3/system-info \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Show me system information in table format",
    "dataType": "all"
  }'
```

#### 3. Command Execution
```bash
curl -X POST http://localhost:8080/api/qwen3/execute-command \
  -H "Content-Type: application/json" \
  -d '{
    "command": "ps aux | head -10"
  }'
```

## 🔧 Configuration

### Environment Variables

```bash
# Required
export QWEN3_API_KEY="your-qwen3-api-key"

# Optional
export QWEN3_MODEL="qwen-turbo"
export QWEN3_MAX_TOKENS="2048"
export QWEN3_TEMPERATURE="0.7"
```

### Application Properties

```yaml
ai:
  qwen3:
    api-key: ${QWEN3_API_KEY:your-qwen3-api-key-here}
    model: qwen-turbo
    max-tokens: 2048
    temperature: 0.7
    api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
```

## 📊 System Data Types

### Performance Metrics
- CPU usage and load average
- Memory usage (total, free, used, heap)
- Thread count and statistics
- System load information

### Network Information
- Network interfaces
- IP addresses
- MAC addresses
- MTU settings

### JVM Information
- Java version and vendor
- Memory settings
- JVM arguments
- Uptime information

### Process Information
- Process ID
- Start time
- CPU time
- User time

## 🎨 Response Formats

### Text Format
```json
{
  "message": "System analysis shows normal performance...",
  "responseFormat": "TEXT",
  "success": true
}
```

### Table Format
```json
{
  "message": "System information in table format",
  "responseFormat": "TABLE",
  "content": {
    "title": "System Data Overview",
    "headers": ["Property", "Value", "Description"],
    "rows": [
      ["CPU Usage", "45%", "Current CPU utilization"],
      ["Memory Usage", "2.1GB", "Current memory usage"]
    ]
  }
}
```

### List Format
```json
{
  "message": "System properties list",
  "responseFormat": "LIST",
  "content": {
    "title": "System Information",
    "items": [
      {
        "title": "CPU Cores",
        "subtitle": "8 cores available"
      }
    ]
  }
}
```

### Card Format
```json
{
  "message": "System summary card",
  "responseFormat": "CARD",
  "content": {
    "title": "System Status",
    "subtitle": "Overall Health: Good",
    "description": "System is running optimally...",
    "data": {
      "cpu_usage": "45%",
      "memory_usage": "2.1GB"
    }
  }
}
```

## 🧪 Testing

### Run Tests
```bash
cd backend
mvn test
```

### Manual Testing
```bash
# Test health endpoint
curl http://localhost:8080/api/qwen3/health

# Test system summary
curl http://localhost:8080/api/qwen3/summary

# Test performance analysis
curl http://localhost:8080/api/qwen3/performance
```

## 🔒 Security

- **API Key Management**: Secure storage using environment variables
- **Input Validation**: All user inputs validated before processing
- **Command Execution**: Limited and monitored system command execution
- **Data Privacy**: System data processed locally before sending to AI

## 📈 Performance Optimization

- **Caching**: System data caching to reduce collection overhead
- **Async Processing**: Long-running operations processed asynchronously
- **Batch Processing**: Multiple system queries batched
- **Connection Pooling**: HTTP connections pooled for API calls

## 🐛 Troubleshooting

### Common Issues

1. **API Key Issues**
   - Verify `QWEN3_API_KEY` environment variable is set
   - Check API key validity in Qwen3 dashboard

2. **System Data Collection Errors**
   - Ensure application has sufficient permissions
   - Check system resource availability

3. **Network Connectivity**
   - Verify internet connectivity for Qwen3 API calls
   - Check firewall settings

### Debug Mode
```yaml
logging:
  level:
    com.chatbot: DEBUG
```

## 📖 Examples

### Java Code Examples

```java
// Basic usage
ChatRequest request = new ChatRequest();
request.setMessage("Analyze system performance");
request.setSystemContext(systemDataProcessor.getSystemSummary());

ChatResponse response = aiService.processMessage(request, null);
System.out.println("AI Response: " + response.getMessage());

// Performance analysis
Map<String, Object> performanceData = systemDataProcessor.collectPerformanceMetrics();
ChatRequest perfRequest = new ChatRequest();
perfRequest.setMessage("Analyze performance and suggest optimizations");
perfRequest.setSystemContext(performanceData);

ChatResponse perfResponse = aiService.processMessage(perfRequest, null);
```

### Real-time Monitoring

```java
// Monitor system health
for (int i = 0; i < 10; i++) {
    Map<String, Object> currentData = systemDataProcessor.getSystemSummary();
    
    ChatRequest request = new ChatRequest();
    request.setMessage("Monitor system health and alert if there are issues");
    request.setSystemContext(currentData);
    
    ChatResponse response = aiService.processMessage(request, null);
    System.out.println("Monitoring Cycle " + (i + 1) + ": " + response.getMessage());
    
    Thread.sleep(5000); // 5 second intervals
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Qwen3 AI for providing the AI capabilities
- Spring Boot for the robust framework
- The open-source community for various dependencies

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the documentation in `docs/QWEN3_INTEGRATION.md`
- Review the example code in `backend/src/main/java/com/chatbot/example/`

---

**Happy coding with Qwen3 AI Integration! 🚀**