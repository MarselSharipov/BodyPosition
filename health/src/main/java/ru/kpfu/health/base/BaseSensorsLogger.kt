package ru.kpfu.health.base

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.kpfu.health.utils.observeOnIo
import ru.kpfu.health.utils.subscribeOnIo
import java.util.concurrent.TimeUnit

open class BaseSensorsLogger(private val context: Context, private val time: Long): SensorEventListener {

    private lateinit var timer: Disposable

    private var mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var accelList = mutableListOf<String>()
    private var gyroList = mutableListOf<String>()

    fun onStart() {
        accelList.clear()
        gyroList.clear()
        registerListeners()
        timer = createTimer()
    }

    fun onDestroy() {
        timer.dispose()
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
 //        doNothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event!!.sensor
        when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                addAccelData("${System.currentTimeMillis()}, ${event.values.joinToString(", ")}")
            }
            Sensor.TYPE_GYROSCOPE -> {
                addGyroData("${System.currentTimeMillis()}, ${event.values.joinToString(", ")}")
            }
        }
    }

    private fun registerListeners() {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.registerListener(
            this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(
            this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_GAME)
    }

    private fun createTimer(): Disposable {
        return Observable.interval(1000, time, TimeUnit.MILLISECONDS)
            .subscribeOnIo()
            .observeOnIo()
            .subscribe(this::onTimer, this::onError)
    }

    protected open fun onTimer(result: Long) {
        // Implement here different algorithms
    }

    private fun onError(throwable: Throwable) {
        Log.d("Movement", "onError: ${throwable.message}")
        Log.d("Movement", "onError: $throwable")
    }

    @Synchronized
    fun getAccelData(): String {
        return accelList.joinToString(separator = "\n")
    }

    @Synchronized
    fun getGyroData(): String {
        return gyroList.joinToString(separator = "\n")
    }

    @Synchronized
    private fun addAccelData(data: String) {
        accelList.add(data)
    }

    @Synchronized
    private fun addGyroData(data: String) {
        gyroList.add(data)
    }

    @Synchronized
    fun clearData() {
        accelList.clear()
        gyroList.clear()
    }

}