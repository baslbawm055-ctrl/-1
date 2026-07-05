package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    onBack: () -> Unit,
    initialCode: String = "",
    initialLanguage: String = "Java"
) {
    var code by remember { mutableStateOf(initialCode) }
    var language by remember { mutableStateOf(initialLanguage) }
    var isPreviewMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val languages = listOf("Java", "Kotlin", "Python", "JavaScript", "C++", "Dart", "XML", "JSON", "Markdown")

    // Auto Indentation logic
    fun onCodeChange(newCode: String) {
        if (newCode.length > code.length && newCode.lastOrNull() == '\n') {
            val lines = code.split("\n")
            val lastLine = if (lines.size >= 2) lines[lines.size - 2] else ""
            val indentation = lastLine.takeWhile { it.isWhitespace() }
            val extraIndent = if (lastLine.trimEnd().endsWith("{")) "    " else ""
            code = newCode + indentation + extraIndent
        } else {
            code = newCode
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Code Editor", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Code", code)
                        clipboard.setPrimaryClip(clip)
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.primaryClip?.getItemAt(0)?.text?.let {
                            code += it.toString()
                        }
                    }) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Paste")
                    }
                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, code)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Code"))
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isPreviewMode = !isPreviewMode },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(if (isPreviewMode) Icons.Filled.Edit else Icons.Filled.PlayArrow, contentDescription = "Toggle Mode")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(languages.size) { index ->
                    FilterChip(
                        selected = language == languages[index],
                        onClick = { language = languages[index] },
                        label = { Text(languages[index]) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                AnimatedContent(targetState = isPreviewMode, label = "ModeSwitch") { preview ->
                    if (preview) {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text(
                                text = "Preview Mode",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = code,
                                color = Color(0xFFD4D4D4),
                                fontFamily = FontFamily.Default
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Line Numbers
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(Color(0xFF252526))
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                val lineCount = code.count { it == '\n' } + 1
                                for (i in 1..lineCount) {
                                    Text(
                                        text = i.toString(),
                                        color = Color(0xFF858585),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                            
                            // Editor
                            BasicTextField(
                                value = code,
                                onValueChange = { onCodeChange(it) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                textStyle = TextStyle(
                                    color = Color(0xFFD4D4D4),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 20.sp
                                ),
                                cursorBrush = SolidColor(Color.White),
                                visualTransformation = CodeVisualTransformation()
                            )
                        }
                    }
                }
            }
        }
    }
}

class CodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            buildSyntaxHighlightedString(text.text),
            OffsetMapping.Identity
        )
    }
}
