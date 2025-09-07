package io.github.tony8864.user.config;

import io.github.tony8864.entities.user.PasswordHasher;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.user.usecase.register.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new RegisterUserUseCase(userRepository, passwordHasher);
    }
}
