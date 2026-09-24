package com.example.knowledge;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nageshasriramappa
 **/
@RestController
public class PingController {

    @GetMapping("/api/ping")
    public String ping() {
        return "Knowledge Assistant is running";
    }
}
