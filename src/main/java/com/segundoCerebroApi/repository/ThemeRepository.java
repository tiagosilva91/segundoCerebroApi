package com.segundoCerebroApi.repository;

import com.segundoCerebroApi.domain.Theme;
import com.segundoCerebroApi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ThemeRepository extends JpaRepository<Theme, UUID> {
    List<Theme> findAllByUser(User user);

    long countByUser(User user);

    boolean existsByUserAndNameIgnoreCase(User user, String name);

    boolean existsByUserAndNameIgnoreCaseAndIdNot(User user, String name, UUID id);

    @Query("SELECT t FROM Theme t WHERE t.id IN :ids AND t.user = :user")
    List<Theme> findAllByIdInAndUser(@Param("ids") List<UUID> ids, @Param("user") User user);
}
