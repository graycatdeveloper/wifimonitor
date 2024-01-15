package com.example.wifimonitor.receiver

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.wifimonitor.StartServiceActivity
import com.example.wifimonitor.utils.isStartOnBootComplete

class StartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, intent.action, Toast.LENGTH_SHORT).show()
        Log.i(javaClass.simpleName, intent.action ?: "")
        val isStartOnBootComplete = context.isStartOnBootComplete
        Toast.makeText(context, "isStartOnBootComplete: $isStartOnBootComplete", Toast.LENGTH_SHORT).show()
        Log.i(javaClass.simpleName, "isStartOnBootComplete: $isStartOnBootComplete")
        if (!isStartOnBootComplete) return
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                StartServiceActivity.start(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                val keyguardManager = context.getSystemService(KeyguardManager::class.java)
                if (!keyguardManager.isDeviceSecure) {
                    StartServiceActivity.start(context)
                }
            }
        }
    }

}