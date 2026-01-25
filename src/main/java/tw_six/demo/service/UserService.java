package tw_six.demo.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import tw_six.demo.entity.User;
import tw_six.demo.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @NonNull
    public User saveUser(@NonNull User user) {
        return userRepository.save(user);
    }
}