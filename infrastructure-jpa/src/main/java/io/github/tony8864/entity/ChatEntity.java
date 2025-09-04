package io.github.tony8864.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "chats")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ChatEntity {
    @Id
    private UUID id;

    protected ChatEntity(UUID id) {
        this.id = id;
    }
}
