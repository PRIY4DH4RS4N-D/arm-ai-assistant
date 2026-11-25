package com.example.adapt_ai_ui

import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test

class AuroraNavigationTest {
    @Test
    fun homeAndFiveTabsRemainReachable() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForVisibleNode("How can I help you learn today?")
            waitForVisibleNode("Ask anything...")
            val destinations = listOf(
                "Journey" to listOf("Learning Journey", "LEARNING JOURNEY", "Explore subjects"),
                "Tutor" to listOf("AI Tutor"),
                "Practice" to listOf("Practice", "Practice Session", "Practice & Revision", "Explore subjects"),
                "Profile" to listOf("My Profile"),
                "Home" to listOf("How can I help you learn today?")
            )
            for ((tab, screenTitle) in destinations) {
                val nav = waitForVisibleNode("$tab tab")
                assertTrue("Could not open $tab", nav.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                waitForVisibleNode(*screenTitle.toTypedArray())
            }
        }
    }

    private fun waitForVisibleNode(vararg labels: String): AccessibilityNodeInfo {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.elapsedRealtime() + 12_000
        while (SystemClock.elapsedRealtime() < deadline) {
            findNode(automation.rootInActiveWindow, labels.toSet())?.let { return it }
            SystemClock.sleep(150)
        }
        error("Visible Android accessibility node not found: ${labels.joinToString()}")
    }

    private fun findNode(node: AccessibilityNodeInfo?, labels: Set<String>): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isVisibleToUser &&
            (node.text?.toString() in labels || node.contentDescription?.toString() in labels)
        ) return node
        for (index in 0 until node.childCount) {
            findNode(node.getChild(index), labels)?.let { return it }
        }
        return null
    }
}
