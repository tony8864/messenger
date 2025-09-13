package io.github.tony8864.chat.usecase.listchats;

import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.chat.repository.GroupChatRepository;
import io.github.tony8864.chat.usecase.listchats.dto.ChatSummaryDto;
import io.github.tony8864.chat.usecase.listchats.dto.ListChatsRequest;
import io.github.tony8864.chat.usecase.listchats.dto.ListChatsResponse;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.chat.GroupChat;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.message.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListChatsUseCaseTest {
    private DirectChatRepository directChatRepository;
    private GroupChatRepository groupChatRepository;
    private MessageRepository messageRepository;
    private ListChatsUseCase useCase;

    private UserId requesterId;

    @BeforeEach
    void setUp() {
        directChatRepository = mock(DirectChatRepository.class);
        groupChatRepository = mock(GroupChatRepository.class);
        messageRepository = mock(MessageRepository.class);
        useCase = new ListChatsUseCase(directChatRepository, groupChatRepository, messageRepository);

        requesterId = UserId.of("req-1");
    }

    @Test
    void shouldListDirectChatsForUser() {
        // --- Arrange ---
        DirectChat directChat = mock(DirectChat.class);
        when(directChat.getChatId()).thenReturn(ChatId.of("chat-1"));
        when(directChat.getParticipants()).thenReturn(List.of(requesterId, UserId.of("user-2")));

        Message lastMessage = mock(Message.class);
        when(lastMessage.getContent()).thenReturn("Hello there");
        when(lastMessage.getCreatedAt()).thenReturn(Instant.parse("2025-09-13T10:15:30Z"));

        when(directChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of(directChat));
        when(messageRepository.findLastMessage(ChatId.of("chat-1")))
                .thenReturn(Optional.of(lastMessage));

        // --- Act ---
        ListChatsRequest request = new ListChatsRequest("req-1", 10);
        ListChatsResponse response = useCase.list(request);

        // --- Assert ---
        assertEquals(1, response.chats().size());
        ChatSummaryDto summary = response.chats().get(0);

        assertEquals("chat-1", summary.chatId());
        assertEquals("DIRECT", summary.type());
        assertEquals("user-2", summary.name()); // other participant
        assertEquals("Hello there", summary.lastMessage());
        assertEquals(Instant.parse("2025-09-13T10:15:30Z"), summary.lastMessageAt());

        verify(directChatRepository).findByParticipant(requesterId);
        verify(messageRepository).findLastMessage(ChatId.of("chat-1"));
    }

    @Test
    void shouldListGroupChatsForUser() {
        // --- Arrange ---
        GroupChat groupChat = mock(GroupChat.class);
        when(groupChat.getChatId()).thenReturn(ChatId.of("chat-2"));
        when(groupChat.getGroupName()).thenReturn("Project Team");

        Message lastMessage = mock(Message.class);
        when(lastMessage.getContent()).thenReturn("Meeting tomorrow at 10");
        when(lastMessage.getCreatedAt()).thenReturn(Instant.parse("2025-09-13T12:00:00Z"));

        when(groupChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of(groupChat));
        when(messageRepository.findLastMessage(ChatId.of("chat-2")))
                .thenReturn(Optional.of(lastMessage));

        // --- Act ---
        ListChatsRequest request = new ListChatsRequest("req-1", 10);
        ListChatsResponse response = useCase.list(request);

        // --- Assert ---
        assertEquals(1, response.chats().size());
        ChatSummaryDto summary = response.chats().get(0);

        assertEquals("chat-2", summary.chatId());
        assertEquals("GROUP", summary.type());
        assertEquals("Project Team", summary.name());
        assertEquals("Meeting tomorrow at 10", summary.lastMessage());
        assertEquals(Instant.parse("2025-09-13T12:00:00Z"), summary.lastMessageAt());

        verify(groupChatRepository).findByParticipant(requesterId);
        verify(messageRepository).findLastMessage(ChatId.of("chat-2"));
    }

    @Test
    void shouldMergeAndSortByLastMessageAt() {
        // --- Arrange ---
        DirectChat directChat = mock(DirectChat.class);
        when(directChat.getChatId()).thenReturn(ChatId.of("chat-1"));
        when(directChat.getParticipants()).thenReturn(List.of(requesterId, UserId.of("user-2")));

        GroupChat groupChat = mock(GroupChat.class);
        when(groupChat.getChatId()).thenReturn(ChatId.of("chat-2"));
        when(groupChat.getGroupName()).thenReturn("Project Group");

        Message directLastMessage = mock(Message.class);
        when(directLastMessage.getContent()).thenReturn("Direct message");
        when(directLastMessage.getCreatedAt()).thenReturn(Instant.parse("2025-09-13T10:00:00Z"));

        Message groupLastMessage = mock(Message.class);
        when(groupLastMessage.getContent()).thenReturn("Group message");
        when(groupLastMessage.getCreatedAt()).thenReturn(Instant.parse("2025-09-13T12:00:00Z"));

        when(directChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of(directChat));
        when(groupChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of(groupChat));

        when(messageRepository.findLastMessage(ChatId.of("chat-1")))
                .thenReturn(Optional.of(directLastMessage));
        when(messageRepository.findLastMessage(ChatId.of("chat-2")))
                .thenReturn(Optional.of(groupLastMessage));

        // --- Act ---
        ListChatsRequest request = new ListChatsRequest("req-1", 10);
        ListChatsResponse response = useCase.list(request);

        // --- Assert ---
        assertEquals(2, response.chats().size());

        // chats should be sorted by lastMessageAt (descending)
        assertEquals("chat-2", response.chats().get(0).chatId());
        assertEquals("Group message", response.chats().get(0).lastMessage());

        assertEquals("chat-1", response.chats().get(1).chatId());
        assertEquals("Direct message", response.chats().get(1).lastMessage());

        verify(directChatRepository).findByParticipant(requesterId);
        verify(groupChatRepository).findByParticipant(requesterId);
    }

    @Test
    void shouldRespectLimitParameter() {
        // --- Arrange ---
        DirectChat chat1 = mock(DirectChat.class);
        when(chat1.getChatId()).thenReturn(ChatId.of("chat-1"));
        when(chat1.getParticipants()).thenReturn(List.of(requesterId, UserId.of("user-x")));

        DirectChat chat2 = mock(DirectChat.class);
        when(chat2.getChatId()).thenReturn(ChatId.of("chat-2"));
        when(chat2.getParticipants()).thenReturn(List.of(requesterId, UserId.of("user-y")));

        Message lastMsg1 = mock(Message.class);
        when(lastMsg1.getContent()).thenReturn("Old message");
        when(lastMsg1.getCreatedAt()).thenReturn(Instant.parse("2025-09-13T10:00:00Z"));

        Message lastMsg2 = mock(Message.class);
        when(lastMsg2.getContent()).thenReturn("New message");
        when(lastMsg2.getCreatedAt()).thenReturn(Instant.parse("2025-09-13T12:00:00Z"));

        when(directChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of(chat1, chat2));
        when(groupChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of()); // no group chats

        when(messageRepository.findLastMessage(ChatId.of("chat-1")))
                .thenReturn(Optional.of(lastMsg1));
        when(messageRepository.findLastMessage(ChatId.of("chat-2")))
                .thenReturn(Optional.of(lastMsg2));

        // --- Act ---
        ListChatsRequest request = new ListChatsRequest("req-1", 1); // limit = 1
        ListChatsResponse response = useCase.list(request);

        // --- Assert ---
        assertEquals(1, response.chats().size());
        ChatSummaryDto summary = response.chats().get(0);

        assertEquals("chat-2", summary.chatId()); // newer chat should remain
        assertEquals("New message", summary.lastMessage());

        verify(directChatRepository).findByParticipant(requesterId);
        verify(messageRepository).findLastMessage(ChatId.of("chat-1"));
        verify(messageRepository).findLastMessage(ChatId.of("chat-2"));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoChats() {
        // --- Arrange ---
        when(directChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of());
        when(groupChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of());

        // --- Act ---
        ListChatsRequest request = new ListChatsRequest("req-1", 10);
        ListChatsResponse response = useCase.list(request);

        // --- Assert ---
        assertNotNull(response.chats());
        assertTrue(response.chats().isEmpty(), "Chats list should be empty");

        verify(directChatRepository).findByParticipant(requesterId);
        verify(groupChatRepository).findByParticipant(requesterId);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void shouldHandleChatsWithoutMessages() {
        // --- Arrange ---
        GroupChat groupChat = mock(GroupChat.class);
        when(groupChat.getChatId()).thenReturn(ChatId.of("chat-99"));
        when(groupChat.getGroupName()).thenReturn("Silent Group");

        when(groupChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of(groupChat));
        when(directChatRepository.findByParticipant(requesterId))
                .thenReturn(List.of());

        // no messages found
        when(messageRepository.findLastMessage(ChatId.of("chat-99")))
                .thenReturn(Optional.empty());

        // --- Act ---
        ListChatsRequest request = new ListChatsRequest("req-1", 10);
        ListChatsResponse response = useCase.list(request);

        // --- Assert ---
        assertEquals(1, response.chats().size());
        ChatSummaryDto summary = response.chats().get(0);

        assertEquals("chat-99", summary.chatId());
        assertEquals("GROUP", summary.type());
        assertEquals("Silent Group", summary.name());
        assertNull(summary.lastMessage(), "Last message should be null if none exist");
        assertNull(summary.lastMessageAt(), "Last messageAt should be null if none exist");

        verify(groupChatRepository).findByParticipant(requesterId);
        verify(messageRepository).findLastMessage(ChatId.of("chat-99"));
    }
}