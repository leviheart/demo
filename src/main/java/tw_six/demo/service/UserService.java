package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw_six.demo.entity.User;
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
 * │ getAllUsers     │ 获取所有用户列表             │ List<User>           │
 * │ getUserById     │ 根据ID获取用户               │ User                 │
 * │ saveUser        │ 保存/更新用户                │ User                 │
 * │ deleteUser      │ 删除用户                     │ void                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【设计模式】
 * - 服务层模式：将业务逻辑从控制器中分离
 * - 依赖注入：通过构造函数注入Repository
 * - 单一职责：只负责用户相关的业务逻辑
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.UserController
 * - 实体类: tw_six.demo.entity.User
 * - 仓库层: tw_six.demo.repository.UserRepository
 * 
 * 【扩展建议】
 * - 添加用户名唯一性校验
 * - 添加密码加密功能
 * - 添加用户权限验证
 * - 添加用户状态管理（启用/禁用）
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
     * 功能说明:
     * - 查询系统中所有注册用户
     * - 直接委托给Repository的findAll方法
     * 
     * @return 用户列表，如果没有用户则返回空列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根据ID获取用户
     * 
     * 功能说明:
     * - 根据用户ID查询用户详情
     * - 如果用户不存在返回null
     * 
     * @param id 用户ID
     * @return 用户对象，不存在则返回null
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 保存用户
     * 
     * 功能说明:
     * - 保存新用户或更新已有用户
     * - 如果user.id为null则新增，否则更新
     * - 创建时间由JPA自动管理
     * 
     * @param user 用户对象
     * @return 保存后的用户对象（包含生成的ID）
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     * 
     * 功能说明:
     * - 根据ID删除用户
     * - 物理删除，数据将永久移除
     * - 如果用户不存在会抛出异常
     * 
     * @param id 要删除的用户ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
