package ru.kpfu.health.movement

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import ru.kpfu.health.base.BaseSensorsLogger
import ru.kpfu.health.repo.HealthRepository

class MovementDetection(
    context: Context,
    private val repository: HealthRepository): BaseSensorsLogger(context, 1000) {

    override fun onTimer(result: Long) {
        repository.movementSubject.onNext(classify(getGyroData()))
        clearData()
    }

    private fun classify(gyroData: String): MovementEnum {
        val python = Python.getInstance()
        val pythonFile = python.getModule("tremor_arm_detector")

        val result = pythonFile.callAttr("is_movement", gyroData).toJava(Int::class.java)
        Log.d("Movement", "movement: $result")
        return if (result == 1) MovementEnum.MOVEMENT else MovementEnum.NOT_MOVEMENT
    }

}