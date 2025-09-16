package io.github.tony8864.user.mapper;

import io.github.tony8864.user.dto.*;
import io.github.tony8864.user.usecase.login.dto.AuthRequest;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import io.github.tony8864.user.usecase.register.dto.RegisterUserRequest;
import io.github.tony8864.user.usecase.register.dto.RegisterUserResponse;
import io.github.tony8864.user.usecase.searchuser.dto.SearchUserRequest;
import io.github.tony8864.user.usecase.searchuser.dto.SearchUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserApiMapper {

    // --- Register ---
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

    // --- Login ---
    public AuthRequest toApplication(LoginApiRequest apiRequest) {
        return new AuthRequest(
                apiRequest.email(),
                apiRequest.password()
        );
    }

    public LoginApiResponse toApi(AuthenticatedUser authenticated) {
        return new LoginApiResponse(
                authenticated.userId(),
                authenticated.username(),
                authenticated.email(),
                authenticated.presenceStatus(),
                authenticated.token()
        );
    }

    // --- Search User ---
    public SearchUserRequest toApplication(SearchUserApiRequest apiRequest) {
        return new SearchUserRequest(apiRequest.username());
    }

    public SearchUserApiResponse toApi(SearchUserResponse appResponse) {
        return new SearchUserApiResponse(
                appResponse.userId(),
                appResponse.username()
        );
    }
}
