package io.github.tony8864.repository;

import io.github.tony8864.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataMessageRepository extends JpaRepository<MessageEntity, UUID> {
    List<MessageEntity> findByChat_IdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);
}
