package com.chatbot.controller;

import com.chatbot.model.ChatMessage;
import com.chatbot.model.ChatSession;
import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {
    
    private final ChatService chatService;
    
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        // Set user ID from authentication if not provided
        if (request.getUserId() == null && authentication != null) {
            request.setUserId(authentication.getName());
        }
        
        log.info("Processing chat message from user: {}, session: {}", 
                request.getUserId(), request.getSessionId());
        
        ChatResponse response = chatService.processMessage(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        log.info("Retrieving chat history for session: {}", sessionId);
        
        List<ChatMessage> history = chatService.getChatHistory(sessionId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getUserSessions(
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Retrieving sessions for user: {}", userId);
        
        List<ChatSession> sessions = chatService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
    @PostMapping("/session/{sessionId}/end")
    public ResponseEntity<Map<String, String>> endSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        log.info("Ending session: {}", sessionId);
        
        chatService.endSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session ended successfully"));
    }
    
    @DeleteMapping("/session/{sessionId}/history")
    public ResponseEntity<Map<String, String>> clearHistory(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        log.info("Clearing history for session: {}", sessionId);
        
        chatService.clearHistory(sessionId);
        return ResponseEntity.ok(Map.of("message", "History cleared successfully"));
    }
    
    @PostMapping("/public/demo")
    public ResponseEntity<ChatResponse> demoMessage(@Valid @RequestBody ChatRequest request) {
        // Demo endpoint that doesn't require authentication
        request.setUserId("demo-user");
        
        log.info("Processing demo message: {}", request.getMessage());
        
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "AI Chatbot API",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}