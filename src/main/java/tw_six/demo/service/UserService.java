package tw_six.demo.service; // 定义服务层所在的包

import org.springframework.stereotype.Service; // 导入Service注解
import org.springframework.transaction.annotation.Transactional; // 导入事务管理注解
import jakarta.validation.Valid; // 导入验证注解
import tw_six.demo.entity.User; // 导入User实体类
import tw_six.demo.repository.UserRepository; // 导入UserRepository数据访问接口

import java.util.List; // 导入List集合类

@Service // 标记此类为业务服务组件
@Transactional // 启用事务管理，确保数据一致性
public class UserService { // 用户业务服务类
    
    private final UserRepository userRepository; // 声明UserRepository实例变量，用于数据访问
    
    public UserService(UserRepository userRepository) { // 构造函数，通过依赖注入获取UserRepository实例
        this.userRepository = userRepository; // 将传入的userRepository赋值给成员变量
    }
    
    public List<User> getAllUsers() { // 获取所有用户的方法
        return userRepository.findAll(); // 调用Repository的findAll方法查询所有用户
    }
    
    public User saveUser(User user) { // 保存用户的方法
        return userRepository.save(user); // 调用Repository的save方法保存或更新用户
    }
}