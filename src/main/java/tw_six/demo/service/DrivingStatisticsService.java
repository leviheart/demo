package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.DrivingStatistics;
import tw_six.demo.repository.DrivingStatisticsRepository;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DrivingStatisticsService {
    
    private final DrivingStatisticsRepository drivingStatisticsRepository;
    
    @Autowired
    public DrivingStatisticsService(DrivingStatisticsRepository drivingStatisticsRepository) {
        this.drivingStatisticsRepository = drivingStatisticsRepository;
    }
    
    public DrivingStatistics saveStatistics(DrivingStatistics statistics) {
        return drivingStatisticsRepository.save(statistics);
    }
    
    public List<DrivingStatistics> getStatisticsByCar(String carName) {
        return drivingStatisticsRepository.findByCarNameOrderByRecordDateDesc(carName);
    }
    
    public List<DrivingStatistics> getStatisticsByDateRange(String carName, LocalDate startDate, LocalDate endDate) {
        return drivingStatisticsRepository.findByCarNameAndRecordDateBetweenOrderByRecordDateDesc(carName, startDate, endDate);
    }
    
    public List<DrivingStatistics> getDailyRanking(LocalDate date) {
        return drivingStatisticsRepository.findByRecordDateOrderByMileageDesc(date);
    }
    
    public Double getAverageMileage(String carName) {
        return drivingStatisticsRepository.findAverageMileageByCarName(carName);
    }
    
    public Double getTotalMileage(String carName) {
        return drivingStatisticsRepository.findTotalMileageByCarName(carName);
    }
    
    public DrivingStatistics getLatestStatistics(String carName) {
        List<DrivingStatistics> stats = getStatisticsByCar(carName);
        return stats.isEmpty() ? null : stats.get(0);
    }
}