package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.service.FenceAlertService;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class FenceAlertController {
    
    private final FenceAlertService fenceAlertService;
    
    @Autowired
    public FenceAlertController(FenceAlertService fenceAlertService) {
        this.fenceAlertService = fenceAlertService;
    }
    
    @GetMapping
    public ApiResponse<List<FenceAlert>> getAllAlerts() {
        List<FenceAlert> alerts = fenceAlertService.getAllAlerts();
        return ApiResponse.success(alerts);
    }
    
    @GetMapping("/unhandled")
    public ApiResponse<List<FenceAlert>> getUnhandledAlerts() {
        List<FenceAlert> alerts = fenceAlertService.getUnhandledAlerts();
        return ApiResponse.success(alerts);
    }
    
    @GetMapping("/count/unhandled")
    public ApiResponse<Long> getUnhandledAlertCount() {
        Long count = fenceAlertService.getUnhandledAlertCount();
        return ApiResponse.success(count);
    }
    
    @GetMapping("/car/{carName}")
    public ApiResponse<List<FenceAlert>> getAlertsByCar(@PathVariable String carName) {
        List<FenceAlert> alerts = fenceAlertService.getAlertsByCar(carName);
        return ApiResponse.success(alerts);
    }
    
    @GetMapping("/range")
    public ApiResponse<List<FenceAlert>> getAlertsByTimeRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<FenceAlert> alerts = fenceAlertService.getAlertsByTimeRange(startTime, endTime);
        return ApiResponse.success(alerts);
    }
    
    @PutMapping("/{id}/handle")
    public ApiResponse<FenceAlert> handleAlert(@PathVariable Long id, @RequestParam String handler) {
        try {
            FenceAlert handledAlert = fenceAlertService.handleAlert(id, handler);
            return ApiResponse.success("告警处理成功", handledAlert);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAlert(@PathVariable Long id) {
        try {
            fenceAlertService.deleteAlert(id);
            return ApiResponse.success("告警记录删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}