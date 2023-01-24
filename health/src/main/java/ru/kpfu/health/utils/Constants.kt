package ru.kpfu.health.utils

object Constants {
    object Values {
        /**
         * Ускорение свободного падения в м/с^2
         */
        const val FREE_FALL_ACCELERATION = 9.8

        const val FACE_PTS_DEVIATION_THRESHOLD = 10.0

        const val HEAD_TREMOR_MEASURE_LEN_SEC: Long = 15

        const val HEAD_TREMOR_PREPARE_LEN_SEC: Long = 5

        const val HEAD_TREMOR_SCORE_THRESHOLD = 25

        const val HEAD_TREMOR_AMPLITUDE_THRESHOLD = 0.8f

        const val HEAD_TREMOR_START_FREQUENCY = 3

        const val HEAD_TREMOR_END_FREQUENCY = 15

    }

    object LogTags {
        /**
         * Тэг для HumanFallLogger
         */
        const val HUMAN_FALL_LOGGER = "HUMAN_FALL_LOGGER"

        const val FACE_DETECTION_LOGGER = "FACE_DETECTION_LOGGER"

        const val MICROPHONE_LOGGER = "MICROPHONE_LOGGER"

    }
}