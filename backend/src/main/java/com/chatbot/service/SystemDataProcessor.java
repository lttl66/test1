package com.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemDataProcessor {

    private final ObjectMapper objectMapper;

    /**
     * Process system data based on user intent
     */
    public Map<String, Object> processSystemData(Map<String, Object> systemContext, String intent) {
        if (systemContext == null || systemContext.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> processedData = new HashMap<>();

        try {
            switch (intent != null ? intent.toLowerCase() : "general_query") {
                case "system_data_query":
                    processedData = processSystemDataQuery(systemContext);
                    break;
                case "user_management":
                    processedData = processUserManagementData(systemContext);
                    break;
                case "system_status":
                    processedData = processSystemStatus(systemContext);
                    break;
                case "list_query":
                    processedData = processListData(systemContext);
                    break;
                case "table_query":
                    processedData = processTableData(systemContext);
                    break;
                case "report_generation":
                    processedData = processReportData(systemContext);
                    break;
                default:
                    processedData = processGeneralContext(systemContext);
                    break;
            }

            // Add metadata about the processed data
            processedData.put("_metadata", Map.of(
                "intent", intent,
                "processed_at", System.currentTimeMillis(),
                "data_keys", processedData.keySet().stream()
                    .filter(key -> !key.startsWith("_"))
                    .collect(Collectors.toList())
            ));

        } catch (Exception e) {
            log.error("Error processing system data: {}", e.getMessage(), e);
            processedData.put("error", "Failed to process system data: " + e.getMessage());
        }

        return processedData;
    }

    /**
     * Analyze system context to extract relevant information
     */
    public Map<String, Object> analyzeContext(Map<String, Object> systemContext) {
        Map<String, Object> analysis = new HashMap<>();

        if (systemContext == null || systemContext.isEmpty()) {
            return analysis;
        }

        try {
            // Analyze data structure and content
            analysis.put("has_user_data", hasUserData(systemContext));
            analysis.put("has_system_metrics", hasSystemMetrics(systemContext));
            analysis.put("has_list_data", hasListData(systemContext));
            analysis.put("has_table_data", hasTableData(systemContext));
            analysis.put("data_complexity", assessDataComplexity(systemContext));
            analysis.put("suggested_format", suggestResponseFormat(systemContext));

            // Extract key entities and metrics
            analysis.put("key_entities", extractKeyEntities(systemContext));
            analysis.put("metrics_summary", extractMetricsSummary(systemContext));
            analysis.put("data_categories", categorizeData(systemContext));

            // Determine appropriate visualization
            analysis.put("visualization_type", determineVisualization(systemContext));

        } catch (Exception e) {
            log.error("Error analyzing context: {}", e.getMessage(), e);
            analysis.put("error", "Failed to analyze context");
        }

        return analysis;
    }

    private Map<String, Object> processSystemDataQuery(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // Extract system information
        result.put("system_info", extractSystemInfo(systemContext));
        result.put("performance_metrics", extractPerformanceMetrics(systemContext));
        result.put("resource_usage", extractResourceUsage(systemContext));
        result.put("active_sessions", extractActiveSessions(systemContext));

        return result;
    }

    private Map<String, Object> processUserManagementData(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // Extract user-related data
        if (systemContext.containsKey("users")) {
            Object usersData = systemContext.get("users");
            result.put("user_list", processUserList(usersData));
            result.put("user_stats", generateUserStats(usersData));
        }

        if (systemContext.containsKey("roles")) {
            result.put("roles", systemContext.get("roles"));
        }

        if (systemContext.containsKey("permissions")) {
            result.put("permissions", systemContext.get("permissions"));
        }

        result.put("response_format", "table");
        return result;
    }

    private Map<String, Object> processSystemStatus(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // Extract system status information
        result.put("status", getSystemStatus(systemContext));
        result.put("uptime", getSystemUptime(systemContext));
        result.put("health_check", performHealthCheck(systemContext));
        result.put("alerts", extractAlerts(systemContext));

        result.put("response_format", "card");
        return result;
    }

    private Map<String, Object> processListData(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // Find and format list-type data
        for (Map.Entry<String, Object> entry : systemContext.entrySet()) {
            if (entry.getValue() instanceof List) {
                result.put(entry.getKey(), formatListData((List<?>) entry.getValue()));
            }
        }

        result.put("response_format", "list");
        return result;
    }

    private Map<String, Object> processTableData(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // Find and format table-type data
        for (Map.Entry<String, Object> entry : systemContext.entrySet()) {
            Object value = entry.getValue();
            if (isTableData(value)) {
                result.put(entry.getKey(), formatTableData(value));
            }
        }

        result.put("response_format", "table");
        return result;
    }

    private Map<String, Object> processReportData(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // Generate summary statistics
        result.put("summary", generateSummaryStats(systemContext));
        result.put("trends", analyzeTrends(systemContext));
        result.put("insights", generateInsights(systemContext));

        result.put("response_format", "card");
        return result;
    }

    private Map<String, Object> processGeneralContext(Map<String, Object> systemContext) {
        Map<String, Object> result = new HashMap<>();

        // General processing - extract relevant information
        for (Map.Entry<String, Object> entry : systemContext.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (isRelevantData(key, value)) {
                result.put(key, simplifyData(value));
            }
        }

        return result;
    }

    // Helper methods
    private boolean hasUserData(Map<String, Object> context) {
        return context.containsKey("users") || context.containsKey("user") || 
               context.containsKey("userList") || context.containsKey("currentUser");
    }

    private boolean hasSystemMetrics(Map<String, Object> context) {
        return context.containsKey("metrics") || context.containsKey("performance") ||
               context.containsKey("cpu") || context.containsKey("memory");
    }

    private boolean hasListData(Map<String, Object> context) {
        return context.values().stream().anyMatch(value -> value instanceof List);
    }

    private boolean hasTableData(Map<String, Object> context) {
        return context.values().stream().anyMatch(this::isTableData);
    }

    private String assessDataComplexity(Map<String, Object> context) {
        int complexity = 0;
        
        for (Object value : context.values()) {
            if (value instanceof Map) complexity += 2;
            else if (value instanceof List) complexity += 1;
            else complexity += 0;
        }

        if (complexity > 10) return "high";
        else if (complexity > 5) return "medium";
        else return "low";
    }

    private String suggestResponseFormat(Map<String, Object> context) {
        if (hasTableData(context)) return "table";
        else if (hasListData(context)) return "list";
        else if (hasSystemMetrics(context)) return "card";
        else return "text";
    }

    private List<String> extractKeyEntities(Map<String, Object> context) {
        return context.keySet().stream()
            .filter(key -> !key.startsWith("_"))
            .limit(5)
            .collect(Collectors.toList());
    }

    private Map<String, Object> extractMetricsSummary(Map<String, Object> context) {
        Map<String, Object> metrics = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (isNumericData(entry.getValue())) {
                metrics.put(entry.getKey(), entry.getValue());
            }
        }
        
        return metrics;
    }

    private List<String> categorizeData(Map<String, Object> context) {
        List<String> categories = new ArrayList<>();
        
        if (hasUserData(context)) categories.add("user_management");
        if (hasSystemMetrics(context)) categories.add("system_metrics");
        if (hasListData(context)) categories.add("list_data");
        if (hasTableData(context)) categories.add("table_data");
        
        return categories;
    }

    private String determineVisualization(Map<String, Object> context) {
        if (hasSystemMetrics(context)) return "metrics_dashboard";
        else if (hasTableData(context)) return "data_table";
        else if (hasListData(context)) return "list_view";
        else return "text_display";
    }

    private Map<String, Object> extractSystemInfo(Map<String, Object> context) {
        Map<String, Object> systemInfo = new HashMap<>();
        
        if (context.containsKey("system")) {
            systemInfo.putAll((Map<String, Object>) context.get("system"));
        }
        
        return systemInfo;
    }

    private Map<String, Object> extractPerformanceMetrics(Map<String, Object> context) {
        Map<String, Object> metrics = new HashMap<>();
        
        String[] metricKeys = {"cpu", "memory", "disk", "network", "performance"};
        for (String key : metricKeys) {
            if (context.containsKey(key)) {
                metrics.put(key, context.get(key));
            }
        }
        
        return metrics;
    }

    private Map<String, Object> extractResourceUsage(Map<String, Object> context) {
        Map<String, Object> resources = new HashMap<>();
        
        if (context.containsKey("resources")) {
            resources.putAll((Map<String, Object>) context.get("resources"));
        }
        
        return resources;
    }

    private Object extractActiveSessions(Map<String, Object> context) {
        return context.getOrDefault("activeSessions", "No active session data available");
    }

    private Object processUserList(Object usersData) {
        if (usersData instanceof List) {
            List<?> users = (List<?>) usersData;
            return users.stream()
                .limit(10) // Limit to first 10 users for response
                .collect(Collectors.toList());
        }
        return usersData;
    }

    private Map<String, Object> generateUserStats(Object usersData) {
        Map<String, Object> stats = new HashMap<>();
        
        if (usersData instanceof List) {
            List<?> users = (List<?>) usersData;
            stats.put("total_users", users.size());
            stats.put("displayed_users", Math.min(users.size(), 10));
        }
        
        return stats;
    }

    private String getSystemStatus(Map<String, Object> context) {
        return (String) context.getOrDefault("status", "Unknown");
    }

    private Object getSystemUptime(Map<String, Object> context) {
        return context.getOrDefault("uptime", "Unknown");
    }

    private Map<String, Object> performHealthCheck(Map<String, Object> context) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("timestamp", System.currentTimeMillis());
        
        if (context.containsKey("health")) {
            health.putAll((Map<String, Object>) context.get("health"));
        }
        
        return health;
    }

    private Object extractAlerts(Map<String, Object> context) {
        return context.getOrDefault("alerts", new ArrayList<>());
    }

    private Object formatListData(List<?> listData) {
        return listData.stream()
            .limit(20) // Limit list items for UI performance
            .collect(Collectors.toList());
    }

    private boolean isTableData(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return !list.isEmpty() && list.get(0) instanceof Map;
        }
        return false;
    }

    private Object formatTableData(Object data) {
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            return list.stream()
                .limit(50) // Limit table rows for UI performance
                .collect(Collectors.toList());
        }
        return data;
    }

    private Map<String, Object> generateSummaryStats(Map<String, Object> context) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_data_points", context.size());
        summary.put("data_types", context.values().stream()
            .map(Object::getClass)
            .map(Class::getSimpleName)
            .distinct()
            .collect(Collectors.toList()));
        return summary;
    }

    private Map<String, Object> analyzeTrends(Map<String, Object> context) {
        Map<String, Object> trends = new HashMap<>();
        // Placeholder for trend analysis
        trends.put("status", "No trend data available");
        return trends;
    }

    private List<String> generateInsights(Map<String, Object> context) {
        List<String> insights = new ArrayList<>();
        
        if (hasUserData(context)) {
            insights.add("User management data is available for analysis");
        }
        if (hasSystemMetrics(context)) {
            insights.add("System performance metrics can be monitored");
        }
        
        return insights;
    }

    private boolean isRelevantData(String key, Object value) {
        // Filter out internal or irrelevant keys
        return !key.startsWith("_") && value != null;
    }

    private Object simplifyData(Object data) {
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            if (map.size() > 5) {
                // Simplify large maps
                return map.entrySet().stream()
                    .limit(5)
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    ));
            }
        } else if (data instanceof List) {
            List<?> list = (List<?>) data;
            if (list.size() > 10) {
                return list.subList(0, 10);
            }
        }
        return data;
    }

    private boolean isNumericData(Object value) {
        return value instanceof Number;
    }
}