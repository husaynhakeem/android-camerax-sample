package com.husaynhakeem.camerax_sample.ui

import android.graphics.Matrix
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.fragment.app.Fragment
import com.husaynhakeem.camerax_sample.R
import kotlinx.android.synthetic.main.fragment_camera.*


class CameraFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraTextureView.post { setupCameraUsecases() }
    }

    private fun setupCameraUsecases() {
        val metrics = DisplayMetrics().also { cameraTextureView.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        val screenRotation = cameraTextureView.display.rotation

        val previewBuilder = PreviewConfig.Builder()
            .setLensFacing(CameraX.LensFacing.BACK)
            .setTargetResolution(screenSize)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(screenRotation)
            .build()

        val preview = Preview(previewBuilder)
        preview.setOnPreviewOutputUpdateListener {
            cameraTextureView.surfaceTexture = it.surfaceTexture
            val matrix = Matrix()

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
            matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

            // Finally, apply transformations to our TextureView
            cameraTextureView.setTransform(matrix)
        }

        CameraX.bindToLifecycle(this, preview)
    }
}