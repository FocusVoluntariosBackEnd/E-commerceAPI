package com.ecommerce.ecommerce_api.auth.service;

import com.ecommerce.ecommerce_api.user.dto.RegisterRequestDTO;
import com.ecommerce.ecommerce_api.user.dto.UserResponseDTO;
import com.ecommerce.ecommerce_api.user.entity.User;
import com.ecommerce.ecommerce_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO register(RegisterRequestDTO requestDTO){
        if (userRepository.existsByEmail(requestDTO.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já está em Uso.");
        }

        String senhaCriptografada = passwordEncoder.encode(requestDTO.password());
        System.out.println("SENHA CRIPTTOGRAFDA QUE VAI PRO BANCO" + senhaCriptografada);

        User user = new User(
                requestDTO.name(),
                requestDTO.email(),
                senhaCriptografada
        );

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );

    }



}
