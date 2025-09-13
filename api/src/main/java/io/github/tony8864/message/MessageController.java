package io.github.tony8864.message;

import io.github.tony8864.message.dto.ListMessagesApiResponse;
import io.github.tony8864.message.dto.SendMessageApiRequest;
import io.github.tony8864.message.dto.SendMessageApiResponse;
import io.github.tony8864.message.mapper.MessageApiMapper;
import io.github.tony8864.message.usecase.listmessages.ListMessagesUseCase;
import io.github.tony8864.message.usecase.sendmessage.SendMessageUseCase;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@AllArgsConstructor
public class MessageController {
    private final SendMessageUseCase sendMessageUseCase;
    private final ListMessagesUseCase listMessagesUseCase;

    private final MessageApiMapper mapper;

    @PostMapping
    public ResponseEntity<SendMessageApiResponse> sendMessage(
            HttpServletRequest request,
            @PathVariable String chatId,
            @RequestBody SendMessageApiRequest apiRequest
    ) {

        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, chatId, requester.userId());
        var appResponse = sendMessageUseCase.send(appRequest);
        var apiResponse = mapper.toApi(appResponse);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ListMessagesApiResponse> listMessages(
            HttpServletRequest request,
            @PathVariable String chatId,
            @RequestParam(defaultValue = "50") int limit
    ) {

        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(chatId, requester.userId(), limit);
        var appResponse = listMessagesUseCase.list(appRequest);
        var apiResponse = mapper.toApi(appResponse);

        return ResponseEntity.ok(apiResponse);
    }
}
