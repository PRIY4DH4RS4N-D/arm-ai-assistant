package com.example.adapt_ai_ui.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.adapt_ai_ui.ui.theme.AdaptTheme

internal val AuroraCyan = Color(0xFF36DFFF)
internal val AuroraBlue = Color(0xFF3D86FF)
internal val AuroraViolet = Color(0xFF926CFF)
internal val AuroraGreen = Color(0xFF67EDAD)

internal val auroraBase: Color
    @Composable get() = if (AdaptTheme.isDark) Color(0xFF02050F) else Color(0xFFF7FAFF)
internal val auroraPanel: Color
    @Composable get() = if (AdaptTheme.isDark) Color(0xFF0A142A) else Color(0xFFECF4FF)
internal val auroraRaised: Color
    @Composable get() = if (AdaptTheme.isDark) Color(0xFF111E39) else Color(0xFFE2EDFF)
internal val auroraLine: Color
    @Composable get() = if (AdaptTheme.isDark) Color(0xFF20375E) else Color(0xFFB8CCEF)
internal val auroraAccent: Color
    @Composable get() = if (AdaptTheme.isDark) AuroraCyan else Color(0xFF174CB3)

@Composable
internal fun AuroraScene(modifier: Modifier = Modifier, waves: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    val dark = AdaptTheme.isDark
    val base = auroraBase
    Box(modifier.background(base)) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Brush.radialGradient(
                listOf(Color(0xFF0A58FF).copy(alpha = if (dark) .16f else .10f), Color.Transparent),
                center = Offset(size.width * .67f, size.height * .16f), radius = size.width * .80f
            ))
            drawRect(Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xFF1844C4).copy(alpha = if (dark) .13f else .08f),
                    Color(0xFF217BEB).copy(alpha = if (dark) .28f else .14f)),
                startY = size.height * .66f, endY = size.height
            ))
            if (waves) repeat(8) { index ->
                val step = index * size.height * .018f
                val path = Path().apply {
                    moveTo(-size.width * .12f, size.height * .91f + step)
                    cubicTo(size.width * .28f, size.height * .75f + step, size.width * .64f, size.height * 1.02f - step,
                        size.width * 1.12f, size.height * .77f + step)
                }
                drawPath(path, Brush.horizontalGradient(
                    listOf(Color.Transparent, AuroraBlue.copy(alpha = .32f), AuroraCyan.copy(alpha = .52f), Color.Transparent)
                ), style = Stroke((if (index == 4) 2.2f else 1f).dp.toPx()))
            }
        }
        content()
    }
}

@Composable
internal fun AuroraPanel(
    modifier: Modifier = Modifier, accent: Color? = null, onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(17.dp)
    val dark = AdaptTheme.isDark
    val panel = auroraPanel
    val surface = if (accent != null) listOf(
        accent.copy(alpha = if (dark) .20f else .13f).let { androidx.compose.ui.graphics.lerp(panel, it.copy(alpha = 1f), it.alpha) },
        panel
    ) else listOf(panel, panel.copy(alpha = .90f))
    Column(
        modifier.clip(shape).background(Brush.linearGradient(surface))
            .border(1.dp, accent?.copy(alpha = if (dark) .40f else .56f) ?: auroraLine, shape)
            .then(if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick))
            .padding(14.dp),
        content = content
    )
}

@Composable
internal fun AuroraAction(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, icon: ImageVector? = null, violet: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    val fill = if (enabled) listOf(Color(0xFF087ED1), Color(0xFF1348BF),
        if (violet) Color(0xFF6537CE) else Color(0xFF0B347D)) else listOf(auroraRaised, auroraPanel)
    Row(
        modifier.heightIn(min = 50.dp).clip(shape).background(Brush.horizontalGradient(fill))
            .border(1.dp, if (enabled) AuroraCyan.copy(alpha = .8f) else auroraLine, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (enabled) Color.White else AdaptTheme.colors.textMuted,
            style = AdaptTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (icon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun AuroraPill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                        active: Boolean = false, enabled: Boolean = true, horizontalPadding: Dp = 13.dp) {
    val shape = RoundedCornerShape(23.dp)
    Box(modifier.heightIn(min = 42.dp).clip(shape)
        .background(if (active) auroraAccent.copy(alpha = .16f) else auroraPanel)
        .border(1.dp, if (active) auroraAccent.copy(alpha = .68f) else auroraLine, shape)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .semantics { selected = active }.padding(horizontal = horizontalPadding, vertical = 8.dp),
        contentAlignment = Alignment.Center) {
        Text(label, color = if (active) auroraAccent else AdaptTheme.colors.textPrimary,
            textAlign = TextAlign.Center, style = AdaptTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun AuroraMedallion(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    val ink = if (AdaptTheme.isDark) color else auroraAccent
    Box(modifier.clip(CircleShape)
        .background(Brush.radialGradient(listOf(ink.copy(alpha = .27f), ink.copy(alpha = .04f))))
        .border(1.dp, ink.copy(alpha = .48f), CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = ink, modifier = Modifier.fillMaxSize(.51f))
    }
}

@Composable
internal fun AuroraHeader(title: String, onBack: (() -> Unit)? = null, caption: String? = null,
                          trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onBack, Modifier.offset(x = (-8).dp)) {
            Icon(AdaptIcons.ArrowBack, "Back", tint = AdaptTheme.colors.textPrimary, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = AdaptTheme.colors.textPrimary, style = AdaptTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)
            caption?.let { Text(it, color = AdaptTheme.colors.textSecondary, style = AdaptTheme.typography.bodySmall) }
        }
        trailing()
    }
}
