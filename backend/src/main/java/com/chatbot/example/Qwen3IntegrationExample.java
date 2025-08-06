package com.chatbot.example;

import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.service.AIService;
import com.chatbot.service.SystemDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating Qwen3 AI integration with system data processing
 * 
 * This class shows how to:
 * 1. Collect system information
 * 2. Process it with Qwen3 AI
 * 3. Get formatted responses
 * 4. Handle different types of system queries
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Qwen3IntegrationExample {
    
    private final AIService aiService;
    private final SystemDataProcessor systemDataProcessor;
    
    /**
     * Example: Get system performance analysis
     */
    public void demonstratePerformanceAnalysis() {
        log.info("=== System Performance Analysis Example ===");
        
        try {
            // Collect performance data
            Map<String, Object> performanceData = systemDataProcessor.collectPerformanceMetrics();
            
            // Create chat request
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the system performance and provide recommendations for optimization");
            request.setSystemContext(performanceData);
            
            // Process with Qwen3 AI
            ChatResponse response = aiService.processMessage(request, null);
            
            log.info("AI Response: {}", response.getMessage());
            log.info("Response Format: {}", response.getResponseFormat());
            log.info("Success: {}", response.isSuccess());
            
        } catch (Exception e) {
            log.error("Error in performance analysis example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example: Get network configuration analysis
     */
    public void demonstrateNetworkAnalysis() {
        log.info("=== Network Configuration Analysis Example ===");
        
        try {
            // Collect network data
            Map<String, Object> networkData = systemDataProcessor.collectNetworkInfo();
            
            // Create chat request
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the network configuration and identify any potential issues");
            request.setSystemContext(networkData);
            
            // Process with Qwen3 AI
            ChatResponse response = aiService.processMessage(request, null);
            
            log.info("AI Response: {}", response.getMessage());
            log.info("Response Format: {}", response.getResponseFormat());
            
        } catch (Exception e) {
            log.error("Error in network analysis example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example: Get JVM analysis
     */
    public void demonstrateJvmAnalysis() {
        log.info("=== JVM Analysis Example ===");
        
        try {
            // Collect JVM data
            Map<String, Object> jvmData = systemDataProcessor.collectJvmInfo();
            
            // Create chat request
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the JVM configuration and suggest memory optimization strategies");
            request.setSystemContext(jvmData);
            
            // Process with Qwen3 AI
            ChatResponse response = aiService.processMessage(request, null);
            
            log.info("AI Response: {}", response.getMessage());
            log.info("Response Format: {}", response.getResponseFormat());
            
        } catch (Exception e) {
            log.error("Error in JVM analysis example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example: Execute system command and analyze output
     */
    public void demonstrateCommandExecution() {
        log.info("=== Command Execution Analysis Example ===");
        
        try {
            // Execute a system command
            String command = "ps aux | head -10";
            String output = systemDataProcessor.executeSystemCommand(command);
            
            // Create context with command output
            Map<String, Object> context = new HashMap<>();
            context.put("command", command);
            context.put("output", output);
            context.put("timestamp", System.currentTimeMillis());
            
            // Create chat request
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the process list and identify any unusual processes");
            request.setSystemContext(context);
            
            // Process with Qwen3 AI
            ChatResponse response = aiService.processMessage(request, null);
            
            log.info("AI Response: {}", response.getMessage());
            log.info("Response Format: {}", response.getResponseFormat());
            
        } catch (Exception e) {
            log.error("Error in command execution example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example: Get comprehensive system overview
     */
    public void demonstrateSystemOverview() {
        log.info("=== System Overview Example ===");
        
        try {
            // Collect comprehensive system data
            Map<String, Object> systemData = systemDataProcessor.collectSystemInfo();
            
            // Create chat request
            ChatRequest request = new ChatRequest();
            request.setMessage("Provide a comprehensive overview of the system status and health");
            request.setSystemContext(systemData);
            
            // Process with Qwen3 AI
            ChatResponse response = aiService.processMessage(request, null);
            
            log.info("AI Response: {}", response.getMessage());
            log.info("Response Format: {}", response.getResponseFormat());
            
        } catch (Exception e) {
            log.error("Error in system overview example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example: Format-specific queries
     */
    public void demonstrateFormatSpecificQueries() {
        log.info("=== Format-Specific Queries Example ===");
        
        try {
            Map<String, Object> systemData = systemDataProcessor.collectSystemInfo();
            
            // Table format request
            ChatRequest tableRequest = new ChatRequest();
            tableRequest.setMessage("Show system information in a table format");
            tableRequest.setSystemContext(systemData);
            
            ChatResponse tableResponse = aiService.processMessage(tableRequest, null);
            log.info("Table Response Format: {}", tableResponse.getResponseFormat());
            
            // List format request
            ChatRequest listRequest = new ChatRequest();
            listRequest.setMessage("List all system properties");
            listRequest.setSystemContext(systemData);
            
            ChatResponse listResponse = aiService.processMessage(listRequest, null);
            log.info("List Response Format: {}", listResponse.getResponseFormat());
            
            // Card format request
            ChatRequest cardRequest = new ChatRequest();
            cardRequest.setMessage("Show system summary in a card format");
            cardRequest.setSystemContext(systemData);
            
            ChatResponse cardResponse = aiService.processMessage(cardRequest, null);
            log.info("Card Response Format: {}", cardResponse.getResponseFormat());
            
        } catch (Exception e) {
            log.error("Error in format-specific queries example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Example: Real-time monitoring simulation
     */
    public void demonstrateRealTimeMonitoring() {
        log.info("=== Real-Time Monitoring Example ===");
        
        try {
            // Simulate real-time data collection
            for (int i = 0; i < 3; i++) {
                Map<String, Object> currentData = systemDataProcessor.getSystemSummary();
                
                ChatRequest request = new ChatRequest();
                request.setMessage("Monitor system health and alert if there are any issues");
                request.setSystemContext(currentData);
                
                ChatResponse response = aiService.processMessage(request, null);
                
                log.info("Monitoring Cycle {}: {}", i + 1, response.getMessage());
                
                // Simulate time delay
                Thread.sleep(2000);
            }
            
        } catch (Exception e) {
            log.error("Error in real-time monitoring example: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Run all examples
     */
    public void runAllExamples() {
        log.info("Starting Qwen3 Integration Examples...");
        
        demonstratePerformanceAnalysis();
        demonstrateNetworkAnalysis();
        demonstrateJvmAnalysis();
        demonstrateCommandExecution();
        demonstrateSystemOverview();
        demonstrateFormatSpecificQueries();
        demonstrateRealTimeMonitoring();
        
        log.info("All examples completed!");
    }
}