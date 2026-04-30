package com.segundoCerebroApi.repository;

import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ThemeRepository extends JpaRepository<Theme, UUID> {
    List<Theme> findAllByUser(User user);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Theme t WHERE t.id IN :ids AND t.user = :user")
    List<Theme> findAllByIdInAndUser(@org.springframework.data.repository.query.Param("ids") List<UUID> ids, @org.springframework.data.repository.query.Param("user") User user);
}
