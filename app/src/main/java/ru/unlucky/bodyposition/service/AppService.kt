package ru.unlucky.bodyposition.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.koin.android.ext.android.inject
import ru.kpfu.health.body_position.HumanActivityDetection
import ru.kpfu.health.body_position.HumanActivityEnum
import ru.kpfu.health.movement.MovementDetection
import ru.kpfu.health.movement.MovementEnum
import ru.kpfu.health.repo.HealthRepository
import ru.kpfu.health.utils.observeOnIo
import ru.kpfu.health.utils.subscribeOnIo

class AppService: Service() {

    private val notificationManager: NotificationManager by inject()
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()

    private val healthRepository: HealthRepository by inject()
    private val bodyPositionDetection: HumanActivityDetection by inject()
    private val movementDetection: MovementDetection by inject()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        bodyPositionDetection.onStart()
        movementDetection.onStart()
    }

    override fun onDestroy() {
        bodyPositionDetection.onDestroy()
        movementDetection.onDestroy()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        initMovementDetection()
        initBodyPosition()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        stopForeground(FOREGROUND_SERVICE_TYPE)
        serviceRunningInForeground = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        stopForeground(FOREGROUND_SERVICE_TYPE)
        serviceRunningInForeground = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        val notification = generateNotification()
        startForeground(NOTIFICATION_ID, notification)
        serviceRunningInForeground = true
        return true
    }

    private fun generateNotification(): Notification {
        val mainNotificationText = "Main"
        val titleText = "Title"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val cancelIntent = Intent(this, AppService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val notificationCompatBuilder = NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID
        )
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setDefaults(0)
            .setSound(null)
            .setVibrate(longArrayOf(-1L))
            .build()
    }

    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder: Binder() {
        internal val service: AppService
            get() = this@AppService
    }

    companion object {
        private val TAG = AppService::class.java.name
        private const val PACKAGE_NAME = "ru.kpfu.amalthea"
        private const val NOTIFICATION_ID = 100017
        private const val FOREGROUND_SERVICE_TYPE: Int = 128
        private const val NOTIFICATION_CHANNEL_ID = "amalthea_channel_01"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
    }

    private fun initBodyPosition() {
        healthRepository.bodyPositionSubject
            .subscribeOnIo()
            .observeOnIo()
            .subscribe(this::onBodyPositionChanged)
    }

    private fun initMovementDetection() {
        healthRepository.movementSubject
            .subscribeOnIo()
            .observeOnIo()
            .subscribe(this::onMovementDetected)
    }

    private fun onBodyPositionChanged(result: HumanActivityEnum) {
        Log.d(TAG, "onBodyPositionChanged: ${result.name}")

    }

    private fun onMovementDetected(result: MovementEnum) {
        Log.d(TAG, "onMovementDetected: ${result.name}")
    }

}