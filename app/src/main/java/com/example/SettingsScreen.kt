package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Note
import com.example.viewmodel.NoteViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: NoteViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    
    val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle()
    val language by settingsManager.language.collectAsStateWithLifecycle()
    val fontSize by settingsManager.fontSize.collectAsStateWithLifecycle()
    
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf<String?>(null) }
    var importText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCategory("Appearance")
            SettingsItem(
                icon = Icons.Outlined.DarkMode,
                title = "Theme",
                subtitle = themeMode,
                onClick = { 
                    val modes = listOf("System", "Light", "Dark")
                    settingsManager.setThemeMode(modes[(modes.indexOf(themeMode) + 1) % modes.size])
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Language,
                title = "Language",
                subtitle = language,
                onClick = { 
                    settingsManager.setLanguage(if (language == "English") "Arabic" else "English")
                }
            )
            SettingsItem(
                icon = Icons.Outlined.FormatSize,
                title = "Font Size",
                subtitle = fontSize,
                onClick = { 
                    val sizes = listOf("Small", "Medium", "Large")
                    settingsManager.setFontSize(sizes[(sizes.indexOf(fontSize) + 1) % sizes.size])
                }
            )

            HorizontalDivider()

            SettingsCategory("Data & Storage")
            SettingsItem(
                icon = Icons.Outlined.Backup,
                title = "Backup",
                subtitle = "Backup your notes to local storage",
                onClick = { 
                    try {
                        val file = File(context.filesDir, "backup.json")
                        file.writeText(notesToJson(notes))
                        android.widget.Toast.makeText(context, "Backed up to ${file.absolutePath}", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Backup failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Restore,
                title = "Restore",
                subtitle = "Restore notes from local backup",
                onClick = { 
                    try {
                        val file = File(context.filesDir, "backup.json")
                        if (file.exists()) {
                            val importedNotes = jsonToNotes(file.readText())
                            importedNotes.forEach { viewModel.addNote(it) }
                            android.widget.Toast.makeText(context, "Restored ${importedNotes.size} notes", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "No backup found", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Restore failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
            SettingsItem(
                icon = Icons.Outlined.ImportExport,
                title = "Export Notes",
                subtitle = "Export to JSON and share",
                onClick = { 
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, notesToJson(notes))
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export Notes"))
                }
            )
            SettingsItem(
                icon = Icons.Outlined.Download,
                title = "Import Notes",
                subtitle = "Import from JSON text",
                onClick = { showDialog = "Import" }
            )

            HorizontalDivider()

            SettingsCategory("About")
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "About",
                subtitle = "Code Notes Version 1.0.0",
                onClick = { showDialog = "About" }
            )
            SettingsItem(
                icon = Icons.Outlined.Policy,
                title = "Privacy Policy",
                subtitle = "View our privacy policy",
                onClick = { showDialog = "Privacy" }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDialog == "Import") {
            AlertDialog(
                onDismissRequest = { showDialog = null },
                title = { Text("Import Notes JSON") },
                text = {
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        label = { Text("Paste JSON here") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        try {
                            val imported = jsonToNotes(importText)
                            imported.forEach { viewModel.addNote(it) }
                            android.widget.Toast.makeText(context, "Imported ${imported.size} notes", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Invalid JSON", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showDialog = null
                    }) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = null }) { Text("Cancel") }
                }
            )
        } else if (showDialog == "About") {
            AlertDialog(
                onDismissRequest = { showDialog = null },
                title = { Text("About") },
                text = { Text("Code Notes is a beautiful developer productivity app built with Jetpack Compose and Room Database. Created by an AI.") },
                confirmButton = { TextButton(onClick = { showDialog = null }) { Text("OK") } }
            )
        } else if (showDialog == "Privacy") {
            AlertDialog(
                onDismissRequest = { showDialog = null },
                title = { Text("Privacy Policy") },
                text = { Text("All your data is stored locally on your device. We do not collect or share any personal information.") },
                confirmButton = { TextButton(onClick = { showDialog = null }) { Text("Close") } }
            )
        }
    }
}

fun notesToJson(notes: List<Note>): String {
    val array = JSONArray()
    notes.forEach { n ->
        val obj = JSONObject()
        obj.put("id", n.id)
        obj.put("title", n.title)
        obj.put("content", n.content)
        obj.put("language", n.language)
        obj.put("isPinned", n.isPinned)
        obj.put("timestamp", n.timestamp)
        obj.put("tags", n.tags)
        obj.put("explanation", n.explanation)
        obj.put("referenceLink", n.referenceLink)
        obj.put("difficulty", n.difficulty)
        obj.put("colorHex", n.colorHex)
        obj.put("attachments", n.attachments)
        array.put(obj)
    }
    return array.toString(4)
}

fun jsonToNotes(jsonStr: String): List<Note> {
    val array = JSONArray(jsonStr)
    val list = mutableListOf<Note>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(Note(
            id = if (obj.has("id")) obj.getInt("id") else 0,
            title = obj.getString("title"),
            content = obj.getString("content"),
            language = obj.getString("language"),
            isPinned = obj.getBoolean("isPinned"),
            timestamp = obj.getLong("timestamp"),
            tags = obj.optString("tags", ""),
            explanation = obj.optString("explanation", ""),
            referenceLink = obj.optString("referenceLink", ""),
            difficulty = obj.optString("difficulty", "Beginner"),
            colorHex = obj.optString("colorHex", "#1E293B"),
            attachments = obj.optString("attachments", "")
        ))
    }
    return list
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
