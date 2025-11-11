package xyz.bluspring.unitytranslate.transcriber.google

import java.io.OutputStream
import java.util.LinkedList

class QueuedByteArrayOutputStream(val sizeToWrite: Int) : OutputStream() {
    private val backing = LinkedList<Int>()
    var whatToWrite: (IntArray) -> Unit = {}

    override fun write(b: Int) {
        synchronized(backing) {
            backing.add(b)

            if (backing.size >= sizeToWrite) {
                val byteArray = backing.toIntArray()
                flush()

                whatToWrite.invoke(byteArray)
            }
        }
    }

    override fun flush() {
        synchronized(backing) {
            backing.clear()
        }
    }
}