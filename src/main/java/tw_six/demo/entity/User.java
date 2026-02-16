package tw_six.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户实体类 - 存储系统用户信息
 * 
 * 【功能概述】
 * 该实体类用于存储系统的用户账号信息，包括用户名、邮箱、年龄等基本信息。
 * 用于用户管理和权限控制。
 * 
 * 【数据库映射】
 * - 表名：users
 * - 主键：id（自增长）
 * 
 * 【业务场景】
 * 1. 用户管理：管理系统用户账号
 * 2. 权限控制：基于用户角色控制访问权限
 * 3. 通知发送：通过邮箱发送系统通知
 * 
 * 【数据验证】
 * 使用Jakarta Validation注解进行数据验证：
 * - @NotBlank：字段不能为空
 * - @NotNull：字段不能为null
 * 
 * 【使用示例】
 * User user = new User();
 * user.setUsername("admin");
 * user.setEmail("admin@example.com");
 * user.setAge(30);
 */
@Entity
@Table(name = "users")
public class User {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名 - 用户的登录账号
     * 必填，全局唯一
     * 用于用户登录和身份识别
     */
    @NotBlank(message = "用户名不能为空")
    @Column(nullable = false, unique = true)
    private String username;
    
    /**
     * 邮箱 - 用户的电子邮箱地址
     * 必填，全局唯一
     * 用于发送通知和找回密码
     */
    @NotBlank(message = "邮箱不能为空")
    @Column(nullable = false, unique = true)
    private String email;
    
    /**
     * 年龄 - 用户的年龄
     * 必填
     * 用于统计分析和个性化服务
     */
    @NotNull(message = "年龄不能为空")
    @Column(nullable = false)
    private Integer age;
    
    /**
     * 地址 - 用户的居住地址
     * 可选字段
     * 最大长度1000字符
     */
    @Column(length = 1000)
    private String address;
    
    /**
     * 默认构造函数
     */
    public User() {}
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
