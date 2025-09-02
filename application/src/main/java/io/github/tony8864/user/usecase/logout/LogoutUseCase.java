package io.github.tony8864.user.usecase.logout;

import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.entities.user.PresenceStatus;
import io.github.tony8864.entities.user.User;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.user.repository.UserRepository;

public class LogoutUseCase {

    private final UserRepository userRepository;

    public LogoutUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void logout(String userId) {
        UserId id = UserId.of(userId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        user.setPresenceStatus(PresenceStatus.OFFLINE);
        userRepository.save(user);
    }
}
