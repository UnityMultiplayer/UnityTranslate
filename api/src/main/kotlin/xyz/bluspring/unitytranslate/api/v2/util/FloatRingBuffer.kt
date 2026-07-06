package xyz.bluspring.unitytranslate.api.v2.util

// hopefully this is good :blobcatfearful:
class FloatRingBuffer(val capacity: Int) {
    private val buffer = FloatArray(this.capacity)
    private var totalWritten = 0L
    private var marked = 0L

    operator fun plusAssign(value: Float) {
        val index = (this.totalWritten % this.capacity).toInt()
        this.buffer[index] = value
        this.totalWritten++
    }

    operator fun plusAssign(values: FloatArray) {
        for (sample in values) {
            this += sample
        }
    }

    val size: Int
        get() = this.totalWritten.coerceAtMost(this.capacity.toLong()).toInt()

    fun mark() {
        this.marked = this.totalWritten
    }

    val updatedSinceMark: Boolean
        get() {
            return this.marked != this.totalWritten
        }

    fun snapshot(): FloatArray {
        val currentSize = this.size
        val snapshot = FloatArray(currentSize)

        if (this.totalWritten <= this.capacity) {
            // Unwrapped
            System.arraycopy(this.buffer, 0, snapshot, 0, currentSize)
        } else {
            // Wrapped
            val startIndex = (this.totalWritten % this.capacity).toInt()
            val firstPartLength = this.capacity - startIndex
            System.arraycopy(this.buffer, startIndex, snapshot, 0, firstPartLength)
            System.arraycopy(this.buffer, 0, snapshot, firstPartLength, startIndex)
        }

        return snapshot
    }

    fun reset() {
        this.totalWritten = 0L
        this.marked = 0
    }
}
