package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.CarLocation;
import tw_six.demo.repository.CarLocationRepository;
import java.util.List;
import java.util.Optional;

/**
 * 车辆位置服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆位置数据的核心业务逻辑，是GPS定位系统的核心服务。
 * 负责位置数据的存储、查询，并触发围栏越界检测。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名              │ 功能描述                       │ 返回值           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ saveLocation        │ 保存位置并触发围栏检测         │ CarLocation      │
 * │ getAllLocations     │ 获取所有车辆位置               │ List<CarLocation>│
 * │ getActiveLocations  │ 获取活跃车辆位置               │ List<CarLocation>│
 * │ getLocationById     │ 根据ID获取位置                 │ Optional<CarLocation>│
 * │ getLocationsByCarName│ 按车辆名称查询位置            │ List<CarLocation>│
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【核心业务流程】
 * 1. GPS设备上报位置数据
 * 2. 保存位置记录到数据库
 * 3. 触发围栏越界检测
 * 4. 如有越界则自动生成告警
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.CarLocationController, MapController
 * - 实体类: tw_six.demo.entity.CarLocation
 * - 仓库层: tw_six.demo.repository.CarLocationRepository
 * - 依赖服务: tw_six.demo.service.GeoFenceService
 * 
 * 【事务说明】
 * - 使用@Transactional注解确保数据一致性
 * - 位置保存和围栏检测在同一事务中执行
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class CarLocationService {
    
    private final CarLocationRepository carLocationRepository;
    private final GeoFenceService geoFenceService;
    
    @Autowired
    public CarLocationService(CarLocationRepository carLocationRepository, GeoFenceService geoFenceService) {
        this.carLocationRepository = carLocationRepository;
        this.geoFenceService = geoFenceService;
    }
    
    /**
     * 保存车辆位置
     * 
     * 功能说明:
     * - 保存车辆位置记录到数据库
     * - 自动触发围栏越界检测
     * - 是GPS数据上报的核心入口
     * 
     * 业务流程:
     * 1. 保存位置数据
     * 2. 调用围栏服务检测越界
     * 3. 返回保存的位置对象
     * 
     * @param location 位置信息对象
     * @return 保存后的位置对象
     */
    public CarLocation saveLocation(CarLocation location) {
        CarLocation savedLocation = carLocationRepository.save(location);
        geoFenceService.checkFenceViolation(location.getCarName(), location.getLatitude(), location.getLongitude());
        return savedLocation;
    }
    
    /**
     * 获取所有车辆位置
     * 
     * 功能说明:
     * - 查询数据库中所有车辆的位置记录
     * - 用于地图初始化加载所有车辆标记
     * 
     * @return 所有车辆位置列表
     */
    public List<CarLocation> getAllLocations() {
        return carLocationRepository.findAll();
    }
    
    /**
     * 获取活跃车辆位置
     * 
     * 功能说明:
     * - 查询状态为"active"的车辆
     * - 用于区分在线/离线车辆
     * - 活跃判断由数据更新或状态字段决定
     * 
     * @return 活跃车辆位置列表
     */
    public List<CarLocation> getActiveLocations() {
        return carLocationRepository.findByStatus("active");
    }
    
    /**
     * 根据ID获取位置记录
     * 
     * 功能说明:
     * - 查询指定ID的位置记录详情
     * 
     * @param id 位置记录ID
     * @return 位置对象（Optional包装）
     */
    public Optional<CarLocation> getLocationById(Long id) {
        return carLocationRepository.findById(id);
    }
    
    /**
     * 按车辆名称查询位置记录
     * 
     * 功能说明:
     * - 查询指定车辆的所有位置历史
     * - 用于轨迹回放和历史查询
     * 
     * @param carName 车辆名称/车牌号
     * @return 该车辆的位置记录列表
     */
    public List<CarLocation> getLocationsByCarName(String carName) {
        return carLocationRepository.findByCarName(carName);
    }
}
