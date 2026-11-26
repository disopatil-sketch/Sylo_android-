package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.ui.theme.SyloBrandGradient
import com.example.ui.theme.SyloEmerald
import com.example.ui.theme.SyloSaffron
import com.example.ui.theme.SyloViolet

@Composable
fun MessagingScreen(
    currentUserId: String,
    conversations: List<Conversation>,
    activeConversationId: String?,
    messages: List<ChatMessage>,
    onlineStatusVisible: Boolean,
    onToggleOnlineStatus: (Boolean) -> Unit,
    onSelectConversation: (String?) -> Unit,
    onCreateConversation: (title: String, peerName: String, groupChat: Boolean) -> Unit,
    onSendMessage: (Conversation, String, String, String?) -> Unit,
    onReactMessage: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val activeConv = remember(conversations, activeConversationId) {
        conversations.firstOrNull { it.id == activeConversationId }
    }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf("ALL") }

    BackHandler {
        if (activeConv != null) onSelectConversation(null) else onBack()
    }

    if (activeConv != null) {
        ChatThreadView(
            currentUserId = currentUserId,
            conversation = activeConv,
            messages = messages,
            onSend = { text, kind, note -> onSendMessage(activeConv, text, kind, note) },
            onReact = { msgId, emoji -> onReactMessage(activeConv.id, msgId, emoji) },
            onBack = { onSelectConversation(null) }
        )
        return
    }

    val filtered = remember(conversations, filterMode) {
        when (filterMode) {
            "GROUPS" -> conversations.filter { it.groupChat }
            "REQUESTS" -> conversations.filter { it.requestStatus == "REQUEST" }
            else -> conversations
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Messages", style = MaterialTheme.typography.headlineSmall)
                }
                IconButton(
                    onClick = { showNewChatDialog = true },
                    modifier = Modifier.testTag("new_chat_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Chat", tint = SyloSaffron)
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (onlineStatusVisible) SyloEmerald else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show Online Status", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = onlineStatusVisible, onCheckedChange = onToggleOnlineStatus)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "Inbox", "GROUPS" to "Groups", "REQUESTS" to "Requests").forEach { (k, label) ->
                    FilterChip(
                        selected = filterMode == k,
                        onClick = { filterMode = k },
                        label = { Text(label) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No conversations here yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap + to start a 1-to-1 chat or multilingual creator group.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { conv ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectConversation(conv.id) }
                        .testTag("conversation_${conv.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(SyloBrandGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (conv.groupChat) Icons.Filled.Group else Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(conv.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                conv.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        var chatTitle by remember { mutableStateOf("") }
        var peerName by remember { mutableStateOf("") }
        var isGroup by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("New SYLO Chat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Group Chat")
                        Switch(checked = isGroup, onCheckedChange = { isGroup = it })
                    }
                    OutlinedTextField(
                        value = chatTitle,
                        onValueChange = { chatTitle = it.take(80) },
                        label = { Text(if (isGroup) "Group Name" else "Chat Subject / Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = peerName,
                        onValueChange = { peerName = it.take(60) },
                        label = { Text("Creator Handle or Name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateConversation(chatTitle.ifBlank { peerName.ifBlank { "Creator Chat" } }, peerName, isGroup)
                        showNewChatDialog = false
                    },
                    modifier = Modifier.testTag("confirm_new_chat_button")
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChatThreadView(
    currentUserId: String,
    conversation: Conversation,
    messages: List<ChatMessage>,
    onSend: (String, String, String?) -> Unit,
    onReact: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var voiceRecording by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onSend("Shared a photo attachment", "PHOTO", uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to inbox")
                }
                Column {
                    Text(conversation.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (conversation.groupChat) "${conversation.memberNames.size} participants" else "Encrypted SYLO Direct Chat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == currentUserId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isMine) SyloViolet else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.clickable {
                            onReact(msg.id, if (msg.reaction == "❤️") "🔥" else "❤️")
                        }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            if (!isMine) {
                                Text(
                                    msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SyloSaffron
                                )
                            }
                            if (msg.messageKind == "VOICE") {
                                Text(
                                    "🎙 Voice Note • ${msg.text}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            } else if (msg.messageKind == "PHOTO") {
                                Text(
                                    "📷 ${msg.text}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (msg.reaction.isNotBlank()) {
                        Text(
                            text = msg.reaction,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 2.dp, start = 6.dp, end = 6.dp)
                        )
                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Filled.Image, contentDescription = "Share photo", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    if (voiceRecording) {
                        voiceRecording = false
                        onSend("0:07 regional voice message", "VOICE", "7s")
                    } else {
                        voiceRecording = true
                    }
                }) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Voice message",
                        tint = if (voiceRecording) Color.Red else SyloSaffron
                    )
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(1000) },
                    placeholder = { Text(if (voiceRecording) "Tap mic again to send voice note..." else "Message...") },
                    singleLine = true,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            onSend(input, "TEXT", null)
                            input = ""
                        }
                    },
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = SyloSaffron)
                }
            }
        }
    }
}
