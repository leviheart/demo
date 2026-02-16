package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.entity.CarLocation;
import tw_six.demo.entity.Route;
import tw_six.demo.service.CarLocationService;
import tw_six.demo.service.RouteService;
import java.util.List;

/**
 * 地图控制器 - 地图相关功能的RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供地图相关的综合功能接口，包括车辆位置查询、活跃车辆获取、路线规划等。
 * 是前端地图组件的主要数据来源。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                    │ 功能描述                           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET  │ /api/map/locations      │ 获取所有车辆位置                   │
 * │ POST │ /api/map/location       │ 更新/保存车辆位置                 │
 * │ GET  │ /api/map/active-cars    │ 获取活跃车辆列表                   │
 * │ GET  │ /api/map/route          │ 规划行车路线                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 实时监控面板：获取所有车辆位置并在地图上展示
 * 2. 位置上报：GPS设备定期更新车辆位置
 * 3. 活跃状态判断：识别最近有位置更新的车辆
 * 4. 路线规划：计算两点之间的最优行车路线
 * 
 * 【关联文件】
 * - 实体类: CarLocation, Route
 * - 服务层: CarLocationService, RouteService
 * - 前端调用: MapView.vue组件
 * 
 * 【请求示例】
 * GET /api/map/route?start=116.4074,39.9042&end=121.4737,31.2304
 * 响应: {"id":1,"startPoint":"...","endPoint":"...","distance":1200,"duration":7200}
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/map")
public class MapController {
    
    private final CarLocationService carLocationService;
    private final RouteService routeService;
    
    @Autowired
    public MapController(CarLocationService carLocationService, RouteService routeService) {
        this.carLocationService = carLocationService;
        this.routeService = routeService;
    }
    
    /**
     * 获取所有车辆位置
     * 
     * 功能说明:
     * - 查询数据库中所有车辆的位置信息
     * - 用于地图初始化时加载所有车辆标记
     * 
     * @return 所有车辆位置列表
     */
    @GetMapping("/locations")
    public List<CarLocation> getAllLocations() {
        return carLocationService.getAllLocations();
    }
    
    /**
     * 更新/保存车辆位置
     * 
     * 功能说明:
     * - 接收车辆上报的新位置数据
     * - 如果是新车则创建记录，已有车辆则更新位置
     * 
     * @param location 位置信息对象
     * @return 保存后的位置对象
     */
    @PostMapping("/location")
    public CarLocation updateLocation(@RequestBody CarLocation location) {
        return carLocationService.saveLocation(location);
    }
    
    /**
     * 获取活跃车辆列表
     * 
     * 功能说明:
     * - 查询最近有位置更新的车辆
     * - 活跃判断标准由服务层定义（如最近5分钟内有更新）
     * - 用于区分在线/离线车辆状态
     * 
     * @return 活跃车辆位置列表
     */
    @GetMapping("/active-cars")
    public List<CarLocation> getActiveCars() {
        return carLocationService.getActiveLocations();
    }
    
    /**
     * 规划行车路线
     * 
     * 功能说明:
     * - 根据起点和终点坐标规划最优路线
     * - 返回路线距离、预计时间等信息
     * - 可能调用第三方地图API（如高德、百度地图）
     * 
     * @param start 起点坐标，格式："经度,纬度"
     * @param end   终点坐标，格式："经度,纬度"
     * @return 路线规划结果，包含距离、时长、途经点等
     */
    @GetMapping("/route")
    public Route planRoute(@RequestParam String start, @RequestParam String end) {
        return routeService.planRoute(start, end);
    }
}
