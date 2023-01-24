package ru.kpfu.health.body_position

import android.content.Context
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

class HARClassifier(private val context: Context) {

    init {
        System.loadLibrary("tensorflow_inference")
        val modelFile = "frozen_HAR.pb"
        mHARClassifier(modelFile)
    }

    private lateinit var inferenceInterface: TensorFlowInferenceInterface
    private val INPUT_NODE = "LSTM_1_input"
    private val OUTPUT_NODES = arrayOf("Dense_2/Softmax")
    private val OUTPUT_NODE = "Dense_2/Softmax"
    private val INPUT_SIZE = longArrayOf(1, 100, 12)
    private val OUTPUT_SIZE = 7

    private fun mHARClassifier(modelFile: String) {
        inferenceInterface = TensorFlowInferenceInterface(context.assets, modelFile)
    }

    fun predictProbabilities(data: FloatArray): FloatArray {
        val inferenceInterface = inferenceInterface
        val result = FloatArray(OUTPUT_SIZE)
        inferenceInterface.feed(INPUT_NODE, data, *INPUT_SIZE)
        inferenceInterface.run(OUTPUT_NODES)
        inferenceInterface.fetch(OUTPUT_NODE, result)

        //Biking   Downstairs	 Jogging	  Sitting	Standing	Upstairs	Walking
        return result
    }

}