package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.VehicleTrack;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleTrackRepository extends JpaRepository<VehicleTrack, Long> {
    
    List<VehicleTrack> findByCarNameOrderByRecordTimeAsc(String carName);
    
    List<VehicleTrack> findByCarNameAndRecordTimeBetweenOrderByRecordTimeAsc(
        String carName, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    @Query("SELECT DISTINCT vt.carName FROM VehicleTrack vt")
    List<String> findDistinctCarNames();
    
    @Query("SELECT vt FROM VehicleTrack vt WHERE vt.carName = :carName AND vt.recordTime >= :time ORDER BY vt.recordTime ASC")
    List<VehicleTrack> findRecentTracksByCarName(@Param("carName") String carName, @Param("time") LocalDateTime time);
}