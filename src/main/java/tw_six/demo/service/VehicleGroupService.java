package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.VehicleGroup;
import tw_six.demo.repository.VehicleGroupRepository;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehicleGroupService {
    
    private final VehicleGroupRepository vehicleGroupRepository;
    
    @Autowired
    public VehicleGroupService(VehicleGroupRepository vehicleGroupRepository) {
        this.vehicleGroupRepository = vehicleGroupRepository;
    }
    
    public VehicleGroup createGroup(VehicleGroup group) {
        if (vehicleGroupRepository.existsByGroupName(group.getGroupName())) {
            throw new IllegalArgumentException("分组名称已存在: " + group.getGroupName());
        }
        return vehicleGroupRepository.save(group);
    }
    
    public VehicleGroup updateGroup(Long id, VehicleGroup groupDetails) {
        VehicleGroup group = vehicleGroupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("分组不存在: " + id));
        
        group.setGroupName(groupDetails.getGroupName());
        group.setDescription(groupDetails.getDescription());
        group.setStatus(groupDetails.getStatus());
        
        return vehicleGroupRepository.save(group);
    }
    
    public void deleteGroup(Long id) {
        if (!vehicleGroupRepository.existsById(id)) {
            throw new RuntimeException("分组不存在: " + id);
        }
        vehicleGroupRepository.deleteById(id);
    }
    
    public List<VehicleGroup> getAllGroups() {
        return vehicleGroupRepository.findAll();
    }
    
    public List<VehicleGroup> getActiveGroups() {
        return vehicleGroupRepository.findByStatus("active");
    }
    
    public Optional<VehicleGroup> getGroupById(Long id) {
        return vehicleGroupRepository.findById(id);
    }
    
    public Optional<VehicleGroup> getGroupByName(String groupName) {
        return vehicleGroupRepository.findByGroupName(groupName);
    }
}