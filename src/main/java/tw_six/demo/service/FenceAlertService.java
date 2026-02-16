package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.repository.FenceAlertRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FenceAlertService {
    
    @Autowired
    private FenceAlertRepository fenceAlertRepository;
    
    public List<FenceAlert> getAllAlerts() {
        return fenceAlertRepository.findAll();
    }
    
    public List<FenceAlert> getUnhandledAlerts() {
        return fenceAlertRepository.findByIsHandled(false);
    }
    
    public List<FenceAlert> getAlertsByCar(String carName) {
        return fenceAlertRepository.findByCarNameOrderByCreatedTimeDesc(carName);
    }
    
    public List<FenceAlert> getAlertsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return fenceAlertRepository.findByTimeRange(startTime, endTime);
    }
    
    public FenceAlert createAlert(FenceAlert alert) {
        alert.setCreatedTime(LocalDateTime.now());
        return fenceAlertRepository.save(alert);
    }
    
    public FenceAlert handleAlert(Long id, String handler) {
        FenceAlert alert = fenceAlertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setIsHandled(true);
            alert.setHandler(handler);
            alert.setHandleTime(LocalDateTime.now());
            return fenceAlertRepository.save(alert);
        }
        return null;
    }
    
    public void deleteAlert(Long id) {
        fenceAlertRepository.deleteById(id);
    }
    
    public Long getUnhandledAlertCount() {
        return fenceAlertRepository.countUnhandledAlerts();
    }
}