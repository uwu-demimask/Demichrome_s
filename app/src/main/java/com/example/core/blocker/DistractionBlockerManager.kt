package com.example.core.blocker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.MainActivity
import com.example.core.audio.RetroAudioSynthesizer
import com.example.core.haptics.RetroHaptics
import com.example.core.timer.FocusTimerManager
import com.example.data.UserPreferences
import com.example.service.FocusTimerService
import com.example.ui.timer.FocusState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class BlockableApp(
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val isBlocked: Boolean,
    val isInstalled: Boolean = true
)

enum class AppCategory(val displayName: String) {
    ALL("All Categories"),
    SOCIAL("Social Media"),
    VIDEO("Video & Streaming"),
    GAMES("Gaming"),
    BROWSERS("Browsers & News"),
    OTHER("Other Apps")
}

data class InterceptionEvent(
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis()
)

object DistractionBlockerManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var monitorJob: Job? = null

    private val _interceptionEvent = MutableStateFlow<InterceptionEvent?>(null)
    val interceptionEvent: StateFlow<InterceptionEvent?> = _interceptionEvent.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // Preset list of common distracting apps for instant configuration and testing
    val PRESET_APPS = listOf(
        BlockableApp("com.instagram.android", "Instagram", AppCategory.SOCIAL, true, false),
        BlockableApp("com.zhiliaoapp.musically", "TikTok", AppCategory.SOCIAL, true, false),
        BlockableApp("com.ss.android.ugc.trill", "TikTok (Global)", AppCategory.SOCIAL, true, false),
        BlockableApp("com.twitter.android", "X / Twitter", AppCategory.SOCIAL, true, false),
        BlockableApp("com.facebook.katana", "Facebook", AppCategory.SOCIAL, true, false),
        BlockableApp("com.reddit.frontpage", "Reddit", AppCategory.SOCIAL, true, false),
        BlockableApp("com.snapchat.android", "Snapchat", AppCategory.SOCIAL, true, false),
        BlockableApp("com.google.android.youtube", "YouTube", AppCategory.VIDEO, true, false),
        BlockableApp("com.netflix.mediaclient", "Netflix", AppCategory.VIDEO, true, false),
        BlockableApp("tv.twitch.android.app", "Twitch", AppCategory.VIDEO, true, false),
        BlockableApp("com.discord", "Discord", AppCategory.SOCIAL, true, false),
        BlockableApp("com.android.chrome", "Google Chrome", AppCategory.BROWSERS, true, false),
        BlockableApp("com.sec.android.app.sbrowser", "Samsung Internet", AppCategory.BROWSERS, true, false),
        BlockableApp("org.mozilla.firefox", "Firefox", AppCategory.BROWSERS, true, false),
        BlockableApp("com.pinterest", "Pinterest", AppCategory.SOCIAL, true, false),
        BlockableApp("com.spotify.music", "Spotify", AppCategory.VIDEO, true, false),
        BlockableApp("com.roblox.client", "Roblox", AppCategory.GAMES, true, false),
        BlockableApp("com.king.candycrushsaga", "Candy Crush", AppCategory.GAMES, true, false),
        BlockableApp("com.supercell.brawlstars", "Brawl Stars", AppCategory.GAMES, true, false),
        BlockableApp("com.supercell.clashroyale", "Clash Royale", AppCategory.GAMES, true, false),
        BlockableApp("com.kiloo.subwaysurf", "Subway Surfers", AppCategory.GAMES, true, false),
        BlockableApp("com.amazon.mShop.android.shopping", "Amazon", AppCategory.OTHER, true, false),
        BlockableApp("com.einnovation.temu", "Temu", AppCategory.OTHER, true, false)
    )

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                } catch (_: Exception) {}
            }
        }
    }

    fun startMonitoring(context: Context, userPreferences: UserPreferences) {
        if (monitorJob?.isActive == true) return

        _isMonitoring.value = true
        monitorJob = scope.launch {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            var lastInterceptedPackage: String? = null
            var lastInterceptTime = 0L

            while (isActive) {
                val timerState = FocusTimerManager.state.value
                val isBlockerEnabled = userPreferences.blockerEnabled.value
                val blockedPackages = userPreferences.blockedPackages.value

                if (timerState == FocusState.RUNNING && isBlockerEnabled && blockedPackages.isNotEmpty()) {
                    val foregroundPackage = getForegroundPackage(context, usageStatsManager)

                    if (foregroundPackage != null &&
                        foregroundPackage != context.packageName &&
                        blockedPackages.contains(foregroundPackage)
                    ) {
                        val now = System.currentTimeMillis()
                        // Ensure interception triggers immediately without spamming relaunch inside 2.5s window
                        if (foregroundPackage != lastInterceptedPackage || (now - lastInterceptTime) > 2000) {
                            lastInterceptedPackage = foregroundPackage
                            lastInterceptTime = now

                            val appName = getAppNameFromPackage(context, foregroundPackage)
                            _interceptionEvent.value = InterceptionEvent(foregroundPackage, appName)
                            userPreferences.incrementBlockedAttempts()
                            FocusTimerManager.setSystemStatus("> SHIELD INTERCEPTED: $appName")

                            // Feedback sound and haptics
                            RetroHaptics.performHeavyClick(context)
                            RetroAudioSynthesizer.playErrorBuzzer()

                            // 1. Show immediate floating full-screen overlay if overlay permission is granted
                            DistractionOverlayManager.showOverlay(context, foregroundPackage, appName)

                            // 2. Show heads-up alert notification
                            FocusTimerService.showInterceptionNotification(context, foregroundPackage, appName)

                            // 3. Immediately kick user to home screen to close the distracting app
                            try {
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(homeIntent)
                            } catch (_: Exception) {}

                            // 4. Bring Demichrome MainActivity to the front with interception dialog
                            try {
                                val bringToFront = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    putExtra("EXTRA_INTERCEPTED_PKG", foregroundPackage)
                                    putExtra("EXTRA_INTERCEPTED_NAME", appName)
                                }
                                context.startActivity(bringToFront)
                            } catch (_: Exception) {}
                        }
                    } else if (foregroundPackage == context.packageName) {
                        // Reset last intercepted package when back in Demichrome
                        lastInterceptedPackage = null
                    }
                } else {
                    lastInterceptedPackage = null
                    // Hide any active floating overlay when timer is not running
                    DistractionOverlayManager.hideOverlay()
                }

                delay(500) // Fast 500ms polling for responsive blocking during focus sprints
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        _isMonitoring.value = false
    }

    fun dismissInterception() {
        DistractionOverlayManager.hideOverlay()
        _interceptionEvent.value = null
    }

    fun simulateInterception(packageName: String, appName: String, userPreferences: UserPreferences) {
        _interceptionEvent.value = InterceptionEvent(packageName, appName)
        userPreferences.incrementBlockedAttempts()
        FocusTimerManager.setSystemStatus("> TEST INTERCEPT: $appName")
    }

    private fun getForegroundPackage(context: Context, usageStatsManager: UsageStatsManager?): String? {
        if (usageStatsManager == null || !hasUsageStatsPermission(context)) return null

        try {
            val now = System.currentTimeMillis()

            // 1. Primary: Look at recent UsageEvents in last 12 seconds
            val events = usageStatsManager.queryEvents(now - 12000, now)
            var lastResumedPkg: String? = null
            var lastResumedTime = 0L
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val type = event.eventType
                // Check for ACTIVITY_RESUMED (1) or MOVE_TO_FOREGROUND
                if ((type == UsageEvents.Event.ACTIVITY_RESUMED || type == 1) && event.timeStamp >= lastResumedTime) {
                    lastResumedPkg = event.packageName
                    lastResumedTime = event.timeStamp
                }
            }

            if (lastResumedPkg != null) {
                return lastResumedPkg
            }

            // 2. Fallback: Query usage stats from the last 30 seconds
            val statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 30000,
                now
            )
            if (!statsList.isNullOrEmpty()) {
                val topStat = statsList.maxByOrNull { it.lastTimeUsed }
                if (topStat != null && (now - topStat.lastTimeUsed) < 15000) {
                    return topStat.packageName
                }
            }
        } catch (_: Exception) {}

        return null
    }

    fun getAppNameFromPackage(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            PRESET_APPS.find { it.packageName == packageName }?.appName
                ?: packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }

    fun getInstalledAndPresetApps(context: Context, blockedPackages: Set<String>): List<BlockableApp> {
        val result = mutableListOf<BlockableApp>()
        val seenPackages = mutableSetOf<String>()

        val pm = context.packageManager

        // 1. First add all launchable user apps
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val launchableActivities = pm.queryIntentActivities(launcherIntent, 0)

            for (resolveInfo in launchableActivities) {
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == context.packageName || seenPackages.contains(pkg)) continue

                val label = resolveInfo.loadLabel(pm).toString()
                val category = categorizeApp(pkg, label)
                val isBlocked = blockedPackages.contains(pkg)

                result.add(
                    BlockableApp(
                        packageName = pkg,
                        appName = label,
                        category = category,
                        isBlocked = isBlocked,
                        isInstalled = true
                    )
                )
                seenPackages.add(pkg)
            }
        } catch (_: Exception) {}

        // 2. Also check installed applications
        try {
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installed) {
                val pkg = app.packageName
                if (pkg == context.packageName || seenPackages.contains(pkg)) continue

                val isLaunchable = pm.getLaunchIntentForPackage(pkg) != null
                if (isLaunchable) {
                    val label = pm.getApplicationLabel(app).toString()
                    val category = categorizeApp(pkg, label)
                    val isBlocked = blockedPackages.contains(pkg)

                    result.add(
                        BlockableApp(
                            packageName = pkg,
                            appName = label,
                            category = category,
                            isBlocked = isBlocked,
                            isInstalled = true
                        )
                    )
                    seenPackages.add(pkg)
                }
            }
        } catch (_: Exception) {}

        // 3. Add all preset distracting apps not found on device so user can still preview & configure shields
        for (preset in PRESET_APPS) {
            if (!seenPackages.contains(preset.packageName)) {
                result.add(
                    preset.copy(isBlocked = blockedPackages.contains(preset.packageName))
                )
                seenPackages.add(preset.packageName)
            }
        }

        return result.sortedWith(
            compareByDescending<BlockableApp> { it.isBlocked }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun categorizeApp(packageName: String, label: String): AppCategory {
        val pkg = packageName.lowercase()
        val lbl = label.lowercase()

        return when {
            pkg.contains("instagram") || pkg.contains("tiktok") || pkg.contains("twitter") ||
            pkg.contains("facebook") || pkg.contains("reddit") || pkg.contains("snapchat") ||
            pkg.contains("discord") || pkg.contains("threads") || pkg.contains("social") ||
            pkg.contains("pinterest") || pkg.contains("bereal") || pkg.contains("telegram") ||
            lbl.contains("instagram") || lbl.contains("tiktok") || lbl.contains("twitter") ||
            lbl.contains("facebook") || lbl.contains("reddit") || lbl.contains("snapchat") -> AppCategory.SOCIAL

            pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("twitch") ||
            pkg.contains("spotify") || pkg.contains("video") || pkg.contains("media") ||
            pkg.contains("disney") || pkg.contains("prime") || pkg.contains("hulu") ||
            lbl.contains("youtube") || lbl.contains("netflix") || lbl.contains("twitch") ||
            lbl.contains("spotify") || lbl.contains("music") || lbl.contains("stream") -> AppCategory.VIDEO

            pkg.contains("game") || pkg.contains("play") || pkg.contains("candy") ||
            pkg.contains("clash") || pkg.contains("roblox") || pkg.contains("arcade") ||
            pkg.contains("craft") || pkg.contains("subway") || pkg.contains("brawl") ||
            lbl.contains("game") || lbl.contains("clash") || lbl.contains("roblox") ||
            lbl.contains("puzzle") || lbl.contains("quest") -> AppCategory.GAMES

            pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("firefox") ||
            pkg.contains("opera") || pkg.contains("news") || pkg.contains("edge") ||
            pkg.contains("sbrowser") || lbl.contains("chrome") || lbl.contains("browser") ||
            lbl.contains("firefox") || lbl.contains("news") -> AppCategory.BROWSERS

            else -> AppCategory.OTHER
        }
    }
}
