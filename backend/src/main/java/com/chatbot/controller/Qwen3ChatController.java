package com.chatbot.controller;

import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.service.impl.Qwen3ServiceImpl;
import com.chatbot.service.SystemDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/v1/qwen3")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class Qwen3ChatController {

    private final Qwen3ServiceImpl qwen3Service;
    private final SystemDataProcessor systemDataProcessor;

    /**
     * Process a chat message using Qwen3 AI with system context
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received Qwen3 chat request from user: {}", request.getUserId());
            
            // Process the message with Qwen3
            List<String> conversationHistory = new ArrayList<>(); // In real app, fetch from database
            ChatResponse response = qwen3Service.processMessage(request, conversationHistory);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing Qwen3 chat request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .message("Failed to process your request. Please try again.")
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }

    /**
     * Generate contextual response based on system data
     */
    @PostMapping("/contextual")
    public ResponseEntity<ChatResponse> generateContextualResponse(
            @RequestParam String message,
            @RequestBody Map<String, Object> systemContext) {
        
        try {
            log.info("Generating contextual response for message: {}", message);
            
            ChatResponse response = qwen3Service.generateContextualResponse(message, systemContext);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating contextual response: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ChatResponse.builder()
                    .message("Failed to generate contextual response.")
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }

    /**
     * Analyze message intent and suggest response format
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeMessage(
            @RequestParam String message,
            @RequestBody(required = false) Map<String, Object> context) {
        
        try {
            Map<String, Object> analysis = qwen3Service.analyzeMessage(message, context);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Process system data for AI consumption
     */
    @PostMapping("/process-system-data")
    public ResponseEntity<Map<String, Object>> processSystemData(
            @RequestBody Map<String, Object> systemContext,
            @RequestParam(required = false) String intent) {
        
        try {
            Map<String, Object> processedData = systemDataProcessor.processSystemData(systemContext, intent);
            return ResponseEntity.ok(processedData);
            
        } catch (Exception e) {
            log.error("Error processing system data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get system data analysis
     */
    @PostMapping("/analyze-context")
    public ResponseEntity<Map<String, Object>> analyzeContext(
            @RequestBody Map<String, Object> systemContext) {
        
        try {
            Map<String, Object> analysis = systemDataProcessor.analyzeContext(systemContext);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing context: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Format response content in different formats
     */
    @PostMapping("/format-response")
    public ResponseEntity<ChatResponse> formatResponse(
            @RequestParam String message,
            @RequestBody Object content,
            @RequestParam String format) {
        
        try {
            ChatResponse response = qwen3Service.formatResponse(message, content, format);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error formatting response: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ChatResponse.builder()
                    .message("Failed to format response.")
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }

    /**
     * Demo endpoint with sample system data
     */
    @GetMapping("/demo")
    public ResponseEntity<ChatResponse> demo(@RequestParam(defaultValue = "What is the system status?") String message) {
        try {
            // Create sample system context
            Map<String, Object> systemContext = createSampleSystemContext();
            
            ChatRequest demoRequest = new ChatRequest();
            demoRequest.setMessage(message);
            demoRequest.setSystemContext(systemContext);
            demoRequest.setUserId("demo-user");
            demoRequest.setSessionId("demo-session");
            demoRequest.setCurrentPage("/dashboard");
            
            ChatResponse response = qwen3Service.processMessage(demoRequest, new ArrayList<>());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in demo endpoint: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ChatResponse.builder()
                    .message("Demo failed: " + e.getMessage())
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Qwen3 AI Integration");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    /**
     * Create sample system context for demonstration
     */
    private Map<String, Object> createSampleSystemContext() {
        Map<String, Object> context = new HashMap<>();
        
        // System information
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("status", "Running");
        systemInfo.put("uptime", "5 days, 3 hours");
        systemInfo.put("version", "2.1.0");
        systemInfo.put("environment", "Production");
        context.put("system", systemInfo);
        
        // Performance metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpu_usage", 45.2);
        metrics.put("memory_usage", 67.8);
        metrics.put("disk_usage", 23.1);
        metrics.put("active_connections", 156);
        context.put("metrics", metrics);
        
        // User data
        List<Map<String, Object>> users = Arrays.asList(
            Map.of("id", 1, "name", "John Doe", "role", "Admin", "status", "Active"),
            Map.of("id", 2, "name", "Jane Smith", "role", "User", "status", "Active"),
            Map.of("id", 3, "name", "Bob Johnson", "role", "Moderator", "status", "Inactive")
        );
        context.put("users", users);
        
        // Recent activities
        List<Map<String, Object>> activities = Arrays.asList(
            Map.of("user", "John Doe", "action", "User Login", "timestamp", "2024-01-15 10:30:00"),
            Map.of("user", "Jane Smith", "action", "Data Export", "timestamp", "2024-01-15 10:25:00"),
            Map.of("user", "System", "action", "Backup Completed", "timestamp", "2024-01-15 10:00:00")
        );
        context.put("activities", activities);
        
        // Alerts
        List<Map<String, Object>> alerts = Arrays.asList(
            Map.of("level", "WARNING", "message", "High memory usage detected", "timestamp", "2024-01-15 09:45:00"),
            Map.of("level", "INFO", "message", "Scheduled backup completed", "timestamp", "2024-01-15 10:00:00")
        );
        context.put("alerts", alerts);
        
        return context;
    }
}