package com.streetlight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

@SpringBootApplication
public class Application implements ApplicationListener<ContextRefreshedEvent> {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final com.streetlight.repository.UserRepository userRepository;

    public Application(UserDetailsService userDetailsService,
                     PasswordEncoder passwordEncoder,
                     com.streetlight.repository.UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Bootstrap: create default admin account if no users exist
        if (userRepository.count() == 0) {
            var admin = new com.streetlight.entity.User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(com.streetlight.entity.enums.Role.ADMIN);
            userRepository.save(admin);
        }
    }
}