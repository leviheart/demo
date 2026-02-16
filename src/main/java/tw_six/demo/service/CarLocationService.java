package tw_six.demo.service; // 定义汽车位置业务服务类所在的包

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.CarLocation;
import tw_six.demo.repository.CarLocationRepository;
import java.util.List;
import java.util.Optional;

/**
 * 汽车位置业务服务类 - 地图导航系统业务逻辑层组件
 * 
 * 文件关联说明：
 * 1. 与CarLocationRepository关联：通过构造函数注入获得车辆位置数据访问能力
 * 2. 与CarLocation实体类关联：处理车辆位置对象的业务逻辑
 * 3. 与MapController关联：通过依赖注入为地图控制层提供车辆位置业务服务
 * 4. 与Spring容器关联：通过@Service注解注册为Spring Bean
 * 
 * 作用说明：
 * - 封装车辆位置相关的业务逻辑处理
 * - 实现车辆位置的实时更新和状态管理
 * - 提供标准化的车辆位置业务接口
 * - 支持地图导航系统的核心位置服务功能
 */
@Service // 标记为Spring业务服务组件，纳入Spring容器管理
@Transactional
public class CarLocationService { // 汽车位置业务服务类定义
    
    // 使用final修饰确保依赖不可变性，通过构造函数注入
    private final CarLocationRepository carLocationRepository;
    private final GeoFenceService geoFenceService;
    
    @Autowired
    public CarLocationService(CarLocationRepository carLocationRepository, GeoFenceService geoFenceService) {
        this.carLocationRepository = carLocationRepository;
        this.geoFenceService = geoFenceService;
    }
    
    public CarLocation saveLocation(CarLocation location) {
        CarLocation savedLocation = carLocationRepository.save(location);
        // 检查地理围栏违规
        geoFenceService.checkFenceViolation(location.getCarName(), location.getLatitude(), location.getLongitude());
        return savedLocation;
    }
    
    public List<CarLocation> getAllLocations() {
        return carLocationRepository.findAll();
    }
    
    public List<CarLocation> getActiveLocations() {
        return carLocationRepository.findByStatus("active");
    }
    
    public Optional<CarLocation> getLocationById(Long id) {
        return carLocationRepository.findById(id);
    }
    
    public List<CarLocation> getLocationsByCarName(String carName) {
        return carLocationRepository.findByCarName(carName);
    }
}