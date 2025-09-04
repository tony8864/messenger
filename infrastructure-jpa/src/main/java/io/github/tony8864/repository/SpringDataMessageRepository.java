package io.github.tony8864.repository;

import io.github.tony8864.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataMessageRepository extends JpaRepository<MessageEntity, UUID> {
}
