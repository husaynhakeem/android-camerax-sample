package com.husaynhakeem.camerax_sample.ui

import android.util.DisplayMetrics
import android.util.Size
import android.view.Display
import androidx.camera.core.*

object UsecaseConfigBuilder {

    fun buildPreviewConfig(display: Display): PreviewConfig {
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        return PreviewConfig.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()
    }

    fun buildImageCaptureConfig(display: Display): ImageCaptureConfig {
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        return ImageCaptureConfig.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setFlashMode(FlashMode.AUTO)
            .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
            .build()
    }

    fun buildImageAnalysisConfig(display: Display): ImageAnalysisConfig {
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        return ImageAnalysisConfig.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .setImageQueueDepth(1)
            .build()
    }
}