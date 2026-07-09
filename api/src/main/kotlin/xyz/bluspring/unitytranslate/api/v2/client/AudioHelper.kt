package xyz.bluspring.unitytranslate.api.v2.client

/**
 * Some utilities and constants that should be used when samples are passed to UnityTranslate.
 */
object AudioHelper {
    // Signed float32, little-endian, mono audio

    const val SAMPLE_RATE = 16_000
    const val SAMPLE_SIZE = 16
    const val CHANNELS = 1
    const val FRAME_SIZE = 2
    const val BUFFER_SIZE = (SAMPLE_RATE / 1000) * 20

    @JvmStatic
    fun ByteArray.toAudioFloatArray(): FloatArray {
        val floats = FloatArray(this.size / 2)

        for (i in 0..<this.size / 2) {
            if ((this[i * 2 + 1].toInt() and 0x80) != 0) {
                floats[i] =
                    (Short.MIN_VALUE + ((this[i * 2 + 1].toInt() and 0x7F) shl 8) or (this[i * 2].toInt() and 0xFF)).toFloat()
            } else {
                floats[i] = (((this[i * 2 + 1].toInt() shl 8) and 0xFF00) or (this[i * 2].toInt() and 0xFF)).toFloat()
            }
        }

        return floats
    }
}
