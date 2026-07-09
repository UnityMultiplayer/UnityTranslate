package xyz.bluspring.unitytranslate.client.transcriber.source

import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper
import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper.toAudioFloatArray
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

class JavaxInputDeviceSource(deviceName: String? = null, source: TranscriberSource) : InputSource(source) {
    val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioHelper.SAMPLE_RATE.toFloat(), AudioHelper.SAMPLE_SIZE,
        AudioHelper.CHANNELS, AudioHelper.FRAME_SIZE, AudioHelper.SAMPLE_RATE.toFloat(), false)

    var deviceName: String? = null
        private set
    val line: TargetDataLine

    init {
        val lineInfo = DataLine.Info(TargetDataLine::class.java, this.format)

        this.line = if (deviceName != null)
            AudioSystem.getMixerInfo().firstOrNull { info ->
                val mixer = AudioSystem.getMixer(info)
                mixer.isLineSupported(lineInfo) && info.name == deviceName
            }
                ?.let { info ->
                    this.deviceName = info.name
                    TargetDataLine::class.java.cast(AudioSystem.getMixer(info).getLine(lineInfo))
                }
                ?: TargetDataLine::class.java.cast(AudioSystem.getLine(lineInfo)) // default device
        else
            TargetDataLine::class.java.cast(AudioSystem.getLine(lineInfo)) // default device
    }

    override val available: Int
        get() = this.line.available() / Float.SIZE_BYTES

    override fun sample(): FloatArray {
        val available = this.available
        if (AudioHelper.BUFFER_SIZE > available)
            throw IllegalStateException("Failed to read samples from input device ${this.deviceName ?: "(default input device)"}! Capacity: ${AudioHelper.BUFFER_SIZE}, available: $available")

        val buffer = ByteArray(AudioHelper.BUFFER_SIZE * Float.SIZE_BYTES)
        this.line.read(buffer, 0, buffer.size)
        return buffer.toAudioFloatArray()
    }

    override fun close() {
        super.close()
        this.line.stop()
        this.line.flush()
        this.line.close()
    }
}
