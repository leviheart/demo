package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.FenceAlert;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FenceAlertRepository extends JpaRepository<FenceAlert, Long> {
    
    List<FenceAlert> findByIsHandled(Boolean isHandled);
    
    List<FenceAlert> findByCarNameOrderByCreatedTimeDesc(String carName);
    
    @Query("SELECT f FROM FenceAlert f WHERE f.createdTime BETWEEN :startTime AND :endTime ORDER BY f.createdTime DESC")
    List<FenceAlert> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(f) FROM FenceAlert f WHERE f.isHandled = false")
    Long countUnhandledAlerts();
}