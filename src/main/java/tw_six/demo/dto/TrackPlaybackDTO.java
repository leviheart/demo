package tw_six.demo.dto;

import java.time.LocalDateTime;

/**
 * 轨迹回放数据传输对象 - 用于轨迹回放功能
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 定义轨迹回放所需的数据格式，包含轨迹点信息和回放控制信息。
 * 用于前端轨迹回放组件的数据展示和交互控制。
 * 
 * 【数据结构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ TrackPlaybackDTO                                                        │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ - trackId: 轨迹点ID                                                     │
 * │ - carName: 车辆名称                                                     │
 * │ - latitude: 纬度                                                        │
 * │ - longitude: 经度                                                       │
 * │ - speed: 速度 (km/h)                                                    │
 * │ - direction: 方向 (度)                                                  │
 * │ - recordTime: 记录时间                                                  │
 * │ - status: 状态                                                          │
 * │ - sequence: 序号 (用于回放排序)                                         │
 * │ - distance: 距起点距离 (米)                                             │
 * │ - totalDistance: 总距离 (米)                                            │
 * │ - progress: 回放进度 (%)                                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【使用场景】
 * 1. 轨迹回放：按时间顺序播放车辆历史轨迹
 * 2. 行程分析：展示行程详情和统计数据
 * 3. 路线展示：在地图上绘制历史行驶路线
 * 
 * 【关联文件】
 * - VehicleTrackController.java: 使用此类返回数据
 * - TrackPlayback.vue: 前端轨迹回放组件
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class TrackPlaybackDTO {

    /** 轨迹点ID */
    private Long trackId;
    
    /** 车辆名称/车牌号 */
    private String carName;
    
    /** 纬度 - WGS84坐标系 */
    private Double latitude;
    
    /** 经度 - WGS84坐标系 */
    private Double longitude;
    
    /** 速度 - 单位：km/h */
    private Double speed;
    
    /** 行驶方向 - 0-360度 */
    private Double direction;
    
    /** 记录时间 */
    private LocalDateTime recordTime;
    
    /** 轨迹点状态 */
    private String status;
    
    /** 序号 - 用于回放排序，从1开始 */
    private Integer sequence;
    
    /** 距起点累计距离 - 单位：米 */
    private Double distance;
    
    /** 总行程距离 - 单位：米 */
    private Double totalDistance;
    
    /** 回放进度百分比 - 0-100 */
    private Double progress;
    
    /** 时间间隔 - 与上一个点的时间差（秒） */
    private Long timeInterval;

    /**
     * 默认构造函数
     */
    public TrackPlaybackDTO() {
    }

    /**
     * 全参数构造函数
     */
    public TrackPlaybackDTO(Long trackId, String carName, Double latitude, Double longitude,
            Double speed, Double direction, LocalDateTime recordTime, String status) {
        this.trackId = trackId;
        this.carName = carName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.direction = direction;
        this.recordTime = recordTime;
        this.status = status;
    }

    // ==================== Getter和Setter方法 ====================

    public Long getTrackId() { return trackId; }
    public void setTrackId(Long trackId) { this.trackId = trackId; }

    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getDirection() { return direction; }
    public void setDirection(Double direction) { this.direction = direction; }

    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public Long getTimeInterval() { return timeInterval; }
    public void setTimeInterval(Long timeInterval) { this.timeInterval = timeInterval; }

    /**
     * 转换为字符串
     */
    @Override
    public String toString() {
        return String.format("TrackPlaybackDTO{seq=%d, lat=%.4f, lng=%.4f, speed=%.1f, time=%s}", 
            sequence, latitude, longitude, speed != null ? speed : 0, recordTime);
    }
}
