package tw_six.demo.websocket;

/**
 * 车辆位置数据模型 - WebSocket传输专用
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 定义车辆位置数据的传输格式，用于WebSocket推送车辆实时位置信息。
 * 这是一个轻量级的数据传输对象(DTO)，只包含必要的位置信息。
 * 
 * 【数据结构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ VehicleLocationData                                                     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ - id: 车辆记录ID                                                        │
 * │ - carName: 车牌号/车辆名称                                              │
 * │ - latitude: 纬度 (WGS84坐标系)                                          │
 * │ - longitude: 经度 (WGS84坐标系)                                         │
 * │ - speed: 当前速度 (km/h)                                                │
 * │ - direction: 行驶方向 (0-360度，0为正北)                                │
 * │ - status: 车辆状态 (正常/超速/离线等)                                    │
 * │ - groupName: 所属分组名称                                               │
 * │ - lastUpdate: 最后更新时间                                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【坐标系说明】
 * 使用WGS84坐标系（GPS原始坐标系）：
 * - 纬度范围: -90 到 90（北纬为正，南纬为负）
 * - 经度范围: -180 到 180（东经为正，西经为负）
 * - 中国境内大致范围: 纬度18-54，经度73-135
 * 
 * 【方向说明】
 * direction字段表示车辆行驶方向：
 * - 0°: 正北
 * - 90°: 正东
 * - 180°: 正南
 * - 270°: 正西
 * 
 * 【JSON示例】
 * ```json
 * {
 *   "id": 1,
 *   "carName": "京A10001",
 *   "latitude": 32.0650,
 *   "longitude": 118.7500,
 *   "speed": 45.5,
 *   "direction": 90.0,
 *   "status": "正常",
 *   "groupName": "配送一组",
 *   "lastUpdate": "2024-01-15T10:30:00"
 * }
 * ```
 * 
 * 【前端使用示例】
 * ```javascript
 * // 接收WebSocket消息
 * stompClient.subscribe('/topic/vehicles', (message) => {
 *   const wsMessage = JSON.parse(message.body);
 *   if (wsMessage.type === 'VEHICLE_UPDATE') {
 *     const vehicle = wsMessage.data; // VehicleLocationData对象
 *     // 更新地图上的车辆标记
 *     updateVehicleMarker(vehicle);
 *   }
 * });
 * 
 * // 更新地图标记
 * function updateVehicleMarker(vehicle) {
 *   const marker = markers[vehicle.id];
 *   if (marker) {
 *     marker.setLngLat([vehicle.longitude, vehicle.latitude]);
 *     // 根据速度设置颜色
 *     const color = vehicle.speed > 60 ? 'red' : 'green';
 *     marker.getElement().style.color = color;
 *   }
 * }
 * ```
 * 
 * 【关联文件】
 * - WebSocketMessage.java: 消息封装类
 * - VehicleLocationPushService.java: 创建并发送此数据
 * - CarLocation.java: 数据库实体类（数据来源）
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class VehicleLocationData {

    /** 车辆记录ID - 唯一标识 */
    private Long id;
    
    /** 车牌号/车辆名称 - 如"京A10001" */
    private String carName;
    
    /** 纬度 - WGS84坐标系，中国境内约为18-54 */
    private Double latitude;
    
    /** 经度 - WGS84坐标系，中国境内约为73-135 */
    private Double longitude;
    
    /** 当前速度 - 单位：km/h */
    private Double speed;
    
    /** 行驶方向 - 0-360度，0为正北，顺时针增加 */
    private Double direction;
    
    /** 车辆状态 - 正常/超速/离线/维修等 */
    private String status;
    
    /** 所属分组名称 - 用于分组显示 */
    private String groupName;
    
    /** 路线名称 - 如"内环线A"、"南北干线B" */
    private String routeName;
    
    /** 路线颜色 - 用于地图显示，如"#1890ff" */
    private String routeColor;
    
    /** 最后更新时间 - ISO 8601格式 */
    private String lastUpdate;

    /**
     * 默认构造函数
     * 用于JSON序列化/反序列化
     */
    public VehicleLocationData() {
    }

    /**
     * 全参数构造函数
     * 
     * @param id 车辆ID
     * @param carName 车牌号
     * @param latitude 纬度
     * @param longitude 经度
     * @param speed 速度
     * @param direction 方向
     * @param status 状态
     * @param groupName 分组名称
     * @param lastUpdate 最后更新时间
     */
    public VehicleLocationData(Long id, String carName, Double latitude, Double longitude,
            Double speed, Double direction, String status, String groupName, String lastUpdate) {
        this.id = id;
        this.carName = carName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.direction = direction;
        this.status = status;
        this.groupName = groupName;
        this.lastUpdate = lastUpdate;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 判断车辆是否超速
     * 默认超速阈值为60km/h
     * 
     * @return true表示超速，false表示正常
     */
    public boolean isSpeeding() {
        return speed != null && speed > 60;
    }

    /**
     * 获取速度状态描述
     * 
     * @return 速度状态文本
     */
    public String getSpeedStatus() {
        if (speed == null) return "未知";
        if (speed > 60) return "超速";
        if (speed > 40) return "较快";
        return "正常";
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getDirection() {
        return direction;
    }

    public void setDirection(Double direction) {
        this.direction = direction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteColor() {
        return routeColor;
    }

    public void setRouteColor(String routeColor) {
        this.routeColor = routeColor;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * 转换为字符串
     * 用于日志输出和调试
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return String.format("VehicleLocationData{id=%d, carName='%s', lat=%.4f, lng=%.4f, speed=%.1f km/h}", 
            id, carName, latitude, longitude, speed != null ? speed : 0);
    }
}
