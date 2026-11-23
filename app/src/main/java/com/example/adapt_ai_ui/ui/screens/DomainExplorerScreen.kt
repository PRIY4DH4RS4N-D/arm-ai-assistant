package com.example.adapt_ai_ui.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.DomainRepository
import com.example.adapt_ai_ui.data.LearningDomain
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.ui.components.AdaptGlassSurface
import com.example.adapt_ai_ui.ui.components.AdaptIcons
import com.example.adapt_ai_ui.ui.components.AdaptPill
import com.example.adapt_ai_ui.ui.components.AdaptPrimaryButton
import com.example.adapt_ai_ui.ui.components.AdaptSecondaryButton
import com.example.adapt_ai_ui.ui.components.AdaptSurface
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

// Filter Category Enum for clean domain grouping without changing IDs
private enum class DomainCategoryFilter(val label: String) {
    ALL("All Tracks"),
    AI_SYSTEMS("AI & Edge"),
    CS_MATH("CS & Math"),
    APPLIED("Applied Engineering"),
    CUSTOM("Custom")
}

@Composable
fun DomainExplorerScreen(
    onNavigateBack: () -> Unit,
    onStartOnboarding: (LearningDomain) -> Unit,
    modifier: Modifier = Modifier
) {
    val learner = SampleData.learnerProfile
    var selectedDomain by remember {
        mutableStateOf(DomainRepository.getDomainById(learner.activeDomainId))
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DomainCategoryFilter.ALL) }
    var newCustomTopicInput by remember { mutableStateOf("") }
    var customTopicError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    // Categorized Domain Filter
    val filteredDomains = remember(searchQuery, selectedCategory) {
        DomainRepository.domains.filter { domain ->
            val matchesCategory = when (selectedCategory) {
                DomainCategoryFilter.ALL -> true
                DomainCategoryFilter.AI_SYSTEMS -> domain.id in listOf("ai_ml", "embedded", "data_science")
                DomainCategoryFilter.CS_MATH -> domain.id in listOf("cs", "math", "programming")
                DomainCategoryFilter.APPLIED -> domain.id in listOf("science", "languages")
                DomainCategoryFilter.CUSTOM -> domain.isCustom
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                domain.title.contains(searchQuery, ignoreCase = true) ||
                        domain.tagline.contains(searchQuery, ignoreCase = true) ||
                        domain.description.contains(searchQuery, ignoreCase = true) ||
                        domain.suggestedTopics.any { it.contains(searchQuery, ignoreCase = true) }
            }

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AdaptTheme.colors.background)
            .statusBarsPadding()
    ) {
        // ====================================================================
        // 1. Editorial Top Bar
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AdaptTheme.colors.raisedSurface)
                        .border(AdaptTheme.stroke.hairline, AdaptTheme.colors.borderSubtle, CircleShape)
                        .clickable(role = Role.Button, onClick = onNavigateBack)
                        .semantics { contentDescription = "Navigate back to profile" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AdaptIcons.ArrowBack,
                        contentDescription = null,
                        tint = AdaptTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "CURRICULUM ECOSYSTEM",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.primaryAccent
                    )
                    Text(
                        text = "Domain Explorer",
                        style = AdaptTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AdaptTheme.colors.textPrimary
                    )
                }
            }

            // Active Track Indicator Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AdaptTheme.radius.full))
                    .background(AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.25f))
                    .border(
                        AdaptTheme.stroke.hairline,
                        AdaptTheme.colors.primaryAccent.copy(alpha = 0.4f),
                        RoundedCornerShape(AdaptTheme.radius.full)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AdaptTheme.colors.primaryAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active: ${learner.activeDomainTitle}",
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.primaryAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ====================================================================
        // 2. Search & Filter Bar
        // ====================================================================
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search domains (e.g. INT8, Rust, SVD, FreeRTOS...)",
                        style = AdaptTheme.typography.bodySmall,
                        color = AdaptTheme.colors.textMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = AdaptIcons.Search,
                        contentDescription = null,
                        tint = AdaptTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AdaptIcons.Cross,
                                contentDescription = "Clear search",
                                tint = AdaptTheme.colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(AdaptTheme.radius.medium),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdaptTheme.colors.primaryAccent,
                    unfocusedBorderColor = AdaptTheme.colors.borderSubtle,
                    focusedContainerColor = AdaptTheme.colors.surface,
                    unfocusedContainerColor = AdaptTheme.colors.raisedSurface,
                    focusedTextColor = AdaptTheme.colors.textPrimary,
                    unfocusedTextColor = AdaptTheme.colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(DomainCategoryFilter.entries) { category ->
                    val isSelected = selectedCategory == category
                    AdaptPill(
                        text = category.label,
                        isSelected = isSelected,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }

        // ====================================================================
        // 3. Domain List & Hero Detail
        // ====================================================================
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selected Domain Hero Preview
            item(key = "selected_domain_hero") {
                SelectedDomainHeroCard(
                    domain = selectedDomain,
                    isActive = selectedDomain.id == learner.activeDomainId,
                    onStartOnboarding = {
                        onStartOnboarding(selectedDomain)
                    },
                    customTopicInput = newCustomTopicInput,
                    onCustomTopicInputChange = {
                        newCustomTopicInput = it
                        if (customTopicError != null) customTopicError = null
                    },
                    customTopicError = customTopicError,
                    onAddCustomTopic = {
                        val trimmed = newCustomTopicInput.trim()
                        when {
                            trimmed.isBlank() -> {
                                customTopicError = "Topic name cannot be empty."
                            }
                            SampleData.customTopics.any { it.equals(trimmed, ignoreCase = true) } -> {
                                customTopicError = "Topic '$trimmed' is already in your custom list."
                            }
                            else -> {
                                SampleData.addCustomTopic(trimmed)
                                newCustomTopicInput = ""
                                customTopicError = null
                                focusManager.clearFocus()
                                // Update selected domain topics if currently on custom domain
                                if (selectedDomain.isCustom) {
                                    selectedDomain = DomainRepository.getDomainById("custom")
                                }
                            }
                        }
                    },
                    onRemoveCustomTopic = { topic ->
                        SampleData.removeCustomTopic(topic)
                        if (selectedDomain.isCustom) {
                            selectedDomain = DomainRepository.getDomainById("custom")
                        }
                    }
                )
            }

            // Domain List Section Header
            item(key = "domain_list_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVAILABLE LEARNING TRACKS (${filteredDomains.size})",
                        style = AdaptTheme.typographyExt.overline,
                        color = AdaptTheme.colors.textMuted
                    )
                    Text(
                        text = "Tap to inspect details",
                        style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }
            }

            if (filteredDomains.isEmpty()) {
                item(key = "empty_domains_state") {
                    AdaptSurface(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = AdaptTheme.colors.surface,
                        borderColor = AdaptTheme.colors.borderSubtle
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔍 No Matching Tracks",
                                style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AdaptTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No domain matches \"$searchQuery\". You can clear search or create a Custom Learning Domain with your own topics.",
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AdaptSecondaryButton(
                                text = "Configure Custom Learning Domain",
                                onClick = {
                                    val custom = DomainRepository.getDomainById("custom")
                                    selectedDomain = custom
                                    searchQuery = ""
                                    selectedCategory = DomainCategoryFilter.ALL
                                }
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredDomains,
                    key = { it.id }
                ) { domain ->
                    val isSelected = domain.id == selectedDomain.id
                    val isActive = domain.id == learner.activeDomainId

                    DomainListItemCard(
                        domain = domain,
                        isSelected = isSelected,
                        isActive = isActive,
                        onClick = { selectedDomain = domain }
                    )
                }
            }
        }
    }
}

// ============================================================================
// Selected Domain Hero Inspector Card
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedDomainHeroCard(
    domain: LearningDomain,
    isActive: Boolean,
    onStartOnboarding: () -> Unit,
    customTopicInput: String,
    onCustomTopicInputChange: (String) -> Unit,
    customTopicError: String?,
    onAddCustomTopic: () -> Unit,
    onRemoveCustomTopic: (String) -> Unit
) {
    AdaptGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        isGlassEnabled = true,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Emoji, Title, Hours Pill, Active Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.35f))
                            .border(
                                AdaptTheme.stroke.hairline,
                                AdaptTheme.colors.primaryAccent.copy(alpha = 0.4f),
                                RoundedCornerShape(AdaptTheme.radius.medium)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = domain.iconEmoji,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = domain.title,
                            style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AdaptTheme.colors.textPrimary
                        )
                        Text(
                            text = "~${domain.estimatedHours} hours • Adaptive Curriculum",
                            style = AdaptTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = AdaptTheme.colors.secondaryAccent
                        )
                    }
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.extraSmall))
                            .background(AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.3f))
                            .border(
                                AdaptTheme.stroke.hairline,
                                AdaptTheme.colors.primaryAccent,
                                RoundedCornerShape(AdaptTheme.radius.extraSmall)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ACTIVE TRACK",
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AdaptTheme.colors.primaryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Domain Tagline & Description
            Text(
                text = domain.tagline,
                style = AdaptTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AdaptTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = domain.description,
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Recommended Goals
            if (domain.recommendedGoals.isNotEmpty()) {
                Text(
                    text = "TARGET OBJECTIVES",
                    style = AdaptTheme.typographyExt.overline.copy(fontSize = 9.sp),
                    color = AdaptTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    domain.recommendedGoals.take(3).forEach { goal ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯",
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = goal,
                                style = AdaptTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = AdaptTheme.colors.textPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Core Topics Section
            Text(
                text = if (domain.isCustom) "CUSTOM LEARNING TOPICS" else "CORE TOPICS INCLUDED",
                style = AdaptTheme.typographyExt.overline.copy(fontSize = 9.sp),
                color = AdaptTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                domain.suggestedTopics.forEach { topic ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.small))
                            .background(AdaptTheme.colors.raisedSurface)
                            .border(
                                AdaptTheme.stroke.hairline,
                                AdaptTheme.colors.borderSubtle,
                                RoundedCornerShape(AdaptTheme.radius.small)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = topic,
                                style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = AdaptTheme.colors.textSecondary
                            )
                            // Allow removing custom topics if custom domain
                            if (domain.isCustom && SampleData.customTopics.contains(topic)) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .clickable { onRemoveCustomTopic(topic) }
                                        .semantics { contentDescription = "Remove topic $topic" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = AdaptIcons.Cross,
                                        contentDescription = null,
                                        tint = AdaptTheme.colors.error,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom Topic Entry (Shown for all domains or prominently for custom track)
            if (domain.isCustom) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ADD CUSTOM TOPIC TO SYLLABUS",
                    style = AdaptTheme.typographyExt.overline.copy(fontSize = 9.sp),
                    color = AdaptTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customTopicInput,
                        onValueChange = onCustomTopicInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "e.g. Edge Transformer Pruning",
                                style = AdaptTheme.typography.bodySmall,
                                color = AdaptTheme.colors.textMuted
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(AdaptTheme.radius.medium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdaptTheme.colors.primaryAccent,
                            unfocusedBorderColor = AdaptTheme.colors.borderSubtle,
                            focusedContainerColor = AdaptTheme.colors.surface,
                            unfocusedContainerColor = AdaptTheme.colors.raisedSurface,
                            focusedTextColor = AdaptTheme.colors.textPrimary,
                            unfocusedTextColor = AdaptTheme.colors.textPrimary
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                            .background(AdaptTheme.colors.primaryAccent)
                            .clickable(role = Role.Button, onClick = onAddCustomTopic)
                            .semantics { contentDescription = "Add custom topic" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AdaptIcons.Plus,
                            contentDescription = null,
                            tint = if (AdaptTheme.isDark) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (customTopicError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = customTopicError,
                        style = AdaptTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = AdaptTheme.colors.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action Button: Personalize & Activate Track
            AdaptPrimaryButton(
                text = if (isActive) "Reconfigure Learning Preferences →" else "Personalize & Activate Track →",
                onClick = onStartOnboarding,
                leadingIcon = AdaptIcons.Target
            )
        }
    }
}

// ============================================================================
// Domain List Item Card
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomainListItemCard(
    domain: LearningDomain,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> AdaptTheme.colors.primaryAccent
        isActive -> AdaptTheme.colors.secondaryAccent.copy(alpha = 0.5f)
        else -> AdaptTheme.colors.borderSubtle
    }

    val backgroundColor = when {
        isSelected -> AdaptTheme.colors.primaryAccentContainer.copy(alpha = if (AdaptTheme.isDark) 0.2f else 0.12f)
        else -> AdaptTheme.colors.surface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AdaptTheme.radius.large))
            .background(backgroundColor)
            .border(
                width = if (isSelected) AdaptTheme.stroke.accent else AdaptTheme.stroke.hairline,
                color = borderColor,
                shape = RoundedCornerShape(AdaptTheme.radius.large)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                selected = isSelected
                contentDescription = "Select domain ${domain.title}"
            }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = domain.iconEmoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = domain.title,
                            style = AdaptTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AdaptTheme.colors.textPrimary
                        )
                        Text(
                            text = domain.tagline,
                            style = AdaptTheme.typography.bodySmall,
                            color = AdaptTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.extraSmall))
                            .background(AdaptTheme.colors.primaryAccentContainer.copy(alpha = 0.35f))
                            .border(
                                AdaptTheme.stroke.hairline,
                                AdaptTheme.colors.primaryAccent,
                                RoundedCornerShape(AdaptTheme.radius.extraSmall)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AdaptTheme.colors.primaryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                domain.suggestedTopics.take(3).forEach { topic ->
                    Text(
                        text = "• $topic",
                        style = AdaptTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = AdaptTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PREVIEWS
// -------------------------------------------------------------
@Preview(name = "Domain Explorer - Dark", showBackground = true)
@Composable
private fun DomainExplorerDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        DomainExplorerScreen(
            onNavigateBack = {},
            onStartOnboarding = {}
        )
    }
}

@Preview(name = "Domain Explorer - Light", showBackground = true)
@Composable
private fun DomainExplorerLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        DomainExplorerScreen(
            onNavigateBack = {},
            onStartOnboarding = {}
        )
    }
}
