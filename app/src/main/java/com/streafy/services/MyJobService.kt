package com.streafy.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyJobService : JobService() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d("MyJobService", "onCreate")
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("MyJobService", "onStartJob")
        coroutineScope.launch {
            for (i in 0 until 100) {
                delay(1000)
                Log.d("MyJobService", "Timer: $i")
            }
            jobFinished(params, true)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("MyJobService", "onStopJob")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        Log.d("MyJobService", "onDestroy")
    }

    companion object {

        const val JOB_ID = 1
    }
}