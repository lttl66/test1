package com.chatbot.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Message cannot be blank")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;
    
    private String sessionId;
    
    private String userId;
    
    private String currentPage; // Current page context
    
    private Map<String, Object> systemContext; // System data context
    
    private Map<String, Object> userPreferences; // User preferences
}