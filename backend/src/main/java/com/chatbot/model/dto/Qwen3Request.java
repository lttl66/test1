package com.chatbot.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qwen3Request {

    /**
     * Model name to use for the request
     */
    private String model;

    /**
     * List of messages in the conversation
     */
    private List<Qwen3Message> messages;

    /**
     * Temperature setting for response generation (0.0 to 2.0)
     */
    private Double temperature;

    /**
     * Maximum number of tokens in the response
     */
    private Integer max_tokens;

    /**
     * Whether to stream the response
     */
    private Boolean stream;

    /**
     * Top-p sampling parameter
     */
    private Double top_p;

    /**
     * Top-k sampling parameter
     */
    private Integer top_k;

    /**
     * Repetition penalty
     */
    private Double repetition_penalty;

    /**
     * Additional parameters for the request
     */
    private Map<String, Object> parameters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Qwen3Message {
        /**
         * Role of the message sender (system, user, assistant)
         */
        private String role;

        /**
         * Content of the message
         */
        private String content;

        /**
         * Additional metadata for the message
         */
        private Map<String, Object> metadata;
    }
}