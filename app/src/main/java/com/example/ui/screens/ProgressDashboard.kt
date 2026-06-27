package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DoubtEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressDashboard(
    historyDoubts: List<DoubtEntity>,
    modifier: Modifier = Modifier
) {
    val totalQuestions = historyDoubts.size

    // Subjects list
    val subjects = listOf("Science", "Mathematics", "Social Science", "English", "Physics", "Chemistry", "Biology")

    // Dynamic counts
    val subjectCounts = remember(historyDoubts) {
        subjects.associateWith { sub ->
            historyDoubts.count { it.subject.equals(sub, ignoreCase = true) }
        }
    }

    val maxQuestionsInSubject = remember(subjectCounts) {
        val maxVal = subjectCounts.values.maxOrNull() ?: 0
        if (maxVal == 0) 1 else maxVal
    }

    // Dynamic Board Readiness Score out of 100
    val boardReadinessScore = remember(totalQuestions) {
        // e.g. 10 points per question, maxing at 100
        (totalQuestions * 8).coerceAtMost(100)
    }

    // Interactive selected subject for analytics tooltip
    var selectedSubjectDetails by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("progress_dashboard_scrollable"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER: Exam Readiness Tracker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE8F0FE), Color(0xFFF1F5FEB))
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BOARD EXAM READINESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0061A4),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Syllabus Mastery Index",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF001D36)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0061A4),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = if (boardReadinessScore >= 80) "EXAM READY" else "LEARNING",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress ring container
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            // Circular ProgressBar Background
                            CircularProgressIndicator(
                                progress = 1f,
                                strokeWidth = 10.dp,
                                color = Color(0xFFD1E4FF),
                                modifier = Modifier.fillMaxSize()
                            )
                            // Animated active ring
                            val animatedProgress by animateFloatAsState(
                                targetValue = boardReadinessScore / 100f,
                                animationSpec = tween(durationMillis = 1000)
                            )
                            CircularProgressIndicator(
                                progress = animatedProgress,
                                strokeWidth = 10.dp,
                                color = Color(0xFF0061A4),
                                modifier = Modifier.fillMaxSize()
                            )

                            // Inner percentage
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$boardReadinessScore%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF001D36),
                                    lineHeight = 24.sp
                               )
                                Text(
                                    text = "Ready",
                                    fontSize = 11.sp,
                                    color = Color(0xFF44474E),
                                    fontWeight = FontWeight.Medium
                               )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Stats Summary
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatRow(
                                title = "Total Doubts Resolved",
                                value = "$totalQuestions questions",
                                icon = Icons.Default.CheckCircle,
                                iconColor = Color(0xFF2E7D32)
                            )
                            StatRow(
                                title = "Subjects Covered",
                                value = "${subjectCounts.values.count { it > 0 }} active topics",
                                icon = Icons.Default.Book,
                                iconColor = Color(0xFF0061A4)
                            )
                            StatRow(
                                title = "Milestones Unlocked",
                                value = "${getUnlockedBadgesCount(totalQuestions)} / 4 awards",
                                icon = Icons.Default.WorkspacePremium,
                                iconColor = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }
        }

        // 2. MAIN CHART: Subject analytics comparative bar graph
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ANALYTICS: DOUBTS PER SUBJECT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topic Coverage Intensity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = "Tap on any bar to inspect specific study statistics.",
                        fontSize = 11.sp,
                        color = Color(0xFF44474E).copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Subject chart rendering rows (perfect fit for mobile screen)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        subjects.forEach { subject ->
                            val count = subjectCounts[subject] ?: 0
                            val fraction = count.toFloat() / maxQuestionsInSubject.toFloat()
                            val animatedFraction by animateFloatAsState(
                                targetValue = if (count == 0) 0.02f else fraction,
                                animationSpec = tween(durationMillis = 800)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedSubjectDetails = if (selectedSubjectDetails == subject) {
                                            null
                                        } else {
                                            subject
                                        }
                                    }
                                    .background(
                                        if (selectedSubjectDetails == subject) Color(0xFFF0F4F8) else Color.Transparent
                                    )
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(getSubjectColor(subject))
                                        )
                                        Text(
                                            text = subject,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedSubjectDetails == subject) FontWeight.Bold else FontWeight.Medium,
                                            color = Color(0xFF1A1C1E)
                                        )
                                    }
                                    Text(
                                        text = "$count resolved",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (count > 0) Color(0xFF0061A4) else Color(0xFF44474E).copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Horizontal Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE1E2E9))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(animatedFraction)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        getSubjectColor(subject).copy(alpha = 0.8f),
                                                        getSubjectColor(subject)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Selected subject detail tooltip card
                    AnimatedVisibility(
                        visible = selectedSubjectDetails != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        selectedSubjectDetails?.let { subject ->
                            val count = subjectCounts[subject] ?: 0
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E4FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Topic: $subject Master Plan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF001D36)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (count == 0) {
                                            "No doubts asked yet, Beta. Try launching basic NCERT textbook queries under this category or asking GuruJi for revision notes!"
                                        } else {
                                            "You have solved $count doubts in $subject. Keep reviewing saved step-by-step solutions to cement your boards exam confidence."
                                        },
                                        fontSize = 11.sp,
                                        color = Color(0xFF001D36).copy(alpha = 0.8f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. STUDY STREAK (Calendar entry)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DAILY REVISION STREAK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Consistency Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw 7 weekdays
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val calendar = Calendar.getInstance()
                        // Find current day of week (1-7, Sunday-Saturday)
                        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                        val daysLetter = listOf("S", "M", "T", "W", "T", "F", "S")

                        // For visual representation, let's say they have been active today if totalQuestions > 0,
                        // and active on a couple of other imaginary days for stellar demo representation, or track database logs
                        daysLetter.forEachIndexed { index, letter ->
                            val isToday = (index + 1) == currentDayOfWeek
                            val isActive = when {
                                isToday -> totalQuestions > 0
                                index == 1 -> totalQuestions >= 2 // Active on Monday
                                index == 3 -> totalQuestions >= 4 // Active on Wednesday
                                else -> false
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color(0xFF0061A4) else Color(0xFF44474E)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isActive -> Color(0xFF2E7D32)
                                                isToday -> Color(0xFFD1E4FF)
                                                else -> Color(0xFFF3F4F9)
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else 1.dp,
                                            color = if (isToday) Color(0xFF0061A4) else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active day",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 15}", // mock date numbers
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = if (isToday) Color(0xFF001D36) else Color(0xFF44474E).copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. UNLOCKED STUDY MILESTONES (BADGES)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACHIEVED BOARD SCHOLASTIC AWARDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF44474E),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scholar Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Badge 1: Curious Kid (Asked at least 1 question)
                        MilestoneBadgeCard(
                            title = "Curious Kid",
                            requiredQuestions = 1,
                            currentQuestions = totalQuestions,
                            iconColor = Color(0xFFA16207),
                            modifier = Modifier.weight(1f)
                        )

                        // Badge 2: Doubt Crusher (Asked at least 3 questions)
                        MilestoneBadgeCard(
                            title = "Doubt Crusher",
                            requiredQuestions = 3,
                            currentQuestions = totalQuestions,
                            iconColor = Color(0xFF4B5563),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Badge 3: Saffron Achiever (Asked at least 5 questions)
                        MilestoneBadgeCard(
                            title = "Saffron Shishya",
                            requiredQuestions = 5,
                            currentQuestions = totalQuestions,
                            iconColor = Color(0xFFEA580C),
                            modifier = Modifier.weight(1f)
                        )

                        // Badge 4: GuruJi favorite (Asked 10+ questions)
                        MilestoneBadgeCard(
                            title = "Topper Scholar",
                            requiredQuestions = 10,
                            currentQuestions = totalQuestions,
                            iconColor = Color(0xFF0D9488),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. PERSONALIZED TEACHER RECOMMENDATIONS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBFA)),
                border = BorderStroke(1.dp, Color(0xFFFFDCD3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFDCD3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFC2410C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "GuruJi's Personal Study Mentorship Advice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E2A00)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getTeacherAdvice(subjectCounts, totalQuestions),
                        fontSize = 12.sp,
                        color = Color(0xFF44474E),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MilestoneBadgeCard(
    title: String,
    requiredQuestions: Int,
    currentQuestions: Int,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val isUnlocked = currentQuestions >= requiredQuestions

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) Color(0xFFF8F9FF) else Color(0xFFF3F4F9),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) iconColor.copy(alpha = 0.5f) else Color(0xFFE1E2E9)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) iconColor.copy(alpha = 0.15f) else Color(0xFFE1E2E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) iconColor else Color(0xFF9E9E9E),
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color(0xFF1A1C1E) else Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isUnlocked) "UNLOCKED!" else "$currentQuestions / $requiredQuestions Qs",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isUnlocked) iconColor else Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatRow(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color(0xFF44474E),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 11.sp,
                color = Color(0xFF001D36),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getUnlockedBadgesCount(totalQuestions: Int): Int {
    var count = 0
    if (totalQuestions >= 1) count++
    if (totalQuestions >= 3) count++
    if (totalQuestions >= 5) count++
    if (totalQuestions >= 10) count++
    return count
}

fun getSubjectColor(subject: String): Color {
    return when (subject.lowercase()) {
        "science" -> Color(0xFF0061A4)      // Blue
        "mathematics" -> Color(0xFF0D9488) // Teal
        "social science" -> Color(0xFFD97706) // Orange/Amber
        "english" -> Color(0xFF059669)     // Green
        "physics" -> Color(0xFF7C3AED)     // Purple
        "chemistry" -> Color(0xFFDB2777)   // Pink
        "biology" -> Color(0xFFEA580C)     // Red/Orange
        else -> Color(0xFF6B7280)
    }
}

fun getTeacherAdvice(subjectCounts: Map<String, Int>, totalCount: Int): String {
    if (totalCount == 0) {
        return "Namaste, Beta! I don't see any doubts asked in your directory yet. Ask GuruJi your very first board exam question (Mathematics, Science, or English) to kickstart your personalized AI learning analytics metrics!"
    }

    val lowSubject = subjectCounts.filterValues { it == 0 }.keys.firstOrNull()
    val highSubject = subjectCounts.maxByOrNull { it.value }?.key

    return when {
        lowSubject != null -> {
            "Shaabash! You are actively resolving questions in $highSubject. But remember, to secure a perfect 100/100, we must cover all areas. I notice you haven't asked any doubts in original $lowSubject. Let's find one difficult concept or textbook problem in $lowSubject and solve it with GuruJi today, clear?"
        }
        else -> {
            "Bohot badhiya, Beta! You have asked questions across all core academic subjects! Continue reviewing the 'Revision Book' tab daily to consolidate your conceptual grasp before the exam starts."
        }
    }
}
