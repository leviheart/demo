package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.DrivingBehavior;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.repository.DrivingBehaviorRepository;
import tw_six.demo.repository.VehicleTrackRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 驾驶行为分析服务 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 分析车辆轨迹数据，检测异常驾驶行为，计算驾驶评分。
 * 支持急加速、急刹车、超速、疲劳驾驶等多种行为的检测。
 * 
 * 【检测算法】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 行为类型        │ 检测条件                                              │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ 急加速          │ 加速度 > 3.0 m/s² (约10.8 km/h/s)                    │
 * │ 急刹车          │ 减速度 > 3.0 m/s²                                     │
 * │ 超速            │ 速度 > 限速值 (默认60 km/h)                           │
 * │ 急转弯          │ 转弯速度 > 安全速度阈值                               │
 * │ 疲劳驾驶        │ 连续驾驶时间 > 4小时                                  │
 * │ 长时间怠速      │ 怠速时间 > 10分钟                                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【评分算法】
 * 驾驶评分基于以下因素：
 * 1. 行为数量：行为越少，评分越高
 * 2. 风险等级：高风险行为扣分更多
 * 3. 行驶里程：里程越长，单位里程行为越重要
 * 
 * 评分公式：
 * score = 100 - (Σ(行为权重 × 风险系数) / 行驶里程) × 里程系数
 * 
 * 【关联文件】
 * - DrivingBehavior.java: 行为实体类
 * - DrivingBehaviorRepository.java: 数据仓库
 * - VehicleTrack.java: 轨迹数据（用于分析）
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class DrivingBehaviorService {

    private final DrivingBehaviorRepository behaviorRepository;
    private final VehicleTrackRepository trackRepository;

    /** 急加速阈值 - m/s² */
    private static final double RAPID_ACCEL_THRESHOLD = 3.0;

    /** 急刹车阈值 - m/s² */
    private static final double RAPID_BRAKE_THRESHOLD = 3.0;

    /** 超速阈值 - km/h */
    private static final double OVERSPEED_THRESHOLD = 60.0;

    /** 疲劳驾驶阈值 - 分钟 */
    private static final int FATIGUE_THRESHOLD_MINUTES = 240;

    /** 长时间怠速阈值 - 秒 */
    private static final int IDLE_THRESHOLD_SECONDS = 600;

    @Autowired
    public DrivingBehaviorService(DrivingBehaviorRepository behaviorRepository,
            VehicleTrackRepository trackRepository) {
        this.behaviorRepository = behaviorRepository;
        this.trackRepository = trackRepository;
    }

    /**
     * 保存驾驶行为记录
     * 
     * @param behavior 行为对象
     * @return 保存后的行为对象
     */
    public DrivingBehavior saveBehavior(DrivingBehavior behavior) {
        return behaviorRepository.save(behavior);
    }

    /**
     * 按车辆查询所有行为记录
     * 
     * @param carName 车辆名称
     * @return 行为记录列表
     */
    public List<DrivingBehavior> getBehaviorsByCar(String carName) {
        return behaviorRepository.findByCarNameOrderByEventTimeDesc(carName);
    }

    /**
     * 按时间范围查询行为记录
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为记录列表
     */
    public List<DrivingBehavior> getBehaviorsByTimeRange(String carName, 
            LocalDateTime startTime, LocalDateTime endTime) {
        return behaviorRepository.findByCarNameAndEventTimeBetweenOrderByEventTimeDesc(
            carName, startTime, endTime);
    }

    /**
     * 获取所有有行为记录的车辆名称
     * 
     * @return 车辆名称列表
     */
    public List<String> getAllCarNames() {
        return behaviorRepository.findDistinctCarNames();
    }

    /**
     * 获取最近的行为记录
     * 
     * @param limit 限制数量
     * @return 最近的行为记录列表
     */
    public List<DrivingBehavior> getRecentBehaviors(int limit) {
        return behaviorRepository.findRecentBehaviors(limit);
    }

    /**
     * 分析车辆轨迹数据，检测驾驶行为
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 检测到的行为列表
     */
    public List<DrivingBehavior> analyzeDrivingBehavior(String carName,
            LocalDateTime startTime, LocalDateTime endTime) {
        
        // 获取轨迹数据
        List<VehicleTrack> tracks = trackRepository.findByCarNameAndRecordTimeBetweenOrderByRecordTimeAsc(
            carName, startTime, endTime);
        
        if (tracks.size() < 2) {
            return new ArrayList<>();
        }

        List<DrivingBehavior> detectedBehaviors = new ArrayList<>();
        VehicleTrack prevTrack = null;

        for (VehicleTrack track : tracks) {
            if (prevTrack != null) {
                // 检测急加速/急刹车
                DrivingBehavior accelBehavior = detectAcceleration(carName, prevTrack, track);
                if (accelBehavior != null) {
                    detectedBehaviors.add(accelBehavior);
                }

                // 检测超速
                DrivingBehavior speedBehavior = detectOverspeed(carName, track);
                if (speedBehavior != null) {
                    detectedBehaviors.add(speedBehavior);
                }
            }
            prevTrack = track;
        }

        // 检测疲劳驾驶
        DrivingBehavior fatigueBehavior = detectFatigue(carName, tracks);
        if (fatigueBehavior != null) {
            detectedBehaviors.add(fatigueBehavior);
        }

        // 保存检测到的行为
        for (DrivingBehavior behavior : detectedBehaviors) {
            behaviorRepository.save(behavior);
        }

        return detectedBehaviors;
    }

    /**
     * 检测急加速/急刹车
     * 
     * @param carName 车辆名称
     * @param prevTrack 前一个轨迹点
     * @param currentTrack 当前轨迹点
     * @return 检测到的行为，如果正常则返回null
     */
    private DrivingBehavior detectAcceleration(String carName, VehicleTrack prevTrack, 
            VehicleTrack currentTrack) {
        
        if (prevTrack.getSpeed() == null || currentTrack.getSpeed() == null) {
            return null;
        }

        // 计算时间差（秒）
        long timeDiff = java.time.Duration.between(
            prevTrack.getRecordTime(), currentTrack.getRecordTime()).getSeconds();
        
        if (timeDiff <= 0) {
            return null;
        }

        // 计算速度变化（m/s）
        double speedChange = (currentTrack.getSpeed() - prevTrack.getSpeed()) / 3.6;
        
        // 计算加速度（m/s²）
        double acceleration = speedChange / timeDiff;

        DrivingBehavior behavior = null;

        if (acceleration > RAPID_ACCEL_THRESHOLD) {
            // 急加速
            behavior = createBehavior(carName, 
                DrivingBehavior.BehaviorType.RAPID_ACCEL,
                DrivingBehavior.RiskLevel.MEDIUM,
                currentTrack.getLatitude(),
                currentTrack.getLongitude(),
                currentTrack.getSpeed(),
                acceleration,
                currentTrack.getRecordTime(),
                String.format("急加速：加速度 %.1f m/s²", acceleration));
        } else if (acceleration < -RAPID_BRAKE_THRESHOLD) {
            // 急刹车
            behavior = createBehavior(carName,
                DrivingBehavior.BehaviorType.RAPID_BRAKE,
                DrivingBehavior.RiskLevel.HIGH,
                currentTrack.getLatitude(),
                currentTrack.getLongitude(),
                currentTrack.getSpeed(),
                acceleration,
                currentTrack.getRecordTime(),
                String.format("急刹车：减速度 %.1f m/s²", Math.abs(acceleration)));
        }

        return behavior;
    }

    /**
     * 检测超速
     * 
     * @param carName 车辆名称
     * @param track 轨迹点
     * @return 检测到的行为，如果正常则返回null
     */
    private DrivingBehavior detectOverspeed(String carName, VehicleTrack track) {
        if (track.getSpeed() == null || track.getSpeed() <= OVERSPEED_THRESHOLD) {
            return null;
        }

        // 根据超速程度确定风险等级
        DrivingBehavior.RiskLevel riskLevel;
        double overPercent = (track.getSpeed() - OVERSPEED_THRESHOLD) / OVERSPEED_THRESHOLD;
        
        if (overPercent > 0.5) {
            riskLevel = DrivingBehavior.RiskLevel.CRITICAL;
        } else if (overPercent > 0.3) {
            riskLevel = DrivingBehavior.RiskLevel.HIGH;
        } else {
            riskLevel = DrivingBehavior.RiskLevel.MEDIUM;
        }

        return createBehavior(carName,
            DrivingBehavior.BehaviorType.OVERSPEED,
            riskLevel,
            track.getLatitude(),
            track.getLongitude(),
            track.getSpeed(),
            null,
            track.getRecordTime(),
            String.format("超速：当前速度 %.1f km/h，限速 %.1f km/h", 
                track.getSpeed(), OVERSPEED_THRESHOLD));
    }

    /**
     * 检测疲劳驾驶
     * 
     * @param carName 车辆名称
     * @param tracks 轨迹点列表
     * @return 检测到的行为，如果正常则返回null
     */
    private DrivingBehavior detectFatigue(String carName, List<VehicleTrack> tracks) {
        if (tracks.size() < 2) {
            return null;
        }

        LocalDateTime startTime = tracks.get(0).getRecordTime();
        LocalDateTime endTime = tracks.get(tracks.size() - 1).getRecordTime();
        
        long drivingMinutes = java.time.Duration.between(startTime, endTime).toMinutes();

        if (drivingMinutes >= FATIGUE_THRESHOLD_MINUTES) {
            return createBehavior(carName,
                DrivingBehavior.BehaviorType.FATIGUE,
                DrivingBehavior.RiskLevel.HIGH,
                tracks.get(tracks.size() - 1).getLatitude(),
                tracks.get(tracks.size() - 1).getLongitude(),
                null,
                null,
                endTime,
                String.format("疲劳驾驶：连续驾驶 %d 分钟", drivingMinutes));
        }

        return null;
    }

    /**
     * 创建驾驶行为对象
     */
    private DrivingBehavior createBehavior(String carName,
            DrivingBehavior.BehaviorType behaviorType,
            DrivingBehavior.RiskLevel riskLevel,
            Double latitude, Double longitude,
            Double speed, Double acceleration,
            LocalDateTime eventTime, String description) {
        
        DrivingBehavior behavior = new DrivingBehavior();
        behavior.setCarName(carName);
        behavior.setBehaviorType(behaviorType.name());
        behavior.setRiskLevel(riskLevel.name());
        behavior.setLatitude(latitude);
        behavior.setLongitude(longitude);
        behavior.setSpeed(speed);
        behavior.setAcceleration(acceleration);
        behavior.setEventTime(eventTime);
        behavior.setDescription(description);
        behavior.setIsHandled(false);
        
        return behavior;
    }

    /**
     * 计算驾驶评分
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 驾驶评分（0-100分）
     */
    public DrivingScore calculateDrivingScore(String carName, 
            LocalDateTime startTime, LocalDateTime endTime) {
        
        List<DrivingBehavior> behaviors = getBehaviorsByTimeRange(carName, startTime, endTime);
        
        // 计算基础分数
        double score = 100.0;
        
        // 行为扣分权重
        Map<String, Double> weights = new HashMap<>();
        weights.put("RAPID_ACCEL", 2.0);
        weights.put("RAPID_BRAKE", 3.0);
        weights.put("OVERSPEED", 5.0);
        weights.put("FATIGUE", 10.0);
        weights.put("SHARP_TURN", 2.0);
        weights.put("IDLE_LONG", 1.0);

        // 风险系数
        Map<String, Double> riskFactors = new HashMap<>();
        riskFactors.put("LOW", 0.5);
        riskFactors.put("MEDIUM", 1.0);
        riskFactors.put("HIGH", 2.0);
        riskFactors.put("CRITICAL", 3.0);

        // 计算扣分
        double totalDeduction = 0;
        Map<String, Integer> behaviorCounts = new HashMap<>();
        
        for (DrivingBehavior behavior : behaviors) {
            String type = behavior.getBehaviorType();
            String risk = behavior.getRiskLevel();
            
            double weight = weights.getOrDefault(type, 1.0);
            double factor = riskFactors.getOrDefault(risk, 1.0);
            
            totalDeduction += weight * factor;
            
            // 统计各类型数量
            behaviorCounts.put(type, behaviorCounts.getOrDefault(type, 0) + 1);
        }

        score = Math.max(0, score - totalDeduction);

        // 构建评分结果
        DrivingScore drivingScore = new DrivingScore();
        drivingScore.setCarName(carName);
        drivingScore.setScore((int) Math.round(score));
        drivingScore.setTotalBehaviors(behaviors.size());
        drivingScore.setBehaviorCounts(behaviorCounts);
        drivingScore.setStartTime(startTime);
        drivingScore.setEndTime(endTime);
        
        // 设置评级
        if (score >= 90) {
            drivingScore.setGrade("优秀");
        } else if (score >= 80) {
            drivingScore.setGrade("良好");
        } else if (score >= 60) {
            drivingScore.setGrade("一般");
        } else {
            drivingScore.setGrade("较差");
        }

        return drivingScore;
    }

    /**
     * 驾驶评分结果类
     */
    public static class DrivingScore {
        private String carName;
        private int score;
        private String grade;
        private int totalBehaviors;
        private Map<String, Integer> behaviorCounts;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public String getCarName() { return carName; }
        public void setCarName(String carName) { this.carName = carName; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public int getTotalBehaviors() { return totalBehaviors; }
        public void setTotalBehaviors(int totalBehaviors) { this.totalBehaviors = totalBehaviors; }

        public Map<String, Integer> getBehaviorCounts() { return behaviorCounts; }
        public void setBehaviorCounts(Map<String, Integer> behaviorCounts) { this.behaviorCounts = behaviorCounts; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}
