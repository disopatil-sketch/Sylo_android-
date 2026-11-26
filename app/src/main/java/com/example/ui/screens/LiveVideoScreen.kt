package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.core.SyloCatalog
import com.example.core.SyloLanguages
import com.example.data.model.LiveMessage
import com.example.data.model.LiveStream
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloLiveGradient
import com.example.ui.theme.SyloMarigold
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloViolet

@Composable
fun LiveVideoScreen(
    currentUserId: String,
    liveStreams: List<LiveStream>,
    activeStreamId: String?,
    liveMessages: List<LiveMessage>,
    startInSetupMode: Boolean,
    onSelectStream: (String?) -> Unit,
    onGoLive: (title: String, langCode: String, category: String) -> Unit,
    onEndLive: (String) -> Unit,
    onToggleSlowMode: (String, Boolean) -> Unit,
    onSendChat: (String, String) -> Unit,
    onSendGift: (String, String, Long, String) -> Unit,
    onSendHeart: (String) -> Unit,
    onReportStream: (LiveStream) -> Unit,
    onBack: () -> Unit
) {
    var showGoLiveSetup by remember(startInSetupMode) { mutableStateOf(startInSetupMode) }
    val activeStream = remember(liveStreams, activeStreamId) {
        liveStreams.firstOrNull { it.id == activeStreamId }
    }

    BackHandler {
        when {
            activeStream != null -> onSelectStream(null)
            showGoLiveSetup -> showGoLiveSetup = false
            else -> onBack()
        }
    }

    when {
        activeStream != null -> {
            LiveRoomStage(
                stream = activeStream,
                isHost = activeStream.hostId == currentUserId,
                messages = liveMessages,
                onSendChat = { onSendChat(activeStream.id, it) },
                onSendGift = { name, coins, emoji -> onSendGift(activeStream.id, name, coins, emoji) },
                onSendHeart = { onSendHeart(activeStream.id) },
                onToggleSlowMode = { onToggleSlowMode(activeStream.id, !activeStream.moderationSlowMode) },
                onEndLive = { onEndLive(activeStream.id) },
                onReport = { onReportStream(activeStream) },
                onLeave = { onSelectStream(null) }
            )
        }
        showGoLiveSetup -> {
            GoLiveSetupScreen(
                onStartBroadcast = { title, lang, cat ->
                    showGoLiveSetup = false
                    onGoLive(title, lang, cat)
                },
                onCancel = { showGoLiveSetup = false }
            )
        }
        else -> {
            LiveLobbyScreen(
                liveStreams = liveStreams,
                onOpenStream = { onSelectStream(it.id) },
                onOpenGoLive = { showGoLiveSetup = true },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun LiveLobbyScreen(
    liveStreams: List<LiveStream>,
    onOpenStream: (LiveStream) -> Unit,
    onOpenGoLive: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("SYLO Live Video", style = MaterialTheme.typography.headlineSmall)
                }
                Button(
                    onClick = onOpenGoLive,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SyloCrimsonLive),
                    shape = CircleShape,
                    modifier = Modifier.testTag("start_live_video_button")
                ) {
                    Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go Live")
                }
            }
        }

        // Go Live Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SyloLiveGradient)
                    .clickable { onOpenGoLive() }
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Broadcast Live to South Asia",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Stream directly with your camera, receive real-time multilingual comments, floating reactions, and virtual creator gifts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Live Now (${liveStreams.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (liveStreams.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.LiveTv, contentDescription = null, tint = SyloCrimsonLive, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No creators are live right now", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap Go Live above to start the first broadcast!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(liveStreams, key = { it.id }) { stream ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SyloCrimsonLive.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStream(stream) }
                        .testTag("live_stream_card_${stream.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_reel_stage_1795713711518),
                                contentDescription = stream.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = SyloCrimsonLive,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stream.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                            Text(
                                "${stream.hostName} (${stream.hostHandle})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Visibility, contentDescription = null, tint = SyloCrimsonLive, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(formatCompactCount(stream.viewersCount), style = MaterialTheme.typography.labelMedium)
                                }
                                Text(
                                    SyloLanguages.byCode(stream.languageCode).nativeName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SyloSaffron
                                )
                                Text(
                                    "${stream.giftsCoinsTotal} coins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SyloMarigold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoLiveSetupScreen(
    onStartBroadcast: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("hi") }
    var selectedCategory by remember { mutableStateOf("Entertainment") }
    var useFrontCamera by remember { mutableStateOf(true) }

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasCameraPerm = grants[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPerm) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPerm) {
            CameraXLivePreview(
                useFrontCamera = useFrontCamera,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_reel_stage_1795713711518),
                contentDescription = "Live preview backdrop",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, contentDescription = "Close setup", tint = Color.White)
                }
                Surface(color = SyloCrimsonLive, shape = CircleShape) {
                    Text(
                        text = "LIVE STUDIO",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                IconButton(onClick = { useFrontCamera = !useFrontCamera }) {
                    Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch camera", tint = Color.White)
                }
            }

            // Setup Form
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!hasCameraPerm) {
                    Surface(
                        color = Color(0xFF3A1228),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Allow Camera & Mic for real live video preview",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                            }) {
                                Text("Grant", color = SyloSaffron)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(140) },
                    label = { Text("Live Video Title", color = Color.White) },
                    placeholder = { Text("e.g., Live Q&A in Hindi & Marathi!", color = Color.LightGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SyloSaffron,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_title_input")
                )

                Text("Broadcast Language", style = MaterialTheme.typography.labelLarge, color = Color.White)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SyloLanguages.all.take(10), key = { it.code }) { lang ->
                        FilterChip(
                            selected = selectedLang == lang.code,
                            onClick = { selectedLang = lang.code },
                            label = { Text(lang.nativeName) }
                        )
                    }
                }

                Text("Category", style = MaterialTheme.typography.labelLarge, color = Color.White)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Entertainment", "Music", "Comedy", "News", "Sports", "Lifestyle")) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Button(
                    onClick = { onStartBroadcast(title, selectedLang, selectedCategory) },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("confirm_go_live_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SyloLiveGradient, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Start Live Video Now", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveRoomStage(
    stream: LiveStream,
    isHost: Boolean,
    messages: List<LiveMessage>,
    onSendChat: (String) -> Unit,
    onSendGift: (String, Long, String) -> Unit,
    onSendHeart: () -> Unit,
    onToggleSlowMode: () -> Unit,
    onEndLive: () -> Unit,
    onReport: () -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    var useFrontCamera by remember { mutableStateOf(true) }
    var micMuted by remember { mutableStateOf(false) }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

    val hasCameraPerm = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isHost && hasCameraPerm) {
            CameraXLivePreview(useFrontCamera = useFrontCamera, modifier = Modifier.fillMaxSize())
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_reel_stage_1795713711518),
                contentDescription = stream.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Top Live HUD
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(color = SyloCrimsonLive, shape = CircleShape) {
                    Text(
                        text = "● LIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(stream.hostName, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text(
                        "${formatCompactCount(stream.viewersCount)} watching • ${formatCompactCount(stream.likesCount)} ❤ • ${stream.giftsCoinsTotal} coins",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE5DEFF)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isHost) {
                    IconButton(onClick = { micMuted = !micMuted }) {
                        Icon(
                            if (micMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Toggle mic",
                            tint = if (micMuted) SyloCrimsonLive else Color.White
                        )
                    }
                    IconButton(onClick = { useFrontCamera = !useFrontCamera }) {
                        Icon(Icons.Filled.Cameraswitch, contentDescription = "Flip camera", tint = Color.White)
                    }
                    IconButton(onClick = onToggleSlowMode) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = "Toggle slow mode",
                            tint = if (stream.moderationSlowMode) SyloMarigold else Color.White
                        )
                    }
                    Button(
                        onClick = { showEndDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SyloCrimsonLive),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("end_live_button")
                    ) {
                        Text("End", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    IconButton(onClick = onReport) {
                        Icon(Icons.Filled.Flag, contentDescription = "Report live stream", tint = Color.White)
                    }
                    IconButton(onClick = onLeave) {
                        Icon(Icons.Filled.Close, contentDescription = "Leave live room", tint = Color.White)
                    }
                }
            }
        }

        // Stream Title + Slow Mode Indicator
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 68.dp, start = 14.dp, end = 14.dp)
        ) {
            Surface(color = Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = "${stream.title} • ${SyloLanguages.byCode(stream.languageCode).nativeName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            if (stream.moderationSlowMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = SyloMarigold.copy(alpha = 0.85f), shape = CircleShape) {
                    Text(
                        text = "Moderation Slow-Mode On",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Bottom Live Comments Overlay + Input Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)
            ) {
                items(messages.takeLast(25), key = { it.id }) { msg ->
                    val bg = if (msg.messageType == "GIFT") {
                        SyloSaffron.copy(alpha = 0.85f)
                    } else Color.Black.copy(alpha = 0.48f)
                    Surface(color = bg, shape = RoundedCornerShape(14.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "${msg.senderName}: ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (msg.messageType == "GIFT") Color.White else Color(0xFFFFB085)
                            )
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it.take(300) },
                    placeholder = { Text("Say something live...", color = Color.LightGray) },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SyloViolet,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("live_chat_input")
                )

                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            onSendChat(chatInput)
                            chatInput = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SyloViolet)
                        .testTag("live_chat_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send live comment", tint = Color.White)
                }

                IconButton(
                    onClick = { showGiftSheet = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SyloSaffron)
                        .testTag("live_gift_button")
                ) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = "Send live gift", tint = Color.White)
                }

                IconButton(
                    onClick = onSendHeart,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SyloCrimsonLive)
                        .testTag("live_heart_button")
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Send live reaction", tint = Color.White)
                }
            }
        }
    }

    if (showGiftSheet) {
        ModalBottomSheet(onDismissRequest = { showGiftSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Send Creator Live Gift", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Support ${stream.hostName} with compliant virtual creator gifts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SyloCatalog.liveGifts.forEach { (name, coins, emoji) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onSendGift(name, coins, emoji)
                                    showGiftSheet = false
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                Text("$coins c", style = MaterialTheme.typography.labelMedium, color = SyloSaffron)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End Live Broadcast?") },
            text = { Text("Your live session will be closed and viewer count & gift coins recorded.") },
            confirmButton = {
                Button(
                    onClick = { showEndDialog = false; onEndLive() },
                    colors = ButtonDefaults.buttonColors(containerColor = SyloCrimsonLive)
                ) {
                    Text("End Live")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) { Text("Keep Streaming") }
            }
        )
    }
}

@Composable
fun CameraXLivePreview(
    useFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    DisposableEffect(useFrontCamera, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var boundProvider: ProcessCameraProvider? = null

        val listener = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                boundProvider = cameraProvider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val selector = if (useFrontCamera && cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (e: Exception) {
                Log.w("LiveCameraX", "CameraX preview unavailable on this device: ${e.message}")
            }
        }
        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            try {
                boundProvider?.unbindAll()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
