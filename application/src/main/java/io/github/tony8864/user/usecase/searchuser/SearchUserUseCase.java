package io.github.tony8864.user.usecase.searchuser;

import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.user.repository.UserRepository;
import io.github.tony8864.user.usecase.register.exception.UsernameAlreadyExistsException;
import io.github.tony8864.user.usecase.searchuser.dto.SearchUserRequest;
import io.github.tony8864.user.usecase.searchuser.dto.SearchUserResponse;

public class SearchUserUseCase {

    private final UserRepository userRepository;

    public SearchUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SearchUserResponse search(SearchUserRequest request) {
        return userRepository.findByUsername(request.username())
                .map(SearchUserResponse::fromDomain)
                .orElseThrow(() -> UserNotFoundException.byUsername(request.username()));
    }
}
