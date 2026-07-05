package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.items
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsManager = remember { SettingsManager.getInstance(applicationContext) }
            val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle()
            val language by settingsManager.language.collectAsStateWithLifecycle()
            val fontSize by settingsManager.fontSize.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            val layoutDirection = if (language == "Arabic") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
            
            val baseTypography = com.example.ui.theme.Typography
            val scaledTypography = when (fontSize) {
                "Small" -> androidx.compose.material3.Typography(
                    bodyLarge = baseTypography.bodyLarge.copy(fontSize = 14.sp),
                    bodyMedium = baseTypography.bodyMedium.copy(fontSize = 12.sp),
                    titleLarge = baseTypography.titleLarge.copy(fontSize = 20.sp)
                )
                "Large" -> androidx.compose.material3.Typography(
                    bodyLarge = baseTypography.bodyLarge.copy(fontSize = 20.sp),
                    bodyMedium = baseTypography.bodyMedium.copy(fontSize = 18.sp),
                    titleLarge = baseTypography.titleLarge.copy(fontSize = 28.sp)
                )
                else -> baseTypography
            }

            com.example.ui.theme.MyApplicationTheme(darkTheme = isDarkTheme) {
                androidx.compose.material3.MaterialTheme(typography = scaledTypography) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection
                    ) {
                        val viewModel: com.example.viewmodel.NoteViewModel = viewModel()
                        AppNavigation(viewModel)
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    data class NoteDetail(val note: com.example.model.Note) : Screen()
    object AddNote : Screen()
    object CodeEditor : Screen()
    object Settings : Screen()
}

@Composable
fun AppNavigation(viewModel: com.example.viewmodel.NoteViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    androidx.compose.animation.AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState is Screen.Home && initialState is Screen.Splash) {
                androidx.compose.animation.fadeIn(animationSpec = tween(800)) togetherWith androidx.compose.animation.fadeOut(animationSpec = tween(800))
            } else if (targetState is Screen.Home) {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { -it }, animationSpec = tween(400)
                ) + androidx.compose.animation.fadeIn(animationSpec = tween(400)) togetherWith
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { it }, animationSpec = tween(400)
                ) + androidx.compose.animation.fadeOut(animationSpec = tween(400))
            } else {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { it }, animationSpec = tween(400)
                ) + androidx.compose.animation.fadeIn(animationSpec = tween(400)) togetherWith
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { -it }, animationSpec = tween(400)
                ) + androidx.compose.animation.fadeOut(animationSpec = tween(400))
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen { currentScreen = Screen.Home }
            is Screen.Home -> DeveloperNotesApp(
                viewModel, 
                onNoteClick = { currentScreen = Screen.NoteDetail(it) },
                onAddClick = { currentScreen = Screen.AddNote },
                onEditorClick = { currentScreen = Screen.CodeEditor },
                onSettingsClick = { currentScreen = Screen.Settings }
            )
            is Screen.NoteDetail -> NoteDetailScreen(
                note = screen.note,
                onBack = { currentScreen = Screen.Home },
                onDelete = {
                    viewModel.deleteNote(screen.note)
                    currentScreen = Screen.Home
                }
            )
            is Screen.AddNote -> AddNoteScreen(
                onSave = { note -> 
                    viewModel.addNote(note)
                    currentScreen = Screen.Home 
                },
                onCancel = { currentScreen = Screen.Home }
            )
            is Screen.CodeEditor -> CodeEditorScreen(
                onBack = { currentScreen = Screen.Home }
            )
            is Screen.Settings -> SettingsScreen(
                viewModel = viewModel,
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
}

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        restartOnPlay = false
    )

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = alphaAnim.value),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun DeveloperNotesApp(
    viewModel: com.example.viewmodel.NoteViewModel, 
    onNoteClick: (com.example.model.Note) -> Unit,
    onAddClick: () -> Unit,
    onEditorClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { GlassBottomBar(onEditorClick, onSettingsClick) },
        floatingActionButton = { 
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                DeveloperFab(onAddClick) 
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        DeveloperContent(
            modifier = Modifier.padding(innerPadding),
            isVisible = isVisible,
            notes = notes,
            searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle().value,
            filterState = viewModel.filterState.collectAsStateWithLifecycle().value,
            onSearch = { viewModel.search(it) },
            onFilterSelected = { viewModel.setFilter(it) },
            onNoteClick = onNoteClick
        )
    }
}

@Composable
fun GlassBottomBar(onEditorClick: () -> Unit, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) // Glassmorphism effect
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavIcon(Icons.Filled.Home, "Home", selected = true)
            BottomNavIcon(Icons.AutoMirrored.Outlined.List, "Notes", selected = false)
            Spacer(modifier = Modifier.width(48.dp)) // Space for FAB
            BottomNavIcon(Icons.Outlined.Menu, "Editor", selected = false, onClick = onEditorClick)
            BottomNavIcon(Icons.Outlined.Settings, "Settings", selected = false, onClick = onSettingsClick)
        }
    }
}

@Composable
fun BottomNavIcon(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeveloperFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .offset(y = 36.dp)
            .size(60.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(Icons.Filled.Add, contentDescription = "New Note", modifier = Modifier.size(28.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperContent(
    modifier: Modifier = Modifier, 
    isVisible: Boolean,
    notes: List<com.example.model.Note>,
    searchQuery: String,
    filterState: String,
    onSearch: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onNoteClick: (com.example.model.Note) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedHeader()
        }
        item {
            SearchBar(searchQuery, onSearch)
        }
        item {
            FilterChipsRow(filterState, onFilterSelected)
        }
        
        if (notes.isEmpty()) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    ) + fadeIn()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp).animateItem(), contentAlignment = Alignment.Center) {
                        Text("No notes found. Create a new one!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(items = notes, key = { it.id }) { note ->
                val index = notes.indexOf(note)
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 100 + (index * 50) },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    ) + fadeIn(),
                    modifier = Modifier.animateItem()
                ) {
                    if (note.isPinned) {
                        CodeSnippetCard(note, onNoteClick)
                    } else {
                        RepoStyleCard(note, onNoteClick)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AnimatedHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "{ }",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column {
                Text(
                    text = "Code Notes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Welcome back, Dev",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(searchQuery: String, onSearch: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = { 
            onSearch(it)
        },
        placeholder = { Text("Search repositories, notes, snippets...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(filterState: String, onFilterSelected: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val filters = listOf("All", "Favorites", "Kotlin", "Java", "Python", "JavaScript", "C++")
        items(filters.size) { index ->
            val filter = filters[index]
            val isSelected = filter == filterState
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
fun RepoStyleCard(note: com.example.model.Note, onNoteClick: (com.example.model.Note) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable { onNoteClick(note) }
            .animateContentSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.List, contentDescription = "Repo", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Public", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = note.content,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFA97BFF)))
                Text(note.language, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Updated just now", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CodeSnippetCard(note: com.example.model.Note, onNoteClick: (com.example.model.Note) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable { onNoteClick(note) }
            .animateContentSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Outlined.Menu, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        // Code Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E)) // VS Code style code block
                .padding(16.dp)
        ) {
            Text(
                text = buildSyntaxHighlightedString(note.content),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

fun buildSyntaxHighlightedString(code: String): androidx.compose.ui.text.AnnotatedString {
    val keywordColor = Color(0xFFC678DD)
    val stringColor = Color(0xFF98C379)
    val commentColor = Color(0xFF5C6370)
    val functionColor = Color(0xFF61AFEF)
    val numberColor = Color(0xFFD19A66)
    val defaultColor = Color(0xFFABB2BF)

    return androidx.compose.ui.text.buildAnnotatedString {
        append(code)
        val keywords = listOf("val", "var", "fun", "class", "interface", "package", "import", "abstract", "public", "private", "protected", "if", "else", "for", "while", "return", "true", "false", "null", "suspend", "override", "val", "data", "object")
        
        val keywordPattern = "\\b(${keywords.joinToString("|")})\\b".toRegex()
        val stringPattern = "\".*?\"".toRegex()
        val commentPattern = "//.*".toRegex()
        val functionPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()".toRegex()
        val numberPattern = "\\b\\d+\\b".toRegex()
        val annotationPattern = "@[a-zA-Z_][a-zA-Z0-9_]*".toRegex()

        addStyle(androidx.compose.ui.text.SpanStyle(color = defaultColor), 0, code.length)

        functionPattern.findAll(code).forEach { match ->
            addStyle(androidx.compose.ui.text.SpanStyle(color = functionColor), match.range.first, match.range.last + 1)
        }
        keywordPattern.findAll(code).forEach { match ->
            addStyle(androidx.compose.ui.text.SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        numberPattern.findAll(code).forEach { match ->
            addStyle(androidx.compose.ui.text.SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
        }
        annotationPattern.findAll(code).forEach { match ->
            addStyle(androidx.compose.ui.text.SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
        }
        stringPattern.findAll(code).forEach { match ->
            addStyle(androidx.compose.ui.text.SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
        }
        commentPattern.findAll(code).forEach { match ->
            addStyle(androidx.compose.ui.text.SpanStyle(color = commentColor), match.range.first, match.range.last + 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: com.example.model.Note,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Note")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = note.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(note.language, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text("Updated just now", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.1f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = buildSyntaxHighlightedString(note.content),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp
                )
            }
        }
    }
}


