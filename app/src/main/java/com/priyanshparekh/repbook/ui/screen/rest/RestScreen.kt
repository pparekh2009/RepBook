package com.priyanshparekh.repbook.ui.screen.rest

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RestScreen(
    viewModel: RestViewModel,
    onNavigateToExercise: (workoutId: Long, exerciseId: Long, setNo: Int) -> Unit,
    onNavigateToFinish: (workoutId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is RestNavEvent.ToExercise -> onNavigateToExercise(event.workoutId, event.exerciseId, event.setNo)
                is RestNavEvent.ToFinish -> onNavigateToFinish(event.workoutId)
            }
        }
    }

    val nextLabel = if (uiState.nextExerciseName.isEmpty()) "Finish" else "Next"

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NextButton(
                    label = nextLabel,
                    enabled = uiState.isNextButtonEnabled,
                    onClick = viewModel::onNextClick
                )
                if (!uiState.isNextButtonEnabled) {
                    Text(
                        text = "Available when timer finishes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                SkipRestButton(onClick = viewModel::onSkipRestClick)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rest",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            TimerDisplay(
                remainingTimeSec = uiState.remainingTimeSec,
                restDurationSec = uiState.restDurationSec
            )
            Spacer(Modifier.height(32.dp))
            NextExercisePreview(
                nextExerciseName = uiState.nextExerciseName,
                nextSetNo = uiState.nextSetNo,
                nextTotalSets = uiState.nextTotalSets
            )
        }
    }
}

@Composable
fun TimerDisplay(
    remainingTimeSec: Int,
    restDurationSec: Int,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (restDurationSec > 0) remainingTimeSec.toFloat() / restDurationSec else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "rest_timer_progress"
    )
    val minutes = remainingTimeSec / 60
    val seconds = remainingTimeSec % 60
    val timeText = if (minutes > 0) "%d:%02d".format(minutes, seconds) else "$seconds"

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.tertiary,
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
fun NextExercisePreview(
    nextExerciseName: String,
    nextSetNo: Int,
    nextTotalSets: Int,
    modifier: Modifier = Modifier
) {
    val label = if (nextExerciseName.isEmpty()) {
        "Ready to finish!"
    } else {
        "$nextExerciseName — Set $nextSetNo of $nextTotalSets"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Up Next",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NextButton(
    label: String = "Next",
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "next_button_container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "next_button_content"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}

@Composable
fun SkipRestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Skip Rest")
    }
}
