package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.GeoFence;
import java.util.List;

@Repository
public interface GeoFenceRepository extends JpaRepository<GeoFence, Long> {
    
    List<GeoFence> findByStatus(String status);
    
    List<GeoFence> findByFenceType(String fenceType);
    
    @Query("SELECT g FROM GeoFence g WHERE g.centerLatitude BETWEEN ?1 AND ?2 AND g.centerLongitude BETWEEN ?3 AND ?4")
    List<GeoFence> findByArea(Double minLat, Double maxLat, Double minLng, Double maxLng);
}