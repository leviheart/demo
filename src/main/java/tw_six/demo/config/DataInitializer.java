package tw_six.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.*;
import tw_six.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据初始化配置类
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 应用启动时自动初始化测试数据，便于开发和测试。
 * 实现CommandLineRunner接口，在Spring容器启动完成后执行。
 * 
 * 【初始化数据】
 * - 车辆位置数据：12辆车的实时位置
 * - 车辆分组数据：3个分组
 * - 地理围栏数据：2个围栏
 * - 围栏告警数据：若干告警记录
 * - 驾驶统计数据：历史统计记录
 * 
 * 【执行时机】
 * Spring Boot应用启动完成后自动执行一次
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CarLocationRepository carLocationRepository;
    
    @Autowired
    private VehicleGroupRepository vehicleGroupRepository;
    
    @Autowired
    private GeoFenceRepository geoFenceRepository;
    
    @Autowired
    private FenceAlertRepository fenceAlertRepository;
    
    @Autowired
    private DrivingStatisticsRepository drivingStatisticsRepository;
    
    @Autowired
    private VehicleTrackRepository vehicleTrackRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        clearAllData();
        initVehicleGroups();
        initCarLocations();
        initGeoFences();
        initFenceAlerts();
        initDrivingStatistics();
        initVehicleTracks();
        initUsers();
        
        System.out.println("========================================");
        System.out.println("测试数据初始化完成！(12辆车辆)");
        System.out.println("========================================");
    }
    
    /**
     * 清空所有表数据
     * 使用deleteAllInBatch直接执行DELETE语句，避免JPA逐个删除的问题
     */
    private void clearAllData() {
        System.out.println("→ 清空旧数据...");
        fenceAlertRepository.deleteAllInBatch();
        drivingStatisticsRepository.deleteAllInBatch();
        vehicleTrackRepository.deleteAllInBatch();
        carLocationRepository.deleteAllInBatch();
        geoFenceRepository.deleteAllInBatch();
        vehicleGroupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        System.out.println("→ 旧数据已清空");
    }
    
    /**
     * 初始化车辆分组数据
     */
    private void initVehicleGroups() {
        VehicleGroup group1 = new VehicleGroup();
        group1.setGroupName("华北区车队");
        group1.setDescription("负责华北区域的配送车辆");
        group1.setStatus("active");
        vehicleGroupRepository.save(group1);
        
        VehicleGroup group2 = new VehicleGroup();
        group2.setGroupName("华东区车队");
        group2.setDescription("负责华东区域的配送车辆");
        group2.setStatus("active");
        vehicleGroupRepository.save(group2);
        
        VehicleGroup group3 = new VehicleGroup();
        group3.setGroupName("华南区车队");
        group3.setDescription("负责华南区域的配送车辆");
        group3.setStatus("active");
        vehicleGroupRepository.save(group3);
        
        System.out.println("→ 车辆分组数据初始化完成");
    }
    
    /**
     * 初始化车辆位置数据 - 12辆车
     * 每辆车有不同的起始位置和方向，分布在南京市区域
     */
    private void initCarLocations() {
        // 车辆1 - 环线内环方向
        saveCarLocation("京A10001", 32.0650, 118.7500, 45.0, 90.0, "active");
        // 车辆2 - 环线内环方向
        saveCarLocation("京A10002", 32.0550, 118.7800, 38.0, 180.0, "active");
        // 车辆3 - 环线外环方向
        saveCarLocation("京B20001", 32.0700, 118.7650, 52.0, 270.0, "active");
        // 车辆4 - 环线外环方向
        saveCarLocation("京B20002", 32.0500, 118.7700, 42.0, 0.0, "active");
        // 车辆5 - 南北向主干道
        saveCarLocation("沪C30001", 32.0600, 118.7550, 55.0, 45.0, "active");
        // 车辆6 - 南北向主干道
        saveCarLocation("沪C30002", 32.0680, 118.7750, 48.0, 225.0, "active");
        // 车辆7 - 东西向主干道
        saveCarLocation("沪D40001", 32.0620, 118.7450, 60.0, 90.0, "active");
        // 车辆8 - 东西向主干道
        saveCarLocation("沪D40002", 32.0580, 118.7850, 35.0, 270.0, "active");
        // 车辆9 - 配送区域1
        saveCarLocation("粤E50001", 32.0720, 118.7580, 40.0, 135.0, "active");
        // 车辆10 - 配送区域2
        saveCarLocation("粤E50002", 32.0480, 118.7720, 50.0, 315.0, "active");
        // 车辆11 - 商业区巡游
        saveCarLocation("苏F60001", 32.0635, 118.7680, 30.0, 60.0, "active");
        // 车辆12 - 商业区巡游
        saveCarLocation("苏F60002", 32.0570, 118.7620, 65.0, 240.0, "active");
        
        System.out.println("→ 车辆位置数据初始化完成 (12辆)");
    }
    
    private void saveCarLocation(String carName, double lat, double lng, double speed, double direction, String status) {
        CarLocation location = new CarLocation();
        location.setCarName(carName);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setSpeed(speed);
        location.setDirection(direction);
        location.setStatus(status);
        location.setUpdateTime(LocalDateTime.now());
        carLocationRepository.save(location);
    }
    
    /**
     * 初始化地理围栏数据
     */
    private void initGeoFences() {
        GeoFence fence1 = new GeoFence();
        fence1.setFenceName("南京市中心围栏");
        fence1.setCenterLatitude(32.0617);
        fence1.setCenterLongitude(118.7634);
        fence1.setRadius(2000.0);
        fence1.setFenceType("circle");
        fence1.setAlertType("exit");
        fence1.setStatus("active");
        fence1.setDescription("车辆离开市中心区域时告警");
        geoFenceRepository.save(fence1);
        
        GeoFence fence2 = new GeoFence();
        fence2.setFenceName("配送站围栏");
        fence2.setCenterLatitude(32.0587);
        fence2.setCenterLongitude(118.7789);
        fence2.setRadius(500.0);
        fence2.setFenceType("circle");
        fence2.setAlertType("both");
        fence2.setStatus("active");
        fence2.setDescription("车辆进出配送站时告警");
        geoFenceRepository.save(fence2);
        
        System.out.println("→ 地理围栏数据初始化完成");
    }
    
    /**
     * 初始化围栏告警数据
     */
    private void initFenceAlerts() {
        GeoFence fence = geoFenceRepository.findAll().stream().findFirst().orElse(null);
        
        createFenceAlert(fence, "京A10001", 32.0650, 118.7500, "exit", false);
        createFenceAlert(fence, "京A10002", 32.0550, 118.7800, "entry", false);
        createFenceAlert(fence, "沪C30001", 32.0600, 118.7550, "exit", true);
        createFenceAlert(fence, "沪D40001", 32.0620, 118.7450, "entry", true);
        
        System.out.println("→ 围栏告警数据初始化完成");
    }
    
    private void createFenceAlert(GeoFence fence, String carName, double lat, double lng, String alertType, boolean handled) {
        FenceAlert alert = new FenceAlert();
        alert.setGeoFence(fence);
        alert.setCarName(carName);
        alert.setLatitude(lat);
        alert.setLongitude(lng);
        alert.setAlertType(alertType);
        alert.setIsHandled(handled);
        alert.setCreatedTime(LocalDateTime.now().minusHours((long)(Math.random() * 24)));
        if (handled) {
            alert.setHandler("管理员");
            alert.setHandleTime(LocalDateTime.now());
        }
        fenceAlertRepository.save(alert);
    }
    
    /**
     * 初始化驾驶统计数据
     */
    private void initDrivingStatistics() {
        String[] carNames = {"京A10001", "京A10002", "京B20001", "京B20002", 
                             "沪C30001", "沪C30002", "沪D40001", "沪D40002",
                             "粤E50001", "粤E50002", "苏F60001", "苏F60002"};
        
        for (String carName : carNames) {
            createDrivingStats(carName, LocalDate.now().minusDays(1), 
                100 + Math.random() * 150, 
                120 + (int)(Math.random() * 100), 
                45 + Math.random() * 20, 
                70 + Math.random() * 30);
            createDrivingStats(carName, LocalDate.now().minusDays(2), 
                80 + Math.random() * 120, 
                100 + (int)(Math.random() * 80), 
                40 + Math.random() * 25, 
                65 + Math.random() * 35);
        }
        
        System.out.println("→ 驾驶统计数据初始化完成");
    }
    
    private void createDrivingStats(String carName, LocalDate date, double mileage, int duration, double avgSpeed, double maxSpeed) {
        DrivingStatistics stats = new DrivingStatistics();
        stats.setCarName(carName);
        stats.setRecordDate(date);
        stats.setTotalMileage(mileage);
        stats.setDrivingTimeMinutes(duration);
        stats.setAverageSpeed(avgSpeed);
        stats.setMaxSpeed(maxSpeed);
        drivingStatisticsRepository.save(stats);
    }
    
    /**
     * 初始化车辆轨迹数据
     */
    private void initVehicleTracks() {
        String[] carNames = {"京A10001", "京A10002", "京B20001"};
        
        for (String carName : carNames) {
            double baseLat = 32.0617 + (Math.random() - 0.5) * 0.02;
            double baseLng = 118.7634 + (Math.random() - 0.5) * 0.02;
            
            for (int i = 0; i < 20; i++) {
                VehicleTrack track = new VehicleTrack();
                track.setCarName(carName);
                track.setLatitude(baseLat + (i * 0.0005));
                track.setLongitude(baseLng + (i * 0.0008));
                track.setSpeed(30.0 + Math.random() * 40);
                track.setDirection(90.0 + i * 5);
                track.setRecordTime(LocalDateTime.now().minusMinutes(20 - i));
                vehicleTrackRepository.save(track);
            }
        }
        
        System.out.println("→ 车辆轨迹数据初始化完成");
    }
    
    /**
     * 初始化用户数据
     */
    private void initUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setAge(30);
        admin.setAddress("北京市朝阳区");
        userRepository.save(admin);
        
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setAge(25);
        user.setAddress("上海市浦东新区");
        userRepository.save(user);
        
        System.out.println("→ 用户数据初始化完成");
    }
}
