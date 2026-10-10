package com.streetlight.controller;

import com.streetlight.entity.User;
import com.streetlight.service.Application;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.*;

import jakarta.servlet.http.*;

@Controller
public class AuthController {

    @Autowired
    private Application app;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Bad credentials. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginProcess(HttpServletRequest request) {
        // Spring Security handles form login automatically
        // This endpoint is just the action target for the form
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    @Transactional
    public String register(@ModelAttribute User user, Model model) {
        // Check if username already exists
        if (app.userRepository.countByUsername(user.getUsername()) > 0) {
            model.addAttribute("errorMessage", "Username already taken. Please choose another.");
            return "auth/register";
        }
        
        // BCrypt password encoding
        String encodedPassword = app.passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(Role.CITIZEN);
        
        app.userRepository.save(user);
        
        // Auto-login after registration
        // Spring Security will handle this
        
        return "redirect:/";
    }
}