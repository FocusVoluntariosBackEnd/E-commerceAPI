package com.ecommerce.ecommerce_api.auth.controller;


import com.ecommerce.ecommerce_api.auth.service.AuthService;
import com.ecommerce.ecommerce_api.user.dto.LoginRequest;
import com.ecommerce.ecommerce_api.user.dto.LoginResponse;
import com.ecommerce.ecommerce_api.user.dto.RegisterRequestDTO;
import com.ecommerce.ecommerce_api.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid RegisterRequestDTO request){

        UserResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

}
