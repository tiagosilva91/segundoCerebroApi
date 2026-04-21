package com.projetoTeste.repository;

import com.projetoTeste.domain.Note;
import com.projetoTeste.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findAllByUser(User user);
}
