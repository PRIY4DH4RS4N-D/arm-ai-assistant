package com.example.adapt_ai_ui.ui.components

import android.animation.ValueAnimator
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

internal enum class OrbState(val speed: Double, val glow: Float) {
    IDLE(.66, .86f), LISTENING(1.04, 1f), THINKING(1.28, .92f), GENERATING(1.84, 1f), LOADING(.92, .85f),
    SPEAKING(.95, 1f), SUCCESS(.7, 1f), ERROR(.24, .75f)
}

@Composable
internal fun AuroraOrb(state: OrbState, amplitude: () -> Float, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var phase by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(lifecycle, state) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (!ValueAnimator.areAnimatorsEnabled()) return@repeatOnLifecycle
            var previous = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                phase += ((now - previous) / 1e9).coerceAtMost(.05) * state.speed
                previous = now
            }
        }
    }
    Spacer(modifier.drawWithCache {
        val center = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension * .382f
        val fullDetail = size.minDimension >= 96.dp.toPx()
        val circle = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(center.x-r, center.y-r, center.x+r, center.y+r))
        }
        val cyan = when (state) {
            OrbState.SUCCESS -> Color(0xFF72FFDD)
            OrbState.ERROR -> Color(0xFFFF83B8)
            else -> Color(0xFF64E9FF)
        }
        val deep = Color(0xFF031134)
        val glow = Brush.radialGradient(
            0f to Color(0xFF125FFF).copy(alpha = .76f), .48f to Color(0xFF145BFF).copy(alpha = .48f),
            .79f to Color(0xFF0C48FA).copy(alpha = .14f), 1f to Color.Transparent,
            center = center + Offset(r*.16f, r*.04f), radius = r*1.55f
        )
        val sphere = Brush.radialGradient(
            0f to Color(0xFF02051A), .56f to deep, .80f to Color(0xFF092881), 1f to Color(0xFF1755EA),
            center = center - Offset(r*.20f, r*.23f), radius = r*1.28f
        )
        val energy = Brush.linearGradient(
            0f to Color(0xFF254AFF), .27f to Color(0xFF33A9FF), .60f to Color(0xFF805BFF),
            .82f to cyan, 1f to Color(0xFFE8FCFF),
            start = center - Offset(r,r*.3f), end = center + Offset(r,r*.1f)
        )
        val rim = Brush.sweepGradient(
            0f to cyan, .12f to Color(0xFF3BAFFF), .22f to Color(0xFF765BFF),
            .43f to Color(0xFF449AFF), .55f to Color(0xFF1E5CFF), .72f to Color(0xFF60BFFF),
            .86f to Color(0xFFE2FBFF), .96f to Color(0xFFC3F9FF), 1f to cyan, center = center
        )
        val flare = Brush.radialGradient(
            listOf(Color.White.copy(alpha=.85f), cyan.copy(alpha=.58f), Color.Transparent),
            center + Offset(.91f*r,-.38f*r), r*.43f
        )
        val violet = Color(0xFFA886FF)
        val orbit = Brush.linearGradient(
            0f to Color(0xFF2446C4), .34f to violet, .68f to Color(0xFF38A5FF), 1f to cyan,
            start = center + Offset(-r, .85f * r), end = center + Offset(.95f * r, -.75f * r)
        )
        val wave = Brush.radialGradient(
            0f to Color.Transparent, .70f to Color.Transparent, .88f to Color(0xFF1868FF).copy(alpha = .22f),
            1f to Color.Transparent, center = center, radius = r * 1.38f
        )
        val spark = Brush.radialGradient(listOf(Color.White, cyan.copy(alpha = .66f), Color.Transparent),
            center = center, radius = r * .123f)
        val band = Path()
        val highlight = Path()
        fun point(x: Float, y: Float) = Offset(center.x + x*r, center.y + y*r)
        onDrawBehind {
            val t = phase
            val sway = sin(t*1.17).toFloat()
            val pulse = 1f + sway*.020f + if (state == OrbState.LISTENING) amplitude().coerceIn(0f,1f)*.10f else 0f
            translate(top = r*.040f*cos(t*.8).toFloat()) {
                drawCircle(glow, r*1.65f, alpha = state.glow)
                if (fullDetail) {
                    drawCircle(wave, r * 1.37f, alpha = .57f + .26f * sin(t * 1.61).toFloat())
                    for (index in 0 until 22) {
                        val angle = index * (2 * PI / 22) + sin(t * .31 + index) * .14 + t * .14
                        val radius = 1.03f + index % 5 * .057f + sin(t * .92 + index * 1.8).toFloat() * .055f
                        val opacity = (.25f + .24f * sin(t * 1.65 + index * 3.1).toFloat()).coerceAtLeast(.03f)
                        drawCircle(if (index % 4 == 0) violet else cyan,
                            r * (if (index % 7 == 0) .012f else .006f), point(cos(angle).toFloat() * radius, sin(angle).toFloat() * radius),
                            alpha = opacity * state.glow)
                    }
                }
                scale(pulse, pivot = center) {
                    drawCircle(sphere,r,center)
                    clipPath(circle) {
                        repeat(if (fullDetail) 7 else 4) { i ->
                            val offset = (i-3)*.085f
                            val motion = sin(t*1.62+i*.37).toFloat()*.19f
                            val a = point(-1.02f, .51f+offset)
                            val b = point(-.76f, .99f+motion+offset)
                            val c = point(-.03f, .18f+offset*.55f)
                            val d = point(.39f, .21f+motion+offset)
                            val e = point(.89f, -.10f+offset)
                            val f = point(1.04f,-.57f+motion)
                            val g = point(.53f,-.91f+offset)
                            band.reset()
                            band.moveTo(a.x,a.y)
                            band.cubicTo(b.x,b.y,c.x,c.y,d.x,d.y)
                            band.cubicTo(e.x,e.y,f.x,f.y,g.x,g.y)
                            val h = point(1.08f,-.40f+motion+offset)
                            val j = point(.38f,.52f+offset)
                            band.cubicTo(h.x,h.y,e.x,e.y,j.x,j.y)
                            band.cubicTo(c.x,c.y+.32f*r,b.x,b.y+.08f*r,a.x,a.y)
                            band.close()
                            drawPath(band,energy,alpha = if(i==3) .24f else .066f)
                            highlight.reset()
                            highlight.moveTo(a.x,a.y)
                            highlight.cubicTo(b.x,b.y,c.x,c.y,d.x,d.y)
                            highlight.cubicTo(e.x,e.y,f.x,f.y,g.x,g.y)
                            if (i==3) drawPath(highlight,energy,alpha=.25f, style=Stroke(r*.067f))
                            drawPath(highlight,energy,alpha=if(i==3) .97f else .27f, style=Stroke(r*(if(i==3) .014f else .006f)))
                        }
                        highlight.reset()
                        val a = point(-1f,.32f)
                        val b = point(-.94f,-.39f)
                        val c = point(-.2f,-.06f+sway*.16f)
                        val d = point(.07f,-.49f)
                        val h = point(.34f,-.93f)
                        val j = point(.51f,-.92f)
                        val k = point(.73f,-.69f)
                        highlight.moveTo(a.x,a.y)
                        highlight.cubicTo(b.x,b.y,c.x,c.y,d.x,d.y)
                        highlight.cubicTo(h.x,h.y,j.x,j.y,k.x,k.y)
                        drawPath(highlight,energy,alpha=.19f,style=Stroke(r*.058f))
                        drawPath(highlight,energy,alpha=.92f,style=Stroke(r*.011f))
                        if (fullDetail) {
                            val lift = sin(t * 1.36 + 1.4).toFloat() * .14f
                            val twist = cos(t * .83 + .3).toFloat() * .22f
                            val left = point(-1.05f, -.39f + lift)
                            val upperKnot = point(-.24f, -.68f + twist)
                            val lowerKnot = point(.28f, .51f + lift)
                            val right = point(1.02f, .38f - lift)
                            band.reset()
                            band.moveTo(left.x, left.y)
                            band.cubicTo(upperKnot.x, upperKnot.y, lowerKnot.x, lowerKnot.y, right.x, right.y)
                            drawPath(band, orbit, alpha = .15f, style = Stroke(r * .13f, cap = StrokeCap.Round))
                            drawPath(band, orbit, alpha = .34f, style = Stroke(r * .043f, cap = StrokeCap.Round))
                            drawPath(band, orbit, alpha = .93f, style = Stroke(r * .009f, cap = StrokeCap.Round))
                            val leading = .5f + .43f * sin(t * 1.37).toFloat()
                            val back = 1f - leading
                            val knot = left * (back * back * back) + upperKnot * (3 * back * back * leading) +
                                lowerKnot * (3 * back * leading * leading) + right * (leading * leading * leading)
                            translate(knot.x - center.x, knot.y - center.y) {
                                drawCircle(spark, r * .123f, alpha = .75f)
                                drawCircle(Color.White, r * .017f, alpha = .82f)
                            }
                        }
                        drawCircle(flare,r*.43f,center+Offset(.91f*r,-.38f*r))
                        repeat(if (fullDetail) 16 else 5) { i ->
                            val x=sin(i*5.13 + t*.17).toFloat()*.89f
                            val y=cos(i*2.71 - t*.12).toFloat()*.85f
                            drawCircle(cyan,r*(.004f+i%4*.0015f),point(x,y),alpha=.26f+.21f*sin(t*.9+i).toFloat())
                        }
                    }
                    val degrees = (t * 32 % 360).toFloat()
                    rotate(degrees, pivot = center) {
                        drawCircle(rim,r,center,alpha=.36f,style=Stroke(r*.065f))
                        drawCircle(rim,r,center,alpha=.93f,style=Stroke(r*.013f))
                    }
                    if (fullDetail) {
                        val angle = t * .83 - .78
                        val lead = point(cos(angle).toFloat(), sin(angle).toFloat())
                        translate(lead.x-center.x, lead.y-center.y) {
                            drawCircle(spark, r * .123f, alpha = if (state == OrbState.GENERATING) .96f else .65f)
                            drawCircle(Color.White, r * .016f, alpha = .88f)
                        }
                    }
                }
            }
        }
    })
}
