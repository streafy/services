package com.streafy.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            coroutineScope.launch {
                var wortItem = params?.dequeueWork()
                while (wortItem != null) {
                    val page = wortItem.intent?.getIntExtra(PAGE, 0)
                    for (i in 0 until 5) {
                        delay(1000)
                        Log.d("MyJobService", "Timer: $i $page")
                    }
                    params?.completeWork(wortItem)
                    wortItem = params?.dequeueWork()
                }
                jobFinished(params, false)
            }
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
        private const val PAGE = "page"

        fun newIntent(page: Int): Intent =
            Intent().apply {
                putExtra(PAGE, page)
            }
    }
}