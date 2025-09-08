package io.github.tony8864.chat.group;

import io.github.tony8864.chat.group.dto.RemoveParticipantApiRequest;
import io.github.tony8864.chat.group.dto.RemoveParticipantApiResponse;
import io.github.tony8864.chat.group.dto.RenameGroupChatApiRequest;
import io.github.tony8864.chat.group.dto.RenameGroupChatApiResponse;
import io.github.tony8864.chat.group.mapper.GroupChatApiMapper;
import io.github.tony8864.chat.usecase.removeparticipant.RemoveParticipantUseCase;
import io.github.tony8864.chat.usecase.renamegroupchat.RenameGroupChatUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats/group")
@AllArgsConstructor
public class GroupChatController {

    private final RenameGroupChatUseCase renameGroupChatUseCase;
    private final RemoveParticipantUseCase removeParticipantUseCase;

    private final GroupChatApiMapper mapper;

    @PostMapping("/rename")
    public ResponseEntity<RenameGroupChatApiResponse> renameGroupChat(@RequestBody RenameGroupChatApiRequest apiRequest) {
        var appRequest = mapper.toApplication(apiRequest);
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
}
