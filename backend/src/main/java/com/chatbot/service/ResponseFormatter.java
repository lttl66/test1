package com.chatbot.service;

import com.chatbot.model.ChatMessage;
import com.chatbot.model.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseFormatter {

    private final ObjectMapper objectMapper;

    /**
     * Format response content based on message, content, and desired format
     */
    public ChatResponse formatResponse(String message, Object content, String format) {
        try {
            ChatResponse.ChatResponseBuilder responseBuilder = ChatResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .success(true);

            switch (format != null ? format.toLowerCase() : "text") {
                case "card":
                    return formatCardResponse(responseBuilder, message, content);
                case "list":
                    return formatListResponse(responseBuilder, message, content);
                case "table":
                    return formatTableResponse(responseBuilder, message, content);
                case "chart":
                    return formatChartResponse(responseBuilder, message, content);
                default:
                    return formatTextResponse(responseBuilder, message, content);
            }

        } catch (Exception e) {
            log.error("Error formatting response: {}", e.getMessage(), e);
            return createErrorResponse(message, e.getMessage());
        }
    }

    private ChatResponse formatTextResponse(ChatResponse.ChatResponseBuilder builder, String message, Object content) {
        builder.responseFormat(ChatMessage.ResponseFormat.TEXT);
        
        // Generate structured text content
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("formatted_at", System.currentTimeMillis());
        
        if (content instanceof Map) {
            Map<String, Object> contentMap = (Map<String, Object>) content;
            String formattedText = formatMapAsText(contentMap);
            builder.content(formattedText);
            metadata.put("content_type", "structured_data");
        } else if (content instanceof List) {
            List<?> contentList = (List<?>) content;
            String formattedText = formatListAsText(contentList);
            builder.content(formattedText);
            metadata.put("content_type", "list_data");
        } else {
            builder.content(content != null ? content.toString() : "");
            metadata.put("content_type", "simple_text");
        }
        
        return builder.metadata(metadata).build();
    }

    private ChatResponse formatCardResponse(ChatResponse.ChatResponseBuilder builder, String message, Object content) {
        builder.responseFormat(ChatMessage.ResponseFormat.CARD);
        
        Map<String, Object> cardContent = new HashMap<>();
        cardContent.put("title", extractTitle(content));
        cardContent.put("description", message);
        cardContent.put("fields", extractCardFields(content));
        cardContent.put("actions", generateCardActions(content));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("card_type", determineCardType(content));
        metadata.put("formatted_at", System.currentTimeMillis());
        
        return builder
            .content(cardContent)
            .metadata(metadata)
            .suggestedActions(generateActionButtons(content))
            .build();
    }

    private ChatResponse formatListResponse(ChatResponse.ChatResponseBuilder builder, String message, Object content) {
        builder.responseFormat(ChatMessage.ResponseFormat.LIST);
        
        Map<String, Object> listContent = new HashMap<>();
        listContent.put("title", "System Information");
        listContent.put("items", extractListItems(content));
        listContent.put("total_count", getListItemCount(content));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("list_type", determineListType(content));
        metadata.put("formatted_at", System.currentTimeMillis());
        
        return builder
            .content(listContent)
            .metadata(metadata)
            .build();
    }

    private ChatResponse formatTableResponse(ChatResponse.ChatResponseBuilder builder, String message, Object content) {
        builder.responseFormat(ChatMessage.ResponseFormat.TABLE);
        
        Map<String, Object> tableContent = new HashMap<>();
        tableContent.put("title", "Data Table");
        tableContent.put("columns", extractTableColumns(content));
        tableContent.put("rows", extractTableRows(content));
        tableContent.put("pagination", createPaginationInfo(content));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("table_type", determineTableType(content));
        metadata.put("formatted_at", System.currentTimeMillis());
        
        return builder
            .content(tableContent)
            .metadata(metadata)
            .build();
    }

    private ChatResponse formatChartResponse(ChatResponse.ChatResponseBuilder builder, String message, Object content) {
        builder.responseFormat(ChatMessage.ResponseFormat.CHART);
        
        Map<String, Object> chartContent = new HashMap<>();
        chartContent.put("title", "Data Visualization");
        chartContent.put("chart_type", determineChartType(content));
        chartContent.put("data", extractChartData(content));
        chartContent.put("config", createChartConfig(content));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("chart_library", "Chart.js");
        metadata.put("formatted_at", System.currentTimeMillis());
        
        return builder
            .content(chartContent)
            .metadata(metadata)
            .build();
    }

    // Helper methods for text formatting
    private String formatMapAsText(Map<String, Object> map) {
        StringBuilder text = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (key.startsWith("_")) continue; // Skip metadata
            
            text.append("**").append(formatKey(key)).append("**: ");
            
            if (value instanceof Map) {
                text.append("\n").append(formatNestedMap((Map<String, Object>) value, "  "));
            } else if (value instanceof List) {
                text.append(formatListValue((List<?>) value));
            } else {
                text.append(value != null ? value.toString() : "N/A");
            }
            text.append("\n\n");
        }
        
        return text.toString().trim();
    }

    private String formatListAsText(List<?> list) {
        StringBuilder text = new StringBuilder();
        
        for (int i = 0; i < Math.min(list.size(), 20); i++) {
            Object item = list.get(i);
            text.append("• ");
            
            if (item instanceof Map) {
                Map<String, Object> itemMap = (Map<String, Object>) item;
                text.append(formatMapAsInlineText(itemMap));
            } else {
                text.append(item != null ? item.toString() : "N/A");
            }
            text.append("\n");
        }
        
        if (list.size() > 20) {
            text.append("... and ").append(list.size() - 20).append(" more items");
        }
        
        return text.toString().trim();
    }

    private String formatNestedMap(Map<String, Object> map, String indent) {
        StringBuilder text = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            text.append(indent).append("- ").append(formatKey(entry.getKey()))
                .append(": ").append(entry.getValue() != null ? entry.getValue().toString() : "N/A")
                .append("\n");
        }
        
        return text.toString();
    }

    private String formatMapAsInlineText(Map<String, Object> map) {
        return map.entrySet().stream()
            .limit(3)
            .map(entry -> entry.getKey() + ": " + (entry.getValue() != null ? entry.getValue().toString() : "N/A"))
            .collect(Collectors.joining(", "));
    }

    private String formatListValue(List<?> list) {
        if (list.isEmpty()) return "Empty list";
        if (list.size() == 1) return list.get(0).toString();
        return list.size() + " items: " + list.stream()
            .limit(3)
            .map(Object::toString)
            .collect(Collectors.joining(", ")) +
            (list.size() > 3 ? "..." : "");
    }

    private String formatKey(String key) {
        return Arrays.stream(key.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    // Helper methods for card formatting
    private String extractTitle(Object content) {
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            if (map.containsKey("title")) return map.get("title").toString();
            if (map.containsKey("name")) return map.get("name").toString();
            if (map.containsKey("system_info")) return "System Information";
            if (map.containsKey("user_list")) return "User Management";
        }
        return "Information Card";
    }

    private List<Map<String, Object>> extractCardFields(Object content) {
        List<Map<String, Object>> fields = new ArrayList<>();
        
        if (content instanceof Map) {
            Map<String, Object> contentMap = (Map<String, Object>) content;
            
            for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                if (entry.getKey().startsWith("_")) continue;
                
                Map<String, Object> field = new HashMap<>();
                field.put("name", formatKey(entry.getKey()));
                field.put("value", formatFieldValue(entry.getValue()));
                field.put("type", determineFieldType(entry.getValue()));
                fields.add(field);
                
                if (fields.size() >= 8) break; // Limit fields for UI
            }
        }
        
        return fields;
    }

    private String formatFieldValue(Object value) {
        if (value == null) return "N/A";
        if (value instanceof Number) return value.toString();
        if (value instanceof Boolean) return (Boolean) value ? "Yes" : "No";
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.size() + " items";
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return map.size() + " properties";
        }
        return value.toString();
    }

    private String determineFieldType(Object value) {
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof List) return "list";
        if (value instanceof Map) return "object";
        return "text";
    }

    private String determineCardType(Object content) {
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            if (map.containsKey("status")) return "status_card";
            if (map.containsKey("metrics") || map.containsKey("performance")) return "metrics_card";
            if (map.containsKey("users") || map.containsKey("user_list")) return "user_card";
        }
        return "info_card";
    }

    private List<Map<String, Object>> generateCardActions(Object content) {
        List<Map<String, Object>> actions = new ArrayList<>();
        
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            
            if (map.containsKey("user_list")) {
                actions.add(Map.of("label", "View All Users", "action", "view_users", "type", "navigation"));
            }
            if (map.containsKey("system_info")) {
                actions.add(Map.of("label", "System Details", "action", "system_details", "type", "function"));
            }
        }
        
        return actions;
    }

    // Helper methods for list formatting
    private List<Map<String, Object>> extractListItems(Object content) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        if (content instanceof List) {
            List<?> contentList = (List<?>) content;
            for (Object item : contentList) {
                items.add(createListItem(item));
                if (items.size() >= 20) break;
            }
        } else if (content instanceof Map) {
            Map<String, Object> contentMap = (Map<String, Object>) content;
            
            for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                if (entry.getKey().startsWith("_")) continue;
                
                if (entry.getValue() instanceof List) {
                    List<?> list = (List<?>) entry.getValue();
                    for (Object item : list) {
                        items.add(createListItem(item));
                        if (items.size() >= 20) break;
                    }
                }
            }
        }
        
        return items;
    }

    private Map<String, Object> createListItem(Object item) {
        Map<String, Object> listItem = new HashMap<>();
        
        if (item instanceof Map) {
            Map<String, Object> itemMap = (Map<String, Object>) item;
            listItem.put("title", extractItemTitle(itemMap));
            listItem.put("description", extractItemDescription(itemMap));
            listItem.put("metadata", extractItemMetadata(itemMap));
        } else {
            listItem.put("title", item.toString());
            listItem.put("description", "");
            listItem.put("metadata", new HashMap<>());
        }
        
        return listItem;
    }

    private String extractItemTitle(Map<String, Object> item) {
        if (item.containsKey("name")) return item.get("name").toString();
        if (item.containsKey("title")) return item.get("title").toString();
        if (item.containsKey("id")) return "Item " + item.get("id").toString();
        return "List Item";
    }

    private String extractItemDescription(Map<String, Object> item) {
        if (item.containsKey("description")) return item.get("description").toString();
        if (item.containsKey("summary")) return item.get("summary").toString();
        return "";
    }

    private Map<String, Object> extractItemMetadata(Map<String, Object> item) {
        Map<String, Object> metadata = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : item.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("name") && !key.equals("title") && !key.equals("description")) {
                metadata.put(key, entry.getValue());
            }
        }
        
        return metadata;
    }

    private int getListItemCount(Object content) {
        if (content instanceof List) {
            return ((List<?>) content).size();
        } else if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            return map.values().stream()
                .mapToInt(value -> value instanceof List ? ((List<?>) value).size() : 0)
                .sum();
        }
        return 0;
    }

    private String determineListType(Object content) {
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            if (map.containsKey("users") || map.containsKey("user_list")) return "user_list";
            if (map.containsKey("alerts")) return "alert_list";
            if (map.containsKey("logs")) return "log_list";
        }
        return "generic_list";
    }

    // Helper methods for table formatting
    private List<Map<String, Object>> extractTableColumns(Object content) {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<?> list = (List<?>) entry.getValue();
                    if (!list.isEmpty() && list.get(0) instanceof Map) {
                        Map<String, Object> firstRow = (Map<String, Object>) list.get(0);
                        
                        for (String key : firstRow.keySet()) {
                            Map<String, Object> column = new HashMap<>();
                            column.put("key", key);
                            column.put("title", formatKey(key));
                            column.put("type", determineColumnType(firstRow.get(key)));
                            column.put("sortable", true);
                            columns.add(column);
                        }
                        break;
                    }
                }
            }
        }
        
        return columns;
    }

    private List<Map<String, Object>> extractTableRows(Object content) {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<?> list = (List<?>) entry.getValue();
                    
                    for (Object item : list) {
                        if (item instanceof Map) {
                            rows.add((Map<String, Object>) item);
                        }
                        if (rows.size() >= 50) break; // Limit rows for performance
                    }
                    break;
                }
            }
        }
        
        return rows;
    }

    private String determineColumnType(Object value) {
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Date || value instanceof LocalDateTime) return "date";
        return "text";
    }

    private Map<String, Object> createPaginationInfo(Object content) {
        Map<String, Object> pagination = new HashMap<>();
        
        int totalRows = 0;
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            for (Object value : map.values()) {
                if (value instanceof List) {
                    totalRows = ((List<?>) value).size();
                    break;
                }
            }
        }
        
        pagination.put("total", totalRows);
        pagination.put("page_size", Math.min(50, totalRows));
        pagination.put("current_page", 1);
        pagination.put("total_pages", Math.max(1, (int) Math.ceil((double) totalRows / 50)));
        
        return pagination;
    }

    private String determineTableType(Object content) {
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            if (map.containsKey("users") || map.containsKey("user_list")) return "user_table";
            if (map.containsKey("logs")) return "log_table";
            if (map.containsKey("metrics")) return "metrics_table";
        }
        return "data_table";
    }

    // Helper methods for chart formatting
    private String determineChartType(Object content) {
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            if (map.containsKey("metrics") || map.containsKey("performance")) return "line";
            if (map.containsKey("categories")) return "bar";
            if (map.containsKey("percentage") || map.containsKey("ratio")) return "pie";
        }
        return "bar";
    }

    private Object extractChartData(Object content) {
        Map<String, Object> chartData = new HashMap<>();
        
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            
            // Extract numeric data for charts
            List<String> labels = new ArrayList<>();
            List<Number> values = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    labels.add(formatKey(entry.getKey()));
                    values.add((Number) entry.getValue());
                }
            }
            
            chartData.put("labels", labels);
            chartData.put("datasets", List.of(Map.of(
                "label", "Data",
                "data", values,
                "backgroundColor", generateColors(values.size())
            )));
        }
        
        return chartData;
    }

    private Map<String, Object> createChartConfig(Object content) {
        Map<String, Object> config = new HashMap<>();
        config.put("responsive", true);
        config.put("maintainAspectRatio", false);
        config.put("plugins", Map.of(
            "legend", Map.of("display", true),
            "title", Map.of("display", true, "text", "Data Visualization")
        ));
        return config;
    }

    private List<String> generateColors(int count) {
        List<String> colors = Arrays.asList(
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0",
            "#9966FF", "#FF9F40", "#FF6384", "#36A2EB"
        );
        
        return colors.stream()
            .limit(count)
            .collect(Collectors.toList());
    }

    // Common helper methods
    private List<ChatResponse.ActionButton> generateActionButtons(Object content) {
        List<ChatResponse.ActionButton> actions = new ArrayList<>();
        
        if (content instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) content;
            
            if (map.containsKey("user_list")) {
                actions.add(ChatResponse.ActionButton.builder()
                    .label("Manage Users")
                    .action("navigate_users")
                    .type("navigation")
                    .build());
            }
            
            if (map.containsKey("system_info")) {
                actions.add(ChatResponse.ActionButton.builder()
                    .label("System Settings")
                    .action("system_settings")
                    .type("function")
                    .build());
            }
        }
        
        return actions;
    }

    private ChatResponse createErrorResponse(String message, String error) {
        return ChatResponse.builder()
            .message("Error formatting response: " + error)
            .responseFormat(ChatMessage.ResponseFormat.TEXT)
            .success(false)
            .error(error)
            .timestamp(LocalDateTime.now())
            .build();
    }
}