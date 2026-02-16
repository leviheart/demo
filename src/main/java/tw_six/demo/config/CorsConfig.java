package tw_six.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置类
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 解决前后端分离架构中的跨域请求问题。
 * 允许前端应用（如Vue开发服务器）访问后端API。
 * 
 * 【跨域问题说明】
 * 当前端运行在 localhost:5173，后端运行在 localhost:8081 时，
 * 浏览器出于安全考虑会阻止跨域请求，需要后端配置CORS策略。
 * 
 * 【配置内容】
 * - 允许的来源：所有来源（开发环境）
 * - 允许的方法：GET, POST, PUT, DELETE, OPTIONS
 * - 允许的头信息：所有头信息
 * - 允许携带凭证：cookies等
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源访问（开发环境使用，生产环境应限制具体域名）
        config.addAllowedOriginPattern("*");
        
        // 允许携带凭证（cookies、authorization headers等）
        config.setAllowCredentials(true);
        
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 配置URL映射
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
