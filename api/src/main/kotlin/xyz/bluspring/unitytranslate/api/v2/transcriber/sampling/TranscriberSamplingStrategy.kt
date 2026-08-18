package xyz.bluspring.unitytranslate.api.v2.transcriber.sampling

/**
 * Represents the manner of which a transcriber will handle incoming samples.
 */
interface TranscriberSamplingStrategy {
    val isUpdated: Boolean
    fun submitSamples(samples: FloatArray)
    fun collectSamples(): FloatArray

    fun reset()
}
