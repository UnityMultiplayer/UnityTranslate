package xyz.bluspring.unitytranslate.client.transcriber.source

import com.mojang.blaze3d.audio.OpenAlUtil
import org.lwjgl.openal.ALC11
import org.lwjgl.openal.ALUtil
import org.lwjgl.openal.EXTFloat32
import org.lwjgl.system.MemoryUtil
import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import kotlin.concurrent.thread

class OpenALInputDeviceSource(deviceName: String? = null, source: TranscriberSource) : InputSource(source) {
    val deviceHandle: Long
    val deviceName: String? = if (deviceName == null)
        ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER)
    else {
        ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER)
            ?.firstOrNull {
                it == deviceName
            }
            ?: ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER)
    }

    val thread: Thread
    private var isRunning = true

    var isMuted = false

    init {
        OpenAlUtil.checkALError("UnityTranslate: Get device name")
        this.deviceHandle = ALC11.alcCaptureOpenDevice(deviceName, AudioHelper.SAMPLE_RATE, EXTFloat32.AL_FORMAT_MONO_FLOAT32, AudioHelper.BUFFER_SIZE)
        OpenAlUtil.checkALError("UnityTranslate: Open device $deviceName")
        if (this.deviceHandle == MemoryUtil.NULL) {
            throw UnsupportedOperationException("No input devices could be found!")
        }

        ALC11.alcCaptureStart(this.deviceHandle)
        this.thread = thread(start = true, isDaemon = true) {
            while (this.isRunning) {
                if (this.isMuted) continue

                val samples = this.sample()
                this.source.submitSpeechSamples(samples)
            }
        }
    }

    override val available: Int
        get() {
            val available = ALC11.alcGetInteger(this.deviceHandle, ALC11.ALC_CAPTURE_SAMPLES)
            OpenAlUtil.checkALError("UnityTranslate: Get available samples for ${this.deviceName ?: "(default input device)"}")
            return available
        }

    override fun sample(): FloatArray {
        if (AudioHelper.BUFFER_SIZE > this.available)
            throw IllegalStateException("Failed to read samples from input device ${this.deviceName ?: "(default input device)"}! Capacity: ${AudioHelper.BUFFER_SIZE}, available: $available")

        val samples = FloatArray(AudioHelper.BUFFER_SIZE)
        ALC11.alcCaptureSamples(this.deviceHandle, samples, AudioHelper.BUFFER_SIZE)
        OpenAlUtil.checkALError("UnityTranslate: Capture samples for ${this.deviceName ?: "(default input device)"}")
        return samples
    }

    override fun close() {
        this.isRunning = false
        ALC11.alcCaptureStop(this.deviceHandle)
    }
}
