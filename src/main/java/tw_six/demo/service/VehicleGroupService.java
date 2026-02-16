package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.VehicleGroup;
import tw_six.demo.repository.VehicleGroupRepository;
import java.util.List;
import java.util.Optional;

/**
 * 车辆分组服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆分组管理的核心业务逻辑，支持分组的创建、查询、更新、删除。
 * 车辆分组用于对车辆进行分类管理，便于批量操作和权限控制。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名          │ 功能描述                       │ 返回值             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ createGroup     │ 创建新分组                     │ VehicleGroup       │
 * │ updateGroup     │ 更新分组信息                   │ VehicleGroup       │
 * │ deleteGroup     │ 删除分组                       │ void               │
 * │ getAllGroups    │ 获取所有分组                   │ List<VehicleGroup> │
 * │ getActiveGroups │ 获取启用的分组                 │ List<VehicleGroup> │
 * │ getGroupById    │ 根据ID获取分组                 │ Optional<VehicleGroup>│
 * │ getGroupByName  │ 根据名称获取分组               │ Optional<VehicleGroup>│
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务规则】
 * - 分组名称必须唯一，不允许重复
 * - 删除分组不会删除分组内的车辆
 * - 分组可以设置启用/禁用状态
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.VehicleGroupController
 * - 实体类: tw_six.demo.entity.VehicleGroup
 * - 仓库层: tw_six.demo.repository.VehicleGroupRepository
 * 
 * 【分组用途】
 * 1. 按部门分组：销售部车队、物流部车队
 * 2. 按区域分组：华北区、华东区、华南区
 * 3. 按用途分组：配送车辆、工程车辆、管理车辆
 * 4. 按状态分组：运营中、维修中、停用
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class VehicleGroupService {
    
    private final VehicleGroupRepository vehicleGroupRepository;
    
    @Autowired
    public VehicleGroupService(VehicleGroupRepository vehicleGroupRepository) {
        this.vehicleGroupRepository = vehicleGroupRepository;
    }
    
    /**
     * 创建新分组
     * 
     * 功能说明:
     * - 创建新的车辆分组
     * - 校验分组名称唯一性
     * - 名称重复时抛出异常
     * 
     * @param group 分组对象
     * @return 创建成功的分组对象
     * @throws IllegalArgumentException 分组名称已存在时抛出
     */
    public VehicleGroup createGroup(VehicleGroup group) {
        if (vehicleGroupRepository.existsByGroupName(group.getGroupName())) {
            throw new IllegalArgumentException("分组名称已存在: " + group.getGroupName());
        }
        return vehicleGroupRepository.save(group);
    }
    
    /**
     * 更新分组信息
     * 
     * 功能说明:
     * - 更新指定分组的属性
     * - 可更新名称、描述、状态等
     * 
     * @param id           分组ID
     * @param groupDetails 更新后的分组信息
     * @return 更新后的分组对象
     * @throws RuntimeException 分组不存在时抛出
     */
    public VehicleGroup updateGroup(Long id, VehicleGroup groupDetails) {
        VehicleGroup group = vehicleGroupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("分组不存在: " + id));
        
        group.setGroupName(groupDetails.getGroupName());
        group.setDescription(groupDetails.getDescription());
        group.setStatus(groupDetails.getStatus());
        
        return vehicleGroupRepository.save(group);
    }
    
    /**
     * 删除分组
     * 
     * 功能说明:
     * - 删除指定的车辆分组
     * - 删除前检查分组是否存在
     * - 注意：分组内的车辆不会被删除
     * 
     * @param id 分组ID
     * @throws RuntimeException 分组不存在时抛出
     */
    public void deleteGroup(Long id) {
        if (!vehicleGroupRepository.existsById(id)) {
            throw new RuntimeException("分组不存在: " + id);
        }
        vehicleGroupRepository.deleteById(id);
    }
    
    /**
     * 获取所有分组
     * 
     * 功能说明:
     * - 查询系统中所有车辆分组
     * - 包含启用和禁用状态的分组
     * 
     * @return 所有分组列表
     */
    public List<VehicleGroup> getAllGroups() {
        return vehicleGroupRepository.findAll();
    }
    
    /**
     * 获取启用的分组
     * 
     * 功能说明:
     * - 查询所有状态为"active"的分组
     * - 用于下拉选择和车辆分配
     * 
     * @return 启用状态的分组列表
     */
    public List<VehicleGroup> getActiveGroups() {
        return vehicleGroupRepository.findByStatus("active");
    }
    
    /**
     * 根据ID获取分组
     * 
     * 功能说明:
     * - 查询指定ID的分组详情
     * 
     * @param id 分组ID
     * @return 分组对象（Optional包装）
     */
    public Optional<VehicleGroup> getGroupById(Long id) {
        return vehicleGroupRepository.findById(id);
    }
    
    /**
     * 根据名称获取分组
     * 
     * 功能说明:
     * - 按分组名称精确查询
     * - 用于名称唯一性校验和快速查找
     * 
     * @param groupName 分组名称
     * @return 分组对象（Optional包装）
     */
    public Optional<VehicleGroup> getGroupByName(String groupName) {
        return vehicleGroupRepository.findByGroupName(groupName);
    }
}
