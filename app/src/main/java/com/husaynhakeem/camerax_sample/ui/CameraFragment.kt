package com.husaynhakeem.camerax_sample.ui

import android.graphics.Matrix
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.husaynhakeem.camerax_sample.R
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File


class CameraFragment : Fragment() {

    private lateinit var config: CameraConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraTextureView.post { setupCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraX.unbindAll()
    }

    private fun setupCamera() {
        val metrics = DisplayMetrics().also { cameraTextureView.display.getRealMetrics(it) }
        config = CameraConfiguration(
            aspectRatio = Rational(metrics.widthPixels, metrics.heightPixels),
            rotation = cameraTextureView.display.rotation,
            resolution = Size(metrics.widthPixels, metrics.heightPixels)
        )

        CameraX.unbindAll()
        CameraX.bindToLifecycle(
            this,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase()
        )
    }

    private fun buildPreviewUseCase(): Preview {
        val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .setTargetRotation(config.rotation)
            .setTargetResolution(config.resolution)
            .setLensFacing(config.lensFacing)
            .build()
        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = cameraTextureView.parent as ViewGroup
            parent.removeView(cameraTextureView)
            parent.addView(cameraTextureView, 0)
            cameraTextureView.surfaceTexture = previewOutput.surfaceTexture

            // Compute the center of the view finder
            val centerX = cameraTextureView.width / 2f
            val centerY = cameraTextureView.height / 2f

            // Correct preview output to account for display rotation
            val rotationDegrees = when (cameraTextureView.display.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> return@setOnPreviewOutputUpdateListener
            }

            val matrix = Matrix()
            matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

            // Finally, apply transformations to our TextureView
            cameraTextureView.setTransform(matrix)
        }

        return preview
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val captureConfig = ImageCaptureConfig.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .setTargetRotation(config.rotation)
            .setTargetResolution(config.resolution)
            .setFlashMode(config.flashMode)
            .setCaptureMode(config.captureMode)
            .build()
        val capture = ImageCapture(captureConfig)

        cameraCaptureImageButton.setOnClickListener {
            val fileName = System.currentTimeMillis().toString()
            val fileFormat = ".jpg"
            val imageFile = createTempFile(fileName, fileFormat)
            capture.takePicture(imageFile, object : ImageCapture.OnImageSavedListener {
                override fun onImageSaved(file: File) {
                    val arguments = ImagePreviewFragment.arguments(file.absolutePath)
                    Navigation.findNavController(requireActivity(), R.id.mainContent)
                        .navigate(R.id.imagePreviewFragment, arguments)
                }

                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                    Log.e("CameraFragment", "Capture error $useCaseError: $message", cause)
                }
            })
        }

        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val analysisConfig = ImageAnalysisConfig.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .setTargetRotation(config.rotation)
            .setTargetResolution(config.resolution)
            .setImageReaderMode(config.readerMode)
            .setImageQueueDepth(config.queueDepth)
            .build()
        val analysis = ImageAnalysis(analysisConfig)

        analysis.setAnalyzer { image, rotationDegrees ->
            Log.d("CameraFragment", "Image analysis: $image - Rotation degrees: $rotationDegrees")
        }

        return analysis
    }
}