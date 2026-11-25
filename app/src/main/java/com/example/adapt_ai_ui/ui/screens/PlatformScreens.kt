package com.example.adapt_ai_ui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.platform.*
import com.example.adapt_ai_ui.ui.components.AuroraOrb
import com.example.adapt_ai_ui.ui.components.OrbState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Composable
fun WelcomePlatform(platform: LearningPlatform) {
    val scope = rememberCoroutineScope()
    var create by rememberSaveable { mutableStateOf(false) }
    var delete by remember { mutableStateOf<LocalLearner?>(null) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ADAPT-AI", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start))
        AuroraOrb(OrbState.IDLE, { 0f }, Modifier.sizeIn(maxWidth = 255.dp).width(250.dp).aspectRatio(1f))
        Text("Learn what matters to you", style = MaterialTheme.typography.headlineSmall)
        Text("A personal AI tutor for your subjects, goals and practice.", modifier = Modifier.padding(vertical = 12.dp))
        Button({ create = true }, Modifier.fillMaxWidth(), enabled = !platform.busy) { Text("Create profile") }
        OutlinedButton(
            { scope.launch { platform.create("Guest", true) } }, Modifier.fillMaxWidth(), enabled = !platform.busy
        ) { Text("Continue as guest") }
        if (platform.profiles.isNotEmpty()) {
            Text("Local profiles", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            for (profile in platform.profiles) {
                ListItem(
                    headlineContent = { Text(profile.name) },
                    supportingContent = { Text(if (profile.guest) "Guest" else "On this device") },
                    trailingContent = { TextButton({ delete = profile }, enabled = !platform.busy) { Text("Delete") } },
                    modifier = Modifier.clickable(enabled = !platform.busy) { scope.launch { platform.open(profile.id) } }
                )
            }
        }
        platform.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (platform.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    if (create) NameDialog("Create a local profile", "", { create = false }) { name ->
        create = false
        scope.launch { platform.create(name, false) }
    }
    delete?.let { profile ->
        AlertDialog(onDismissRequest = { delete = null }, title = { Text("Delete ${profile.name}?") },
            text = { Text("This permanently removes this profile's saved learning activity and conversations.") },
            confirmButton = { TextButton({
                delete = null
                scope.launch { platform.delete(profile.id) }
            }) { Text("Delete profile") } },
            dismissButton = { TextButton({ delete = null }) { Text("Cancel") } })
    }
}

@Composable
internal fun NameDialog(title: String, initial: String, close: () -> Unit, save: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = close, title = { Text(title) },
        text = { OutlinedTextField(name, { name = it.take(60) }, label = { Text("Display name") }, singleLine = true) },
        confirmButton = { Button({ save(name.trim()) }, enabled = name.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(close) { Text("Cancel") } })
}

@Composable
internal fun PanelHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onBack) { Text("Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SubjectExplorer(platform: LearningPlatform, onBack: () -> Unit, onTopic: () -> Unit, onArchive: () -> Unit) {
    val current = platform.session ?: return
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    var selected by remember(current.profile.id) { mutableStateOf(current.preferences.interests) }
    var editor by remember { mutableStateOf<LearningTopic?>(null) }
    var editOpen by rememberSaveable { mutableStateOf(false) }
    var detail by remember { mutableStateOf<LearningTopic?>(null) }
    val categories = current.catalog.map { it.category }.distinct()
    val list = LearningCatalog.search(query, filter, current.preferences.customTopics)
        .filter { !savedOnly || it.id in selected }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        PanelHeader("Explore subjects", onBack)
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search subjects or concepts") }, singleLine = true)
        Row(Modifier.padding(vertical = 6.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(filter == null && !savedOnly, { filter = null; savedOnly = false }, { Text("All") })
            FilterChip(savedOnly, { filter = null; savedOnly = !savedOnly }, { Text("Saved (${selected.size})") })
            categories.forEach { category ->
                FilterChip(filter == category, { filter = category; savedOnly = false }, { Text(category) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({
                scope.launch {
                    platform.savePreferences(current.preferences.copy(
                        interests = selected, activeTopic = current.preferences.activeTopic?.takeIf { it in selected }
                    ))
                }
            }, enabled = !platform.busy) { Text("Save interests") }
            OutlinedButton({ editor = null; editOpen = true }, enabled = !platform.busy) { Text("Custom topic") }
        }
        platform.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            if (query.isBlank() && filter == null && !savedOnly && current.preferences.recentTopics.isNotEmpty()) {
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        current.preferences.recentTopics.mapNotNull { id -> current.catalog.firstOrNull { it.id == id } }.forEach { topic ->
                            AssistChip({ detail = topic }, { Text(topic.name) })
                        }
                    }
                }
            }
            if (list.isEmpty()) item { Text("No topics match this search.") }
            items(list, key = { it.id }) { topic ->
                Card(Modifier.fillMaxWidth().clickable { detail = topic }, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )) {
                    Row(Modifier.padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                            Text(topic.name, style = MaterialTheme.typography.titleMedium)
                            Text(topic.subject, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(topic.concepts.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(topic.id in selected, {
                            selected = if (it) selected + topic.id else selected - topic.id
                        })
                    }
                }
            }
            item { TextButton(onArchive) { Text("Browse original courses") } }
        }
    }
    detail?.let { topic ->
        AlertDialog(onDismissRequest = { detail = null }, title = { Text(topic.name) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(topic.subject, color = MaterialTheme.colorScheme.primary)
                    topic.concepts.forEach { Text(it, Modifier.padding(vertical = 5.dp)) }
                    if (topic.objective.isNotBlank()) Text(topic.objective)
                    if (topic.custom) {
                        TextButton({ detail = null; editor = topic; editOpen = true }) { Text("Edit topic") }
                        TextButton({
                            detail = null
                            selected -= topic.id
                            scope.launch {
                                platform.savePreferences(current.preferences.copy(
                                    customTopics = current.preferences.customTopics.filterNot { it.id == topic.id },
                                    interests = selected,
                                    activeTopic = current.preferences.activeTopic?.takeIf { it != topic.id }
                                ))
                            }
                        }) { Text("Remove topic from catalog") }
                    }
                }
            },
            confirmButton = {
                Button({
                    detail = null
                    scope.launch {
                        platform.savePreferences(current.preferences.copy(
                            interests = selected + topic.id, activeTopic = topic.id,
                            recentTopics = (listOf(topic.id) + current.preferences.recentTopics).distinct().take(12)
                        ))
                        if (platform.session?.topic?.id == topic.id) onTopic()
                    }
                }, enabled = !platform.busy) { Text("Open learning workspace") }
            },
            dismissButton = { TextButton({ detail = null }) { Text("Close") } })
    }
    if (editOpen) CustomTopicDialog(editor, { editOpen = false }) { topic ->
        editOpen = false
        selected += topic.id
        scope.launch {
            platform.savePreferences(current.preferences.copy(
                interests = selected,
                customTopics = current.preferences.customTopics.filterNot { it.id == topic.id } + topic
            ))
        }
    }
}

@Composable
private fun CustomTopicDialog(existing: LearningTopic?, close: () -> Unit, save: (LearningTopic) -> Unit) {
    var subject by rememberSaveable { mutableStateOf(existing?.subject.orEmpty()) }
    var topic by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var concepts by rememberSaveable { mutableStateOf(existing?.concepts?.joinToString().orEmpty()) }
    var objective by rememberSaveable { mutableStateOf(existing?.objective.orEmpty()) }
    AlertDialog(onDismissRequest = close, title = { Text(if (existing == null) "Create a topic" else "Edit topic") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(subject, { subject = it.take(80) }, label = { Text("Subject") }, singleLine = true)
                OutlinedTextField(topic, { topic = it.take(80) }, label = { Text("Topic") }, singleLine = true)
                OutlinedTextField(concepts, { concepts = it.take(400) }, label = { Text("Subtopics, separated by commas") })
                OutlinedTextField(objective, { objective = it.take(250) }, label = { Text("Learning objective") })
            }
        }, confirmButton = {
            Button({
                save(LearningTopic(existing?.id ?: "custom/${UUID.randomUUID()}", "Custom", subject.trim(),
                    topic.trim(), concepts.split(',').map(String::trim).filter(String::isNotEmpty).distinct().take(12), objective.trim(), true))
            }, enabled = subject.isNotBlank() && topic.isNotBlank()) { Text("Save topic") }
        }, dismissButton = { TextButton(close) { Text("Cancel") } })
}

@Composable
fun PlatformPreferences(platform: LearningPlatform, onBack: () -> Unit) {
    val current = platform.session ?: return
    val scope = rememberCoroutineScope()
    var prefs by remember(current.profile.id) { mutableStateOf(current.preferences) }
    var minutes by rememberSaveable { mutableStateOf(prefs.minutes.toString()) }
    var deadline by rememberSaveable { mutableStateOf(prefs.deadline?.toString().orEmpty()) }
    val date = remember(deadline) { runCatching { LocalDate.parse(deadline) }.getOrNull() }
    val valid = minutes.toIntOrNull() in 5..180 && prefs.studyDays.isNotEmpty() &&
        (deadline.isBlank() || date != null)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        PanelHeader("Learning preferences", onBack)
        OutlinedTextField(prefs.goal, { prefs = prefs.copy(goal = it.take(250)) }, Modifier.fillMaxWidth(), label = { Text("Learning goal") })
        Text("How much do you already know?", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(prefs.level == null, { prefs = prefs.copy(level = null) }, { Text("Not sure") })
            DifficultyLevel.entries.forEach { level ->
                FilterChip(prefs.level == level, { prefs = prefs.copy(level = level) }, { Text(level.label) })
            }
        }
        OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, label = { Text("Minutes per study day") }, singleLine = true)
        OutlinedTextField(deadline, { deadline = it.take(10) }, label = { Text("Deadline (YYYY-MM-DD, optional)") }, singleLine = true)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(day in prefs.studyDays, {
                    prefs = prefs.copy(studyDays = if (day in prefs.studyDays) prefs.studyDays - day else prefs.studyDays + day)
                }, { Text(day.name.take(3)) })
            }
        }
        OutlinedTextField(prefs.style, { prefs = prefs.copy(style = it.take(80)) }, label = { Text("Explanation style") }, singleLine = true)
        OutlinedTextField(prefs.language, { prefs = prefs.copy(language = it.take(40)) }, label = { Text("Preferred language") }, singleLine = true)
        OutlinedTextField(prefs.assistant, { prefs = prefs.copy(assistant = it.take(40)) }, label = { Text("Assistant name") }, singleLine = true)
        Button({
            scope.launch {
                platform.savePreferences(prefs.copy(
                    minutes = requireNotNull(minutes.toIntOrNull()), deadline = date,
                    language = prefs.language.ifBlank { "English" }, assistant = prefs.assistant.ifBlank { "Aurora" }
                ))
                if (platform.error == null) onBack()
            }
        }, enabled = valid && !platform.busy) { Text("Save preferences") }
        platform.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
