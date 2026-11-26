package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.LocalAppPreferences
import com.example.core.SyloLanguages
import com.example.core.UserPrefsStore
import com.example.data.model.AiModerationResult
import com.example.data.model.AiTranslationResult
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.LiveMessage
import com.example.data.model.LiveStream
import com.example.data.model.ModerationReport
import com.example.data.model.PayoutRequest
import com.example.data.model.PostComment
import com.example.data.model.PostItem
import com.example.data.model.PublicProfile
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserNotification
import com.example.data.repository.SyloAiRepository
import com.example.data.repository.SyloRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "SyloViewModel"
private const val STOP_TIMEOUT_MS = 5000L

@OptIn(ExperimentalCoroutinesApi::class)
class SyloViewModel(
    private val repository: SyloRepository,
    private val aiRepository: SyloAiRepository,
    private val prefsStore: UserPrefsStore,
    val currentUserId: String
) : ViewModel() {

    val isAdmin: Boolean = repository.isCurrentUserAdmin()

    val localPrefs: StateFlow<LocalAppPreferences> = prefsStore.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LocalAppPreferences())

    val userAccountState: StateFlow<UiState<UserAccount?>> =
        repository.observeUserAccount(currentUserId).asUiState("Failed to load profile")

    val postsState: StateFlow<UiState<List<PostItem>>> =
        repository.observePublicPosts().asUiState("Failed to load feed")

    val liveStreamsState: StateFlow<UiState<List<LiveStream>>> =
        repository.observeLiveStreams().asUiState("Failed to load live videos")

    val publicProfilesState: StateFlow<UiState<List<PublicProfile>>> =
        repository.observePublicProfiles().asUiState("Failed to load creators")

    val conversationsState: StateFlow<UiState<List<Conversation>>> =
        repository.observeConversations(currentUserId).asUiState("Failed to load messages")

    val notificationsState: StateFlow<UiState<List<UserNotification>>> =
        repository.observeNotifications(currentUserId).asUiState("Failed to load notifications")

    val payoutsState: StateFlow<UiState<List<PayoutRequest>>> =
        repository.observePayouts(currentUserId).asUiState("Failed to load payouts")

    val reportsState: StateFlow<UiState<List<ModerationReport>>> =
        repository.observeReports(currentUserId, isAdmin).asUiState("Failed to load reports")

    // Active sub-resource observation
    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostComments: StateFlow<List<PostComment>> = _selectedPostId
        .flatMapLatest { id -> if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observePostComments(id) }
        .catch { e -> handleStreamError(e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _activeLiveStreamId = MutableStateFlow<String?>(null)
    val activeLiveStreamId: StateFlow<String?> = _activeLiveStreamId.asStateFlow()

    val activeLiveMessages: StateFlow<List<LiveMessage>> = _activeLiveStreamId
        .flatMapLatest { id -> if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observeLiveMessages(id) }
        .catch { e -> handleStreamError(e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeConversationId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeChatMessages(id, currentUserId)
        }
        .catch { e -> handleStreamError(e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    // AI Translation & Moderation states
    private val _translations = MutableStateFlow<Map<String, AiTranslationResult>>(emptyMap())
    val translations: StateFlow<Map<String, AiTranslationResult>> = _translations.asStateFlow()

    private val _translatingKeys = MutableStateFlow<Set<String>>(emptySet())
    val translatingKeys: StateFlow<Set<String>> = _translatingKeys.asStateFlow()

    private val _moderationState = MutableStateFlow<UiState<AiModerationResult?>?>(null)
    val moderationState: StateFlow<UiState<AiModerationResult?>?> = _moderationState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            val result = repository.ensureUserProfileAndSeedIfEmpty()
            result.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Account sync failed"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectPostForComments(postId: String?) {
        _selectedPostId.value = postId
    }

    fun selectLiveStream(streamId: String?) {
        _activeLiveStreamId.value = streamId
    }

    fun selectConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
    }

    // --- Preferences ---

    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch { prefsStore.setDarkTheme(enabled) }

    fun setLowDataMode(enabled: Boolean) = viewModelScope.launch {
        prefsStore.setLowDataMode(enabled)
        repository.updateAccountPreferences(lowDataMode = enabled)
    }

    fun setInterfaceLanguage(code: String) = viewModelScope.launch {
        prefsStore.setInterfaceLanguage(code)
        repository.updateAccountPreferences(interfaceLanguage = code)
        val lang = SyloLanguages.byCode(code)
        _toastMessage.value = "Language switched to ${lang.nativeName} (${lang.englishName})"
    }

    fun setNotificationPref(key: String, enabled: Boolean) = viewModelScope.launch {
        prefsStore.setNotificationPref(key, enabled)
    }

    // --- Onboarding & Profile ---

    fun completeOnboarding(
        displayName: String,
        username: String,
        bio: String,
        location: String,
        interfaceLanguage: String,
        preferredLanguages: List<String>,
        interests: List<String>,
        privateAccount: Boolean
    ) = viewModelScope.launch {
        prefsStore.setInterfaceLanguage(interfaceLanguage)
        repository.completeOnboarding(
            displayName, username, bio, location,
            interfaceLanguage, preferredLanguages, interests, privateAccount
        ).onSuccess {
            _toastMessage.value = "Welcome to SYLO!"
        }.onFailure {
            _toastMessage.value = it.localizedMessage ?: "Could not save onboarding profile"
        }
    }

    fun updateProfile(displayName: String, bio: String, location: String, privateAccount: Boolean) =
        viewModelScope.launch {
            repository.updateAccountPreferences(
                displayName = displayName,
                bio = bio,
                location = location,
                privateAccount = privateAccount
            ).onSuccess {
                _toastMessage.value = "Profile updated"
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Failed to update profile"
            }
        }

    // --- AI Multilingual Translation & Moderation ---

    fun translateText(key: String, originalText: String, targetLangCode: String) {
        if (_translatingKeys.value.contains(key)) return
        val targetLang = SyloLanguages.byCode(targetLangCode)
        viewModelScope.launch {
            _translatingKeys.value = _translatingKeys.value + key
            val result = aiRepository.translateAndDetect(originalText, targetLang.englishName)
            _translatingKeys.value = _translatingKeys.value - key
            result.onSuccess { translated ->
                _translations.value = _translations.value + (key to translated)
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Translation unavailable"
            }
        }
    }

    fun clearTranslation(key: String) {
        _translations.value = _translations.value - key
    }

    fun runAiSafetyCheck(title: String, body: String) {
        viewModelScope.launch {
            _moderationState.value = UiState.Loading
            aiRepository.moderateContent(title, body)
                .onSuccess { _moderationState.value = UiState.Success(it) }
                .onFailure { _moderationState.value = UiState.Error(it.localizedMessage ?: "Safety check failed") }
        }
    }

    fun clearModerationState() {
        _moderationState.value = null
    }

    // --- Content Creation & Engagement ---

    fun publishContent(
        contentType: String,
        languageCode: String,
        category: String,
        title: String,
        body: String,
        mediaKind: String,
        mediaAssetKey: String,
        durationSeconds: Long = 0L,
        musicTrack: String = "",
        videoFilter: String = "Original",
        videoSpeed: String = "1x",
        overlayCaption: String = "",
        publisherSource: String = "",
        hashtags: List<String> = emptyList(),
        onPublished: () -> Unit = {}
    ) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data ?: UserAccount(
            userId = currentUserId,
            displayName = "Sylo Creator",
            username = "creator"
        )
        repository.createPost(
            account = account,
            contentType = contentType,
            languageCode = languageCode,
            category = category,
            title = title,
            body = body,
            mediaKind = mediaKind,
            mediaAssetKey = mediaAssetKey,
            durationSeconds = durationSeconds,
            musicTrack = musicTrack,
            videoFilter = videoFilter,
            videoSpeed = videoSpeed,
            overlayCaption = overlayCaption,
            publisherSource = publisherSource,
            hashtags = hashtags
        ).onSuccess {
            _toastMessage.value = "Published to SYLO!"
            onPublished()
        }.onFailure {
            _toastMessage.value = it.localizedMessage ?: "Publish failed"
        }
    }

    fun toggleLike(post: PostItem) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data ?: return@launch
        val liked = account.likedPostIds.contains(post.id)
        repository.toggleLikePost(post.id, liked, account.likedPostIds)
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Like failed" }
    }

    fun toggleSave(post: PostItem) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data ?: return@launch
        val saved = account.savedPostIds.contains(post.id)
        repository.toggleSavePost(post.id, saved, account.savedPostIds)
            .onSuccess { _toastMessage.value = if (saved) "Removed from Saved" else "Saved to your collection" }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Save failed" }
    }

    fun toggleFollow(targetUserId: String) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data ?: return@launch
        val following = account.followingIds.contains(targetUserId)
        repository.toggleFollowCreator(targetUserId, following, account.followingIds)
            .onSuccess { _toastMessage.value = if (following) "Unfollowed" else "Following creator" }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Action failed" }
    }

    fun sharePost(post: PostItem) = viewModelScope.launch {
        repository.incrementShareCount(post.id)
        _toastMessage.value = "Share link copied for \"${post.title.take(36)}\""
    }

    fun addComment(postId: String, text: String) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data
        repository.addComment(
            postId = postId,
            authorName = account?.displayName ?: "Sylo User",
            languageCode = localPrefs.value.interfaceLanguage,
            text = text
        ).onFailure { _toastMessage.value = it.localizedMessage ?: "Comment failed" }
    }

    // --- Live Video Actions ---

    fun goLive(title: String, languageCode: String, category: String, onStarted: (String) -> Unit) =
        viewModelScope.launch {
            val account = (userAccountState.value as? UiState.Success)?.data ?: UserAccount(
                userId = currentUserId,
                displayName = "Sylo Creator",
                username = "creator"
            )
            repository.startLiveStream(account, title, languageCode, category)
                .onSuccess { streamId ->
                    _activeLiveStreamId.value = streamId
                    _toastMessage.value = "You are LIVE!"
                    onStarted(streamId)
                }
                .onFailure { _toastMessage.value = it.localizedMessage ?: "Could not start live video" }
        }

    fun endLive(streamId: String, onEnded: () -> Unit = {}) = viewModelScope.launch {
        repository.endLiveStream(streamId)
            .onSuccess {
                _activeLiveStreamId.value = null
                _toastMessage.value = "Live video ended"
                onEnded()
            }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Failed to end live stream" }
    }

    fun toggleLiveSlowMode(streamId: String, enabled: Boolean) = viewModelScope.launch {
        repository.toggleLiveSlowMode(streamId, enabled)
            .onSuccess { _toastMessage.value = if (enabled) "Slow-mode enabled" else "Slow-mode disabled" }
    }

    fun sendLiveChat(streamId: String, text: String) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data
        repository.sendLiveMessage(
            streamId = streamId,
            senderName = account?.displayName ?: "Viewer",
            text = text,
            messageType = "CHAT",
            languageCode = localPrefs.value.interfaceLanguage
        ).onFailure { _toastMessage.value = it.localizedMessage ?: "Message failed" }
    }

    fun sendLiveGift(streamId: String, giftName: String, coins: Long, emoji: String) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data
        repository.sendLiveMessage(
            streamId = streamId,
            senderName = account?.displayName ?: "Fan",
            text = "$emoji Sent $giftName ($coins coins)",
            messageType = "GIFT",
            giftAmountCoins = coins,
            languageCode = localPrefs.value.interfaceLanguage
        ).onSuccess {
            _toastMessage.value = "Sent $giftName gift!"
        }.onFailure {
            _toastMessage.value = it.localizedMessage ?: "Gift failed"
        }
    }

    fun sendLiveHeart(streamId: String) = viewModelScope.launch {
        repository.sendLiveReaction(streamId)
    }

    // --- Messaging ---

    fun startConversation(title: String, peerName: String, groupChat: Boolean) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data
        repository.createConversation(
            title = title,
            groupChat = groupChat,
            peerName = peerName,
            creatorName = account?.displayName ?: "You"
        ).onSuccess { id ->
            _activeConversationId.value = id
            _toastMessage.value = if (groupChat) "Group chat created" else "Chat started"
        }.onFailure {
            _toastMessage.value = it.localizedMessage ?: "Could not create chat"
        }
    }

    fun sendMessage(conversation: Conversation, text: String, kind: String = "TEXT", mediaNote: String? = null) =
        viewModelScope.launch {
            val account = (userAccountState.value as? UiState.Success)?.data
            repository.sendChatMessage(
                conversation = conversation,
                senderName = account?.displayName ?: "You",
                text = text,
                messageKind = kind,
                mediaNote = mediaNote
            ).onFailure { _toastMessage.value = it.localizedMessage ?: "Send failed" }
        }

    fun reactToMessage(conversationId: String, messageId: String, emoji: String) = viewModelScope.launch {
        repository.reactToMessage(conversationId, messageId, emoji)
    }

    // --- Reports, Blocking, Notifications, Monetization & Admin ---

    fun reportContent(targetType: String, targetId: String, targetTitle: String, reason: String) =
        viewModelScope.launch {
            val aiCheck = aiRepository.moderateContent(targetTitle, reason).getOrNull()
            repository.submitReport(
                targetType = targetType,
                targetId = targetId,
                targetTitle = targetTitle,
                reason = reason,
                aiRiskSummary = aiCheck?.let { "${it.riskLevel}: ${it.summary}" }
            ).onSuccess {
                _toastMessage.value = "Report submitted for safety review"
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "Could not submit report"
            }
        }

    fun blockUser(targetUserId: String) = viewModelScope.launch {
        val account = (userAccountState.value as? UiState.Success)?.data ?: return@launch
        repository.blockUser(targetUserId, account.blockedUserIds)
            .onSuccess { _toastMessage.value = "User blocked" }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Block failed" }
    }

    fun markNotificationRead(id: String) = viewModelScope.launch {
        repository.markNotificationRead(id)
    }

    fun requestWithdrawal(amountInr: Double, upiId: String) = viewModelScope.launch {
        repository.requestPayout(amountInr, "Ad Share + Live Gifts", upiId)
            .onSuccess { _toastMessage.value = "Withdrawal request of ₹${amountInr.toInt()} submitted" }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Withdrawal failed" }
    }

    fun adminResolveReport(reportId: String, status: String, removePostId: String? = null) =
        viewModelScope.launch {
            repository.adminResolveReport(reportId, status, removePostId)
                .onSuccess { _toastMessage.value = "Report marked $status" }
                .onFailure { _toastMessage.value = it.localizedMessage ?: "Admin action failed" }
        }

    fun adminToggleFeatured(postId: String, featured: Boolean) = viewModelScope.launch {
        repository.adminToggleFeaturedPost(postId, featured)
            .onSuccess { _toastMessage.value = if (featured) "Marked as Featured" else "Removed from Featured" }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Admin action failed" }
    }

    fun adminToggleVerified(targetUserId: String, verified: Boolean) = viewModelScope.launch {
        repository.adminToggleCreatorVerified(targetUserId, verified)
            .onSuccess { _toastMessage.value = if (verified) "Creator Verified" else "Verification revoked" }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Admin action failed" }
    }

    fun deleteAccount(onComplete: () -> Unit) = viewModelScope.launch {
        repository.deleteAccountData()
            .onSuccess {
                _toastMessage.value = "Account data deleted"
                onComplete()
            }
            .onFailure { _toastMessage.value = it.localizedMessage ?: "Could not delete account" }
    }

    // --- Two-Tier Flow Unwrapping ---

    private fun <T> Flow<T>.asUiState(fallbackError: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, fallbackError, rootError)
                emit(UiState.Error(rootError.localizedMessage ?: fallbackError))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    private fun handleStreamError(error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Subcollection stream error", firestoreError ?: error)
    }
}
