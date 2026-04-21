package com.segundoCerebroApi.repository;

import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ThemeRepository extends JpaRepository<Theme, UUID> {
    List<Theme> findAllByUser(User user);
}
