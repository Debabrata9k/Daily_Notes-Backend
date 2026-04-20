package com.daily.note.save.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.daily.note.save.entity.Note;
import com.daily.note.save.entity.User;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUser(User user, Pageable pageable);
    Page<Note> findByUserAndTitleContainingIgnoreCase(User user, String keyword, Pageable pageable);
    Optional<Note> findByIdAndUser(Long id, User user);
}
