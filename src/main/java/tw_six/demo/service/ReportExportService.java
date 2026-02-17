package tw_six.demo.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw_six.demo.entity.CarLocation;
import tw_six.demo.entity.DrivingStatistics;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.repository.CarLocationRepository;
import tw_six.demo.repository.DrivingStatisticsRepository;
import tw_six.demo.repository.FenceAlertRepository;
import tw_six.demo.repository.VehicleTrackRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报表导出服务 - Excel报表生成
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供各类数据的Excel报表导出功能，使用Apache POI生成Excel文件。
 * 支持车辆位置、驾驶统计、告警记录、轨迹数据等多种报表。
 * 
 * 【支持的报表类型】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 报表类型          │ 内容                                                  │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ 车辆位置报表      │ 所有车辆的当前位置信息                                │
 * │ 驾驶统计报表      │ 车辆的里程、时长、速度等统计数据                      │
 * │ 告警记录报表      │ 围栏告警历史记录                                      │
 * │ 轨迹数据报表      │ 指定车辆的轨迹点数据                                  │
 * │ 综合报表          │ 多种数据的汇总报表                                    │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【Excel格式说明】
 * - 使用.xlsx格式（Excel 2007+）
 * - 包含标题行（加粗、背景色）
 * - 自动调整列宽
 * - 支持日期格式化
 * 
 * 【关联文件】
 * - ReportController.java: 报表导出API
 * - Apache POI库: Excel文件处理
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class ReportExportService {

    private final CarLocationRepository carLocationRepository;
    private final DrivingStatisticsRepository drivingStatisticsRepository;
    private final FenceAlertRepository fenceAlertRepository;
    private final VehicleTrackRepository vehicleTrackRepository;

    /** 日期时间格式化器 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public ReportExportService(CarLocationRepository carLocationRepository,
            DrivingStatisticsRepository drivingStatisticsRepository,
            FenceAlertRepository fenceAlertRepository,
            VehicleTrackRepository vehicleTrackRepository) {
        this.carLocationRepository = carLocationRepository;
        this.drivingStatisticsRepository = drivingStatisticsRepository;
        this.fenceAlertRepository = fenceAlertRepository;
        this.vehicleTrackRepository = vehicleTrackRepository;
    }

    /**
     * 导出车辆位置报表
     * 
     * @return Excel文件字节数组
     * @throws IOException IO异常
     */
    public byte[] exportVehicleLocations() throws IOException {
        List<CarLocation> locations = carLocationRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("车辆位置");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "车牌号", "纬度", "经度", "速度(km/h)", "方向(度)", "状态", "更新时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (CarLocation location : locations) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(location.getId());
                row.createCell(1).setCellValue(location.getCarName());
                row.createCell(2).setCellValue(location.getLatitude() != null ? location.getLatitude() : 0);
                row.createCell(3).setCellValue(location.getLongitude() != null ? location.getLongitude() : 0);
                row.createCell(4).setCellValue(location.getSpeed() != null ? location.getSpeed() : 0);
                row.createCell(5).setCellValue(location.getDirection() != null ? location.getDirection() : 0);
                row.createCell(6).setCellValue(location.getStatus() != null ? location.getStatus() : "");
                row.createCell(7).setCellValue(location.getUpdateTime() != null ? 
                    location.getUpdateTime().format(DATE_TIME_FORMATTER) : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 导出驾驶统计报表
     * 
     * @return Excel文件字节数组
     * @throws IOException IO异常
     */
    public byte[] exportDrivingStatistics() throws IOException {
        List<DrivingStatistics> statistics = drivingStatisticsRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("驾驶统计");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "车牌号", "日期", "行驶里程(km)", "行驶时长(分钟)", "平均速度(km/h)", "最高速度(km/h)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (DrivingStatistics stats : statistics) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(stats.getId());
                row.createCell(1).setCellValue(stats.getCarName());
                row.createCell(2).setCellValue(stats.getRecordDate() != null ? 
                    stats.getRecordDate().toString() : "");
                row.createCell(3).setCellValue(stats.getTotalMileage() != null ? stats.getTotalMileage() : 0);
                row.createCell(4).setCellValue(stats.getDrivingTimeMinutes() != null ? stats.getDrivingTimeMinutes() : 0);
                row.createCell(5).setCellValue(stats.getAverageSpeed() != null ? stats.getAverageSpeed() : 0);
                row.createCell(6).setCellValue(stats.getMaxSpeed() != null ? stats.getMaxSpeed() : 0);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 导出告警记录报表
     * 
     * @return Excel文件字节数组
     * @throws IOException IO异常
     */
    public byte[] exportFenceAlerts() throws IOException {
        List<FenceAlert> alerts = fenceAlertRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("告警记录");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "车牌号", "告警类型", "纬度", "经度", "告警时间", "是否已处理", "处理人"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (FenceAlert alert : alerts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(alert.getId());
                row.createCell(1).setCellValue(alert.getCarName());
                row.createCell(2).setCellValue(alert.getAlertType() != null ? alert.getAlertType() : "");
                row.createCell(3).setCellValue(alert.getLatitude() != null ? alert.getLatitude() : 0);
                row.createCell(4).setCellValue(alert.getLongitude() != null ? alert.getLongitude() : 0);
                row.createCell(5).setCellValue(alert.getAlertTime() != null ? 
                    alert.getAlertTime().format(DATE_TIME_FORMATTER) : "");
                row.createCell(6).setCellValue(alert.getIsHandled() != null && alert.getIsHandled() ? "是" : "否");
                row.createCell(7).setCellValue(alert.getHandler() != null ? alert.getHandler() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 导出轨迹数据报表
     * 
     * @param carName 车辆名称
     * @return Excel文件字节数组
     * @throws IOException IO异常
     */
    public byte[] exportVehicleTracks(String carName) throws IOException {
        List<VehicleTrack> tracks = carName != null ? 
            vehicleTrackRepository.findByCarNameOrderByRecordTimeAsc(carName) :
            vehicleTrackRepository.findAllByOrderByRecordTimeAsc();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("轨迹数据");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "车牌号", "纬度", "经度", "速度(km/h)", "方向(度)", "记录时间", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (VehicleTrack track : tracks) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(track.getId());
                row.createCell(1).setCellValue(track.getCarName());
                row.createCell(2).setCellValue(track.getLatitude() != null ? track.getLatitude() : 0);
                row.createCell(3).setCellValue(track.getLongitude() != null ? track.getLongitude() : 0);
                row.createCell(4).setCellValue(track.getSpeed() != null ? track.getSpeed() : 0);
                row.createCell(5).setCellValue(track.getDirection() != null ? track.getDirection() : 0);
                row.createCell(6).setCellValue(track.getRecordTime() != null ? 
                    track.getRecordTime().format(DATE_TIME_FORMATTER) : "");
                row.createCell(7).setCellValue(track.getStatus() != null ? track.getStatus() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 导出综合报表
     * 
     * 包含多个工作表：车辆位置、驾驶统计、告警记录
     * 
     * @return Excel文件字节数组
     * @throws IOException IO异常
     */
    public byte[] exportComprehensiveReport() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);

            // 工作表1：车辆位置
            createVehicleLocationSheet(workbook, headerStyle);

            // 工作表2：驾驶统计
            createDrivingStatisticsSheet(workbook, headerStyle);

            // 工作表3：告警记录
            createFenceAlertSheet(workbook, headerStyle);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createVehicleLocationSheet(Workbook workbook, CellStyle headerStyle) {
        List<CarLocation> locations = carLocationRepository.findAll();
        Sheet sheet = workbook.createSheet("车辆位置");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "车牌号", "纬度", "经度", "速度(km/h)", "方向(度)", "状态", "更新时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (CarLocation location : locations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(location.getId());
            row.createCell(1).setCellValue(location.getCarName());
            row.createCell(2).setCellValue(location.getLatitude() != null ? location.getLatitude() : 0);
            row.createCell(3).setCellValue(location.getLongitude() != null ? location.getLongitude() : 0);
            row.createCell(4).setCellValue(location.getSpeed() != null ? location.getSpeed() : 0);
            row.createCell(5).setCellValue(location.getDirection() != null ? location.getDirection() : 0);
            row.createCell(6).setCellValue(location.getStatus() != null ? location.getStatus() : "");
            row.createCell(7).setCellValue(location.getUpdateTime() != null ? 
                location.getUpdateTime().format(DATE_TIME_FORMATTER) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDrivingStatisticsSheet(Workbook workbook, CellStyle headerStyle) {
        List<DrivingStatistics> statistics = drivingStatisticsRepository.findAll();
        Sheet sheet = workbook.createSheet("驾驶统计");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "车牌号", "日期", "行驶里程(km)", "行驶时长(分钟)", "平均速度(km/h)", "最高速度(km/h)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (DrivingStatistics stats : statistics) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stats.getId());
            row.createCell(1).setCellValue(stats.getCarName());
            row.createCell(2).setCellValue(stats.getRecordDate() != null ? stats.getRecordDate().toString() : "");
            row.createCell(3).setCellValue(stats.getTotalMileage() != null ? stats.getTotalMileage() : 0);
            row.createCell(4).setCellValue(stats.getDrivingTimeMinutes() != null ? stats.getDrivingTimeMinutes() : 0);
            row.createCell(5).setCellValue(stats.getAverageSpeed() != null ? stats.getAverageSpeed() : 0);
            row.createCell(6).setCellValue(stats.getMaxSpeed() != null ? stats.getMaxSpeed() : 0);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createFenceAlertSheet(Workbook workbook, CellStyle headerStyle) {
        List<FenceAlert> alerts = fenceAlertRepository.findAll();
        Sheet sheet = workbook.createSheet("告警记录");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "车牌号", "告警类型", "纬度", "经度", "告警时间", "是否已处理", "处理人"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (FenceAlert alert : alerts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(alert.getId());
            row.createCell(1).setCellValue(alert.getCarName());
            row.createCell(2).setCellValue(alert.getAlertType() != null ? alert.getAlertType() : "");
            row.createCell(3).setCellValue(alert.getLatitude() != null ? alert.getLatitude() : 0);
            row.createCell(4).setCellValue(alert.getLongitude() != null ? alert.getLongitude() : 0);
            row.createCell(5).setCellValue(alert.getAlertTime() != null ? 
                alert.getAlertTime().format(DATE_TIME_FORMATTER) : "");
            row.createCell(6).setCellValue(alert.getIsHandled() != null && alert.getIsHandled() ? "是" : "否");
            row.createCell(7).setCellValue(alert.getHandler() != null ? alert.getHandler() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建标题行样式
     * 
     * @param workbook 工作簿
     * @return 样式对象
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }
}
