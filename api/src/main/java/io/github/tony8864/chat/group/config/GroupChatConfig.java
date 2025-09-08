package io.github.tony8864.chat.group.config;

import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.removeparticipant.RemoveParticipantUseCase;
import io.github.tony8864.chat.usecase.renamegroupchat.RenameGroupChatUseCase;
import io.github.tony8864.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupChatConfig {

    @Bean
    public RenameGroupChatUseCase renameGroupChatUseCase(UserRepository userRepository, GroupChatRepository groupChatRepository) {
        return new RenameGroupChatUseCase(userRepository, groupChatRepository);
    }

    @Bean
    public RemoveParticipantUseCase removeParticipantUseCase(UserRepository userRepository, GroupChatRepository groupChatRepository) {
        return new RemoveParticipantUseCase(userRepository, groupChatRepository);
    }
}
