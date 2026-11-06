/*
 * The CamperMax logo: a tent outline over a faint "vector-grid" background, with
 * a campfire inside and grid nodes at the tent's corners, above the app name.
 * Shown on the Welcome screen. The launcher icon (res/drawable/ic_launcher_*.xml)
 * draws the same shapes, so change both together.
 *
 * Outstanding issues: none known.
 */
package com.example.project_halfway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The logo's colours (the launcher icon uses the same ones). */
internal object LogoColors {
    /** Dark navy background. */
    val DarkBg = Color(0xFF0F172A)
    /** Emerald for the grid, the tent opening and the corner nodes. */
    val GridGreen = Color(0xFF10B981)
    /** Orange tent outline. */
    val TentOrange = Color(0xFFF97316)
    /** Yellow campfire. */
    val FireYellow = Color(0xFFFBBF24)
    /** Off-white app name. */
    val OffWhite = Color(0xFFF8FAFC)
    /** Muted grey for the "BY VECTOR-GRID" line. */
    val MutedText = Color(0xFF94A3B8)
}

/**
 * The logo mark on its own: grid, tent, campfire and corner nodes. It has a
 * transparent background, so it can sit on any dark surface.
 *
 * @param modifier modifier for the drawing area
 * @param size width and height of the mark
 */
@Composable
fun CampermaxLogoMark(modifier: Modifier = Modifier, size: Dp = 160.dp) {
    Canvas(modifier = modifier.size(size).testTag("campermax_logo")) {
        val width = this.size.width
        val height = this.size.height

        // 1. Faint grid lines (the "vector-grid" background)
        val gridSpacing = width / 6
        for (i in 1..5) {
            drawLine(
                color = LogoColors.GridGreen.copy(alpha = 0.15f),
                start = Offset(i * gridSpacing, 0f),
                end = Offset(i * gridSpacing, height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = LogoColors.GridGreen.copy(alpha = 0.15f),
                start = Offset(0f, i * gridSpacing),
                end = Offset(width, i * gridSpacing),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Tent outline
        val tentPath = Path().apply {
            moveTo(width * 0.50f, height * 0.15f) // peak
            lineTo(width * 0.85f, height * 0.75f) // bottom right
            lineTo(width * 0.15f, height * 0.75f) // bottom left
            close()
        }
        drawPath(
            path = tentPath,
            color = LogoColors.TentOrange,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Tent opening
        val innerPath = Path().apply {
            moveTo(width * 0.50f, height * 0.35f)
            lineTo(width * 0.70f, height * 0.75f)
            lineTo(width * 0.30f, height * 0.75f)
            close()
        }
        drawPath(path = innerPath, color = LogoColors.GridGreen)

        // 4. Campfire
        val flamePath = Path().apply {
            moveTo(width * 0.50f, height * 0.52f)
            cubicTo(
                width * 0.58f, height * 0.60f,
                width * 0.55f, height * 0.72f,
                width * 0.50f, height * 0.72f
            )
            cubicTo(
                width * 0.45f, height * 0.72f,
                width * 0.42f, height * 0.60f,
                width * 0.50f, height * 0.52f
            )
            close()
        }
        drawPath(path = flamePath, color = LogoColors.FireYellow)

        // 5. Grid nodes at the tent's corners
        val nodeRadius = 4.dp.toPx()
        drawCircle(LogoColors.GridGreen, nodeRadius, Offset(width * 0.50f, height * 0.15f))
        drawCircle(LogoColors.GridGreen, nodeRadius, Offset(width * 0.85f, height * 0.75f))
        drawCircle(LogoColors.GridGreen, nodeRadius, Offset(width * 0.15f, height * 0.75f))
    }
}

/**
 * The mark on a dark navy rounded tile, the same as the launcher icon, so it
 * stands out on the green screens.
 *
 * @param modifier modifier for the tile
 * @param size width and height of the tile
 */
@Composable
fun CampermaxLogoTile(modifier: Modifier = Modifier, size: Dp = 140.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(LogoColors.DarkBg, RoundedCornerShape(size * 0.22f)),
        contentAlignment = Alignment.Center
    ) {
        CampermaxLogoMark(size = size * 0.86f)
    }
}

/**
 * The full logo: the tile, then "CAMPERMAX" and "BY VECTOR-GRID" under it.
 *
 * @param modifier modifier for the column holding the logo
 * @param tileSize size of the navy tile holding the mark
 * @param nameSize font size of "CAMPERMAX"; the tagline scales with it
 * @param nameColor colour of "CAMPERMAX"
 * @param taglineColor colour of "BY VECTOR-GRID"
 */
@Composable
fun CampermaxLogo(
    modifier: Modifier = Modifier,
    tileSize: Dp = 140.dp,
    nameSize: Int = 32,
    nameColor: Color = LogoColors.OffWhite,
    taglineColor: Color = LogoColors.MutedText
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CampermaxLogoTile(size = tileSize)
        Spacer(Modifier.height(nameSize.dp * 0.6f))
        Text(
            text = "CAMPERMAX",
            color = nameColor,
            fontSize = nameSize.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (nameSize / 16f).sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "BY VECTOR-GRID",
            color = taglineColor,
            fontSize = (nameSize * 0.375f).sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (nameSize * 0.09f).sp
        )
    }
}

// Preview of the logo on the app's green
@Preview(showBackground = true)
@Composable
private fun CampermaxLogoPreview() {
    Column(
        modifier = Modifier.fillMaxSize().background(AppGreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CampermaxLogo(nameColor = Color.White, taglineColor = Color.White.copy(alpha = 0.8f))
    }
}
