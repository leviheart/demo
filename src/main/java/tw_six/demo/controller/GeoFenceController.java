package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.GeoFence;
import tw_six.demo.service.GeoFenceService;
import java.util.List;

@RestController
@RequestMapping("/api/fences")
public class GeoFenceController {
    
    private final GeoFenceService geoFenceService;
    
    @Autowired
    public GeoFenceController(GeoFenceService geoFenceService) {
        this.geoFenceService = geoFenceService;
    }
    
    @PostMapping
    public ApiResponse<GeoFence> createFence(@RequestBody GeoFence fence) {
        GeoFence createdFence = geoFenceService.createFence(fence);
        return ApiResponse.success("围栏创建成功", createdFence);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<GeoFence> updateFence(@PathVariable Long id, @RequestBody GeoFence fence) {
        try {
            GeoFence updatedFence = geoFenceService.updateFence(id, fence);
            return ApiResponse.success("围栏更新成功", updatedFence);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFence(@PathVariable Long id) {
        try {
            geoFenceService.deleteFence(id);
            return ApiResponse.success("围栏删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @GetMapping
    public ApiResponse<List<GeoFence>> getAllFences() {
        List<GeoFence> fences = geoFenceService.getAllFences();
        return ApiResponse.success(fences);
    }
    
    @GetMapping("/active")
    public ApiResponse<List<GeoFence>> getActiveFences() {
        List<GeoFence> fences = geoFenceService.getActiveFences();
        return ApiResponse.success(fences);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<GeoFence> getFenceById(@PathVariable Long id) {
        try {
            GeoFence fence = geoFenceService.getFenceById(id);
            return ApiResponse.success(fence);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}