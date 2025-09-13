package io.github.tony8864.message.config;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.message.repository.MessageEventPublisher;
import io.github.tony8864.message.repository.MessageRepository;
import io.github.tony8864.message.usecase.listmessages.ListMessagesUseCase;
import io.github.tony8864.message.usecase.sendmessage.SendMessageUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {
    @Bean
    public SendMessageUseCase sendMessageUseCase(
            MessageRepository messageRepository,
            GroupChatRepository groupChatRepository,
            DirectChatRepository directChatRepository,
            MessageEventPublisher messageEventPublisher
    ) {
        return new SendMessageUseCase(
                messageRepository,
                groupChatRepository,
                directChatRepository,
                messageEventPublisher
        );
    }

    @Bean
    public ListMessagesUseCase listMessagesUseCase(MessageRepository messageRepository, GroupChatRepository groupChatRepository, DirectChatRepository directChatRepository) {
        return new ListMessagesUseCase(messageRepository, groupChatRepository, directChatRepository);
    }
}
