package tw_six.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import tw_six.demo.entity.User;
import tw_six.demo.service.UserService;

import java.util.List;

@RestController
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
}