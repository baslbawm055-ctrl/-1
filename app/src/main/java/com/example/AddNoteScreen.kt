package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    onSave: (Note) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Kotlin") }
    var tags by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var referenceLink by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Beginner") }
    var isPinned by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf("#1E293B") }
    var attachments by remember { mutableStateOf("") }

    val colors = listOf("#1E293B", "#8B5CF6", "#3B82F6", "#10B981", "#F59E0B", "#EF4444")
    val difficulties = listOf("Beginner", "Intermediate", "Advanced")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Snippet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            if (isPinned) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isPinned) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    Note(
                                        title = title,
                                        content = content,
                                        language = language,
                                        tags = tags,
                                        explanation = explanation,
                                        referenceLink = referenceLink,
                                        difficulty = difficulty,
                                        isPinned = isPinned,
                                        colorHex = colorHex,
                                        attachments = attachments
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("Difficulty", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                difficulties.forEach { diff ->
                    val selected = diff == difficulty
                    FilterChip(
                        selected = selected,
                        onClick = { difficulty = diff },
                        label = { Text(diff) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Text("Card Color", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (colorHex == hex) 3.dp else 0.dp,
                                color = if (colorHex == hex) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { colorHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorHex == hex) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Code Snippet") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Explanation") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = referenceLink,
                onValueChange = { referenceLink = it },
                label = { Text("Reference Link") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = attachments,
                onValueChange = { attachments = it },
                label = { Text("Attachments (URLs)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
