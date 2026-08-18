package xyz.bluspring.unitytranslate.api.v2.transcriber.sampling

import com.google.common.collect.Queues
import xyz.bluspring.unitytranslate.api.v2.util.FloatRingBuffer
import java.nio.FloatBuffer
import java.util.*

open class BufferedSamplingStrategy : TranscriberSamplingStrategy {
    protected val buffer = FloatRingBuffer(8 * 1024 * 1024) // 8 MiB
    protected val queuedOverflow: Queue<FloatArray> = Queues.newConcurrentLinkedQueue()

    override val isUpdated: Boolean
        get() = this.buffer.updatedSinceMark

    override fun submitSamples(samples: FloatArray) {
        synchronized(this.buffer) {
            // Guard against accidental overflow
            if (this.buffer.totalWritten + samples.size >= this.buffer.capacity) {
                synchronized(this.queuedOverflow) {
                    this.queuedOverflow.add(this.buffer.snapshot())
                    this.buffer.reset()
                }
            }

            this.buffer += samples
        }
    }

    override fun collectSamples(): FloatArray {
        return synchronized(this.buffer) {
            synchronized(this.queuedOverflow) {
                this.buffer.mark()
                val current = this.buffer.snapshot()
                val fullBuffer = FloatBuffer.allocate(this.queuedOverflow.sumOf { it.size } + current.size)

                while (this.queuedOverflow.isNotEmpty()) {
                    val overflow = this.queuedOverflow.poll() ?: break
                    fullBuffer.put(overflow)
                }

                fullBuffer.put(current)
                fullBuffer.flip().array()
            }
        }
    }

    override fun reset() {
        this.buffer.reset()
    }
}
