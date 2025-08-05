package com.chatbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sessionId;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseFormat responseFormat;
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    public enum MessageType {
        USER_QUERY,
        SYSTEM_INFO,
        AI_RESPONSE,
        ERROR
    }
    
    public enum ResponseFormat {
        TEXT,
        CARD,
        LIST,
        TABLE,
        RICH_MEDIA,
        CUSTOM
    }
}