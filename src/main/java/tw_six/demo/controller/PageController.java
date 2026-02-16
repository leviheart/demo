package tw_six.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/groups")
    public String groupsPage() {
        return "groups";
    }
    
    @GetMapping("/alarms")
    public String alarmsPage() {
        return "alarms";
    }
    
    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }
    
    @GetMapping("/geofences")
    public String geofencesPage() {
        return "geofences";
    }
}