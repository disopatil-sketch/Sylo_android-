package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.LiveMessage
import com.example.data.model.LiveStream
import com.example.data.model.ModerationReport
import com.example.data.model.PayoutRequest
import com.example.data.model.PostComment
import com.example.data.model.PostItem
import com.example.data.model.PublicProfile
import com.example.data.model.UserAccount
import com.example.data.model.UserNotification
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val ADMIN_EMAIL = "disopatil@gmail.com"

class SyloRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(ADMIN_EMAIL, ignoreCase = true) && user.isEmailVerified
    }

    // --- Real-time Streams (1:1 aligned with firestore.rules) ---

    fun observeUserAccount(userId: String): Flow<UserAccount?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error -> logFlowError(error, OperationType.GET, path, userId); throw error }
        )
    }

    fun observePublicPosts(): Flow<List<PostItem>> = flow {
        val uid = requireUserId()
        val path = "posts"
        emitAll(
            db.collection("posts")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PostItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    fun observePostComments(postId: String): Flow<List<PostComment>> = flow {
        val uid = requireUserId()
        val path = "posts/$postId/comments"
        emitAll(
            db.collection("posts").document(postId).collection("comments")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PostComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    fun observeLiveStreams(): Flow<List<LiveStream>> = flow {
        val uid = requireUserId()
        val path = "live_streams"
        emitAll(
            db.collection("live_streams")
                .whereEqualTo("status", "LIVE")
                .snapshots()
                .map { snap ->
                    snap.toObjects(LiveStream::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.viewersCount }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    fun observeLiveMessages(streamId: String): Flow<List<LiveMessage>> = flow {
        val uid = requireUserId()
        val path = "live_streams/$streamId/messages"
        emitAll(
            db.collection("live_streams").document(streamId).collection("messages")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snap ->
                    snap.toObjects(LiveMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    fun observePublicProfiles(): Flow<List<PublicProfile>> = flow {
        val uid = requireUserId()
        val path = "public_profiles"
        emitAll(
            db.collection("public_profiles")
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.followersCount }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, uid); throw error }
        )
    }

    fun observeConversations(userId: String): Flow<List<Conversation>> = flow {
        val path = "conversations"
        emitAll(
            db.collection("conversations")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, userId); throw error }
        )
    }

    fun observeChatMessages(conversationId: String, userId: String): Flow<List<ChatMessage>> = flow {
        val path = "conversations/$conversationId/messages"
        emitAll(
            db.collection("conversations").document(conversationId).collection("messages")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, userId); throw error }
        )
    }

    fun observeNotifications(userId: String): Flow<List<UserNotification>> = flow {
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, userId); throw error }
        )
    }

    fun observePayouts(userId: String): Flow<List<PayoutRequest>> = flow {
        val path = "users/$userId/payouts"
        emitAll(
            db.collection("users").document(userId).collection("payouts")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PayoutRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, userId); throw error }
        )
    }

    fun observeReports(userId: String, asAdmin: Boolean): Flow<List<ModerationReport>> = flow {
        val path = "reports"
        val query = if (asAdmin) {
            db.collection("reports")
        } else {
            db.collection("reports").whereEqualTo("reporterId", userId)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(ModerationReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error -> logFlowError(error, OperationType.LIST, path, userId); throw error }
        )
    }

    // --- Bootstrap Account & Rich Multilingual South Asian Sample Content ---

    suspend fun ensureUserProfileAndSeedIfEmpty(): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val firebaseUser = auth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val userRef = db.collection("users").document(uid)

        return try {
            val existingSnap = userRef.get().await()
            val displayName = (firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Sylo Creator").take(80)
            val baseHandle = displayName.lowercase().replace(Regex("[^a-z0-9]"), "").ifBlank { "creator" }
            val username = "${baseHandle.take(18)}_${uid.take(4).lowercase()}"

            if (!existingSnap.exists()) {
                val accountPayload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName,
                    "username" to username,
                    "email" to (firebaseUser.email ?: ""),
                    "bio" to "Exploring South Asia in my language on SYLO.",
                    "location" to "Mumbai, India",
                    "avatarTag" to "violet",
                    "interfaceLanguage" to "en",
                    "preferredLanguages" to listOf("en", "hi", "mr", "ta"),
                    "interests" to listOf("Entertainment", "Bollywood", "News", "Memes", "Sports"),
                    "likedPostIds" to emptyList<String>(),
                    "savedPostIds" to emptyList<String>(),
                    "followingIds" to emptyList<String>(),
                    "blockedUserIds" to emptyList<String>(),
                    "privateAccount" to false,
                    "onboardingCompleted" to false,
                    "lowDataMode" to false,
                    "verified" to false,
                    "earningsInr" to 0.0,
                    "giftCoins" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(accountPayload).await()

                val publicRef = db.collection("public_profiles").document(uid)
                publicRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to displayName,
                        "username" to username,
                        "bio" to "Exploring South Asia in my language on SYLO.",
                        "location" to "Mumbai, India",
                        "avatarTag" to "violet",
                        "visibility" to "public",
                        "verified" to false,
                        "celebrity" to false,
                        "category" to "Creator",
                        "languages" to listOf("en", "hi", "mr"),
                        "followersCount" to 124L,
                        "followingCount" to 8L,
                        "postsCount" to 1L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                // Welcome Notification
                userRef.collection("notifications").document("welcome_${uid.take(8)}").set(
                    mapOf(
                        "category" to "LIVE",
                        "title" to "Welcome to SYLO!",
                        "body" to "Tap Live Video anytime to broadcast or join multilingual creator rooms.",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                // Starter Creator Group Conversation
                val convId = "conv_welcome_${uid.take(10)}"
                db.collection("conversations").document(convId).set(
                    mapOf(
                        "title" to "SYLO Creators Lounge",
                        "groupChat" to true,
                        "memberUids" to listOf(uid),
                        "memberNames" to listOf(displayName, "SYLO Community Host"),
                        "lastMessage" to "Namaste! Share your first Hindi, Marathi, or Tamil reel here.",
                        "lastSenderId" to uid,
                        "requestStatus" to "ACCEPTED",
                        "createdBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                db.collection("conversations").document(convId).collection("messages").document("msg_welcome").set(
                    mapOf(
                        "conversationId" to convId,
                        "senderId" to uid,
                        "senderName" to displayName,
                        "memberUids" to listOf(uid),
                        "text" to "Namaste! Excited to connect with creators across India and South Asia on SYLO.",
                        "messageKind" to "TEXT",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Seed rich multilingual posts & a live video room if platform feed has no public posts yet
            val existingPosts = db.collection("posts")
                .whereEqualTo("visibility", "public")
                .limit(1)
                .get()
                .await()

            if (existingPosts.isEmpty) {
                seedInitialCommunityContent(uid, displayName, username)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }

    private suspend fun seedInitialCommunityContent(uid: String, displayName: String, username: String) {
        val batch = db.batch()
        val samplePosts = listOf(
            mapOf(
                "authorId" to uid,
                "authorName" to "Aarav Kulkarni",
                "authorHandle" to "@aarav_beats",
                "authorVerified" to false,
                "contentType" to "SHORT_VIDEO",
                "languageCode" to "hi",
                "category" to "Entertainment",
                "title" to "मुंबई मॉनसून फ्यूजन डांस रील",
                "body" to "जब क्लासिकल कथक और मॉडर्न बीट्स मिलते हैं! आप किस शहर से देख रहे हैं? कमेंट में बताएं।",
                "mediaAssetKey" to "reel_stage",
                "mediaKind" to "VIDEO",
                "durationSeconds" to 30L,
                "musicTrack" to "Mumbai Monsoon Lo-Fi (Licensed)",
                "videoFilter" to "Saffron Glow",
                "videoSpeed" to "1x",
                "overlayCaption" to "Mumbai Fusion Stage",
                "publisherSource" to "",
                "hashtags" to listOf("#SyloReels", "#Hindi", "#MumbaiDance"),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 1480L,
                "commentsCount" to 92L,
                "sharesCount" to 215L,
                "viewsCount" to 28400L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            mapOf(
                "authorId" to uid,
                "authorName" to "SYLO Cinema Desk",
                "authorHandle" to "@sylo_news",
                "authorVerified" to false,
                "contentType" to "NEWS",
                "languageCode" to "en",
                "category" to "Bollywood",
                "title" to "Pan-India Film Festival Announces 2026 Regional Cinema Showcase",
                "body" to "Filmmakers from Mumbai, Pune, Chennai, Hyderabad, and Kolkata unite for a 5-day multilingual cinema showcase celebrating Marathi, Tamil, Telugu, Bengali, and Hindi storytelling.",
                "mediaAssetKey" to "news_awards",
                "mediaKind" to "IMAGE",
                "durationSeconds" to 0L,
                "musicTrack" to "",
                "videoFilter" to "Original",
                "videoSpeed" to "1x",
                "overlayCaption" to "Verified Publisher • Fact-Checked",
                "publisherSource" to "SYLO Authorized Cinema Desk",
                "hashtags" to listOf("#Bollywood", "#MarathiCinema", "#SouthCinema"),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 890L,
                "commentsCount" to 64L,
                "sharesCount" to 310L,
                "viewsCount" to 15900L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            mapOf(
                "authorId" to uid,
                "authorName" to "Chai & Memes Pune",
                "authorHandle" to "@puneri_chai",
                "authorVerified" to false,
                "contentType" to "MEME",
                "languageCode" to "mr",
                "category" to "Chai & Monsoon",
                "title" to "पाऊस सुरू झाला की पहिला विचार!",
                "body" to "मीटिंग चालू असो किंवा ट्रॅफिक — पहिला पाऊस पडला की गरमागरम कुल्हड चहा आणि कांदा भजी हवीच!",
                "mediaAssetKey" to "meme_chai",
                "mediaKind" to "IMAGE",
                "durationSeconds" to 0L,
                "musicTrack" to "",
                "videoFilter" to "Original",
                "videoSpeed" to "1x",
                "overlayCaption" to "Monsoon + Kulhad Chai = Pure Peace",
                "publisherSource" to "",
                "hashtags" to listOf("#MarathiMemes", "#ChaiLovers", "#Monsoon"),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 2340L,
                "commentsCount" to 148L,
                "sharesCount" to 620L,
                "viewsCount" to 41200L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            mapOf(
                "authorId" to uid,
                "authorName" to "Ananya Krishnan",
                "authorHandle" to "@ananya_cinema",
                "authorVerified" to false,
                "contentType" to "CELEBRITY",
                "languageCode" to "ta",
                "category" to "South Cinema",
                "title" to "புதிய பான்-இந்தியா திரைப்படத்தின் இசை வெளியீட்டு விழா!",
                "body" to "சென்னையில் பிரம்மாண்டமாக நடைபெற்ற இசை வெளியீட்டு விழாவின் பின்னணி காட்சிகள் மற்றும் ரசிகர்களின் ஆரவாரம்.",
                "mediaAssetKey" to "news_awards",
                "mediaKind" to "IMAGE",
                "durationSeconds" to 0L,
                "musicTrack" to "Chennai Sunset Funk (Licensed)",
                "videoFilter" to "Cinema 35mm",
                "videoSpeed" to "1x",
                "overlayCaption" to "Behind The Scenes • Fan Community",
                "publisherSource" to "SYLO Entertainment Zone",
                "hashtags" to listOf("#TamilCinema", "#Kollywood", "#TrailerDrop"),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 3120L,
                "commentsCount" to 210L,
                "sharesCount" to 540L,
                "viewsCount" to 62000L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            mapOf(
                "authorId" to uid,
                "authorName" to "Rohan Sen",
                "authorHandle" to "@kolkata_frames",
                "authorVerified" to false,
                "contentType" to "SHORT_VIDEO",
                "languageCode" to "bn",
                "category" to "Music",
                "title" to "কলকাতার সন্ধ্যায় লাইভ জ্যাম সেশন",
                "body" to "বৃষ্টি ভেজা সন্ধ্যায় সেতার আর লো-ফাই বিটের অসাধারণ মেলবন্ধন। হেডফোন লাগিয়ে শুনুন!",
                "mediaAssetKey" to "auth_hero",
                "mediaKind" to "VIDEO",
                "durationSeconds" to 60L,
                "musicTrack" to "Sitar Chillwave Beat (Licensed)",
                "videoFilter" to "Violet Night",
                "videoSpeed" to "1x",
                "overlayCaption" to "Kolkata Lo-Fi Sitar",
                "publisherSource" to "",
                "hashtags" to listOf("#BengaliMusic", "#SyloShorts", "#Kolkata"),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 960L,
                "commentsCount" to 54L,
                "sharesCount" to 130L,
                "viewsCount" to 19400L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            mapOf(
                "authorId" to uid,
                "authorName" to "SYLO Tech & Cricket",
                "authorHandle" to "@sylo_pulse",
                "authorVerified" to false,
                "contentType" to "POST",
                "languageCode" to "te",
                "category" to "Sports",
                "title" to "క్రికెట్ అభిమానుల కోసం లైవ్ మ్యాచ్ డిస్కషన్!",
                "body" to "ఈ రోజు మ్యాచ్‌లో ఎవరు గెలుస్తారు? మీ అంచనాను కామెంట్‌లో చెప్పండి మరియు తెలుగు లైవ్ రూమ్‌లో జాయిన్ అవ్వండి.",
                "mediaAssetKey" to "auth_hero",
                "mediaKind" to "IMAGE",
                "durationSeconds" to 0L,
                "musicTrack" to "",
                "videoFilter" to "Original",
                "videoSpeed" to "1x",
                "overlayCaption" to "",
                "publisherSource" to "",
                "hashtags" to listOf("#Cricket", "#Telugu", "#SyloSports"),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 745L,
                "commentsCount" to 88L,
                "sharesCount" to 94L,
                "viewsCount" to 12100L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        samplePosts.forEachIndexed { idx, data ->
            val ref = db.collection("posts").document("seed_post_${uid.take(6)}_$idx")
            batch.set(ref, data)
        }

        // Seed an active Live Video room so Live lobby is immediately vibrant
        val liveRef = db.collection("live_streams").document("live_featured_${uid.take(6)}")
        batch.set(
            liveRef,
            mapOf(
                "hostId" to uid,
                "hostName" to displayName,
                "hostHandle" to "@$username",
                "title" to "SYLO Multilingual Unplugged & Creator Q&A",
                "languageCode" to "hi",
                "category" to "Entertainment",
                "status" to "LIVE",
                "viewersCount" to 248L,
                "likesCount" to 620L,
                "giftsCoinsTotal" to 1450L,
                "moderationSlowMode" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        batch.commit().await()
    }

    // --- Profile & Onboarding Updates ---

    suspend fun completeOnboarding(
        displayName: String,
        username: String,
        bio: String,
        location: String,
        interfaceLanguage: String,
        preferredLanguages: List<String>,
        interests: List<String>,
        privateAccount: Boolean
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanName = displayName.trim().ifBlank { "Sylo Creator" }.take(80)
        val cleanHandle = username.trim().lowercase().replace(Regex("[^a-z0-9_.]"), "").ifBlank { "user_${uid.take(5)}" }.take(40)
        val cleanBio = bio.trim().take(300)
        val cleanLocation = location.trim().ifBlank { "India" }.take(80)

        return try {
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to cleanName,
                    "username" to cleanHandle,
                    "bio" to cleanBio,
                    "location" to cleanLocation,
                    "interfaceLanguage" to interfaceLanguage.take(10),
                    "preferredLanguages" to preferredLanguages.take(15),
                    "interests" to interests.take(12),
                    "privateAccount" to privateAccount,
                    "onboardingCompleted" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            db.collection("public_profiles").document(uid).update(
                mapOf(
                    "displayName" to cleanName,
                    "username" to cleanHandle,
                    "bio" to cleanBio,
                    "location" to cleanLocation,
                    "visibility" to if (privateAccount) "private" else "public",
                    "languages" to preferredLanguages.take(15),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun updateAccountPreferences(
        interfaceLanguage: String? = null,
        lowDataMode: Boolean? = null,
        privateAccount: Boolean? = null,
        bio: String? = null,
        location: String? = null,
        displayName: String? = null
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            interfaceLanguage?.let { updates["interfaceLanguage"] = it.take(10) }
            lowDataMode?.let { updates["lowDataMode"] = it }
            privateAccount?.let { updates["privateAccount"] = it }
            bio?.let { updates["bio"] = it.trim().take(300) }
            location?.let { updates["location"] = it.trim().take(80) }
            displayName?.takeIf { it.isNotBlank() }?.let { updates["displayName"] = it.trim().take(80) }

            db.collection("users").document(uid).update(updates).await()

            val publicUpdates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            privateAccount?.let { publicUpdates["visibility"] = if (it) "private" else "public" }
            bio?.let { publicUpdates["bio"] = it.trim().take(300) }
            location?.let { publicUpdates["location"] = it.trim().take(80) }
            displayName?.takeIf { it.isNotBlank() }?.let { publicUpdates["displayName"] = it.trim().take(80) }
            if (publicUpdates.size > 1) {
                db.collection("public_profiles").document(uid).update(publicUpdates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
            Result.failure(e)
        }
    }

    // --- Publishing Posts, Short Videos, Memes, News ---

    suspend fun createPost(
        account: UserAccount,
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
        hashtags: List<String> = emptyList()
    ): Result<String> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanTitle = title.trim().take(200)
        val cleanBody = body.trim().take(2500)
        if (cleanTitle.isEmpty() || cleanBody.isEmpty()) {
            return Result.failure(IllegalArgumentException("Title and description cannot be empty."))
        }
        val docRef = db.collection("posts").document()
        return try {
            val payload = mapOf(
                "authorId" to uid,
                "authorName" to account.displayName.ifBlank { "Sylo Creator" }.take(80),
                "authorHandle" to "@${account.username.ifBlank { "creator" }}".take(45),
                "authorVerified" to (account.verified && isCurrentUserAdmin()),
                "contentType" to contentType,
                "languageCode" to languageCode.take(10),
                "category" to category.take(60),
                "title" to cleanTitle,
                "body" to cleanBody,
                "mediaAssetKey" to mediaAssetKey.take(600),
                "mediaKind" to mediaKind,
                "durationSeconds" to durationSeconds.coerceIn(0L, 180L),
                "musicTrack" to musicTrack.take(120),
                "videoFilter" to videoFilter.take(40),
                "videoSpeed" to videoSpeed.take(16),
                "overlayCaption" to overlayCaption.take(200),
                "publisherSource" to publisherSource.take(120),
                "hashtags" to hashtags.map { it.trim() }.filter { it.isNotBlank() }.take(10),
                "visibility" to "public",
                "featured" to false,
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "sharesCount" to 0L,
                "viewsCount" to 1L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            db.collection("public_profiles").document(uid).update(
                mapOf("postsCount" to FieldValue.increment(1), "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleLikePost(postId: String, currentlyLiked: Boolean, currentLikedIds: List<String>): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            val nextLiked = if (currentlyLiked) {
                currentLikedIds.filterNot { it == postId }
            } else {
                (currentLikedIds + postId).distinct().takeLast(200)
            }
            db.collection("users").document(uid).update(
                mapOf("likedPostIds" to nextLiked, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            val postRef = db.collection("posts").document(postId)
            val delta = if (currentlyLiked) {
                val currentLikes = postRef.get().await().getLong("likesCount") ?: 0L
                if (currentLikes > 0L) -1L else 0L
            } else {
                1L
            }
            if (delta != 0L) {
                postRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(delta),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun toggleSavePost(postId: String, currentlySaved: Boolean, currentSavedIds: List<String>): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            val nextSaved = if (currentlySaved) {
                currentSavedIds.filterNot { it == postId }
            } else {
                (currentSavedIds + postId).distinct().takeLast(200)
            }
            db.collection("users").document(uid).update(
                mapOf("savedPostIds" to nextSaved, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun toggleFollowCreator(targetUserId: String, currentlyFollowing: Boolean, currentFollowingIds: List<String>): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            val nextFollowing = if (currentlyFollowing) {
                currentFollowingIds.filterNot { it == targetUserId }
            } else {
                (currentFollowingIds + targetUserId).distinct().takeLast(200)
            }
            db.collection("users").document(uid).update(
                mapOf("followingIds" to nextFollowing, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            // Update follower count if public profile exists
            val targetProfile = db.collection("public_profiles").document(targetUserId)
            val profileSnap = targetProfile.get().await()
            if (profileSnap.exists()) {
                val currentFollowers = profileSnap.getLong("followersCount") ?: 0L
                val delta = if (currentlyFollowing) {
                    if (currentFollowers > 0L) -1L else 0L
                } else {
                    1L
                }
                if (delta != 0L) {
                    targetProfile.update(
                        mapOf(
                            "followersCount" to FieldValue.increment(delta),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "public_profiles/$targetUserId")
            Result.failure(e)
        }
    }

    suspend fun incrementShareCount(postId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("posts").document(postId).update(
                mapOf("sharesCount" to FieldValue.increment(1L), "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, authorName: String, languageCode: String, text: String): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanText = text.trim().take(600)
        if (cleanText.isEmpty()) return Result.failure(IllegalArgumentException("Comment cannot be empty."))
        val commentRef = db.collection("posts").document(postId).collection("comments").document()
        return try {
            commentRef.set(
                mapOf(
                    "postId" to postId,
                    "authorId" to uid,
                    "authorName" to authorName.ifBlank { "Sylo User" }.take(80),
                    "languageCode" to languageCode.take(10),
                    "text" to cleanText,
                    "visibility" to "public",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            db.collection("posts").document(postId).update(
                mapOf("commentsCount" to FieldValue.increment(1L), "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    // --- Live Video Streaming ---

    suspend fun startLiveStream(
        account: UserAccount,
        title: String,
        languageCode: String,
        category: String
    ): Result<String> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanTitle = title.trim().ifBlank { "${account.displayName} is Live on SYLO!" }.take(160)
        val streamRef = db.collection("live_streams").document()
        return try {
            streamRef.set(
                mapOf(
                    "hostId" to uid,
                    "hostName" to account.displayName.ifBlank { "Sylo Creator" }.take(80),
                    "hostHandle" to "@${account.username.ifBlank { "creator" }}".take(45),
                    "title" to cleanTitle,
                    "languageCode" to languageCode.take(10),
                    "category" to category.take(60),
                    "status" to "LIVE",
                    "viewersCount" to 1L,
                    "likesCount" to 0L,
                    "giftsCoinsTotal" to 0L,
                    "moderationSlowMode" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Notify user's notification center
            db.collection("users").document(uid).collection("notifications").document().set(
                mapOf(
                    "category" to "LIVE",
                    "title" to "You went Live!",
                    "body" to "\"$cleanTitle\" is now streaming to your followers.",
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(streamRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, streamRef.path)
            Result.failure(e)
        }
    }

    suspend fun endLiveStream(streamId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("live_streams").document(streamId).update(
                mapOf("status" to "ENDED", "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "live_streams/$streamId")
            Result.failure(e)
        }
    }

    suspend fun toggleLiveSlowMode(streamId: String, enabled: Boolean): Result<Unit> {
        return try {
            requireUserId()
            db.collection("live_streams").document(streamId).update(
                mapOf("moderationSlowMode" to enabled, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "live_streams/$streamId")
            Result.failure(e)
        }
    }

    suspend fun sendLiveMessage(
        streamId: String,
        senderName: String,
        text: String,
        messageType: String = "CHAT",
        giftAmountCoins: Long = 0L,
        languageCode: String = "en"
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanText = text.trim().take(400)
        if (cleanText.isEmpty()) return Result.failure(IllegalArgumentException("Message cannot be empty."))
        val msgRef = db.collection("live_streams").document(streamId).collection("messages").document()
        return try {
            msgRef.set(
                mapOf(
                    "streamId" to streamId,
                    "senderId" to uid,
                    "senderName" to senderName.ifBlank { "Viewer" }.take(80),
                    "text" to cleanText,
                    "messageType" to messageType,
                    "giftAmountCoins" to giftAmountCoins.coerceIn(0L, 100000L),
                    "languageCode" to languageCode.take(10),
                    "visibility" to "public",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            if (messageType == "GIFT" && giftAmountCoins > 0L) {
                updates["giftsCoinsTotal"] = FieldValue.increment(giftAmountCoins)
            }
            if (messageType == "REACTION") {
                updates["likesCount"] = FieldValue.increment(1L)
            }
            if (updates.size > 1) {
                db.collection("live_streams").document(streamId).update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendLiveReaction(streamId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("live_streams").document(streamId).update(
                mapOf("likesCount" to FieldValue.increment(1L), "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "live_streams/$streamId")
            Result.failure(e)
        }
    }

    // --- Messaging (1:1 & Group Chats) ---

    suspend fun createConversation(
        title: String,
        groupChat: Boolean,
        peerName: String,
        creatorName: String
    ): Result<String> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanTitle = title.trim().ifBlank { peerName.ifBlank { "New Chat" } }.take(100)
        val convRef = db.collection("conversations").document()
        return try {
            convRef.set(
                mapOf(
                    "title" to cleanTitle,
                    "groupChat" to groupChat,
                    "memberUids" to listOf(uid),
                    "memberNames" to listOf(creatorName.ifBlank { "You" }, peerName.ifBlank { "Creator" }),
                    "lastMessage" to "Started a new conversation",
                    "lastSenderId" to uid,
                    "requestStatus" to "ACCEPTED",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(convRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(
        conversation: Conversation,
        senderName: String,
        text: String,
        messageKind: String = "TEXT",
        mediaNote: String? = null
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val cleanText = text.trim().take(1200)
        if (cleanText.isEmpty()) return Result.failure(IllegalArgumentException("Message cannot be empty."))
        val msgRef = db.collection("conversations").document(conversation.id).collection("messages").document()
        return try {
            val payload = mapOf(
                "conversationId" to conversation.id,
                "senderId" to uid,
                "senderName" to senderName.ifBlank { "You" }.take(80),
                "memberUids" to conversation.memberUids.ifEmpty { listOf(uid) },
                "text" to cleanText,
                "messageKind" to messageKind,
                "mediaNote" to mediaNote?.take(300),
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            msgRef.set(payload).await()

            db.collection("conversations").document(conversation.id).update(
                mapOf(
                    "lastMessage" to cleanText.take(500),
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun reactToMessage(conversationId: String, messageId: String, reaction: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
                .update(mapOf("reaction" to reaction.take(20))).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "conversations/$conversationId/messages/$messageId")
            Result.failure(e)
        }
    }

    // --- Safety Reports, Blocking, Notifications, Payouts & Admin Actions ---

    suspend fun submitReport(
        targetType: String,
        targetId: String,
        targetTitle: String,
        reason: String,
        aiRiskSummary: String? = null
    ): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        val reportRef = db.collection("reports").document()
        return try {
            val payload = mapOf(
                "reporterId" to uid,
                "targetType" to targetType,
                "targetId" to targetId.take(128),
                "targetTitle" to targetTitle.trim().ifBlank { "Reported Content" }.take(200),
                "reason" to reason.trim().ifBlank { "Community guidelines concern" }.take(500),
                "aiRiskSummary" to aiRiskSummary?.take(600),
                "status" to "OPEN",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            reportRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reportRef.path)
            Result.failure(e)
        }
    }

    suspend fun blockUser(targetUserId: String, currentBlocked: List<String>): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            val updated = (currentBlocked + targetUserId).distinct().takeLast(200)
            db.collection("users").document(uid).update(
                mapOf("blockedUserIds" to updated, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            db.collection("users").document(uid).collection("notifications").document(notificationId)
                .update(mapOf("read" to true)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/notifications/$notificationId")
            Result.failure(e)
        }
    }

    suspend fun requestPayout(amountInr: Double, source: String, upiId: String): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        if (amountInr < 100.0 || amountInr > 500000.0) {
            return Result.failure(IllegalArgumentException("Withdrawal amount must be between ₹100 and ₹5,00,000."))
        }
        val cleanUpi = upiId.trim().take(80)
        if (cleanUpi.length < 3) {
            return Result.failure(IllegalArgumentException("Please enter a valid UPI ID or masked bank reference."))
        }
        val payoutRef = db.collection("users").document(uid).collection("payouts").document()
        return try {
            payoutRef.set(
                mapOf(
                    "amountInr" to amountInr,
                    "source" to source.take(80),
                    "upiOrBankMasked" to cleanUpi,
                    "status" to "PENDING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, payoutRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminResolveReport(reportId: String, newStatus: String, removePostId: String? = null): Result<Unit> {
        return try {
            requireUserId()
            db.collection("reports").document(reportId).update(
                mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            if (!removePostId.isNullOrBlank()) {
                val postRef = db.collection("posts").document(removePostId)
                if (postRef.get().await().exists()) {
                    postRef.update(
                        mapOf("visibility" to "removed", "updatedAt" to FieldValue.serverTimestamp())
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "reports/$reportId")
            Result.failure(e)
        }
    }

    suspend fun adminToggleFeaturedPost(postId: String, featured: Boolean): Result<Unit> {
        return try {
            requireUserId()
            db.collection("posts").document(postId).update(
                mapOf("featured" to featured, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun adminToggleCreatorVerified(targetUserId: String, verified: Boolean): Result<Unit> {
        return try {
            requireUserId()
            db.collection("public_profiles").document(targetUserId).update(
                mapOf("verified" to verified, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            val userRef = db.collection("users").document(targetUserId)
            if (userRef.get().await().exists()) {
                userRef.update(
                    mapOf("verified" to verified, "updatedAt" to FieldValue.serverTimestamp())
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "public_profiles/$targetUserId")
            Result.failure(e)
        }
    }

    suspend fun deleteAccountData(): Result<Unit> {
        val uid = try { requireUserId() } catch (e: Exception) { return Result.failure(e) }
        return try {
            db.collection("public_profiles").document(uid).delete().await()
            db.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid")
            Result.failure(e)
        }
    }

    private fun logFlowError(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
