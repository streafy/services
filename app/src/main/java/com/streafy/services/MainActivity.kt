package com.streafy.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.streafy.services.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission(), ::onGotNotificationPermissionResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.simpleService.setOnClickListener {
            startService(MyService.newIntent(this))
        }
        binding.foregroundService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startForegroundService()
            }
        }
    }

    private fun startForegroundService() {
        ContextCompat.startForegroundService(this, MyForegroundService.newIntent(this))
    }

    private fun onGotNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            startForegroundService()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                askUserToOpenAppSettingsForPermission()
            } else {
                showNotificationPermissionNeededToast()
            }
        }
    }

    private fun askUserToOpenAppSettingsForPermission() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        if (packageManager.resolveActivity(
                appSettingsIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) == null
        ) {
            Toast.makeText(this, R.string.permission_denied_forever, Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton("Open") { _, _ ->
                    startActivity(appSettingsIntent)
                }
                .create()
                .show()
        }
    }

    private fun showNotificationPermissionNeededToast() {
        Toast.makeText(
            this,
            R.string.permission_required,
            Toast.LENGTH_SHORT
        ).show()
    }
}