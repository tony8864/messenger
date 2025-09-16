package io.github.tony8864.user;

import io.github.tony8864.user.dto.*;
import io.github.tony8864.user.mapper.UserApiMapper;
import io.github.tony8864.user.usecase.login.LoginUserUseCase;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import io.github.tony8864.user.usecase.logout.LogoutUseCase;
import io.github.tony8864.user.usecase.register.RegisterUserUseCase;
import io.github.tony8864.user.usecase.searchuser.SearchUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private final RegisterUserUseCase registerUserUseCase;
    private final SearchUserUseCase searchUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final LogoutUseCase logoutUseCase;

    private final UserApiMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserApiResponse> reigster(@RequestBody RegisterUserApiRequest apiRequest) {
        var request = mapper.toApplication(apiRequest);
        var registeredUser = registerUserUseCase.register(request);
        var response = mapper.toApi(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginApiResponse> login(@RequestBody LoginApiRequest apiRequest) {
        var request = mapper.toApplication(apiRequest);
        var authenticatedUser = loginUserUseCase.login(request);
        var response = mapper.toApi(authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutApiResponse> logout(HttpServletRequest request) {
        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        logoutUseCase.logout(requester.userId());
        return ResponseEntity.ok(LogoutApiResponse.success(requester.userId()));
    }

    @PostMapping("/search")
    public ResponseEntity<SearchUserApiResponse> searchUser(@RequestBody SearchUserApiRequest apiRequest) {
        var appRequest = mapper.toApplication(apiRequest);
        var appResponse = searchUserUseCase.search(appRequest);
        return ResponseEntity.ok(mapper.toApi(appResponse));
    }
}
