package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val location: String = "Mumbai, India",
    val avatarTag: String = "violet",
    val interfaceLanguage: String = "en",
    val preferredLanguages: List<String> = listOf("en", "hi"),
    val interests: List<String> = listOf("Entertainment", "News", "Memes"),
    val likedPostIds: List<String> = emptyList(),
    val savedPostIds: List<String> = emptyList(),
    val followingIds: List<String> = emptyList(),
    val blockedUserIds: List<String> = emptyList(),
    val privateAccount: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val lowDataMode: Boolean = false,
    val verified: Boolean = false,
    val earningsInr: Double = 0.0,
    val giftCoins: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PublicProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val location: String = "",
    val avatarTag: String = "violet",
    val visibility: String = "public",
    val verified: Boolean = false,
    val celebrity: Boolean = false,
    val category: String = "Creator",
    val languages: List<String> = listOf("en"),
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PostItem(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val authorVerified: Boolean = false,
    val contentType: String = "POST", // POST, SHORT_VIDEO, MEME, NEWS, CELEBRITY
    val languageCode: String = "en",
    val category: String = "Entertainment",
    val title: String = "",
    val body: String = "",
    val mediaAssetKey: String = "",
    val mediaKind: String = "TEXT", // IMAGE, VIDEO, TEXT
    val durationSeconds: Long = 0L,
    val musicTrack: String = "",
    val videoFilter: String = "Original",
    val videoSpeed: String = "1x",
    val overlayCaption: String = "",
    val publisherSource: String = "",
    val hashtags: List<String> = emptyList(),
    val visibility: String = "public",
    val featured: Boolean = false,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val sharesCount: Long = 0L,
    val viewsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PostComment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val languageCode: String = "en",
    val text: String = "",
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LiveStream(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostHandle: String = "",
    val title: String = "",
    val languageCode: String = "hi",
    val category: String = "Entertainment",
    val status: String = "LIVE", // LIVE, ENDED
    val viewersCount: Long = 1L,
    val likesCount: Long = 0L,
    val giftsCoinsTotal: Long = 0L,
    val moderationSlowMode: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LiveMessage(
    @DocumentId val id: String = "",
    val streamId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val messageType: String = "CHAT", // CHAT, GIFT, REACTION
    val giftAmountCoins: Long = 0L,
    val languageCode: String = "en",
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val title: String = "",
    val groupChat: Boolean = false,
    val memberUids: List<String> = emptyList(),
    val memberNames: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val requestStatus: String = "ACCEPTED",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val memberUids: List<String> = emptyList(),
    val text: String = "",
    val messageKind: String = "TEXT", // TEXT, PHOTO, VOICE
    val mediaNote: String = "",
    val reaction: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ModerationReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "POST",
    val targetId: String = "",
    val targetTitle: String = "",
    val reason: String = "",
    val aiRiskSummary: String = "",
    val status: String = "OPEN", // OPEN, RESOLVED, DISMISSED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserNotification(
    @DocumentId val id: String = "",
    val category: String = "FOLLOW",
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PayoutRequest(
    @DocumentId val id: String = "",
    val amountInr: Double = 0.0,
    val source: String = "",
    val upiOrBankMasked: String = "",
    val status: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AiTranslationResult(
    val detectedLanguage: String,
    val translatedText: String,
    val targetLanguage: String
)

data class AiModerationResult(
    val safe: Boolean,
    val riskLevel: String,
    val summary: String
)
