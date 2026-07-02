package com.priyanshparekh.repbook.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.priyanshparekh.repbook.domain.model.Workout
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBottomSheet(
    pendingSchedule: Map<Int, Long?>,
    currentSchedule: Map<Int, Long>,
    allWorkouts: List<Workout>,
    onDayTapped: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val workoutMap = remember(allWorkouts) { allWorkouts.associateBy { it.id } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan Week",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = onSave) {
                    Text("Save")
                }
            }

            Spacer(Modifier.height(16.dp))

            (1..7).forEach { day ->
                val pendingId: Long? = if (pendingSchedule.containsKey(day)) pendingSchedule[day] else currentSchedule[day]
                val currentId: Long? = currentSchedule[day]
                val hasPendingChange = pendingSchedule.containsKey(day) && pendingSchedule[day] != currentId
                val workoutName = pendingId?.let { workoutMap[it]?.name }

                DayScheduleRow(
                    day = day,
                    workoutName = workoutName,
                    hasPendingChange = hasPendingChange,
                    onClick = { onDayTapped(day) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DayScheduleRow(
    day: Int,
    workoutName: String?,
    hasPendingChange: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = workoutName ?: "Rest",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasPendingChange) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WorkoutPickerDialog(
    day: Int,
    allWorkouts: List<Workout>,
    initialSelection: Long?,
    onConfirm: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val dayName = DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.getDefault())
    var selected by remember { mutableStateOf(initialSelection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dayName) },
        text = {
            Column {
                PickerRow(
                    label = "Rest",
                    selected = selected == null,
                    onClick = { selected = null }
                )
                allWorkouts.forEach { workout ->
                    PickerRow(
                        label = workout.name,
                        selected = selected == workout.id,
                        onClick = { selected = workout.id }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PickerRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
