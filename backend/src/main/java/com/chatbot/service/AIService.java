package com.chatbot.service;

import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;

import java.util.List;
import java.util.Map;

public interface AIService {
    
    /**
     * Process a chat message and generate AI response
     */
    ChatResponse processMessage(ChatRequest request, List<String> conversationHistory);
    
    /**
     * Generate a response based on system context
     */
    ChatResponse generateContextualResponse(String message, Map<String, Object> systemContext);
    
    /**
     * Format response content based on the detected intent and data
     */
    ChatResponse formatResponse(String message, Object content, String format);
    
    /**
     * Analyze user message to determine response format and content type
     */
    Map<String, Object> analyzeMessage(String message, Map<String, Object> context);
}