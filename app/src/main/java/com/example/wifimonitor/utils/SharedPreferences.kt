package com.example.wifimonitor.utils

import android.content.Context
import android.os.Build

private const val START_ON_BOOT_COMPLETE = "startOnBootComplete"

private val Context.sharedPreferences get() =
    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        createDeviceProtectedStorageContext()
    else this).getSharedPreferences(packageName, Context.MODE_PRIVATE)

fun Context.startOnBootComplete(value: Boolean) = sharedPreferences?.edit()?.also {
    it.putBoolean(START_ON_BOOT_COMPLETE, value)
}?.commit()

val Context.isStartOnBootComplete get() = sharedPreferences.getBoolean(START_ON_BOOT_COMPLETE, false)