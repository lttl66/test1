package com.chatbot.model.repository;

import com.chatbot.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    
    List<ChatSession> findByUserIdAndActiveTrue(String userId);
    
    Optional<ChatSession> findBySessionIdAndActiveTrue(String sessionId);
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId AND cs.active = true ORDER BY cs.lastActivity DESC")
    List<ChatSession> findActiveSessionsByUser(@Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE ChatSession cs SET cs.active = false WHERE cs.lastActivity < :cutoffTime")
    int deactivateInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Modifying
    @Query("UPDATE ChatSession cs SET cs.active = false WHERE cs.sessionId = :sessionId")
    int deactivateSession(@Param("sessionId") String sessionId);
    
    void deleteByLastActivityBefore(LocalDateTime before);
}