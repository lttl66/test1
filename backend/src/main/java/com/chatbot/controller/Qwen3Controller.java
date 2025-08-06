package com.chatbot.controller;

import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.service.AIService;
import com.chatbot.service.SystemDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qwen3")
@RequiredArgsConstructor
@Slf4j
public class Qwen3Controller {
    
    private final AIService aiService;
    private final SystemDataProcessor systemDataProcessor;
    
    /**
     * Process chat message with Qwen3 AI
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> processChat(@Valid @RequestBody ChatRequest request) {
        try {
            // If no system context provided, collect basic system info
            if (request.getSystemContext() == null || request.getSystemContext().isEmpty()) {
                Map<String, Object> systemContext = systemDataProcessor.getSystemSummary();
                request.setSystemContext(systemContext);
            }
            
            ChatResponse response = aiService.processMessage(request, null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing chat request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Failed to process request: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get system information with AI analysis
     */
    @PostMapping("/system-info")
    public ResponseEntity<ChatResponse> getSystemInfo(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.getOrDefault("query", "Show me system information");
            String dataType = (String) request.getOrDefault("dataType", "all");
            
            // Collect system data based on request
            Map<String, Object> systemData;
            if ("all".equals(dataType)) {
                systemData = systemDataProcessor.collectSystemInfo();
            } else {
                systemData = systemDataProcessor.collectSpecificData(dataType);
            }
            
            // Create chat request with system context
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setMessage(query);
            chatRequest.setSystemContext(systemData);
            
            ChatResponse response = aiService.processMessage(chatRequest, null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting system info: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Failed to get system info: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get system performance metrics with AI analysis
     */
    @GetMapping("/performance")
    public ResponseEntity<ChatResponse> getPerformanceAnalysis() {
        try {
            Map<String, Object> performanceData = systemDataProcessor.collectPerformanceMetrics();
            
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the system performance and provide insights");
            request.setSystemContext(performanceData);
            
            ChatResponse response = aiService.processMessage(request, null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing performance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Failed to analyze performance: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get network information with AI analysis
     */
    @GetMapping("/network")
    public ResponseEntity<ChatResponse> getNetworkAnalysis() {
        try {
            Map<String, Object> networkData = systemDataProcessor.collectNetworkInfo();
            
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the network configuration and provide details");
            request.setSystemContext(networkData);
            
            ChatResponse response = aiService.processMessage(request, null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing network: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Failed to analyze network: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get JVM information with AI analysis
     */
    @GetMapping("/jvm")
    public ResponseEntity<ChatResponse> getJvmAnalysis() {
        try {
            Map<String, Object> jvmData = systemDataProcessor.collectJvmInfo();
            
            ChatRequest request = new ChatRequest();
            request.setMessage("Analyze the JVM configuration and provide insights");
            request.setSystemContext(jvmData);
            
            ChatResponse response = aiService.processMessage(request, null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing JVM: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Failed to analyze JVM: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Execute system command and get AI analysis
     */
    @PostMapping("/execute-command")
    public ResponseEntity<ChatResponse> executeCommand(@RequestBody Map<String, String> request) {
        try {
            String command = request.get("command");
            if (command == null || command.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ChatResponse.builder()
                                .success(false)
                                .error("Command is required")
                                .build());
            }
            
            // Execute command
            String output = systemDataProcessor.executeSystemCommand(command);
            
            // Create context with command output
            Map<String, Object> context = new HashMap<>();
            context.put("command", command);
            context.put("output", output);
            context.put("timestamp", System.currentTimeMillis());
            
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setMessage("Analyze the output of the executed command: " + command);
            chatRequest.setSystemContext(context);
            
            ChatResponse response = aiService.processMessage(chatRequest, null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error executing command: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Failed to execute command: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get formatted system data for AI processing
     */
    @GetMapping("/system-data")
    public ResponseEntity<Map<String, Object>> getSystemData(@RequestParam(defaultValue = "all") String type) {
        try {
            Map<String, Object> systemData;
            if ("all".equals(type)) {
                systemData = systemDataProcessor.collectSystemInfo();
            } else {
                systemData = systemDataProcessor.collectSpecificData(type);
            }
            
            return ResponseEntity.ok(systemData);
            
        } catch (Exception e) {
            log.error("Error getting system data: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get system data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get system summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSystemSummary() {
        try {
            Map<String, Object> summary = systemDataProcessor.getSystemSummary();
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting system summary: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get system summary: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Qwen3 AI Integration");
        health.put("timestamp", System.currentTimeMillis());
        
        try {
            // Quick system check
            Map<String, Object> summary = systemDataProcessor.getSystemSummary();
            health.put("system_status", "OK");
            health.put("memory_usage", summary.get("memory_usage_percent"));
        } catch (Exception e) {
            health.put("system_status", "ERROR");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}