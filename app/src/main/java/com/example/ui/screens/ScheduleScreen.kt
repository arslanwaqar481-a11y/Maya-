package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReminderEntity
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardBorderGlow
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun ScheduleScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingCount = reminders.count { !it.isCompleted }
    val completedCount = reminders.count { it.isCompleted }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = HudBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ArcCyan,
                contentColor = Color(0xFF060B14),
                shape = CircleShape,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Directive")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats & Speech Briefing Action
            item {
                HudGlassCard(
                    headerTitle = "Tactical Agenda & Schedule",
                    statusIndicator = "$pendingCount PENDING",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DAILY OPERATIONAL MATRIX",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HudTextPrimary
                                )
                                Text(
                                    text = "$completedCount completed // $pendingCount pending review",
                                    fontSize = 11.sp,
                                    color = HudTextSecondary
                                )
                            }

                            Button(
                                onClick = { viewModel.readScheduleVoice() },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisGold, contentColor = Color(0xFF060B14)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("read_schedule_voice_button")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BRIEF ME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (completedCount > 0) {
                            TextButton(
                                onClick = { viewModel.clearCompletedReminders() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Clear Completed Items", color = HudTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Reminders List
            if (reminders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No directives currently registered, Sir. Tap '+' or speak to log a schedule item.",
                            color = HudTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(reminders, key = { it.id }) { item ->
                    ScheduleItemCard(
                        reminder = item,
                        onToggle = { viewModel.toggleReminder(item) },
                        onDelete = { viewModel.deleteReminder(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, time, category, priority ->
                viewModel.addReminder(title, time, category, priority)
                viewModel.speak("Added $title to tactical schedule at $time, Sir.")
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleItemCard(
    reminder: ReminderEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (reminder.priority) {
        "Urgent" -> StatusRed
        "High" -> JarvisGold
        else -> ArcCyan
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (reminder.isCompleted) HudSurface.copy(alpha = 0.5f) else HudSurface)
            .border(1.dp, if (reminder.isCompleted) HudCardBorder.copy(alpha = 0.5f) else HudCardBorder, RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle status",
                tint = if (reminder.isCompleted) StatusGreen else HudTextMuted
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reminder.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (reminder.isCompleted) HudTextMuted else HudTextPrimary,
                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reminder.timeLabel,
                    fontSize = 10.sp,
                    color = ArcCyanLight
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = reminder.priority.uppercase(),
                        fontSize = 8.5.sp,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "• ${reminder.category}",
                    fontSize = 9.5.sp,
                    color = HudTextSecondary
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = HudTextMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("09:00 AM") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("Normal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HudSurface,
        title = {
            Text("NEW TACTICAL DIRECTIVE", color = ArcCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Directive Title", color = HudTextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = HudCardBorder,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Execution Time (e.g. 10:30 AM)", color = HudTextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = HudCardBorder,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Work", "Routine", "Health").forEach { cat ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (category == cat) ArcCyan.copy(alpha = 0.25f) else HudBackground)
                                .border(1.dp, if (category == cat) ArcCyan else HudCardBorder, RoundedCornerShape(6.dp))
                                .clickable { category = cat }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = if (category == cat) ArcCyanLight else HudTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, time, category, priority)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = Color(0xFF060B14))
            ) {
                Text("LOG DIRECTIVE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = HudTextMuted, fontSize = 11.sp)
            }
        }
    )
}
