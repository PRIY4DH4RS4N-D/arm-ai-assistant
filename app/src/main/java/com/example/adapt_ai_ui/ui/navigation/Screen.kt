package com.example.adapt_ai_ui.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Pathway : Screen("pathway", "Journey")
    object Tutor : Screen("tutor", "Tutor")
    object Profile : Screen("profile", "Profile")
    object Model : Screen("model", "Model Management")
    object Scan : Screen("scan", "Scan & Learn")
    object Activity : Screen("activity", "Activity & Achievements")
    object Coding : Screen("coding", "Coding Practice")
    object ProfileSettings : Screen("profile-settings", "Profile Settings")
    object Lesson : Screen("lesson", "Lesson")
    object Practice : Screen("practice", "Practice")
    object Explore : Screen("explore", "Explore Domains")
    object Archive : Screen("archive", "Original courses")
    object Workspace : Screen("workspace", "Topic")
    object Preferences : Screen("preferences", "Learning preferences")
    object Progress : Screen("progress", "Progress")
    object Onboarding : Screen("onboarding", "Personalized Onboarding")
}
