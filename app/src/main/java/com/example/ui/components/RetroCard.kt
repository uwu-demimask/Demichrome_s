package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RetroTheme

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    badge: String? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = RetroTheme.palette

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(shape)
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(contentPadding)
    ) {
        Column {
            if (title != null || badge != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(palette.textAccent, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = title.uppercase(),
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 1.1.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, palette.textAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .background(palette.buttonBackground, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = badge.uppercase(),
                                color = palette.textAccent,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}
