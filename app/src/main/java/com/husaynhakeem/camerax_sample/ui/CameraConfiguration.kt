package com.husaynhakeem.camerax_sample.ui

import android.util.Rational
import android.util.Size
import androidx.camera.core.CameraX
import androidx.camera.core.FlashMode
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture


data class CameraConfiguration(
    val aspectRatio: Rational,
    val rotation: Int = 0,
    val resolution: Size,

    // Preview
    val lensFacing: CameraX.LensFacing = CameraX.LensFacing.BACK,

    // Image capture
    val flashMode: FlashMode = FlashMode.OFF,
    val captureMode: ImageCapture.CaptureMode = ImageCapture.CaptureMode.MIN_LATENCY,

    // Image analysis
    val readerMode: ImageAnalysis.ImageReaderMode = ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE,
    val queueDepth: Int = 1
)