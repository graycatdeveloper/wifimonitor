package com.example.wifimonitor

import android.app.Application
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import java.util.concurrent.TimeUnit

class App : Application() {

    private lateinit var wakeLock: WakeLock

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:wake_lock")
        wakeLock.acquire(TimeUnit.MINUTES.toMillis(10))
    }

    override fun onTerminate() {
        if (wakeLock.isHeld) wakeLock.release()
        super.onTerminate()
    }

}