package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.entity.CarLocation;
import tw_six.demo.service.CarLocationService;
import java.util.List;

/**
 * 车辆位置控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆实时位置数据的CRUD操作接口，是GPS定位系统的核心API之一。
 * 支持获取所有车辆最新位置、按车辆名称查询、新增位置记录等功能。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                      │ 功能描述                         │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET  │ /api/car-locations/latest │ 获取所有车辆最新位置列表          │
 * │ GET  │ /api/car-locations/car/{carName} │ 按车辆名称查询位置历史    │
 * │ POST │ /api/car-locations        │ 保存新的车辆位置记录             │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 实时监控：前端地图组件定时轮询获取车辆最新位置
 * 2. 轨迹回放：查询指定车辆的历史位置数据用于轨迹绘制
 * 3. 位置上报：GPS设备或移动端定期上报当前位置坐标
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.CarLocation
 * - 服务层: tw_six.demo.service.CarLocationService
 * - 仓库层: tw_six.demo.repository.CarLocationRepository
 * - 前端调用: MapView.vue组件
 * 
 * 【请求示例】
 * GET /api/car-locations/latest
 * 响应: [{"id":1,"carName":"京A12345","latitude":39.9042,"longitude":116.4074,...}]
 * 
 * POST /api/car-locations
 * 请求体: {"carName":"京A12345","latitude":39.9042,"longitude":116.4074}
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/car-locations")
public class CarLocationController {
    
    private final CarLocationService carLocationService;
    
    @Autowired
    public CarLocationController(CarLocationService carLocationService) {
        this.carLocationService = carLocationService;
    }
    
    /**
     * 获取所有车辆最新位置
     * 
     * 功能说明:
     * - 查询数据库中所有车辆的位置记录
     * - 用于地图上显示所有车辆的实时位置标记
     * 
     * @return 车辆位置列表，包含经纬度、速度、方向等信息
     */
    @GetMapping("/latest")
    public List<CarLocation> getLatestLocations() {
        return carLocationService.getAllLocations();
    }
    
    /**
     * 按车辆名称查询位置记录
     * 
     * 功能说明:
     * - 根据车牌号或车辆标识查询该车辆的所有位置记录
     * - 用于轨迹回放功能，获取历史轨迹点
     * 
     * @param carName 车辆名称/车牌号（URL路径参数）
     * @return 该车辆的位置记录列表，按时间排序
     */
    @GetMapping("/car/{carName}")
    public List<CarLocation> getLocationsByCarName(@PathVariable String carName) {
        return carLocationService.getLocationsByCarName(carName);
    }
    
    /**
     * 保存车辆位置记录
     * 
     * 功能说明:
     * - 接收GPS设备或移动端上报的位置数据
     * - 将位置信息持久化到数据库
     * - 通常由车载设备定时调用（如每10秒一次）
     * 
     * @param location 位置信息对象（JSON格式请求体）
     * @return 保存后的位置对象，包含生成的ID
     */
    @PostMapping
    public CarLocation saveLocation(@RequestBody CarLocation location) {
        return carLocationService.saveLocation(location);
    }
}
