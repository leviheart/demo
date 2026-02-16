package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.GeoFence;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.repository.GeoFenceRepository;
import tw_six.demo.repository.FenceAlertRepository;
import java.util.List;

/**
 * 地理围栏服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供地理围栏的核心业务逻辑，包括围栏管理、越界检测、告警生成等功能。
 * 是车辆监控系统的核心服务之一，负责实时监控车辆位置与围栏的关系。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名              │ 功能描述                       │ 返回值           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ createFence         │ 创建新围栏                     │ GeoFence         │
 * │ updateFence         │ 更新围栏信息                   │ GeoFence         │
 * │ deleteFence         │ 删除围栏                       │ void             │
 * │ getAllFences        │ 获取所有围栏                   │ List<GeoFence>   │
 * │ getActiveFences     │ 获取启用的围栏                 │ List<GeoFence>   │
 * │ getFenceById        │ 根据ID获取围栏                 │ GeoFence         │
 * │ updateFenceStatus   │ 更新围栏状态                   │ GeoFence         │
 * │ checkFenceViolation │ 检查车辆是否越界               │ void             │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【越界检测算法】
 * 1. 获取所有启用状态的围栏
 * 2. 计算车辆位置与围栏中心的距离
 * 3. 判断距离是否超过围栏半径
 * 4. 根据围栏告警类型生成相应告警
 * 
 * 【告警类型说明】
 * - entry: 进入围栏时告警
 * - exit: 离开围栏时告警
 * - both: 进出都告警
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.GeoFenceController
 * - 实体类: tw_six.demo.entity.GeoFence, FenceAlert
 * - 仓库层: tw_six.demo.repository.GeoFenceRepository, FenceAlertRepository
 * - 调用方: tw_six.demo.service.CarLocationService
 * 
 * 【距离计算】
 * 使用Haversine公式计算地球表面两点间的大圆距离：
 * - 考虑了地球曲率
 * - 精度可满足一般应用需求
 * - 单位：米
 * ═══════════════════════════════════════════════════════════════════════════
 */
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
    
    /**
     * 创建新围栏
     * 
     * 功能说明:
     * - 保存新的地理围栏记录
     * - 围栏创建后默认为启用状态
     * 
     * @param fence 围栏对象
     * @return 保存后的围栏对象
     */
    public GeoFence createFence(GeoFence fence) {
        return geoFenceRepository.save(fence);
    }
    
    /**
     * 更新围栏信息
     * 
     * 功能说明:
     * - 更新指定围栏的所有可编辑属性
     * - 包括名称、坐标、半径、类型、状态等
     * 
     * @param id           围栏ID
     * @param fenceDetails 更新后的围栏信息
     * @return 更新后的围栏对象
     * @throws RuntimeException 围栏不存在时抛出异常
     */
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
    
    /**
     * 删除围栏
     * 
     * 功能说明:
     * - 物理删除指定围栏
     * - 删除前检查围栏是否存在
     * 
     * @param id 围栏ID
     * @throws RuntimeException 围栏不存在时抛出异常
     */
    public void deleteFence(Long id) {
        if (!geoFenceRepository.existsById(id)) {
            throw new RuntimeException("围栏不存在: " + id);
        }
        geoFenceRepository.deleteById(id);
    }
    
    /**
     * 获取所有围栏
     * 
     * 功能说明:
     * - 查询系统中所有围栏记录
     * - 包含启用和禁用状态
     * 
     * @return 所有围栏列表
     */
    public List<GeoFence> getAllFences() {
        return geoFenceRepository.findAll();
    }
    
    /**
     * 获取启用的围栏
     * 
     * 功能说明:
     * - 查询所有状态为"active"的围栏
     * - 用于越界检测和地图展示
     * 
     * @return 启用状态的围栏列表
     */
    public List<GeoFence> getActiveFences() {
        return geoFenceRepository.findByStatus("active");
    }
    
    /**
     * 根据ID获取围栏
     * 
     * 功能说明:
     * - 查询指定ID的围栏详情
     * 
     * @param id 围栏ID
     * @return 围栏对象
     * @throws RuntimeException 围栏不存在时抛出异常
     */
    public GeoFence getFenceById(Long id) {
        return geoFenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("围栏不存在: " + id));
    }
    
    /**
     * 更新围栏状态
     * 
     * 功能说明:
     * - 启用或禁用指定围栏
     * - 禁用后围栏不再参与越界检测
     * 
     * @param id     围栏ID
     * @param status 新状态（active/inactive）
     * @return 更新后的围栏对象
     */
    public GeoFence updateFenceStatus(Long id, String status) {
        GeoFence fence = geoFenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("围栏不存在: " + id));
        fence.setStatus(status);
        return geoFenceRepository.save(fence);
    }
    
    /**
     * 检查车辆围栏越界
     * 
     * 功能说明:
     * - 核心业务方法，检测车辆是否触发围栏告警
     * - 遍历所有启用围栏进行检测
     * - 根据检测结果生成告警记录
     * 
     * 检测逻辑:
     * 1. 计算车辆与围栏中心的距离
     * 2. 判断是否在围栏范围内
     * 3. 根据围栏告警类型决定是否生成告警
     * 
     * @param carName   车辆名称
     * @param latitude  车辆纬度
     * @param longitude 车辆经度
     */
    public void checkFenceViolation(String carName, Double latitude, Double longitude) {
        List<GeoFence> activeFences = getActiveFences();
        
        for (GeoFence fence : activeFences) {
            boolean isInFence = isPointInCircle(latitude, longitude, 
                fence.getCenterLatitude(), fence.getCenterLongitude(), fence.getRadius());
            
            if (isInFence && ("entry".equals(fence.getAlertType()) || "both".equals(fence.getAlertType()))) {
                createAlert(fence, carName, latitude, longitude, "entry");
            } else if (!isInFence && ("exit".equals(fence.getAlertType()) || "both".equals(fence.getAlertType()))) {
                createAlert(fence, carName, latitude, longitude, "exit");
            }
        }
    }
    
    /**
     * 计算两点间的球面距离
     * 
     * 功能说明:
     * - 使用Haversine公式计算地球表面两点距离
     * - 考虑地球曲率，精度较高
     * 
     * 算法原理:
     * - 基于地球是球体的假设
     * - 使用大圆距离公式
     * - 地球半径取平均值6371公里
     * 
     * @param lat1 点1纬度
     * @param lng1 点1经度
     * @param lat2 点2纬度
     * @param lng2 点2经度
     * @return 两点间距离（单位：米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }
    
    /**
     * 判断点是否在圆形围栏内
     * 
     * 功能说明:
     * - 计算点到圆心的距离
     * - 与围栏半径比较判断是否在范围内
     * 
     * @param pointLat   点纬度
     * @param pointLng   点经度
     * @param centerLat  圆心纬度
     * @param centerLng  圆心经度
     * @param radius     围栏半径（米）
     * @return true-在围栏内，false-在围栏外
     */
    private boolean isPointInCircle(double pointLat, double pointLng, 
                                  double centerLat, double centerLng, double radius) {
        double distance = calculateDistance(pointLat, pointLng, centerLat, centerLng);
        return distance <= radius;
    }
    
    /**
     * 创建围栏告警记录
     * 
     * 功能说明:
     * - 当检测到越界时创建告警记录
     * - 告警初始状态为未处理
     * 
     * @param fence     触发的围栏
     * @param carName   车辆名称
     * @param latitude  告警位置纬度
     * @param longitude 告警位置经度
     * @param alertType 告警类型（entry/exit）
     */
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
