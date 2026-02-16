package tw_six.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器 - 服务端渲染页面路由控制层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供传统服务端渲染的页面路由，配合Thymeleaf模板引擎使用。
 * 负责将URL路径映射到对应的HTML模板文件。
 * 
 * 【页面路由列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ URL路径     │ 模板文件        │ 页面描述                             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ /           │ index.html      │ 应用主页                             │
 * │ /groups     │ groups.html     │ 车辆分组管理页面                     │
 * │ /alarms     │ alarms.html     │ 告警管理页面                         │
 * │ /reports    │ reports.html    │ 报表统计页面                         │
 * │ /geofences  │ geofences.html  │ 地理围栏管理页面                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【架构说明】
 * - @Controller: 返回视图名称，由模板引擎渲染HTML
 * - 与@RestController的区别: 后者返回JSON数据
 * - 当前项目主要使用Vue3前端，此控制器作为备用方案
 * 
 * 【关联文件】
 * - 模板目录: src/main/resources/templates/
 * - 静态资源: src/main/resources/static/
 * 
 * 【注意事项】
 * - 如果使用Vue3单页应用，大部分路由由前端Router处理
 * - 此控制器主要用于支持多页应用模式或SEO优化需求
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Controller
public class PageController {

    /**
     * 主页路由
     * 
     * 功能说明:
     * - 处理根路径请求，返回应用主页
     * - 主页通常包含导航菜单和概览信息
     * 
     * @return 视图名称，对应templates/index.html
     */
    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    /**
     * 车辆分组页面路由
     * 
     * 功能说明:
     * - 显示车辆分组管理界面
     * - 支持分组的创建、编辑、删除操作
     * 
     * @return 视图名称，对应templates/groups.html
     */
    @GetMapping("/groups")
    public String groupsPage() {
        return "groups";
    }
    
    /**
     * 告警管理页面路由
     * 
     * 功能说明:
     * - 显示围栏告警列表和处理界面
     * - 支持告警查询、处理、统计功能
     * 
     * @return 视图名称，对应templates/alarms.html
     */
    @GetMapping("/alarms")
    public String alarmsPage() {
        return "alarms";
    }
    
    /**
     * 报表统计页面路由
     * 
     * 功能说明:
     * - 显示各类统计报表和图表
     * - 支持里程统计、驾驶行为分析等
     * 
     * @return 视图名称，对应templates/reports.html
     */
    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }
    
    /**
     * 地理围栏管理页面路由
     * 
     * 功能说明:
     * - 显示围栏配置和管理界面
     * - 支持在地图上绘制和管理围栏
     * 
     * @return 视图名称，对应templates/geofences.html
     */
    @GetMapping("/geofences")
    public String geofencesPage() {
        return "geofences";
    }
}
