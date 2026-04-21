package com.projetoTeste.repository;

import com.projetoTeste.domain.Theme;
import com.projetoTeste.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ThemeRepository extends JpaRepository<Theme, UUID> {
    List<Theme> findAllByUser(User user);
}
