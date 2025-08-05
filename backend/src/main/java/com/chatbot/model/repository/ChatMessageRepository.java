package com.chatbot.model.repository;

import com.chatbot.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);
    
    List<ChatMessage> findByUserIdOrderByTimestampDesc(String userId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sessionId = :sessionId AND cm.timestamp >= :since ORDER BY cm.timestamp ASC")
    List<ChatMessage> findRecentMessagesBySession(@Param("sessionId") String sessionId, @Param("since") LocalDateTime since);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.userId = :userId AND cm.timestamp >= :since ORDER BY cm.timestamp DESC")
    List<ChatMessage> findRecentMessagesByUser(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    void deleteBySessionId(String sessionId);
    
    void deleteByUserIdAndTimestampBefore(String userId, LocalDateTime before);
}