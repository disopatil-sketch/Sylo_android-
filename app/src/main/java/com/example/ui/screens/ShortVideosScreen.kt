package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.core.SyloLanguages
import com.example.data.model.AiTranslationResult
import com.example.data.model.PostItem
import com.example.data.model.UserAccount
import com.example.ui.components.formatCompactCount
import com.example.ui.components.resolveBundledDrawable
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloLiveGradient
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloViolet

@Composable
fun ShortVideosScreen(
    account: UserAccount?,
    posts: List<PostItem>,
    interfaceLangCode: String,
    translations: Map<String, AiTranslationResult>,
    translatingKeys: Set<String>,
    onGoLive: () -> Unit,
    onLike: (PostItem) -> Unit,
    onComment: (PostItem) -> Unit,
    onShare: (PostItem) -> Unit,
    onSave: (PostItem) -> Unit,
    onFollow: (String) -> Unit,
    onTranslate: (PostItem) -> Unit,
    onReport: (PostItem) -> Unit
) {
    val videos = remember(posts) {
        val shorts = posts.filter { it.contentType == "SHORT_VIDEO" || it.mediaKind == "VIDEO" }
        if (shorts.isNotEmpty()) shorts else posts
    }

    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0714)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No SYLO Short Videos yet", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap Create (+) to record or upload a 15s–3m reel!", color = Color(0xFFB7ACD4))
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { videos.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("short_videos_pager")
    ) { page ->
        val video = videos[page]
        var playing by remember(video.id) { mutableStateOf(true) }
        val liked = account?.likedPostIds?.contains(video.id) == true
        val saved = account?.savedPostIds?.contains(video.id) == true
        val following = account?.followingIds?.contains(video.authorId) == true
        val transKey = "post_${video.id}"
        val translation = translations[transKey]
        val isTranslating = translatingKeys.contains(transKey)

        val infiniteTransition = rememberInfiniteTransition(label = "disc")
        val discAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        val filterTint = when (video.videoFilter) {
            "Saffron Glow" -> Color(0x33FF6B2B)
            "Violet Night" -> Color(0x387C3AED)
            "Cinema 35mm" -> Color(0x28F59E0B)
            "Monsoon Blue" -> Color(0x330EA5E9)
            else -> Color.Transparent
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { playing = !playing }
        ) {
            // Video or Animated Visual Canvas
            val isLocalVideoUri = video.mediaAssetKey.startsWith("content://") || video.mediaAssetKey.startsWith("file://")
            if (isLocalVideoUri && video.mediaKind == "VIDEO") {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(video.mediaAssetKey))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                if (playing) start()
                            }
                        }
                    },
                    update = { view ->
                        if (playing && !view.isPlaying) view.start()
                        else if (!playing && view.isPlaying) view.pause()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val resId = resolveBundledDrawable(video.mediaAssetKey)
                    ?: R.drawable.img_reel_stage_1795713711518
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Filter overlay & gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filterTint)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Center Pause indicator
            if (!playing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Top Bar: SYLO Shorts + Live Video Shortcut + Video Metadata
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYLO Shorts",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val durLabel = if (video.durationSeconds >= 60) "${video.durationSeconds / 60}m" else "${video.durationSeconds.coerceAtLeast(15)}s"
                        ShortBadge("$durLabel • ${video.videoSpeed}")
                        ShortBadge(video.videoFilter)
                        ShortBadge(SyloLanguages.byCode(video.languageCode).nativeName)
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SyloLiveGradient)
                        .clickable { onGoLive() }
                        .testTag("shorts_go_live_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Videocam, contentDescription = "Go Live", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }

            // Center-top text overlay caption if present
            if (video.overlayCaption.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.52f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = video.overlayCaption,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Right Vertical Action Rail
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ReelRailAction(
                    icon = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    tint = if (liked) SyloCrimsonLive else Color.White,
                    label = formatCompactCount(video.likesCount),
                    onClick = { onLike(video) }
                )
                ReelRailAction(
                    icon = Icons.Filled.ChatBubble,
                    tint = Color.White,
                    label = formatCompactCount(video.commentsCount),
                    onClick = { onComment(video) }
                )
                ReelRailAction(
                    icon = Icons.Filled.Share,
                    tint = Color.White,
                    label = formatCompactCount(video.sharesCount),
                    onClick = { onShare(video) }
                )
                ReelRailAction(
                    icon = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    tint = if (saved) SyloSaffron else Color.White,
                    label = if (saved) "Saved" else "Save",
                    onClick = { onSave(video) }
                )
                IconButton(onClick = { onReport(video) }) {
                    Icon(Icons.Filled.Flag, contentDescription = "Report video", tint = Color.White.copy(alpha = 0.85f))
                }

                // Rotating Licensed Music Disc
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rotate(if (playing) discAngle else 0f)
                        .clip(CircleShape)
                        .background(SyloBrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = "Audio", tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }

            // Bottom Creator Info, AI Subtitles & Music Track
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.78f)
                    .padding(start = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = video.authorHandle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (video.authorId != account?.userId) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (following) Color.White.copy(alpha = 0.2f) else SyloSaffron,
                            shape = CircleShape,
                            modifier = Modifier.clickable { onFollow(video.authorId) }
                        ) {
                            Text(
                                text = if (following) "Following" else "Follow",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = translation?.translatedText ?: "${video.title} — ${video.body}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 4
                )

                // Translate Captions / Subtitles Pill
                Surface(
                    color = SyloViolet.copy(alpha = 0.72f),
                    shape = CircleShape,
                    modifier = Modifier.clickable(enabled = !isTranslating) { onTranslate(video) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(modifier = Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Translate, contentDescription = "Translate captions", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (translation != null) {
                                "Subtitles: ${SyloLanguages.byCode(interfaceLangCode).nativeName}"
                            } else {
                                "AI Subtitles -> ${SyloLanguages.byCode(interfaceLangCode).nativeName}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (playing) Icons.Filled.MusicNote else Icons.Filled.Pause,
                        contentDescription = null,
                        tint = Color(0xFFFFB085),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = video.musicTrack.ifBlank { "Original Creator Sound" } + " • ${formatCompactCount(video.viewsCount)} views",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE5DEFF)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortBadge(text: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.45f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ReelRailAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.42f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
