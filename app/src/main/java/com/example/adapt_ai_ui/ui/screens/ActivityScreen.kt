package com.example.adapt_ai_ui.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.adapt_ai_ui.data.platform.LearningSession
import com.example.adapt_ai_ui.data.platform.activity
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun ActivityScreen(session: LearningSession, onBack: () -> Unit) {
    val today = LocalDate.now()
    val activity = remember(session, today) { session.activity(today, ZoneId.systemDefault()) }
    var month by remember { mutableStateOf(false) }
    val length = if (month) 30 else 7
    val dates = (length - 1 downTo 0).map { today.minusDays(it.toLong()) }
    val max = dates.maxOf { activity.minutesByDate[it] ?: 0 }.coerceAtLeast(1)
    val accent = auroraAccent
    AuroraScene {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuroraHeader("Activity & Achievements", onBack)
            AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue) {
                Text("${activity.currentStreak} day streak", style = AdaptTheme.typography.headlineSmall)
                Text("Best streak ${activity.bestStreak} days", color = AdaptTheme.colors.textSecondary)
                Text("${activity.minutesByDate[today] ?: 0} of ${session.preferences.minutes} study minutes today",
                    color = accent, modifier = Modifier.padding(top = 9.dp))
            }
            AuroraPanel(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraPill("7 days", { month = false }, active = !month)
                    AuroraPill("30 days", { month = true }, active = month)
                }
                val count = dates.count { it in activity.qualifiedDays }
                Text("$count active days in this period", Modifier.padding(vertical = 12.dp))
                if (dates.all { activity.minutesByDate[it].orZero() == 0L }) {
                    Text("Study minutes appear here after a lesson is viewed.", color = AdaptTheme.colors.textSecondary)
                } else {
                    Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom) {
                        dates.forEach { date ->
                            val minutes = activity.minutesByDate[date].orZero()
                            val height by animateFloatAsState((minutes.toFloat() / max).coerceIn(.03f, 1f), label = "Study minutes")
                            Box(Modifier.weight(1f).fillMaxHeight(height).background(accent, RoundedCornerShape(3.dp))
                                .semantics { contentDescription = "$date, $minutes study minutes" })
                        }
                    }
                    val label = DateTimeFormatter.ofPattern("d MMM")
                    Text("${dates.first().format(label)} to ${today.format(label)}", Modifier.padding(top = 8.dp),
                        color = AdaptTheme.colors.textSecondary, style = AdaptTheme.typography.bodySmall)
                }
            }
            Text("Achievements", style = AdaptTheme.typography.titleLarge)
            listOf(
                "First lesson" to (activity.completedLessons > 0),
                "First scored assessment" to (activity.quizzes > 0),
                "First perfect assessment" to (activity.perfectQuizzes > 0),
                "Three-day streak" to (activity.bestStreak >= 3),
                "Seven-day streak" to (activity.bestStreak >= 7)
            ).forEach { (title, earned) ->
                AuroraPanel(Modifier.fillMaxWidth(), if (earned) AuroraViolet else null) {
                    Text(title, style = AdaptTheme.typography.titleMedium)
                    Text(if (earned) "Earned" else "In progress", color = if (earned) accent else AdaptTheme.colors.textSecondary)
                }
            }
        }
    }
}

private fun Long?.orZero() = this ?: 0L
