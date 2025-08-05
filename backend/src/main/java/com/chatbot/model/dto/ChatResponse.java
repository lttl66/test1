package com.chatbot.model.dto;

import com.chatbot.model.ChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    
    private String sessionId;
    
    private String message;
    
    private ChatMessage.ResponseFormat responseFormat;
    
    private Object content; // Dynamic content based on response format
    
    private LocalDateTime timestamp;
    
    private boolean success;
    
    private String error;
    
    private Map<String, Object> metadata;
    
    private List<ActionButton> suggestedActions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionButton {
        private String label;
        private String action;
        private String type; // 'link', 'function', 'navigation'
        private Map<String, Object> parameters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CardContent {
        private String title;
        private String subtitle;
        private String description;
        private String imageUrl;
        private List<ActionButton> actions;
        private Map<String, Object> data;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListContent {
        private String title;
        private List<ListItem> items;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ListItem {
            private String title;
            private String subtitle;
            private String description;
            private String iconUrl;
            private Map<String, Object> data;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TableContent {
        private String title;
        private List<String> headers;
        private List<List<Object>> rows;
        private Map<String, Object> options;
    }
}