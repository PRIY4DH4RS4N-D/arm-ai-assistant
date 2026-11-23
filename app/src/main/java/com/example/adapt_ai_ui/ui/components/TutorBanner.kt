package com.example.adapt_ai_ui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

@Composable
fun TutorBanner(
    prompts: List<TutorQuickPrompt>,
    onBannerClick: () -> Unit,
    onPromptClick: (TutorQuickPrompt) -> Unit,
    modifier: Modifier = Modifier
) {
    AdaptSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onBannerClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "AI Tutor Banner: Tap to open AI guide or select a quick prompt"
            },
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AdaptTheme.colors.primaryAccentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AdaptIcons.Sparkle,
                            contentDescription = null,
                            tint = AdaptTheme.colors.primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Socratic AI Tutor",
                        style = AdaptTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AdaptTheme.colors.textPrimary
                    )
                }

                // Honest Prototype Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdaptTheme.radius.extraSmall))
                        .background(AdaptTheme.colors.secondaryAccentContainer.copy(alpha = 0.25f))
                        .border(
                            width = AdaptTheme.stroke.hairline,
                            color = AdaptTheme.colors.secondaryAccent.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(AdaptTheme.radius.extraSmall)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ON-DEVICE READY",
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = AdaptTheme.colors.secondaryAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Have questions on this concept? Ask your on-device AI guide for a step-by-step hint.",
                style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Prompt Chips (horizontal scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prompts.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.full))
                            .background(AdaptTheme.colors.raisedSurface)
                            .border(
                                width = AdaptTheme.stroke.hairline,
                                color = AdaptTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(AdaptTheme.radius.full)
                            )
                            .clickable(
                                role = Role.Button,
                                onClick = { onPromptClick(prompt) }
                            )
                            .semantics {
                                contentDescription = "Ask tutor: ${prompt.prompt}"
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt.prompt,
                            style = AdaptTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AdaptTheme.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "TutorBanner - Dark", showBackground = true)
@Composable
private fun TutorBannerDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        TutorBanner(
            prompts = listOf(
                TutorQuickPrompt("1", "Why INT8?", "quantization"),
                TutorQuickPrompt("2", "Explain Scale S", "quantization")
            ),
            onBannerClick = {},
            onPromptClick = {}
        )
    }
}
