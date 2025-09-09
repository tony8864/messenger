package io.github.tony8864.chat.group;

import io.github.tony8864.chat.group.dto.*;
import io.github.tony8864.chat.group.mapper.GroupChatApiMapper;
import io.github.tony8864.chat.usecase.addparticipant.AddParticipantUseCase;
import io.github.tony8864.chat.usecase.creategroupchat.CreateGroupChatUseCase;
import io.github.tony8864.chat.usecase.deletegroupchat.DeleteGroupChatUseCase;
import io.github.tony8864.chat.usecase.deletegroupchat.dto.DeleteGroupChatRequest;
import io.github.tony8864.chat.usecase.removeparticipant.RemoveParticipantUseCase;
import io.github.tony8864.chat.usecase.renamegroupchat.RenameGroupChatUseCase;
import io.github.tony8864.user.usecase.login.dto.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats/group")
@AllArgsConstructor
public class GroupChatController {

    private final RemoveParticipantUseCase removeParticipantUseCase;
    private final CreateGroupChatUseCase createGroupChatUseCase;
    private final DeleteGroupChatUseCase deleteGroupChatUseCase;
    private final RenameGroupChatUseCase renameGroupChatUseCase;
    private final AddParticipantUseCase addParticipantUseCase;

    private final GroupChatApiMapper mapper;

    @PostMapping("/create")
    public ResponseEntity<CreateGroupChatApiResponse> createGroupChat(
            HttpServletRequest request,
            @RequestBody CreateGroupChatApiRequest apiRequest) {

        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, requester.userId());
        var appResponse = createGroupChatUseCase.create(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/rename")
    public ResponseEntity<RenameGroupChatApiResponse> renameGroupChat(
            HttpServletRequest httpServletRequest,
            @RequestBody RenameGroupChatApiRequest apiRequest) {

        var requester = (AuthenticatedUser) httpServletRequest.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, requester.userId());
        var appResponse = renameGroupChatUseCase.rename(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/add-participant")
    public ResponseEntity<AddParticipantApiResponse> addParticipant(
            HttpServletRequest request,
            @RequestBody AddParticipantApiRequest apiRequest
    ) {

        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, requester.userId());
        var appResponse = addParticipantUseCase.add(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/remove-participant")
    public ResponseEntity<RemoveParticipantApiResponse> removeParticipant(
            HttpServletRequest httpServletRequest,
            @RequestBody RemoveParticipantApiRequest apiRequest) {

        var requester = (AuthenticatedUser) httpServletRequest.getAttribute("authenticatedUser");
        var appRequest = mapper.toApplication(apiRequest, requester.userId());
        var appResponse = removeParticipantUseCase.remove(appRequest);
        var apiResponse = mapper.toApi(appResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteGroupChat(HttpServletRequest request, @PathVariable String chatId) {
        var requester = (AuthenticatedUser) request.getAttribute("authenticatedUser");
        var appRequest = new DeleteGroupChatRequest(chatId, requester.userId());
        deleteGroupChatUseCase.delete(appRequest);
        return ResponseEntity.noContent().build();
    }
}
