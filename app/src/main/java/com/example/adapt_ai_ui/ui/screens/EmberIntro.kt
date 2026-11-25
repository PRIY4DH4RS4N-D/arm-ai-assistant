package com.example.adapt_ai_ui.ui.screens

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.adapt_ai_ui.ui.components.*

@Composable
internal fun EmberIntro(onFinish: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val time = remember { Animatable(0f) }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (!ValueAnimator.areAnimatorsEnabled()) time.snapTo(6.7f)
            else if (time.value < 6.7f) {
                time.animateTo(6.7f, tween(((6.7f - time.value) * 1000).toInt(), easing = LinearEasing))
            }
        }
    }
    BackHandler(onBack = onFinish)
    val seconds = time.value
    fun reveal(delay: Float, duration: Float) = ((seconds - delay) / duration).coerceIn(0f, 1f)
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF03050B))) {
        AuroraScene(Modifier.fillMaxSize().alpha(reveal(0f, 1.1f)), waves = true) {}
        val orbSize = (maxHeight * .44f).coerceIn(105.dp, 320.dp)
        Column(
            Modifier.align(Alignment.Center).safeDrawingPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuroraOrb(OrbState.IDLE, { 0f }, Modifier.size(orbSize)
                .alpha(reveal(.7f, 2.5f)).scale(.76f + .24f * reveal(.7f, 2.5f)))
            if (seconds > 3.2f) {
                Text("EMBER", Modifier.alpha(reveal(3.2f, 1f)), color = Color(0xFFF5F7FF), fontSize = 48.sp,
                    letterSpacing = 7.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Your personal AI learning intelligence", Modifier.alpha(reveal(4.2f, 1f)),
                    color = AuroraCyan, textAlign = TextAlign.Center)
                Text("Learn smarter. Grow faster.", Modifier.padding(top = 9.dp).alpha(reveal(4.9f, .8f)),
                    color = Color(0xFFA5B1C2), textAlign = TextAlign.Center)
            } else Spacer(Modifier.height(117.dp))
            Spacer(Modifier.height(25.dp))
            if (seconds >= 5.7f) {
                AuroraAction("Get started", onFinish, Modifier.alpha(reveal(5.7f, .75f)), icon = AdaptIcons.ArrowForward)
            } else Spacer(Modifier.height(50.dp))
        }
        TextButton(onFinish, Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(10.dp)) {
            Text("Skip intro", color = Color(0xFFCED9F2))
        }
    }
}
