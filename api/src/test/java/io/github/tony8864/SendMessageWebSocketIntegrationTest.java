package io.github.tony8864;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tony8864.chat.repository.DirectChatRepository;
import io.github.tony8864.dto.ChatMessageDto;
import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.chat.DirectChat;
import io.github.tony8864.entities.user.*;
import io.github.tony8864.security.TokenService;
import io.github.tony8864.security.UserClaims;
import io.github.tony8864.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ChatApplication.class)
@Testcontainers
public class SendMessageWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private DirectChatRepository directChatRepository;
    @Autowired private PasswordHasher passwordHasher;
    @Autowired private TokenService tokenService;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void whenMessageSentOverHttp_thenReceivedOverWebSocket() throws Exception {
        // --- Arrange ---
        var user1 = User.create(
                UserId.newId(),
                "user1_" + UUID.randomUUID(),
                Email.of("user1_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );
        var user2 = User.create(
                UserId.newId(),
                "user2_" + UUID.randomUUID(),
                Email.of("user2_" + UUID.randomUUID() + "@example.com"),
                PasswordHash.newHash("secret123")
        );

        userRepository.save(user1);
        userRepository.save(user2);

        DirectChat chat = DirectChat.create(ChatId.newId(), List.of(user1.getUserId(), user2.getUserId()));
        directChatRepository.save(chat);

        var claims = new UserClaims(
                user1.getUserId().getValue(),
                user1.getEmail().getValue(),
                Set.of("USER")
        );
        String token = tokenService.generate(claims, Duration.ofHours(1));

        CompletableFuture<ChatMessageDto> future = new CompletableFuture<>();

        // --- Subscribe over WebSocket ---
        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/chats/" + chat.getChatId().getValue() + "/messages",
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessageDto.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        future.complete((ChatMessageDto) payload);
                    }
                });

        // --- Act: send HTTP POST with JWT ---
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        Map<String, String> body = Map.of(
                "senderId", user1.getUserId().getValue(),
                "content", "Hello from WebSocket test!"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        new RestTemplate().postForEntity(
                "http://localhost:" + port + "/api/chats/" + chat.getChatId().getValue() + "/messages",
                entity,
                Void.class
        );

        // --- Assert ---
        ChatMessageDto received = future.get(3, TimeUnit.SECONDS);
        assertEquals("Hello from WebSocket test!", received.getContent());
        assertEquals(user1.getUserId().getValue(), received.getSenderId());
    }
}
