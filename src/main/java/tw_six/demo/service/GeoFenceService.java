package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.GeoFence;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.repository.GeoFenceRepository;
import tw_six.demo.repository.FenceAlertRepository;
import java.util.List;

@Service
@Transactional
public class GeoFenceService {
    
    private final GeoFenceRepository geoFenceRepository;
    private final FenceAlertRepository fenceAlertRepository;
    
    @Autowired
    public GeoFenceService(GeoFenceRepository geoFenceRepository, FenceAlertRepository fenceAlertRepository) {
        this.geoFenceRepository = geoFenceRepository;
        this.fenceAlertRepository = fenceAlertRepository;
    }
    
    public GeoFence createFence(GeoFence fence) {
        return geoFenceRepository.save(fence);
    }
    
    public GeoFence updateFence(Long id, GeoFence fenceDetails) {
        GeoFence fence = geoFenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("围栏不存在: " + id));
        
        fence.setFenceName(fenceDetails.getFenceName());
        fence.setCenterLatitude(fenceDetails.getCenterLatitude());
        fence.setCenterLongitude(fenceDetails.getCenterLongitude());
        fence.setRadius(fenceDetails.getRadius());
        fence.setFenceType(fenceDetails.getFenceType());
        fence.setAlertType(fenceDetails.getAlertType());
        fence.setStatus(fenceDetails.getStatus());
        fence.setDescription(fenceDetails.getDescription());
        
        return geoFenceRepository.save(fence);
    }
    
    public void deleteFence(Long id) {
        if (!geoFenceRepository.existsById(id)) {
            throw new RuntimeException("围栏不存在: " + id);
        }
        geoFenceRepository.deleteById(id);
    }
    
    public List<GeoFence> getAllFences() {
        return geoFenceRepository.findAll();
    }
    
    public List<GeoFence> getActiveFences() {
        return geoFenceRepository.findByStatus("active");
    }
    
    public GeoFence getFenceById(Long id) {
        return geoFenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("围栏不存在: " + id));
    }
    
    // 检查车辆是否进入或离开围栏
    public void checkFenceViolation(String carName, Double latitude, Double longitude) {
        List<GeoFence> activeFences = getActiveFences();
        
        for (GeoFence fence : activeFences) {
            boolean isInFence = isPointInCircle(latitude, longitude, 
                fence.getCenterLatitude(), fence.getCenterLongitude(), fence.getRadius());
            
            // 这里应该检查车辆之前的位置状态来判断是进入还是离开
            // 简化实现：假设每次都触发告警
            if (isInFence && ("entry".equals(fence.getAlertType()) || "both".equals(fence.getAlertType()))) {
                createAlert(fence, carName, latitude, longitude, "entry");
            } else if (!isInFence && ("exit".equals(fence.getAlertType()) || "both".equals(fence.getAlertType()))) {
                createAlert(fence, carName, latitude, longitude, "exit");
            }
        }
    }
    
    // 计算两点间距离（米）
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }
    
    // 判断点是否在圆形围栏内
    private boolean isPointInCircle(double pointLat, double pointLng, 
                                  double centerLat, double centerLng, double radius) {
        double distance = calculateDistance(pointLat, pointLng, centerLat, centerLng);
        return distance <= radius;
    }
    
    private void createAlert(GeoFence fence, String carName, Double latitude, Double longitude, String alertType) {
        FenceAlert alert = new FenceAlert();
        alert.setGeoFence(fence);
        alert.setCarName(carName);
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        alert.setAlertType(alertType);
        alert.setIsHandled(false);
        
        fenceAlertRepository.save(alert);
    }
}