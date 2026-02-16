package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.DrivingStatistics;
import tw_six.demo.service.DrivingStatisticsService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class DrivingStatisticsController {
    
    private final DrivingStatisticsService drivingStatisticsService;
    
    @Autowired
    public DrivingStatisticsController(DrivingStatisticsService drivingStatisticsService) {
        this.drivingStatisticsService = drivingStatisticsService;
    }
    
    @PostMapping
    public ApiResponse<DrivingStatistics> saveStatistics(@RequestBody DrivingStatistics statistics) {
        DrivingStatistics savedStats = drivingStatisticsService.saveStatistics(statistics);
        return ApiResponse.success("统计数据保存成功", savedStats);
    }
    
    @GetMapping("/car/{carName}")
    public ApiResponse<List<DrivingStatistics>> getStatisticsByCar(@PathVariable String carName) {
        List<DrivingStatistics> statistics = drivingStatisticsService.getStatisticsByCar(carName);
        return ApiResponse.success(statistics);
    }
    
    @GetMapping("/car/{carName}/range")
    public ApiResponse<List<DrivingStatistics>> getStatisticsByDateRange(
        @PathVariable String carName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DrivingStatistics> statistics = drivingStatisticsService.getStatisticsByDateRange(carName, startDate, endDate);
        return ApiResponse.success(statistics);
    }
    
    @GetMapping("/daily-ranking")
    public ApiResponse<List<DrivingStatistics>> getDailyRanking(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DrivingStatistics> ranking = drivingStatisticsService.getDailyRanking(date);
        return ApiResponse.success(ranking);
    }
    
    @GetMapping("/summary/{carName}")
    public ApiResponse<Map<String, Object>> getCarSummary(@PathVariable String carName) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageMileage", drivingStatisticsService.getAverageMileage(carName));
        summary.put("totalMileage", drivingStatisticsService.getTotalMileage(carName));
        summary.put("latestStats", drivingStatisticsService.getLatestStatistics(carName));
        return ApiResponse.success(summary);
    }
}