package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.DoubtEntity
import com.example.data.remote.TeacherResponse
import com.example.ui.AskUiState
import com.example.ui.TutorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainTutorScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // State flows from viewmodel
    val studentClass by viewModel.studentClass.collectAsStateWithLifecycle()
    val board by viewModel.board.collectAsStateWithLifecycle()
    val subject by viewModel.subject.collectAsStateWithLifecycle()
    val questionText by viewModel.questionText.collectAsStateWithLifecycle()
    val isHinglish by viewModel.isHinglish.collectAsStateWithLifecycle()
    val askUiState by viewModel.askUiState.collectAsStateWithLifecycle()
    val selectedDoubt by viewModel.selectedDoubt.collectAsStateWithLifecycle()
    val historyDoubts by viewModel.historyDoubts.collectAsStateWithLifecycle()
    val bookmarkedDoubts by viewModel.bookmarkedDoubts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Local UI screens
    var activeTab by remember { mutableStateOf("ask") } // "ask" or "revision"
    var showClassDropdown by remember { mutableStateOf(false) }
    var showBoardDropdown by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }

    // Constants
    val grades = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
    val boards = listOf("CBSE", "ICSE / ISC", "State Board")
    val subjects = listOf("Science", "Mathematics", "Social Science", "English", "Physics", "Chemistry", "Biology")

    val sampleDoubts = listOf(
        "Draw a labeled diagram of a Plant Cell and explain the vacuole function.",
        "Prove that √5 is an irrational number step-by-step.",
        "Discuss the causes of the Revolt of 1857 in a point-wise format.",
        "Explain Ohm's Law and solve: output resistance if V = 12V, I = 3A."
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFD1E4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "GuruJi Icon",
                            tint = Color(0xFF001D36),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GuruJi: AI Exam Tutor",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "15+ Years CBSE/ICSE Board Teacher Experience",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFE1E2E9), thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "ask",
                    onClick = { activeTab = "ask" },
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "Ask GuruJi") },
                    label = { Text("Ask Doubt") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "revision",
                    onClick = { activeTab = "revision" },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Revision notebook") },
                    label = { Text("Revision Book") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "progress",
                    onClick = { activeTab = "progress" },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Progress and Analytics") },
                    label = { Text("Progress") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (activeTab == "ask") {
                // Outer column with scroll modifier is NOT added directly; we use LazyColumn or structured components to prevent nested scroll crashes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Display Hero Banner Image
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_hero_banner),
                                    contentDescription = "GuruJi Classroom Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                            )
                                        )
                                )
                                Text(
                                    text = "Score Full Marks in Boards & Exams!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                )
                            }
                        }
                    }

                    item {
                        // Class/Board selectors
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Select Student & Exam Details",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Class Selector
                                    Box {
                                        Button(
                                            onClick = { showClassDropdown = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("class_dropdown_btn")
                                        ) {
                                            Text(text = studentClass, fontSize = 13.sp)
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(
                                            expanded = showClassDropdown,
                                            onDismissRequest = { showClassDropdown = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            grades.forEach { grade ->
                                                DropdownMenuItem(
                                                    text = { Text(grade) },
                                                    onClick = {
                                                        viewModel.setStudentClass(grade)
                                                        showClassDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Board Selector
                                    Box {
                                        Button(
                                            onClick = { showBoardDropdown = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("board_dropdown_btn")
                                        ) {
                                            Text(text = board, fontSize = 13.sp)
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(
                                            expanded = showBoardDropdown,
                                            onDismissRequest = { showBoardDropdown = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            boards.forEach { b ->
                                                DropdownMenuItem(
                                                    text = { Text(b) },
                                                    onClick = {
                                                        viewModel.setBoard(b)
                                                        showBoardDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Subject Selector
                                    Box {
                                        Button(
                                            onClick = { showSubjectDropdown = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                                contentColor = MaterialTheme.colorScheme.tertiary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("subject_dropdown_btn")
                                        ) {
                                            Text(text = subject, fontSize = 13.sp)
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(
                                            expanded = showSubjectDropdown,
                                            onDismissRequest = { showSubjectDropdown = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            subjects.forEach { s ->
                                                DropdownMenuItem(
                                                    text = { Text(s) },
                                                    onClick = {
                                                        viewModel.setSubject(s)
                                                        showSubjectDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Explain in Hinglish Toggle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                        .clickable { viewModel.toggleHinglish() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Hinglish Toggle",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Explain in Hinglish (Hindi + English)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Using conversational Hinglish script for easy learning",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                    Switch(
                                        checked = isHinglish,
                                        onCheckedChange = { viewModel.toggleHinglish() },
                                        modifier = Modifier.testTag("hinglish_switch")
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Ask GuruJi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Type your science concept, history question, or math problem of $studentClass below:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.61f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Question block input
                                OutlinedTextField(
                                    value = questionText,
                                    onValueChange = { viewModel.setQuestionText(it) },
                                    placeholder = { Text("What is your inquiry, Beta?") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("question_input_field"),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            focusManager.clearFocus()
                                            viewModel.submitQuestion()
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick doubts list
                                Text(
                                    text = "💡 Tap a suggestion to fill instantly:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    sampleDoubts.forEach { sampleText ->
                                        SuggestionChip(
                                            onClick = { viewModel.setQuestionText(sampleText) },
                                            label = {
                                                Text(
                                                    text = sampleText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            modifier = Modifier.testTag("suggestion_chip_${sampleText.take(5)}")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Main submit button
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.submitQuestion()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_question_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = questionText.isNotBlank() && askUiState != AskUiState.Loading
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Solve My Doubt, GuruJi!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Content based on response state
                    item {
                        when (val state = askUiState) {
                            is AskUiState.Loading -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(50.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "GuruJi is explaining step-by-step...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "बीटा, उत्तर तैयार हो रहा है... Best study tips are loading! Wait for 15+ Yrs Board Expert secrets.",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            is AskUiState.Success -> {
                                TeacherResponseSection(
                                    response = state.response,
                                    questionText = questionText,
                                    isSavedState = selectedDoubt != null,
                                    doubtReference = selectedDoubt,
                                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                                    onReset = { viewModel.resetState() }
                                )
                            }
                            is AskUiState.Error -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error icon",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "API Alert",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.submitQuestion() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Retry Beta", color = Color.White)
                                        }
                                    }
                                }
                            }
                            is AskUiState.Idle -> {
                                // Encouraging educational greeting
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Guidance icon",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "GuruJi's Pedagogical Methodology",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "✓ Real Concept building over simple mug-up.\n✓ Pointwise examiner-friendly scoring patterns.\n✓ Highlighting Silly Mistakes to prevent loss of marks.\n✓ Authentic explanations supporting CBSE marking schemes.",
                                            style = MaterialTheme.typography.bodySmall,
                                            lineHeight = 20.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            } else if (activeTab == "revision") {
                // Revision Book View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your Revision Notebook",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Review solved questions & bookmarks to score high during revision loops.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search doubts or matching formulas...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_history_input"),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bookmark vs ALL Tabs
                    var filterBookmarkedOnly by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabRow(
                            selectedTabIndex = if (filterBookmarkedOnly) 1 else 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            containerColor = Color.Transparent,
                            divider = {}
                        ) {
                            Tab(
                                selected = !filterBookmarkedOnly,
                                onClick = { filterBookmarkedOnly = false },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("All (${historyDoubts.size})", fontSize = 13.sp)
                                }
                            }
                            Tab(
                                selected = filterBookmarkedOnly,
                                onClick = { filterBookmarkedOnly = true },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bookmarks (${bookmarkedDoubts.size})", fontSize = 13.sp)
                                }
                            }
                        }

                        if (historyDoubts.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearAllHistory() },
                                modifier = Modifier.testTag("clear_history_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Clear notebook",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val activeList = if (filterBookmarkedOnly) bookmarkedDoubts else historyDoubts

                    if (activeList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (filterBookmarkedOnly) Icons.Default.FavoriteBorder else Icons.Default.MenuBook,
                                    contentDescription = "Empty book outline",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(70.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (filterBookmarkedOnly) "No bookmarked formulas or solutions!" else "Revision notebook is blank, Beta!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (filterBookmarkedOnly) "Tap the heart icon on any solution to bookmark core explanations." else "Ask GuruJi some math or science questions! Every asked doubt automatically saves here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(activeList) { doubt ->
                                SavedDoubtItemCard(
                                    doubt = doubt,
                                    onSelect = {
                                        viewModel.selectDoubt(doubt)
                                        activeTab = "ask" // Swap back to ask layout to show results
                                    },
                                    onDelete = { viewModel.deleteDoubt(doubt.id) },
                                    onToggleBookmark = { viewModel.toggleBookmark(doubt) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            } else {
                // Progress Dashboard View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your Progress Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Track solved topics, milestones, consistency, and personalized exam-readiness stats.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProgressDashboard(
                        historyDoubts = historyDoubts,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedDoubtItemCard(
    doubt: DoubtEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTime = remember(doubt.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(doubt.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("doubt_item_${doubt.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = doubt.studentClass,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = doubt.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = doubt.board,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (doubt.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = if (doubt.isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question
            Text(
                text = doubt.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Concept preview snippet
            Text(
                text = doubt.conceptExplanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hinglish badge if enabled
                if (doubt.isHinglish) {
                    Text(
                        text = "Hinglish Expl.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun TeacherResponseSection(
    response: TeacherResponse,
    questionText: String,
    isSavedState: Boolean,
    doubtReference: DoubtEntity?,
    onToggleBookmark: (DoubtEntity) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubTab by remember(response) { mutableStateOf("concept") } // "concept", "steps", "final", "tips", "mistakes"

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("result_display_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Result Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Success Star",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GuruJi's Solid Explanation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (doubtReference != null) {
                        IconButton(
                            onClick = { onToggleBookmark(doubtReference) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (doubtReference.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark",
                                tint = if (doubtReference.isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Reset / New Doubt trigger
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(32.dp).testTag("close_results_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reset doubt flow",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Question context box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "STUDENT'S DOUBT:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = questionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Tabs for structured sections
            ScrollableTabRow(
                selectedTabIndex = when (activeSubTab) {
                    "concept" -> 0
                    "steps" -> 1
                    "final" -> 2
                    "tips" -> 3
                    "mistakes" -> 4
                    else -> 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                containerColor = Color.Transparent,
                divider = {},
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = activeSubTab == "concept",
                    onClick = { activeSubTab = "concept" },
                    modifier = Modifier.testTag("tab_concept")
                ) {
                    Text("💡 Concept", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Tab(
                    selected = activeSubTab == "steps",
                    onClick = { activeSubTab = "steps" },
                    modifier = Modifier.testTag("tab_steps")
                ) {
                    Text("📝 Steps", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Tab(
                    selected = activeSubTab == "final",
                    onClick = { activeSubTab = "final" },
                    modifier = Modifier.testTag("tab_final")
                ) {
                    Text("🎯 Final Ans", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Tab(
                    selected = activeSubTab == "tips",
                    onClick = { activeSubTab = "tips" },
                    modifier = Modifier.testTag("tab_tips")
                ) {
                    Text("🌟 Tips/Keys", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Tab(
                    selected = activeSubTab == "mistakes",
                    onClick = { activeSubTab = "mistakes" },
                    modifier = Modifier.testTag("tab_mistakes")
                ) {
                    Text("⚠️ Mistakes", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Output block with formatted data which can be copied!
            val displayText = when (activeSubTab) {
                "concept" -> response.conceptExplanation
                "steps" -> response.stepByStepAnswer
                "final" -> response.finalAnswer
                "tips" -> response.examTips
                "mistakes" -> response.commonMistakes
                else -> response.conceptExplanation
            }

            val styleBgColor: Color
            val styleBorderColor: Color
            val styleTextColor: Color
            val styleHeaderColor: Color
            val styleTitle: String
            val styleIcon: androidx.compose.ui.graphics.vector.ImageVector

            when (activeSubTab) {
                "concept" -> {
                    styleBgColor = Color(0xFFF0F4F8)
                    styleBorderColor = Color(0xFFD1E4FF)
                    styleTextColor = Color(0xFF1A1C1E)
                    styleHeaderColor = Color(0xFF0061A4)
                    styleTitle = "CONCEPT EXPLANATION"
                    styleIcon = Icons.Default.Lightbulb
                }
                "steps" -> {
                    styleBgColor = Color(0xFFFFFFFF)
                    styleBorderColor = Color(0xFFE1E2E9)
                    styleTextColor = Color(0xFF1A1C1E)
                    styleHeaderColor = Color(0xFF44474E)
                    styleTitle = "STEP-BY-STEP ANSWER"
                    styleIcon = Icons.Default.List
                }
                "final" -> {
                    styleBgColor = Color(0xFFF8F9FF)
                    styleBorderColor = Color(0xFFD1E4FF)
                    styleTextColor = Color(0xFF1A1C1E)
                    styleHeaderColor = Color(0xFF0061A4)
                    styleTitle = "FINAL ANSWER SUMMARY"
                    styleIcon = Icons.Default.CheckCircle
                }
                "tips" -> {
                    styleBgColor = Color(0xFFE8F5E9)
                    styleBorderColor = Color(0xFFC8E6C9)
                    styleTextColor = Color(0xFF166534)
                    styleHeaderColor = Color(0xFF15803D)
                    styleTitle = "EXAM TIPS & SCORE BOOSTING KEYWORDS"
                    styleIcon = Icons.Default.WorkspacePremium
                }
                "mistakes" -> {
                    styleBgColor = Color(0xFFFFEBEE)
                    styleBorderColor = Color(0xFFFFCDD2)
                    styleTextColor = Color(0xFF991B1B)
                    styleHeaderColor = Color(0xFFB91C1C)
                    styleTitle = "COMMON MISTAKES & HOW TO AVOID THEM"
                    styleIcon = Icons.Default.Warning
                }
                else -> {
                    styleBgColor = Color(0xFFF0F4F8)
                    styleBorderColor = Color(0xFFD1E4FF)
                    styleTextColor = Color(0xFF1A1C1E)
                    styleHeaderColor = Color(0xFF0061A4)
                    styleTitle = "CONCEPT EXPLANATION"
                    styleIcon = Icons.Default.Lightbulb
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(styleBgColor, RoundedCornerShape(12.dp))
                    .border(1.dp, styleBorderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = styleIcon,
                            contentDescription = null,
                            tint = styleHeaderColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = styleTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = styleHeaderColor,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = styleBorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = displayText,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = styleTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Copy, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val fullAnswerToCopy = """
                            GURUJI EXAM TUTOR ANSWER:
                            
                            1. Core Concept:
                            ${response.conceptExplanation}
                            
                            2. Solved Steps:
                            ${response.stepByStepAnswer}
                            
                            3. Final Answer:
                            ${response.finalAnswer}
                            
                            4. Score Booster Tips / Keywords:
                            ${response.examTips}
                            
                            5. Silly Mistakes to Avoid:
                            ${response.commonMistakes}
                        """.trimIndent()
                        val clip = ClipData.newPlainText("GuruJi Tutor Solution", fullAnswerToCopy)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full solution copied to Revision Clipboard, Beta!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("copy_answer_btn")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Code/Full Answer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Share status note
                Text(
                    text = if (isSavedState) "✓ Revision History Saved" else "GuruJi solved instantly",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
