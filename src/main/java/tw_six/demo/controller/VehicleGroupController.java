package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.VehicleGroup;
import tw_six.demo.service.VehicleGroupService;
import java.util.List;
import java.util.Optional;

/**
 * 车辆分组控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆分组的管理接口，支持分组的创建、查询、更新、删除等操作。
 * 车辆分组用于对车辆进行分类管理，便于批量操作和权限控制。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法   │ 路径                           │ 功能描述                     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ POST   │ /api/vehicle-groups            │ 创建新分组                   │
 * │ PUT    │ /api/vehicle-groups/{id}       │ 更新分组信息                 │
 * │ DELETE │ /api/vehicle-groups/{id}       │ 删除分组                     │
 * │ GET    │ /api/vehicle-groups            │ 获取所有分组                 │
 * │ GET    │ /api/vehicle-groups/active     │ 获取启用的分组               │
 * │ GET    │ /api/vehicle-groups/{id}       │ 根据ID获取分组               │
 * │ GET    │ /api/vehicle-groups/name/{name}│ 根据名称获取分组             │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务场景】
 * 1. 车辆分类：按部门、区域、用途等维度分组
 * 2. 批量管理：对分组内的车辆进行批量操作
 * 3. 权限控制：按分组分配用户查看权限
 * 4. 统计报表：按分组生成运营报表
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.VehicleGroup
 * - 服务层: tw_six.demo.service.VehicleGroupService
 * - 仓库层: tw_six.demo.repository.VehicleGroupRepository
 * - 前端调用: VehicleGroups.vue组件
 * 
 * 【分组数据结构】
 * - groupName: 分组名称，如"华北区车队"
 * - description: 分组描述
 * - vehicles: 分组包含的车辆列表（JSON数组）
 * - status: 分组状态（ACTIVE/INACTIVE）
 * 
 * 【使用示例】
 * 创建分组请求体:
 * {
 *   "groupName": "华北区车队",
 *   "description": "负责华北区域的配送车辆",
 *   "vehicles": ["京A12345", "京B67890"],
 *   "status": "ACTIVE"
 * }
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/vehicle-groups")
public class VehicleGroupController {
    
    private final VehicleGroupService vehicleGroupService;
    
    @Autowired
    public VehicleGroupController(VehicleGroupService vehicleGroupService) {
        this.vehicleGroupService = vehicleGroupService;
    }
    
    /**
     * 创建新分组
     * 
     * 功能说明:
     * - 创建新的车辆分组
     * - 分组名称不能重复
     * - 创建后默认为启用状态
     * 
     * @param group 分组对象（JSON格式请求体）
     * @return 创建成功的分组对象
     */
    @PostMapping
    public ApiResponse<VehicleGroup> createGroup(@RequestBody VehicleGroup group) {
        try {
            VehicleGroup createdGroup = vehicleGroupService.createGroup(group);
            return ApiResponse.success("分组创建成功", createdGroup);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    /**
     * 更新分组信息
     * 
     * 功能说明:
     * - 更新指定分组的配置信息
     * - 可更新名称、描述、车辆列表等
     * 
     * @param id    分组ID
     * @param group 更新后的分组对象
     * @return 更新后的分组对象
     */
    @PutMapping("/{id}")
    public ApiResponse<VehicleGroup> updateGroup(@PathVariable Long id, @RequestBody VehicleGroup group) {
        try {
            VehicleGroup updatedGroup = vehicleGroupService.updateGroup(id, group);
            return ApiResponse.success("分组更新成功", updatedGroup);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 删除分组
     * 
     * 功能说明:
     * - 删除指定的车辆分组
     * - 删除分组不会删除分组内的车辆
     * - 建议先移出车辆再删除分组
     * 
     * @param id 要删除的分组ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        try {
            vehicleGroupService.deleteGroup(id);
            return ApiResponse.success("分组删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 获取所有分组
     * 
     * 功能说明:
     * - 查询系统中所有的车辆分组
     * - 包含启用和禁用状态的分组
     * - 用于分组管理列表展示
     * 
     * @return 所有分组列表
     */
    @GetMapping
    public ApiResponse<List<VehicleGroup>> getAllGroups() {
        List<VehicleGroup> groups = vehicleGroupService.getAllGroups();
        return ApiResponse.success(groups);
    }
    
    /**
     * 获取启用的分组
     * 
     * 功能说明:
     * - 查询所有状态为ACTIVE的分组
     * - 用于下拉选择和车辆分配
     * 
     * @return 启用状态的分组列表
     */
    @GetMapping("/active")
    public ApiResponse<List<VehicleGroup>> getActiveGroups() {
        List<VehicleGroup> groups = vehicleGroupService.getActiveGroups();
        return ApiResponse.success(groups);
    }
    
    /**
     * 根据ID获取分组详情
     * 
     * 功能说明:
     * - 查询指定分组的详细信息
     * - 用于分组编辑前的数据获取
     * 
     * @param id 分组ID
     * @return 分组详细信息
     */
    @GetMapping("/{id}")
    public ApiResponse<VehicleGroup> getGroupById(@PathVariable Long id) {
        Optional<VehicleGroup> group = vehicleGroupService.getGroupById(id);
        if (group.isPresent()) {
            return ApiResponse.success(group.get());
        } else {
            return ApiResponse.error(404, "分组不存在");
        }
    }
    
    /**
     * 根据名称获取分组
     * 
     * 功能说明:
     * - 按分组名称精确查询
     * - 用于名称唯一性校验和快速查找
     * 
     * @param groupName 分组名称
     * @return 匹配的分组对象
     */
    @GetMapping("/name/{groupName}")
    public ApiResponse<VehicleGroup> getGroupByName(@PathVariable String groupName) {
        Optional<VehicleGroup> group = vehicleGroupService.getGroupByName(groupName);
        if (group.isPresent()) {
            return ApiResponse.success(group.get());
        } else {
            return ApiResponse.error(404, "分组不存在");
        }
    }
}
