package com.example.ailauncher.data

import android.content.Context
import android.util.Log
import com.example.ailauncher.model.ModelDownloadState
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModelDownloader(private val context: Context) {

    private val modelUrl =
        "https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilebert/uncased_L-24_H-128_B-512_A-2_F-4_OPT.tflite"
    private val modelFileName = "mobilebert_uncased.tflite"

    private val destination: File
        get() = File(context.filesDir, modelFileName)

    suspend fun ensureModelAvailable(onProgress: (ModelDownloadState.Downloading) -> Unit): File {
        val currentFile = destination
        if (currentFile.exists()) {
            return currentFile
        }
        return withContext(Dispatchers.IO) {
            downloadModel(onProgress)
            destination
        }
    }

    fun currentModelFile(): File = destination

    private fun downloadModel(onProgress: (ModelDownloadState.Downloading) -> Unit) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(modelUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Failed to download model: HTTP ${connection.responseCode}")
            }

            val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: -1L
            val input = BufferedInputStream(connection.inputStream)
            val outputFile = destination
            FileOutputStream(outputFile).use { outputStream ->
                val data = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int
                var downloaded = 0L
                while (input.read(data).also { bytesRead = it } != -1) {
                    outputStream.write(data, 0, bytesRead)
                    downloaded += bytesRead
                    val percent = if (totalBytes > 0) {
                        ((downloaded * 100) / totalBytes).toInt()
                    } else {
                        0
                    }
                    onProgress(
                        ModelDownloadState.Downloading(
                            progress = percent,
                            message = "Downloading MobileBERT for on-device personalization…"
                        )
                    )
                }
                outputStream.flush()
            }
        } catch (e: Exception) {
            Log.e("ModelDownloader", "Model download failed", e)
            throw e
        } finally {
            connection?.disconnect()
        }
    }
}
