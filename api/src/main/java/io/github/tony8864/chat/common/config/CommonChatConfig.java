package io.github.tony8864.chat.common.config;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.listchats.ListChatsUseCase;
import io.github.tony8864.message.repository.MessageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonChatConfig {

    @Bean
    public ListChatsUseCase listChatsUseCase(MessageRepository messageRepository, GroupChatRepository groupChatRepository, DirectChatRepository directChatRepository) {
        return new ListChatsUseCase(directChatRepository, groupChatRepository, messageRepository);
    }
}
