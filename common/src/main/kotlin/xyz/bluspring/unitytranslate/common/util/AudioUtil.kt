package xyz.bluspring.unitytranslate.common.util

object AudioUtil {
    // Downsamples audio from one sample rate to another.
    fun downsample(original: FloatArray, inputRate: Int, outputRate: Int): FloatArray {
        val copy = FloatArray(original.size)
        var inputSampleIndex = -1
        var outputSampleIndex = 0
        var current = outputRate

        forever@while (true) {
            var sum = 0f
            for (i in 0 until inputRate) {
                if (current == outputRate) {
                    inputSampleIndex++
                    if (inputSampleIndex >= original.size)
                        break@forever

                    current = 0
                }

                sum += original[inputSampleIndex]
                current++
            }

            copy[outputSampleIndex++] = (sum / inputSampleIndex)
        }

        return copy.copyOf(outputSampleIndex)
    }

    fun ShortArray.audioToFloatArray(): FloatArray {
        val floatArray = FloatArray(this.size)

        for ((index, sh) in this.withIndex()) {
            floatArray[index] = (sh * 3.051851E-5F).toFloat()
        }

        return floatArray
    }
}