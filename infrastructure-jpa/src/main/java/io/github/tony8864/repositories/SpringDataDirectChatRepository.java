package io.github.tony8864.repositories;

import io.github.tony8864.mappings.DirectChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDirectChatRepository extends JpaRepository<DirectChatEntity, UUID> {
    @Query(
            "SELECT dc FROM DirectChatEntity dc " +
            "WHERE (dc.user1.id = :user1Id AND dc.user2.id = :user2Id) " +
            "OR (dc.user1.id = :user2Id AND dc.user2.id = :user1Id)"
    )
    Optional<DirectChatEntity> findByUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
}
