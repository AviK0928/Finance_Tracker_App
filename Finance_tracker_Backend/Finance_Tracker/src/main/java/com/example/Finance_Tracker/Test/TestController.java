package com.example.Finance_Tracker.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/test","/api/test/"})
public class TestController {

    @GetMapping
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "Anonymous";
        return ResponseEntity.ok("Hello, " + username);
    }
}
