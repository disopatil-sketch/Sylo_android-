package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.SyloCatalog
import com.example.core.SyloLanguages
import com.example.data.model.UserAccount
import com.example.ui.theme.SyloBrandGradient

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    account: UserAccount,
    onComplete: (
        displayName: String,
        username: String,
        bio: String,
        location: String,
        interfaceLanguage: String,
        preferredLanguages: List<String>,
        interests: List<String>,
        privateAccount: Boolean
    ) -> Unit
) {
    var displayName by remember(account.displayName) { mutableStateOf(account.displayName) }
    var username by remember(account.username) { mutableStateOf(account.username) }
    var bio by remember(account.bio) { mutableStateOf(account.bio) }
    var location by remember(account.location) { mutableStateOf(account.location) }
    var interfaceLang by remember(account.interfaceLanguage) { mutableStateOf(account.interfaceLanguage) }
    var selectedLanguages by remember { mutableStateOf(account.preferredLanguages.ifEmpty { listOf("en", "hi") }.toSet()) }
    var selectedInterests by remember { mutableStateOf(account.interests.ifEmpty { listOf("Entertainment", "News", "Memes") }.toSet()) }
    var privateAccount by remember { mutableStateOf(account.privateAccount) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Personalize Your SYLO",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Pick your preferred South Asian languages, interests, and creator profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Profile Details", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it.take(80) },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.take(40) },
                        label = { Text("Username") },
                        prefix = { Text("@") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_username_input")
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it.take(80) },
                        label = { Text("City / Region (e.g., Pune, Dhaka, Kathmandu)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it.take(240) },
                        label = { Text("Bio") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Private Account", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Approve followers before they view private posts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = privateAccount, onCheckedChange = { privateAccount = it })
                    }
                }
            }

            // Interface Language
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Interface Language", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyloLanguages.all.take(9).forEach { lang ->
                            FilterChip(
                                selected = interfaceLang == lang.code,
                                onClick = {
                                    interfaceLang = lang.code
                                    selectedLanguages = selectedLanguages + lang.code
                                },
                                label = { Text("${lang.nativeName} • ${lang.englishName}") }
                            )
                        }
                    }
                }
            }

            // Content Languages
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Preferred Content Languages", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyloLanguages.all.forEach { lang ->
                            val selected = selectedLanguages.contains(lang.code)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedLanguages = if (selected && selectedLanguages.size > 1) {
                                        selectedLanguages - lang.code
                                    } else {
                                        selectedLanguages + lang.code
                                    }
                                },
                                label = { Text(lang.nativeName) }
                            )
                        }
                    }
                }
            }

            // Interests
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your Interests", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyloCatalog.interests.forEach { topic ->
                            val selected = selectedInterests.contains(topic)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedInterests = if (selected && selectedInterests.size > 1) {
                                        selectedInterests - topic
                                    } else {
                                        selectedInterests + topic
                                    }
                                },
                                label = { Text(topic) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    onComplete(
                        displayName,
                        username,
                        bio,
                        location,
                        interfaceLang,
                        selectedLanguages.toList(),
                        selectedInterests.toList(),
                        privateAccount
                    )
                },
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("complete_onboarding_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SyloBrandGradient, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enter SYLO",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
