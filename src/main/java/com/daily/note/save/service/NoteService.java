package com.daily.note.save.service;

import java.util.List;

import org.modelmapper.ModelMapper;
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

    private User getCurrentUser() {
        return (User) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

    public List<NoteDto> getNotes() {
        List<Note> notes = noteRepository.findByUser(getCurrentUser());
        return notes.stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .toList();
    }

    public List<NoteDto> searchNotesByTitle(String keyword) {
        User user = getCurrentUser();

        return noteRepository
                .findByUserAndTitleContainingIgnoreCase(user, keyword)
                .stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .toList();
    }

    public NoteDto createNewNote(AddNoteDto addNoteDto) {
        User user = getCurrentUser();
        Note note = modelMapper.map(addNoteDto, Note.class);
        note.setUser(user);
        Note savedNote = noteRepository.save(note);
        return modelMapper.map(savedNote, NoteDto.class);
    }

    public NoteDto updateNote(Long id, AddNoteDto addNoteDto) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        note.setTitle(addNoteDto.getTitle());
        note.setContent(addNoteDto.getContent());
        Note updatedNote = noteRepository.save(note);
        return modelMapper.map(updatedNote, NoteDto.class);
    }

    public void deleteNote(Long id) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        noteRepository.delete(note);
    }

}
