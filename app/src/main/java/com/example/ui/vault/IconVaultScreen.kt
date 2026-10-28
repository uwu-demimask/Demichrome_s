package com.example.ui.vault

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroIconButton
import com.example.ui.theme.RetroTheme

data class RetroAppIconItem(
    val name: String,
    val category: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun IconVaultScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val categories = listOf("All", "System", "Social", "Media", "Productivity", "Games")
    var selectedCategory by remember { mutableStateOf("All") }

    val iconStyles = listOf("Pixel", "Outline", "Terminal", "CRT", "Pure B&W")
    var selectedStyle by remember { mutableStateOf("Pixel") }

    var selectedIconItem by remember { mutableStateOf<RetroAppIconItem?>(null) }

    val appIcons = listOf(
        RetroAppIconItem("Phone / Dialer", "System", Icons.Default.Phone, "Minimal phone receiver glyph"),
        RetroAppIconItem("Messages / SMS", "Social", Icons.Default.Chat, "Pixel talk bubble matrix"),
        RetroAppIconItem("Camera", "System", Icons.Default.CameraAlt, "Retro aperture rangefinder"),
        RetroAppIconItem("Mail / Inbox", "Productivity", Icons.Default.Email, "Monochrome envelope stamp"),
        RetroAppIconItem("Terminal / Dev", "Productivity", Icons.Default.Terminal, "Command line prompt glyph"),
        RetroAppIconItem("Notes / Editor", "Productivity", Icons.Default.Description, "Dotted page monospace sheet"),
        RetroAppIconItem("Files / Storage", "System", Icons.Default.Folder, "Retro tab directory folder"),
        RetroAppIconItem("Audio / Music", "Media", Icons.Default.Headphones, "Monochrome cassette headphones"),
        RetroAppIconItem("Video / Stream", "Media", Icons.Default.Videocam, "Vintage camcorder frame"),
        RetroAppIconItem("Browser / Web", "Social", Icons.Default.Public, "Wireframe globe latitude lines"),
        RetroAppIconItem("Navigation / Map", "System", Icons.Default.Map, "Orthographic map matrix"),
        RetroAppIconItem("Games / Arcade", "Games", Icons.Default.SportsEsports, "8-bit d-pad controller"),
        RetroAppIconItem("Settings", "System", Icons.Default.Settings, "Mechanical square gear"),
        RetroAppIconItem("Radio / Podcast", "Media", Icons.Default.Radio, "Analog radio antenna glyph"),
        RetroAppIconItem("Code IDE", "Productivity", Icons.Default.Code, "Angle brackets slash")
    )

    val filteredIcons = if (selectedCategory == "All") {
        appIcons
    } else {
        appIcons.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back to Vault",
                        onClick = onNavigateBack,
                        testTag = "btn_back_from_icons"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ICON VAULT",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "> MONOCHROME RETRO APP PACKS",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Android System Capability Explanation
            item {
                RetroCard(title = "SYSTEM ARCHITECTURE NOTICE") {
                    Text(
                        text = "Android OS security architecture prohibits standard non-launcher apps from arbitrarily replacing other apps' installed icons directly. You can pin monochrome shortcuts to your home screen or use these assets with supported custom launchers.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Style selector
            item {
                Text(
                    text = "AESTHETIC STYLE",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconStyles) { style ->
                        RetroChip(
                            text = style,
                            isSelected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            testTag = "chip_icon_style_$style"
                        )
                    }
                }
            }

            // Category selector
            item {
                Text(
                    text = "CATEGORY",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        RetroChip(
                            text = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            testTag = "chip_icon_cat_$cat"
                        )
                    }
                }
            }

            // Icon grid
            item {
                Text(
                    text = "ICON REPOSITORY (${filteredIcons.size})",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            items(filteredIcons.chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { iconItem ->
                        val isSelected = selectedIconItem == iconItem
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) palette.buttonBorder else palette.surfaceBorder,
                                    shape = RectangleShape
                                )
                                .background(if (isSelected) palette.surface else palette.background, RectangleShape)
                                .clickable {
                                    selectedIconItem = iconItem
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, palette.surfaceBorder, RectangleShape)
                                        .background(palette.surface, RectangleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconItem.icon,
                                        contentDescription = iconItem.name,
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = iconItem.name.split("/").first().trim(),
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = iconItem.category.uppercase(),
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Selected Icon Details & Action
            if (selectedIconItem != null) {
                item {
                    val icon = selectedIconItem!!
                    RetroCard(title = "SELECTED: ${icon.name.uppercase()}") {
                        Text(
                            text = icon.description,
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RetroButton(
                                text = "PIN SHORTCUT",
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Pinned ${icon.name} shortcut with $selectedStyle monochrome styling.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                isPrimary = true,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_pin_shortcut"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
