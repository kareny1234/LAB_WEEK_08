package com.example.labweek08.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("ThirdWorker", "ThirdWorker started...")
        Thread.sleep(4000)
        Log.d("ThirdWorker", "ThirdWorker finished work successfully.")
        return Result.success()
    }
}
