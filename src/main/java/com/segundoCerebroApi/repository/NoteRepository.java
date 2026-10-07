package com.segundoCerebroApi.repository;

import com.segundoCerebroApi.domain.Note;
import com.segundoCerebroApi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findAllByUser(User user);

    List<Note> findAllByUserOrderByUpdatedAtDesc(User user);

    long countByUser(User user);

    /** Busca textual nas notas do usuário: título, conteúdo, temas e referências bíblicas. */
    @Query("""
            SELECT DISTINCT n FROM Note n
            LEFT JOIN n.themes t
            LEFT JOIN n.biblicalReferences r
            WHERE n.user = :user AND (
                LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(n.content) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    List<Note> search(@Param("user") User user, @Param("q") String q);
}
