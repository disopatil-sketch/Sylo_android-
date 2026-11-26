package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.SyloStrings
import com.example.core.UserPrefsStore
import com.example.core.configureAppCheck
import com.example.data.model.PostItem
import com.example.data.model.UiState
import com.example.data.repository.SyloAiRepository
import com.example.data.repository.SyloRepository
import com.example.ui.SyloViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.OnboardingScreen
import com.example.ui.auth.signOutSylo
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.ReportDialog
import com.example.ui.screens.CreateStudioScreen
import com.example.ui.screens.ExploreHubScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.LiveVideoScreen
import com.example.ui.screens.MessagingScreen
import com.example.ui.screens.ProfileDashboardScreen
import com.example.ui.screens.ShortVideosScreen
import com.example.ui.theme.SyloTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        // STEP 2: Enable Edge-to-Edge display
        enableEdgeToEdge()

        setContent {
            SyloRootApp()
        }
    }
}

private enum class MainTab { HOME, EXPLORE, CREATE, VIDEOS, PROFILE }
private enum class OverlayRoute { NONE, LIVE_LOBBY, LIVE_SETUP, MESSAGES }

@Composable
fun SyloRootApp() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        SyloTheme(darkTheme = true) {
            AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
        }
    } else {
        val uid = user.uid
        // Key composition and ViewModel by uid so signing out/switching accounts resets cleanly
        key(uid) {
            SyloAuthenticatedShell(
                currentUserId = uid,
                onSignedOut = { currentUser = null }
            )
        }
    }
}

@Composable
private fun SyloAuthenticatedShell(
    currentUserId: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: SyloViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SyloViewModel(
                    repository = SyloRepository(db),
                    aiRepository = SyloAiRepository(),
                    prefsStore = UserPrefsStore(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val localPrefs by viewModel.localPrefs.collectAsStateWithLifecycle()
    val userAccountState by viewModel.userAccountState.collectAsStateWithLifecycle()
    val postsState by viewModel.postsState.collectAsStateWithLifecycle()
    val liveStreamsState by viewModel.liveStreamsState.collectAsStateWithLifecycle()
    val profilesState by viewModel.publicProfilesState.collectAsStateWithLifecycle()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val payoutsState by viewModel.payoutsState.collectAsStateWithLifecycle()
    val reportsState by viewModel.reportsState.collectAsStateWithLifecycle()
    val postComments by viewModel.selectedPostComments.collectAsStateWithLifecycle()
    val activeLiveStreamId by viewModel.activeLiveStreamId.collectAsStateWithLifecycle()
    val activeLiveMessages by viewModel.activeLiveMessages.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val translations by viewModel.translations.collectAsStateWithLifecycle()
    val translatingKeys by viewModel.translatingKeys.collectAsStateWithLifecycle()
    val moderationState by viewModel.moderationState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var overlayRoute by remember { mutableStateOf(OverlayRoute.NONE) }
    var exploreInitialTab by remember { mutableStateOf("DISCOVER") }
    var profileInitialSection by remember { mutableStateOf("POSTS") }
    var commentSheetPost by remember { mutableStateOf<PostItem?>(null) }
    var reportTarget by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    BackHandler(enabled = overlayRoute == OverlayRoute.NONE && currentTab != MainTab.HOME) {
        currentTab = MainTab.HOME
    }

    val account = (userAccountState as? UiState.Success)?.data
    val posts = (postsState as? UiState.Success)?.data.orEmpty()
    val liveStreams = (liveStreamsState as? UiState.Success)?.data.orEmpty()
    val profiles = (profilesState as? UiState.Success)?.data.orEmpty()
    val conversations = (conversationsState as? UiState.Success)?.data.orEmpty()
    val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()
    val payouts = (payoutsState as? UiState.Success)?.data.orEmpty()
    val reports = (reportsState as? UiState.Success)?.data.orEmpty()
    val labels = remember(localPrefs.interfaceLanguage) { SyloStrings.forLang(localPrefs.interfaceLanguage) }

    SyloTheme(darkTheme = localPrefs.darkTheme) {
        // Show Onboarding once for brand-new profiles
        if (userAccountState is UiState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@SyloTheme
        }

        if (account != null && !account.onboardingCompleted) {
            OnboardingScreen(
                account = account,
                onComplete = { name, username, bio, loc, lang, prefLangs, interests, priv ->
                    viewModel.completeOnboarding(name, username, bio, loc, lang, prefLangs, interests, priv)
                }
            )
            return@SyloTheme
        }

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (overlayRoute == OverlayRoute.NONE) {
                    NavigationBar {
                        val navItems = listOf(
                            Triple(MainTab.HOME, labels.navHome, Icons.Filled.Home to Icons.Outlined.Home),
                            Triple(MainTab.EXPLORE, labels.navExplore, Icons.Filled.Explore to Icons.Outlined.Explore),
                            Triple(MainTab.CREATE, labels.navCreate, Icons.Filled.AddCircle to Icons.Outlined.AddCircleOutline),
                            Triple(MainTab.VIDEOS, labels.navVideos, Icons.Filled.PlayCircle to Icons.Outlined.PlayCircleOutline),
                            Triple(MainTab.PROFILE, labels.navProfile, Icons.Filled.Person to Icons.Outlined.Person)
                        )
                        navItems.forEach { (tab, label, icons) ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    currentTab = tab
                                    if (tab == MainTab.EXPLORE) exploreInitialTab = "DISCOVER"
                                    if (tab == MainTab.PROFILE) profileInitialSection = "POSTS"
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (overlayRoute) {
                    OverlayRoute.LIVE_LOBBY, OverlayRoute.LIVE_SETUP -> {
                        LiveVideoScreen(
                            currentUserId = currentUserId,
                            liveStreams = liveStreams,
                            activeStreamId = activeLiveStreamId,
                            liveMessages = activeLiveMessages,
                            startInSetupMode = overlayRoute == OverlayRoute.LIVE_SETUP,
                            onSelectStream = { viewModel.selectLiveStream(it) },
                            onGoLive = { title, lang, cat ->
                                viewModel.goLive(title, lang, cat) { _ ->
                                    overlayRoute = OverlayRoute.LIVE_LOBBY
                                }
                            },
                            onEndLive = { streamId -> viewModel.endLive(streamId) },
                            onToggleSlowMode = { id, enabled -> viewModel.toggleLiveSlowMode(id, enabled) },
                            onSendChat = { id, msg -> viewModel.sendLiveChat(id, msg) },
                            onSendGift = { id, name, coins, emoji -> viewModel.sendLiveGift(id, name, coins, emoji) },
                            onSendHeart = { id -> viewModel.sendLiveHeart(id) },
                            onReportStream = { stream ->
                                reportTarget = Triple("LIVE_STREAM", stream.id, stream.title)
                            },
                            onBack = {
                                viewModel.selectLiveStream(null)
                                overlayRoute = OverlayRoute.NONE
                            }
                        )
                    }

                    OverlayRoute.MESSAGES -> {
                        MessagingScreen(
                            currentUserId = currentUserId,
                            conversations = conversations,
                            activeConversationId = activeConversationId,
                            messages = activeChatMessages,
                            onlineStatusVisible = localPrefs.onlineStatusVisible,
                            onToggleOnlineStatus = { viewModel.setNotificationPref("online", it) },
                            onSelectConversation = { viewModel.selectConversation(it) },
                            onCreateConversation = { title, peer, isGroup ->
                                viewModel.startConversation(title, peer, isGroup)
                            },
                            onSendMessage = { conv, text, kind, note ->
                                viewModel.sendMessage(conv, text, kind, note)
                            },
                            onReactMessage = { convId, msgId, emoji ->
                                viewModel.reactToMessage(convId, msgId, emoji)
                            },
                            onBack = {
                                viewModel.selectConversation(null)
                                overlayRoute = OverlayRoute.NONE
                            }
                        )
                    }

                    OverlayRoute.NONE -> {
                        when (currentTab) {
                            MainTab.HOME -> HomeFeedScreen(
                                account = account,
                                postsState = postsState,
                                liveStreams = liveStreams,
                                unreadAlertsCount = notifications.count { !it.read },
                                lowDataMode = localPrefs.lowDataMode,
                                interfaceLangCode = localPrefs.interfaceLanguage,
                                labels = labels,
                                translations = translations,
                                translatingKeys = translatingKeys,
                                onOpenLiveHub = { overlayRoute = OverlayRoute.LIVE_LOBBY },
                                onStartLiveNow = { overlayRoute = OverlayRoute.LIVE_SETUP },
                                onOpenExploreTab = { tabKey ->
                                    exploreInitialTab = tabKey
                                    currentTab = MainTab.EXPLORE
                                },
                                onOpenMessages = { overlayRoute = OverlayRoute.MESSAGES },
                                onOpenNotifications = {
                                    profileInitialSection = "ALERTS"
                                    currentTab = MainTab.PROFILE
                                },
                                onOpenShortVideos = { currentTab = MainTab.VIDEOS },
                                onLike = { viewModel.toggleLike(it) },
                                onComment = { post ->
                                    viewModel.selectPostForComments(post.id)
                                    commentSheetPost = post
                                },
                                onShare = { viewModel.sharePost(it) },
                                onSave = { viewModel.toggleSave(it) },
                                onFollow = { viewModel.toggleFollow(it) },
                                onTranslate = { post ->
                                    viewModel.translateText("post_${post.id}", "${post.title}\n${post.body}", localPrefs.interfaceLanguage)
                                },
                                onClearTranslation = { viewModel.clearTranslation(it) },
                                onReport = { post ->
                                    reportTarget = Triple(
                                        if (post.contentType == "SHORT_VIDEO") "VIDEO" else "POST",
                                        post.id,
                                        post.title
                                    )
                                },
                                onBlock = { viewModel.blockUser(it) }
                            )

                            MainTab.EXPLORE -> ExploreHubScreen(
                                initialTab = exploreInitialTab,
                                account = account,
                                posts = posts,
                                profiles = profiles,
                                lowDataMode = localPrefs.lowDataMode,
                                interfaceLangCode = localPrefs.interfaceLanguage,
                                labels = labels,
                                translations = translations,
                                translatingKeys = translatingKeys,
                                onLike = { viewModel.toggleLike(it) },
                                onComment = { post ->
                                    viewModel.selectPostForComments(post.id)
                                    commentSheetPost = post
                                },
                                onShare = { viewModel.sharePost(it) },
                                onSave = { viewModel.toggleSave(it) },
                                onFollow = { viewModel.toggleFollow(it) },
                                onTranslate = { post ->
                                    viewModel.translateText("post_${post.id}", "${post.title}\n${post.body}", localPrefs.interfaceLanguage)
                                },
                                onClearTranslation = { viewModel.clearTranslation(it) },
                                onReport = { post ->
                                    reportTarget = Triple("POST", post.id, post.title)
                                },
                                onBlock = { viewModel.blockUser(it) },
                                onOpenShortVideos = { currentTab = MainTab.VIDEOS }
                            )

                            MainTab.CREATE -> CreateStudioScreen(
                                defaultLanguageCode = localPrefs.interfaceLanguage,
                                moderationState = moderationState,
                                onRunSafetyCheck = { t, b -> viewModel.runAiSafetyCheck(t, b) },
                                onClearModeration = { viewModel.clearModerationState() },
                                onLaunchLiveStudio = { overlayRoute = OverlayRoute.LIVE_SETUP },
                                onPublish = { type, lang, cat, title, body, mediaKind, asset, dur, music, filter, speed, overlay, source, tags ->
                                    viewModel.publishContent(
                                        contentType = type,
                                        languageCode = lang,
                                        category = cat,
                                        title = title,
                                        body = body,
                                        mediaKind = mediaKind,
                                        mediaAssetKey = asset,
                                        durationSeconds = dur,
                                        musicTrack = music,
                                        videoFilter = filter,
                                        videoSpeed = speed,
                                        overlayCaption = overlay,
                                        publisherSource = source,
                                        hashtags = tags,
                                        onPublished = {
                                            currentTab = if (type == "SHORT_VIDEO") MainTab.VIDEOS else MainTab.HOME
                                        }
                                    )
                                }
                            )

                            MainTab.VIDEOS -> ShortVideosScreen(
                                account = account,
                                posts = posts,
                                interfaceLangCode = localPrefs.interfaceLanguage,
                                translations = translations,
                                translatingKeys = translatingKeys,
                                onGoLive = { overlayRoute = OverlayRoute.LIVE_SETUP },
                                onLike = { viewModel.toggleLike(it) },
                                onComment = { post ->
                                    viewModel.selectPostForComments(post.id)
                                    commentSheetPost = post
                                },
                                onShare = { viewModel.sharePost(it) },
                                onSave = { viewModel.toggleSave(it) },
                                onFollow = { viewModel.toggleFollow(it) },
                                onTranslate = { post ->
                                    viewModel.translateText("post_${post.id}", "${post.title} — ${post.body}", localPrefs.interfaceLanguage)
                                },
                                onReport = { post ->
                                    reportTarget = Triple("VIDEO", post.id, post.title)
                                }
                            )

                            MainTab.PROFILE -> ProfileDashboardScreen(
                                account = account,
                                allPosts = posts,
                                publicProfiles = profiles,
                                notifications = notifications,
                                payouts = payouts,
                                reports = reports,
                                localPrefs = localPrefs,
                                isAdmin = viewModel.isAdmin,
                                initialSection = profileInitialSection,
                                onUpdateProfile = { name, bio, loc, priv -> viewModel.updateProfile(name, bio, loc, priv) },
                                onSetDarkTheme = { viewModel.setDarkTheme(it) },
                                onSetLowDataMode = { viewModel.setLowDataMode(it) },
                                onSetInterfaceLanguage = { viewModel.setInterfaceLanguage(it) },
                                onSetNotificationPref = { k, v -> viewModel.setNotificationPref(k, v) },
                                onMarkNotificationRead = { viewModel.markNotificationRead(it) },
                                onRequestWithdrawal = { amt, upi -> viewModel.requestWithdrawal(amt, upi) },
                                onAdminResolveReport = { id, status, removePostId ->
                                    viewModel.adminResolveReport(id, status, removePostId)
                                },
                                onAdminToggleFeatured = { id, feat -> viewModel.adminToggleFeatured(id, feat) },
                                onAdminToggleVerified = { uid, ver -> viewModel.adminToggleVerified(uid, ver) },
                                onDeleteAccount = {
                                    viewModel.deleteAccount {
                                        signOutSylo(credentialManager, scope, onSignedOut)
                                    }
                                },
                                onSignOut = {
                                    signOutSylo(credentialManager, scope, onSignedOut)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Comments Bottom Sheet
        commentSheetPost?.let { post ->
            CommentsBottomSheet(
                postTitle = post.title,
                comments = postComments,
                translations = translations,
                translatingKeys = translatingKeys,
                targetLangCode = localPrefs.interfaceLanguage,
                onTranslateComment = { c ->
                    viewModel.translateText("comment_${c.id}", c.text, localPrefs.interfaceLanguage)
                },
                onAddComment = { text -> viewModel.addComment(post.id, text) },
                onDismiss = {
                    commentSheetPost = null
                    viewModel.selectPostForComments(null)
                }
            )
        }

        // Safety & Copyright Report Dialog
        reportTarget?.let { (type, targetId, targetTitle) ->
            ReportDialog(
                targetTitle = targetTitle,
                onSubmit = { reason ->
                    viewModel.reportContent(type, targetId, targetTitle, reason)
                    reportTarget = null
                },
                onDismiss = { reportTarget = null }
            )
        }
    }
}
