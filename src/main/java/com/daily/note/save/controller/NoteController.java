package com.daily.note.save.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daily.note.save.dto.AddNoteDto;
import com.daily.note.save.dto.NoteDto;
import com.daily.note.save.service.NoteService;

import lombok.RequiredArgsConstructor;



import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    @GetMapping
    public ResponseEntity<List<NoteDto>> getAllNote(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(noteService.getNotes(page, size));
    }
    @GetMapping("/search")
    public ResponseEntity<List<NoteDto>> searchNotes(@RequestParam String title,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(noteService.searchNotesByTitle(title, page, size));
    }
    @PostMapping
    public ResponseEntity<NoteDto> createNewNote(@RequestBody AddNoteDto addNoteDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNewNote(addNoteDto));
    }
    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long id, @RequestBody AddNoteDto addNoteDto) {
        return ResponseEntity.ok(noteService.updateNote(id, addNoteDto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<NoteDto> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}