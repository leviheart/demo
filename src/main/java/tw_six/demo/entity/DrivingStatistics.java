package tw_six.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driving_statistics")
public class DrivingStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "car_name", nullable = false)
    private String carName;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Column(name = "total_mileage")
    private Double totalMileage;
    
    @Column(name = "average_speed")
    private Double averageSpeed;
    
    @Column(name = "max_speed")
    private Double maxSpeed;
    
    @Column(name = "driving_time_minutes")
    private Integer drivingTimeMinutes;
    
    @Column(name = "fuel_consumption")
    private Double fuelConsumption;
    
    @Column(name = "harsh_acceleration_count")
    private Integer harshAccelerationCount;
    
    @Column(name = "harsh_braking_count")
    private Integer harshBrakingCount;
    
    @PrePersist
    protected void onCreate() {
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
    }
}