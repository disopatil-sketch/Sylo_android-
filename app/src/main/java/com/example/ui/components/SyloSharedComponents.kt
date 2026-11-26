package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DataSaverOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.SyloLanguages
import com.example.core.SyloLocalizedLabels
import com.example.data.model.AiTranslationResult
import com.example.data.model.PostComment
import com.example.data.model.PostItem
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloMarigold
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloSkyVerified
import com.example.ui.theme.SyloViolet

fun resolveBundledDrawable(mediaAssetKey: String): Int? = when (mediaAssetKey) {
    "reel_stage" -> R.drawable.img_reel_stage_1795713711518
    "news_awards" -> R.drawable.img_news_awards_1795713712656
    "meme_chai" -> R.drawable.img_meme_chai_1795713713203
    "auth_hero" -> R.drawable.img_auth_hero_1795713712074
    else -> null
}

fun formatCompactCount(count: Long): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

@Composable
fun SyloMediaFrame(
    mediaAssetKey: String,
    mediaKind: String,
    overlayCaption: String,
    durationSeconds: Long,
    lowDataMode: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var forceLoadInLowData by remember(mediaAssetKey) { mutableStateOf(false) }

    if (lowDataMode && !forceLoadInLowData && mediaAssetKey.isNotBlank()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .height(112.dp)
                .clickable { forceLoadInLowData = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DataSaverOn,
                    contentDescription = "Low data mode",
                    tint = SyloSaffron
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Low-Data Mode Active", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Tap to load media (${if (mediaKind == "VIDEO") "${durationSeconds}s video" else "image"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val drawableId = resolveBundledDrawable(mediaAssetKey)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1A122B))
    ) {
        when {
            drawableId != null -> {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            mediaAssetKey.startsWith("content://") || mediaAssetKey.startsWith("file://") || mediaAssetKey.startsWith("http") -> {
                AsyncImage(
                    model = Uri.parse(mediaAssetKey),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(SyloBrandGradient)
                )
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.72f))
                    )
                )
        )

        if (mediaKind == "VIDEO") {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.52f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "Play video",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
            if (durationSeconds > 0L) {
                Surface(
                    color = Color.Black.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (durationSeconds >= 60) "${durationSeconds / 60}m" else "${durationSeconds}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (overlayCaption.isNotBlank()) {
            Text(
                text = overlayCaption,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyloPostCard(
    post: PostItem,
    liked: Boolean,
    saved: Boolean,
    following: Boolean,
    isOwnPost: Boolean,
    lowDataMode: Boolean,
    labels: SyloLocalizedLabels,
    targetLangCode: String,
    translation: AiTranslationResult?,
    isTranslating: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onFollow: () -> Unit,
    onTranslate: () -> Unit,
    onClearTranslation: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onOpenVideo: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val postLang = remember(post.languageCode) { SyloLanguages.byCode(post.languageCode) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SyloBrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (post.authorVerified || post.contentType == "NEWS" || post.contentType == "CELEBRITY") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = SyloSkyVerified,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = post.authorHandle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = postLang.nativeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        if (post.featured) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Featured",
                                tint = SyloMarigold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (!isOwnPost) {
                    TextButton(
                        onClick = onFollow,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = if (following) "Following" else "Follow",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (following) MaterialTheme.colorScheme.onSurfaceVariant else SyloSaffron
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Report Content / Copyright") },
                            leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                            onClick = { menuExpanded = false; onReport() }
                        )
                        if (!isOwnPost) {
                            DropdownMenuItem(
                                text = { Text("Block Creator") },
                                onClick = { menuExpanded = false; onBlock() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Type & Category Pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeColor = when (post.contentType) {
                    "NEWS" -> Color(0xFF0EA5E9)
                    "MEME" -> SyloSaffron
                    "SHORT_VIDEO" -> SyloViolet
                    "CELEBRITY" -> SyloCrimsonLive
                    else -> MaterialTheme.colorScheme.primary
                }
                Surface(color = badgeColor.copy(alpha = 0.16f), shape = CircleShape) {
                    Text(
                        text = "${post.contentType.replace('_', ' ')} • ${post.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
                if (post.publisherSource.isNotBlank()) {
                    Text(
                        text = post.publisherSource,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Body (or AI Translated view)
            if (translation != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Translated from ${translation.detectedLanguage} -> ${translation.targetLanguage}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = translation.translatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }

            // AI Translate Action Chip
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .clickable(enabled = !isTranslating) {
                            if (translation != null) onClearTranslation() else onTranslate()
                        }
                        .testTag("translate_post_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = "Translate",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (translation != null) {
                                labels.original
                            } else {
                                "${labels.translate} -> ${SyloLanguages.byCode(targetLangCode).nativeName}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Media Frame
            if (post.mediaKind != "TEXT" || post.mediaAssetKey.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                SyloMediaFrame(
                    mediaAssetKey = post.mediaAssetKey,
                    mediaKind = post.mediaKind,
                    overlayCaption = post.overlayCaption,
                    durationSeconds = post.durationSeconds,
                    lowDataMode = lowDataMode,
                    contentDescription = post.title,
                    modifier = if (onOpenVideo != null && post.mediaKind == "VIDEO") {
                        Modifier.clickable { onOpenVideo() }
                    } else Modifier
                )
            }

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.hashtags.forEach { tag ->
                        Text(
                            text = if (tag.startsWith("#")) tag else "#$tag",
                            style = MaterialTheme.typography.labelMedium,
                            color = SyloSaffron
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(4.dp))

            // Engagement Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike, modifier = Modifier.testTag("like_btn_${post.id}")) {
                        Icon(
                            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (liked) SyloCrimsonLive else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(formatCompactCount(post.likesCount), style = MaterialTheme.typography.labelMedium)

                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onComment, modifier = Modifier.testTag("comment_btn_${post.id}")) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(formatCompactCount(post.commentsCount), style = MaterialTheme.typography.labelMedium)

                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(formatCompactCount(post.sharesCount), style = MaterialTheme.typography.labelMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatCompactCount(post.viewsCount)} views",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (saved) SyloSaffron else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postTitle: String,
    comments: List<PostComment>,
    translations: Map<String, AiTranslationResult>,
    translatingKeys: Set<String>,
    targetLangCode: String,
    onTranslateComment: (PostComment) -> Unit,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = postTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (comments.isEmpty()) {
                Text(
                    text = "Be the first to comment in your language!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        val key = "comment_${comment.id}"
                        val translated = translations[key]
                        val translating = translatingKeys.contains(key)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(comment.authorName, style = MaterialTheme.typography.titleSmall)
                                    TextButton(
                                        onClick = { onTranslateComment(comment) },
                                        enabled = !translating,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = if (translating) "..." else "Translate",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                Text(
                                    text = translated?.translatedText ?: comment.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (translated != null) {
                                    Text(
                                        text = "Translated (${translated.detectedLanguage} -> ${SyloLanguages.byCode(targetLangCode).nativeName})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it.take(500) },
                    placeholder = { Text("Add a comment...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment(commentText)
                            commentText = ""
                        }
                    },
                    modifier = Modifier.testTag("send_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Post comment",
                        tint = SyloSaffron
                    )
                }
            }
        }
    }
}

@Composable
fun ReportDialog(
    targetTitle: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val reasons = listOf(
        "Spam or fake account",
        "Hate speech or harassment",
        "Misinformation / Needs fact-check",
        "Copyright infringement",
        "Age-inappropriate content"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var extraDetails by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Content") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = targetTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2
                )
                reasons.forEach { reason ->
                    Surface(
                        color = if (selectedReason == reason) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                    ) {
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = extraDetails,
                    onValueChange = { extraDetails = it.take(250) },
                    label = { Text("Additional details (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val combined = if (extraDetails.isBlank()) selectedReason else "$selectedReason — $extraDetails"
                    onSubmit(combined)
                },
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
