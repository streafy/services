package com.streafy.services

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
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

    private val requestPermissionLauncherForeground =
        registerForActivityResult(RequestPermission(), ::onGotNotificationPermissionResultForeground)

    private val requestPermissionLauncherIntent =
        registerForActivityResult(RequestPermission(), ::onGotNotificationPermissionResultIntent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.simpleService.setOnClickListener {
            stopService(MyForegroundService.newIntent(this))
            startService(MyService.newIntent(this))
        }
        binding.foregroundService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncherForeground.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                stopService(MyService.newIntent(this))
                startForegroundService()
            }
        }
        binding.intentService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncherIntent.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startIntentService()
            }
        }
        binding.jobScheduler.setOnClickListener {
            val componentName = ComponentName(this, MyJobService::class.java)
            val jobInfo = JobInfo.Builder(MyJobService.JOB_ID, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build()
            val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(jobInfo)
        }
    }

    private fun startIntentService() {
        ContextCompat.startForegroundService(this, MyIntentService.newIntent(this))
    }

    private fun startForegroundService() {
        ContextCompat.startForegroundService(this, MyForegroundService.newIntent(this))
    }

    private fun onGotNotificationPermissionResultForeground(isGranted: Boolean) {
        if (isGranted) {
            startForegroundService()
        } else {
            explainPermissionNecessityToUser()
        }
    }

    private fun onGotNotificationPermissionResultIntent(isGranted: Boolean) {
        if (isGranted) {
            startIntentService()
        } else {
            explainPermissionNecessityToUser()
        }
    }

    private fun explainPermissionNecessityToUser() {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            askUserToOpenAppSettingsForPermission()
        } else {
            showNotificationPermissionNeededToast()
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