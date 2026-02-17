package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw_six.demo.entity.UserEntity;
import tw_six.demo.repository.UserRepository;
import java.util.List;

/**
 * 用户服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供用户管理的核心业务逻辑，封装用户的增删改查操作。
 * 作为Controller层和Repository层之间的桥梁，处理业务规则和数据转换。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名          │ 功能描述                     │ 返回值               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ getAllUsers     │ 获取所有用户列表             │ List<UserEntity>     │
 * │ getUserById     │ 根据ID获取用户               │ UserEntity           │
 * │ getUserByUsername│ 根据用户名获取用户           │ UserEntity           │
 * │ saveUser        │ 保存/更新用户                │ UserEntity           │
 * │ deleteUser      │ 删除用户                     │ void                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【设计模式】
 * - 服务层模式：将业务逻辑从控制器中分离
 * - 依赖注入：通过构造函数注入Repository
 * - 单一职责：只负责用户相关的业务逻辑
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.AuthController
 * - 实体类: tw_six.demo.entity.UserEntity
 * - 仓库层: tw_six.demo.repository.UserRepository
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户列表，如果没有用户则返回空列表
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户对象，不存在则返回null
     */
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 根据用户名获取用户
     * 
     * @param username 用户名
     * @return 用户对象，不存在则返回null
     */
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    /**
     * 保存用户
     * 
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     * 
     * @param id 要删除的用户ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
