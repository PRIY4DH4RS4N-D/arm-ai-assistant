package com.example.adapt_ai_ui.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AdaptIcons {
    val Play: ImageVector by lazy {
        ImageVector.Builder(
            name = "Play",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(8f, 5f)
                lineTo(19f, 12f)
                lineTo(8f, 19f)
                close()
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.5f
            ) {
                moveTo(4f, 12f)
                lineTo(9f, 17f)
                lineTo(20f, 6f)
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(7f, 11f)
                lineTo(17f, 11f)
                lineTo(17f, 20f)
                lineTo(7f, 20f)
                close()
                moveTo(9f, 11f)
                lineTo(9f, 7f)
                curveTo(9f, 5.34f, 10.34f, 4f, 12f, 4f)
                curveTo(13.66f, 4f, 15f, 5.34f, 15f, 7f)
                lineTo(15f, 11f)
            }
        }.build()
    }

    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 3f)
                lineTo(3f, 10f)
                lineTo(5f, 10f)
                lineTo(5f, 20f)
                lineTo(10f, 20f)
                lineTo(10f, 14f)
                lineTo(14f, 14f)
                lineTo(14f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 10f)
                lineTo(21f, 10f)
                close()
            }
        }.build()
    }

    val Pathway: ImageVector by lazy {
        ImageVector.Builder(
            name = "Pathway",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(4f, 19f)
                curveTo(8f, 19f, 8f, 12f, 12f, 12f)
                curveTo(16f, 12f, 16f, 5f, 20f, 5f)
            }
            path(fill = SolidColor(Color.White)) {
                // Milestone circles
                moveTo(4f, 19f)
                lineTo(4f, 18f)
                moveTo(12f, 12f)
                lineTo(12f, 11f)
                moveTo(20f, 5f)
                lineTo(20f, 4f)
            }
        }.build()
    }

    val Tutor: ImageVector by lazy {
        ImageVector.Builder(
            name = "Tutor",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                // Speech bubble with AI spark
                moveTo(4f, 6f)
                curveTo(4f, 4.9f, 4.9f, 4f, 6f, 4f)
                lineTo(18f, 4f)
                curveTo(19.1f, 4f, 20f, 4.9f, 20f, 6f)
                lineTo(20f, 15f)
                curveTo(20f, 16.1f, 19.1f, 17f, 18f, 17f)
                lineTo(8f, 17f)
                lineTo(4f, 21f)
                close()
            }
        }.build()
    }

    val Profile: ImageVector by lazy {
        ImageVector.Builder(
            name = "Profile",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                // Head
                moveTo(12f, 12f)
                curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
                curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
                curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
                curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
                close()
                // Shoulders
                moveTo(5f, 20f)
                curveTo(5f, 16.5f, 8f, 14.5f, 12f, 14.5f)
                curveTo(16f, 14.5f, 19f, 16.5f, 19f, 20f)
            }
        }.build()
    }

    val ArrowForward: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowForward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(5f, 12f)
                lineTo(19f, 12f)
                moveTo(13f, 6f)
                lineTo(19f, 12f)
                lineTo(13f, 18f)
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(19f, 12f)
                lineTo(5f, 12f)
                moveTo(11f, 6f)
                lineTo(5f, 12f)
                lineTo(11f, 18f)
            }
        }.build()
    }

    val Sparkle: ImageVector by lazy {
        ImageVector.Builder(
            name = "Sparkle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 2f)
                lineTo(14.5f, 9.5f)
                lineTo(22f, 12f)
                lineTo(14.5f, 14.5f)
                lineTo(12f, 22f)
                lineTo(9.5f, 14.5f)
                lineTo(2f, 12f)
                lineTo(9.5f, 9.5f)
                close()
            }
        }.build()
    }

    val ChevronDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(6f, 9f)
                lineTo(12f, 15f)
                lineTo(18f, 9f)
            }
        }.build()
    }

    val ChevronUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(6f, 15f)
                lineTo(12f, 9f)
                lineTo(18f, 15f)
            }
        }.build()
    }

    val Lightbulb: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lightbulb",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(9f, 21f)
                lineTo(15f, 21f)
                moveTo(10f, 18f)
                lineTo(14f, 18f)
                moveTo(12f, 3f)
                curveTo(8.13f, 3f, 5f, 6.13f, 5f, 10f)
                curveTo(5f, 12.38f, 6.19f, 14.47f, 8f, 15.74f)
                lineTo(8f, 18f)
                lineTo(16f, 18f)
                lineTo(16f, 15.74f)
                curveTo(17.81f, 14.47f, 19f, 12.38f, 19f, 10f)
                curveTo(19f, 6.13f, 15.87f, 3f, 12f, 3f)
                close()
            }
        }.build()
    }

    val Retry: ImageVector by lazy {
        ImageVector.Builder(
            name = "Retry",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
                curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
                moveTo(18f, 4f)
                lineTo(18f, 9f)
                lineTo(13f, 9f)
            }
        }.build()
    }

    val Cross: ImageVector by lazy {
        ImageVector.Builder(
            name = "Cross",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.5f
            ) {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                moveTo(18f, 6f)
                lineTo(6f, 18f)
            }
        }.build()
    }

    val Notebook: ImageVector by lazy {
        ImageVector.Builder(
            name = "Notebook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(5f, 4f)
                curveTo(5f, 2.9f, 5.9f, 2f, 7f, 2f)
                lineTo(19f, 2f)
                curveTo(20.1f, 2f, 21f, 2.9f, 21f, 4f)
                lineTo(21f, 20f)
                curveTo(21f, 21.1f, 20.1f, 22f, 19f, 22f)
                lineTo(7f, 22f)
                curveTo(5.9f, 22f, 5f, 21.1f, 5f, 20f)
                close()
                moveTo(5f, 7f)
                lineTo(8f, 7f)
                moveTo(5f, 12f)
                lineTo(8f, 12f)
                moveTo(5f, 17f)
                lineTo(8f, 17f)
                moveTo(11f, 8f)
                lineTo(17f, 8f)
                moveTo(11f, 12f)
                lineTo(17f, 12f)
            }
        }.build()
    }

    val Compass: ImageVector by lazy {
        ImageVector.Builder(
            name = "Compass",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(14.5f, 9.5f)
                lineTo(13f, 14f)
                lineTo(9.5f, 14.5f)
                lineTo(11f, 10f)
                close()
            }
        }.build()
    }

    val Target: ImageVector by lazy {
        ImageVector.Builder(
            name = "Target",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(12f, 4f)
                curveTo(7.58f, 4f, 4f, 7.58f, 4f, 12f)
                curveTo(4f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(16.42f, 20f, 20f, 16.42f, 20f, 12f)
                curveTo(20f, 7.58f, 16.42f, 4f, 12f, 4f)
                close()
                moveTo(12f, 8f)
                curveTo(9.79f, 8f, 8f, 9.79f, 8f, 12f)
                curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f)
                curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
                curveTo(16f, 9.79f, 14.21f, 8f, 12f, 8f)
                close()
            }
        }.build()
    }

    val Copy: ImageVector by lazy {
        ImageVector.Builder(
            name = "Copy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                // Back card
                moveTo(8f, 4f)
                lineTo(16f, 4f)
                curveTo(17.1f, 4f, 18f, 4.9f, 18f, 6f)
                lineTo(18f, 14f)
                // Front card
                moveTo(6f, 8f)
                lineTo(14f, 8f)
                curveTo(15.1f, 8f, 16f, 8.9f, 16f, 10f)
                lineTo(16f, 18f)
                curveTo(16f, 19.1f, 15.1f, 20f, 14f, 20f)
                lineTo(6f, 20f)
                curveTo(4.9f, 20f, 4f, 19.1f, 4f, 18f)
                lineTo(4f, 10f)
                curveTo(4f, 8.9f, 4.9f, 8f, 6f, 8f)
                close()
            }
        }.build()
    }

    val Trash: ImageVector by lazy {
        ImageVector.Builder(
            name = "Trash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(3f, 6f)
                lineTo(21f, 6f)
                moveTo(8f, 6f)
                lineTo(8f, 4f)
                curveTo(8f, 3.45f, 8.45f, 3f, 9f, 3f)
                lineTo(15f, 3f)
                curveTo(15.55f, 3f, 16f, 3.45f, 16f, 4f)
                lineTo(16f, 6f)
                moveTo(19f, 6f)
                lineTo(18.1f, 19.1f)
                curveTo(18.05f, 19.6f, 17.65f, 20f, 17.15f, 20f)
                lineTo(6.85f, 20f)
                curveTo(6.35f, 20f, 5.95f, 19.6f, 5.9f, 19.1f)
                lineTo(5f, 6f)
                moveTo(10f, 11f)
                lineTo(10f, 16f)
                moveTo(14f, 11f)
                lineTo(14f, 16f)
            }
        }.build()
    }

    val Code: ImageVector by lazy {
        ImageVector.Builder(
            name = "Code",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(16f, 18f)
                lineTo(22f, 12f)
                lineTo(16f, 6f)
                moveTo(8f, 6f)
                lineTo(2f, 12f)
                lineTo(8f, 18f)
            }
        }.build()
    }

    val Bookmark: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bookmark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White)
            ) {
                moveTo(17f, 3f)
                lineTo(7f, 3f)
                curveTo(5.9f, 3f, 5f, 3.9f, 5f, 5f)
                lineTo(5f, 21f)
                lineTo(12f, 18f)
                lineTo(19f, 21f)
                lineTo(19f, 5f)
                curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
                close()
            }
        }.build()
    }

    val BookmarkBorder: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookmarkBorder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(17f, 3f)
                lineTo(7f, 3f)
                curveTo(5.9f, 3f, 5f, 3.9f, 5f, 5f)
                lineTo(5f, 21f)
                lineTo(12f, 18f)
                lineTo(19f, 21f)
                lineTo(19f, 5f)
                curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
                close()
            }
        }.build()
    }

    val Plus: ImageVector by lazy {
        ImageVector.Builder(
            name = "Plus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(12f, 5f)
                lineTo(12f, 19f)
                moveTo(5f, 12f)
                lineTo(19f, 12f)
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f
            ) {
                moveTo(11f, 19f)
                curveTo(15.42f, 19f, 19f, 15.42f, 19f, 11f)
                curveTo(19f, 6.58f, 15.42f, 3f, 11f, 3f)
                curveTo(6.58f, 3f, 3f, 6.58f, 3f, 11f)
                curveTo(3f, 15.42f, 6.58f, 19f, 11f, 19f)
                close()
                moveTo(21f, 21f)
                lineTo(16.65f, 16.65f)
            }
        }.build()
    }
}
