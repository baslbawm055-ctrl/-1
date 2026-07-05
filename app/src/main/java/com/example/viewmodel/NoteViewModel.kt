package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.model.Note
import com.example.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterState = MutableStateFlow("All")
    val filterState: StateFlow<String> = _filterState

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        
        viewModelScope.launch {
            val initialNotes = repository.allNotes.first()
            if (initialNotes.isEmpty()) {
                repository.insert(Note(title = "Room DB Repository.java", content = "@Dao\npublic interface NoteDao {\n  @Query(\"SELECT * FROM notes\")\n  List<Note> getAll();\n}", language = "Java", isPinned = true))
                repository.insert(Note(title = "MainActivity.kt", content = "class MainActivity : ComponentActivity()", language = "Kotlin", isPinned = false))
                repository.insert(Note(title = "basil/compose-ui-components", content = "A collection of beautiful, reusable Jetpack Compose components.", language = "Kotlin", isPinned = false))
            }
        }
    }

    val notes: StateFlow<List<Note>> = kotlinx.coroutines.flow.combine(_searchQuery, _filterState) { query, filter ->
        Pair(query, filter)
    }
        .flatMapLatest { (query, filter) ->
            repository.searchAndFilterNotes(query, filter)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _filterState.value = filter
    }

    fun addNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
