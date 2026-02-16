package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.VehicleGroup;
import tw_six.demo.service.VehicleGroupService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class VehicleGroupController {
    
    private final VehicleGroupService vehicleGroupService;
    
    @Autowired
    public VehicleGroupController(VehicleGroupService vehicleGroupService) {
        this.vehicleGroupService = vehicleGroupService;
    }
    
    @PostMapping
    public ApiResponse<VehicleGroup> createGroup(@RequestBody VehicleGroup group) {
        try {
            VehicleGroup createdGroup = vehicleGroupService.createGroup(group);
            return ApiResponse.success("分组创建成功", createdGroup);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<VehicleGroup> updateGroup(@PathVariable Long id, @RequestBody VehicleGroup group) {
        try {
            VehicleGroup updatedGroup = vehicleGroupService.updateGroup(id, group);
            return ApiResponse.success("分组更新成功", updatedGroup);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        try {
            vehicleGroupService.deleteGroup(id);
            return ApiResponse.success("分组删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @GetMapping
    public ApiResponse<List<VehicleGroup>> getAllGroups() {
        List<VehicleGroup> groups = vehicleGroupService.getAllGroups();
        return ApiResponse.success(groups);
    }
    
    @GetMapping("/active")
    public ApiResponse<List<VehicleGroup>> getActiveGroups() {
        List<VehicleGroup> groups = vehicleGroupService.getActiveGroups();
        return ApiResponse.success(groups);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<VehicleGroup> getGroupById(@PathVariable Long id) {
        Optional<VehicleGroup> group = vehicleGroupService.getGroupById(id);
        if (group.isPresent()) {
            return ApiResponse.success(group.get());
        } else {
            return ApiResponse.error(404, "分组不存在");
        }
    }
    
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