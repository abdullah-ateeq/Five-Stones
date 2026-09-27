package com.example.fivestones.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Thin line icons drawn to match the calm, fine-grid visual language. */
object AppIcons {
    val Back: ImageVector by lazy { lineIcon("back", "M15 18l-6-6 6-6") }
    val Home: ImageVector by lazy {
        lineIcon("home", "M3.5 10.5L12 3.5l8.5 7", "M5.5 9v11h13V9", "M10 20v-5.5h4V20")
    }
    val Undo: ImageVector by lazy {
        lineIcon("undo", "M8.5 14L4 9.5 8.5 5", "M4 9.5h10.5a5.25 5.25 0 0 1 0 10.5H10")
    }
    val Restart: ImageVector by lazy {
        lineIcon("restart", "M19.5 12a7.5 7.5 0 1 1-2.2-5.3", "M19.5 4.5v4h-4")
    }

    private fun lineIcon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.7f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
