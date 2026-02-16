package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.entity.User;
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
 * 【业务场景】
 * 1. 用户注册：新用户创建账号
 * 2. 用户管理：管理员查看和删除用户
 * 3. 权限控制：根据用户角色分配系统权限
 * 
 * 【关联文件】
 * - 实体类: tw_six.demo.entity.User
 * - 服务层: tw_six.demo.service.UserService
 * - 仓库层: tw_six.demo.repository.UserRepository
 * 
 * 【数据模型】
 * User实体包含: id, username, password, email, role, createTime等字段
 * 
 * 【安全提示】
 * - 实际生产环境应添加密码加密（如BCrypt）
 * - 应添加身份验证和授权拦截器
 * - 敏感字段（如密码）不应直接返回给前端
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
     * 功能说明:
     * - 查询系统中所有注册用户
     * - 用于管理后台的用户列表展示
     * 
     * @return 用户列表，包含用户基本信息
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * 根据ID获取单个用户
     * 
     * 功能说明:
     * - 查询指定ID的用户详细信息
     * - 用于用户详情查看或编辑前获取数据
     * 
     * @param id 用户ID（URL路径参数）
     * @return 用户对象，如果不存在则返回null
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    /**
     * 创建新用户
     * 
     * 功能说明:
     * - 接收前端提交的用户信息
     * - 创建新用户记录并保存到数据库
     * - 创建时间由系统自动设置
     * 
     * @param user 用户对象（JSON格式请求体）
     * @return 创建成功的用户对象，包含生成的ID
     */
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    
    /**
     * 删除用户
     * 
     * 功能说明:
     * - 根据ID删除指定用户
     * - 物理删除，数据将从数据库中永久移除
     * 
     * @param id 要删除的用户ID（URL路径参数）
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
