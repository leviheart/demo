package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.UserEntity;
import tw_six.demo.service.UserService;
import java.util.List;

/**
 * 用户控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供系统用户的CRUD操作接口，用于用户管理和权限控制。
 * 支持用户查询、创建、删除等基本操作。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法   │ 路径              │ 功能描述                             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET    │ /api/users        │ 获取所有用户列表                     │
 * │ GET    │ /api/users/{id}   │ 根据ID获取单个用户信息               │
 * │ POST   │ /api/users        │ 创建新用户                           │
 * │ DELETE │ /api/users/{id}   │ 删除指定用户                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.UserEntity
 * - 服务层: tw_six.demo.service.UserService
 * - 仓库层: tw_six.demo.repository.UserRepository
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    @GetMapping
    public ApiResponse<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }
    
    /**
     * 根据ID获取单个用户
     * 
     * @param id 用户ID
     * @return 用户对象
     */
    @GetMapping("/{id}")
    public ApiResponse<UserEntity> getUserById(@PathVariable Long id) {
        UserEntity user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(user);
    }
    
    /**
     * 创建新用户
     * 
     * @param user 用户对象
     * @return 创建成功的用户对象
     */
    @PostMapping
    public ApiResponse<UserEntity> createUser(@RequestBody UserEntity user) {
        UserEntity savedUser = userService.saveUser(user);
        return ApiResponse.success("用户创建成功", savedUser);
    }
    
    /**
     * 删除用户
     * 
     * @param id 要删除的用户ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("用户删除成功", null);
    }
}
