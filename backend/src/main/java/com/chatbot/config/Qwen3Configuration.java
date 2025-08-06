package com.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qwen3")
@Data
public class Qwen3Configuration {

    /**
     * Qwen3 API endpoint URL
     */
    private String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    /**
     * API key for authentication
     */
    private String apiKey;

    /**
     * Model name to use for text generation
     */
    private String modelName = "qwen-turbo";

    /**
     * Temperature for response generation (0.0 to 2.0)
     */
    private Double temperature = 0.7;

    /**
     * Maximum number of tokens in the response
     */
    private Integer maxTokens = 1000;

    /**
     * Maximum number of tokens for analysis requests
     */
    private Integer analysisMaxTokens = 150;

    /**
     * System prompt for general responses
     */
    private String systemPrompt = "You are an intelligent assistant for a backend management system. " +
            "Provide helpful, accurate, and contextual responses based on the system data provided. " +
            "Format your responses appropriately for the user interface - use clear, professional language " +
            "and structure information logically. When dealing with system data, highlight key information " +
            "and provide actionable insights when possible.";

    /**
     * System prompt for message analysis
     */
    private String analysisSystemPrompt = "You are an expert at analyzing user messages to determine intent " +
            "and appropriate response formats. Analyze the user's message and respond with a JSON object " +
            "containing: intent (the user's primary intention), format (best response format: text, card, list, or table), " +
            "and confidence (0.0-1.0 confidence score). Focus on system administration, data queries, " +
            "and management interface needs.";

    /**
     * Default timeout for API requests in milliseconds
     */
    private Integer requestTimeout = 30000;

    /**
     * Maximum retry attempts for failed API calls
     */
    private Integer maxRetries = 3;

    /**
     * Whether to enable response caching
     */
    private Boolean enableCaching = true;

    /**
     * Cache TTL in seconds
     */
    private Integer cacheTtlSeconds = 300;

    /**
     * Whether to log API requests and responses (for debugging)
     */
    private Boolean enableLogging = false;

    /**
     * Maximum length of conversation history to include in requests
     */
    private Integer maxHistoryLength = 5;

    /**
     * Available response formats
     */
    public enum ResponseFormat {
        TEXT, CARD, LIST, TABLE, CHART
    }

    /**
     * Available message intents
     */
    public enum MessageIntent {
        GENERAL_QUERY,
        SYSTEM_DATA_QUERY,
        USER_MANAGEMENT,
        SYSTEM_STATUS,
        REPORT_GENERATION,
        LIST_QUERY,
        TABLE_QUERY,
        NAVIGATION_HELP,
        TROUBLESHOOTING
    }

    /**
     * Get the appropriate model name based on request type
     */
    public String getModelForRequest(String requestType) {
        switch (requestType.toLowerCase()) {
            case "analysis":
                return "qwen-turbo"; // Fast model for analysis
            case "complex_query":
                return "qwen-plus"; // More capable model for complex queries
            case "system_data":
                return "qwen-max"; // Most capable model for system data processing
            default:
                return modelName;
        }
    }

    /**
     * Get temperature setting based on request type
     */
    public Double getTemperatureForRequest(String requestType) {
        switch (requestType.toLowerCase()) {
            case "analysis":
                return 0.1; // Low temperature for consistent analysis
            case "creative":
                return 1.0; // Higher temperature for creative responses
            case "system_data":
                return 0.3; // Low temperature for factual data responses
            default:
                return temperature;
        }
    }

    /**
     * Get max tokens based on request type
     */
    public Integer getMaxTokensForRequest(String requestType) {
        switch (requestType.toLowerCase()) {
            case "analysis":
                return analysisMaxTokens;
            case "summary":
                return 500;
            case "detailed_response":
                return 2000;
            default:
                return maxTokens;
        }
    }

    /**
     * Validate configuration settings
     */
    public boolean isValidConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            return false;
        }
        if (temperature < 0.0 || temperature > 2.0) {
            return false;
        }
        if (maxTokens <= 0 || maxTokens > 4000) {
            return false;
        }
        return true;
    }

    /**
     * Get headers for API requests
     */
    public java.util.Map<String, String> getApiHeaders() {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");
        headers.put("X-DashScope-SSE", "disable");
        return headers;
    }
}