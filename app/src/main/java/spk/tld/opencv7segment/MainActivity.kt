package spk.tld.opencv7segment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import spk.tld.opencv7segment.databinding.ActivityMainBinding
import spk.tld.opencv7segment.preprocessing.SegmentProcessor

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding
    private lateinit var segmentProcessor: SegmentProcessor
    private var mRgba: Mat? = null
    private var mGray: Mat? = null

    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initOpenCV()
        initPermission()
        segmentProcessor = SegmentProcessor()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        } else {
            setupCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                binding.opencvCameraView.enableView()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.opencvCameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.opencvCameraView.disableView()
    }

    private fun setupCamera() {
        try {
            binding.opencvCameraView.apply {
                setCameraPermissionGranted()
                visibility = SurfaceView.VISIBLE
                setCvCameraViewListener(this@MainActivity)
                setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
                enableFpsMeter()
                enableView()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up camera: ${e.message}")
        }
    }

    private fun initOpenCV() {
        if (!OpenCVLoader.initLocal()) {
            Toast.makeText(this, "OpenCV initialization failed", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d(TAG, "Camera view started: $width x $height")
        mRgba = Mat()
        mGray = Mat()
    }

    override fun onCameraViewStopped() {
        mRgba?.release()
        mGray?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame?.rgba()
        mGray = inputFrame?.gray()

        try {
            mGray?.let { gray ->
                mRgba?.let { rgba ->
                    segmentProcessor.processFrame(gray.clone(), rgba)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame: ${e.message}")
        }

        return mRgba ?: Mat()
    }


}