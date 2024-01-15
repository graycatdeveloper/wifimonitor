package com.example.wifimonitor

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.wifimonitor.service.WifiMonitorService

class StartServiceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "StartServiceActivity", Toast.LENGTH_SHORT).show()
        Log.i(javaClass.simpleName, "StartServiceActivity")
        WifiMonitorService.start(this)
        finish()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, StartServiceActivity::class.java)
                .setPackage(context.packageName)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val options = ActivityOptions.makeBasic()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                options.setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            }
            try {
                PendingIntent.getActivity(context, 0,
                    intent, FLAG_IMMUTABLE, options.toBundle()).send()
            } catch (e: PendingIntent.CanceledException) {
                Log.e("StartServiceActivity", e.message, e)
            }
        }
    }

}