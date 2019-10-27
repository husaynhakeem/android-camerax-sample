package com.husaynhakeem.camerax_sample.ui

import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedListener
import androidx.camera.core.Preview
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.husaynhakeem.camerax_sample.R
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        CameraX.unbindAll()
        CameraX.bindToLifecycle(
            this,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase()
        )
    }

    private fun buildPreviewUseCase(): Preview {
        val preview = Preview(UsecaseConfigBuilder.buildPreviewConfig(root.display))
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
        val capture = ImageCapture(UsecaseConfigBuilder.buildImageCaptureConfig(root.display))
        cameraCaptureImageButton.setOnClickListener {
            val fileName = System.currentTimeMillis().toString()
            val fileFormat = ".jpg"
            val imageFile = createTempFile(fileName, fileFormat)
            capture.takePicture(
                imageFile,
                Executors.newSingleThreadExecutor(),
                object : OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        val arguments = ImagePreviewFragment.arguments(file.absolutePath)
                        Navigation.findNavController(requireActivity(), R.id.mainContent)
                            .navigate(R.id.imagePreviewFragment, arguments)
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG)
                            .show()
                        Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                    }
                })
        }
        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val analysis = ImageAnalysis(UsecaseConfigBuilder.buildImageAnalysisConfig(root.display))
        analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { image, rotationDegrees ->
                Log.d(
                    "CameraFragment",
                    "Image analysis: $image - Rotation degrees: $rotationDegrees"
                )
            })
        return analysis
    }
}