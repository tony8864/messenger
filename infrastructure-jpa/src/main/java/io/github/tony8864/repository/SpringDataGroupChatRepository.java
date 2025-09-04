package io.github.tony8864.repository;

import io.github.tony8864.entity.GroupChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataGroupChatRepository extends JpaRepository<GroupChatEntity, UUID> {
    @Query("SELECT g FROM GroupChatEntity g LEFT JOIN FETCH g.participants WHERE g.id = :id")
    Optional<GroupChatEntity> findByIdWithParticipants(@Param("id") UUID id);
}
