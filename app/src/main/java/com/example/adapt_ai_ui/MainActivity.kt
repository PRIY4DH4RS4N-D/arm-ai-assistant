package com.example.adapt_ai_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.adapt_ai_ui.data.DomainRepository
import com.example.adapt_ai_ui.data.LearningDomain
import com.example.adapt_ai_ui.data.PracticeMode
import com.example.adapt_ai_ui.data.SampleData
import com.example.adapt_ai_ui.data.TutorQuickPrompt
import androidx.lifecycle.ViewModelProvider
import com.example.adapt_ai_ui.ui.components.AdaptBottomNav
import com.example.adapt_ai_ui.data.persistence.AdaptPersistence
import com.example.adapt_ai_ui.ui.navigation.Screen
import com.example.adapt_ai_ui.ui.screens.DomainExplorerScreen
import com.example.adapt_ai_ui.ui.screens.HomeScreen
import com.example.adapt_ai_ui.ui.screens.LessonScreen
import com.example.adapt_ai_ui.ui.screens.OnboardingScreen
import com.example.adapt_ai_ui.ui.screens.PathwayScreen
import com.example.adapt_ai_ui.ui.screens.PracticeScreen
import com.example.adapt_ai_ui.ui.screens.ProfileScreen
import com.example.adapt_ai_ui.ui.screens.TutorScreen
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.LocalThemeManager
import com.example.adapt_ai_ui.ui.theme.ThemeManager
import com.example.adapt_ai_ui.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent storage before UI composition
        AdaptPersistence.init(this)
        val tutorViewModel = ViewModelProvider(this)[TutorViewModel::class.java]

        val themeManager = ThemeManager(this)

        setContent {
            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                ADAPTAIUITheme(themeMode = themeManager.currentThemeMode) {
                    AdaptAppRoot(tutorViewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Ensure all pending asynchronous persistence writes are safely flushed to disk
        AdaptPersistence.flushBlocking()
    }
}

@Composable
fun AdaptAppRoot(tutorViewModel: TutorViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val backStack = remember { mutableStateListOf<Screen>() }

    var tutorInitialPrompt by remember { mutableStateOf<TutorQuickPrompt?>(null) }
    var practiceInitialMode by remember { mutableStateOf(PracticeMode.SESSION) }
    var selectedExploreDomain by remember {
        mutableStateOf<LearningDomain>(DomainRepository.getDomainById(SampleData.learnerProfile.activeDomainId))
    }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(currentScreen)
            currentScreen = screen
        }
    }

    fun navigateBack(fallback: Screen = Screen.Home) {
        while (backStack.isNotEmpty() && backStack.last() == currentScreen) {
            backStack.removeAt(backStack.lastIndex)
        }
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.lastIndex)
        } else {
            currentScreen = fallback
        }
    }

    val isFullScreenMode = currentScreen == Screen.Lesson ||
            currentScreen == Screen.Explore ||
            currentScreen == Screen.Onboarding

    // Intercept hardware/gesture back to navigate to the correct previous screen
    BackHandler(enabled = currentScreen != Screen.Home) {
        when (currentScreen) {
            Screen.Lesson -> navigateBack(fallback = Screen.Pathway)
            Screen.Explore -> navigateBack(fallback = Screen.Home)
            Screen.Onboarding -> navigateBack(fallback = Screen.Explore)
            else -> navigateBack(fallback = Screen.Home)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isFullScreenMode) {
                AdaptBottomNav(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        if (screen != Screen.Tutor) {
                            tutorInitialPrompt = null
                        }
                        if (screen == Screen.Practice) {
                            practiceInitialMode = PracticeMode.SESSION
                        }
                        if (currentScreen != screen) {
                            backStack.clear()
                            if (screen != Screen.Home) {
                                backStack.add(Screen.Home)
                            }
                            currentScreen = screen
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Home -> HomeScreen(
                        onNavigateToPathway = { navigateTo(Screen.Pathway) },
                        onNavigateToLesson = { navigateTo(Screen.Lesson) },
                        onNavigateToPractice = { mode ->
                            practiceInitialMode = mode
                            navigateTo(Screen.Practice)
                        },
                        onNavigateToTutor = { prompt ->
                            tutorInitialPrompt = prompt
                            navigateTo(Screen.Tutor)
                        },
                        onNavigateToProfile = { navigateTo(Screen.Profile) },
                        onNavigateToExplore = { navigateTo(Screen.Explore) }
                    )

                    Screen.Pathway -> PathwayScreen(
                        onNavigateBack = { navigateBack(fallback = Screen.Home) },
                        onNavigateToLesson = { navigateTo(Screen.Lesson) },
                        onNavigateToExplore = { navigateTo(Screen.Explore) },
                        onPractice = { mode ->
                            practiceInitialMode = mode
                            navigateTo(Screen.Practice)
                        },
                        onAskTutor = { prompt ->
                            tutorInitialPrompt = prompt
                            navigateTo(Screen.Tutor)
                        }
                    )

                    Screen.Lesson -> LessonScreen(
                        onNavigateBack = {
                            navigateBack(fallback = Screen.Pathway)
                        },
                        onAskTutor = { prompt ->
                            tutorInitialPrompt = prompt
                            navigateTo(Screen.Tutor)
                        },
                        onContinueToPractice = {
                            practiceInitialMode = PracticeMode.SESSION
                            navigateTo(Screen.Practice)
                        },
                        onCompleteLesson = {
                            SampleData.completeCurrentLesson()
                            // Clear any Lesson or Practice occurrences from back-stack
                            backStack.removeAll { it == Screen.Lesson || it == Screen.Practice }
                            // Completing a lesson navigates to the Learning Journey / Pathway
                            currentScreen = Screen.Pathway
                        }
                    )

                    Screen.Practice -> PracticeScreen(
                        initialMode = practiceInitialMode,
                        onNavigateBack = { navigateBack(fallback = Screen.Home) },
                        onSessionCompleted = { navigateBack(fallback = Screen.Home) },
                        onAskTutor = { prompt ->
                            tutorInitialPrompt = prompt
                            navigateTo(Screen.Tutor)
                        }
                    )

                    Screen.Tutor -> TutorScreen(
                        initialPrompt = tutorInitialPrompt,
                        viewModel = tutorViewModel,
                        onInitialPromptConsumed = { tutorInitialPrompt = null },
                        onOpenModelManager = { navigateTo(Screen.Profile) },
                        onNavigateBack = if (backStack.isNotEmpty()) {
                            { navigateBack(fallback = Screen.Home) }
                        } else null
                    )

                    Screen.Profile -> ProfileScreen(
                        onNavigateToExplore = { navigateTo(Screen.Explore) },
                        onNavigateToLesson = { navigateTo(Screen.Lesson) },
                        onNavigateToPractice = { mode ->
                            practiceInitialMode = mode
                            navigateTo(Screen.Practice)
                        },
                        onNavigateToPathway = { navigateTo(Screen.Pathway) }
                    )

                    Screen.Explore -> DomainExplorerScreen(
                        onNavigateBack = { navigateBack(fallback = Screen.Home) },
                        onStartOnboarding = { domain ->
                            selectedExploreDomain = domain
                            navigateTo(Screen.Onboarding)
                        }
                    )

                    Screen.Onboarding -> OnboardingScreen(
                        domain = selectedExploreDomain,
                        onNavigateBack = { navigateBack(fallback = Screen.Explore) },
                        onOnboardingCompleted = {
                            backStack.clear()
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }
}

@Preview(name = "Home - Dark Mode", showBackground = true)
@Composable
fun HomeDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            onNavigateToPathway = {},
            onNavigateToLesson = {},
            onNavigateToPractice = {},
            onNavigateToTutor = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(name = "Home - Light Mode", showBackground = true)
@Composable
fun HomeLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        HomeScreen(
            onNavigateToPathway = {},
            onNavigateToLesson = {},
            onNavigateToPractice = {},
            onNavigateToTutor = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(name = "Lesson - Dark Mode", showBackground = true)
@Composable
fun LessonDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        LessonScreen(
            onNavigateBack = {},
            onAskTutor = {},
            onContinueToPractice = {},
            onCompleteLesson = {}
        )
    }
}

@Preview(name = "Lesson - Light Mode", showBackground = true)
@Composable
fun LessonLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        LessonScreen(
            onNavigateBack = {},
            onAskTutor = {},
            onContinueToPractice = {},
            onCompleteLesson = {}
        )
    }
}

@Preview(name = "Practice - Dark Mode", showBackground = true)
@Composable
fun PracticeDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        PracticeScreen(
            onNavigateBack = {},
            onSessionCompleted = {}
        )
    }
}

@Preview(name = "Practice - Light Mode", showBackground = true)
@Composable
fun PracticeLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        PracticeScreen(
            onNavigateBack = {},
            onSessionCompleted = {}
        )
    }
}
