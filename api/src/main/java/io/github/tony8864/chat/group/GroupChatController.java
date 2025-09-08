package io.github.tony8864.chat.group;

import io.github.tony8864.chat.group.dto.*;
import io.github.tony8864.chat.group.mapper.GroupChatApiMapper;
import io.github.tony8864.chat.usecase.deletegroupchat.DeleteGroupChatUseCase;
import io.github.tony8864.chat.usecase.deletegroupchat.dto.DeleteGroupChatRequest;
import io.github.tony8864.chat.usecase.removeparticipant.RemoveParticipantUseCase;
import io.github.tony8864.chat.usecase.renamegroupchat.RenameGroupChatUseCase;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats/group")
@AllArgsConstructor
public class GroupChatController {

    private final RemoveParticipantUseCase removeParticipantUseCase;
    private final DeleteGroupChatUseCase deleteGroupChatUseCase;
    private final RenameGroupChatUseCase renameGroupChatUseCase;

    private final GroupChatApiMapper mapper;

    @PostMapping("/rename")
    public ResponseEntity<RenameGroupChatApiResponse> renameGroupChat(HttpServletRequest httpServletRequest, @RequestBody RenameGroupChatApiRequest apiRequest) {
        var requester = (AuthenticatedUser) httpServletRequest.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, requester.userId());
        var appResponse = renameGroupChatUseCase.rename(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/remove-participant")
    public ResponseEntity<RemoveParticipantApiResponse> removeParticipant(@RequestBody RemoveParticipantApiRequest apiRequest) {
        var appRequest = mapper.toApplication(apiRequest);
        var appResponse = removeParticipantUseCase.remove(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteGroupChat(@PathVariable String chatId, @RequestParam String requesterId) {
        var request = new DeleteGroupChatRequest(chatId, requesterId);
        deleteGroupChatUseCase.delete(request);
        return ResponseEntity.noContent().build();
    }
}
