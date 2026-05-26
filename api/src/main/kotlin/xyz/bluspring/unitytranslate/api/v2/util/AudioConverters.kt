package xyz.bluspring.unitytranslate.api.v2.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioConverters {
    /**
     * Converts 16-bit signed little-endian PCM bytes to float32 samples
     * normalized to the range [-1.0, 1.0].
     *
     * @param pcm the byte[] formatted packet
     * @return the converted audio
     */
    @JvmStatic
    fun bytePcm16ToFloat(pcm: ByteArray): FloatArray {
        val samples = pcm.size / 2
        val result = FloatArray(samples)
        val bb: ByteBuffer = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until samples) {
            val s: Short = bb.getShort()
            result[i] = s / 32768.0f // normalize to [-1, 1]
        }

        return result
    }

    /**
     * Converts 16-bit signed little-endian PCM bytes to float32 samples
     * normalized to the range [-1.0, 1.0].
     *
     * @param pcm the double[] formatted packet
     * @return the converted audio
     */
    @JvmStatic
    fun doublePcm16ToFloat(pcm: DoubleArray): FloatArray {
        val result = FloatArray(pcm.size)

        for (i in pcm.indices) {
            var d = pcm[i]

            // Clamp to [-1.0, 1.0] just in case
            if (d > 1.0)
                d = 1.0
            else if (d < -1.0)
                d = -1.0

            result[i] = d.toFloat()
        }

        return result
    }

    /**
     * Converts 16-bit signed little-endian PCM bytes to float32 samples
     * normalized to the range [-1.0, 1.0].
     *
     * @param pcm the short[] formatted packet
     * @return the converted audio
     */
    @JvmStatic
    fun shortPcm16ToFloat(pcm: ShortArray): FloatArray {
        val result = FloatArray(pcm.size)
        val scale = 1.0f / 32768.0f

        for (i in pcm.indices) {
            result[i] = pcm[i] * scale
        }

        return result
    }
}
