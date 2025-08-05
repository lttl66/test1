package com.chatbot.service;

import com.chatbot.model.ChatMessage;
import com.chatbot.model.ChatSession;
import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.model.repository.ChatMessageRepository;
import com.chatbot.model.repository.ChatSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        try {
            // Get or create session
            ChatSession session = getOrCreateSession(request);
            
            // Get conversation history
            List<String> history = getConversationHistory(session.getSessionId());
            
            // Process message with AI service
            ChatResponse response = aiService.processMessage(request, history);
            
            // Save message and response
            saveMessageExchange(request, response, session);
            
            // Update session activity
            updateSessionActivity(session);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .message("I apologize, but I encountered an error. Please try again.")
                    .responseFormat(ChatMessage.ResponseFormat.TEXT)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
    
    public List<ChatMessage> getChatHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
    
    public List<ChatSession> getUserSessions(String userId) {
        return sessionRepository.findByUserIdAndActiveTrue(userId);
    }
    
    @Transactional
    public void endSession(String sessionId) {
        sessionRepository.deactivateSession(sessionId);
    }
    
    @Transactional
    public void clearHistory(String sessionId) {
        messageRepository.deleteBySessionId(sessionId);
    }
    
    @Transactional
    public void cleanupOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        sessionRepository.deactivateInactiveSessions(cutoff);
        
        LocalDateTime deleteCutoff = LocalDateTime.now().minusDays(30);
        sessionRepository.deleteByLastActivityBefore(deleteCutoff);
    }
    
    private ChatSession getOrCreateSession(ChatRequest request) {
        String sessionId = request.getSessionId();
        
        if (sessionId != null) {
            Optional<ChatSession> existingSession = sessionRepository.findBySessionIdAndActiveTrue(sessionId);
            if (existingSession.isPresent()) {
                return existingSession.get();
            }
        }
        
        // Create new session
        sessionId = UUID.randomUUID().toString();
        ChatSession newSession = ChatSession.builder()
                .sessionId(sessionId)
                .userId(request.getUserId())
                .context(serializeContext(request))
                .build();
        
        return sessionRepository.save(newSession);
    }
    
    private List<String> getConversationHistory(String sessionId) {
        List<ChatMessage> messages = messageRepository.findRecentMessagesBySession(
                sessionId, LocalDateTime.now().minusHours(24));
        
        return messages.stream()
                .map(msg -> String.format("User: %s\nAssistant: %s", msg.getMessage(), msg.getResponse()))
                .collect(Collectors.toList());
    }
    
    private void saveMessageExchange(ChatRequest request, ChatResponse response, ChatSession session) {
        try {
            ChatMessage message = ChatMessage.builder()
                    .sessionId(session.getSessionId())
                    .userId(session.getUserId())
                    .message(request.getMessage())
                    .response(response.getMessage())
                    .messageType(ChatMessage.MessageType.USER_QUERY)
                    .responseFormat(response.getResponseFormat())
                    .metadata(objectMapper.writeValueAsString(response.getMetadata()))
                    .build();
            
            messageRepository.save(message);
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing message metadata: {}", e.getMessage());
        }
    }
    
    private void updateSessionActivity(ChatSession session) {
        session.setLastActivity(LocalDateTime.now());
        sessionRepository.save(session);
    }
    
    private String serializeContext(ChatRequest request) {
        try {
            return objectMapper.writeValueAsString(request.getSystemContext());
        } catch (JsonProcessingException e) {
            log.error("Error serializing session context: {}", e.getMessage());
            return "{}";
        }
    }
}