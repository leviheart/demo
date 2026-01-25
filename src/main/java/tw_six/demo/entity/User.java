package tw_six.demo.entity; // 定义实体类所在的包

import jakarta.persistence.Entity; // 导入JPA实体注解
import jakarta.persistence.GeneratedValue; // 导入ID自动生成策略注解
import jakarta.persistence.GenerationType; // 导入ID生成类型枚举
import jakarta.persistence.Id; // 导入主键注解
import jakarta.validation.constraints.NotBlank; // 导入非空验证注解
import jakarta.validation.constraints.Email; // 导入邮箱格式验证注解
import lombok.Data; // 导入Lombok的@Data注解，自动生成getter/setter等方法

@Entity // 标记此类为JPA实体，对应数据库中的表
@Data // Lombok注解，自动生成getter、setter、toString、equals、hashCode等方法
public class User { // 用户实体类
    @Id // 标记此字段为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键自增策略
    private Long id; // 用户ID，Long类型
    
    @NotBlank(message = "姓名不能为空") // 验证姓名不为空，如果为空则显示指定消息
    private String name; // 用户姓名
    
    @Email(message = "邮箱格式不正确") // 验证邮箱格式，如果不正确则显示指定消息
    private String email; // 用户邮箱
    
    public User() {} // 无参构造函数，JPA需要
    
    public User(String name, String email) { // 有参构造函数
        this.name = name; // 设置姓名
        this.email = email; // 设置邮箱
    }
}