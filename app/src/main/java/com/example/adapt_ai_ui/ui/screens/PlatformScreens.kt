package com.example.adapt_ai_ui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.adapt_ai_ui.data.DifficultyLevel
import com.example.adapt_ai_ui.data.platform.*
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Composable
fun WelcomePlatform(platform: LearningPlatform) {
    val scope = rememberCoroutineScope()
    var page by rememberSaveable { mutableStateOf(if (platform.profiles.isEmpty()) "welcome" else "profiles") }
    var name by rememberSaveable { mutableStateOf("") }
    var delete by remember { mutableStateOf<LocalLearner?>(null) }
    BackHandler(page != "welcome") { page = if (page == "create" && platform.profiles.isNotEmpty()) "profiles" else "welcome" }
    AuroraScene(Modifier.fillMaxSize().safeDrawingPadding(), waves = true) {
        Column(
            Modifier.align(Alignment.Center).widthIn(max = 500.dp).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(horizontal = 25.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (page != "welcome") IconButton({ page = if (page == "create" && platform.profiles.isNotEmpty()) "profiles" else "welcome" }) {
                    Icon(AdaptIcons.ArrowBack, "Back")
                }
                AuroraOrb(OrbState.IDLE, { 0f }, Modifier.size(33.dp))
                Text("EMBER", color = AdaptTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium)
            }
            if (page == "welcome") {
                Spacer(Modifier.height(39.dp))
                AuroraOrb(OrbState.IDLE, { 0f }, Modifier.size(260.dp))
                Text("Your personal AI learning intelligence", color = auroraAccent, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(22.dp))
                Text("Learn smarter.\nGrow faster.", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = AdaptTheme.colors.textPrimary)
                Spacer(Modifier.height(60.dp))
                AuroraAction("Get started", { page = "create" }, Modifier.fillMaxWidth(), icon = AdaptIcons.ArrowForward)
                Spacer(Modifier.height(8.dp))
                AuroraPill("Continue as guest", { scope.launch { platform.create("Guest", true) } },
                    Modifier.fillMaxWidth(), enabled = !platform.busy)
                if (platform.profiles.isNotEmpty()) TextButton({ page = "profiles" }) { Text("Local profiles") }
            } else {
                Spacer(Modifier.height(22.dp))
                Text(if (page == "create") "Create your profile" else "Welcome back",
                    color = AdaptTheme.colors.textPrimary, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text(if (page == "create") "Choose the name Aurora will use." else "Choose a local learner profile.",
                    color = AdaptTheme.colors.textSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 19.dp))
                AuroraMedallion(AdaptIcons.Profile, AuroraCyan, Modifier.size(92.dp))
                Spacer(Modifier.height(25.dp))
                if (page == "create") {
                    OutlinedTextField(name, { name = it.take(60) }, Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("Display name") }, shape = RoundedCornerShape(25.dp),
                        leadingIcon = { Icon(AdaptIcons.Profile, null, tint = auroraAccent) })
                    Spacer(Modifier.height(14.dp))
                    AuroraAction("Create profile", { scope.launch { platform.create(name.trim(), false) } },
                        Modifier.fillMaxWidth(), enabled = name.isNotBlank() && !platform.busy, violet = true)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        platform.profiles.forEach { profile ->
                            AuroraPanel(Modifier.fillMaxWidth(), onClick = {
                                if (!platform.busy) scope.launch { platform.open(profile.id) }
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AuroraMedallion(AdaptIcons.Profile, AuroraViolet, Modifier.size(39.dp))
                                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                        Text(profile.name, color = AdaptTheme.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                                        Text(if (profile.guest) "Guest" else "On this device", color = AdaptTheme.colors.textSecondary,
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton({ delete = profile }, enabled = !platform.busy) {
                                        Icon(AdaptIcons.Trash, "Delete " + profile.name, tint = AdaptTheme.colors.textSecondary,
                                            modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    AuroraAction("Create profile", { page = "create" }, Modifier.fillMaxWidth(), violet = true)
                    TextButton({ scope.launch { platform.create("Guest", true) } }, enabled = !platform.busy) {
                        Text("Continue as guest")
                    }
                }
            }
            platform.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
            if (platform.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
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
internal fun PanelHeader(title: String, onBack: () -> Unit) {
    AuroraHeader(title, onBack)
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
    var group by remember { mutableStateOf<String?>(null) }
    val subjects = list.groupBy { it.subject }
    AuroraScene(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 17.dp)) {
            PanelHeader("Explore subjects", onBack)
            OutlinedTextField(query, { query = it; group = null }, Modifier.fillMaxWidth(),
                placeholder = { Text("Search subjects or concepts") }, singleLine = true,
                shape = RoundedCornerShape(27.dp), leadingIcon = { Icon(AdaptIcons.Search, null, tint = auroraAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = auroraPanel, unfocusedContainerColor = auroraPanel,
                    focusedBorderColor = auroraAccent, unfocusedBorderColor = auroraLine
                ))
            Row(Modifier.padding(vertical = 9.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AuroraPill("All", { filter = null; savedOnly = false; group = null }, active = filter == null && !savedOnly)
                val ordered = listOf("Science & mathematics", "Languages", "Creative") + categories
                ordered.distinct().filter { it in categories }.forEach { category ->
                    val label = when (category) {
                        "Science & mathematics" -> "Science & Math"
                        "Creative" -> "Arts"
                        else -> category
                    }
                    AuroraPill(label, { filter = category; savedOnly = false; group = null },
                        active = filter == category, horizontalPadding = 10.dp)
                }
                AuroraPill("Saved (${selected.size})", { filter = null; savedOnly = !savedOnly; group = null }, active = savedOnly)
            }
            platform.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyVerticalGrid(GridCells.Adaptive(136.dp), Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp)) {
                if (group != null) item(span = { GridItemSpan(maxLineSpan) }) {
                    TextButton({ group = null }) { Text("All subjects") }
                }
                if (query.isBlank() && filter == null && !savedOnly && group == null && current.preferences.recentTopics.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            current.preferences.recentTopics.mapNotNull { id -> current.catalog.firstOrNull { it.id == id } }.forEach { topic ->
                                AuroraPill(topic.name, { detail = topic })
                            }
                        }
                    }
                }
                if (list.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Text("No topics match this search.") }
                if (group == null && query.isBlank() && !savedOnly) {
                    items(subjects.entries.toList(), key = { it.key }) { (subject, topics) ->
                        val badge = when (subject) {
                            "Mathematics" -> AdaptIcons.Target to AuroraCyan
                            "Physics" -> AdaptIcons.Sparkle to AuroraBlue
                            "Chemistry" -> AdaptIcons.Lightbulb to AuroraViolet
                            "Biology" -> AdaptIcons.Pathway to AuroraGreen
                            "Programming", "Computer Science" -> AdaptIcons.Code to AuroraCyan
                            "Artificial Intelligence" -> AdaptIcons.Tutor to AuroraBlue
                            else -> when (topics.first().category) {
                                "Engineering" -> AdaptIcons.Pathway to AuroraBlue
                                "Technology" -> AdaptIcons.Code to AuroraCyan
                                "Languages", "Humanities" -> AdaptIcons.Notebook to AuroraViolet
                                else -> AdaptIcons.Compass to AuroraGreen
                            }
                        }
                        AuroraPanel(Modifier.fillMaxWidth().heightIn(min = 128.dp), onClick = { group = subject }) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                AuroraMedallion(badge.first, badge.second, Modifier.size(42.dp))
                                Spacer(Modifier.height(7.dp))
                                Text(subject, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                                    color = AdaptTheme.colors.textPrimary, style = MaterialTheme.typography.titleSmall)
                                Text(topics.take(2).joinToString(", ") { it.name }, textAlign = TextAlign.Center,
                                    color = AdaptTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                } else {
                    items(if (group == null) list else subjects[group].orEmpty(), key = { it.id }) { topic ->
                        val checked = topic.id in selected
                        AuroraPanel(Modifier.fillMaxWidth(), if (checked) AuroraCyan else null, onClick = { detail = topic }) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                AuroraMedallion(AdaptIcons.Notebook, AuroraViolet, Modifier.size(34.dp))
                                Spacer(Modifier.weight(1f))
                                Checkbox(checked, { selected = if (it) selected + topic.id else selected - topic.id },
                                    modifier = Modifier.semantics { contentDescription = "Save " + topic.name })
                            }
                            Text(topic.name, color = AdaptTheme.colors.textPrimary, style = MaterialTheme.typography.titleSmall)
                            Text(topic.concepts.joinToString(" / "), color = AdaptTheme.colors.textSecondary,
                                style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { TextButton(onArchive) { Text("Browse original courses") } }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                AuroraPill("Save interests", {
                    scope.launch {
                        platform.savePreferences(current.preferences.copy(
                            interests = selected, activeTopic = current.preferences.activeTopic?.takeIf { it in selected }
                        ))
                    }
                }, Modifier.weight(1f), enabled = !platform.busy)
                AuroraAction("Custom topic", { editor = null; editOpen = true }, Modifier.weight(1f),
                    enabled = !platform.busy, icon = AdaptIcons.Plus)
            }
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
    AuroraScene {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        AuroraHeader("Learning Preferences", onBack)
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
        AuroraAction("Save preferences", {
            scope.launch {
                platform.savePreferences(prefs.copy(
                    minutes = requireNotNull(minutes.toIntOrNull()), deadline = date,
                    language = prefs.language.ifBlank { "English" }, assistant = prefs.assistant.ifBlank { "Aurora" }
                ))
                if (platform.error == null) onBack()
            }
        }, Modifier.fillMaxWidth(), enabled = valid && !platform.busy)
        platform.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
}
