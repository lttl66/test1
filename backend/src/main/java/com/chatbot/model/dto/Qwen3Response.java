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
public class Qwen3Response {

    /**
     * Unique identifier for the response
     */
    private String id;

    /**
     * Object type (usually "chat.completion")
     */
    private String object;

    /**
     * Timestamp when the response was created
     */
    private Long created;

    /**
     * Model used for generating the response
     */
    private String model;

    /**
     * List of choice objects containing the generated responses
     */
    private List<Qwen3Choice> choices;

    /**
     * Usage statistics for the request
     */
    private Qwen3Usage usage;

    /**
     * Additional metadata from the API
     */
    private Map<String, Object> metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Qwen3Choice {
        /**
         * Index of this choice
         */
        private Integer index;

        /**
         * The generated message
         */
        private Qwen3Message message;

        /**
         * Reason for finishing (stop, length, content_filter, etc.)
         */
        private String finish_reason;

        /**
         * Delta for streaming responses
         */
        private Qwen3Message delta;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Qwen3Message {
        /**
         * Role of the message (assistant, user, system)
         */
        private String role;

        /**
         * Content of the generated message
         */
        private String content;

        /**
         * Additional properties of the message
         */
        private Map<String, Object> properties;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Qwen3Usage {
        /**
         * Number of tokens in the prompt
         */
        private Integer prompt_tokens;

        /**
         * Number of tokens in the completion
         */
        private Integer completion_tokens;

        /**
         * Total number of tokens used
         */
        private Integer total_tokens;

        /**
         * Additional usage details
         */
        private Map<String, Object> details;
    }

    /**
     * Extract the main response content from the first choice
     */
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            Qwen3Choice firstChoice = choices.get(0);
            if (firstChoice.getMessage() != null) {
                return firstChoice.getMessage().getContent();
            }
        }
        return null;
    }

    /**
     * Check if the response was successful
     */
    public boolean isSuccessful() {
        return choices != null && !choices.isEmpty() && getContent() != null;
    }

    /**
     * Get the finish reason for the response
     */
    public String getFinishReason() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getFinish_reason();
        }
        return null;
    }
}