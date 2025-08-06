package com.chatbot.config;

import com.chatbot.service.AIService;
import com.chatbot.service.impl.Qwen3ServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class Qwen3Config {
    
    /**
     * Configure Qwen3 service as the primary AI service
     * This bean will be used when ai.qwen3.enabled=true
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.qwen3.enabled", havingValue = "true", matchIfMissing = true)
    public AIService qwen3AIService() {
        return new Qwen3ServiceImpl();
    }
    
    /**
     * Alternative configuration for when Qwen3 is explicitly disabled
     */
    @Bean
    @ConditionalOnProperty(name = "ai.qwen3.enabled", havingValue = "false")
    public AIService fallbackAIService() {
        // Return a fallback service or throw an exception
        throw new IllegalStateException("Qwen3 AI service is disabled. Please enable it or configure an alternative AI service.");
    }
}