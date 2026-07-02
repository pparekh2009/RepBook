package com.priyanshparekh.repbook.ui.screen.exercise

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel,
    onNavigateToRest: (workoutId: Long, exerciseId: Long, completedSetNo: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { args ->
            onNavigateToRest(args.workoutId, args.exerciseId, args.completedSetNo)
        }
    }

    val buttonLabel = when {
        uiState.setNo == uiState.totalSets &&
        uiState.completedExerciseCount == uiState.totalExerciseCount - 1 -> "Done — Finish Workout"
        uiState.setNo == uiState.totalSets -> "Done — Next Exercise"
        else -> "Done — Next Set"
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(uiState.workoutName) })
        },
        bottomBar = {
            DoneButton(
                label = buttonLabel,
                onClick = viewModel::onDoneClick,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WorkoutProgressSection(
                completedExerciseCount = uiState.completedExerciseCount,
                totalExerciseCount = uiState.totalExerciseCount,
                progressPercent = uiState.progressPercent
            )
            if (uiState.isTimeBased) {
                TimeBasedExerciseCard(uiState = uiState)
            } else {
                ExerciseInfoCard(uiState = uiState)
            }
        }
    }
}

@Composable
fun WorkoutProgressSection(
    completedExerciseCount: Int,
    totalExerciseCount: Int,
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Exercise $completedExerciseCount of $totalExerciseCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progressPercent.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ExerciseInfoCard(uiState: ExerciseUiState, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = uiState.exerciseName,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Set ${uiState.setNo} of ${uiState.totalSets}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SetProgressDots(setNo = uiState.setNo, totalSets = uiState.totalSets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SetValueColumn(
                    label = "Weight",
                    value = if (uiState.weight % 1f == 0f) "${uiState.weight.toInt()} kg"
                            else "${"%.1f".format(uiState.weight)} kg"
                )
                SetValueColumn(
                    label = "Reps",
                    value = uiState.reps.toString()
                )
            }
        }
    }
}

@Composable
fun TimeBasedExerciseCard(uiState: ExerciseUiState, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = uiState.exerciseName,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Set ${uiState.setNo} of ${uiState.totalSets}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SetProgressDots(setNo = uiState.setNo, totalSets = uiState.totalSets)
            ExerciseTimerDisplay(
                remainingSeconds = uiState.remainingSeconds,
                durationSeconds = uiState.durationSeconds
            )
        }
    }
}

@Composable
private fun SetProgressDots(setNo: Int, totalSets: Int) {
    if (totalSets > 6) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSets) {
            val color = when {
                i < setNo -> MaterialTheme.colorScheme.primaryContainer
                i == setNo -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ExerciseTimerDisplay(
    remainingSeconds: Int,
    durationSeconds: Int,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (durationSeconds > 0) remainingSeconds.toFloat() / durationSeconds else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "timer_progress"
    )
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = if (minutes > 0) "%d:%02d".format(minutes, seconds) else "$seconds"

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SetValueColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DoneButton(label: String = "Done", onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}
