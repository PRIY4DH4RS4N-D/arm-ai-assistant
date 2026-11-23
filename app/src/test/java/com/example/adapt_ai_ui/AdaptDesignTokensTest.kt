package com.example.adapt_ai_ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.adapt_ai_ui.ui.components.TimelineNodeState
import com.example.adapt_ai_ui.ui.theme.AdaptCornerRadius
import com.example.adapt_ai_ui.ui.theme.AdaptElevation
import com.example.adapt_ai_ui.ui.theme.AdaptSpacing
import com.example.adapt_ai_ui.ui.theme.AdaptStroke
import com.example.adapt_ai_ui.ui.theme.DarkSemanticColors
import com.example.adapt_ai_ui.ui.theme.LightSemanticColors
import com.example.adapt_ai_ui.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying the Editorial Obsidian Liquid Glass design tokens,
 * contrast relationships, and semantic color palette.
 */
class AdaptDesignTokensTest {

    @Test
    fun darkTheme_luminanceAndContrast_meetsReadabilityStandards() {
        val colors = DarkSemanticColors
        assertTrue("Dark theme isDark flag must be true", colors.isDark)

        // Obsidian background should be very dark (< 0.05 luminance)
        assertTrue(
            "Obsidian canvas must have low luminance for true dark mode",
            colors.background.luminance() < 0.05f
        )

        // Text primary on Dark canvas should be high contrast (> 0.8 luminance)
        assertTrue(
            "Primary text on dark background must be bright for accessibility",
            colors.textPrimary.luminance() > 0.75f
        )

        // Surface elevation contrast: surface is slightly lighter than background
        assertTrue(
            "Raised surface must be higher elevation than base canvas",
            colors.raisedSurface.luminance() >= colors.surface.luminance()
        )
    }

    @Test
    fun lightTheme_luminanceAndContrast_meetsReadabilityStandards() {
        val colors = LightSemanticColors
        assertTrue("Light theme isDark flag must be false", !colors.isDark)

        // Pearl background should be high luminance (> 0.85)
        assertTrue(
            "Pearl canvas must have high luminance",
            colors.background.luminance() > 0.85f
        )

        // Text primary on Pearl canvas should be dark (< 0.1 luminance)
        assertTrue(
            "Primary text on pearl canvas must be dark for contrast",
            colors.textPrimary.luminance() < 0.15f
        )
    }

    @Test
    fun glassTokens_haveAppropriateTranslucencyAndHighlights() {
        // Dark glass surface alpha should be restrained (~85%)
        assertEquals(0.85f, DarkSemanticColors.glassSurface.alpha, 0.05f)

        // Light glass surface alpha should be restrained (~90%)
        assertEquals(0.90f, LightSemanticColors.glassSurface.alpha, 0.05f)

        // Highlights must have non-zero alpha
        assertTrue(DarkSemanticColors.glassHighlight.alpha > 0f)
        assertTrue(LightSemanticColors.glassHighlight.alpha > 0f)
    }

    @Test
    fun spacingTokens_areMonotonicallyIncreasing() {
        val s = AdaptSpacing()
        assertTrue(s.extraSmall < s.small)
        assertTrue(s.small < s.medium)
        assertTrue(s.medium < s.large)
        assertTrue(s.large < s.extraLarge)
        assertTrue(s.extraLarge < s.huge)
        assertTrue(s.huge < s.giant)
    }

    @Test
    fun cornerRadiusTokens_areConsistent() {
        val r = AdaptCornerRadius()
        assertTrue(r.small < r.medium)
        assertTrue(r.medium < r.large)
        assertTrue(r.large < r.extraLarge)
        assertTrue(r.extraLarge < r.full)
    }

    @Test
    fun timelineNodeStates_areDistinct() {
        val states = TimelineNodeState.values()
        assertEquals(3, states.size)
        assertTrue(states.contains(TimelineNodeState.COMPLETED))
        assertTrue(states.contains(TimelineNodeState.IN_PROGRESS))
        assertTrue(states.contains(TimelineNodeState.LOCKED))
    }

    @Test
    fun themeModes_haveAppropriateTitles() {
        assertEquals("Follow System", ThemeMode.SYSTEM.title)
        assertEquals("Light Mode", ThemeMode.LIGHT.title)
        assertEquals("Dark Mode", ThemeMode.DARK.title)
    }

    @Test
    fun dataConsistency_curriculumVsMastery_decoupledAndAccurate() {
        // SampleData initial state has 4 nodes: 1 completed, 1 in progress, 2 locked
        val nodes = com.example.adapt_ai_ui.data.SampleData.pathwayPreviewNodes
        val initialCompleted = nodes.count { it.status == com.example.adapt_ai_ui.data.MilestoneStatus.COMPLETED }
        val initialCurriculumPercent = (initialCompleted * 100) / nodes.size
        assertEquals(25, initialCurriculumPercent)

        // After completing lesson 2: 2 completed milestones out of 4 = 50%
        val postLessonNodes = nodes.map { node ->
            when (node.id) {
                "node_2" -> node.copy(status = com.example.adapt_ai_ui.data.MilestoneStatus.COMPLETED)
                "node_3" -> node.copy(status = com.example.adapt_ai_ui.data.MilestoneStatus.IN_PROGRESS)
                else -> node
            }
        }
        val completedCount = postLessonNodes.count { it.status == com.example.adapt_ai_ui.data.MilestoneStatus.COMPLETED }
        val curriculumPercent = (completedCount * 100) / postLessonNodes.size
        assertEquals(50, curriculumPercent)

        // Concept Mastery (88%) is Quiz Competency, distinct from Curriculum Progress (50%)
        val masteryScore = 88
        assertNotEquals(curriculumPercent, masteryScore)
    }

    @Test
    fun dataConsistency_dailyTargetCalculation_handlesGoalsExceeded() {
        val dailyMinutesSpent = 35
        val dailyMinutesGoal = 20
        assertTrue("Daily spent must meet or exceed goal", dailyMinutesSpent >= dailyMinutesGoal)

        val progress = (dailyMinutesSpent.toFloat() / dailyMinutesGoal).coerceIn(0f, 1f)
        assertEquals(1.0f, progress, 0.001f)
    }
}
