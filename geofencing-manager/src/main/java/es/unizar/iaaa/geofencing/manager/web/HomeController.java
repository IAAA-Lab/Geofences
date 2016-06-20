package es.unizar.iaaa.geofencing.manager.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String login() {
        return "login";
    }

    @RequestMapping("/sign-up")
    public String register() {
        return "sign-up";
    }

    @RequestMapping("/user")
    public String user() {
        return "user";
    }

    @RequestMapping("/geofences")
    public String geofences() {
        return "geofences";
    }

    @RequestMapping("/rules")
    public String rules() {
        return "rules";
    }

}
