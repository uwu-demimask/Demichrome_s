package com.example.ui.blocker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.blocker.AppCategory
import com.example.core.blocker.BlockableApp
import com.example.core.haptics.RetroHaptics
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroChip
import com.example.ui.components.RetroHeader
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.components.RetroToggle
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme

enum class ShieldTab(val title: String) {
    APP_SHIELD("APP SHIELD"),
    WEB_SHIELD("WEB SHIELD")
}

@Composable
fun DistractionBlockerScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val palette = RetroTheme.palette

    var activeTab by remember { mutableStateOf(ShieldTab.APP_SHIELD) }

    val isBlockerEnabled by viewModel.blockerEnabled.collectAsState()
    val blockedPackages by viewModel.blockedPackages.collectAsState()
    val isStrictMode by viewModel.blockerStrictMode.collectAsState()
    val blockedAttemptsToday by viewModel.blockedAttemptsToday.collectAsState()

    val isWebShieldEnabled by viewModel.webShieldEnabled.collectAsState()
    val blockedWebDomains by viewModel.webShieldBlockedDomains.collectAsState()

    var showAddDomainDialog by remember { mutableStateOf(false) }
    var simulatedWebBlockedDomain by remember { mutableStateOf<String?>(null) }

    // Dynamically refresh permissions when user returns from system Settings
    var hasUsagePermission by remember { mutableStateOf(viewModel.hasUsageStatsPermission()) }
    var hasOverlayPermission by remember { mutableStateOf(viewModel.hasOverlayPermission()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsagePermission = viewModel.hasUsageStatsPermission()
                hasOverlayPermission = viewModel.hasOverlayPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedCategory by remember { mutableStateOf(AppCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    val allApps = remember(blockedPackages) { viewModel.getBlockableApps() }

    val filteredApps = remember(allApps, selectedCategory, searchQuery) {
        allApps.filter { app ->
            val matchesCategory = selectedCategory == AppCategory.ALL || app.category == selectedCategory
            val matchesQuery = searchQuery.isEmpty() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Screen Title Banner
            RetroHeader(
                title = "DISTRACTION SHIELD",
                subtitle = "ATTENTION & FOCUS INTERVENTION"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dual Tab Switcher: [ APP SHIELD ] vs [ WEB SHIELD ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
                    .background(palette.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShieldTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                1.dp,
                                if (isSelected) palette.textPrimary else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSelected) palette.background else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                RetroHaptics.performClick(context)
                                activeTab = tab
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) palette.textPrimary else palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        if (activeTab == ShieldTab.APP_SHIELD) {
            // APP SHIELD CONTENT

            // Usage Access Permission Card (Required)
            if (!hasUsagePermission) {
                item {
                    RetroCard(
                        title = "PERMISSION REQUIRED (STEP 1)",
                        badge = "ACTION"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = palette.textAccent,
                                modifier = Modifier.size(26.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Usage Access Required",
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Android requires Usage Access permission to detect when distracting apps are opened during focus timers.",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        RetroOutlineButton(
                            text = "GRANT USAGE ACCESS IN SETTINGS",
                            onClick = { viewModel.openUsageAccessSettings() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Overlay Permission Card (Recommended for popups)
            if (!hasOverlayPermission) {
                item {
                    RetroCard(
                        title = "POPUP OVERLAY SHIELD (RECOMMENDED)",
                        badge = "ENHANCE"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Display Over Other Apps",
                                    color = palette.textPrimary,
                                    fontFamily = palette.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Enables instant full-screen distraction intervention over blocked apps.",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        RetroOutlineButton(
                            text = "ENABLE OVERLAY PERMISSION",
                            onClick = { viewModel.openOverlaySettings() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Master Controls Card
            item {
                RetroCard(
                    title = "APP SHIELD CONFIGURATION",
                    badge = "${blockedPackages.size} BLOCKED"
                ) {
                    RetroToggle(
                        label = "App Distraction Shield",
                        checked = isBlockerEnabled,
                        onCheckedChange = { viewModel.toggleBlockerEnabled() },
                        description = "Intercepts distracting apps during active focus timers."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RetroToggle(
                        label = "Strict Zero-Tolerance Mode",
                        checked = isStrictMode,
                        onCheckedChange = { viewModel.setBlockerStrictMode(it) },
                        description = "Requires 10-second deep breath affirmation to bypass."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShieldStatBadge(
                            label = "INTERCEPTIONS",
                            value = "$blockedAttemptsToday",
                            palette = palette
                        )
                        ShieldStatBadge(
                            label = "SHIELDED APPS",
                            value = "${blockedPackages.size}",
                            palette = palette
                        )
                        ShieldStatBadge(
                            label = "STRICT LOCK",
                            value = if (isStrictMode) "ON" else "OFF",
                            palette = palette
                        )
                    }
                }
            }

            // Quick Shield Profiles & Category Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QUICK SHIELD PROFILES",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RetroChip(
                            text = "DEEP SPRINT",
                            isSelected = false,
                            onClick = {
                                viewModel.applyShieldProfile("DEEP_WORK")
                                RetroHaptics.performClick(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RetroChip(
                            text = "STUDY FLOW",
                            isSelected = false,
                            onClick = {
                                viewModel.applyShieldProfile("STUDY")
                                RetroHaptics.performClick(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RetroChip(
                            text = "SOCIAL DETOX",
                            isSelected = false,
                            onClick = {
                                viewModel.applyShieldProfile("DETOX")
                                RetroHaptics.performClick(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // App Shield Interception Simulator
                    RetroCard(
                        title = "APP SHIELD SANDBOX",
                        badge = "SIMULATOR"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Test full-screen app intervention behavior and haptics:",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RetroOutlineButton(
                                    text = "TEST INSTAGRAM",
                                    onClick = {
                                        viewModel.simulateDistractionInterception("com.instagram.android", "Instagram")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                RetroOutlineButton(
                                    text = "TEST YOUTUBE",
                                    onClick = {
                                        viewModel.simulateDistractionInterception("com.google.android.youtube", "YouTube")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "APP CATEGORIES",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(AppCategory.values()) { category ->
                            val isSelected = selectedCategory == category
                            RetroChip(
                                text = category.displayName,
                                isSelected = isSelected,
                                onClick = {
                                    selectedCategory = category
                                    RetroHaptics.performClick(context)
                                }
                            )
                        }
                    }

                    // Category-specific quick toggle action
                    if (selectedCategory != AppCategory.ALL) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RetroOutlineButton(
                                text = "+ SHIELD ${selectedCategory.displayName.uppercase()}",
                                onClick = {
                                    viewModel.blockCategory(selectedCategory)
                                    RetroHaptics.performClick(context)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            RetroOutlineButton(
                                text = "- ALLOW ${selectedCategory.displayName.uppercase()}",
                                onClick = {
                                    viewModel.unblockCategory(selectedCategory)
                                    RetroHaptics.performClick(context)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search apps...",
                                color = palette.textSecondary.copy(alpha = 0.5f),
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.buttonActiveBackground,
                            unfocusedBorderColor = palette.surfaceBorder,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                            cursorColor = palette.textAccent
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    // Batch buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RetroOutlineButton(
                            text = "SHIELD ALL",
                            onClick = {
                                viewModel.blockAllApps()
                                RetroHaptics.performClick(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RetroOutlineButton(
                            text = "ALLOW ALL",
                            onClick = {
                                viewModel.unblockAllApps()
                                RetroHaptics.performClick(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        RetroOutlineButton(
                            text = "DEFAULTS",
                            onClick = {
                                viewModel.resetShieldToDefaults()
                                RetroHaptics.performClick(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // App Items List
            items(filteredApps, key = { it.packageName }) { app ->
                val isBlocked = blockedPackages.contains(app.packageName)
                AppBlockItem(
                    app = app,
                    isBlocked = isBlocked,
                    onToggle = {
                        viewModel.toggleBlockedPackage(app.packageName)
                        RetroHaptics.performClick(context)
                    },
                    palette = palette
                )
            }
        } else {
            // WEB SHIELD TAB CONTENT
            item {
                RetroCard(
                    title = "WEB SHIELD CONFIGURATION",
                    badge = "${blockedWebDomains.size} DOMAINS"
                ) {
                    RetroToggle(
                        label = "Web Distraction Shield",
                        checked = isWebShieldEnabled,
                        onCheckedChange = { viewModel.toggleWebShieldEnabled() },
                        description = "Blocks access to distracting websites and web portals during focus sessions."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "+ ADD WEBSITE",
                            onClick = { showAddDomainDialog = true },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_add_web_domain"
                        )
                        RetroOutlineButton(
                            text = "RESTORE DEFAULTS",
                            onClick = { viewModel.resetWebShieldDefaults() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "PROTECTED WEB DOMAINS (${blockedWebDomains.size})",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Web Domains List
            items(blockedWebDomains.toList().sorted()) { domain ->
                WebDomainBlockItem(
                    domain = domain,
                    isBlocked = true,
                    onToggle = { viewModel.toggleBlockedWebDomain(domain) },
                    onRemove = { viewModel.removeBlockedWebDomain(domain) },
                    onTestIntercept = { simulatedWebBlockedDomain = domain },
                    palette = palette
                )
            }

            // Web Shield Interception Simulator Sandbox Card
            item {
                RetroCard(
                    title = "WEB SHIELD SANDBOX",
                    badge = "SIMULATOR"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Test web domain intervention behavior:",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RetroOutlineButton(
                                text = "TEST REDDIT.COM",
                                onClick = { simulatedWebBlockedDomain = "reddit.com" },
                                modifier = Modifier.weight(1f)
                            )
                            RetroOutlineButton(
                                text = "TEST YOUTUBE.COM",
                                onClick = { simulatedWebBlockedDomain = "youtube.com" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Add Web Domain Dialog
    if (showAddDomainDialog) {
        AddWebDomainDialog(
            onDismiss = { showAddDomainDialog = false },
            onAdd = { domain ->
                viewModel.addBlockedWebDomain(domain)
                showAddDomainDialog = false
            }
        )
    }

    // Web Intercept Simulation Dialog
    simulatedWebBlockedDomain?.let { domain ->
        WebInterceptDialog(
            domain = domain,
            onDismiss = { simulatedWebBlockedDomain = null }
        )
    }
}

@Composable
private fun WebDomainBlockItem(
    domain: String,
    isBlocked: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onTestIntercept: () -> Unit,
    palette: com.example.ui.theme.RetroPalette
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(
                1.dp,
                if (isBlocked) palette.surfaceBorder.copy(alpha = 0.9f) else palette.surfaceBorder.copy(alpha = 0.4f),
                RoundedCornerShape(6.dp)
            )
            .background(
                if (isBlocked) palette.surface else palette.surface.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = palette.textAccent,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = domain,
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Web Domain Shielded",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Test button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, palette.surfaceBorder, RoundedCornerShape(4.dp))
                        .background(palette.background)
                        .clickable {
                            RetroHaptics.performClick(context)
                            onTestIntercept()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TEST",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 9.sp
                    )
                }

                // ON / OFF Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            1.dp,
                            if (isBlocked) palette.buttonActiveBackground else palette.surfaceBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .background(
                            if (isBlocked) palette.buttonActiveBackground else palette.surface,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            RetroHaptics.performClick(context)
                            onToggle()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isBlocked) "ON" else "OFF",
                        color = if (isBlocked) palette.buttonActiveText else palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                RetroIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Remove",
                    onClick = onRemove,
                    isSelected = false
                )
            }
        }
    }
}

@Composable
private fun AppBlockItem(
    app: BlockableApp,
    isBlocked: Boolean,
    onToggle: () -> Unit,
    palette: com.example.ui.theme.RetroPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(
                1.dp,
                if (isBlocked) palette.surfaceBorder.copy(alpha = 0.9f) else palette.surfaceBorder.copy(alpha = 0.4f),
                RoundedCornerShape(6.dp)
            )
            .background(
                if (isBlocked) palette.surface else palette.surface.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Monogram
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isBlocked) palette.buttonActiveBackground.copy(alpha = 0.2f)
                            else palette.surfaceBorder.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (isBlocked) palette.buttonActiveBackground else palette.surfaceBorder,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = if (isBlocked) palette.buttonActiveBackground else palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = app.category.displayName,
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp
                        )
                        if (!app.isInstalled) {
                            Text(
                                text = "• PRESET",
                                color = palette.textSecondary.copy(alpha = 0.6f),
                                fontFamily = palette.fontFamily,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // Shield Toggle Indicator (ON / OFF)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        1.dp,
                        if (isBlocked) palette.buttonActiveBackground else palette.surfaceBorder,
                        RoundedCornerShape(4.dp)
                    )
                    .background(
                        if (isBlocked) palette.buttonActiveBackground else palette.surface,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isBlocked) "ON" else "OFF",
                    color = if (isBlocked) palette.buttonActiveText else palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ShieldStatBadge(
    label: String,
    value: String,
    palette: com.example.ui.theme.RetroPalette
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(1.dp, palette.surfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .background(palette.surface.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            color = palette.textPrimary,
            fontFamily = palette.fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            color = palette.textSecondary,
            fontFamily = palette.fontFamily,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun AddWebDomainDialog(
    onDismiss: () -> Unit,
    onAdd: (domain: String) -> Unit
) {
    val palette = RetroTheme.palette
    var domainInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, palette.textPrimary, RoundedCornerShape(8.dp))
                .background(palette.background)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SHIELD WEB DOMAIN",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = domainInput,
                    onValueChange = { domainInput = it },
                    placeholder = { Text("e.g. reddit.com, twitter.com", color = palette.textSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RetroOutlineButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    RetroButton(
                        text = "ADD DOMAIN",
                        onClick = {
                            if (domainInput.isNotBlank()) {
                                val clean = domainInput.trim().lowercase()
                                    .removePrefix("https://")
                                    .removePrefix("http://")
                                    .removePrefix("www.")
                                onAdd(clean)
                            }
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WebInterceptDialog(
    domain: String,
    onDismiss: () -> Unit
) {
    val palette = RetroTheme.palette

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, palette.accent, RoundedCornerShape(8.dp))
                .background(palette.background)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "┌──────────────────────────────┐\n│     DEMICHROME WEB SHIELD    │\n│       ACCESS RESTRICTED      │\n└──────────────────────────────┘",
                    color = palette.accent,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "DOMAIN: $domain",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "This site is shielded to preserve deep focus and flow state.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                RetroButton(
                    text = "RETURN TO FOCUS",
                    onClick = onDismiss,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )

                RetroOutlineButton(
                    text = "TEMPORARY BYPASS (60s)",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
