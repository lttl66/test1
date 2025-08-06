# Qwen3 AI Integration with System Data Processing

This Java application integrates with Qwen3's AI question-answering system to provide intelligent analysis of system data. The integration can process system information and format the output in various formats (text, table, list, card) based on user queries.

## Features

- **System Data Collection**: Comprehensive collection of system information including performance metrics, network configuration, JVM details, and process information
- **AI-Powered Analysis**: Qwen3 AI processes system data and provides intelligent insights
- **Multiple Response Formats**: Support for text, table, list, and card response formats
- **Real-time Monitoring**: Continuous system monitoring with AI analysis
- **Command Execution**: Execute system commands and analyze output with AI
- **RESTful API**: Complete REST API for integration with other systems

## Architecture

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

## Components

### 1. Qwen3ServiceImpl
Main service that integrates with Qwen3 AI API:
- Processes user queries with system context
- Analyzes message intent and data requirements
- Formats responses in appropriate formats
- Handles error scenarios gracefully

### 2. SystemDataProcessor
Service for collecting and processing system information:
- Performance metrics (CPU, memory, threads)
- Network configuration
- JVM information
- Process details
- System command execution

### 3. Qwen3Controller
REST API endpoints for:
- Chat processing with system context
- System information analysis
- Performance monitoring
- Network analysis
- JVM analysis
- Command execution and analysis

## Configuration

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

## API Endpoints

### 1. Chat Processing
```http
POST /api/qwen3/chat
Content-Type: application/json

{
  "message": "Analyze system performance",
  "sessionId": "optional-session-id",
  "userId": "optional-user-id",
  "systemContext": {
    "custom_data": "value"
  }
}
```

### 2. System Information Analysis
```http
POST /api/qwen3/system-info
Content-Type: application/json

{
  "query": "Show me system information",
  "dataType": "all"
}
```

### 3. Performance Analysis
```http
GET /api/qwen3/performance
```

### 4. Network Analysis
```http
GET /api/qwen3/network
```

### 5. JVM Analysis
```http
GET /api/qwen3/jvm
```

### 6. Command Execution
```http
POST /api/qwen3/execute-command
Content-Type: application/json

{
  "command": "ps aux | head -10"
}
```

### 7. System Data
```http
GET /api/qwen3/system-data?type=all
GET /api/qwen3/system-data?type=performance
GET /api/qwen3/system-data?type=network
GET /api/qwen3/system-data?type=jvm
```

### 8. System Summary
```http
GET /api/qwen3/summary
```

### 9. Health Check
```http
GET /api/qwen3/health
```

## Usage Examples

### 1. Basic Chat with System Context
```java
ChatRequest request = new ChatRequest();
request.setMessage("Analyze the system performance");
request.setSystemContext(systemDataProcessor.getSystemSummary());

ChatResponse response = aiService.processMessage(request, null);
System.out.println("AI Response: " + response.getMessage());
```

### 2. Performance Analysis
```java
Map<String, Object> performanceData = systemDataProcessor.collectPerformanceMetrics();

ChatRequest request = new ChatRequest();
request.setMessage("Analyze performance and suggest optimizations");
request.setSystemContext(performanceData);

ChatResponse response = aiService.processMessage(request, null);
```

### 3. Command Execution and Analysis
```java
String command = "ps aux | head -10";
String output = systemDataProcessor.executeSystemCommand(command);

Map<String, Object> context = new HashMap<>();
context.put("command", command);
context.put("output", output);

ChatRequest request = new ChatRequest();
request.setMessage("Analyze the process list");
request.setSystemContext(context);

ChatResponse response = aiService.processMessage(request, null);
```

## Response Formats

The system supports multiple response formats:

### 1. Text Format
```json
{
  "message": "System analysis shows normal performance...",
  "responseFormat": "TEXT",
  "success": true
}
```

### 2. Table Format
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

### 3. List Format
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
      },
      {
        "title": "Memory",
        "subtitle": "16GB total"
      }
    ]
  }
}
```

### 4. Card Format
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

## System Data Types

### 1. Performance Metrics
- CPU usage and load average
- Memory usage (total, free, used, heap)
- Thread count and statistics
- System load information

### 2. Network Information
- Network interfaces
- IP addresses
- MAC addresses
- MTU settings

### 3. JVM Information
- Java version and vendor
- Memory settings
- JVM arguments
- Uptime information

### 4. Process Information
- Process ID
- Start time
- CPU time
- User time

## Error Handling

The system includes comprehensive error handling:

```java
try {
    ChatResponse response = aiService.processMessage(request, null);
    // Process successful response
} catch (Exception e) {
    // Handle error scenarios
    log.error("Error processing request: {}", e.getMessage());
}
```

## Security Considerations

1. **API Key Management**: Store Qwen3 API keys securely using environment variables
2. **Input Validation**: All user inputs are validated before processing
3. **Command Execution**: System command execution is limited and monitored
4. **Data Privacy**: System data is processed locally before sending to AI

## Monitoring and Logging

The system includes comprehensive logging:

```yaml
logging:
  level:
    com.chatbot.service.impl.Qwen3ServiceImpl: DEBUG
    com.chatbot.service.SystemDataProcessor: INFO
    com.chatbot.controller.Qwen3Controller: INFO
```

## Performance Optimization

1. **Caching**: System data can be cached to reduce collection overhead
2. **Async Processing**: Long-running operations can be processed asynchronously
3. **Batch Processing**: Multiple system queries can be batched
4. **Connection Pooling**: HTTP connections are pooled for API calls

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:11-jre-slim
COPY target/ai-chatbot-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Setup
```bash
# Set required environment variables
export QWEN3_API_KEY="your-api-key"

# Run the application
java -jar target/ai-chatbot-backend-1.0.0.jar
```

## Testing

### Unit Tests
```java
@Test
public void testSystemDataCollection() {
    Map<String, Object> data = systemDataProcessor.collectSystemInfo();
    assertNotNull(data);
    assertTrue(data.containsKey("basic"));
    assertTrue(data.containsKey("performance"));
}
```

### Integration Tests
```java
@Test
public void testQwen3Integration() {
    ChatRequest request = new ChatRequest();
    request.setMessage("Test message");
    
    ChatResponse response = aiService.processMessage(request, null);
    assertTrue(response.isSuccess());
}
```

## Troubleshooting

### Common Issues

1. **API Key Issues**
   - Verify QWEN3_API_KEY environment variable is set
   - Check API key validity in Qwen3 dashboard

2. **System Data Collection Errors**
   - Ensure application has sufficient permissions
   - Check system resource availability

3. **Network Connectivity**
   - Verify internet connectivity for Qwen3 API calls
   - Check firewall settings

### Debug Mode
Enable debug logging for detailed troubleshooting:

```yaml
logging:
  level:
    com.chatbot: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.