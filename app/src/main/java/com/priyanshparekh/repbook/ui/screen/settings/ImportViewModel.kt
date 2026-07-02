package com.priyanshparekh.repbook.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.export.ExportData
import com.priyanshparekh.repbook.data.export.ImportService
import com.priyanshparekh.repbook.data.export.ImportValidator
import com.priyanshparekh.repbook.data.export.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class ImportUiState(
    val isPendingConfirmation: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null,
    val importSuccess: Boolean = false
)

class ImportViewModel(
    private val importValidator: ImportValidator,
    private val importService: ImportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private var pendingJson: String? = null

    fun onFileSelected(json: String) {
        pendingJson = json
        _uiState.update { it.copy(isPendingConfirmation = true, error = null, importSuccess = false) }
    }

    fun onDismissWarning() {
        pendingJson = null
        _uiState.update { it.copy(isPendingConfirmation = false) }
    }

    fun onImportConfirmed() {
        val json = pendingJson ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, isPendingConfirmation = false) }
            when (val result = importValidator.validate(json)) {
                is ValidationResult.Invalid -> {
                    _uiState.update { it.copy(isImporting = false, error = result.reason) }
                }
                ValidationResult.Valid -> {
                    try {
                        val data = Json.decodeFromString<ExportData>(json)
                        importService.import(data)
                        _uiState.update { it.copy(isImporting = false, importSuccess = true) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isImporting = false, error = e.message ?: "Import failed") }
                    }
                }
            }
            pendingJson = null
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }

    fun onImportSuccessDismissed() {
        _uiState.update { it.copy(importSuccess = false) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ImportViewModel(container.importValidator, container.importService) as T
            }
    }
}
