package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * 애플리케이션 상태 확인용 엔드포인트
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "애플리케이션 상태 확인 API")
public class HealthController {

    /**
     * 기본 Health Check
     * GET /api/health
     */
    @GetMapping
    @Operation(summary = "Health Check", description = "애플리케이션 기본 상태 확인")
    public ApiResponse<Map<String, Object>> health() {
        log.info("Health check requested");

        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", "SoIce MES Backend");
        healthInfo.put("version", "0.1.0-SNAPSHOT");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("message", "서버가 정상적으로 실행 중입니다.");

        return ApiResponse.success("Health check successful", healthInfo);
    }

    /**
     * 상세 Health Check
     * GET /api/health/detail
     */
    @GetMapping("/detail")
    @Operation(summary = "Detailed Health Check", description = "애플리케이션 상세 상태 확인 (메모리, 시스템 정보 포함)")
    public ApiResponse<Map<String, Object>> healthDetail() {
        log.info("Detailed health check requested");

        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", "SoIce MES Backend");
        healthInfo.put("version", "0.1.0-SNAPSHOT");
        healthInfo.put("timestamp", LocalDateTime.now());

        // Runtime 정보
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("availableProcessors", runtime.availableProcessors());
        runtimeInfo.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");

        healthInfo.put("runtime", runtimeInfo);

        // System 정보
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));

        healthInfo.put("system", systemInfo);

        return ApiResponse.success("Detailed health check successful", healthInfo);
    }
}
