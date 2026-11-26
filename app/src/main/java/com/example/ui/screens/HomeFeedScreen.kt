package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.SyloLanguages
import com.example.core.SyloLocalizedLabels
import com.example.data.model.AiTranslationResult
import com.example.data.model.LiveStream
import com.example.data.model.PostItem
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.ui.components.SyloPostCard
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloLiveGradient
import com.example.ui.theme.SyloMarigold
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloViolet

@Composable
fun HomeFeedScreen(
    account: UserAccount?,
    postsState: UiState<List<PostItem>>,
    liveStreams: List<LiveStream>,
    unreadAlertsCount: Int,
    lowDataMode: Boolean,
    interfaceLangCode: String,
    labels: SyloLocalizedLabels,
    translations: Map<String, AiTranslationResult>,
    translatingKeys: Set<String>,
    onOpenLiveHub: () -> Unit,
    onStartLiveNow: () -> Unit,
    onOpenExploreTab: (String) -> Unit,
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenShortVideos: () -> Unit,
    onLike: (PostItem) -> Unit,
    onComment: (PostItem) -> Unit,
    onShare: (PostItem) -> Unit,
    onSave: (PostItem) -> Unit,
    onFollow: (String) -> Unit,
    onTranslate: (PostItem) -> Unit,
    onClearTranslation: (String) -> Unit,
    onReport: (PostItem) -> Unit,
    onBlock: (String) -> Unit
) {
    var selectedFeedMode by remember { mutableStateOf("FOR_YOU") }
    var selectedLanguageFilter by remember { mutableStateOf("ALL") }

    val allPosts = (postsState as? UiState.Success)?.data.orEmpty()
    val blocked = account?.blockedUserIds.orEmpty().toSet()
    val following = account?.followingIds.orEmpty().toSet()
    val preferredLangs = account?.preferredLanguages.orEmpty().toSet()

    val filteredPosts = remember(allPosts, selectedFeedMode, selectedLanguageFilter, blocked, following, preferredLangs) {
        val visible = allPosts.filterNot { blocked.contains(it.authorId) }
        val byLang = if (selectedLanguageFilter == "ALL") visible else visible.filter { it.languageCode == selectedLanguageFilter }
        when (selectedFeedMode) {
            "FOLLOWING" -> byLang.filter { following.contains(it.authorId) || it.authorId == account?.userId }
            "TRENDING" -> byLang.sortedByDescending { it.likesCount + it.viewsCount / 10 }
            else -> byLang.sortedByDescending { post ->
                val langBoost = if (preferredLangs.contains(post.languageCode)) 5000L else 0L
                val featBoost = if (post.featured) 8000L else 0L
                langBoost + featBoost + post.likesCount
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Brand Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SyloBrandGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SYLO", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        text = labels.tagline,
                        style = MaterialTheme.typography.labelSmall,
                        color = SyloSaffron
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Prominent Live Video CTA Pill
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SyloLiveGradient)
                            .clickable { onStartLiveNow() }
                            .testTag("home_go_live_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = "Go Live Video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = labels.goLive,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(onClick = onOpenMessages, modifier = Modifier.testTag("home_messages_button")) {
                        Icon(Icons.Filled.Forum, contentDescription = "Messages")
                    }

                    Box {
                        IconButton(onClick = onOpenNotifications, modifier = Modifier.testTag("home_alerts_button")) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        if (unreadAlertsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(SyloCrimsonLive)
                            )
                        }
                    }
                }
            }
        }

        // Live Video Banner Strip (Active Rooms)
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SyloCrimsonLive.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLiveHub() }
                    .testTag("live_video_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = SyloCrimsonLive, shape = CircleShape) {
                                Text(
                                    text = "● ${labels.liveNow} VIDEO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${liveStreams.size} Active Broadcasts",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "Watch / Go Live ->",
                            style = MaterialTheme.typography.labelMedium,
                            color = SyloSaffron
                        )
                    }

                    if (liveStreams.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(liveStreams.take(5), key = { it.id }) { stream ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.clickable { onOpenLiveHub() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.LiveTv,
                                            contentDescription = null,
                                            tint = SyloCrimsonLive,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = stream.hostName,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            Text(
                                                text = "${formatCompactCount(stream.viewersCount)} watching • ${SyloLanguages.byCode(stream.languageCode).nativeName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Zones Bar (News, Memes, Celebs, Live, Messages)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    QuickZoneChip(labels.liveNow, Icons.Filled.Videocam, SyloCrimsonLive) { onOpenLiveHub() }
                }
                item {
                    QuickZoneChip(labels.news, Icons.Filled.Newspaper, Color(0xFF0EA5E9)) { onOpenExploreTab("NEWS") }
                }
                item {
                    QuickZoneChip(labels.memes, Icons.Filled.Mood, SyloSaffron) { onOpenExploreTab("MEMES") }
                }
                item {
                    QuickZoneChip(labels.celebs, Icons.Filled.AutoAwesome, SyloMarigold) { onOpenExploreTab("CELEBS") }
                }
                item {
                    QuickZoneChip(labels.messages, Icons.Filled.Forum, SyloViolet) { onOpenMessages() }
                }
            }
        }

        // Feed Mode Selector (For You, Following, Trending)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "FOR_YOU" to labels.recommended,
                    "FOLLOWING" to labels.following,
                    "TRENDING" to labels.trending
                ).forEach { (key, title) ->
                    FilterChip(
                        selected = selectedFeedMode == key,
                        onClick = { selectedFeedMode = key },
                        label = { Text(title) }
                    )
                }
            }
        }

        // Regional Language Ribbon
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedLanguageFilter == "ALL",
                        onClick = { selectedLanguageFilter = "ALL" },
                        label = { Text("All Languages") }
                    )
                }
                items(SyloLanguages.all, key = { it.code }) { lang ->
                    FilterChip(
                        selected = selectedLanguageFilter == lang.code,
                        onClick = { selectedLanguageFilter = lang.code },
                        label = { Text(lang.nativeName) }
                    )
                }
            }
        }

        // Feed States
        when (postsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = postsState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (filteredPosts.isEmpty()) {
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
                                Text("No posts match this filter yet", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Switch to All Languages or tap Create (+) to publish in your native language.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPosts, key = { it.id }) { post ->
                        val transKey = "post_${post.id}"
                        SyloPostCard(
                            post = post,
                            liked = account?.likedPostIds?.contains(post.id) == true,
                            saved = account?.savedPostIds?.contains(post.id) == true,
                            following = account?.followingIds?.contains(post.authorId) == true,
                            isOwnPost = post.authorId == account?.userId,
                            lowDataMode = lowDataMode,
                            labels = labels,
                            targetLangCode = interfaceLangCode,
                            translation = translations[transKey],
                            isTranslating = translatingKeys.contains(transKey),
                            onLike = { onLike(post) },
                            onComment = { onComment(post) },
                            onShare = { onShare(post) },
                            onSave = { onSave(post) },
                            onFollow = { onFollow(post.authorId) },
                            onTranslate = { onTranslate(post) },
                            onClearTranslation = { onClearTranslation(transKey) },
                            onReport = { onReport(post) },
                            onBlock = { onBlock(post.authorId) },
                            onOpenVideo = onOpenShortVideos
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun QuickZoneChip(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
