package com.priyanshparekh.repbook.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(historyRepository: WorkoutHistoryRepository) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = historyRepository.getAll()
        .map { HistoryUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HistoryUiState())

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HistoryViewModel(container.historyRepository) as T
            }
    }
}
