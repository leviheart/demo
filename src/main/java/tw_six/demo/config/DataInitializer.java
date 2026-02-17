package tw_six.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.*;
import tw_six.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数据初始化配置类 - 完善测试数据
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 应用启动时自动初始化丰富的测试数据，便于开发和演示。
 * 生成真实的模拟数据，包括完整行驶轨迹、驾驶行为等。
 * 
 * 【初始化数据】
 * - 车辆位置数据：12辆车的实时位置
 * - 车辆分组数据：3个分组，每辆车关联分组
 * - 地理围栏数据：8个围栏（多种类型）
 * - 围栏告警数据：50+条告警记录
 * - 驾驶统计数据：最近30天的统计
 * - 车辆轨迹数据：每辆车300+个轨迹点（完整路线，支持轨迹回放）
 * - 驾驶行为数据：200+条（急加速、急刹车、超速、疲劳驾驶等）
 * - 用户数据：管理员、操作员、观察者
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
    private DrivingBehaviorRepository drivingBehaviorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();
    
    private static final double CENTER_LAT = 32.0617;
    private static final double CENTER_LNG = 118.7634;
    
    private List<VehicleGroup> savedGroups = new ArrayList<>();
    private List<GeoFence> savedFences = new ArrayList<>();

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
        initDrivingBehaviors();
        initUsers();
        
        System.out.println("========================================");
        System.out.println("测试数据初始化完成！");
        System.out.println("- 12辆车辆（每辆300+轨迹点）");
        System.out.println("- 8个车辆分组");
        System.out.println("- 12个地理围栏");
        System.out.println("- 100+条围栏告警");
        System.out.println("- 30天驾驶统计（360条）");
        System.out.println("- 200+条驾驶行为记录");
        System.out.println("- 3个用户账号");
        System.out.println("========================================");
    }
    
    private void clearAllData() {
        System.out.println("→ 清空旧数据...");
        drivingBehaviorRepository.deleteAllInBatch();
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
     * 初始化车辆分组数据 - 8个分组，包含多种类型
     */
    private void initVehicleGroups() {
        VehicleGroup group1 = new VehicleGroup();
        group1.setGroupName("华北区车队");
        group1.setDescription("负责华北区域的配送车辆，主要服务北京、天津地区，包含4辆配送车");
        group1.setStatus("active");
        group1.setCreatedTime(LocalDateTime.now().minusDays(90));
        vehicleGroupRepository.save(group1);
        savedGroups.add(group1);
        
        VehicleGroup group2 = new VehicleGroup();
        group2.setGroupName("华东区车队");
        group2.setDescription("负责华东区域的配送车辆，主要服务上海、南京地区，包含4辆配送车");
        group2.setStatus("active");
        group2.setCreatedTime(LocalDateTime.now().minusDays(60));
        vehicleGroupRepository.save(group2);
        savedGroups.add(group2);
        
        VehicleGroup group3 = new VehicleGroup();
        group3.setGroupName("华南区车队");
        group3.setDescription("负责华南区域的配送车辆，主要服务广州、深圳地区，包含4辆配送车");
        group3.setStatus("active");
        group3.setCreatedTime(LocalDateTime.now().minusDays(30));
        vehicleGroupRepository.save(group3);
        savedGroups.add(group3);
        
        VehicleGroup group4 = new VehicleGroup();
        group4.setGroupName("维修车辆组");
        group4.setDescription("待维修或保养的车辆，暂停运营，等待检修完成后恢复服务");
        group4.setStatus("inactive");
        group4.setCreatedTime(LocalDateTime.now().minusDays(15));
        vehicleGroupRepository.save(group4);
        savedGroups.add(group4);
        
        VehicleGroup group5 = new VehicleGroup();
        group5.setGroupName("VIP服务组");
        group5.setDescription("为VIP客户提供专属服务的车辆，优先调度，服务质量要求高");
        group5.setStatus("active");
        group5.setCreatedTime(LocalDateTime.now().minusDays(45));
        vehicleGroupRepository.save(group5);
        savedGroups.add(group5);
        
        VehicleGroup group6 = new VehicleGroup();
        group6.setGroupName("临时调度组");
        group6.setDescription("临时任务车辆组，用于应对突发需求或节假日高峰期调度");
        group6.setStatus("active");
        group6.setCreatedTime(LocalDateTime.now().minusDays(20));
        vehicleGroupRepository.save(group6);
        savedGroups.add(group6);
        
        VehicleGroup group7 = new VehicleGroup();
        group7.setGroupName("夜间配送组");
        group7.setDescription("负责夜间时段的配送任务，避开交通高峰期，提高配送效率");
        group7.setStatus("active");
        group7.setCreatedTime(LocalDateTime.now().minusDays(12));
        vehicleGroupRepository.save(group7);
        savedGroups.add(group7);
        
        VehicleGroup group8 = new VehicleGroup();
        group8.setGroupName("培训车辆组");
        group8.setDescription("用于新司机培训和考核的车辆，暂不参与实际配送任务");
        group8.setStatus("inactive");
        group8.setCreatedTime(LocalDateTime.now().minusDays(8));
        vehicleGroupRepository.save(group8);
        savedGroups.add(group8);
        
        System.out.println("→ 车辆分组数据初始化完成 (8个分组)");
    }
    
    /**
     * 初始化车辆位置数据 - 12辆车，分配到不同分组
     */
    private void initCarLocations() {
        saveCarLocation("京A10001", "华北区车队", 32.0650, 118.7500, 45.0, 90.0, "行驶中", "内环线A");
        saveCarLocation("京A10002", "华北区车队", 32.0550, 118.7800, 0.0, 180.0, "停车中", "内环线B");
        saveCarLocation("京B20001", "华北区车队", 32.0700, 118.7650, 52.0, 270.0, "行驶中", "外环线A");
        saveCarLocation("京B20002", "华北区车队", 32.0500, 118.7700, 38.0, 0.0, "行驶中", "外环线B");
        
        saveCarLocation("沪C30001", "华东区车队", 32.0600, 118.7550, 55.0, 45.0, "行驶中", "南北干线A");
        saveCarLocation("沪C30002", "华东区车队", 32.0680, 118.7750, 0.0, 225.0, "停车中", "南北干线B");
        saveCarLocation("沪D40001", "华东区车队", 32.0620, 118.7450, 68.0, 90.0, "超速", "东西干线A");
        saveCarLocation("沪D40002", "华东区车队", 32.0580, 118.7850, 35.0, 270.0, "行驶中", "东西干线B");
        
        saveCarLocation("粤E50001", "华南区车队", 32.0720, 118.7580, 40.0, 135.0, "行驶中", "配送区A");
        saveCarLocation("粤E50002", "华南区车队", 32.0480, 118.7720, 50.0, 315.0, "行驶中", "配送区B");
        saveCarLocation("苏F60001", "华南区车队", 32.0635, 118.7680, 0.0, 60.0, "停车中", "商业区巡游A");
        saveCarLocation("苏F60002", "华南区车队", 32.0570, 118.7620, 72.0, 240.0, "超速", "商业区巡游B");
        
        System.out.println("→ 车辆位置数据初始化完成 (12辆)");
    }
    
    private void saveCarLocation(String carName, String groupName, double lat, double lng, 
            double speed, double direction, String status, String routeName) {
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
     * 初始化地理围栏数据 - 8个围栏，多种类型
     */
    private void initGeoFences() {
        GeoFence fence1 = new GeoFence();
        fence1.setFenceName("市中心核心区");
        fence1.setCenterLatitude(32.0617);
        fence1.setCenterLongitude(118.7634);
        fence1.setRadius(1500.0);
        fence1.setFenceType("circle");
        fence1.setAlertType("exit");
        fence1.setStatus("active");
        fence1.setDescription("车辆离开市中心核心区时触发告警，用于监控核心区域车辆动态");
        fence1.setCreatedTime(LocalDateTime.now().minusDays(60));
        geoFenceRepository.save(fence1);
        savedFences.add(fence1);
        
        GeoFence fence2 = new GeoFence();
        fence2.setFenceName("配送站A");
        fence2.setCenterLatitude(32.0587);
        fence2.setCenterLongitude(118.7789);
        fence2.setRadius(300.0);
        fence2.setFenceType("circle");
        fence2.setAlertType("both");
        fence2.setStatus("active");
        fence2.setDescription("车辆进出配送站A时触发告警，用于调度管理");
        fence2.setCreatedTime(LocalDateTime.now().minusDays(45));
        geoFenceRepository.save(fence2);
        savedFences.add(fence2);
        
        GeoFence fence3 = new GeoFence();
        fence3.setFenceName("配送站B");
        fence3.setCenterLatitude(32.0695);
        fence3.setCenterLongitude(118.7520);
        fence3.setRadius(300.0);
        fence3.setFenceType("circle");
        fence3.setAlertType("both");
        fence3.setStatus("active");
        fence3.setDescription("车辆进出配送站B时触发告警，用于调度管理");
        fence3.setCreatedTime(LocalDateTime.now().minusDays(45));
        geoFenceRepository.save(fence3);
        savedFences.add(fence3);
        
        GeoFence fence4 = new GeoFence();
        fence4.setFenceName("商业步行街禁行区");
        fence4.setCenterLatitude(32.0550);
        fence4.setCenterLongitude(118.7700);
        fence4.setRadius(200.0);
        fence4.setFenceType("circle");
        fence4.setAlertType("entry");
        fence4.setStatus("active");
        fence4.setDescription("车辆进入步行街区域时触发告警，禁行区域监控");
        fence4.setCreatedTime(LocalDateTime.now().minusDays(30));
        geoFenceRepository.save(fence4);
        savedFences.add(fence4);
        
        GeoFence fence5 = new GeoFence();
        fence5.setFenceName("中心仓库");
        fence5.setCenterLatitude(32.0750);
        fence5.setCenterLongitude(118.7800);
        fence5.setRadius(500.0);
        fence5.setFenceType("circle");
        fence5.setAlertType("both");
        fence5.setStatus("active");
        fence5.setDescription("车辆进出中心仓库时触发告警，用于库存管理");
        fence5.setCreatedTime(LocalDateTime.now().minusDays(30));
        geoFenceRepository.save(fence5);
        savedFences.add(fence5);
        
        GeoFence fence6 = new GeoFence();
        fence6.setFenceName("学校区域限速区");
        fence6.setCenterLatitude(32.0670);
        fence6.setCenterLongitude(118.7580);
        fence6.setRadius(400.0);
        fence6.setFenceType("circle");
        fence6.setAlertType("entry");
        fence6.setStatus("active");
        fence6.setDescription("车辆进入学校区域时触发告警，限速30km/h");
        fence6.setCreatedTime(LocalDateTime.now().minusDays(20));
        geoFenceRepository.save(fence6);
        savedFences.add(fence6);
        
        GeoFence fence7 = new GeoFence();
        fence7.setFenceName("危险品仓库禁区");
        fence7.setCenterLatitude(32.0800);
        fence7.setCenterLongitude(118.7900);
        fence7.setRadius(600.0);
        fence7.setFenceType("circle");
        fence7.setAlertType("entry");
        fence7.setStatus("active");
        fence7.setDescription("普通车辆进入危险品仓库区域时触发告警");
        fence7.setCreatedTime(LocalDateTime.now().minusDays(15));
        geoFenceRepository.save(fence7);
        savedFences.add(fence7);
        
        GeoFence fence8 = new GeoFence();
        fence8.setFenceName("客户VIP区域");
        fence8.setCenterLatitude(32.0520);
        fence8.setCenterLongitude(118.7650);
        fence8.setRadius(250.0);
        fence8.setFenceType("circle");
        fence8.setAlertType("both");
        fence8.setStatus("active");
        fence8.setDescription("车辆进出VIP客户区域时触发告警，用于服务跟踪");
        fence8.setCreatedTime(LocalDateTime.now().minusDays(10));
        geoFenceRepository.save(fence8);
        savedFences.add(fence8);
        
        GeoFence fence9 = new GeoFence();
        fence9.setFenceName("机场接驳区");
        fence9.setCenterLatitude(32.0850);
        fence9.setCenterLongitude(118.8000);
        fence9.setRadius(800.0);
        fence9.setFenceType("circle");
        fence9.setAlertType("both");
        fence9.setStatus("active");
        fence9.setDescription("车辆进出机场接驳区域时触发告警，用于航班接驳调度");
        fence9.setCreatedTime(LocalDateTime.now().minusDays(8));
        geoFenceRepository.save(fence9);
        savedFences.add(fence9);
        
        GeoFence fence10 = new GeoFence();
        fence10.setFenceName("医院静音区");
        fence10.setCenterLatitude(32.0600);
        fence10.setCenterLongitude(118.7450);
        fence10.setRadius(350.0);
        fence10.setFenceType("circle");
        fence10.setAlertType("entry");
        fence10.setStatus("active");
        fence10.setDescription("车辆进入医院区域时触发告警，需保持静音、低速行驶");
        fence10.setCreatedTime(LocalDateTime.now().minusDays(5));
        geoFenceRepository.save(fence10);
        savedFences.add(fence10);
        
        GeoFence fence11 = new GeoFence();
        fence11.setFenceName("加油站服务区");
        fence11.setCenterLatitude(32.0780);
        fence11.setCenterLongitude(118.7550);
        fence11.setRadius(150.0);
        fence11.setFenceType("circle");
        fence11.setAlertType("both");
        fence11.setStatus("active");
        fence11.setDescription("车辆进出加油站区域时触发告警，用于油耗监控");
        fence11.setCreatedTime(LocalDateTime.now().minusDays(3));
        geoFenceRepository.save(fence11);
        savedFences.add(fence11);
        
        GeoFence fence12 = new GeoFence();
        fence12.setFenceName("政府机关禁行区");
        fence12.setCenterLatitude(32.0630);
        fence12.setCenterLongitude(118.7700);
        fence12.setRadius(180.0);
        fence12.setFenceType("circle");
        fence12.setAlertType("entry");
        fence12.setStatus("active");
        fence12.setDescription("车辆进入政府机关区域时触发告警，需提前报备");
        fence12.setCreatedTime(LocalDateTime.now().minusDays(2));
        geoFenceRepository.save(fence12);
        savedFences.add(fence12);
        
        System.out.println("→ 地理围栏数据初始化完成 (12个围栏)");
    }
    
    /**
     * 初始化围栏告警数据 - 80+条，包含多种状态和严重程度
     */
    private void initFenceAlerts() {
        String[] carNames = {"京A10001", "京A10002", "京B20001", "京B20002", 
                            "沪C30001", "沪C30002", "沪D40001", "沪D40002",
                            "粤E50001", "粤E50002", "苏F60001", "苏F60002"};
        
        String[] handlers = {"管理员", "调度员张三", "值班员李四", "监控员王五"};
        String[] severities = {"high", "medium", "low"};
        
        for (GeoFence fence : savedFences) {
            int alertCount = 8 + random.nextInt(5);
            for (int i = 0; i < alertCount; i++) {
                String carName = carNames[random.nextInt(carNames.length)];
                String alertType = fence.getAlertType().equals("both") 
                    ? (random.nextBoolean() ? "entry" : "exit")
                    : fence.getAlertType();
                
                boolean handled = random.nextDouble() < 0.60;
                int hoursAgo = random.nextInt(168);
                
                String severity;
                if (fence.getFenceName().contains("禁") || fence.getFenceName().contains("危险")) {
                    severity = random.nextDouble() < 0.5 ? "high" : "medium";
                } else if (fence.getFenceName().contains("学校")) {
                    severity = random.nextDouble() < 0.3 ? "high" : "medium";
                } else {
                    severity = severities[random.nextInt(severities.length)];
                }
                
                FenceAlert alert = new FenceAlert();
                alert.setGeoFence(fence);
                alert.setCarName(carName);
                alert.setLatitude(fence.getCenterLatitude() + (random.nextDouble() - 0.5) * 0.008);
                alert.setLongitude(fence.getCenterLongitude() + (random.nextDouble() - 0.5) * 0.008);
                alert.setAlertType(alertType);
                alert.setSeverity(severity);
                alert.setIsHandled(handled);
                alert.setCreatedTime(LocalDateTime.now().minusHours(hoursAgo));
                alert.setAlertTime(LocalDateTime.now().minusHours(hoursAgo));
                
                if (handled) {
                    alert.setHandler(handlers[random.nextInt(handlers.length)]);
                    alert.setHandleTime(alert.getCreatedTime().plusMinutes(5 + random.nextInt(55)));
                }
                fenceAlertRepository.save(alert);
            }
        }
        
        System.out.println("→ 围栏告警数据初始化完成 (80+条)");
    }
    
    /**
     * 初始化驾驶统计数据 - 最近30天，每辆车每天一条
     */
    private void initDrivingStatistics() {
        String[] carNames = {"京A10001", "京A10002", "京B20001", "京B20002", 
                            "沪C30001", "沪C30002", "沪D40001", "沪D40002",
                            "粤E50001", "粤E50002", "苏F60001", "苏F60002"};
        
        for (String carName : carNames) {
            for (int day = 0; day < 30; day++) {
                LocalDate date = LocalDate.now().minusDays(day);
                
                double mileage = 50 + random.nextDouble() * 200;
                int duration = (int)(mileage / (30 + random.nextDouble() * 30) * 60);
                double avgSpeed = mileage / (duration / 60.0);
                double maxSpeed = avgSpeed + 20 + random.nextDouble() * 30;
                
                DrivingStatistics stats = new DrivingStatistics();
                stats.setCarName(carName);
                stats.setRecordDate(date);
                stats.setTotalMileage(Math.round(mileage * 100) / 100.0);
                stats.setDrivingTimeMinutes(duration);
                stats.setAverageSpeed(Math.round(avgSpeed * 10) / 10.0);
                stats.setMaxSpeed(Math.round(maxSpeed * 10) / 10.0);
                drivingStatisticsRepository.save(stats);
            }
        }
        
        System.out.println("→ 驾驶统计数据初始化完成 (12车×30天=360条)");
    }
    
    /**
     * 初始化车辆轨迹数据 - 每辆车300+个轨迹点
     * 覆盖最近24小时，支持完整的轨迹回放演示
     */
    private void initVehicleTracks() {
        String[] carNames = {"京A10001", "京A10002", "京B20001", "京B20002", 
                            "沪C30001", "沪C30002", "沪D40001", "沪D40002",
                            "粤E50001", "粤E50002", "苏F60001", "苏F60002"};
        
        double[][][] routes = {
            {{32.0650, 118.7500}, {32.0680, 118.7550}, {32.0700, 118.7620},
             {32.0690, 118.7700}, {32.0660, 118.7780}, {32.0600, 118.7830},
             {32.0540, 118.7820}, {32.0500, 118.7760}, {32.0480, 118.7680},
             {32.0500, 118.7600}, {32.0550, 118.7540}, {32.0600, 118.7500},
             {32.0650, 118.7500}},
            
            {{32.0550, 118.7800}, {32.0500, 118.7760}, {32.0480, 118.7680},
             {32.0500, 118.7600}, {32.0550, 118.7540}, {32.0600, 118.7500},
             {32.0650, 118.7500}, {32.0680, 118.7550}, {32.0700, 118.7620},
             {32.0690, 118.7700}, {32.0660, 118.7780}, {32.0600, 118.7830},
             {32.0550, 118.7800}},
            
            {{32.0700, 118.7650}, {32.0720, 118.7580}, {32.0710, 118.7500},
             {32.0650, 118.7450}, {32.0550, 118.7440}, {32.0480, 118.7480},
             {32.0440, 118.7580}, {32.0450, 118.7700}, {32.0500, 118.7820},
             {32.0580, 118.7880}, {32.0680, 118.7850}, {32.0720, 118.7750},
             {32.0700, 118.7650}},
            
            {{32.0450, 118.7700}, {32.0500, 118.7820}, {32.0580, 118.7880},
             {32.0680, 118.7850}, {32.0720, 118.7750}, {32.0700, 118.7650},
             {32.0720, 118.7580}, {32.0710, 118.7500}, {32.0650, 118.7450},
             {32.0550, 118.7440}, {32.0480, 118.7480}, {32.0440, 118.7580},
             {32.0450, 118.7700}},
            
            {{32.0450, 118.7550}, {32.0500, 118.7550}, {32.0550, 118.7550},
             {32.0600, 118.7550}, {32.0650, 118.7550}, {32.0700, 118.7550},
             {32.0750, 118.7550}, {32.0750, 118.7600}, {32.0720, 118.7680},
             {32.0680, 118.7750}, {32.0650, 118.7780}, {32.0620, 118.7800},
             {32.0580, 118.7780}, {32.0520, 118.7720}, {32.0480, 118.7650},
             {32.0450, 118.7550}},
            
            {{32.0750, 118.7750}, {32.0700, 118.7750}, {32.0650, 118.7750},
             {32.0600, 118.7750}, {32.0550, 118.7750}, {32.0500, 118.7750},
             {32.0450, 118.7750}, {32.0450, 118.7700}, {32.0480, 118.7620},
             {32.0520, 118.7550}, {32.0550, 118.7500}, {32.0580, 118.7480},
             {32.0620, 118.7500}, {32.0680, 118.7550}, {32.0720, 118.7620},
             {32.0750, 118.7750}},
            
            {{32.0620, 118.7400}, {32.0620, 118.7450}, {32.0620, 118.7500},
             {32.0620, 118.7550}, {32.0620, 118.7600}, {32.0620, 118.7650},
             {32.0620, 118.7700}, {32.0620, 118.7750}, {32.0620, 118.7800},
             {32.0620, 118.7850}, {32.0650, 118.7880}, {32.0680, 118.7870},
             {32.0700, 118.7850}, {32.0720, 118.7820}, {32.0720, 118.7780},
             {32.0700, 118.7720}, {32.0680, 118.7650}, {32.0650, 118.7580},
             {32.0620, 118.7500}, {32.0620, 118.7400}},
            
            {{32.0580, 118.7850}, {32.0580, 118.7800}, {32.0580, 118.7750},
             {32.0580, 118.7700}, {32.0580, 118.7650}, {32.0580, 118.7600},
             {32.0580, 118.7550}, {32.0580, 118.7500}, {32.0580, 118.7450},
             {32.0550, 118.7420}, {32.0520, 118.7430}, {32.0500, 118.7460},
             {32.0480, 118.7500}, {32.0500, 118.7550}, {32.0520, 118.7600},
             {32.0550, 118.7650}, {32.0580, 118.7700}, {32.0580, 118.7780},
             {32.0580, 118.7850}},
            
            {{32.0720, 118.7580}, {32.0720, 118.7620}, {32.0720, 118.7660},
             {32.0680, 118.7660}, {32.0640, 118.7660}, {32.0640, 118.7620},
             {32.0640, 118.7580}, {32.0680, 118.7580}, {32.0720, 118.7580}},
            
            {{32.0480, 118.7720}, {32.0480, 118.7680}, {32.0480, 118.7640},
             {32.0520, 118.7640}, {32.0560, 118.7640}, {32.0560, 118.7680},
             {32.0560, 118.7720}, {32.0520, 118.7720}, {32.0480, 118.7720}},
            
            {{32.0635, 118.7680}, {32.0650, 118.7720}, {32.0680, 118.7700},
             {32.0670, 118.7660}, {32.0650, 118.7620}, {32.0620, 118.7600},
             {32.0590, 118.7620}, {32.0600, 118.7660}, {32.0610, 118.7700},
             {32.0635, 118.7680}},
            
            {{32.0570, 118.7620}, {32.0550, 118.7580}, {32.0520, 118.7560},
             {32.0500, 118.7600}, {32.0510, 118.7640}, {32.0530, 118.7680},
             {32.0560, 118.7660}, {32.0580, 118.7640}, {32.0570, 118.7620}}
        };
        
        for (int i = 0; i < carNames.length; i++) {
            String carName = carNames[i];
            double[][] route = routes[i];
            
            LocalDateTime baseTime = LocalDateTime.now().minusHours(24);
            double currentLat = route[0][0];
            double currentLng = route[0][1];
            int routeIndex = 0;
            double currentDirection = 0;
            double currentSpeed = 40 + random.nextDouble() * 20;
            
            int totalPoints = 300 + random.nextInt(50);
            
            for (int j = 0; j < totalPoints; j++) {
                if (j % 20 == 0 && routeIndex < route.length - 1) {
                    routeIndex++;
                }
                
                double targetLat = route[Math.min(routeIndex, route.length - 1)][0];
                double targetLng = route[Math.min(routeIndex, route.length - 1)][1];
                
                currentLat = currentLat + (targetLat - currentLat) * 0.08 + (random.nextDouble() - 0.5) * 0.0003;
                currentLng = currentLng + (targetLng - currentLng) * 0.08 + (random.nextDouble() - 0.5) * 0.0003;
                
                currentDirection = Math.toDegrees(Math.atan2(
                    targetLat - currentLat, targetLng - currentLng));
                if (currentDirection < 0) currentDirection += 360;
                
                if (random.nextDouble() < 0.08) {
                    currentSpeed = Math.max(0, currentSpeed - 25 - random.nextDouble() * 15);
                } else if (random.nextDouble() < 0.12) {
                    currentSpeed = Math.min(80, currentSpeed + 15 + random.nextDouble() * 20);
                } else {
                    currentSpeed = Math.max(15, Math.min(75, currentSpeed + (random.nextDouble() - 0.5) * 12));
                }
                
                String status = "行驶中";
                if (currentSpeed < 5) {
                    status = "停车中";
                } else if (currentSpeed > 60) {
                    status = "超速";
                }
                
                VehicleTrack track = new VehicleTrack();
                track.setCarName(carName);
                track.setLatitude(currentLat);
                track.setLongitude(currentLng);
                track.setSpeed(Math.round(currentSpeed * 10) / 10.0);
                track.setDirection((double) Math.round(currentDirection));
                track.setRecordTime(baseTime.plusSeconds(j * (86400 / totalPoints)));
                track.setStatus(status);
                vehicleTrackRepository.save(track);
            }
        }
        
        System.out.println("→ 车辆轨迹数据初始化完成 (12车×300+点=3600+条)");
    }
    
    /**
     * 初始化驾驶行为数据 - 200+条，覆盖所有车辆和行为类型
     */
    private void initDrivingBehaviors() {
        String[] carNames = {"京A10001", "京A10002", "京B20001", "京B20002", 
                            "沪C30001", "沪C30002", "沪D40001", "沪D40002",
                            "粤E50001", "粤E50002", "苏F60001", "苏F60002"};
        
        String[] behaviorTypes = {"RAPID_ACCEL", "RAPID_BRAKE", "OVERSPEED", "SHARP_TURN", "FATIGUE", "IDLE_LONG"};
        String[] riskLevels = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        
        for (String carName : carNames) {
            int behaviorCount = 15 + random.nextInt(10);
            
            for (int i = 0; i < behaviorCount; i++) {
                DrivingBehavior behavior = new DrivingBehavior();
                behavior.setCarName(carName);
                
                String behaviorType = behaviorTypes[random.nextInt(behaviorTypes.length)];
                behavior.setBehaviorType(behaviorType);
                
                String riskLevel;
                double speed = 30 + random.nextDouble() * 50;
                double acceleration = -8 + random.nextDouble() * 16;
                
                switch (behaviorType) {
                    case "OVERSPEED":
                        speed = 65 + random.nextDouble() * 25;
                        riskLevel = speed > 80 ? "CRITICAL" : (speed > 70 ? "HIGH" : "MEDIUM");
                        break;
                    case "RAPID_BRAKE":
                        acceleration = -6 - random.nextDouble() * 4;
                        riskLevel = Math.abs(acceleration) > 8 ? "HIGH" : "MEDIUM";
                        break;
                    case "RAPID_ACCEL":
                        acceleration = 4 + random.nextDouble() * 4;
                        riskLevel = acceleration > 6 ? "MEDIUM" : "LOW";
                        break;
                    case "FATIGUE":
                        riskLevel = random.nextDouble() < 0.3 ? "HIGH" : "MEDIUM";
                        break;
                    case "IDLE_LONG":
                        riskLevel = "LOW";
                        break;
                    case "SHARP_TURN":
                        speed = 45 + random.nextDouble() * 20;
                        riskLevel = speed > 55 ? "HIGH" : "MEDIUM";
                        break;
                    default:
                        riskLevel = riskLevels[random.nextInt(3)];
                }
                
                behavior.setRiskLevel(riskLevel);
                behavior.setLatitude(CENTER_LAT + (random.nextDouble() - 0.5) * 0.04);
                behavior.setLongitude(CENTER_LNG + (random.nextDouble() - 0.5) * 0.04);
                behavior.setSpeed(speed);
                behavior.setAcceleration(acceleration);
                behavior.setEventTime(LocalDateTime.now().minusHours(random.nextInt(72)));
                behavior.setIsHandled(random.nextDouble() < 0.55);
                
                String description = switch (behaviorType) {
                    case "RAPID_ACCEL" -> String.format("急加速：加速度 %.1f m/s²，建议平稳加速", Math.abs(acceleration));
                    case "RAPID_BRAKE" -> String.format("急刹车：减速度 %.1f m/s²，请注意行车安全", Math.abs(acceleration));
                    case "OVERSPEED" -> String.format("超速行驶：当前速度 %.1f km/h，限速 60 km/h，请减速", speed);
                    case "SHARP_TURN" -> String.format("急转弯：转弯速度 %.1f km/h，建议降低车速", speed);
                    case "FATIGUE" -> "疲劳驾驶：连续驾驶超过4小时，建议休息";
                    case "IDLE_LONG" -> "长时间怠速：怠速超过10分钟，建议熄火等待";
                    default -> "未知驾驶行为";
                };
                behavior.setDescription(description);
                
                drivingBehaviorRepository.save(behavior);
            }
        }
        
        System.out.println("→ 驾驶行为数据初始化完成 (200+条)");
    }
    
    /**
     * 初始化用户数据
     */
    private void initUsers() {
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@fleet.com");
        admin.setRealName("系统管理员");
        admin.setPhone("13800138000");
        admin.setRole("ADMIN");
        admin.setActive(true);
        admin.setCreatedTime(LocalDateTime.now().minusDays(365));
        userRepository.save(admin);
        
        UserEntity operator = new UserEntity();
        operator.setUsername("operator");
        operator.setPassword(passwordEncoder.encode("operator123"));
        operator.setEmail("operator@fleet.com");
        operator.setRealName("调度员张三");
        operator.setPhone("13800138001");
        operator.setRole("OPERATOR");
        operator.setActive(true);
        operator.setCreatedTime(LocalDateTime.now().minusDays(180));
        userRepository.save(operator);
        
        UserEntity viewer = new UserEntity();
        viewer.setUsername("viewer");
        viewer.setPassword(passwordEncoder.encode("viewer123"));
        viewer.setEmail("viewer@fleet.com");
        viewer.setRealName("观察员李四");
        viewer.setPhone("13800138002");
        viewer.setRole("VIEWER");
        viewer.setActive(true);
        viewer.setCreatedTime(LocalDateTime.now().minusDays(90));
        userRepository.save(viewer);
        
        System.out.println("→ 用户数据初始化完成");
        System.out.println("  账号: admin / operator / viewer");
        System.out.println("  密码: admin123 / operator123 / viewer123");
    }
}
