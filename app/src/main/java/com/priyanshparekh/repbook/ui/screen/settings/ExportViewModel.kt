package com.priyanshparekh.repbook.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.export.ExportSerializer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportUiState(
    val isExporting: Boolean = false,
    val error: String? = null
)

class ExportViewModel(private val exportSerializer: ExportSerializer) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _exportJson = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val exportJson: SharedFlow<String> = _exportJson.asSharedFlow()

    fun startExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }
            try {
                val json = exportSerializer.serialize()
                _exportJson.emit(json)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Export failed") }
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ExportViewModel(container.exportSerializer) as T
            }
    }
}
