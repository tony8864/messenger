package io.github.tony8864.adapter;

import io.github.tony8864.entities.chat.ChatId;
import io.github.tony8864.entities.message.Message;
import io.github.tony8864.entities.message.MessageId;
import io.github.tony8864.entities.message.MessageStatus;
import io.github.tony8864.entities.user.UserId;
import io.github.tony8864.entity.GroupChatEntity;
import io.github.tony8864.entity.UserEntity;
import io.github.tony8864.mapping.MessageMapper;
import io.github.tony8864.repository.SpringDataGroupChatRepository;
import io.github.tony8864.repository.SpringDataMessageRepository;
import io.github.tony8864.repository.SpringDataUserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
class JpaMessageRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SpringDataMessageRepository springDataMessageRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Autowired
    private SpringDataGroupChatRepository springDataGroupChatRepository;

    @Autowired
    private EntityManager entityManager;

    private JpaMessageRepository jpaMessageRepository;

    @BeforeEach
    void setUp() {
        jpaMessageRepository = new JpaMessageRepository(
                springDataMessageRepository,
                springDataUserRepository,
                entityManager,
                new MessageMapper()
        );
    }

    @Test
    void saveAndFindLastNMessages_shouldWorkCorrectly() {
        // given
        UUID userId = UUID.randomUUID();
        UserEntity user = springDataUserRepository.save(
                UserEntity.builder()
                        .id(userId)
                        .username("alice")
                        .email("alice@example.com")
                        .passwordHash("secret")
                        .createdAt(Instant.now())
                        .status("ACTIVE")
                        .build()
        );

        UUID chatId = UUID.randomUUID();
        GroupChatEntity chat = springDataGroupChatRepository.save(
                new GroupChatEntity(
                        chatId,
                        "Test Group",
                        "ACTIVE",
                        Instant.now(),
                        null,
                        List.of()
                )
        );

        // create two messages in domain model
        Message message1 = Message.restore(
                MessageId.of(UUID.randomUUID().toString()),
                ChatId.of(chatId.toString()),
                UserId.of(userId.toString()),
                "first message",
                Instant.now().minusSeconds(60),
                MessageStatus.SENT,
                null
        );

        Message message2 = Message.restore(
                MessageId.of(UUID.randomUUID().toString()),
                ChatId.of(chatId.toString()),
                UserId.of(userId.toString()),
                "second message",
                Instant.now(),
                MessageStatus.SENT,
                null
        );

        // when: save messages
        jpaMessageRepository.save(message1);
        jpaMessageRepository.save(message2);

        // then: retrieve only the last message
        List<Message> lastMessages = jpaMessageRepository.findLastNMessages(ChatId.of(chatId.toString()), 1);

        assertThat(lastMessages).hasSize(1);
        assertThat(lastMessages.get(0).getContent()).isEqualTo("second message");

        // and retrieving 2 should return both
        List<Message> twoMessages = jpaMessageRepository.findLastNMessages(ChatId.of(chatId.toString()), 2);
        assertThat(twoMessages).hasSize(2);
        assertThat(twoMessages.get(0).getContent()).isEqualTo("second message");
        assertThat(twoMessages.get(1).getContent()).isEqualTo("first message");
    }
}