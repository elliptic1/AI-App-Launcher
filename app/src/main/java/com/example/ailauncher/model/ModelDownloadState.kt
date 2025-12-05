package com.example.ailauncher.model

sealed class ModelDownloadState {
    data object Idle : ModelDownloadState()
    data class Downloading(val progress: Int, val message: String) : ModelDownloadState()
    data class Ready(val filePath: String) : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}
