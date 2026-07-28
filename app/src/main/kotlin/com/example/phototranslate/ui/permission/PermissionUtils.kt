package com.example.phototranslate.ui.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionUtils - Centralized permission handling for Photo Translate App.
 * Encapsulates permission checking and request logic for camera and storage.
 * Follows Android 13+ permission model with READ_MEDIA_IMAGES.
 */
object PermissionUtils {

    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val READ_IMAGES_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES

    // Permission request codes
    private const val REQUEST_CODE_CAMERA = 1001
    private const val REQUEST_CODE_STORAGE = 1002

    /**
     * Check if the given permission is granted.
     */
    fun hasPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if both camera and storage permissions are granted.
     */
    fun hasAllPermissions(activity: Activity): Boolean {
        return hasPermission(activity, CAMERA_PERMISSION) &&
               hasPermission(activity, READ_IMAGES_PERMISSION)
    }

    /**
     * Request camera permission at runtime.
     */
    fun requestCameraPermission(activity: Activity, onRequestGranted: () -> Unit) {
        if (hasPermission(activity, CAMERA_PERMISSION)) {
            onRequestGranted()
            return
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                CAMERA_PERMISSION
            )) {
            showPermissionRationale(activity, CAMERA_PERMISSION, onRequestGranted)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(CAMERA_PERMISSION),
                REQUEST_CODE_CAMERA
            )
        }
    }

    /**
     * Request storage permissions for saving images.
     */
    fun requestStoragePermission(activity: Activity, onRequestGranted: () -> Unit) {
        if (hasPermission(activity, READ_IMAGES_PERMISSION)) {
            onRequestGranted()
            return
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(READ_IMAGES_PERMISSION),
            REQUEST_CODE_STORAGE
        )
    }

    /**
     * Handle permission request results in Activity/Fragment.
     */
    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onRequestGranted: () -> Unit,
        onPermissionDenied: (String) -> Unit
    ) {
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onRequestGranted()
                } else {
                    onPermissionDenied(CAMERA_PERMISSION)
                }
            }
            REQUEST_CODE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onRequestGranted()
                } else {
                    onPermissionDenied(READ_IMAGES_PERMISSION)
                }
            }
        }
    }

    /**
     * Check if permission was permanently denied.
     */
    fun isPermanentlyDenied(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) == false &&
               !hasPermission(activity, permission)
    }

    private fun showPermissionRationale(
        activity: Activity,
        permission: String,
        callback: () -> Unit
        ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            if (permission == CAMERA_PERMISSION) REQUEST_CODE_CAMERA else REQUEST_CODE_STORAGE
        )
    }
}
