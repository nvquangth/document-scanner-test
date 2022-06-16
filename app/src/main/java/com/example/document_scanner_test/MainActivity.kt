package com.example.document_scanner_test

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.SurfaceView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.document_scanner_test.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),

            ) { isGranted: Boolean ->
            when {
                isGranted -> {
                    initCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    showPermissionDeniedDialog()
                }
                else -> {
                    showMandatoryPermissionsNeedDialog()
                }
            }
        }

    private val openSettingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkCameraPermission()
    }

    private val loaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    binding.cameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCameraPermission()

        if (OpenCVLoader.initDebug()) {
            loaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS)
            Log.d("quangnv", "OpenCv configured successfully")
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback)
            Log.d("quangnv", "OpenCv doesn’t configured successfully")
        }

        binding.cameraView.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {
            }

            override fun onCameraViewStopped() {
            }

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
                return inputFrame!!.rgba()
            }
        })
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                initCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionDeniedDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setMessage(getString(R.string.permission_location_access_required))
            setPositiveButton(R.string.allow_permission) { dialog, _ ->
                dialog.dismiss()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            setNegativeButton(R.string.deny_permission) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        }.show()
    }

    private fun showMandatoryPermissionsNeedDialog() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setMessage(getString(R.string.mandatory_permission_location_access_required))
            setPositiveButton(R.string.allow_permission) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                openSettingLauncher.launch(intent)
            }
            setNegativeButton(R.string.deny_permission) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        }.show()
    }

    private fun initCamera() {
        binding.cameraView.apply {
            visibility = SurfaceView.VISIBLE
            setCameraPermissionGranted()
        }
    }
}