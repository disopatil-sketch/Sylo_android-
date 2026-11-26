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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.SyloCatalog
import com.example.core.SyloLanguages
import com.example.core.SyloLocalizedLabels
import com.example.data.model.AiTranslationResult
import com.example.data.model.PostItem
import com.example.data.model.PublicProfile
import com.example.data.model.UserAccount
import com.example.ui.components.SyloPostCard
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloMarigold
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloSkyVerified

@Composable
fun ExploreHubScreen(
    initialTab: String,
    account: UserAccount?,
    posts: List<PostItem>,
    profiles: List<PublicProfile>,
    lowDataMode: Boolean,
    interfaceLangCode: String,
    labels: SyloLocalizedLabels,
    translations: Map<String, AiTranslationResult>,
    translatingKeys: Set<String>,
    onLike: (PostItem) -> Unit,
    onComment: (PostItem) -> Unit,
    onShare: (PostItem) -> Unit,
    onSave: (PostItem) -> Unit,
    onFollow: (String) -> Unit,
    onTranslate: (PostItem) -> Unit,
    onClearTranslation: (String) -> Unit,
    onReport: (PostItem) -> Unit,
    onBlock: (String) -> Unit,
    onOpenShortVideos: () -> Unit
) {
    var activeTab by remember(initialTab) { mutableStateOf(initialTab.ifBlank { "DISCOVER" }) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNewsCat by remember { mutableStateOf("All") }
    var selectedMemeCat by remember { mutableStateOf("All") }
    var selectedLang by remember { mutableStateOf("ALL") }

    val trendingTags = remember(posts) {
        posts.flatMap { it.hashtags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .ifEmpty { listOf("#SyloReels", "#Bollywood", "#MarathiCinema", "#SouthCinema", "#ChaiMemes", "#Cricket") }
            .take(10)
    }

    val filteredPosts = remember(posts, activeTab, searchQuery, selectedNewsCat, selectedMemeCat, selectedLang) {
        val q = searchQuery.trim().lowercase()
        posts.filter { post ->
            val tabMatch = when (activeTab) {
                "NEWS" -> post.contentType == "NEWS"
                "MEMES" -> post.contentType == "MEME"
                "CELEBS" -> post.contentType == "CELEBRITY" || post.category.contains("Cinema", true) || post.category.contains("Bollywood", true)
                else -> true
            }
            val subCatMatch = when (activeTab) {
                "NEWS" -> selectedNewsCat == "All" || post.category.equals(selectedNewsCat, true)
                "MEMES" -> selectedMemeCat == "All" || post.category.equals(selectedMemeCat, true)
                else -> true
            }
            val langMatch = selectedLang == "ALL" || post.languageCode == selectedLang
            val queryMatch = q.isEmpty() ||
                post.title.lowercase().contains(q) ||
                post.body.lowercase().contains(q) ||
                post.authorName.lowercase().contains(q) ||
                post.category.lowercase().contains(q) ||
                post.hashtags.any { it.lowercase().contains(q) }
            tabMatch && subCatMatch && langMatch && queryMatch
        }
    }

    val matchingCreators = remember(profiles, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) profiles.take(6)
        else profiles.filter {
            it.displayName.lowercase().contains(q) || it.username.lowercase().contains(q) || it.location.lowercase().contains(q)
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
            Text("Explore South Asia", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search users, #hashtags, news, memes, celebs...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_search_input")
            )
        }

        // Hub Tabs: Discover | SYLO News | SYLO Memes | Celeb Zone
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(
                        "DISCOVER" to "Discover",
                        "NEWS" to "SYLO News",
                        "MEMES" to "SYLO Memes",
                        "CELEBS" to "Celebrity Zone"
                    )
                ) { (key, title) ->
                    FilterChip(
                        selected = activeTab == key,
                        onClick = { activeTab = key },
                        label = { Text(title) }
                    )
                }
            }
        }

        // Contextual sub-filters
        if (activeTab == "NEWS") {
            item {
                Surface(
                    color = Color(0xFF0EA5E9).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("BREAKING & AUTHORIZED NEWS DESK", style = MaterialTheme.typography.labelMedium, color = Color(0xFF38BDF8))
                        Text(
                            "Verified regional, cinema, sports & tech stories with publisher attribution and fact-check badges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SyloCatalog.newsCategories) { cat ->
                        FilterChip(
                            selected = selectedNewsCat == cat,
                            onClick = { selectedNewsCat = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        } else if (activeTab == "MEMES") {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SyloCatalog.memeCategories) { cat ->
                        FilterChip(
                            selected = selectedMemeCat == cat,
                            onClick = { selectedMemeCat = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        } else if (activeTab == "CELEBS") {
            item {
                Surface(
                    color = SyloMarigold.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, SyloMarigold.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Celebrity & Entertainment Zone", style = MaterialTheme.typography.titleMedium, color = SyloMarigold)
                        Text(
                            "Movie announcements, trailers, TV serial updates, behind-the-scenes clips & verified South Asian fan communities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Trending Hashtags Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = SyloSaffron, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trending Hashtags", style = MaterialTheme.typography.titleSmall)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trendingTags) { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.clickable { searchQuery = tag.removePrefix("#") }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = SyloSaffron,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Regional Language Filter
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedLang == "ALL",
                        onClick = { selectedLang = "ALL" },
                        label = { Text("All Regions") }
                    )
                }
                items(SyloLanguages.all.take(12), key = { it.code }) { lang ->
                    FilterChip(
                        selected = selectedLang == lang.code,
                        onClick = { selectedLang = lang.code },
                        label = { Text(lang.nativeName) }
                    )
                }
            }
        }

        // Creators / Celebrities Horizontal Rail
        if (matchingCreators.isNotEmpty() && (activeTab == "DISCOVER" || activeTab == "CELEBS")) {
            item {
                Text(
                    text = if (activeTab == "CELEBS") "Featured Celebrities & Creators" else "Creators to Follow",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(matchingCreators, key = { it.id }) { creator ->
                        val isFollowing = account?.followingIds?.contains(creator.userId) == true
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.width(168.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SyloBrandGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(creator.displayName.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(creator.displayName, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                    if (creator.verified || creator.celebrity) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = SyloSkyVerified, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    "${formatCompactCount(creator.followersCount)} followers",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (creator.userId != account?.userId) {
                                    TextButton(onClick = { onFollow(creator.userId) }) {
                                        Text(
                                            if (isFollowing) "Following" else "Follow",
                                            color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else SyloCrimsonLive
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filtered Content Results
        if (filteredPosts.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No matching stories found", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Try another keyword, switch category, or publish new content in Create (+).",
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

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
