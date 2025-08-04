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
public class OpenAIServiceImpl implements AIService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.openai.api-key}")
    private String apiKey;
    
    @Value("${ai.openai.model}")
    private String model;
    
    @Value("${ai.openai.max-tokens}")
    private int maxTokens;
    
    @Value("${ai.openai.temperature}")
    private double temperature;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    @Override
    public ChatResponse processMessage(ChatRequest request, List<String> conversationHistory) {
        try {
            // Analyze message to determine response format
            Map<String, Object> analysis = analyzeMessage(request.getMessage(), request.getSystemContext());
            
            // Build context-aware prompt
            String enhancedPrompt = buildContextualPrompt(request, conversationHistory);
            
            // Call OpenAI API
            String aiResponse = callOpenAI(enhancedPrompt);
            
            // Format response based on analysis
            ChatMessage.ResponseFormat format = determineResponseFormat(analysis, aiResponse);
            Object formattedContent = formatContent(aiResponse, format, analysis);
            
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
            log.error("Error processing message: {}", e.getMessage(), e);
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
        Object formattedContent = formatContent(message, responseFormat, Collections.emptyMap());
        
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
        
        // Intent detection
        String intent = detectIntent(message);
        analysis.put("intent", intent);
        
        // Data type detection
        String dataType = detectDataType(message, context);
        analysis.put("dataType", dataType);
        
        // Response format suggestion
        String suggestedFormat = suggestResponseFormat(intent, dataType);
        analysis.put("suggestedFormat", suggestedFormat);
        
        // Extract entities
        Map<String, String> entities = extractEntities(message);
        analysis.put("entities", entities);
        
        return analysis;
    }
    
    private String buildContextualPrompt(ChatRequest request, List<String> history) {
        StringBuilder prompt = new StringBuilder();
        
        // System context
        prompt.append("You are an AI assistant integrated into a backend management system. ");
        prompt.append("Provide helpful, accurate responses based on the context provided.\n\n");
        
        // Add system context if available
        if (request.getSystemContext() != null && !request.getSystemContext().isEmpty()) {
            prompt.append("System Context:\n");
            request.getSystemContext().forEach((key, value) -> 
                prompt.append("- ").append(key).append(": ").append(value).append("\n"));
            prompt.append("\n");
        }
        
        // Add conversation history
        if (!history.isEmpty()) {
            prompt.append("Previous conversation:\n");
            history.forEach(msg -> prompt.append(msg).append("\n"));
            prompt.append("\n");
        }
        
        // Current page context
        if (request.getCurrentPage() != null) {
            prompt.append("Current page: ").append(request.getCurrentPage()).append("\n\n");
        }
        
        prompt.append("User question: ").append(request.getMessage());
        
        return prompt.toString();
    }
    
    private String callOpenAI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL, HttpMethod.POST, entity, String.class);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.path("choices").get(0).path("message").path("content").asText();
    }
    
    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("list") || lowerMessage.contains("show all") || lowerMessage.contains("display")) {
            return "list";
        } else if (lowerMessage.contains("details") || lowerMessage.contains("information") || lowerMessage.contains("about")) {
            return "details";
        } else if (lowerMessage.contains("create") || lowerMessage.contains("add") || lowerMessage.contains("new")) {
            return "create";
        } else if (lowerMessage.contains("update") || lowerMessage.contains("edit") || lowerMessage.contains("modify")) {
            return "update";
        } else if (lowerMessage.contains("delete") || lowerMessage.contains("remove")) {
            return "delete";
        } else if (lowerMessage.contains("help") || lowerMessage.contains("how to")) {
            return "help";
        }
        
        return "general";
    }
    
    private String detectDataType(String message, Map<String, Object> context) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("user") || lowerMessage.contains("account")) {
            return "user";
        } else if (lowerMessage.contains("order") || lowerMessage.contains("purchase")) {
            return "order";
        } else if (lowerMessage.contains("product") || lowerMessage.contains("item")) {
            return "product";
        } else if (lowerMessage.contains("report") || lowerMessage.contains("analytics")) {
            return "report";
        }
        
        return "general";
    }
    
    private String suggestResponseFormat(String intent, String dataType) {
        if ("list".equals(intent)) {
            return "LIST";
        } else if ("details".equals(intent)) {
            return "CARD";
        } else if ("report".equals(dataType)) {
            return "TABLE";
        }
        
        return "TEXT";
    }
    
    private Map<String, String> extractEntities(String message) {
        Map<String, String> entities = new HashMap<>();
        
        // Simple entity extraction (in production, use NLP libraries)
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Pattern phonePattern = Pattern.compile("\\b\\d{3}-\\d{3}-\\d{4}\\b");
        Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
        
        if (emailPattern.matcher(message).find()) {
            entities.put("email", emailPattern.matcher(message).group());
        }
        if (phonePattern.matcher(message).find()) {
            entities.put("phone", phonePattern.matcher(message).group());
        }
        
        return entities;
    }
    
    private ChatMessage.ResponseFormat determineResponseFormat(Map<String, Object> analysis, String response) {
        String suggestedFormat = (String) analysis.get("suggestedFormat");
        
        // Check if response contains structured data indicators
        if (response.contains("```json") || response.contains("{") || response.contains("[")) {
            return ChatMessage.ResponseFormat.CARD;
        }
        
        return ChatMessage.ResponseFormat.valueOf(suggestedFormat);
    }
    
    private Object formatContent(String response, ChatMessage.ResponseFormat format, Map<String, Object> analysis) {
        switch (format) {
            case CARD:
                return createCardContent(response, analysis);
            case LIST:
                return createListContent(response, analysis);
            case TABLE:
                return createTableContent(response, analysis);
            default:
                return response;
        }
    }
    
    private ChatResponse.CardContent createCardContent(String response, Map<String, Object> analysis) {
        return ChatResponse.CardContent.builder()
                .title("Information")
                .description(response)
                .data(analysis)
                .build();
    }
    
    private ChatResponse.ListContent createListContent(String response, Map<String, Object> analysis) {
        List<ChatResponse.ListContent.ListItem> items = new ArrayList<>();
        
        // Parse response into list items (simplified)
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("-") || line.trim().startsWith("•")) {
                items.add(ChatResponse.ListContent.ListItem.builder()
                        .title(line.trim().substring(1).trim())
                        .build());
            }
        }
        
        return ChatResponse.ListContent.builder()
                .title("Results")
                .items(items)
                .build();
    }
    
    private ChatResponse.TableContent createTableContent(String response, Map<String, Object> analysis) {
        return ChatResponse.TableContent.builder()
                .title("Data Table")
                .headers(Arrays.asList("Item", "Value"))
                .rows(Collections.singletonList(Arrays.asList("Response", response)))
                .build();
    }
    
    private List<ChatResponse.ActionButton> generateSuggestedActions(Map<String, Object> analysis) {
        List<ChatResponse.ActionButton> actions = new ArrayList<>();
        
        String intent = (String) analysis.get("intent");
        if ("list".equals(intent)) {
            actions.add(ChatResponse.ActionButton.builder()
                    .label("View Details")
                    .action("view_details")
                    .type("function")
                    .build());
        }
        
        return actions;
    }
}