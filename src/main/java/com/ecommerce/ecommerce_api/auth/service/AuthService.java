package com.ecommerce.ecommerce_api.auth.service;

import com.ecommerce.ecommerce_api.exception.EmailAlreadyExistsException;
import com.ecommerce.ecommerce_api.security.JwtService;
import com.ecommerce.ecommerce_api.user.dto.LoginRequest;
import com.ecommerce.ecommerce_api.user.dto.LoginResponse;
import com.ecommerce.ecommerce_api.user.dto.RegisterRequestDTO;
import com.ecommerce.ecommerce_api.user.dto.UserResponseDTO;
import com.ecommerce.ecommerce_api.user.entity.User;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponseDTO register(RegisterRequestDTO requestDTO) {
        String email = requestDTO.email().trim().toLowerCase();
        User user = new User(
                requestDTO.name().trim(),
                email,
                passwordEncoder.encode(requestDTO.password())
        );

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException();
        }

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        String jwt = jwtService.generateToken(user);

        return new LoginResponse(jwt, "Bearer");
    }
}