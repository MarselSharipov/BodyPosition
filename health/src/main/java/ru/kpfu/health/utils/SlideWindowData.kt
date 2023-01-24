package ru.kpfu.health.utils

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * Класс для реализации механизма скользяшего окна для измеряемых данных
 * ВНИМАНИЕ: Класс работает с ПОРЦИЯМИ получаемых данных ввиду потенциально разных размеров порций
 *
 * @param T - тип элемента порции данных
 * @param totalDesiredSizeMs - Общая необходимая длительность в миллисекундах
 * @param chunkDesiredSizeMs - Длительность одной порции данных
 */
class SlideWindowData<T>(totalDesiredSizeMs: Int, chunkDesiredSizeMs: Int) {
    private val dataBuffer: Queue<T> = LinkedList()
    private val numOfRequiredChunks: Int = (totalDesiredSizeMs.toFloat() / chunkDesiredSizeMs.toFloat()).roundToInt()
    private var numOfCurrentChunks = 0

    /**
     * Получение буффера данных
     *
     * @return список всех данных буффера типа ArrayList<T>? (возвращает null если буффер не заполнен)
     */
    @Synchronized
    fun getBuffer(): ArrayList<T>? = if (numOfCurrentChunks == numOfRequiredChunks) {
        ArrayList(dataBuffer)
    } else {
        null
    }

    /**
     * Заполнение данных буффера скользящего окна
     *
     * @param chunk - порция данных
     */
    @Synchronized
    fun putData(chunk: ArrayList<T>) {
        if (numOfCurrentChunks < numOfRequiredChunks) {
            for (i in chunk) {
                dataBuffer.add(i)
            }
            numOfCurrentChunks++
        } else {
            for (i in chunk.indices) {
                dataBuffer.remove()
                dataBuffer.add(chunk[i])
            }
        }
    }

    @Synchronized
    fun putData(chunk: T) {
        if (numOfCurrentChunks < numOfRequiredChunks) {
            numOfCurrentChunks++
            dataBuffer.add(chunk)
        } else {
            dataBuffer.remove()
            dataBuffer.add(chunk)
        }
    }

    /**
     * Заполнение данных буффера скользящего окна и получение буффера
     *
     * @param chunk - порция данных
     * @return список всех данных буффера типа ArrayList<T>? (возвращает null если буффер не заполнен)
     */
    @Synchronized
    fun putAndGetBuffer(chunk: ArrayList<T>): ArrayList<T>? {
        if (numOfCurrentChunks < numOfRequiredChunks) {
            for (i in chunk) {
                dataBuffer.add(i)
            }
            numOfCurrentChunks++
        } else {
            for (i in chunk.indices) {
                dataBuffer.remove()
                dataBuffer.add(chunk[i])
            }
        }
        return if (numOfCurrentChunks == numOfRequiredChunks) {
            ArrayList(dataBuffer)
        } else {
            null
        }
    }

    /**
     * Очищение буффера скользящего окна
     */
    @Synchronized
    fun clearBuffer() {
        dataBuffer.clear()
        numOfCurrentChunks = 0
    }
}