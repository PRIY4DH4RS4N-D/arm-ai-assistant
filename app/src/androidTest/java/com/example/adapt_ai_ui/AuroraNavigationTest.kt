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
            waitForVisibleNode("Ask anything")
            val destinations = listOf(
                "Journey" to "LEARNING JOURNEY",
                "Tutor" to "AI Learning Tutor",
                "Practice" to "Practice & Revision",
                "Profile" to "Offline AI model",
                "Home" to "How can I help you learn today?"
            )
            for ((tab, screenTitle) in destinations) {
                val nav = waitForVisibleNode("$tab tab")
                assertTrue("Could not open $tab", nav.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                waitForVisibleNode(screenTitle)
            }
        }
    }

    private fun waitForVisibleNode(label: String): AccessibilityNodeInfo {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.elapsedRealtime() + 12_000
        while (SystemClock.elapsedRealtime() < deadline) {
            findNode(automation.rootInActiveWindow, label)?.let { return it }
            SystemClock.sleep(150)
        }
        error("Visible Android accessibility node not found: $label")
    }

    private fun findNode(node: AccessibilityNodeInfo?, label: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isVisibleToUser &&
            (node.text?.toString() == label || node.contentDescription?.toString() == label)
        ) return node
        for (index in 0 until node.childCount) {
            findNode(node.getChild(index), label)?.let { return it }
        }
        return null
    }
}
