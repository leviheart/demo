package tw_six.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "邮箱不能为空")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotNull(message = "年龄不能为空")
    @Column(nullable = false)
    private Integer age;
    
    @Column(length = 1000)
    private String address;
}