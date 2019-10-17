package io.matel.common.controller;


import io.matel.common.repository.UserRepository;
import io.matel.common.model.UserView;
import io.matel.common.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/public")
@CrossOrigin
public class SecurityController {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    public SecurityController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Available to all authenticated users
    @GetMapping("test")
    public String test1() {
        return "API Test";
    }

    // Available to managers
    @GetMapping("management/reports")
    public String reports() {
        return "Some report data";
    }

    // Available to ROLE_ADMIN
    @GetMapping("admin/users")
    public List<User> users() {
        return this.userRepository.findAll();
    }

    @PostMapping("register")
    public ResponseEntity register(@RequestBody UserView model) {
        System.out.println(model.getUsername());
        if (this.userRepository.findByUsername(model.getUsername()) == null) {
            User user = new User(model.getUsername(), passwordEncoder.encode(model.getPassword()),"USER","READ");
            this.userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("Registration successful");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Username already exists!");
        }
    }
}