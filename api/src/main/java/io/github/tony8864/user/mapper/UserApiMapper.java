package io.github.tony8864.user.mapper;

import io.github.tony8864.user.dto.RegisterUserApiRequest;
import io.github.tony8864.user.dto.RegisterUserApiResponse;
import io.github.tony8864.user.usecase.register.dto.RegisterUserRequest;
import io.github.tony8864.user.usecase.register.dto.RegisterUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserApiMapper {
    public RegisterUserRequest toApplication(RegisterUserApiRequest apiRequest) {
        return new RegisterUserRequest(
                apiRequest.username(),
                apiRequest.email(),
                apiRequest.password()
        );
    }

    public RegisterUserApiResponse toApi(RegisterUserResponse response) {
        return new RegisterUserApiResponse(
                response.userId(),
                response.username(),
                response.email()
        );
    }
}
