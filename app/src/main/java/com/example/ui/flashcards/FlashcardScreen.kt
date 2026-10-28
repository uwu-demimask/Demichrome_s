package com.example.ui.flashcards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.haptics.RetroHaptics
import com.example.data.Flashcard
import com.example.data.FlashcardDeck
import com.example.ui.components.DemichromeGlassButton
import com.example.ui.components.DemichromeGlassCard
import com.example.ui.components.DemichromeGlassDefaults
import com.example.ui.components.DemichromeGlassPill
import com.example.ui.components.DemichromeThinDivider
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroCard
import com.example.ui.components.RetroHeader
import com.example.ui.components.RetroIconButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme
import com.example.ui.timer.SmoothProgressBar

@Composable
fun FlashcardScreen(
    viewModel: FocusViewModel,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    val decks by viewModel.allDecks.collectAsState()
    val cornerRadiusDp by viewModel.userPreferences.cornerRadiusDp.collectAsState()
    val glassIntensity by viewModel.userPreferences.glassIntensity.collectAsState()

    var selectedDeckForStudy by remember { mutableStateOf<FlashcardDeck?>(null) }
    var selectedDeckForManage by remember { mutableStateOf<FlashcardDeck?>(null) }
    var showCreateDeckDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedDeckForStudy != null) {
                // Study Mode Active View
                FlashcardStudyView(
                    deck = selectedDeckForStudy!!,
                    viewModel = viewModel,
                    onExitStudy = { selectedDeckForStudy = null },
                    onStartFocusSession = {
                        viewModel.selectDuration(25)
                        viewModel.startTimer()
                        onNavigateToFocus()
                    }
                )
            } else if (selectedDeckForManage != null) {
                // Deck Card Manager View
                DeckCardsManagerView(
                    deck = selectedDeckForManage!!,
                    viewModel = viewModel,
                    onBack = { selectedDeckForManage = null },
                    onStartStudy = {
                        val deck = selectedDeckForManage!!
                        selectedDeckForManage = null
                        selectedDeckForStudy = deck
                    }
                )
            } else {
                // Main Decks Directory
                RetroHeader(
                    title = "FLASH CARDS",
                    subtitle = "MONOCHROME RETRO RECALL SYSTEM"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECKS (${decks.size})",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    DemichromeGlassButton(
                        text = "+ NEW DECK",
                        onClick = { showCreateDeckDialog = true },
                        palette = palette,
                        isPrimary = true,
                        cornerRadius = (cornerRadiusDp - 4).coerceAtLeast(8).dp,
                        glassIntensity = glassIntensity,
                        testTag = "btn_create_deck"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (decks.isEmpty()) {
                    DemichromeGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        palette = palette,
                        cornerRadius = cornerRadiusDp.dp,
                        glassIntensity = glassIntensity
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "NO DECKS FOUND",
                                color = palette.textPrimary,
                                fontFamily = palette.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Create your first flashcard deck to begin active recall study.",
                                color = palette.textSecondary,
                                fontFamily = palette.fontFamily,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            DemichromeGlassButton(
                                text = "CREATE FIRST DECK",
                                onClick = { showCreateDeckDialog = true },
                                palette = palette,
                                isPrimary = true,
                                cornerRadius = (cornerRadiusDp - 4).coerceAtLeast(8).dp,
                                glassIntensity = glassIntensity
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(decks, key = { it.id }) { deck ->
                            DeckCardItem(
                                deck = deck,
                                viewModel = viewModel,
                                onStudy = { selectedDeckForStudy = deck },
                                onManage = { selectedDeckForManage = deck },
                                onStartFocus = {
                                    viewModel.selectDuration(25)
                                    viewModel.startTimer()
                                    onNavigateToFocus()
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }

        // Create Deck Dialog
        if (showCreateDeckDialog) {
            CreateOrEditDeckDialog(
                initialDeck = null,
                onDismiss = { showCreateDeckDialog = false },
                onSave = { title, desc, cat ->
                    viewModel.createDeck(title, desc, cat)
                    showCreateDeckDialog = false
                }
            )
        }
    }
}

@Composable
private fun DeckCardItem(
    deck: FlashcardDeck,
    viewModel: FocusViewModel,
    onStudy: () -> Unit,
    onManage: () -> Unit,
    onStartFocus: () -> Unit
) {
    val palette = RetroTheme.palette
    val context = LocalContext.current

    val cards by viewModel.getCardsForDeck(deck.id).collectAsState(initial = emptyList())
    val cornerRadiusDp by viewModel.userPreferences.cornerRadiusDp.collectAsState()
    val glassIntensity by viewModel.userPreferences.glassIntensity.collectAsState()

    val totalCount = cards.size
    val knownCount = cards.count { it.isKnown }
    val progressPercent = if (totalCount > 0) ((knownCount * 100) / totalCount) else 0

    DemichromeGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deck_item_${deck.id}"),
        palette = palette,
        cornerRadius = cornerRadiusDp.dp,
        glassIntensity = glassIntensity
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "[${deck.category.uppercase()}]",
                            color = palette.textAccent,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "• $totalCount CARDS",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = deck.title,
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )

                    if (deck.description.isNotBlank()) {
                        Text(
                            text = deck.description,
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                }

                // Mastery badge with DemichromeGlassPill
                DemichromeGlassPill(
                    cornerRadius = 8.dp,
                    glassIntensity = 0.6f
                ) {
                    Text(
                        text = "$progressPercent% MASTERED",
                        color = if (progressPercent >= 80) palette.textAccent else palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Mastery progress bar
            SmoothProgressBar(
                progress = (progressPercent / 100f).coerceIn(0f, 1f),
                palette = palette,
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom action row with DemichromeGlassButton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DemichromeGlassButton(
                    text = "STUDY DECK",
                    onClick = onStudy,
                    palette = palette,
                    isPrimary = true,
                    cornerRadius = (cornerRadiusDp - 6).coerceAtLeast(8).dp,
                    glassIntensity = glassIntensity,
                    modifier = Modifier.weight(1.2f),
                    testTag = "btn_study_deck_${deck.id}"
                )

                DemichromeGlassButton(
                    text = "MANAGE",
                    onClick = onManage,
                    palette = palette,
                    isPrimary = false,
                    cornerRadius = (cornerRadiusDp - 6).coerceAtLeast(8).dp,
                    glassIntensity = glassIntensity,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_manage_deck_${deck.id}"
                )

                DemichromeGlassButton(
                    text = "25M FOCUS",
                    onClick = onStartFocus,
                    palette = palette,
                    isPrimary = false,
                    cornerRadius = (cornerRadiusDp - 6).coerceAtLeast(8).dp,
                    glassIntensity = glassIntensity,
                    modifier = Modifier.weight(1.1f),
                    testTag = "btn_focus_deck_${deck.id}"
                )
            }
        }
    }
}

/**
 * Interactive Study Mode View:
 * Displays cards with tap-to-flip, hints, and review tracking.
 */
@Composable
private fun FlashcardStudyView(
    deck: FlashcardDeck,
    viewModel: FocusViewModel,
    onExitStudy: () -> Unit,
    onStartFocusSession: () -> Unit
) {
    val palette = RetroTheme.palette
    val context = LocalContext.current
    val cards by viewModel.getCardsForDeck(deck.id).collectAsState(initial = emptyList())

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    if (cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DECK IS EMPTY",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Add cards to this deck before beginning study mode.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                RetroButton(
                    text = "RETURN TO DECKS",
                    onClick = onExitStudy,
                    isPrimary = true
                )
            }
        }
        return
    }

    if (isCompleted || currentIndex >= cards.size) {
        val total = cards.size
        val known = cards.count { it.isKnown }
        val percent = if (total > 0) ((known * 100) / total) else 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DemichromeGlassCard(
                modifier = Modifier.fillMaxWidth(),
                palette = palette,
                cornerRadius = 20.dp,
                glassIntensity = 0.85f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "RECALL RUN FINISHED",
                        color = palette.textAccent,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "$known of $total Cards Mastered ($percent%)",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    SmoothProgressBar(
                        progress = (percent / 100f).coerceIn(0f, 1f),
                        palette = palette,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    DemichromeGlassButton(
                        text = "STUDY AGAIN",
                        onClick = {
                            currentIndex = 0
                            isFlipped = false
                            showHint = false
                            isCompleted = false
                        },
                        palette = palette,
                        isPrimary = true,
                        cornerRadius = 14.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DemichromeGlassButton(
                        text = "RESET PROGRESS TO ZERO",
                        onClick = {
                            viewModel.resetDeckProgress(deck.id)
                            currentIndex = 0
                            isFlipped = false
                            showHint = false
                            isCompleted = false
                        },
                        palette = palette,
                        isPrimary = false,
                        cornerRadius = 14.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DemichromeGlassButton(
                        text = "START 25M FOCUS FOR DECK",
                        onClick = onStartFocusSession,
                        palette = palette,
                        isPrimary = false,
                        cornerRadius = 14.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DemichromeGlassButton(
                        text = "EXIT TO DECKS",
                        onClick = onExitStudy,
                        palette = palette,
                        isPrimary = false,
                        cornerRadius = 14.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        return
    }

    val currentCard = cards[currentIndex]
    val cardProgress = "${currentIndex + 1} / ${cards.size}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Study Header Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Exit Study",
                onClick = onExitStudy,
                isSelected = false
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = deck.title.uppercase(),
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "CARD $cardProgress",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp
                )
            }

            RetroIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "Reset",
                onClick = {
                    isFlipped = false
                    showHint = false
                },
                isSelected = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar for current deck run
        val sessionProgress = ((currentIndex.toFloat()) / cards.size.toFloat()).coerceIn(0f, 1f)
        SmoothProgressBar(progress = sessionProgress, palette = palette, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Study Card Frame (Tap to flip)
        val rotation by animateFloatAsState(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = tween(300),
            label = "card_flip"
        )

        DemichromeGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable {
                    RetroHaptics.performClick(context)
                    isFlipped = !isFlipped
                }
                .testTag("study_flashcard_surface"),
            palette = palette,
            cornerRadius = 22.dp,
            glassIntensity = 0.85f,
            borderColor = if (isFlipped) palette.accent else palette.surfaceBorder.copy(alpha = 0.8f),
            borderWidth = if (isFlipped) 2.dp else 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Front / Back content
                if (rotation <= 90f) {
                    // FRONT SIDE (Question / Prompt)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROMPT [FRONT]",
                                color = palette.textAccent,
                                fontFamily = palette.fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            if (currentCard.hint.isNotBlank()) {
                                Text(
                                    text = if (showHint) "HINT: ${currentCard.hint}" else "[ SHOW HINT ]",
                                    color = palette.textSecondary,
                                    fontFamily = palette.fontFamily,
                                    fontSize = 10.sp,
                                    modifier = Modifier.clickable { showHint = !showHint }
                                )
                            }
                        }

                        Text(
                            text = currentCard.front,
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Text(
                            text = "▼ TAP CARD TO FLIP ▼",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // BACK SIDE (Answer / Definition) - Rendered with graphicsLayer flip correction
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ANSWER [REVERSE]",
                            color = palette.accent,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = currentCard.back,
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Text(
                            text = if (currentCard.hint.isNotBlank()) "Note: ${currentCard.hint}" else "Active Recall Verified",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Evaluation buttons: [ NEED REVIEW ] vs [ I KNOW THIS ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DemichromeGlassButton(
                text = "NEED REVIEW",
                onClick = {
                    viewModel.markCardReviewed(currentCard.id, isKnown = false)
                    isFlipped = false
                    showHint = false
                    if (currentIndex < cards.size - 1) {
                        currentIndex++
                    } else {
                        isCompleted = true
                    }
                },
                palette = palette,
                isPrimary = false,
                cornerRadius = 14.dp,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Close,
                testTag = "btn_need_review"
            )

            DemichromeGlassButton(
                text = "I KNOW THIS",
                onClick = {
                    viewModel.markCardReviewed(currentCard.id, isKnown = true)
                    isFlipped = false
                    showHint = false
                    if (currentIndex < cards.size - 1) {
                        currentIndex++
                    } else {
                        isCompleted = true
                    }
                },
                palette = palette,
                isPrimary = true,
                cornerRadius = 14.dp,
                modifier = Modifier.weight(1.2f),
                icon = Icons.Default.Check,
                testTag = "btn_know_this"
            )
        }
    }
}

/**
 * Deck Cards Manager View:
 * Allows user to add, edit, and delete cards within a deck.
 */
@Composable
private fun DeckCardsManagerView(
    deck: FlashcardDeck,
    viewModel: FocusViewModel,
    onBack: () -> Unit,
    onStartStudy: () -> Unit
) {
    val palette = RetroTheme.palette
    val cards by viewModel.getCardsForDeck(deck.id).collectAsState(initial = emptyList())

    var showAddCardDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<Flashcard?>(null) }
    var showEditDeckDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RetroIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                isSelected = false
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = deck.title,
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${cards.size} Cards",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RetroIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit Deck",
                    onClick = { showEditDeckDialog = true },
                    isSelected = false
                )
                RetroIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete Deck",
                    onClick = {
                        viewModel.deleteDeck(deck)
                        onBack()
                    },
                    isSelected = false
                )
            }
        }

        // Action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RetroButton(
                text = "+ ADD CARD",
                onClick = { showAddCardDialog = true },
                isPrimary = true,
                modifier = Modifier.weight(1f),
                testTag = "btn_add_card"
            )

            if (cards.isNotEmpty()) {
                RetroOutlineButton(
                    text = "STUDY NOW",
                    onClick = onStartStudy,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_study_from_manager"
                )
            }
        }

        // Cards List
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
                    .border(1.dp, palette.surfaceBorder, RoundedCornerShape(8.dp))
                    .background(palette.surface)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NO CARDS IN THIS DECK",
                        color = palette.textPrimary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tap '+ ADD CARD' above to create study flashcards.",
                        color = palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    CardListItem(
                        card = card,
                        onEdit = { cardToEdit = card },
                        onDelete = { viewModel.deleteCard(card) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Add / Edit Card Dialog
    if (showAddCardDialog || cardToEdit != null) {
        CreateOrEditCardDialog(
            initialCard = cardToEdit,
            deckId = deck.id,
            onDismiss = {
                showAddCardDialog = false
                cardToEdit = null
            },
            onSave = { front, back, hint ->
                if (cardToEdit != null) {
                    viewModel.updateCard(cardToEdit!!.copy(front = front, back = back, hint = hint))
                } else {
                    viewModel.addCard(deck.id, front, back, hint)
                }
                showAddCardDialog = false
                cardToEdit = null
            }
        )
    }

    // Edit Deck Dialog
    if (showEditDeckDialog) {
        CreateOrEditDeckDialog(
            initialDeck = deck,
            onDismiss = { showEditDeckDialog = false },
            onSave = { title, desc, cat ->
                viewModel.updateDeck(deck.copy(title = title, description = desc, category = cat))
                showEditDeckDialog = false
            }
        )
    }
}

@Composable
private fun CardListItem(
    card: Flashcard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = RetroTheme.palette

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, palette.surfaceBorder, RoundedCornerShape(6.dp))
            .background(palette.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (card.isKnown) "[MASTERED]" else "[NEEDS REVIEW]",
                        color = if (card.isKnown) palette.accent else palette.textSecondary,
                        fontFamily = palette.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    if (card.timesReviewed > 0) {
                        Text(
                            text = "• Reviewed ${card.timesReviewed}x",
                            color = palette.textSecondary,
                            fontFamily = palette.fontFamily,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Q: ${card.front}",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = "A: ${card.back}",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                RetroIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit Card",
                    onClick = onEdit,
                    isSelected = false
                )
                RetroIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete Card",
                    onClick = onDelete,
                    isSelected = false
                )
            }
        }
    }
}

@Composable
private fun CreateOrEditDeckDialog(
    initialDeck: FlashcardDeck?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: String) -> Unit
) {
    val palette = RetroTheme.palette
    var title by remember { mutableStateOf(initialDeck?.title ?: "") }
    var description by remember { mutableStateOf(initialDeck?.description ?: "") }
    var category by remember { mutableStateOf(initialDeck?.category ?: "STUDY") }

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (initialDeck == null) "NEW FLASHCARD DECK" else "EDIT DECK",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Deck Title (e.g. Biology, CS Core)", color = palette.textSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (Optional)", color = palette.textSecondary, fontSize = 11.sp) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Tag (e.g. STUDY, CODE, LANG)", color = palette.textSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                        text = if (initialDeck == null) "CREATE DECK" else "SAVE",
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, description, category)
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
private fun CreateOrEditCardDialog(
    initialCard: Flashcard?,
    deckId: Long,
    onDismiss: () -> Unit,
    onSave: (front: String, back: String, hint: String) -> Unit
) {
    val palette = RetroTheme.palette
    var front by remember { mutableStateOf(initialCard?.front ?: "") }
    var back by remember { mutableStateOf(initialCard?.back ?: "") }
    var hint by remember { mutableStateOf(initialCard?.hint ?: "") }

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (initialCard == null) "ADD FLASHCARD" else "EDIT CARD",
                    color = palette.textPrimary,
                    fontFamily = palette.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Front / Question Prompt", color = palette.textSecondary, fontSize = 11.sp) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Back / Answer / Definition", color = palette.textSecondary, fontSize = 11.sp) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Recall Hint (Optional)", color = palette.textSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.buttonActiveBackground,
                        unfocusedBorderColor = palette.surfaceBorder,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                        text = if (initialCard == null) "ADD CARD" else "SAVE",
                        onClick = {
                            if (front.isNotBlank() && back.isNotBlank()) {
                                onSave(front, back, hint)
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
