package tw_six.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "geo_fences")
public class GeoFence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fence_name")
    private String fenceName;
    
    @Column(name = "center_latitude")
    private Double centerLatitude;
    
    @Column(name = "center_longitude")
    private Double centerLongitude;
    
    private Double radius;
    
    @Column(name = "fence_type")
    private String fenceType;
    
    @Column(name = "alert_type")
    private String alertType;
    
    private String status;
    private String description;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}