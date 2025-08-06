package com.chatbot.service.impl;

import com.chatbot.model.ChatMessage;
import com.chatbot.model.dto.ChatRequest;
import com.chatbot.model.dto.ChatResponse;
import com.chatbot.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class Qwen3ServiceImpl implements AIService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.qwen3.api-key}")
    private String apiKey;
    
    @Value("${ai.qwen3.model:qwen-turbo}")
    private String model;
    
    @Value("${ai.qwen3.max-tokens:2048}")
    private int maxTokens;
    
    @Value("${ai.qwen3.temperature:0.7}")
    private double temperature;
    
    @Value("${ai.qwen3.api-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String qwen3ApiUrl;
    
    @Override
    public ChatResponse processMessage(ChatRequest request, List<String> conversationHistory) {
        try {
            // Analyze message to determine response format and data requirements
            Map<String, Object> analysis = analyzeMessage(request.getMessage(), request.getSystemContext());
            
            // Build context-aware prompt with system data
            String enhancedPrompt = buildContextualPrompt(request, conversationHistory, analysis);
            
            // Call Qwen3 API
            String aiResponse = callQwen3(enhancedPrompt);
            
            // Process and format the response based on analysis
            ChatMessage.ResponseFormat format = determineResponseFormat(analysis, aiResponse);
            Object formattedContent = formatContent(aiResponse, format, analysis, request.getSystemContext());
            
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .message(aiResponse)
                    .responseFormat(format)
                    .content(formattedContent)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .metadata(analysis)
                    .suggestedActions(generateSuggestedActions(analysis))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing message with Qwen3: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .message("I apologize, but I encountered an error processing your request. Please try again.")
                    .responseFormat(ChatMessage.ResponseFormat.TEXT)
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
    
    @Override
    public ChatResponse generateContextualResponse(String message, Map<String, Object> systemContext) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setSystemContext(systemContext);
        return processMessage(request, Collections.emptyList());
    }
    
    @Override
    public ChatResponse formatResponse(String message, Object content, String format) {
        ChatMessage.ResponseFormat responseFormat = ChatMessage.ResponseFormat.valueOf(format.toUpperCase());
        Object formattedContent = formatContent(message, responseFormat, Collections.emptyMap(), Collections.emptyMap());
        
        return ChatResponse.builder()
                .message(message)
                .responseFormat(responseFormat)
                .content(formattedContent)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }
    
    @Override
    public Map<String, Object> analyzeMessage(String message, Map<String, Object> context) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Detect intent
        String intent = detectIntent(message);
        analysis.put("intent", intent);
        
        // Detect data type requirements
        String dataType = detectDataType(message, context);
        analysis.put("dataType", dataType);
        
        // Extract entities
        Map<String, String> entities = extractEntities(message);
        analysis.put("entities", entities);
        
        // Determine response format
        String suggestedFormat = suggestResponseFormat(intent, dataType);
        analysis.put("suggestedFormat", suggestedFormat);
        
        // Check if system data is required
        boolean requiresSystemData = requiresSystemData(intent, dataType);
        analysis.put("requiresSystemData", requiresSystemData);
        
        // Determine confidence level
        double confidence = calculateConfidence(intent, entities, context);
        analysis.put("confidence", confidence);
        
        return analysis;
    }
    
    private String buildContextualPrompt(ChatRequest request, List<String> history, Map<String, Object> analysis) {
        StringBuilder prompt = new StringBuilder();
        
        // System context and data
        if (request.getSystemContext() != null && !request.getSystemContext().isEmpty()) {
            prompt.append("System Context and Available Data:\n");
            prompt.append(formatSystemContext(request.getSystemContext()));
            prompt.append("\n\n");
        }
        
        // Conversation history
        if (!history.isEmpty()) {
            prompt.append("Previous Conversation:\n");
            for (String hist : history) {
                prompt.append(hist).append("\n");
            }
            prompt.append("\n");
        }
        
        // Current user message with context
        prompt.append("User Query: ").append(request.getMessage()).append("\n\n");
        
        // Instructions based on analysis
        String intent = (String) analysis.get("intent");
        String dataType = (String) analysis.get("dataType");
        
        prompt.append("Instructions: ");
        if ("system_info".equals(intent)) {
            prompt.append("Provide detailed system information in a structured format. ");
            prompt.append("If system data is available, use it to provide accurate information. ");
            prompt.append("Format the response appropriately based on the data type requested.");
        } else if ("data_query".equals(intent)) {
            prompt.append("Process the available system data to answer the query. ");
            prompt.append("Format the response as requested (table, list, or card format). ");
            prompt.append("Ensure data accuracy and provide relevant context.");
        } else {
            prompt.append("Provide a helpful and informative response. ");
            prompt.append("If system data is relevant, incorporate it appropriately.");
        }
        
        return prompt.toString();
    }
    
    private String callQwen3(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", Arrays.asList(
            Map.of("role", "system", "content", "You are a helpful AI assistant that can process system data and provide formatted responses."),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("input", input);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_tokens", maxTokens);
        parameters.put("temperature", temperature);
        parameters.put("top_p", 0.8);
        requestBody.put("parameters", parameters);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                qwen3ApiUrl, 
                HttpMethod.POST, 
                entity, 
                JsonNode.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode body = response.getBody();
                JsonNode output = body.get("output");
                if (output != null && output.has("text")) {
                    return output.get("text").asText();
                }
            }
            
            throw new RuntimeException("Invalid response from Qwen3 API");
            
        } catch (Exception e) {
            log.error("Error calling Qwen3 API: {}", e.getMessage());
            throw e;
        }
    }
    
    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("system") || lowerMessage.contains("info") || lowerMessage.contains("status")) {
            return "system_info";
        } else if (lowerMessage.contains("data") || lowerMessage.contains("query") || lowerMessage.contains("show")) {
            return "data_query";
        } else if (lowerMessage.contains("help") || lowerMessage.contains("how") || lowerMessage.contains("what")) {
            return "help_request";
        } else {
            return "general_query";
        }
    }
    
    private String detectDataType(String message, Map<String, Object> context) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("table") || lowerMessage.contains("list") || lowerMessage.contains("format")) {
            return "structured_data";
        } else if (lowerMessage.contains("chart") || lowerMessage.contains("graph") || lowerMessage.contains("visual")) {
            return "visual_data";
        } else if (lowerMessage.contains("summary") || lowerMessage.contains("overview")) {
            return "summary_data";
        } else {
            return "text_data";
        }
    }
    
    private String suggestResponseFormat(String intent, String dataType) {
        if ("system_info".equals(intent)) {
            if ("structured_data".equals(dataType)) {
                return "TABLE";
            } else if ("visual_data".equals(dataType)) {
                return "CARD";
            } else {
                return "LIST";
            }
        } else if ("data_query".equals(intent)) {
            if ("structured_data".equals(dataType)) {
                return "TABLE";
            } else {
                return "TEXT";
            }
        } else {
            return "TEXT";
        }
    }
    
    private Map<String, String> extractEntities(String message) {
        Map<String, String> entities = new HashMap<>();
        
        // Extract system-related entities
        Pattern systemPattern = Pattern.compile("\\b(system|status|info|data|query)\\b", Pattern.CASE_INSENSITIVE);
        if (systemPattern.matcher(message).find()) {
            entities.put("type", "system_query");
        }
        
        // Extract format preferences
        Pattern formatPattern = Pattern.compile("\\b(table|list|card|chart|graph)\\b", Pattern.CASE_INSENSITIVE);
        if (formatPattern.matcher(message).find()) {
            entities.put("format", "structured");
        }
        
        return entities;
    }
    
    private boolean requiresSystemData(String intent, String dataType) {
        return "system_info".equals(intent) || "data_query".equals(intent);
    }
    
    private double calculateConfidence(String intent, Map<String, String> entities, Map<String, Object> context) {
        double confidence = 0.5; // Base confidence
        
        if ("system_info".equals(intent) && context != null && !context.isEmpty()) {
            confidence += 0.3;
        }
        
        if (!entities.isEmpty()) {
            confidence += 0.2;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    private String formatSystemContext(Map<String, Object> systemContext) {
        StringBuilder formatted = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : systemContext.entrySet()) {
            formatted.append(entry.getKey()).append(": ");
            if (entry.getValue() instanceof Map) {
                formatted.append(formatMap((Map<?, ?>) entry.getValue()));
            } else {
                formatted.append(entry.getValue());
            }
            formatted.append("\n");
        }
        
        return formatted.toString();
    }
    
    private String formatMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        if (!map.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("}");
        return sb.toString();
    }
    
    private ChatMessage.ResponseFormat determineResponseFormat(Map<String, Object> analysis, String response) {
        String suggestedFormat = (String) analysis.get("suggestedFormat");
        if (suggestedFormat != null) {
            try {
                return ChatMessage.ResponseFormat.valueOf(suggestedFormat);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid response format: {}", suggestedFormat);
            }
        }
        return ChatMessage.ResponseFormat.TEXT;
    }
    
    private Object formatContent(String response, ChatMessage.ResponseFormat format, 
                               Map<String, Object> analysis, Map<String, Object> systemContext) {
        switch (format) {
            case CARD:
                return createCardContent(response, analysis, systemContext);
            case LIST:
                return createListContent(response, analysis, systemContext);
            case TABLE:
                return createTableContent(response, analysis, systemContext);
            case TEXT:
            default:
                return response;
        }
    }
    
    private ChatResponse.CardContent createCardContent(String response, Map<String, Object> analysis, 
                                                     Map<String, Object> systemContext) {
        String title = "System Information";
        if (analysis.containsKey("entities")) {
            Map<String, String> entities = (Map<String, String>) analysis.get("entities");
            if (entities.containsKey("type")) {
                title = "System " + entities.get("type").replace("_", " ").toUpperCase();
            }
        }
        
        return ChatResponse.CardContent.builder()
                .title(title)
                .subtitle("Processed System Data")
                .description(response)
                .data(systemContext)
                .build();
    }
    
    private ChatResponse.ListContent createListContent(String response, Map<String, Object> analysis,
                                                     Map<String, Object> systemContext) {
        List<ChatResponse.ListContent.ListItem> items = new ArrayList<>();
        
        // Parse response and create list items
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                items.add(ChatResponse.ListContent.ListItem.builder()
                        .title(line.trim())
                        .build());
            }
        }
        
        return ChatResponse.ListContent.builder()
                .title("System Information")
                .items(items)
                .build();
    }
    
    private ChatResponse.TableContent createTableContent(String response, Map<String, Object> analysis,
                                                       Map<String, Object> systemContext) {
        List<String> headers = Arrays.asList("Property", "Value", "Description");
        List<List<Object>> rows = new ArrayList<>();
        
        // Process system context into table rows
        if (systemContext != null) {
            for (Map.Entry<String, Object> entry : systemContext.entrySet()) {
                List<Object> row = Arrays.asList(
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue().toString() : "N/A",
                    "System property"
                );
                rows.add(row);
            }
        }
        
        return ChatResponse.TableContent.builder()
                .title("System Data Overview")
                .headers(headers)
                .rows(rows)
                .build();
    }
    
    private List<ChatResponse.ActionButton> generateSuggestedActions(Map<String, Object> analysis) {
        List<ChatResponse.ActionButton> actions = new ArrayList<>();
        
        String intent = (String) analysis.get("intent");
        if ("system_info".equals(intent)) {
            actions.add(ChatResponse.ActionButton.builder()
                    .label("Export Data")
                    .action("export_system_data")
                    .type("function")
                    .build());
            
            actions.add(ChatResponse.ActionButton.builder()
                    .label("Detailed Report")
                    .action("generate_detailed_report")
                    .type("function")
                    .build());
        }
        
        return actions;
    }
}