package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.service.ReportExportService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 报表导出控制器 - Excel报表下载API
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供各类报表的Excel导出下载接口。
 * 支持车辆位置、驾驶统计、告警记录、轨迹数据等报表导出。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                              │ 功能描述                     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET  │ /api/reports/vehicles             │ 导出车辆位置报表             │
 * │ GET  │ /api/reports/statistics           │ 导出驾驶统计报表             │
 * │ GET  │ /api/reports/alerts               │ 导出告警记录报表             │
 * │ GET  │ /api/reports/tracks               │ 导出轨迹数据报表             │
 * │ GET  │ /api/reports/comprehensive        │ 导出综合报表                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【文件命名规则】
 * 报表类型_日期时间.xlsx
 * 例如：车辆位置报表_20240115_103000.xlsx
 * 
 * 【关联文件】
 * - ReportExportService.java: 报表生成服务
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportExportService reportExportService;

    @Autowired
    public ReportController(ReportExportService reportExportService) {
        this.reportExportService = reportExportService;
    }

    /**
     * 导出车辆位置报表
     * 
     * @return Excel文件
     * @throws Exception 异常
     */
    @GetMapping("/vehicles")
    public ResponseEntity<byte[]> exportVehicleLocations() throws Exception {
        byte[] content = reportExportService.exportVehicleLocations();
        String filename = generateFilename("车辆位置报表");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content);
    }

    /**
     * 导出驾驶统计报表
     * 
     * @return Excel文件
     * @throws Exception 异常
     */
    @GetMapping("/statistics")
    public ResponseEntity<byte[]> exportDrivingStatistics() throws Exception {
        byte[] content = reportExportService.exportDrivingStatistics();
        String filename = generateFilename("驾驶统计报表");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content);
    }

    /**
     * 导出告警记录报表
     * 
     * @return Excel文件
     * @throws Exception 异常
     */
    @GetMapping("/alerts")
    public ResponseEntity<byte[]> exportFenceAlerts() throws Exception {
        byte[] content = reportExportService.exportFenceAlerts();
        String filename = generateFilename("告警记录报表");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content);
    }

    /**
     * 导出轨迹数据报表
     * 
     * @param carName 车辆名称（可选，不传则导出所有）
     * @return Excel文件
     * @throws Exception 异常
     */
    @GetMapping("/tracks")
    public ResponseEntity<byte[]> exportVehicleTracks(
            @RequestParam(required = false) String carName) throws Exception {
        byte[] content = reportExportService.exportVehicleTracks(carName);
        String filename = generateFilename(carName != null ? 
            carName + "_轨迹数据报表" : "轨迹数据报表");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content);
    }

    /**
     * 导出综合报表
     * 
     * 包含多个工作表：车辆位置、驾驶统计、告警记录
     * 
     * @return Excel文件
     * @throws Exception 异常
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<byte[]> exportComprehensiveReport() throws Exception {
        byte[] content = reportExportService.exportComprehensiveReport();
        String filename = generateFilename("综合报表");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content);
    }

    /**
     * 生成报表文件名
     * 
     * 格式：报表类型_日期时间.xlsx
     * 
     * @param reportType 报表类型
     * @return 文件名
     */
    private String generateFilename(String reportType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return reportType + "_" + timestamp + ".xlsx";
    }
}
