package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.SyloCatalog
import com.example.core.SyloLanguages
import com.example.data.model.AiModerationResult
import com.example.data.model.UiState
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloEmerald
import com.example.ui.theme.SyloLiveGradient
import com.example.ui.theme.SyloSaffron

@Composable
fun CreateStudioScreen(
    defaultLanguageCode: String,
    moderationState: UiState<AiModerationResult?>?,
    onRunSafetyCheck: (String, String) -> Unit,
    onClearModeration: () -> Unit,
    onLaunchLiveStudio: () -> Unit,
    onPublish: (
        contentType: String,
        languageCode: String,
        category: String,
        title: String,
        body: String,
        mediaKind: String,
        mediaAssetKey: String,
        durationSeconds: Long,
        musicTrack: String,
        videoFilter: String,
        videoSpeed: String,
        overlayCaption: String,
        publisherSource: String,
        hashtags: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf("SHORT_VIDEO") } // SHORT_VIDEO, POST, MEME, NEWS
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var overlayCaption by remember { mutableStateOf("") }
    var hashtagsInput by remember { mutableStateOf("#SYLO #SouthAsia") }
    var publisherSource by remember { mutableStateOf("") }
    var languageCode by remember { mutableStateOf(defaultLanguageCode) }
    var category by remember { mutableStateOf("Entertainment") }

    // Short video controls
    var durationSeconds by remember { mutableStateOf(30L) }
    var selectedFilter by remember { mutableStateOf("Saffron Glow") }
    var selectedSpeed by remember { mutableStateOf("1x") }
    var selectedMusic by remember { mutableStateOf(SyloCatalog.licensedMusicTracks.first()) }
    var trimRange by remember { mutableStateOf(0f..30f) }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var useCameraRecordPreview by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasCameraPermission = grants[Manifest.permission.CAMERA] == true
        if (hasCameraPermission) useCameraRecordPreview = true
    }

    // Zero-permission Android Photo Picker (Google Play Policy compliant)
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            useCameraRecordPreview = false
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("SYLO Creator Studio", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Record reels, go live on video, craft regional memes, or share news in your language.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Go Live Video Feature Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SyloLiveGradient)
                    .clickable { onLaunchLiveStudio() }
                    .padding(16.dp)
                    .testTag("create_go_live_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Go Live Video", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                "Start a real-time broadcast with camera, chat & gifts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Surface(color = Color.White, shape = CircleShape) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelLarge,
                            color = SyloCrimsonLive,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Creation Mode Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(
                        "SHORT_VIDEO" to "Short Video",
                        "POST" to "Post / Photo",
                        "MEME" to "Meme",
                        "NEWS" to "News Story"
                    )
                ) { (key, label) ->
                    FilterChip(
                        selected = mode == key,
                        onClick = {
                            mode = key
                            onClearModeration()
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Media Picker or CameraX Preview
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (mode == "SHORT_VIDEO") "Video Source (Gallery or Camera)" else "Attach Visual Media",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (useCameraRecordPreview && hasCameraPermission && mode == "SHORT_VIDEO") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            CameraXLivePreview(useFrontCamera = true, modifier = Modifier.fillMaxSize())
                            Surface(
                                color = SyloCrimsonLive,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    "REC READY • ${durationSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (mode == "SHORT_VIDEO") {
                            OutlinedButton(
                                onClick = {
                                    videoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("pick_video_button")
                            ) {
                                Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery Video")
                            }
                            OutlinedButton(
                                onClick = {
                                    if (hasCameraPermission) {
                                        useCameraRecordPreview = !useCameraRecordPreview
                                    } else {
                                        cameraPermLauncher.launch(
                                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("record_camera_button")
                            ) {
                                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (useCameraRecordPreview) "Hide Cam" else "Record Cam")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("pick_image_button")
                            ) {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Photo from Gallery")
                            }
                        }
                    }

                    if (selectedMediaUri != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SyloEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Media attached from device gallery",
                                style = MaterialTheme.typography.labelMedium,
                                color = SyloEmerald
                            )
                        }
                    }
                }
            }
        }

        // Short Video Editing Suite
        if (mode == "SHORT_VIDEO") {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Video Editor Controls", style = MaterialTheme.typography.titleSmall)

                        Text("Duration Option", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyloCatalog.videoDurations.forEach { sec ->
                                FilterChip(
                                    selected = durationSeconds == sec,
                                    onClick = {
                                        durationSeconds = sec
                                        trimRange = 0f..sec.toFloat()
                                    },
                                    label = { Text(if (sec >= 60) "${sec / 60} min" else "${sec}s") }
                                )
                            }
                        }

                        Text(
                            "Trim Range: ${trimRange.start.toInt()}s – ${trimRange.endInclusive.toInt()}s",
                            style = MaterialTheme.typography.labelMedium
                        )
                        RangeSlider(
                            value = trimRange,
                            onValueChange = { trimRange = it },
                            valueRange = 0f..durationSeconds.toFloat()
                        )

                        Text("Speed", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyloCatalog.videoSpeeds.forEach { sp ->
                                FilterChip(
                                    selected = selectedSpeed == sp,
                                    onClick = { selectedSpeed = sp },
                                    label = { Text(sp) }
                                )
                            }
                        }

                        Text("Visual Filter", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SyloCatalog.videoFilters) { f ->
                                FilterChip(
                                    selected = selectedFilter == f,
                                    onClick = { selectedFilter = f },
                                    label = { Text(f) }
                                )
                            }
                        }

                        Text("Licensed Audio Library", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SyloCatalog.licensedMusicTracks) { track ->
                                FilterChip(
                                    selected = selectedMusic == track,
                                    onClick = { selectedMusic = track },
                                    label = { Text(track) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Core Post Fields
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(180) },
                        label = {
                            Text(
                                when (mode) {
                                    "MEME" -> "Meme Headline"
                                    "NEWS" -> "News Headline"
                                    "SHORT_VIDEO" -> "Short Video Title"
                                    else -> "Post Title"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("create_title_input")
                    )

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it.take(2000) },
                        label = { Text("Caption / Story in Your Native Language") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().testTag("create_body_input")
                    )

                    if (mode == "SHORT_VIDEO" || mode == "MEME") {
                        OutlinedTextField(
                            value = overlayCaption,
                            onValueChange = { overlayCaption = it.take(160) },
                            label = { Text("Text Overlay / Meme Punchline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (mode == "NEWS") {
                        OutlinedTextField(
                            value = publisherSource,
                            onValueChange = { publisherSource = it.take(100) },
                            label = { Text("Authorized Publisher / Source Attribution") },
                            placeholder = { Text("e.g., Original Community Report / Press Release") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = hashtagsInput,
                        onValueChange = { hashtagsInput = it.take(180) },
                        label = { Text("Hashtags (space separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Publish Language", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SyloLanguages.all, key = { it.code }) { lang ->
                            FilterChip(
                                selected = languageCode == lang.code,
                                onClick = { languageCode = lang.code },
                                label = { Text(lang.nativeName) }
                            )
                        }
                    }

                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SyloCatalog.interests) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }
        }

        // AI Safety Pre-Check Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = SyloSaffron, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Safety & Guideline Check", style = MaterialTheme.typography.titleSmall)
                        }
                        OutlinedButton(
                            onClick = { onRunSafetyCheck(title, body) },
                            enabled = title.isNotBlank() && body.isNotBlank() && moderationState !is UiState.Loading,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("ai_safety_check_button")
                        ) {
                            Text("Check")
                        }
                    }

                    when (moderationState) {
                        is UiState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing guidelines compliance...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is UiState.Success -> {
                            moderationState.data?.let { res ->
                                Text(
                                    text = "${if (res.safe) "Safe to publish" else "Review suggested"} (${res.riskLevel}): ${res.summary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (res.safe) SyloEmerald else SyloCrimsonLive
                                )
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                text = moderationState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        null -> {}
                    }
                }
            }
        }

        // Publish CTA
        item {
            Button(
                onClick = {
                    val defaultAsset = when (mode) {
                        "SHORT_VIDEO" -> "reel_stage"
                        "MEME" -> "meme_chai"
                        "NEWS" -> "news_awards"
                        else -> "auth_hero"
                    }
                    val tags = hashtagsInput.split(Regex("[\\s,]+")).filter { it.isNotBlank() }.take(10)
                    onPublish(
                        mode,
                        languageCode,
                        category,
                        title,
                        body,
                        if (mode == "SHORT_VIDEO") "VIDEO" else if (selectedMediaUri != null || mode != "POST") "IMAGE" else "TEXT",
                        selectedMediaUri ?: defaultAsset,
                        if (mode == "SHORT_VIDEO") durationSeconds else 0L,
                        if (mode == "SHORT_VIDEO") selectedMusic else "",
                        selectedFilter,
                        selectedSpeed,
                        overlayCaption,
                        publisherSource,
                        tags
                    )
                    title = ""
                    body = ""
                    overlayCaption = ""
                    selectedMediaUri = null
                    onClearModeration()
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("publish_content_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SyloBrandGradient, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Publish to SYLO", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
