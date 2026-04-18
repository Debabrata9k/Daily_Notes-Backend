package com.daily.note.save.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.daily.note.save.dto.AddNoteDto;
import com.daily.note.save.dto.NoteDto;
import com.daily.note.save.entity.Note;
import com.daily.note.save.entity.User;
import com.daily.note.save.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final ModelMapper modelMapper;

    // ✅ keep private (no need to expose)
    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // ✅ expose only ID for caching (safe)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // =========================
    // 📌 GET NOTES
    // =========================
    @Cacheable(
        value = "notes",
        key = "#root.target.getCurrentUserId() + '_' + #page + '_' + #size"
    )
    public List<NoteDto> getNotes(int page, int size) {

        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<Note> notePage = noteRepository.findByUser(user, pageable);

        return notePage.getContent().stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .toList();
    }

    // =========================
    // 🔍 SEARCH NOTES
    // =========================
    @Cacheable(
        value = "notes_search",
        key = "#root.target.getCurrentUserId() + '_' + #keyword + '_' + #page + '_' + #size"
    )
    public List<NoteDto> searchNotesByTitle(String keyword, int page, int size) {

        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        return noteRepository
                .findByUserAndTitleContainingIgnoreCase(user, keyword, pageable)
                .stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .toList();
    }

    // =========================
    // ➕ CREATE NOTE
    // =========================
    @Caching(evict = {
        @CacheEvict(value = "notes", allEntries = true),
        @CacheEvict(value = "notes_search", allEntries = true)
    })
    public NoteDto createNewNote(AddNoteDto addNoteDto) {

        User user = getCurrentUser();

        Note note = modelMapper.map(addNoteDto, Note.class);
        note.setUser(user);

        Note savedNote = noteRepository.save(note);
        return modelMapper.map(savedNote, NoteDto.class);
    }

    // =========================
    // ✏️ UPDATE NOTE
    // =========================
    @Caching(evict = {
        @CacheEvict(value = "notes", allEntries = true),
        @CacheEvict(value = "notes_search", allEntries = true)
    })
    public NoteDto updateNote(Long id, AddNoteDto addNoteDto) {

        User user = getCurrentUser();

        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        note.setTitle(addNoteDto.getTitle());
        note.setContent(addNoteDto.getContent());

        Note updatedNote = noteRepository.save(note);
        return modelMapper.map(updatedNote, NoteDto.class);
    }

    // =========================
    // ❌ DELETE NOTE
    // =========================
    @Caching(evict = {
        @CacheEvict(value = "notes", allEntries = true),
        @CacheEvict(value = "notes_search", allEntries = true)
    })
    public void deleteNote(Long id) {

        User user = getCurrentUser();

        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        noteRepository.delete(note);
    }
}