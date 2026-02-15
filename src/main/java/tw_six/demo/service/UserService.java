package tw_six.demo.service; // 定义业务逻辑层服务类所在的包

import lombok.RequiredArgsConstructor; // Lombok注解，生成必要的构造函数
import org.springframework.stereotype.Service; // Spring注解，标记为业务服务组件
import tw_six.demo.entity.User; // 引入用户实体类
import tw_six.demo.repository.UserRepository; // 引入用户数据访问接口
import java.util.List; // Java集合框架，用于返回列表数据

/**
 * 用户业务服务类 - 业务逻辑层组件
 * 
 * 文件关联说明：
 * 1. 与UserRepository关联：通过构造函数注入获得数据访问能力
 * 2. 与User实体类关联：处理User对象的业务逻辑
 * 3. 与UserController关联：通过依赖注入为控制层提供业务服务
 * 4. 与Spring容器关联：通过@Service注解注册为Spring Bean
 * 
 * 作用说明：
 * - 封装用户相关的业务逻辑处理
 * - 作为控制层和数据访问层之间的桥梁
 * - 提供标准化的业务接口，隐藏底层数据操作细节
 * - 确保业务规则的一致性和数据完整性
 */
@Service // 标记为Spring业务服务组件，纳入Spring容器管理
@RequiredArgsConstructor // Lombok注解，为final字段生成构造函数，支持构造函数注入
public class UserService { // 用户业务服务类定义
    
    // 使用final修饰确保依赖不可变性，通过构造函数注入
    private final UserRepository userRepository; // 用户数据访问接口依赖
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户列表，包含所有存储的用户信息
     */
    public List<User> getAllUsers() { // 业务方法：获取全部用户
        return userRepository.findAll(); // 调用数据访问层方法获取所有用户
    }
    
    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    public User getUserById(Long id) { // 业务方法：根据ID查找用户
        return userRepository.findById(id).orElse(null); // 调用数据访问层方法查找用户
    }
    
    /**
     * 保存用户信息
     * 
     * @param user 用户对象
     * @return 保存后的用户对象（包含生成的ID）
     */
    public User saveUser(User user) { // 业务方法：保存用户
        return userRepository.save(user); // 调用数据访问层方法保存用户
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    public void deleteUser(Long id) { // 业务方法：删除用户
        userRepository.deleteById(id); // 调用数据访问层方法删除用户
    }
}