package com.example.adapt_ai_ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AuroraNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeAndFiveTabsRemainReachable() {
        compose.onNodeWithText("How can I help you learn today?").assertIsDisplayed()
        compose.onNodeWithContentDescription("Ask anything").assertIsDisplayed()
        listOf("Journey", "Tutor", "Practice", "Profile", "Home").forEach {
            compose.onNodeWithContentDescription("$it tab").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("How can I help you learn today?").assertIsDisplayed()
    }
}
