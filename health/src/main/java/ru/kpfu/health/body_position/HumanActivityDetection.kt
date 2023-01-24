package ru.kpfu.health.body_position

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.kpfu.health.movement.MovementEnum
import ru.kpfu.health.repo.HealthRepository
import ru.kpfu.health.utils.observeOnIo
import ru.kpfu.health.utils.subscribeOnIo
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow
import kotlin.math.sqrt

class HumanActivityDetection(private val context: Context,
                             private val classifier: HARClassifier,
                             private val repository: HealthRepository): SensorEventListener {

    companion object {
        const val TAG = "HumanActivity"
    }

    private val nSamples = 100
    private var ax: ArrayList<Float> = arrayListOf()
    private var ay: ArrayList<Float> = arrayListOf()
    private var az: ArrayList<Float> = arrayListOf()

    private var lx: ArrayList<Float> = arrayListOf()
    private var ly: ArrayList<Float> = arrayListOf()
    private var lz: ArrayList<Float> = arrayListOf()

    private var gx: ArrayList<Float> = arrayListOf()
    private var gy: ArrayList<Float> = arrayListOf()
    private var gz: ArrayList<Float> = arrayListOf()

    private var ma: ArrayList<Float> = arrayListOf()
    private var ml: ArrayList<Float> = arrayListOf()
    private var mg: ArrayList<Float> = arrayListOf()

    private var mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var results: FloatArray = floatArrayOf()

    private var accelList = arrayListOf<String>()
    private var gyroList = arrayListOf<String>()

    private var newMax: Float = -1f
    private var newIdx: Int = -1

    private var oldIdx: Int = -1

    private lateinit var humanActivitySubscription: Disposable

    @Volatile
    private var isLastMovement = false
//    private var isLastMovement= AtomicBoolean(false)

    fun onStart() {
        ax = ArrayList()
        ay = ArrayList()
        az = ArrayList()

        lx = ArrayList()
        ly = ArrayList()
        lz = ArrayList()

        gx = ArrayList()
        gy = ArrayList()
        gz = ArrayList()

        ma = ArrayList()
        ml = ArrayList()
        mg = ArrayList()

        registerListeners()

        humanActivitySubscription = createHumanActivityTimer()

        repository.movementSubject
            .observeOnIo()
            .subscribe(this::onMovementChanged)
    }

    private fun onMovementChanged(result: MovementEnum) {
        isLastMovement = when (result) {
            MovementEnum.NOT_MOVEMENT -> {
    //                isLastMovement.set(false)
                false
            }
    //            else -> isLastMovement.set(true)
            else -> true
        }

//        when (result) {
//            MovementEnum.NOT_MOVEMENT -> {
////                isLastMovement.set(false)
//                isLastMovement = false
//            }
////            else -> isLastMovement.set(true)
//            else -> isLastMovement = true
//        }
    }

    fun onDestroy() {
        humanActivitySubscription.dispose()
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        doNothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        activityPrediction()
        val sensor = event!!.sensor
        when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                ax.add(event.values[0])
                ay.add(event.values[1])
                az.add(event.values[2])

                accelList.add(System.currentTimeMillis().toString() + ", " + event.values.joinToString(", "))
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                lx.add(event.values[0])
                ly.add(event.values[1])
                lz.add(event.values[2])
            }
            Sensor.TYPE_GYROSCOPE -> {
                gx.add(event.values[0])
                gy.add(event.values[1])
                gz.add(event.values[2])

                gyroList.add(System.currentTimeMillis().toString() + ", " + event.values.joinToString(", "))
            }
        }
    }

    private fun registerListeners() {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST)
    }


    private fun activityPrediction() {
        val data: MutableList<Float> = ArrayList()
        if (ax.size >= nSamples
            && ay.size >= nSamples
            && az.size >= nSamples
            && lx.size >= nSamples
            && ly.size >= nSamples
            && lz.size >= nSamples
            && gx.size >= nSamples
            && gy.size >= nSamples
            && gz.size >= nSamples
        ) {
            var maValue: Double
            var mgValue: Double
            var mlValue: Double
            for (i in 0 until nSamples) {
                maValue = sqrt(
                    ax[i].toDouble().pow(2.0) + ay[i].toDouble().pow(2.0) + az[i].toDouble()
                        .pow(2.0)
                )
                mlValue = sqrt(
                    lx[i].toDouble().pow(2.0) + ly[i].toDouble().pow(2.0) + lz[i].toDouble()
                        .pow(2.0)
                )
                mgValue = sqrt(
                    gx[i].toDouble().pow(2.0) + gy[i].toDouble().pow(2.0) + gz[i].toDouble()
                        .pow(2.0)
                )
                ma.add(maValue.toFloat())
                ml.add(mlValue.toFloat())
                mg.add(mgValue.toFloat())
            }
            data.addAll(ax.subList(0, nSamples))
            data.addAll(ay.subList(0, nSamples))
            data.addAll(az.subList(0, nSamples))
            data.addAll(lx.subList(0, nSamples))
            data.addAll(ly.subList(0, nSamples))
            data.addAll(lz.subList(0, nSamples))
            data.addAll(gx.subList(0, nSamples))
            data.addAll(gy.subList(0, nSamples))
            data.addAll(gz.subList(0, nSamples))
            data.addAll(ma.subList(0, nSamples))
            data.addAll(ml.subList(0, nSamples))
            data.addAll(mg.subList(0, nSamples))

            results = classifier.predictProbabilities(toFloatArray(data))
            for (i in results.indices) {
                if (results[i] > newMax) {
                    newIdx = i
                    newMax = results[i]
                }
            }
            ax.clear()
            ay.clear()
            az.clear()
            lx.clear()
            ly.clear()
            lz.clear()
            gx.clear()
            gy.clear()
            gz.clear()
            ma.clear()
            ml.clear()
            mg.clear()
        }
    }

    private fun createHumanActivityTimer(): Disposable {
        return Observable.interval(1000, 1500, TimeUnit.MILLISECONDS)
            .subscribeOnIo()
            .observeOnIo()
            .subscribe(this::onActivityInterval) {}
    }

    private fun onActivityInterval(result: Long) {
        if (newIdx > -1) {
            if (oldIdx != newIdx) {
                when (HumanActivityEnum.values()[newIdx]) {
                    HumanActivityEnum.BIKING, HumanActivityEnum.JOGGING, HumanActivityEnum.DOWNSTAIRS, HumanActivityEnum.UPSTAIRS, HumanActivityEnum.WALKING -> {
//                        if (!isLastMovement.get()) {
                        if (!isLastMovement) {
                            repository.bodyPositionSubject.onNext(HumanActivityEnum.values()[oldIdx])
                        } else {
                            repository.bodyPositionSubject.onNext(HumanActivityEnum.values()[newIdx])
                            oldIdx = newIdx
                        }
                    }
                    else -> {
                        repository.bodyPositionSubject.onNext(HumanActivityEnum.values()[newIdx])
                        oldIdx = newIdx
                    }
                }
            }
            newIdx = -1
            newMax = -1f
        }
    }

    private fun toFloatArray(list: List<Float>): FloatArray {
        var i = 0
        val array = FloatArray(list.size)
        for (f in list) {
            array[i++] = f
        }
        return array
    }

}