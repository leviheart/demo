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
@Table(name = "fence_alerts")
public class FenceAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "fence_id")
    private GeoFence geoFence;
    
    @Column(name = "car_name")
    private String carName;
    
    private Double latitude;
    private Double longitude;
    
    @Column(name = "alert_type")
    private String alertType;
    
    @Column(name = "is_handled")
    private Boolean isHandled;
    
    @Column(name = "handle_time")
    private LocalDateTime handleTime;
    
    private String handler;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}