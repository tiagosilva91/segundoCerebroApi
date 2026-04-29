package com.segundoCerebroApi.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(nullable = false)
        private String title;

        @Column(columnDefinition = "TEXT", nullable = false)
        private String content;

        private String audioUrl;

        private String imageUrl;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)") // <--- PROTEÇÃO EXTRA AQUI
        private User user;

        @ManyToMany
        @JoinTable(name = "note_themes", joinColumns = @JoinColumn(name = "note_id"), inverseJoinColumns = @JoinColumn(name = "theme_id"))
        private Set<Theme> themes;

        @ElementCollection
        @CollectionTable(name = "note_biblical_references", joinColumns = @JoinColumn(name = "note_id"))
        @Column(name = "reference", nullable = false)
        private List<String> biblicalReferences;
}