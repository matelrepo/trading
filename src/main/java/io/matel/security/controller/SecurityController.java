package io.matel.security.controller;


import io.matel.security.config.CustomHttpResponse;
import io.matel.security.config.User;
import io.matel.security.config.UserRepository;
import io.matel.security.config.UserView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
public class SecurityController {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    public SecurityController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody UserView model) {
        System.out.println(model.getUsername());
        System.out.println(this.userRepository.findByUsername(model.getUsername()));
        if (this.userRepository.findByUsername(model.getUsername()) == null) {
            User user = new User(model.getUsername(), passwordEncoder.encode(model.getPassword()),"STUDENT","");
            this.userRepository.save(user);
            return new ResponseEntity(new CustomHttpResponse("Registration Succesfull!", HttpStatus.OK.value()), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User already exists");        }
    }
}
