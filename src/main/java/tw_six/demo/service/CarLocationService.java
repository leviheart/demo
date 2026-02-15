package tw_six.demo.service; // 定义汽车位置业务服务类所在的包

import lombok.RequiredArgsConstructor; // Lombok注解，生成必要的构造函数
import org.springframework.stereotype.Service; // Spring注解，标记为业务服务组件
import tw_six.demo.entity.CarLocation; // 引入汽车位置实体类
import tw_six.demo.repository.CarLocationRepository; // 引入汽车位置数据访问接口
import java.util.List; // Java集合框架，用于返回列表数据

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
@RequiredArgsConstructor // Lombok注解，为final字段生成构造函数，支持构造函数注入
public class CarLocationService { // 汽车位置业务服务类定义
    
    // 使用final修饰确保依赖不可变性，通过构造函数注入
    private final CarLocationRepository carLocationRepository; // 车辆位置数据访问接口依赖
    
    /**
     * 获取所有车辆位置信息
     * 
     * @return 车辆位置列表，包含所有车辆的当前位置信息
     */
    public List<CarLocation> getAllLocations() { // 业务方法：获取全部车辆位置
        return carLocationRepository.findAll(); // 调用数据访问层方法获取所有车辆位置
    }
    
    /**
     * 保存或更新车辆位置信息
     * 
     * @param location 车辆位置对象
     * @return 保存后的车辆位置对象
     */
    public CarLocation saveLocation(CarLocation location) { // 业务方法：保存车辆位置
        return carLocationRepository.save(location); // 调用数据访问层方法保存车辆位置
    }
    
    /**
     * 获取运行中的车辆列表
     * 
     * @return 运行中车辆位置列表
     */
    public List<CarLocation> getActiveCars() { // 业务方法：获取运行中车辆
        return carLocationRepository.findByStatus("active"); // 调用自定义查询方法获取活跃车辆
    }
}