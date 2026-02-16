package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.GeoFence;
import tw_six.demo.service.GeoFenceService;
import java.util.List;

/**
 * 地理围栏控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供地理围栏的管理接口，支持围栏的创建、查询、更新、删除等操作。
 * 地理围栏是车辆监控系统的重要组成部分，用于限定车辆的活动范围。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法   │ 路径                         │ 功能描述                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ POST   │ /api/geo-fences              │ 创建新围栏                     │
 * │ PUT    │ /api/geo-fences/{id}         │ 更新围栏信息                   │
 * │ DELETE │ /api/geo-fences/{id}         │ 删除围栏                       │
 * │ GET    │ /api/geo-fences              │ 获取所有围栏                   │
 * │ GET    │ /api/geo-fences/active       │ 获取启用的围栏                 │
 * │ GET    │ /api/geo-fences/{id}         │ 根据ID获取围栏                 │
 * │ PUT    │ /api/geo-fences/{id}/status  │ 更新围栏状态                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 围栏配置：管理员创建和配置监控区域
 * 2. 区域管理：设置允许/禁止进入的区域
 * 3. 实时监控：后台服务检测车辆是否越界
 * 4. 告警触发：车辆越界时自动生成告警记录
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.GeoFence
 * - 服务层: tw_six.demo.service.GeoFenceService
 * - 仓库层: tw_six.demo.repository.GeoFenceRepository
 * - 前端调用: GeoFences.vue组件
 * 
 * 【围栏类型说明】
 * - CIRCLE: 圆形围栏，由中心点和半径定义
 * - POLYGON: 多边形围栏，由多个顶点坐标定义
 * 
 * 【围栏状态】
 * - ACTIVE: 启用状态，参与越界检测
 * - INACTIVE: 禁用状态，不参与检测
 * 
 * 【坐标格式】
 * 围栏坐标使用JSON数组格式存储，如:
 * 圆形: {"center":[116.4074,39.9042],"radius":1000}
 * 多边形: [[116.40,39.90],[116.41,39.91],[116.42,39.90]]
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/geo-fences")
public class GeoFenceController {
    
    private final GeoFenceService geoFenceService;
    
    @Autowired
    public GeoFenceController(GeoFenceService geoFenceService) {
        this.geoFenceService = geoFenceService;
    }
    
    /**
     * 创建新围栏
     * 
     * 功能说明:
     * - 接收前端提交的围栏配置数据
     * - 创建新的地理围栏记录
     * - 围栏创建后默认为启用状态
     * 
     * @param fence 围栏对象（JSON格式请求体）
     * @return 创建成功的围栏对象
     */
    @PostMapping
    public ApiResponse<GeoFence> createFence(@RequestBody GeoFence fence) {
        GeoFence createdFence = geoFenceService.createFence(fence);
        return ApiResponse.success("围栏创建成功", createdFence);
    }
    
    /**
     * 更新围栏信息
     * 
     * 功能说明:
     * - 更新指定围栏的配置信息
     * - 可更新名称、坐标、类型等属性
     * 
     * @param id    围栏ID
     * @param fence 更新后的围栏对象
     * @return 更新后的围栏对象
     */
    @PutMapping("/{id}")
    public ApiResponse<GeoFence> updateFence(@PathVariable Long id, @RequestBody GeoFence fence) {
        try {
            GeoFence updatedFence = geoFenceService.updateFence(id, fence);
            return ApiResponse.success("围栏更新成功", updatedFence);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 删除围栏
     * 
     * 功能说明:
     * - 删除指定的地理围栏
     * - 删除后相关联的告警规则将失效
     * - 建议先禁用再删除，避免误操作
     * 
     * @param id 要删除的围栏ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFence(@PathVariable Long id) {
        try {
            geoFenceService.deleteFence(id);
            return ApiResponse.success("围栏删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 获取所有围栏
     * 
     * 功能说明:
     * - 查询系统中所有的地理围栏
     * - 包含启用和禁用状态的围栏
     * - 用于围栏管理列表展示
     * 
     * @return 所有围栏列表
     */
    @GetMapping
    public ApiResponse<List<GeoFence>> getAllFences() {
        List<GeoFence> fences = geoFenceService.getAllFences();
        return ApiResponse.success(fences);
    }
    
    /**
     * 获取启用的围栏
     * 
     * 功能说明:
     * - 查询所有状态为ACTIVE的围栏
     * - 用于地图展示和越界检测
     * - 只返回当前生效的围栏
     * 
     * @return 启用状态的围栏列表
     */
    @GetMapping("/active")
    public ApiResponse<List<GeoFence>> getActiveFences() {
        List<GeoFence> fences = geoFenceService.getActiveFences();
        return ApiResponse.success(fences);
    }
    
    /**
     * 根据ID获取围栏详情
     * 
     * 功能说明:
     * - 查询指定围栏的详细信息
     * - 用于围栏编辑前的数据获取
     * 
     * @param id 围栏ID
     * @return 围栏详细信息
     */
    @GetMapping("/{id}")
    public ApiResponse<GeoFence> getFenceById(@PathVariable Long id) {
        try {
            GeoFence fence = geoFenceService.getFenceById(id);
            return ApiResponse.success(fence);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 更新围栏状态
     * 
     * 功能说明:
     * - 启用或禁用指定围栏
     * - 禁用后围栏不再参与越界检测
     * - 用于临时关闭围栏监控
     * 
     * @param id   围栏ID
     * @param body 请求体，包含status字段（ACTIVE/INACTIVE）
     * @return 更新后的围栏对象
     */
    @PutMapping("/{id}/status")
    public ApiResponse<GeoFence> updateFenceStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            String status = body.get("status");
            GeoFence updatedFence = geoFenceService.updateFenceStatus(id, status);
            return ApiResponse.success("围栏状态更新成功", updatedFence);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}
