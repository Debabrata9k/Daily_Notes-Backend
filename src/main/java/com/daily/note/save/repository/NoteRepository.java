package com.daily.note.save.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daily.note.save.entity.Note;
import com.daily.note.save.entity.User;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);

    Optional<Note> findByIdAndUser(Long id, User user);

    List<Note> findByUserAndTitleContainingIgnoreCase(User user, String title);

}
