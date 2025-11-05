package com.example.labweek08

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.labweek08.worker.FirstWorker
import com.example.labweek08.worker.SecondWorker

class MainActivity : AppCompatActivity() {

    private lateinit var workManager: WorkManager
    private lateinit var firstRequest: OneTimeWorkRequest
    private lateinit var secondRequest: OneTimeWorkRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Initialize WorkManager
        workManager = WorkManager.getInstance(this)
        firstRequest = OneTimeWorkRequestBuilder<FirstWorker>().build()
        secondRequest = OneTimeWorkRequestBuilder<SecondWorker>().build()

        // Chain the two workers
        workManager.beginWith(firstRequest).then(secondRequest).enqueue()

        // Observe SecondWorker completion
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this) { info ->
            if (info != null && info.state.isFinished) {
                showResult("Second worker process is done")
                launchNotificationService()
            }
        }
    }

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun launchNotificationService() {
        // Observe LiveData from NotificationService
        NotificationService.trackingCompletion.observe(this, Observer { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
        })

        // Start ForegroundService for notification countdown
        val serviceIntent = Intent(this, NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }

        ContextCompat.startForegroundService(this, serviceIntent)
    }

    companion object {
        const val EXTRA_ID = "Id"
    }
}
