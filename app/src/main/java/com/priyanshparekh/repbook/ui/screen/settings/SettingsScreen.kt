package com.priyanshparekh.repbook.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.priyanshparekh.repbook.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToExport: () -> Unit,
    onNavigateToImport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SettingsSection(title = "Workout Settings") {
                    ToggleSettingItem(
                        label = "Auto-advance",
                        supporting = "Automatically start the next exercise when rest ends",
                        checked = uiState.autoAdvance,
                        onCheckedChange = viewModel::onAutoAdvanceChanged
                    )
                    NumberSettingItem(
                        label = "Rest between sets",
                        supporting = "Countdown shown between consecutive sets of the same exercise",
                        value = uiState.restBetweenSets,
                        unit = "sec",
                        onValueChange = viewModel::onRestBetweenSetsChanged
                    )
                    NumberSettingItem(
                        label = "Rest between exercises",
                        supporting = "Countdown shown when moving to the next exercise",
                        value = uiState.restBetweenExercises,
                        unit = "sec",
                        onValueChange = viewModel::onRestBetweenExercisesChanged
                    )
                }
                HorizontalDivider()
            }
            item {
                SettingsSection(title = "Appearance") {
                    ThemeSegmentedControl(
                        selected = uiState.themeMode,
                        onSelected = viewModel::onThemeModeChanged
                    )
                }
                HorizontalDivider()
            }
            item {
                SettingsSection(title = "Data Management") {
                    SettingsClickItem(
                        label = "Export",
                        supporting = "Save all workouts and data to a file",
                        onClick = onNavigateToExport
                    )
                    SettingsClickItem(
                        label = "Import",
                        supporting = "Restore data from a previously exported file. Replaces all current data.",
                        onClick = onNavigateToImport
                    )
                }
                HorizontalDivider()
            }
            item {
                SettingsSection(title = "About") {
                    AboutSection(appVersion = uiState.appVersion)
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

@Composable
fun ToggleSettingItem(
    label: String,
    supporting: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = if (supporting.isNotEmpty()) {
            { Text(supporting) }
        } else null,
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
fun NumberSettingItem(
    label: String,
    supporting: String = "",
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(label) },
        supportingContent = if (supporting.isNotEmpty()) {
            { Text(supporting) }
        } else null,
        trailingContent = {
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        NumberInputDialog(
            title = label,
            initialValue = value,
            unit = unit,
            onConfirm = { newValue ->
                onValueChange(newValue)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun NumberInputDialog(
    title: String,
    initialValue: Int,
    unit: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    val parsed = text.toIntOrNull()
    val isValid = parsed != null && parsed in 5..600

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text(
                    text = "10–300 seconds recommended",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let { onConfirm(it) } },
                enabled = isValid
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ThemeSegmentedControl(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            val label = mode.name.lowercase().replaceFirstChar { it.uppercase() }
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ThemeMode.entries.size
                ),
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun SettingsClickItem(
    label: String,
    supporting: String = "",
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = if (supporting.isNotEmpty()) {
            { Text(supporting) }
        } else null,
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun AboutSection(appVersion: String) {
    ListItem(
        headlineContent = { Text("RepBook") },
        supportingContent = { Text("Version $appVersion") }
    )
}
