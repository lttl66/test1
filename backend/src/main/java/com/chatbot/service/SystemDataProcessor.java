package com.chatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SystemDataProcessor {
    
    /**
     * Collect comprehensive system information
     */
    public Map<String, Object> collectSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        try {
            // Basic system information
            systemInfo.put("basic", collectBasicSystemInfo());
            
            // Performance metrics
            systemInfo.put("performance", collectPerformanceMetrics());
            
            // Network information
            systemInfo.put("network", collectNetworkInfo());
            
            // JVM information
            systemInfo.put("jvm", collectJvmInfo());
            
            // Process information
            systemInfo.put("process", collectProcessInfo());
            
            // Timestamp
            systemInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            log.error("Error collecting system information: {}", e.getMessage(), e);
            systemInfo.put("error", "Failed to collect system information: " + e.getMessage());
        }
        
        return systemInfo;
    }
    
    /**
     * Collect specific system data based on query type
     */
    public Map<String, Object> collectSpecificData(String dataType) {
        switch (dataType.toLowerCase()) {
            case "performance":
                return collectPerformanceMetrics();
            case "network":
                return collectNetworkInfo();
            case "jvm":
                return collectJvmInfo();
            case "process":
                return collectProcessInfo();
            case "basic":
                return collectBasicSystemInfo();
            default:
                return collectSystemInfo();
        }
    }
    
    /**
     * Format system data for AI processing
     */
    public String formatSystemDataForAI(Map<String, Object> systemData) {
        StringBuilder formatted = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : systemData.entrySet()) {
            if (entry.getValue() instanceof Map) {
                formatted.append(entry.getKey()).append(":\n");
                Map<?, ?> subMap = (Map<?, ?>) entry.getValue();
                for (Map.Entry<?, ?> subEntry : subMap.entrySet()) {
                    formatted.append("  ").append(subEntry.getKey()).append(": ").append(subEntry.getValue()).append("\n");
                }
            } else {
                formatted.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        return formatted.toString();
    }
    
    private Map<String, Object> collectBasicSystemInfo() {
        Map<String, Object> basicInfo = new HashMap<>();
        
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            basicInfo.put("os_name", osBean.getName());
            basicInfo.put("os_version", osBean.getVersion());
            basicInfo.put("os_arch", osBean.getArch());
            basicInfo.put("available_processors", osBean.getAvailableProcessors());
            basicInfo.put("system_load_average", osBean.getSystemLoadAverage());
            
            // Host information
            String hostname = InetAddress.getLocalHost().getHostName();
            basicInfo.put("hostname", hostname);
            
        } catch (Exception e) {
            log.error("Error collecting basic system info: {}", e.getMessage());
            basicInfo.put("error", e.getMessage());
        }
        
        return basicInfo;
    }
    
    private Map<String, Object> collectPerformanceMetrics() {
        Map<String, Object> performance = new HashMap<>();
        
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // Memory information
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = Runtime.getRuntime().maxMemory();
            
            performance.put("total_memory_mb", totalMemory / (1024 * 1024));
            performance.put("free_memory_mb", freeMemory / (1024 * 1024));
            performance.put("used_memory_mb", usedMemory / (1024 * 1024));
            performance.put("max_memory_mb", maxMemory / (1024 * 1024));
            performance.put("memory_usage_percent", (double) usedMemory / totalMemory * 100);
            
            // Heap memory
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            performance.put("heap_used_mb", heapUsed / (1024 * 1024));
            performance.put("heap_max_mb", heapMax / (1024 * 1024));
            performance.put("heap_usage_percent", (double) heapUsed / heapMax * 100);
            
            // Thread information
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            performance.put("thread_count", threadBean.getThreadCount());
            performance.put("peak_thread_count", threadBean.getPeakThreadCount());
            performance.put("daemon_thread_count", threadBean.getDaemonThreadCount());
            
            // CPU information
            performance.put("system_load_average", osBean.getSystemLoadAverage());
            performance.put("available_processors", osBean.getAvailableProcessors());
            
        } catch (Exception e) {
            log.error("Error collecting performance metrics: {}", e.getMessage());
            performance.put("error", e.getMessage());
        }
        
        return performance;
    }
    
    private Map<String, Object> collectNetworkInfo() {
        Map<String, Object> network = new HashMap<>();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            List<Map<String, Object>> interfaceList = new ArrayList<>();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Map<String, Object> interfaceInfo = new HashMap<>();
                interfaceInfo.put("name", networkInterface.getName());
                interfaceInfo.put("display_name", networkInterface.getDisplayName());
                interfaceInfo.put("mtu", networkInterface.getMTU());
                interfaceInfo.put("hardware_address", formatMacAddress(networkInterface.getHardwareAddress()));
                
                // Get IP addresses
                List<String> ipAddresses = new ArrayList<>();
                networkInterface.getInterfaceAddresses().forEach(addr -> {
                    ipAddresses.add(addr.getAddress().getHostAddress());
                });
                interfaceInfo.put("ip_addresses", ipAddresses);
                
                interfaceList.add(interfaceInfo);
            }
            
            network.put("interfaces", interfaceList);
            network.put("interface_count", interfaceList.size());
            
        } catch (Exception e) {
            log.error("Error collecting network info: {}", e.getMessage());
            network.put("error", e.getMessage());
        }
        
        return network;
    }
    
    private Map<String, Object> collectJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            
            jvm.put("java_version", System.getProperty("java.version"));
            jvm.put("java_vendor", System.getProperty("java.vendor"));
            jvm.put("java_home", System.getProperty("java.home"));
            jvm.put("java_class_path", System.getProperty("java.class.path"));
            jvm.put("java_library_path", System.getProperty("java.library.path"));
            
            // Memory information
            jvm.put("total_memory", runtime.totalMemory());
            jvm.put("free_memory", runtime.freeMemory());
            jvm.put("max_memory", runtime.maxMemory());
            jvm.put("available_processors", runtime.availableProcessors());
            
            // JVM uptime
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            jvm.put("uptime_seconds", uptime / 1000);
            jvm.put("uptime_formatted", formatUptime(uptime));
            
            // JVM arguments
            List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            jvm.put("jvm_arguments", inputArguments);
            
        } catch (Exception e) {
            log.error("Error collecting JVM info: {}", e.getMessage());
            jvm.put("error", e.getMessage());
        }
        
        return jvm;
    }
    
    private Map<String, Object> collectProcessInfo() {
        Map<String, Object> process = new HashMap<>();
        
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            
            process.put("pid", getProcessId());
            process.put("start_time", runtimeBean.getStartTime());
            process.put("start_time_formatted", new Date(runtimeBean.getStartTime()).toString());
            
            // Process CPU time
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            long cpuTime = threadBean.getCurrentThreadCpuTime();
            process.put("cpu_time_nanos", cpuTime);
            process.put("cpu_time_seconds", TimeUnit.NANOSECONDS.toSeconds(cpuTime));
            
            // User and system time
            long userTime = threadBean.getCurrentThreadUserTime();
            process.put("user_time_nanos", userTime);
            process.put("user_time_seconds", TimeUnit.NANOSECONDS.toSeconds(userTime));
            
        } catch (Exception e) {
            log.error("Error collecting process info: {}", e.getMessage());
            process.put("error", e.getMessage());
        }
        
        return process;
    }
    
    private String formatMacAddress(byte[] mac) {
        if (mac == null) {
            return "N/A";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }
    
    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
    
    private String getProcessId() {
        try {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            return processName.split("@")[0];
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Execute system command and return output
     */
    public String executeSystemCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("Command '{}' exited with code {}", command, exitCode);
            }
            
            return output.toString();
            
        } catch (Exception e) {
            log.error("Error executing command '{}': {}", command, e.getMessage());
            return "Error executing command: " + e.getMessage();
        }
    }
    
    /**
     * Get system data summary for quick overview
     */
    public Map<String, Object> getSystemSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Basic info
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            Runtime runtime = Runtime.getRuntime();
            
            summary.put("os", osBean.getName() + " " + osBean.getVersion());
            summary.put("hostname", InetAddress.getLocalHost().getHostName());
            summary.put("java_version", System.getProperty("java.version"));
            
            // Memory summary
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            summary.put("memory_usage_percent", Math.round((double) usedMemory / totalMemory * 100));
            summary.put("memory_used_mb", usedMemory / (1024 * 1024));
            summary.put("memory_total_mb", totalMemory / (1024 * 1024));
            
            // Performance summary
            summary.put("cpu_cores", osBean.getAvailableProcessors());
            summary.put("system_load", osBean.getSystemLoadAverage());
            summary.put("thread_count", ManagementFactory.getThreadMXBean().getThreadCount());
            
            // Uptime
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            summary.put("uptime", formatUptime(uptime));
            
        } catch (Exception e) {
            log.error("Error getting system summary: {}", e.getMessage());
            summary.put("error", e.getMessage());
        }
        
        return summary;
    }
}