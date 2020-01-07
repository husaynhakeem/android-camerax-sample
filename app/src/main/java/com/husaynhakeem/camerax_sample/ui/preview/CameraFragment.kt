package com.husaynhakeem.camerax_sample.ui.preview

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.TextureViewMeteringPointFactory
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.common.util.concurrent.ListenableFuture
import com.husaynhakeem.camerax_sample.R
import com.husaynhakeem.camerax_sample.ui.gallery.GalleryFragment
import com.husaynhakeem.camerax_sample.ui.preview.FileCreator.JPEG_FORMAT
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    private lateinit var processCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processCameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processCameraProviderFuture.addListener(Runnable {
            processCameraProvider = processCameraProviderFuture.get()
            viewFinder.post { setupCamera() }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::processCameraProvider.isInitialized) {
            processCameraProvider.unbindAll()
        }
    }

    private fun setupCamera() {
        processCameraProvider.unbindAll()
        val camera = processCameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                buildPreviewUseCase(),
                buildImageCaptureUseCase(),
                buildImageAnalysisUseCase())
        setupTapForFocus(camera.cameraControl)
    }

    private fun buildPreviewUseCase(): Preview {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val preview = Preview.Builder()
                .setTargetRotation(display.rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .build()
                .apply {
                    previewSurfaceProvider = viewFinder.previewSurfaceProvider
                }
        preview.previewSurfaceProvider = viewFinder.previewSurfaceProvider
        return preview
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val capture = ImageCapture.Builder()
                .setTargetRotation(display.rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

        val executor = Executors.newSingleThreadExecutor()
        cameraCaptureImageButton.setOnClickListener {
            capture.takePicture(
                    FileCreator.createTempFile(JPEG_FORMAT),
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(file: File) {
                            val arguments = GalleryFragment.arguments(file.absolutePath)
                            Navigation.findNavController(requireActivity(), R.id.mainContent)
                                    .navigate(R.id.imagePreviewFragment, arguments)
                        }

                        override fun onError(imageCaptureError: Int, message: String, cause: Throwable?) {
                            Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                            Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                        }
                    })
        }
        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val display = viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val analysis = ImageAnalysis.Builder()
                .setTargetRotation(display.rotation)
                .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .setImageQueueDepth(10)
                .build()
        analysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                ImageAnalysis.Analyzer { imageProxy ->
                    Log.d("CameraFragment", "Image analysis result $imageProxy")
                    imageProxy.close()
                })
        return analysis
    }

    private fun setupTapForFocus(cameraControl: CameraControl) {
        viewFinder.setOnTouchListener { _, event ->
            if (event.action != MotionEvent.ACTION_UP) {
                return@setOnTouchListener true
            }

            val textureView = viewFinder.getChildAt(0) as? TextureView
                    ?: return@setOnTouchListener true
            val factory = TextureViewMeteringPointFactory(textureView)

            val point = factory.createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder.from(point).build()
            cameraControl.startFocusAndMetering(action)
            return@setOnTouchListener true
        }
    }
}