package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.VehicleGroup;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleGroupRepository extends JpaRepository<VehicleGroup, Long> {
    
    List<VehicleGroup> findByStatus(String status);
    
    Optional<VehicleGroup> findByGroupName(String groupName);
    
    boolean existsByGroupName(String groupName);
}