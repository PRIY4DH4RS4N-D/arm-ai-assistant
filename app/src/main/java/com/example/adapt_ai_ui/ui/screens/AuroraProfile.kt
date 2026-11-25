package com.example.adapt_ai_ui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.adapt_ai_ui.data.TutorRepository
import com.example.adapt_ai_ui.data.platform.LearningSession
import com.example.adapt_ai_ui.inference.ModelState
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.LocalThemeManager

@Composable
internal fun AuroraProfile(
    session: LearningSession, openPreferences: () -> Unit, openInterests: () -> Unit,
    openJourney: () -> Unit, openProgress: () -> Unit, openModel: () -> Unit,
    openSettings: () -> Unit, switchProfile: () -> Unit,
    openScan: () -> Unit, openActivity: () -> Unit, openCoding: () -> Unit
) {
    val runtime = TutorRepository.runtime
    val modelState = runtime?.state?.collectAsState()?.value ?: ModelState.MISSING
    val installed = runtime?.installedModel?.collectAsState()?.value
    val prefs = session.preferences
    val initials = session.profile.name.split(' ').mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    val theme = LocalThemeManager.current?.currentThemeMode
    AuroraScene {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)) {
            AuroraHeader("My Profile", trailing = {
                IconButton(openSettings) { Icon(AdaptIcons.Profile, "Profile settings", tint = auroraAccent) }
            })
            Column(Modifier.fillMaxWidth().padding(bottom = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(88.dp)
                    .background(Brush.radialGradient(listOf(Color(0xFF183B7A), Color(0xFF091428))), CircleShape)
                    .border(2.dp, Brush.sweepGradient(listOf(AuroraCyan, AuroraBlue, AuroraViolet, AuroraCyan)), CircleShape),
                    contentAlignment = Alignment.Center) {
                    Text(initials, style = AdaptTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(9.dp))
                Text(session.profile.name, style = AdaptTheme.typography.headlineSmall, color = AdaptTheme.colors.textPrimary)
                Text(if (session.profile.guest) "Guest learner" else "Local learner",
                    style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
            }
            ProfileRow("Learning Goal", prefs.goal.ifBlank { "Set a goal" }, AdaptIcons.Target, AuroraViolet,
                openPreferences, highlighted = true)
            ProfileRow("My Interests",
                session.topic?.name ?: if (prefs.interests.isEmpty()) "Explore subjects" else "${prefs.interests.size} saved topics",
                AdaptIcons.Compass, AuroraViolet, openInterests)
            ProfileRow("Study Plan", "${prefs.minutes} min per study day", AdaptIcons.Pathway, AuroraViolet, openJourney)
            session.topic?.let {
                ProfileRow("My Progress", it.name, AdaptIcons.Check, AuroraCyan, openProgress)
            }
            ProfileRow("Preferences", listOfNotNull(theme?.name?.lowercase()?.replaceFirstChar(Char::titlecase), prefs.language)
                .joinToString("  |  "), AdaptIcons.Notebook, AuroraCyan, openPreferences)
            ProfileRow("Model Status", modelState.label, AdaptIcons.Sparkle, AuroraCyan, openModel)
            ProfileRow("Scan & Learn", "Camera and local text recognition", AdaptIcons.Search, AuroraCyan, openScan)
            ProfileRow("Coding Practice", "Trace C, C++ and Python exercises", AdaptIcons.Code, AuroraCyan, openCoding)
            ProfileRow("Activity & Achievements", "Study goals, activity and milestones", AdaptIcons.Target, AuroraCyan, openActivity)
            ProfileRow("Storage Used", installed?.let { "${it.bytes / 1_048_576} MiB for offline model" } ?: "No model installed",
                AdaptIcons.Lock, AuroraCyan, openModel)
            ProfileRow("Profile Settings", "Name and appearance", AdaptIcons.Profile, AuroraCyan, openSettings)
            AuroraPill("Switch profile", switchProfile, Modifier.fillMaxWidth().padding(bottom = 14.dp))
        }
    }
}

@Composable
private fun ProfileRow(
    label: String, detail: String, icon: ImageVector, color: Color,
    onClick: () -> Unit, highlighted: Boolean = false
) {
    AuroraPanel(Modifier.fillMaxWidth(), if (highlighted) color else null, onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AuroraMedallion(icon, color, Modifier.size(34.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = AdaptTheme.colors.textPrimary, style = AdaptTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(detail, color = AdaptTheme.colors.textSecondary, style = AdaptTheme.typography.bodySmall)
            }
            Icon(AdaptIcons.ArrowForward, null, tint = AdaptTheme.colors.textSecondary, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
internal fun AuroraModelScreen(onBack: () -> Unit) {
    AuroraScene {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuroraHeader("Model Management", onBack)
            val runtime = TutorRepository.runtime
            if (runtime != null) {
                AuroraModelManager(runtime)
            } else {
                AuroraPanel(Modifier.fillMaxWidth()) {
                    Text("The local runtime is unavailable.", color = AdaptTheme.colors.textPrimary)
                }
            }
        }
    }
}
