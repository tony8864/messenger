package io.github.tony8864.user.config;

import io.github.tony8864.entities.user.PasswordHasher;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.user.usecase.login.LoginUserUseCase;
import io.github.tony8864.user.usecase.register.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new RegisterUserUseCase(userRepository, passwordHasher);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher, TokenService tokenService) {
        return new LoginUserUseCase(userRepository, passwordHasher, tokenService);
    }
}
