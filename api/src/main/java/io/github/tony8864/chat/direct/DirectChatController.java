package io.github.tony8864.chat.direct;

import io.github.tony8864.chat.direct.dto.CreateDirectChatApiRequest;
import io.github.tony8864.chat.direct.dto.CreateDirectChatApiResponse;
import io.github.tony8864.chat.direct.mapper.DirectChatApiMapper;
import io.github.tony8864.chat.usecase.createdirectchat.CreateDirectChatUseCase;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats/direct")
@AllArgsConstructor
public class DirectChatController {

    private final CreateDirectChatUseCase createDirectChatUseCase;

    private final DirectChatApiMapper mapper;

    @PostMapping("/create")
    public ResponseEntity<CreateDirectChatApiResponse> createDirectChat(
            HttpServletRequest request,
            @RequestBody CreateDirectChatApiRequest apiRequest) {

        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, requester.userId());
        var appResponse = createDirectChatUseCase.create(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
