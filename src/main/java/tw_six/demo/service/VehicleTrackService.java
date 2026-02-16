package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.repository.VehicleTrackRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VehicleTrackService {
    
    private final VehicleTrackRepository vehicleTrackRepository;
    
    @Autowired
    public VehicleTrackService(VehicleTrackRepository vehicleTrackRepository) {
        this.vehicleTrackRepository = vehicleTrackRepository;
    }
    
    public VehicleTrack saveTrack(VehicleTrack track) {
        return vehicleTrackRepository.save(track);
    }
    
    public List<VehicleTrack> getTracksByCarName(String carName) {
        return vehicleTrackRepository.findByCarNameOrderByRecordTimeAsc(carName);
    }
    
    public List<VehicleTrack> getTracksByTimeRange(String carName, LocalDateTime startTime, LocalDateTime endTime) {
        return vehicleTrackRepository.findByCarNameAndRecordTimeBetweenOrderByRecordTimeAsc(carName, startTime, endTime);
    }
    
    public List<String> getAllCarNames() {
        return vehicleTrackRepository.findDistinctCarNames();
    }
    
    public List<VehicleTrack> getRecentTracks(String carName, int minutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        return vehicleTrackRepository.findRecentTracksByCarName(carName, startTime);
    }
}