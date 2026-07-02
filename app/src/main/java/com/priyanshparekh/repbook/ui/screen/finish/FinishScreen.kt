package com.priyanshparekh.repbook.ui.screen.finish

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.priyanshparekh.repbook.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun FinishScreen(
    viewModel: FinishViewModel,
    onNavigateToHome: () -> Unit
) {
    var textVisible by remember { mutableStateOf(false) }
    var animationComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { onNavigateToHome() }
    }

    LaunchedEffect(Unit) {
        delay(400)
        textVisible = true
    }

    // Auto-navigate 1500ms after animation finishes; Done button is the immediate fallback.
    LaunchedEffect(animationComplete) {
        if (animationComplete) {
            delay(1500)
            viewModel.onDoneClick()
        }
    }

    Scaffold(
        bottomBar = {
            DoneButton(
                onClick = viewModel::onDoneClick,
                modifier = Modifier.padding(16.dp)
            )
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
            CompletionAnimation(
                onComplete = { animationComplete = true }
            )
            Spacer(Modifier.height(24.dp))
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
            ) {
                WorkoutFinishedText()
            }
        }
    }
}

@Composable
fun CompletionAnimation(
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.checkmark_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(composition) {
        if (composition == null) return@LaunchedEffect
        snapshotFlow { progress }.first { it >= 1f }
        onComplete()
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
            .fillMaxWidth(0.6f)
            .aspectRatio(1f)
    )
}

@Composable
fun WorkoutFinishedText(modifier: Modifier = Modifier) {
    Text(
        text = "Workout Finished!",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun DoneButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Done")
    }
}
