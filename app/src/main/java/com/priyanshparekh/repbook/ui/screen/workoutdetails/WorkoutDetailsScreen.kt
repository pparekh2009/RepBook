package com.priyanshparekh.repbook.ui.screen.workoutdetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.priyanshparekh.repbook.domain.model.Exercise
import com.priyanshparekh.repbook.domain.model.WorkoutSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    viewModel: WorkoutDetailsViewModel,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val exerciseCount = uiState.exercises.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.workout?.name ?: "")
                        Text(
                            text = "$exerciseCount exercise${if (exerciseCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openBottomSheet) {
                Icon(Icons.Default.Add, contentDescription = "Add exercises")
            }
        }
    ) { innerPadding ->
        if (uiState.exercises.isEmpty()) {
            WorkoutDetailsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            ExerciseList(
                exercises = uiState.exercises,
                onEdit = { exercise, sets -> viewModel.showEditDialog(exercise, sets) },
                onRemove = viewModel::showRemoveDialog,
                contentPadding = innerPadding
            )
        }
    }

    if (uiState.isBottomSheetOpen) {
        AddExercisesBottomSheet(
            allExercises = ExerciseData.exercises,
            selectedIds = uiState.selectedIds,
            onToggle = viewModel::toggleSelection,
            onSave = viewModel::saveSelectedExercises,
            onDismiss = viewModel::closeBottomSheet
        )
    }

    when (val dialog = uiState.dialogState) {
        WorkoutDetailsDialogState.None -> {}
        is WorkoutDetailsDialogState.Edit -> EditExerciseBottomSheet(
            exercise = dialog.exercise,
            sets = dialog.sets,
            onConfirm = viewModel::updateSets,
            onDismiss = viewModel::dismissDialog
        )
        is WorkoutDetailsDialogState.RemoveConfirm -> RemoveExerciseDialog(
            exercise = dialog.exercise,
            onConfirm = { viewModel.removeExercise(dialog.exercise) },
            onDismiss = viewModel::dismissDialog
        )
    }
}

@Composable
fun ExerciseList(
    exercises: List<ExerciseWithSets>,
    onEdit: (Exercise, List<WorkoutSet>) -> Unit,
    onRemove: (Exercise) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(exercises, key = { it.exercise.id }) { exerciseWithSets ->
            ExerciseCard(
                exerciseWithSets = exerciseWithSets,
                onEdit = onEdit,
                onRemove = onRemove
            )
        }
    }
}

@Composable
fun ExerciseCard(
    exerciseWithSets: ExerciseWithSets,
    onEdit: (Exercise, List<WorkoutSet>) -> Unit,
    onRemove: (Exercise) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val exercise = exerciseWithSets.exercise

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // end = 48.dp reserves space for the 48dp-wide IconButton overlay
                    .padding(start = 16.dp, end = 48.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium
                )
                exerciseWithSets.sets.forEach { set ->
                    SetSummary(set = set, isTimeBased = exercise.isTimeBased)
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit(exercise, exerciseWithSets.sets)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove") },
                        onClick = {
                            menuExpanded = false
                            onRemove(exercise)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SetSummary(set: WorkoutSet, isTimeBased: Boolean) {
    val weightDisplay = if (set.weight % 1f == 0f) "${set.weight.toInt()} kg" else "${set.weight} kg"
    val effortDisplay = if (isTimeBased) "${set.durationSeconds ?: 0} secs" else "× ${set.reps} reps"
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${set.setNo}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = weightDisplay,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = effortDisplay,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WorkoutDetailsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No exercises yet",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap + to add exercises to this workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExercisesBottomSheet(
    allExercises: List<ExerciseDataItem>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "Add Exercises",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider()
        if (allExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exercises in library yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(allExercises, key = { it.id }) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(exercise.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = exercise.id in selectedIds,
                            onCheckedChange = { onToggle(exercise.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        SelectionActionBar(
            count = selectedIds.size,
            onSave = onSave
        )
    }
}

@Composable
fun SelectionActionBar(
    count: Int,
    onSave: () -> Unit
) {
    if (count == 0) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(onClick = onSave) {
            Text("Add $count exercise${if (count > 1) "s" else ""}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseBottomSheet(
    exercise: Exercise,
    sets: List<WorkoutSet>,
    onConfirm: (List<WorkoutSet>) -> Unit,
    onDismiss: () -> Unit
) {
    val isTimeBased = exercise.isTimeBased
    var editedWeights by remember { mutableStateOf(sets.map { it.weight.toString() }) }
    var editedReps by remember { mutableStateOf(sets.map { it.reps.toString() }) }
    var editedDurations by remember { mutableStateOf(sets.map { (it.durationSeconds ?: 0).toString() }) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(sets) { index, set ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Set ${set.setNo}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(44.dp)
                    )
                    OutlinedTextField(
                        value = editedWeights[index],
                        onValueChange = { value ->
                            editedWeights = editedWeights.toMutableList().also { it[index] = value }
                        },
                        label = { Text("kg") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    if (isTimeBased) {
                        OutlinedTextField(
                            value = editedDurations[index],
                            onValueChange = { value ->
                                editedDurations = editedDurations.toMutableList().also { it[index] = value }
                            },
                            label = { Text("secs") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    } else {
                        OutlinedTextField(
                            value = editedReps[index],
                            onValueChange = { value ->
                                editedReps = editedReps.toMutableList().also { it[index] = value }
                            },
                            label = { Text("reps") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Button(
                onClick = {
                    val updated = sets.mapIndexed { index, set ->
                        if (isTimeBased) {
                            set.copy(
                                weight = editedWeights[index].toFloatOrNull() ?: set.weight,
                                reps = 0,
                                durationSeconds = editedDurations[index].toIntOrNull() ?: set.durationSeconds
                            )
                        } else {
                            set.copy(
                                weight = editedWeights[index].toFloatOrNull() ?: set.weight,
                                reps = editedReps[index].toIntOrNull() ?: set.reps
                            )
                        }
                    }
                    onConfirm(updated)
                }
            ) { Text("Save") }
        }
    }
}

@Composable
fun RemoveExerciseDialog(
    exercise: Exercise,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Exercise") },
        text = {
            Text("Remove \"${exercise.name}\"? This will also delete all its sets.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
