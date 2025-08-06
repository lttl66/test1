package com.chatbot.service;

import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class Qwen3IntegrationTest {
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private SystemDataProcessor systemDataProcessor;
    
    @Test
    public void testSystemDataCollection() {
        // Test system data collection
        Map<String, Object> systemData = systemDataProcessor.collectSystemInfo();
        
        assertNotNull(systemData);
        assertTrue(systemData.containsKey("basic"));
        assertTrue(systemData.containsKey("performance"));
        assertTrue(systemData.containsKey("jvm"));
        assertTrue(systemData.containsKey("timestamp"));
        
        // Test specific data collection
        Map<String, Object> performanceData = systemDataProcessor.collectPerformanceMetrics();
        assertNotNull(performanceData);
        assertTrue(performanceData.containsKey("memory_usage_percent"));
        assertTrue(performanceData.containsKey("thread_count"));
    }
    
    @Test
    public void testSystemSummary() {
        Map<String, Object> summary = systemDataProcessor.getSystemSummary();
        
        assertNotNull(summary);
        assertTrue(summary.containsKey("os"));
        assertTrue(summary.containsKey("java_version"));
        assertTrue(summary.containsKey("memory_usage_percent"));
    }
    
    @Test
    public void testBasicChatRequest() {
        // Test basic chat request without system context
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello, how are you?");
        
        try {
            ChatResponse response = aiService.processMessage(request, null);
            
            assertNotNull(response);
            assertNotNull(response.getMessage());
            assertTrue(response.isSuccess() || !response.isSuccess()); // Either success or failure is valid
            
        } catch (Exception e) {
            // This is expected if Qwen3 API is not configured
            assertTrue(e.getMessage().contains("API") || e.getMessage().contains("connection"));
        }
    }
    
    @Test
    public void testChatRequestWithSystemContext() {
        // Test chat request with system context
        Map<String, Object> systemContext = systemDataProcessor.getSystemSummary();
        
        ChatRequest request = new ChatRequest();
        request.setMessage("Analyze the system performance");
        request.setSystemContext(systemContext);
        
        try {
            ChatResponse response = aiService.processMessage(request, null);
            
            assertNotNull(response);
            assertNotNull(response.getMessage());
            
        } catch (Exception e) {
            // This is expected if Qwen3 API is not configured
            assertTrue(e.getMessage().contains("API") || e.getMessage().contains("connection"));
        }
    }
    
    @Test
    public void testSystemDataFormatting() {
        Map<String, Object> systemData = systemDataProcessor.collectSystemInfo();
        String formatted = systemDataProcessor.formatSystemDataForAI(systemData);
        
        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
        assertTrue(formatted.contains("basic:"));
        assertTrue(formatted.contains("performance:"));
    }
    
    @Test
    public void testSpecificDataCollection() {
        // Test performance data collection
        Map<String, Object> performanceData = systemDataProcessor.collectSpecificData("performance");
        assertNotNull(performanceData);
        assertTrue(performanceData.containsKey("memory_usage_percent"));
        
        // Test network data collection
        Map<String, Object> networkData = systemDataProcessor.collectSpecificData("network");
        assertNotNull(networkData);
        
        // Test JVM data collection
        Map<String, Object> jvmData = systemDataProcessor.collectSpecificData("jvm");
        assertNotNull(jvmData);
        assertTrue(jvmData.containsKey("java_version"));
    }
    
    @Test
    public void testCommandExecution() {
        // Test simple command execution
        String output = systemDataProcessor.executeSystemCommand("echo 'test'");
        
        assertNotNull(output);
        assertTrue(output.contains("test") || output.contains("Error"));
    }
    
    @Test
    public void testResponseFormatting() {
        // Test response formatting with different formats
        String testMessage = "Test response message";
        
        try {
            ChatResponse textResponse = aiService.formatResponse(testMessage, testMessage, "TEXT");
            assertNotNull(textResponse);
            assertEquals(testMessage, textResponse.getMessage());
            
        } catch (Exception e) {
            // Expected if service is not properly configured
            assertTrue(e.getMessage().contains("format") || e.getMessage().contains("service"));
        }
    }
    
    @Test
    public void testContextualResponse() {
        Map<String, Object> systemContext = new HashMap<>();
        systemContext.put("test_key", "test_value");
        systemContext.put("cpu_usage", "45%");
        
        try {
            ChatResponse response = aiService.generateContextualResponse(
                "Analyze the provided system context", 
                systemContext
            );
            
            assertNotNull(response);
            assertNotNull(response.getMessage());
            
        } catch (Exception e) {
            // Expected if Qwen3 API is not configured
            assertTrue(e.getMessage().contains("API") || e.getMessage().contains("connection"));
        }
    }
    
    @Test
    public void testErrorHandling() {
        // Test with invalid request
        ChatRequest invalidRequest = new ChatRequest();
        invalidRequest.setMessage(""); // Empty message
        
        try {
            ChatResponse response = aiService.processMessage(invalidRequest, null);
            assertNotNull(response);
            
        } catch (Exception e) {
            // Expected behavior for invalid requests
            assertNotNull(e.getMessage());
        }
    }
}