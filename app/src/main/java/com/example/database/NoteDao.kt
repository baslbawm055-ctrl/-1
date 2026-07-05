package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :searchQuery || '%' 
        OR content LIKE '%' || :searchQuery || '%' 
        OR tags LIKE '%' || :searchQuery || '%' 
        OR language LIKE '%' || :searchQuery || '%')
        AND (:filter = 'All' OR (:filter = 'Favorites' AND isPinned = 1) OR language = :filter)
        ORDER BY isPinned DESC, timestamp DESC
    """)
    fun searchAndFilterNotes(searchQuery: String, filter: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}
