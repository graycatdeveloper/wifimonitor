package com.example.wifimonitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import com.example.wifimonitor.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WifiMonitorService : Service(), CoroutineScope by CoroutineScope(SupervisorJob()) {

    private lateinit var notificationManager: NotificationManager
    private lateinit var connectivityManager: ConnectivityManager

    private var isStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        connectivityManager = getSystemService(ConnectivityManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ))
        }

        val request = NetworkRequest.Builder()
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "start" -> {
                if (!isStarted) {
                    isStarted = true
                    val value = connectivityManager.activeNetwork?.let { network ->
                        connectivityManager.getNetworkCapabilities(network)?.parse()
                    } ?: getString(R.string.no_connection)
                    sendBroadcast(value)
                    startAsForeground(createNotification(value))
                }
            }
            "delete_notification" -> {
                connectivityManager.activeNetwork?.let { network ->
                    connectivityManager.getNetworkCapabilities(network)?.let {
                        updateNotification(it.parse())
                    }
                }
            }
            "stop" -> {
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    private fun createNotification(title: String, sound: Boolean = true) : Notification {
        val offPendingIntent = PendingIntentCompat.getService(this, 0,
            Intent(this, this::class.java).setAction("stop"), 0, false)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setAutoCancel(false)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(android.R.drawable.ic_lock_power_off, "Выключить", offPendingIntent)
        if (!sound) {
            notificationBuilder.setSilent(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationBuilder.setDeleteIntent(PendingIntentCompat.getService(this, 0,
                Intent(this, this::class.java).setAction("delete_notification"), 0, false))
        }
        return notificationBuilder.build()
    }

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private val networkCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            override fun onAvailable(network: Network) {
                connectivityManager.getNetworkCapabilities(network)?.let {
                    val value = it.parse()
                    sendBroadcast(value)
                    updateNotification(value)
                }
            }
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val value = networkCapabilities.parse()
                sendBroadcast(value)
                updateNotification(value)
            }
            override fun onLost(network: Network) {
                val value = getString(R.string.no_connection)
                sendBroadcast(value)
                notificationManager.notify(NOTIFICATION_ID, createNotification(value, false))
            }
        }
    else
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.getNetworkCapabilities(network)?.let {
                    val value = it.parse()
                    sendBroadcast(value)
                    updateNotification(value)
                }
            }
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val value = networkCapabilities.parse()
                sendBroadcast(value)
                updateNotification(value)
            }
            override fun onLost(network: Network) {
                val value = getString(R.string.no_connection)
                sendBroadcast(value)
                notificationManager.notify(NOTIFICATION_ID, createNotification(value, false))
            }
        }

    private fun NetworkCapabilities.parse() = getString(when {
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> R.string.transport_cellular
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> R.string.transport_wifi
        else -> R.string.transport_not_defined
    })

    private fun sendBroadcast(text: String) =
        sendBroadcast(Intent(ACTION_CONNECTION_STATE)
        .putExtra(EXTRA_CONNECTION_STATE, text))

    private fun updateNotification(text: String) {
        notificationManager.notify(NOTIFICATION_ID, createNotification(text, false))
    }

    companion object {
        const val ACTION_CONNECTION_STATE = "ACTION_CONNECTION_STATE"
        const val EXTRA_CONNECTION_STATE = "state"
        private const val NOTIFICATION_ID = 1234
        private const val NOTIFICATION_CHANNEL_ID = "wifi_monitor"
        private const val NOTIFICATION_CHANNEL_NAME = "Wifi Monitor"
        fun start(context: Context) = context.startService(
            Intent(context, WifiMonitorService::class.java).setAction("start"))
    }

}