package io.github.tony8864.chat.direct.config;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.usecase.createdirectchat.CreateDirectChatUseCase;
import io.github.tony8864.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectChatConfig {

    @Bean
    public CreateDirectChatUseCase createDirectChatUseCase(UserRepository userRepository, DirectChatRepository directChatRepository) {
        return new CreateDirectChatUseCase(userRepository, directChatRepository);
    }
}
