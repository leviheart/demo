# 车辆监控系统 - 学习指南

## 一、项目架构

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端 (Vue 3)                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │  地图监控   │  │  轨迹回放   │  │  车辆分组   │  │  地理围栏   │       │
│  │ MapView.vue │  │TrackPlayback│  │  Groups.vue │  │ GeoFences   │       │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘       │
│         │                │                │                │               │
│         └────────────────┴────────────────┴────────────────┘               │
│                                   │                                         │
│                    ┌──────────────┴──────────────┐                         │
│                    │    WebSocket / HTTP API     │                         │
│                    └──────────────┬──────────────┘                         │
└───────────────────────────────────┼─────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼─────────────────────────────────────────┐
│                         后端 (Spring Boot 3)                                │
│                                   │                                         │
│  ┌──────────────┐  ┌──────────────┴──────────────┐  ┌──────────────┐       │
│  │   Security   │  │      Controller层           │  │   WebSocket  │       │
│  │ JWT认证过滤器 │──│  处理HTTP请求，返回JSON响应  │──│  实时推送服务 │       │
│  └──────────────┘  └──────────────┬──────────────┘  └──────────────┘       │
│                                   │                                         │
│                    ┌──────────────┴──────────────┐                         │
│                    │        Service层            │                         │
│                    │   业务逻辑处理、数据计算     │                         │
│                    └──────────────┬──────────────┘                         │
│                                   │                                         │
│                    ┌──────────────┴──────────────┐                         │
│                    │      Repository层           │                         │
│                    │   数据库访问、CRUD操作       │                         │
│                    └──────────────┬──────────────┘                         │
└───────────────────────────────────┼─────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │      H2 Database / MySQL      │
                    │         数据存储              │
                    └───────────────────────────────┘
```

### 1.2 技术栈详情

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **前端框架** | Vue 3 | 3.x | 响应式UI框架 |
| **UI组件库** | Element Plus | 2.x | 企业级UI组件 |
| **地图引擎** | Mapbox GL JS | 2.x | 矢量地图渲染 |
| **状态管理** | Pinia/Composition API | - | 数据状态管理 |
| **后端框架** | Spring Boot | 3.x | 微服务框架 |
| **安全框架** | Spring Security | 6.x | 认证授权 |
| **Token认证** | JWT (jjwt) | 0.11.x | 无状态认证 |
| **ORM框架** | Spring Data JPA | 3.x | 数据库操作 |
| **数据库** | H2 Database | 2.x | 内存数据库 |
| **WebSocket** | STOMP | - | 实时通信 |
| **Excel导出** | Apache POI | 5.x | 报表生成 |

---

## 二、后端代码结构详解

### 2.1 分层架构

```
src/main/java/tw_six/demo/
│
├── config/                    # 配置层
│   ├── SecurityConfig.java    # Spring Security安全配置
│   │   - JWT过滤器配置
│   │   - 路径权限配置
│   │   - 密码加密配置
│   │
│   ├── WebSocketConfig.java   # WebSocket配置
│   │   - STOMP消息代理配置
│   │   - 消息端点配置
│   │
│   └── DataInitializer.java   # 数据初始化
│       - 启动时自动创建测试数据
│
├── controller/                # 控制器层 - API接口
│   ├── VehicleGroupController.java    # 车辆分组API
│   ├── GeoFenceController.java        # 地理围栏API
│   ├── FenceAlertController.java      # 围栏告警API
│   ├── VehicleTrackController.java    # 轨迹回放API
│   ├── DrivingBehaviorController.java # 驾驶行为API
│   └── AuthController.java            # 认证API
│
├── service/                   # 服务层 - 业务逻辑
│   ├── VehicleGroupService.java       # 分组业务逻辑
│   ├── GeoFenceService.java           # 围栏业务逻辑
│   ├── FenceAlertService.java         # 告警业务逻辑
│   ├── VehicleTrackService.java       # 轨迹业务逻辑
│   └── DrivingBehaviorService.java    # 驾驶行为分析
│
├── repository/                # 数据访问层
│   ├── VehicleGroupRepository.java    # 继承JpaRepository
│   ├── GeoFenceRepository.java
│   ├── FenceAlertRepository.java
│   └── ...
│
├── entity/                    # 实体类 - 数据库映射
│   ├── VehicleGroup.java      # @Entity注解
│   ├── GeoFence.java
│   ├── FenceAlert.java
│   ├── VehicleTrack.java
│   └── DrivingBehavior.java
│
├── dto/                       # 数据传输对象
│   ├── TrackPlaybackDTO.java  # 轨迹回放DTO
│   └── TrackPlaybackResponse.java
│
├── websocket/                 # WebSocket相关
│   ├── VehicleLocationPushService.java  # 位置推送服务
│   ├── AlertPushService.java            # 告警推送服务
│   ├── WebSocketMessage.java            # 消息封装
│   └── VehicleLocationData.java         # 位置数据
│
├── security/                  # 安全认证
│   ├── JwtUtils.java          # JWT工具类
│   ├── JwtAuthenticationFilter.java  # JWT过滤器
│   └── UserDetailsServiceImpl.java   # 用户详情服务
│
└── common/                    # 公共类
    └── ApiResponse.java       # 统一响应格式
```

### 2.2 核心代码阅读顺序

**建议阅读顺序：**

1. **实体层** → 理解数据模型
   - `entity/VehicleGroup.java` - 分组实体
   - `entity/GeoFence.java` - 围栏实体
   - `entity/FenceAlert.java` - 告警实体

2. **仓库层** → 理解数据访问
   - `repository/*Repository.java` - JPA接口

3. **服务层** → 理解业务逻辑
   - `service/VehicleGroupService.java`
   - `service/GeoFenceService.java`

4. **控制器层** → 理解API设计
   - `controller/VehicleGroupController.java`
   - `controller/GeoFenceController.java`

5. **安全配置** → 理解认证授权
   - `config/SecurityConfig.java`
   - `security/JwtUtils.java`

6. **WebSocket** → 理解实时通信
   - `websocket/VehicleLocationPushService.java`

---

## 三、前端代码结构详解

### 3.1 组件结构

```
frontend/src/
│
├── components/                # 页面组件
│   ├── MapView.vue           # 地图监控（核心）
│   │   - Mapbox地图初始化
│   │   - WebSocket连接
│   │   - 车辆标记渲染
│   │   - 路线可视化
│   │
│   ├── TrackPlayback.vue     # 轨迹回放
│   │   - 时间轴控制
│   │   - 播放/暂停/速度控制
│   │   - 进度条拖动
│   │
│   ├── VehicleGroups.vue     # 车辆分组管理
│   │   - 分组列表展示
│   │   - 新建/删除分组
│   │
│   ├── GeoFences.vue         # 地理围栏管理
│   │   - 围栏地图展示
│   │   - 围栏创建/删除
│   │   - 状态切换
│   │
│   ├── FenceAlerts.vue       # 报警记录
│   │   - 告警列表
│   │   - 告警处理
│   │   - 时间范围筛选
│   │
│   └── DrivingAnalysis.vue   # 驾驶行为分析
│       - 行为统计图表
│       - 风险等级分布
│
├── services/                  # 服务封装
│   ├── WebSocketService.js   # WebSocket连接管理
│   └── NotificationService.js # 通知服务
│
├── App.vue                    # 主应用组件
│   - 路由导航
│   - 布局框架
│
└── main.js                    # 应用入口
```

### 3.2 前端学习要点

#### 3.2.1 Vue 3 Composition API

```javascript
// 使用 <script setup> 语法糖
<script setup>
import { ref, onMounted, computed } from 'vue'

// 响应式数据
const vehicles = ref([])
const loading = ref(false)

// 计算属性
const filteredVehicles = computed(() => {
  return vehicles.value.filter(v => v.status === 'active')
})

// 生命周期
onMounted(() => {
  loadVehicles()
})

// 方法
const loadVehicles = async () => {
  loading.value = true
  // ...
}
</script>
```

#### 3.2.2 Mapbox GL JS 地图使用

```javascript
// 1. 初始化地图
const map = new mapboxgl.Map({
  container: mapContainer.value,
  style: 'mapbox://styles/mapbox/streets-v11',
  center: [118.7634, 32.0617],
  zoom: 13
})

// 2. 添加标记
const marker = new mapboxgl.Marker()
  .setLngLat([lng, lat])
  .addTo(map)

// 3. 添加GeoJSON图层
map.addSource('fences', {
  type: 'geojson',
  data: geojsonData
})

map.addLayer({
  id: 'fences-layer',
  type: 'fill',
  source: 'fences',
  paint: {
    'fill-color': '#409eff',
    'fill-opacity': 0.3
  }
})
```

#### 3.2.3 WebSocket 实时通信

```javascript
// 1. 建立连接
const socket = new SockJS('http://localhost:8081/ws')
const stompClient = Stomp.over(socket)

// 2. 订阅主题
stompClient.connect({}, () => {
  stompClient.subscribe('/topic/vehicles', (message) => {
    const data = JSON.parse(message.body)
    // 处理接收到的数据
  })
})

// 3. 发送消息
stompClient.send('/app/location', {}, JSON.stringify(data))
```

---

## 四、API接口文档

### 4.1 统一响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... },
  "code": 200,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 4.2 主要API端点

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **车辆分组** | GET | /api/vehicle-groups | 获取所有分组 |
| | POST | /api/vehicle-groups | 创建分组 |
| | DELETE | /api/vehicle-groups/{id} | 删除分组 |
| **地理围栏** | GET | /api/geo-fences | 获取所有围栏 |
| | POST | /api/geo-fences | 创建围栏 |
| | PUT | /api/geo-fences/{id}/status | 更新状态 |
| **围栏告警** | GET | /api/fence-alerts | 获取所有告警 |
| | POST | /api/fence-alerts/{id}/resolve | 处理告警 |
| **轨迹回放** | GET | /api/tracks/playback | 获取回放数据 |
| **驾驶行为** | GET | /api/behaviors/statistics | 行为统计 |
| **认证** | POST | /api/auth/login | 用户登录 |
| | POST | /api/auth/register | 用户注册 |

---

## 五、数据库设计

### 5.1 核心表结构

```sql
-- 车辆分组表
CREATE TABLE vehicle_groups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_name VARCHAR(50),
  description VARCHAR(500),
  status VARCHAR(20),
  created_time TIMESTAMP
);

-- 地理围栏表
CREATE TABLE geo_fences (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  fence_name VARCHAR(50),
  center_latitude DOUBLE,
  center_longitude DOUBLE,
  radius DOUBLE,
  fence_type VARCHAR(20),
  alert_type VARCHAR(20),
  status VARCHAR(20),
  description VARCHAR(500)
);

-- 围栏告警表
CREATE TABLE fence_alerts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  geo_fence_id BIGINT,
  car_name VARCHAR(20),
  latitude DOUBLE,
  longitude DOUBLE,
  alert_type VARCHAR(20),
  severity VARCHAR(20),
  is_handled BOOLEAN,
  handler VARCHAR(50),
  created_time TIMESTAMP
);

-- 车辆轨迹表
CREATE TABLE vehicle_tracks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  car_name VARCHAR(20),
  latitude DOUBLE,
  longitude DOUBLE,
  speed DOUBLE,
  direction DOUBLE,
  status VARCHAR(20),
  record_time TIMESTAMP
);

-- 驾驶行为表
CREATE TABLE driving_behaviors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  car_name VARCHAR(20),
  behavior_type VARCHAR(30),
  risk_level VARCHAR(20),
  speed DOUBLE,
  acceleration DOUBLE,
  latitude DOUBLE,
  longitude DOUBLE,
  description VARCHAR(200),
  is_handled BOOLEAN,
  event_time TIMESTAMP
);
```

---

## 六、学习路径建议

### 6.1 初级阶段（1-2周）

1. **理解项目结构**
   - 阅读本文档
   - 运行项目，体验所有功能
   - 查看数据库数据

2. **学习基础CRUD**
   - 阅读 VehicleGroup 相关代码
   - 理解 Controller → Service → Repository 流程
   - 尝试修改/新增字段

### 6.2 中级阶段（2-4周）

1. **学习WebSocket实时通信**
   - 阅读 VehicleLocationPushService
   - 理解 STOMP 协议
   - 尝试修改推送频率或数据格式

2. **学习地图集成**
   - 阅读 MapView.vue
   - 学习 Mapbox GL JS API
   - 尝试添加新的地图图层

3. **学习安全认证**
   - 阅读 SecurityConfig
   - 理解 JWT 认证流程
   - 尝试添加新的权限控制

### 6.3 高级阶段（4周+）

1. **性能优化**
   - 数据库查询优化
   - 前端渲染优化
   - WebSocket消息优化

2. **功能扩展**
   - 添加新的分析报表
   - 集成第三方地图服务
   - 添加移动端适配

---

## 七、常见问题

### Q1: 如何切换到MySQL数据库？

修改 `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vehicle_monitor
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

### Q2: 如何修改WebSocket推送频率？

修改 `VehicleLocationPushService.java`:
```java
@Scheduled(initialDelay = 5000, fixedRate = 1000)  // 修改fixedRate值
public void pushVehicleLocations() {
    // ...
}
```

### Q3: 如何添加新的API接口？

1. 创建Entity实体类
2. 创建Repository接口
3. 创建Service服务类
4. 创建Controller控制器
5. 在SecurityConfig中配置权限

---

## 八、相关资源

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Element Plus 文档](https://element-plus.org/zh-CN/)
- [Mapbox GL JS 文档](https://docs.mapbox.com/mapbox-gl-js/api/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Spring Security 文档](https://spring.io/projects/spring-security)
- [Spring Data JPA 文档](https://spring.io/projects/spring-data-jpa)
