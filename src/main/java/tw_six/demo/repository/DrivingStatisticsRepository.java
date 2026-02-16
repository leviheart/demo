package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.DrivingStatistics;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DrivingStatisticsRepository extends JpaRepository<DrivingStatistics, Long> {
    
    List<DrivingStatistics> findByCarNameOrderByRecordDateDesc(String carName);
    
    List<DrivingStatistics> findByCarNameAndRecordDateBetweenOrderByRecordDateDesc(
        String carName, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ds FROM DrivingStatistics ds WHERE ds.recordDate = ?1 ORDER BY ds.totalMileage DESC")
    List<DrivingStatistics> findByRecordDateOrderByMileageDesc(LocalDate recordDate);
    
    @Query("SELECT AVG(ds.totalMileage) FROM DrivingStatistics ds WHERE ds.carName = ?1")
    Double findAverageMileageByCarName(String carName);
    
    @Query("SELECT SUM(ds.totalMileage) FROM DrivingStatistics ds WHERE ds.carName = ?1")
    Double findTotalMileageByCarName(String carName);
}