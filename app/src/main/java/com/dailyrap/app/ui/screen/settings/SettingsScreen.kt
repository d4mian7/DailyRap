package com.dailyrap.app.ui.screen.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.BorderSubtle
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText

private val ErrorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Wróć",
                    tint = TileText
                )
            }
            Text(
                text = "Ustawienia",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TileText
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Wibracje
            SettingsRow(
                icon = "\uD83D\uDCF3",
                title = "Wibracje",
                description = "Wibracja przy klikaniu klawiszy w RapWord"
            ) {
                Switch(
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = { viewModel.setVibration(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TileText,
                        checkedTrackColor = TileSelected,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = TileDefault
                    )
                )
            }

            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

            // 2. Powiadomienia
            SettingsRow(
                icon = "\uD83D\uDD14",
                title = "Powiadomienia",
                description = "Przypomnienie o dzisiejszym wyzwaniu"
            ) {
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotifications(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TileText,
                        checkedTrackColor = TileSelected,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = TileDefault
                    )
                )
            }

            // Time picker
            if (uiState.notificationsEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileDefault)
                        .clickable { showTimePicker = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Godzina przypomnienia",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = String.format("%02d:%02d", uiState.notificationHour, uiState.notificationMinute),
                        color = TileSelected,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

            // 3. Resetuj statystyki
            SettingsRow(
                icon = "\uD83D\uDDD1\uFE0F",
                title = "Resetuj statystyki",
                description = "Wyczyść wszystkie wyniki gier"
            ) {
                Button(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                ) {
                    Text("Resetuj", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

            // 4. Udostępnij aplikację
            SettingsRow(
                icon = "\uD83D\uDCE4",
                title = "Udostępnij aplikację",
                description = "Poleć DailyRap znajomym"
            ) {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Sprawdź DailyRap — codzienne rapowe łamigłówki! \uD83C\uDFA4\uD83D\uDD25 https://play.google.com/store/apps/details?id=com.dailyrap.app"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Udostępnij"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TileDefault)
                ) {
                    Text("Udostępnij", color = TileText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

            Spacer(modifier = Modifier.height(24.dp))

            // 5. O aplikacji
            Text(
                text = "O aplikacji",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileDefault)
                    .padding(16.dp)
            ) {
                Text(text = "DailyRap v1.0", color = TileText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Kontakt: dailyrap@email.com", color = TextSecondary, fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:dailyrap@email.com"))
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Oceń aplikację \u2B50",
                    color = TileSelected,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dailyrap.app"))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = BackgroundDark,
            title = { Text("Resetuj statystyki", color = TileText, fontWeight = FontWeight.Bold) },
            text = { Text("Czy na pewno chcesz zresetować wszystkie statystyki? Ta akcja jest nieodwracalna.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetStats()
                    showResetDialog = false
                    Toast.makeText(context, "Statystyki zresetowane", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Resetuj", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Anuluj", color = TextSecondary)
                }
            }
        )
    }

    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.notificationHour,
            initialMinute = uiState.notificationMinute,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier
                    .background(BackgroundDark, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Godzina przypomnienia", color = TileText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Anuluj", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        viewModel.setNotificationTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) {
                        Text("OK", color = TileSelected, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    description: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TileText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(text = description, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        trailing()
    }
}
