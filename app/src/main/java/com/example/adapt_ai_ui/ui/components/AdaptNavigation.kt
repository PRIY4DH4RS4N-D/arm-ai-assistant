package com.example.adapt_ai_ui.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adapt_ai_ui.ui.navigation.Screen
import com.example.adapt_ai_ui.ui.theme.ADAPTAIUITheme
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import com.example.adapt_ai_ui.ui.theme.ThemeMode

data class NavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String = screen.title
)

/**
 * Editorial Obsidian Liquid Glass Bottom Navigation Bar.
 *
 * Translucent glass surface docking seamlessly with system insets,
 * featuring a delicate specular top hairline border and refined active pill indicators.
 * Visibly displays all five first-class destinations: Home, Journey, Tutor, Practice, Profile.
 */
@Composable
fun AdaptBottomNav(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(Screen.Home, AdaptIcons.Home, "Home"),
        NavItem(Screen.Pathway, AdaptIcons.Pathway, "Journey"),
        NavItem(Screen.Tutor, AdaptIcons.Tutor, "Tutor"),
        NavItem(Screen.Practice, AdaptIcons.Target, "Practice"),
        NavItem(Screen.Profile, AdaptIcons.Profile, "Profile")
    )

    val shape = RoundedCornerShape(topStart = 21.dp, topEnd = 21.dp)
    Box(
        modifier = modifier.fillMaxWidth().background(auroraBase)
            .padding(horizontal = 4.dp).clip(shape)
            .background(Brush.verticalGradient(listOf(auroraPanel, auroraBase)))
            .border(1.dp, auroraLine, shape).navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen ||
                    (currentScreen == Screen.Progress && item.screen == Screen.Pathway)
                val selectionColor by animateColorAsState(
                    if (isSelected) auroraAccent.copy(alpha = .13f) else Color.Transparent,
                    label = "Selected navigation tab"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(AdaptTheme.radius.medium))
                        .clickable(
                            role = Role.Tab,
                            onClick = { onScreenSelected(item.screen) }
                        )
                        .semantics {
                            selected = isSelected
                            contentDescription = "${item.label} tab"
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AdaptTheme.radius.full))
                            .background(selectionColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected)
                                auroraAccent
                            else
                                AdaptTheme.colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.label,
                        style = AdaptTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected)
                            auroraAccent
                        else
                            AdaptTheme.colors.textMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================================================
// Previews for Five-Destination Bottom Navigation
// ============================================================================

@Preview(name = "Bottom Nav 5 Items - Dark Mode", showBackground = true)
@Composable
fun AdaptBottomNavDarkPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.DARK) {
        AdaptBottomNav(
            currentScreen = Screen.Practice,
            onScreenSelected = {}
        )
    }
}

@Preview(name = "Bottom Nav 5 Items - Light Mode", showBackground = true)
@Composable
fun AdaptBottomNavLightPreview() {
    ADAPTAIUITheme(themeMode = ThemeMode.LIGHT) {
        AdaptBottomNav(
            currentScreen = Screen.Practice,
            onScreenSelected = {}
        )
    }
}
