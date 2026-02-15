package tw_six.demo; // 定义主应用类所在的包

import org.springframework.boot.SpringApplication; // Spring Boot启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // Spring Boot核心注解

/**
 * Spring Boot主应用类 - 整个应用程序的入口点和配置中心
 * 
 * 文件关联说明：
 * 1. 与Spring Boot框架关联：通过@SpringBootApplication启用自动配置
 * 2. 与组件扫描关联：自动扫描同包及子包下的所有Spring组件
 * 3. 与配置文件关联：自动加载src/main/resources/application.properties
 * 4. 与数据库关联：通过自动配置初始化数据源和JPA
 * 5. 与Web服务器关联：内嵌Tomcat服务器自动启动
 * 
 * 作用说明：
 * - 作为Spring Boot应用的启动入口
 * - 启用组件自动扫描和自动配置功能
 * - 初始化整个应用程序上下文环境
 * - 协调各个模块组件的集成和运行
 */
@SpringBootApplication // 核心注解：启用Spring Boot自动配置、组件扫描和自动装配
public class DemoApplication { // 主应用类定义

	/**
	 * 应用程序主入口方法
	 * 
	 * @param args 命令行参数
	 */
	public static void main(String[] args) { // 主方法：程序启动入口
		SpringApplication.run(DemoApplication.class, args); // 启动Spring Boot应用
	}

}