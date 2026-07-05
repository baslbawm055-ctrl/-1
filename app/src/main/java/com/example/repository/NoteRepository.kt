package com.example.repository

import com.example.database.NoteDao
import com.example.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun searchAndFilterNotes(query: String, filter: String): Flow<List<Note>> {
        return noteDao.searchAndFilterNotes(query, filter)
    }

    suspend fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun update(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }
}
