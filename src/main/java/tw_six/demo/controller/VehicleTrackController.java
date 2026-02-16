package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.service.VehicleTrackService;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tracks")
public class VehicleTrackController {
    
    private final VehicleTrackService vehicleTrackService;
    
    @Autowired
    public VehicleTrackController(VehicleTrackService vehicleTrackService) {
        this.vehicleTrackService = vehicleTrackService;
    }
    
    @PostMapping
    public ApiResponse<VehicleTrack> addTrack(@RequestBody VehicleTrack track) {
        VehicleTrack savedTrack = vehicleTrackService.saveTrack(track);
        return ApiResponse.success("轨迹记录保存成功", savedTrack);
    }
    
    @GetMapping("/car/{carName}")
    public ApiResponse<List<VehicleTrack>> getTracksByCar(@PathVariable String carName) {
        List<VehicleTrack> tracks = vehicleTrackService.getTracksByCarName(carName);
        return ApiResponse.success(tracks);
    }
    
    @GetMapping("/car/{carName}/recent")
    public ApiResponse<List<VehicleTrack>> getRecentTracks(
        @PathVariable String carName,
        @RequestParam(defaultValue = "60") int minutes) {
        List<VehicleTrack> tracks = vehicleTrackService.getRecentTracks(carName, minutes);
        return ApiResponse.success(tracks);
    }
    
    @GetMapping("/cars")
    public ApiResponse<List<String>> getAllCarNames() {
        List<String> carNames = vehicleTrackService.getAllCarNames();
        return ApiResponse.success(carNames);
    }
    
    @GetMapping("/car/{carName}/range")
    public ApiResponse<List<VehicleTrack>> getTracksByTimeRange(
        @PathVariable String carName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<VehicleTrack> tracks = vehicleTrackService.getTracksByTimeRange(carName, startTime, endTime);
        return ApiResponse.success(tracks);
    }
}