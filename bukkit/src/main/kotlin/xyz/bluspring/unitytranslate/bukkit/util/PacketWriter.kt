package xyz.bluspring.unitytranslate.bukkit.util

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*

class PacketWriter {
    private val byteStream = ByteArrayOutputStream()
    private val dataStream = DataOutputStream(byteStream)

    fun writeVarInt(value: Int) {
        var value = value

        while (true) {
            if ((value and SEGMENT_BITS.inv()) == 0) {
                writeByte(value.toByte())
                return
            }

            writeByte(((value and SEGMENT_BITS) or CONTINUE_BIT).toByte())
            value = value ushr 7
        }
    }

    fun writeVarLong(value: Long) {
        var value = value

        while (true) {
            if ((value and SEGMENT_BITS.inv().toLong()) == 0L) {
                writeByte(value.toByte())
                return
            }

            writeByte(((value and SEGMENT_BITS.toLong()) or CONTINUE_BIT.toLong()).toByte())
            value = value ushr 7
        }
    }

    fun writeByte(byte: Byte) {
        dataStream.writeByte(byte.toInt())
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun writeUnsignedByte(byte: UByte) {
        dataStream.writeByte(byte.toInt())
    }

    fun writeShort(short: Short) {
        dataStream.writeShort(short.toInt())
    }

    fun writeUnsignedShort(short: UShort) {
        dataStream.writeShort(short.toInt())
    }

    fun <E : Enum<E>> writeEnumSetUnchecked(enumSet: EnumSet<*>, enumClass: Class<E>) {
        val enums = enumClass.enumConstants
        val bitSet = BitSet(enums.size)
        for (i in 0 until enums.size) {
            bitSet.set(i, enumSet.contains(enums[i]))
        }

        this.writeFixedBitSet(bitSet, enums.size)
    }

    fun <E : Enum<E>> writeEnumSet(enumSet: EnumSet<E>, enumClass: Class<E>) {
        val enums = enumClass.enumConstants
        val bitSet = BitSet(enums.size)
        for (i in 0 until enums.size) {
            bitSet.set(i, enumSet.contains(enums[i]))
        }

        this.writeFixedBitSet(bitSet, enums.size)
    }

    fun writeFixedBitSet(bitSet: BitSet, size: Int) {
        if (bitSet.size() > size)
            throw IllegalStateException("BitSet is larger than expected! (${bitSet.size()} > $size)")

        val length = -Math.floorDiv(-size, 8)
        val array = bitSet.toByteArray().copyOf(length)
        for (i in 0 until length) {
            writeByte(array[i])
        }
    }

    fun writeString(text: String) {
        val length = text.length * MAX_BYTES_PER_UTF8
        writeVarInt(length)

        // Copied from Netty's ByteBufUtil#safeDirectWriteUtf8
        var i = 0
        while (i < text.length) {
            val c = text[i++]

            if (c.code < 0x80) {
                writeByte(c.code.toByte())
            } else if (c.code < 0x800) {
                writeByte((0xC0 or (c.code shr 6)).toByte())
                writeByte((0x80 or (c.code and 0x3f)).toByte())
            } else if (c.code >= '\uD800'.code && c.code <= '\uDFFF'.code) {
                if (!Character.isHighSurrogate(c)) {
                    writeByte(UTF_UNKNOWN)
                    continue
                }

                if (++i == text.length) {
                    writeByte(UTF_UNKNOWN)
                    break
                }

                val c2 = text[i]
                if (!Character.isLowSurrogate(c2)) {
                    writeByte(UTF_UNKNOWN)
                    writeByte(if (Character.isHighSurrogate(c)) UTF_UNKNOWN else c2.code.toByte())
                } else {
                    val codePoint = Character.toCodePoint(c, c2)
                    writeByte((0xF0 or (codePoint shr 18)).toByte())
                    writeByte((0x80 or ((codePoint shr 12) and 0x3F)).toByte())
                    writeByte((0x80 or ((codePoint shr 6) and 0x3F)).toByte())
                    writeByte((0x80 or (codePoint and 0x3F)).toByte())
                }
            } else {
                writeByte((0xE0 or (c.code shr 12)).toByte())
                writeByte((0x80 or ((c.code shr 6) and 0x3F)).toByte())
                writeByte((0x80 or (c.code and 0x3F)).toByte())
            }
        }
    }

    fun writeEnum(enum: Enum<*>) {
        writeVarInt(enum.ordinal)
    }

    fun writeUUID(uuid: UUID) {
        dataStream.writeLong(uuid.mostSignificantBits)
        dataStream.writeLong(uuid.leastSignificantBits)
    }

    fun writeBoolean(value: Boolean) {
        writeByte(if (value) 1 else 0)
    }

    fun asByteArray(): ByteArray {
        return byteStream.toByteArray()
    }

    companion object {
        internal const val SEGMENT_BITS = 0x7F
        internal const val CONTINUE_BIT = 0x80

        internal val MAX_BYTES_PER_UTF8 = Charsets.UTF_8.newEncoder().maxBytesPerChar().toInt()
        internal const val UTF_UNKNOWN: Byte = 0x63
    }
}