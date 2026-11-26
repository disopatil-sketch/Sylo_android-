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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.core.LocalAppPreferences
import com.example.core.SyloLanguages
import com.example.data.model.ModerationReport
import com.example.data.model.PayoutRequest
import com.example.data.model.PostItem
import com.example.data.model.PublicProfile
import com.example.data.model.UserAccount
import com.example.data.model.UserNotification
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloCrimsonLive
import com.example.ui.theme.SyloEmerald
import com.example.ui.theme.SyloMarigold
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloSkyVerified
import com.example.ui.theme.SyloViolet

@Composable
fun ProfileDashboardScreen(
    account: UserAccount?,
    allPosts: List<PostItem>,
    publicProfiles: List<PublicProfile>,
    notifications: List<UserNotification>,
    payouts: List<PayoutRequest>,
    reports: List<ModerationReport>,
    localPrefs: LocalAppPreferences,
    isAdmin: Boolean,
    initialSection: String,
    onUpdateProfile: (String, String, String, Boolean) -> Unit,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetLowDataMode: (Boolean) -> Unit,
    onSetInterfaceLanguage: (String) -> Unit,
    onSetNotificationPref: (String, Boolean) -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onRequestWithdrawal: (Double, String) -> Unit,
    onAdminResolveReport: (String, String, String?) -> Unit,
    onAdminToggleFeatured: (String, Boolean) -> Unit,
    onAdminToggleVerified: (String, Boolean) -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    var activeSection by remember(initialSection) { mutableStateOf(initialSection.ifBlank { "POSTS" }) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val myPosts = remember(allPosts, account) { allPosts.filter { it.authorId == account?.userId } }
    val likedPosts = remember(allPosts, account) {
        val ids = account?.likedPostIds.orEmpty().toSet()
        allPosts.filter { ids.contains(it.id) }
    }
    val savedPosts = remember(allPosts, account) {
        val ids = account?.savedPostIds.orEmpty().toSet()
        allPosts.filter { ids.contains(it.id) }
    }
    val totalViews = remember(myPosts) { myPosts.sumOf { it.viewsCount } }
    val totalLikes = remember(myPosts) { myPosts.sumOf { it.likesCount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cover + Profile Identity Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            .background(SyloBrandGradient)
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            IconButton(onClick = { showEditDialog = true }, modifier = Modifier.testTag("edit_profile_button")) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit profile", tint = Color.White)
                            }
                            IconButton(onClick = onSignOut, modifier = Modifier.testTag("sign_out_button")) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out", tint = Color.White)
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(SyloViolet),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (account?.displayName ?: "S").take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(account?.displayName ?: "Sylo Creator", style = MaterialTheme.typography.titleLarge)
                                    if (account?.verified == true || isAdmin) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = SyloSkyVerified, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    "@${account?.username ?: "creator"} • ${account?.location ?: "India"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (account?.privateAccount == true) {
                                    Text("Private Account", style = MaterialTheme.typography.labelSmall, color = SyloSaffron)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            account?.bio.orEmpty().ifBlank { "Creator on SYLO — Your Language. Your World." },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatPill("Posts", myPosts.size.toString())
                            StatPill("Views", formatCompactCount(totalViews))
                            StatPill("Likes", formatCompactCount(totalLikes))
                            StatPill("Following", (account?.followingIds?.size ?: 0).toString())
                        }
                    }
                }
            }
        }

        // Section Switcher
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = buildList {
                    add("POSTS" to "My Content")
                    add("SAVED" to "Liked & Saved")
                    add("MONETIZE" to "Creator Studio ₹")
                    add("ALERTS" to "Notifications (${notifications.count { !it.read }})")
                    add("SETTINGS" to "Settings & Safety")
                    if (isAdmin) add("ADMIN" to "Admin Panel")
                }
                items(tabs) { (key, label) ->
                    FilterChip(
                        selected = activeSection == key,
                        onClick = { activeSection = key },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Section Content
        when (activeSection) {
            "POSTS" -> {
                if (myPosts.isEmpty()) {
                    item { EmptyProfileCard("No uploads yet", "Tap Create (+) in the bottom bar to publish reels, memes, or posts.") }
                } else {
                    items(myPosts, key = { it.id }) { post ->
                        CompactProfilePostRow(post)
                    }
                }
            }

            "SAVED" -> {
                item { Text("Saved Posts (${savedPosts.size})", style = MaterialTheme.typography.titleMedium) }
                if (savedPosts.isEmpty()) {
                    item { EmptyProfileCard("No saved posts", "Tap the bookmark icon on any post or video to save it here.") }
                } else {
                    items(savedPosts, key = { "saved_${it.id}" }) { CompactProfilePostRow(it) }
                }
                item { Text("Liked Posts (${likedPosts.size})", style = MaterialTheme.typography.titleMedium) }
                items(likedPosts, key = { "liked_${it.id}" }) { CompactProfilePostRow(it) }
            }

            "MONETIZE" -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SyloEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = SyloEmerald)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Creator Monetization & Eligibility", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "Estimated Balance: ₹${String.format("%.2f", (account?.earningsInr ?: 0.0) + totalViews * 0.05)} • Gift Coins: ${account?.giftCoins ?: 0}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = SyloEmerald
                            )
                            Text(
                                "• Ad Revenue Sharing: Active for verified regional creators\n" +
                                    "• Live Gifts & Brand Collaborations: Eligible under Indian tax & GST/TDS compliance\n" +
                                    "• Fraud Protection: All payouts audited by Admin before settlement",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showWithdrawDialog = true },
                                modifier = Modifier.fillMaxWidth().testTag("request_payout_button")
                            ) {
                                Text("Request UPI / Bank Withdrawal")
                            }
                        }
                    }
                }

                item { Text("Payment & Withdrawal History", style = MaterialTheme.typography.titleMedium) }
                if (payouts.isEmpty()) {
                    item { EmptyProfileCard("No withdrawal requests yet", "Submit a withdrawal request above once eligible (min ₹100).") }
                } else {
                    items(payouts, key = { it.id }) { p ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("₹${p.amountInr.toInt()} • ${p.source}", style = MaterialTheme.typography.titleSmall)
                                    Text(p.upiOrBankMasked, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(color = SyloMarigold.copy(alpha = 0.2f), shape = CircleShape) {
                                    Text(p.status, style = MaterialTheme.typography.labelSmall, color = SyloMarigold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            "ALERTS" -> {
                item {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = SyloSaffron)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Notification Preferences", style = MaterialTheme.typography.titleSmall)
                            }
                            ToggleSettingRow("New Followers", localPrefs.notifyFollows) { onSetNotificationPref("follows", it) }
                            ToggleSettingRow("Likes, Comments & Mentions", localPrefs.notifyLikesComments) { onSetNotificationPref("likes", it) }
                            ToggleSettingRow("Creator Live Video Alerts", localPrefs.notifyLiveAlerts) { onSetNotificationPref("live", it) }
                            ToggleSettingRow("Breaking News Alerts", localPrefs.notifyNewsAlerts) { onSetNotificationPref("news", it) }
                        }
                    }
                }

                items(notifications, key = { it.id }) { alert ->
                    Surface(
                        color = if (alert.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!alert.read) onMarkNotificationRead(alert.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("${alert.category} • ${alert.title}", style = MaterialTheme.typography.titleSmall)
                            Text(alert.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            "SETTINGS" -> {
                item {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = SyloViolet)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Display & Network", style = MaterialTheme.typography.titleMedium)
                            }
                            ToggleSettingRow("Dark Mode (Obsidian Plum)", localPrefs.darkTheme, onSetDarkTheme)
                            ToggleSettingRow("Low-Data Mode (Indian Mobile Networks)", localPrefs.lowDataMode, onSetLowDataMode)

                            HorizontalDivider()

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Language, contentDescription = null, tint = SyloSaffron)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("App Interface Language", style = MaterialTheme.typography.titleMedium)
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(SyloLanguages.all, key = { it.code }) { lang ->
                                    FilterChip(
                                        selected = localPrefs.interfaceLanguage == lang.code,
                                        onClick = { onSetInterfaceLanguage(lang.code) },
                                        label = { Text("${lang.nativeName} (${lang.englishName})") }
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = SyloEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Safety, Privacy & Data Rights", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "Blocked accounts: ${account?.blockedUserIds?.size ?: 0} • Google Sign-In protected • Firestore zero-trust isolation active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SyloCrimsonLive),
                                modifier = Modifier.fillMaxWidth().testTag("delete_account_button")
                            ) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete Account Data")
                            }
                        }
                    }
                }
            }

            "ADMIN" -> {
                if (isAdmin) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SyloSaffron.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = SyloSaffron)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SYLO Admin Governance Dashboard", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    "Total Public Posts: ${allPosts.size} • Creators: ${publicProfiles.size} • Open Reports: ${reports.count { it.status == "OPEN" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item { Text("Reported Content & Moderation Queue", style = MaterialTheme.typography.titleMedium) }
                    if (reports.isEmpty()) {
                        item { EmptyProfileCard("Moderation queue clear", "No open user safety or copyright reports.") }
                    } else {
                        items(reports, key = { it.id }) { report ->
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${report.targetType} • ${report.targetTitle} [${report.status}]", style = MaterialTheme.typography.titleSmall)
                                    Text("Reason: ${report.reason}", style = MaterialTheme.typography.bodySmall)
                                    if (report.aiRiskSummary.isNotBlank()) {
                                        Text("AI Triage: ${report.aiRiskSummary}", style = MaterialTheme.typography.labelSmall, color = SyloSaffron)
                                    }
                                    if (report.status == "OPEN") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = {
                                                onAdminResolveReport(
                                                    report.id,
                                                    "RESOLVED",
                                                    if (report.targetType == "POST" || report.targetType == "VIDEO") report.targetId else null
                                                )
                                            }) { Text("Remove & Resolve") }
                                            OutlinedButton(onClick = { onAdminResolveReport(report.id, "DISMISSED", null) }) {
                                                Text("Dismiss")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Text("Featured News & Trending Control", style = MaterialTheme.typography.titleMedium) }
                    items(allPosts.take(6), key = { "admin_post_${it.id}" }) { post ->
                        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    Text("${post.contentType} • ${post.authorHandle}", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { onAdminToggleFeatured(post.id, !post.featured) }) {
                                    Text(if (post.featured) "Unfeature" else "Feature")
                                }
                            }
                        }
                    }

                    item { Text("Creator Verification Requests", style = MaterialTheme.typography.titleMedium) }
                    items(publicProfiles.take(6), key = { "admin_prof_${it.id}" }) { prof ->
                        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prof.displayName, style = MaterialTheme.typography.titleSmall)
                                    Text("@${prof.username}", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { onAdminToggleVerified(prof.userId, !prof.verified) }) {
                                    Text(if (prof.verified) "Verified ✓" else "Verify")
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(account?.displayName ?: "") }
        var bio by remember { mutableStateOf(account?.bio ?: "") }
        var loc by remember { mutableStateOf(account?.location ?: "") }
        var priv by remember { mutableStateOf(account?.privateAccount ?: false) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it.take(80) }, label = { Text("Display Name") }, singleLine = true)
                    OutlinedTextField(value = loc, onValueChange = { loc = it.take(80) }, label = { Text("Location") }, singleLine = true)
                    OutlinedTextField(value = bio, onValueChange = { bio = it.take(240) }, label = { Text("Bio") }, maxLines = 3)
                    ToggleSettingRow("Private Account", priv) { priv = it }
                }
            },
            confirmButton = {
                Button(onClick = { onUpdateProfile(name, bio, loc, priv); showEditDialog = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showWithdrawDialog) {
        var amountStr by remember { mutableStateOf("500") }
        var upi by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Creator Withdrawal Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount (INR, min ₹100)") }, singleLine = true)
                    OutlinedTextField(value = upi, onValueChange = { upi = it.take(60) }, label = { Text("UPI ID or Masked Bank (e.g., creator@upi)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    onRequestWithdrawal(amt, upi)
                    showWithdrawDialog = false
                }) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account Data?") },
            text = { Text("This permanently deletes your SYLO profile data from Firestore and signs you out.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDeleteAccount() },
                    colors = ButtonDefaults.buttonColors(containerColor = SyloCrimsonLive)
                ) { Text("Delete Permanently") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompactProfilePostRow(post: PostItem) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(post.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(
                    "${post.contentType} • ${SyloLanguages.byCode(post.languageCode).nativeName} • ${formatCompactCount(post.likesCount)} likes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("${formatCompactCount(post.viewsCount)} views", style = MaterialTheme.typography.labelSmall, color = SyloSaffron)
        }
    }
}

@Composable
private fun ToggleSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EmptyProfileCard(title: String, subtitle: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
