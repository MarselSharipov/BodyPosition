package ru.kpfu.health.utils

import ru.kpfu.health.utils.math.Butterworth
import ru.kpfu.health.utils.math.DoubleFft1d
import java.nio.ByteBuffer

private fun decodeYUV420SPtoRedBlueGreenSum(width: Int, height: Int, byteBuffer: ByteBuffer, type: Int): Int {
    val frameSize = width * height

    var sum = 0
    var sumr = 0
    var sumg = 0
    var sumb = 0

    var j = 0
    var yp = 0
    while (j < height) {
        var uvp = frameSize + (j shr 1) * width
        var u = 0
        var v = 0
        var i = 0
        while (i < width) {
            var y = (0xff and byteBuffer[yp].toInt()) - 16
            if (y < 0) y = 0
            if (i and 1 == 0 && uvp < byteBuffer.capacity()) {
                v = (0xff and byteBuffer[uvp++].toInt()) - 128
                u = (0xff and byteBuffer[uvp++].toInt()) - 128
            }
            val y1192 = 1192 * y
            var r = y1192 + 1634 * v
            var g = y1192 - 833 * v - 400 * u
            var b = y1192 + 2066 * u
            if (r < 0) r = 0 else if (r > 262143) r = 262143
            if (g < 0) g = 0 else if (g > 262143) g = 262143
            if (b < 0) b = 0 else if (b > 262143) b = 262143
            val pixel = -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
            val red = pixel shr 16 and 0xff
            val green = pixel shr 8 and 0xff
            val blue = pixel and 0xff
            sumr += red
            sumg += green
            sumb += blue
            i++
            yp++
        }
        j++
    }

    when (type) {
        1 -> sum = sumr
        2 -> sum = sumb
        3 -> sum = sumg
    }

    return sum
}

fun decodeYUV420SPtoRedBlueGreenAvg(byteBuffer: ByteBuffer?, width: Int, height: Int,  type: Int): Double {
    if (byteBuffer == null) return 0.0
    val frameSize = width * height
    val sum = decodeYUV420SPtoRedBlueGreenSum(width, height, byteBuffer, type)
    return (sum / frameSize).toDouble()
}

//private fun decodeYUV420SPtoRedBlueGreenSum(yuv420sp: ByteBuffer?, width: Int, height: Int, type: Int): Int {
//    if (yuv420sp == null) {
//        return 0
//    }
//    val frameSize = width * height
//    var sum = 0
//    var sumr = 0
//    var sumg = 0
//    var sumb = 0
//
//    var j = 0
//    var yp = 0
//    while (j < height) {
//        var uvp = frameSize + (j shr 1) * width
//        var u = 0
//        var v = 0
//        var i = 0
//        while (i < width) {
//            var y = (0xff and yuv420sp[yp].toInt()) - 16
//            if (y < 0) y = 0
//            if (i and 1 == 0) {
//                v = (0xff and yuv420sp[uvp++].toInt()) - 128
//                u = (0xff and yuv420sp[uvp++].toInt()) - 128
//            }
//            val y1192 = 1192 * y
//            var r = y1192 + 1634 * v
//            var g = y1192 - 833 * v - 400 * u
//            var b = y1192 + 2066 * u
//            if (r < 0) r = 0 else if (r > 262143) r = 262143
//            if (g < 0) g = 0 else if (g > 262143) g = 262143
//            if (b < 0) b = 0 else if (b > 262143) b = 262143
//            val pixel = 0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
//            val red = pixel shr 16 and 0xff
//            val green = pixel shr 8 and 0xff
//            val blue = pixel and 0xff
//            sumr += red
//            sumg += green
//            sumb += blue
//            i++
//            yp++
//        }
//        j++
//    }
//
//    when (type) {
//        1 -> sum = sumr
//        2 -> sum = sumb
//        3 -> sum = sumg
//    }
//    return sum
//}
//
//fun decodeYUV420SPtoRedBlueGreenAvg(yuv420sp: ByteBuffer?, width: Int, height: Int, type: Int): Double {
//    if (yuv420sp == null) return 0.0
//    val frameSize = width * height
//    val sum: Int = decodeYUV420SPtoRedBlueGreenSum(yuv420sp, width, height, type)
//    return (sum / frameSize).toDouble()
//}

fun fFT(data: Array<Double>, size: Int, samplingFrequency: Double): Double {
    var temp = 0.0
    var POMP = 0.0
    val output = DoubleArray(2 * size)
    for (i in output.indices) output[i] = 0.0
    for (x in 0 until size) {
        output[x] = data[x]
    }
    val fft = DoubleFft1d(size)
    fft.realForward(output)
    for (x in 0 until 2 * size) {
        output[x] = Math.abs(output[x])
    }
    for (p in 35 until size) { // 12 was chosen because it is a minimum frequency that we think people can get to determine heart rate.
        if (temp < output[p]) {
            temp = output[p]
            POMP = p.toDouble()
        }
    }
    if (POMP < 35) {
        POMP = 0.0
    }
    return POMP * samplingFrequency / (2 * size)
}

fun fFT2(data: Array<Double>, size: Int, samplingFrequency: Double): Double {
    var temp = 0.0
    var POMP = 0.0
    val output = DoubleArray(2 * size)
    val butterworth = Butterworth()
    butterworth.bandPass(2, samplingFrequency, 0.2, 0.3)
    for (i in output.indices) output[i] = 0.0
    for (x in 0 until size) {
        output[x] = data[x]
    }
    val fft = DoubleFft1d(size)
    fft.realForward(output)
    for (x in 0 until 2 * size) {
        output[x] = butterworth.filter(output[x])
    }
    for (x in 0 until 2 * size) {
        output[x] = Math.abs(output[x])
    }
    for (p in 12 until size) {
        if (temp < output[p]) {
            temp = output[p]
            POMP = p.toDouble()
        }
    }
    return POMP * samplingFrequency / (2 * size)
}