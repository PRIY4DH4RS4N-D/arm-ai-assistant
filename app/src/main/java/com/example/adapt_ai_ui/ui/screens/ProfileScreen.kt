package com.example.adapt_ai_ui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.AdaptiveLearningEngine
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.MilestoneStatus
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import com.example.adapt_ai_ui.ui.components.AdaptGlassSurface
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptPrimaryButton
import com.example.adapt_ai_ui.ui.components.AdaptProgressIndicator
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.components.AdaptSectionHeader
import com.example.adapt_ai_ui.ui.components.AdaptSurface
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.LocalThemeManager
import com.example.adapt_ai_ui.ui.theme.ThemeMode

@Composable
fun ProfileScreen(
    onNavigateToExplore: () -> Unit = {},
    onNavigateToLesson: (() -> Unit)? = null,
    onNavigateToPractice: ((PracticeMode) -> Unit)? = null,
    onNavigateToPathway: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeManager = LocalThemeManager.current
    var localThemeMode by remember { mutableStateOf(themeManager?.currentThemeMode ?: ThemeMode.DARK) }
    val activeThemeMode = themeManager?.currentThemeMode ?: localThemeMode

    val learner = SampleData.learnerProfile
    val currentModule = SampleData.currentModule
    val todayGoal = SampleData.todayGoal
    val bookmarkedIds = SampleData.bookmarkedLessonIds
    val mistakeList = AdaptiveLearningEngine.mistakeNotebook
    val topicStats = AdaptiveLearningEngine.topicStats

    var showResetDialog by remember { mutableStateOf(false) }

    // Aggregate authentic evaluation metrics
    val totalAttempts = topicStats.values.sumOf { it.totalAttempts }
    val totalCorrect = topicStats.values.sumOf { it.correctAttempts }
    val accuracyPercent = if (totalAttempts > 0) (totalCorrect * 100 / totalAttempts) else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AdaptTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // ====================================================================
        // 1. Editorial Masthead
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth()) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 400.dp
                if (isNarrow) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LEARNER PASSPORT & ANALYTICS",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.primaryAccent
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AdaptTheme.radius.extraSmall))
                                .background(AdaptTheme.colors.secondaryAccentContainer.copy(alpha = 0.25f))
                                .border(
                                    AdaptTheme.stroke.hairline,
                                    AdaptTheme.colors.secondaryAccent.copy(alpha = 0.35f),
                                    RoundedCornerShape(AdaptTheme.radius.extraSmall)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LOCAL LEARNING ANALYTICS",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = AdaptTheme.colors.secondaryAccent
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LEARNER PASSPORT & ANALYTICS",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.primaryAccent,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AdaptTheme.radius.extraSmall))
                                .background(AdaptTheme.colors.secondaryAccentContainer.copy(alpha = 0.25f))
                                .border(
                                    AdaptTheme.stroke.hairline,
                                    AdaptTheme.colors.secondaryAccent.copy(alpha = 0.35f),
                                    RoundedCornerShape(AdaptTheme.radius.extraSmall)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LOCAL LEARNING ANALYTICS",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = AdaptTheme.colors.secondaryAccent
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Learner Profile & Telemetry",
                style = AdaptTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = AdaptTheme.colors.textPrimary
            )
            Text(
                text = "Verified on-device competency passport, study telemetry & curriculum preferences",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )
        }

        // ====================================================================
        // 2. Learner Passport Hero Identity Card
        // ====================================================================
        AdaptGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            isGlassEnabled = true,
            shape = RoundedCornerShape(AdaptTheme.radius.extraLarge),
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Identity Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with glowing monogram
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AdaptTheme.colors.primaryAccentContainer)
                            .border(
                                width = AdaptTheme.stroke.accent,
                                color = AdaptTheme.colors.primaryAccent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = learner.name.take(1).uppercase(),
                            style = AdaptTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = AdaptTheme.colors.primaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = learner.name,
                                style = AdaptTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AdaptTheme.radius.full))
                                    .background(AdaptTheme.colors.raisedSurface)
                                    .border(
                                        width = AdaptTheme.stroke.hairline,
                                        color = AdaptTheme.colors.borderSubtle,
                                        shape = RoundedCornerShape(AdaptTheme.radius.full)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = learner.skillLevel.label,
                                    style = AdaptTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = AdaptTheme.colors.primaryAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = learner.activeDomainTitle,
                            style = AdaptTheme.typography.bodyMedium,
                            color = AdaptTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Goal Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AdaptIcons.Target,
                            contentDescription = null,
                            tint = AdaptTheme.colors.secondaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Target Goal: ${learner.activeGoal}",
                            style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = AdaptTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4-Pill Telemetry Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PassportStatPill(
                        label = "STREAK",
                        value = "1d (Today)",
                        modifier = Modifier.weight(1f)
                    )
                    PassportStatPill(
                        label = "TODAY",
                        value = "${learner.dailyMinutesSpent}/${learner.dailyMinutesGoal}m",
                        modifier = Modifier.weight(1f)
                    )
                    PassportStatPill(
                        label = "MASTERY",
                        value = "🎯 ${learner.masteryScorePercent}%",
                        modifier = Modifier.weight(1f)
                    )
                    PassportStatPill(
                        label = "WEEKLY",
                        value = "—",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ====================================================================
        // 3. Learning Analytics Dashboard
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth()) {
            AdaptSectionHeader(
                title = "Competency & Telemetry",
                overline = "AUTHENTIC EVALUATION METRICS"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Card Analytics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricCard(
                    title = "OVERALL MASTERY",
                    value = "${learner.masteryScorePercent}%",
                    subtitle = AdaptiveLearningEngine.getMasteryLevelLabel(
                        masteryScorePercent = learner.masteryScorePercent,
                        totalAttempts = totalAttempts
                    ),
                    accentColor = AdaptTheme.colors.primaryAccent,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsMetricCard(
                    title = "ACCURACY",
                    value = if (totalAttempts > 0) "$accuracyPercent%" else "N/A",
                    subtitle = if (totalAttempts > 0) "$totalCorrect of $totalAttempts correct" else "No questions yet",
                    accentColor = AdaptTheme.colors.secondaryAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val studyGoalProgressPercent = if (learner.dailyMinutesGoal > 0) {
                ((learner.dailyMinutesSpent.toFloat() / learner.dailyMinutesGoal) * 100).toInt()
            } else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricCard(
                    title = "STUDY TIME",
                    value = "${learner.dailyMinutesSpent}m",
                    subtitle = "Goal: ${learner.dailyMinutesGoal}m ($studyGoalProgressPercent%)",
                    accentColor = AdaptTheme.colors.primaryAccent,
                    modifier = Modifier.weight(1f)
                )

                AnalyticsMetricCard(
                    title = "TODAY'S TASKS",
                    value = "${todayGoal.current} / ${todayGoal.target}",
                    subtitle = if (todayGoal.isCompleted) "Goal Achieved ✓" else "${todayGoal.target - todayGoal.current} remaining",
                    accentColor = if (todayGoal.isCompleted) AdaptTheme.colors.success else AdaptTheme.colors.secondaryAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Topic Competency Breakdown Card
            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOPIC COMPETENCY MATRIX",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        Text(
                            text = "${topicStats.size} Verified Domains",
                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = AdaptTheme.colors.secondaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (topicStats.isEmpty()) {
                        Text(
                            text = "No topic assessment records available yet. Complete practice drills to populate competency telemetry.",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topicStats.values.forEach { comp ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = comp.topic,
                                            style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = AdaptTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "${comp.correctAttempts}/${comp.totalAttempts} (${comp.masteryPercentage}%)",
                                            style = AdaptTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            ),
                                            color = when {
                                                comp.masteryPercentage >= 70 -> AdaptTheme.colors.secondaryAccent
                                                comp.masteryPercentage >= 50 -> AdaptTheme.colors.primaryAccent
                                                else -> AdaptTheme.colors.textMuted
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AdaptProgressIndicator(
                                        progress = if (comp.totalAttempts > 0) comp.correctAttempts.toFloat() / comp.totalAttempts else 0f,
                                        color = when {
                                            comp.masteryPercentage >= 70 -> AdaptTheme.colors.secondaryAccent
                                            comp.masteryPercentage >= 50 -> AdaptTheme.colors.primaryAccent
                                            else -> AdaptTheme.colors.warning
                                        },
                                        height = 5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Consistency View (Honest Representation)
            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SEVEN-DAY CONSISTENCY",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        Text(
                            text = "Today: Active",
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AdaptTheme.colors.primaryAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val currentDayOfWeekIndex = remember {
                        val cal = java.util.Calendar.getInstance()
                        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                            java.util.Calendar.MONDAY -> 0
                            java.util.Calendar.TUESDAY -> 1
                            java.util.Calendar.WEDNESDAY -> 2
                            java.util.Calendar.THURSDAY -> 3
                            java.util.Calendar.FRIDAY -> 4
                            java.util.Calendar.SATURDAY -> 5
                            java.util.Calendar.SUNDAY -> 6
                            else -> 0
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            // Current active session slot determined by actual calendar weekday
                            val isToday = index == currentDayOfWeekIndex

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = day,
                                    style = AdaptTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isToday) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textMuted
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isToday) AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.35f)
                                            else AdaptTheme.colors.raisedSurface
                                        )
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = if (isToday) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isToday && learner.dailyMinutesSpent > 0) {
                                        Icon(
                                            imageVector = AdaptIcons.Check,
                                            contentDescription = "Today active session",
                                            tint = AdaptTheme.colors.primaryAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "—",
                                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = AdaptTheme.colors.textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Honest empty state when historical dated records are unavailable
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(AdaptTheme.colors.raisedSurface)
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = AdaptTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(AdaptTheme.radius.medium)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Not enough history yet",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = AdaptTheme.colors.primaryAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Historical daily streaks and weekly cumulative hours will populate as daily sessions complete. Current active session: ${learner.dailyMinutesSpent}m recorded today.",
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Today's recorded study time: ${learner.dailyMinutesSpent}m. Session data is stored strictly on-device without cloud sync.",
                        style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = AdaptTheme.colors.textMuted
                    )
                }
            }
        }

        // ====================================================================
        // 4. Learning Library (Bookmarks, Mistake Notebook, Milestones)
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth()) {
            AdaptSectionHeader(
                title = "Your Learning Library",
                overline = "SAVED ARTIFACTS & REPOSITORY"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bookmarked Lessons Card
            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AdaptIcons.Bookmark,
                                contentDescription = null,
                                tint = AdaptTheme.colors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bookmarked Lessons",
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                        Text(
                            text = "${bookmarkedIds.size} saved",
                            style = AdaptTheme.typography.labelSmall,
                            color = AdaptTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (bookmarkedIds.isEmpty()) {
                        Text(
                            text = "No lessons bookmarked yet. Tap the bookmark icon in any interactive lesson to pin core references here.",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            bookmarkedIds.forEach { lessonId ->
                                val isCurrentMod = lessonId == currentModule.id || lessonId == "mod_quant_1" || lessonId == "lesson_int8_quant"
                                val displayTitle = if (isCurrentMod) currentModule.title else "Lesson: $lessonId"
                                val displayTrack = if (isCurrentMod) currentModule.trackName else learner.activeDomainTitle

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                        .background(AdaptTheme.colors.raisedSurface)
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = AdaptTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                        )
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = displayTitle,
                                            style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = AdaptTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = displayTrack,
                                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = AdaptTheme.colors.textSecondary
                                        )
                                    }

                                    if (onNavigateToLesson != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(AdaptTheme.radius.small))
                                                .clickable(role = Role.Button, onClick = onNavigateToLesson)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .semantics { contentDescription = "Open $displayTitle" }
                                        ) {
                                            Text(
                                                text = "Open →",
                                                style = AdaptTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = AdaptTheme.colors.primaryAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mistake Notebook Card (Spaced Repetition)
            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AdaptIcons.Notebook,
                                contentDescription = null,
                                tint = AdaptTheme.colors.secondaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mistake Notebook",
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }

                        val unresolvedCount = mistakeList.count { !it.isResolved }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AdaptTheme.radius.full))
                                .background(
                                    if (unresolvedCount > 0) AdaptTheme.colors.warning.copy(alpha = 0.2f)
                                    else AdaptTheme.colors.success.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (unresolvedCount > 0) "$unresolvedCount Due" else "Clear ✓",
                                style = AdaptTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (unresolvedCount > 0) AdaptTheme.colors.warning else AdaptTheme.colors.success
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (mistakeList.isEmpty()) {
                        Text(
                            text = "No recorded mistakes! As you practice drills, concepts requiring reinforcement are scheduled here automatically.",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            mistakeList.forEach { mistake ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                        .background(AdaptTheme.colors.raisedSurface)
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = AdaptTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                        )
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mistake.question.topicTag,
                                            style = AdaptTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = AdaptTheme.colors.primaryAccent
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mistake.question.question,
                                            style = AdaptTheme.typography.bodySmall,
                                            color = AdaptTheme.colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (mistake.isResolved) "Resolved • Review in ${mistake.nextRevisionDays}d" else "${mistake.mistakeCount} error(s) • Review Due",
                                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (mistake.isResolved) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.warning
                                        )
                                    }

                                    if (onNavigateToPractice != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(AdaptTheme.radius.small))
                                                .clickable(role = Role.Button) {
                                                    onNavigateToPractice(PracticeMode.REVISION)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .semantics { contentDescription = "Review mistake in practice" }
                                        ) {
                                            Text(
                                                text = "Review →",
                                                style = AdaptTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = AdaptTheme.colors.secondaryAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Completed Milestones Card
            val completedMilestones = SampleData.pathwayPreviewNodes.filter { it.status == MilestoneStatus.COMPLETED }
            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AdaptIcons.Check,
                                contentDescription = null,
                                tint = AdaptTheme.colors.secondaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Completed Milestones",
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                        Text(
                            text = "${completedMilestones.size} of ${SampleData.pathwayPreviewNodes.size}",
                            style = AdaptTheme.typography.labelSmall,
                            color = AdaptTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (completedMilestones.isEmpty()) {
                        Text(
                            text = "No milestones completed yet. Advance through your Learning Pathway to unlock verified competencies.",
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            completedMilestones.forEach { node ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                        .background(AdaptTheme.colors.raisedSurface)
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = AdaptTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                        )
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${node.index}. ${node.title}",
                                            style = AdaptTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = AdaptTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "${node.track} • ${node.estimatedMinutes}m",
                                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = AdaptTheme.colors.textSecondary
                                        )
                                    }

                                    if (onNavigateToPathway != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(AdaptTheme.radius.small))
                                                .clickable(role = Role.Button, onClick = onNavigateToPathway)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .semantics { contentDescription = "View ${node.title} in Pathway" }
                                        ) {
                                            Text(
                                                text = "View →",
                                                style = AdaptTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = AdaptTheme.colors.primaryAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 5. Curriculum & Onboarding Preferences
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth()) {
            AdaptSectionHeader(
                title = "Curriculum & Preferences",
                overline = "ADAPTIVE PERSONALIZATION"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Active Domain with Switch button
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ACTIVE LEARNING TRACK",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = learner.activeDomainTitle,
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.primaryAccent
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AdaptTheme.radius.small))
                                    .background(AdaptTheme.colors.raisedSurface)
                                    .border(
                                        width = AdaptTheme.stroke.hairline,
                                        color = AdaptTheme.colors.borderSubtle,
                                        shape = RoundedCornerShape(AdaptTheme.radius.small)
                                    )
                                    .clickable(role = Role.Button, onClick = onNavigateToExplore)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .semantics { contentDescription = "Switch learning domain" }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AdaptIcons.Compass,
                                        contentDescription = null,
                                        tint = AdaptTheme.colors.primaryAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Explore Domains",
                                        style = AdaptTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AdaptTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Skill Level Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "PROFICIENCY LEVEL",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DifficultyLevel.entries.forEach { level ->
                                val isSelected = learner.skillLevel == level
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                        .background(
                                            if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f)
                                            else AdaptTheme.colors.raisedSurface
                                        )
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                        )
                                        .clickable(role = Role.RadioButton) {
                                            if (!isSelected) {
                                                SampleData.learnerProfile = SampleData.learnerProfile.copy(skillLevel = level)
                                                if (AdaptPersistence.isInitialized) {
                                                    AdaptPersistence.scheduleSaveProfile(SampleData.learnerProfile)
                                                }
                                            }
                                        }
                                        .semantics {
                                            selected = isSelected
                                            contentDescription = "Select ${level.label} skill level"
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level.label,
                                        style = AdaptTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Daily Study Target Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "DAILY STUDY TARGET",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 25, 35, 45).forEach { minutes ->
                                val isSelected = learner.dailyMinutesGoal == minutes
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                        .background(
                                            if (isSelected) AdaptTheme.colors.secondaryAccentContainer.copy(alpha = 0.25f)
                                            else AdaptTheme.colors.raisedSurface
                                        )
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = if (isSelected) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                        )
                                        .clickable(role = Role.RadioButton) {
                                            if (!isSelected) {
                                                SampleData.learnerProfile = SampleData.learnerProfile.copy(dailyMinutesGoal = minutes)
                                                if (AdaptPersistence.isInitialized) {
                                                    AdaptPersistence.scheduleSaveProfile(SampleData.learnerProfile)
                                                }
                                            }
                                        }
                                        .semantics {
                                            selected = isSelected
                                            contentDescription = "$minutes minutes daily target"
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${minutes}m",
                                        style = AdaptTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) AdaptTheme.colors.secondaryAccent else AdaptTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Learning Style Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "PEDAGOGICAL LEARNING STYLE",
                            style = AdaptTheme.typographyExt.overline,
                            color = AdaptTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val styles = listOf(
                            "Hands-on Projects & Code",
                            "Visual Intuition & Diagrams",
                            "Mathematical Formalism"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            styles.forEach { style ->
                                val isSelected = learner.preferredLearningStyle == style
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                        .background(
                                            if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.3f else 0.18f)
                                            else AdaptTheme.colors.raisedSurface
                                        )
                                        .border(
                                            width = AdaptTheme.stroke.hairline,
                                            color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                        )
                                        .clickable(role = Role.RadioButton) {
                                            if (!isSelected) {
                                                SampleData.learnerProfile = SampleData.learnerProfile.copy(preferredLearningStyle = style)
                                                if (AdaptPersistence.isInitialized) {
                                                    AdaptPersistence.scheduleSaveProfile(SampleData.learnerProfile)
                                                }
                                            }
                                        }
                                        .semantics {
                                            selected = isSelected
                                            contentDescription = "Select $style learning style"
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = style,
                                        style = AdaptTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = AdaptIcons.Check,
                                            contentDescription = null,
                                            tint = AdaptTheme.colors.primaryAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 6. Appearance & Display Theme
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth()) {
            AdaptSectionHeader(
                title = "Appearance & Theme",
                overline = "DISPLAY CUSTOMIZATION"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = activeThemeMode == mode

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                                .background(
                                    if (isSelected) AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.35f else 0.22f)
                                    else AdaptTheme.colors.raisedSurface
                                )
                                .border(
                                    width = AdaptTheme.stroke.hairline,
                                    color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(AdaptTheme.radius.medium)
                                )
                                .clickable(
                                    role = Role.RadioButton,
                                    onClick = {
                                        themeManager?.setThemeMode(mode)
                                        localThemeMode = mode
                                    }
                                )
                                .semantics {
                                    selected = isSelected
                                    contentDescription = "Select ${mode.title} theme"
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    style = AdaptTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (mode) {
                                        ThemeMode.SYSTEM -> "Automatically matches device system dark/light configuration"
                                        ThemeMode.LIGHT -> "Soft Pearl high-contrast day palette"
                                        ThemeMode.DARK -> "Editorial Obsidian Midnight Liquid Glass"
                                    },
                                    style = AdaptTheme.typography.bodySmall,
                                    color = AdaptTheme.colors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) AdaptTheme.colors.primaryAccent else AdaptTheme.colors.textMuted,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(AdaptTheme.colors.primaryAccent)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 7. Environment & Prototype Verification Details
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth()) {
            AdaptSectionHeader(
                title = "Architecture & System Info",
                overline = "ARM BHARAT AI SOC TARGET"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdaptSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AdaptTheme.radius.large),
                backgroundColor = AdaptTheme.colors.surface,
                borderColor = AdaptTheme.colors.borderSubtle,
                borderWidth = AdaptTheme.stroke.hairline,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemInfoRow(label = "Application Target", value = "ADAPT-AI v1.0")
                    SystemInfoRow(label = "Challenge Category", value = "ARM Bharat AI SoC (PS4)")
                    SystemInfoRow(label = "Hardware Target", value = "ARMv9 Cortex & NEON Vector SIMD")
                    SystemInfoRow(label = "Inference Architecture", value = "Local Heuristic (ExecuTorch Ready)")
                    SystemInfoRow(label = "Telemetry Telemetry", value = "Zero Cloud Leakage • 100% Local")
                    SystemInfoRow(label = "Persistence Engine", value = "Atomic SharedPreferences Storage")

                    Spacer(modifier = Modifier.height(6.dp))

                    AdaptSecondaryButton(
                        text = "Reset Curriculum to Default Track",
                        onClick = { showResetDialog = true },
                        leadingIcon = AdaptIcons.Retry
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // ========================================================================
    // Reset Confirmation Dialog
    // ========================================================================
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = AdaptTheme.colors.surface,
            titleContentColor = AdaptTheme.colors.textPrimary,
            textContentColor = AdaptTheme.colors.textSecondary,
            title = {
                Text(
                    text = "Reset Default Track?",
                    style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will reset your active domain to Artificial Intelligence & ML (INT8 Weight Quantization) and restore default milestone states.",
                    style = AdaptTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        SampleData.resetToDefaultTrack()
                        showResetDialog = false
                    }
                ) {
                    Text(
                        text = "Reset",
                        color = AdaptTheme.colors.error,
                        fontWeight = FontWeight.Bold,
                        style = AdaptTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = AdaptTheme.colors.textSecondary,
                        style = AdaptTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

// -------------------------------------------------------------
// HELPER COMPOSABLES
// -------------------------------------------------------------

@Composable
private fun PassportStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
            .background(AdaptTheme.colors.raisedSurface)
            .border(
                width = AdaptTheme.stroke.hairline,
                color = AdaptTheme.colors.borderSubtle,
                shape = RoundedCornerShape(AdaptTheme.radius.medium)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = AdaptTheme.typographyExt.overline.copy(fontSize = 8.sp),
                color = AdaptTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = AdaptTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = AdaptTheme.colors.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    AdaptSurface(
        modifier = modifier,
        shape = RoundedCornerShape(AdaptTheme.radius.large),
        backgroundColor = AdaptTheme.colors.surface,
        borderColor = AdaptTheme.colors.borderSubtle,
        borderWidth = AdaptTheme.stroke.hairline,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = AdaptTheme.typographyExt.overline,
                color = AdaptTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = AdaptTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = AdaptTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AdaptTheme.typography.bodySmall,
            color = AdaptTheme.colors.textSecondary
        )
        Text(
            text = value,
            style = AdaptTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = AdaptTheme.colors.textPrimary
        )
    }
}

// -------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------
@Preview(name = "Profile - Dark Mode", showBackground = true)
@Composable
private fun ProfileScreenDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        ProfileScreen()
    }
}

@Preview(name = "Profile - Light Mode", showBackground = true)
@Composable
private fun ProfileScreenLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        ProfileScreen()
    }
}
