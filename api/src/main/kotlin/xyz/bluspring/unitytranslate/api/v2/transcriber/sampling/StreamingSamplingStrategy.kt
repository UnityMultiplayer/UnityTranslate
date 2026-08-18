package xyz.bluspring.unitytranslate.api.v2.transcriber.sampling

class StreamingSamplingStrategy : BufferedSamplingStrategy() {
    override fun collectSamples(): FloatArray {
        val samples = super.collectSamples()
        this.reset()
        return samples
    }
}
