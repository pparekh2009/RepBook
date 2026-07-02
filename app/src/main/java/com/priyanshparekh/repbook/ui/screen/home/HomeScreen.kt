package com.priyanshparekh.repbook.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.priyanshparekh.repbook.domain.model.WorkoutStatus
import com.priyanshparekh.repbook.domain.model.WorkoutWithStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalLocale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartWorkout: (workoutId: Long, exerciseId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Plan Week") },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                onClick = viewModel::openScheduleSheet
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "RepBook",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            WeeklyCalendar(
                scheduledDays = uiState.currentSchedule.keys,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.workoutsWithStatus.isEmpty() ->
                    HomeEmptyState(modifier = Modifier.weight(1f))

                else -> WorkoutCardList(
                    workoutsWithStatus = uiState.workoutsWithStatus,
                    onCardTapped = viewModel::onWorkoutCardTapped,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (uiState.isScheduleSheetOpen) {
        ScheduleBottomSheet(
            pendingSchedule = uiState.pendingSchedule,
            currentSchedule = uiState.currentSchedule,
            allWorkouts = uiState.allWorkouts,
            onDayTapped = viewModel::onDayTapped,
            onSave = viewModel::saveSchedule,
            onDismiss = viewModel::dismissScheduleSheet
        )
    }

    uiState.pickerDay?.let { day ->
        val initialSelection = if (uiState.pendingSchedule.containsKey(day)) {
            uiState.pendingSchedule[day]
        } else {
            uiState.currentSchedule[day]
        }
        WorkoutPickerDialog(
            day = day,
            allWorkouts = uiState.allWorkouts,
            initialSelection = initialSelection,
            onConfirm = { workoutId -> viewModel.onWorkoutSelectedForDay(day, workoutId) },
            onDismiss = viewModel::dismissPickerDialog
        )
    }

    uiState.startWorkoutDialog?.let { dialog ->
        when (dialog) {
            is StartWorkoutDialog.ForToday -> StartTodayWorkoutDialog(
                workoutName = dialog.workoutName,
                onStart = {
                    viewModel.dismissStartWorkoutDialog()
                    onStartWorkout(dialog.workoutId, dialog.exerciseId)
                },
                onDismiss = viewModel::dismissStartWorkoutDialog
            )
            is StartWorkoutDialog.ForFutureDay -> StartFutureDayWorkoutDialog(
                workoutName = dialog.workoutName,
                onStartAnyway = {
                    viewModel.dismissStartWorkoutDialog()
                    onStartWorkout(dialog.workoutId, dialog.exerciseId)
                },
                onDismiss = viewModel::dismissStartWorkoutDialog
            )
        }
    }
}

@Composable
fun WeeklyCalendar(scheduledDays: Set<Int>, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(7) { offset ->
            val date = monday.plusDays(offset.toLong())
            // ISO day-of-week: Monday = 1, Sunday = 7; matches currentSchedule keys
            DayCell(
                date = date,
                isToday = date == today,
                hasWorkout = scheduledDays.contains(offset + 1)
            )
        }
    }
}

@Composable
fun DayCell(date: LocalDate, isToday: Boolean, hasWorkout: Boolean) {
    val dayAbbr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dayAbbr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(
                    if (isToday) Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .semantics(mergeDescendants = true) { testTag = "today_highlight" }
                    else Modifier
                )
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .then(
                    if (hasWorkout) Modifier.background(MaterialTheme.colorScheme.primary)
                    else Modifier
                )
        )
    }
}

@Composable
fun WorkoutCardList(
    workoutsWithStatus: List<WorkoutWithStatus>,
    onCardTapped: (WorkoutWithStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(workoutsWithStatus, key = { it.day }) { item ->
            WorkoutCard(
                workoutWithStatus = item,
                onClick = { onCardTapped(item) }
            )
        }
    }
}

@Composable
fun WorkoutCard(workoutWithStatus: WorkoutWithStatus, onClick: () -> Unit) {
    val dayName = DayOfWeek.of(workoutWithStatus.day)
        .getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workoutWithStatus.workout.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = workoutWithStatus.status)
        }
    }
}

@Composable
fun StatusBadge(status: WorkoutStatus) {
    val containerColor = when (status) {
        WorkoutStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        WorkoutStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
        WorkoutStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceVariant
        WorkoutStatus.INCOMPLETE -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (status) {
        WorkoutStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
        WorkoutStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondaryContainer
        WorkoutStatus.SCHEDULED -> MaterialTheme.colorScheme.onSurfaceVariant
        WorkoutStatus.INCOMPLETE -> MaterialTheme.colorScheme.onErrorContainer
    }
    val label = when (status) {
        WorkoutStatus.COMPLETED -> "Done"
        WorkoutStatus.IN_PROGRESS -> "Active"
        WorkoutStatus.SCHEDULED -> "Scheduled"
        WorkoutStatus.INCOMPLETE -> "Missed"
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun HomeEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No workouts this week",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap 'Plan Week' to assign workouts to days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StartTodayWorkoutDialog(
    workoutName: String,
    onStart: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Workout") },
        text = { Text("Ready to start $workoutName?") },
        confirmButton = {
            TextButton(onClick = onStart) { Text("Start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StartFutureDayWorkoutDialog(
    workoutName: String,
    onStartAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Not Scheduled for Today") },
        text = { Text("$workoutName is scheduled for a future day. Start it anyway?") },
        confirmButton = {
            TextButton(onClick = onStartAnyway) { Text("Start Anyway") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
