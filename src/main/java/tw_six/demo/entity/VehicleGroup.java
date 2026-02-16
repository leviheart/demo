package tw_six.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle_groups")
public class VehicleGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "group_name")
    private String groupName;
    
    private String description;
    private String status;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @OneToMany(mappedBy = "vehicleGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarLocation> carLocations;
}