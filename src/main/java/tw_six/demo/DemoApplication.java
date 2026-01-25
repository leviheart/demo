package tw_six.demo; // 定义包名，组织代码结构

import org.springframework.boot.SpringApplication; // 导入Spring Boot核心类，用于启动应用程序
import org.springframework.boot.autoconfigure.SpringBootApplication; // 导入Spring Boot自动配置注解

@SpringBootApplication // Spring Boot应用主注解，启用自动配置、组件扫描等功能
public class DemoApplication { // 定义主应用程序类

	public static void main(String[] args) { // Java程序入口方法
		SpringApplication.run(DemoApplication.class, args); // 启动Spring Boot应用程序
	}

}