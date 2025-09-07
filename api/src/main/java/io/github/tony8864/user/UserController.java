package io.github.tony8864.user;

import io.github.tony8864.user.dto.RegisterUserApiRequest;
import io.github.tony8864.user.dto.RegisterUserApiResponse;
import io.github.tony8864.user.mapper.UserApiMapper;
import io.github.tony8864.user.usecase.register.RegisterUserUseCase;
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
    private final UserApiMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserApiResponse> reigster(@RequestBody RegisterUserApiRequest apiRequest) {
        var request = mapper.toApplication(apiRequest);
        var response = registerUserUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toApi(response));
    }
}
