package com.chatbot.service.impl;

import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.model.ChatMessage;
import com.chatbot.service.AIService;
import com.chatbot.config.Qwen3Configuration;
import com.chatbot.service.SystemDataProcessor;
import com.chatbot.service.ResponseFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service("qwen3Service")
@RequiredArgsConstructor
@Slf4j
public class Qwen3ServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Qwen3Configuration qwen3Config;
    private final SystemDataProcessor systemDataProcessor;
    private final ResponseFormatter responseFormatter;

    @Override
    public ChatResponse processMessage(ChatRequest request, List<String> conversationHistory) {
        try {
            log.info("Processing message with Qwen3: {}", request.getMessage());
            
            // Analyze the message to determine intent and required data
            Map<String, Object> analysis = analyzeMessage(request.getMessage(), request.getSystemContext());
            
            // Process system data based on analysis
            Map<String, Object> processedSystemData = systemDataProcessor.processSystemData(
                request.getSystemContext(), (String) analysis.get("intent"));
            
            // Create Qwen3 request payload
            Map<String, Object> qwen3Request = buildQwen3Request(
                request.getMessage(), 
                conversationHistory, 
                processedSystemData,
                analysis
            );
            
            // Call Qwen3 API
            Map<String, Object> qwen3Response = callQwen3API(qwen3Request);
            
            // Format the response
            return formatResponse(
                (String) qwen3Response.get("response"),
                processedSystemData,
                (String) analysis.get("format")
            );
            
        } catch (Exception e) {
            log.error("Error processing message with Qwen3: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .message("I apologize, but I encountered an error processing your request. Please try again.")
                    .responseFormat(ChatMessage.ResponseFormat.TEXT)
                    .success(false)
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public ChatResponse generateContextualResponse(String message, Map<String, Object> systemContext) {
        try {
            // Process system context for relevant information
            Map<String, Object> contextAnalysis = systemDataProcessor.analyzeContext(systemContext);
            
            // Create contextual prompt for Qwen3
            String contextualPrompt = buildContextualPrompt(message, contextAnalysis);
            
            // Create Qwen3 request
            Map<String, Object> qwen3Request = Map.of(
                "model", qwen3Config.getModelName(),
                "messages", List.of(
                    Map.of("role", "system", "content", qwen3Config.getSystemPrompt()),
                    Map.of("role", "user", "content", contextualPrompt)
                ),
                "temperature", qwen3Config.getTemperature(),
                "max_tokens", qwen3Config.getMaxTokens()
            );
            
            // Call Qwen3 API
            Map<String, Object> qwen3Response = callQwen3API(qwen3Request);
            
            // Format response based on context
            String responseFormat = determineResponseFormat(contextAnalysis);
            
            return formatResponse(
                (String) qwen3Response.get("response"),
                contextAnalysis,
                responseFormat
            );
            
        } catch (Exception e) {
            log.error("Error generating contextual response: {}", e.getMessage(), e);
            return createErrorResponse("Error generating contextual response", e.getMessage());
        }
    }

    @Override
    public ChatResponse formatResponse(String message, Object content, String format) {
        try {
            return responseFormatter.formatResponse(message, content, format);
        } catch (Exception e) {
            log.error("Error formatting response: {}", e.getMessage(), e);
            return createErrorResponse("Error formatting response", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> analyzeMessage(String message, Map<String, Object> context) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Use Qwen3 to analyze message intent and determine response format
            String analysisPrompt = buildAnalysisPrompt(message, context);
            
            Map<String, Object> qwen3Request = Map.of(
                "model", qwen3Config.getModelName(),
                "messages", List.of(
                    Map.of("role", "system", "content", qwen3Config.getAnalysisSystemPrompt()),
                    Map.of("role", "user", "content", analysisPrompt)
                ),
                "temperature", 0.1, // Low temperature for consistent analysis
                "max_tokens", 150
            );
            
            Map<String, Object> qwen3Response = callQwen3API(qwen3Request);
            String analysisResult = (String) qwen3Response.get("response");
            
            // Parse the analysis result
            analysis = parseAnalysisResult(analysisResult);
            
        } catch (Exception e) {
            log.error("Error analyzing message: {}", e.getMessage(), e);
            // Fallback analysis
            analysis.put("intent", "general_query");
            analysis.put("format", "text");
            analysis.put("confidence", 0.5);
        }
        
        return analysis;
    }

    private Map<String, Object> buildQwen3Request(String message, List<String> history, 
                                                  Map<String, Object> systemData, 
                                                  Map<String, Object> analysis) {
        
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // Add system message with context
        String systemPrompt = buildSystemPromptWithContext(systemData, analysis);
        messages.add(Map.of("role", "system", "content", systemPrompt));
        
        // Add conversation history (last 5 exchanges to keep context manageable)
        if (history != null && !history.isEmpty()) {
            int startIndex = Math.max(0, history.size() - 5);
            for (int i = startIndex; i < history.size(); i++) {
                String historyItem = history.get(i);
                // Parse history item and add as separate user/assistant messages
                if (historyItem.contains("User:") && historyItem.contains("Assistant:")) {
                    String[] parts = historyItem.split("\\nAssistant:");
                    String userPart = parts[0].replace("User:", "").trim();
                    String assistantPart = parts.length > 1 ? parts[1].trim() : "";
                    
                    messages.add(Map.of("role", "user", "content", userPart));
                    if (!assistantPart.isEmpty()) {
                        messages.add(Map.of("role", "assistant", "content", assistantPart));
                    }
                }
            }
        }
        
        // Add current user message
        messages.add(Map.of("role", "user", "content", message));
        
        return Map.of(
            "model", qwen3Config.getModelName(),
            "messages", messages,
            "temperature", qwen3Config.getTemperature(),
            "max_tokens", qwen3Config.getMaxTokens(),
            "stream", false
        );
    }

    private Map<String, Object> callQwen3API(Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(qwen3Config.getApiKey());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                qwen3Config.getApiUrl(),
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message != null) {
                        return Map.of(
                            "response", message.get("content"),
                            "usage", responseBody.get("usage"),
                            "model", responseBody.get("model")
                        );
                    }
                }
            }
            
            throw new RuntimeException("Invalid response format from Qwen3 API");
            
        } catch (Exception e) {
            log.error("Error calling Qwen3 API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Qwen3 API: " + e.getMessage(), e);
        }
    }

    private String buildSystemPromptWithContext(Map<String, Object> systemData, Map<String, Object> analysis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(qwen3Config.getSystemPrompt()).append("\n\n");
        
        if (systemData != null && !systemData.isEmpty()) {
            prompt.append("Current System Context:\n");
            systemData.forEach((key, value) -> {
                prompt.append(String.format("- %s: %s\n", key, value));
            });
            prompt.append("\n");
        }
        
        String intent = (String) analysis.get("intent");
        if (intent != null) {
            prompt.append(String.format("User Intent: %s\n", intent));
            prompt.append("Please provide a helpful response based on this context and intent.\n");
        }
        
        return prompt.toString();
    }

    private String buildContextualPrompt(String message, Map<String, Object> contextAnalysis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("User Message: ").append(message).append("\n\n");
        
        if (contextAnalysis != null && !contextAnalysis.isEmpty()) {
            prompt.append("Relevant System Information:\n");
            contextAnalysis.forEach((key, value) -> {
                prompt.append(String.format("- %s: %s\n", key, value));
            });
        }
        
        return prompt.toString();
    }

    private String buildAnalysisPrompt(String message, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this user message and determine the intent and appropriate response format:\n\n");
        prompt.append("Message: ").append(message).append("\n\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("Available Context:\n");
            context.forEach((key, value) -> {
                prompt.append(String.format("- %s\n", key));
            });
        }
        
        prompt.append("\nRespond with JSON format: {\"intent\": \"intent_name\", \"format\": \"text|card|list|table\", \"confidence\": 0.0-1.0}");
        
        return prompt.toString();
    }

    private Map<String, Object> parseAnalysisResult(String analysisResult) {
        try {
            // Try to parse as JSON first
            return objectMapper.readValue(analysisResult, Map.class);
        } catch (Exception e) {
            // Fallback parsing if JSON format is not perfect
            Map<String, Object> result = new HashMap<>();
            
            if (analysisResult.toLowerCase().contains("data") || analysisResult.toLowerCase().contains("system")) {
                result.put("intent", "system_data_query");
            } else if (analysisResult.toLowerCase().contains("list")) {
                result.put("format", "list");
                result.put("intent", "list_query");
            } else if (analysisResult.toLowerCase().contains("table")) {
                result.put("format", "table");
                result.put("intent", "table_query");
            } else {
                result.put("intent", "general_query");
                result.put("format", "text");
            }
            
            result.put("confidence", 0.7);
            return result;
        }
    }

    private String determineResponseFormat(Map<String, Object> contextAnalysis) {
        if (contextAnalysis == null || contextAnalysis.isEmpty()) {
            return "text";
        }
        
        // Determine format based on context data structure
        if (contextAnalysis.containsKey("table_data")) {
            return "table";
        } else if (contextAnalysis.containsKey("list_data")) {
            return "list";
        } else if (contextAnalysis.containsKey("card_data")) {
            return "card";
        } else {
            return "text";
        }
    }

    private ChatResponse createErrorResponse(String message, String error) {
        return ChatResponse.builder()
                .message(message)
                .responseFormat(ChatMessage.ResponseFormat.TEXT)
                .success(false)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }
}