package tw_six.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轨迹回放响应数据传输对象 - 包含完整回放数据
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 封装轨迹回放的完整数据，包括轨迹点列表、统计信息等。
 * 用于一次性返回所有回放所需数据，减少网络请求。
 * 
 * 【数据结构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ TrackPlaybackResponse                                                   │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ - carName: 车辆名称                                                     │
 * │ - tracks: 轨迹点列表                                                    │
 * │ - startTime: 开始时间                                                   │
 * │ - endTime: 结束时间                                                     │
 * │ - totalDistance: 总距离 (公里)                                          │
 * │ - totalDuration: 总时长 (分钟)                                          │
 * │ - avgSpeed: 平均速度 (km/h)                                             │
 * │ - maxSpeed: 最高速度 (km/h)                                             │
 * │ - pointCount: 轨迹点数量                                                │
 * │ - startLocation: 起点位置                                               │
 * │ - endLocation: 终点位置                                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【关联文件】
 * - VehicleTrackController.java: 使用此类返回数据
 * - TrackPlayback.vue: 前端轨迹回放组件
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class TrackPlaybackResponse {

    /** 车辆名称 */
    private String carName;
    
    /** 轨迹点列表 */
    private List<TrackPlaybackDTO> tracks;
    
    /** 行程开始时间 */
    private LocalDateTime startTime;
    
    /** 行程结束时间 */
    private LocalDateTime endTime;
    
    /** 总行驶距离 - 单位：公里 */
    private Double totalDistance;
    
    /** 总行驶时长 - 单位：分钟 */
    private Long totalDuration;
    
    /** 平均速度 - 单位：km/h */
    private Double avgSpeed;
    
    /** 最高速度 - 单位：km/h */
    private Double maxSpeed;
    
    /** 轨迹点数量 */
    private Integer pointCount;
    
    /** 起点位置描述 */
    private String startLocation;
    
    /** 终点位置描述 */
    private String endLocation;

    /**
     * 默认构造函数
     */
    public TrackPlaybackResponse() {
    }

    /**
     * 静态工厂方法 - 创建回放响应
     * 
     * @param carName 车辆名称
     * @param tracks 轨迹点列表
     * @return 回放响应对象
     */
    public static TrackPlaybackResponse of(String carName, List<TrackPlaybackDTO> tracks) {
        TrackPlaybackResponse response = new TrackPlaybackResponse();
        response.setCarName(carName);
        response.setTracks(tracks);
        response.setPointCount(tracks.size());
        
        if (!tracks.isEmpty()) {
            response.setStartTime(tracks.get(0).getRecordTime());
            response.setEndTime(tracks.get(tracks.size() - 1).getRecordTime());
            
            // 计算总距离
            Double totalDist = tracks.get(tracks.size() - 1).getDistance();
            response.setTotalDistance(totalDist != null ? totalDist / 1000 : 0);
            
            // 计算总时长（分钟）
            if (response.getStartTime() != null && response.getEndTime() != null) {
                long minutes = java.time.Duration.between(
                    response.getStartTime(), response.getEndTime()
                ).toMinutes();
                response.setTotalDuration(minutes);
                
                // 计算平均速度
                if (minutes > 0 && totalDist != null) {
                    double hours = minutes / 60.0;
                    response.setAvgSpeed(totalDist / 1000 / hours);
                }
            }
            
            // 计算最高速度
            Double maxSpd = tracks.stream()
                .map(TrackPlaybackDTO::getSpeed)
                .filter(s -> s != null)
                .max(Double::compare)
                .orElse(0.0);
            response.setMaxSpeed(maxSpd);
        }
        
        return response;
    }

    // ==================== Getter和Setter方法 ====================

    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }

    public List<TrackPlaybackDTO> getTracks() { return tracks; }
    public void setTracks(List<TrackPlaybackDTO> tracks) { this.tracks = tracks; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public Long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }

    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }

    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }

    public Integer getPointCount() { return pointCount; }
    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
}
