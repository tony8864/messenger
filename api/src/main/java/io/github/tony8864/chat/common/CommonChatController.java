package io.github.tony8864.chat.common;

import io.github.tony8864.chat.common.dto.ListChatsApiResponse;
import io.github.tony8864.chat.common.mapper.CommonChatMapper;
import io.github.tony8864.chat.usecase.listchats.ListChatsUseCase;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
@AllArgsConstructor
public class CommonChatController {

    private final ListChatsUseCase listChatsUseCase;
    private final CommonChatMapper mapper;

    @GetMapping
    public ListChatsApiResponse listChats(
            HttpServletRequest request,
            @RequestParam(name = "limit", defaultValue = "0") int limit
    ) {

        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(requester.userId(), limit);
        var appResponse = listChatsUseCase.list(appRequest);

        return mapper.toApi(appResponse);
    }
}
