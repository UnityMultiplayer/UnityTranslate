package xyz.bluspring.unitytranslate.bukkit.util

import xyz.bluspring.unitytranslate.bukkit.util.PacketWriter.Companion.CONTINUE_BIT
import xyz.bluspring.unitytranslate.bukkit.util.PacketWriter.Companion.SEGMENT_BITS
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*
import kotlin.experimental.and

class PacketReader(private val byteArray: ByteArray) {
    private val dataStream = DataInputStream(ByteArrayInputStream(byteArray))

    fun readVarInt(): Int {
        var value = 0
        var position = 0

        while (true) {
            val currentByte = readByte()
            value = value or ((currentByte and SEGMENT_BITS.toByte()).toInt() shl position)

            if ((currentByte and CONTINUE_BIT.toByte()).toInt() == 0)
                break

            position += 7

            if (position >= 32)
                throw RuntimeException("VarInt too big!")
        }

        return value
    }

    fun readVarLong(): Long {
        var value = 0L
        var position = 0

        while (true) {
            val currentByte = readByte()
            value = value or ((currentByte and SEGMENT_BITS.toByte()).toLong() shl position)

            if ((currentByte and CONTINUE_BIT.toByte()).toLong() == 0L)
                break

            position += 7

            if (position >= 64)
                throw RuntimeException("VarLong too big!")
        }

        return value
    }

    fun readString(): String {
        val length = readVarInt()

        if (length < 0)
            throw RuntimeException("wtf is this string")

        if (length == 0)
            return ""

        val bytes = dataStream.readNBytes(length)
        return String(bytes, Charsets.UTF_8)
    }

    fun readByte(): Byte {
        return dataStream.readByte()
    }

    fun readInt(): Int {
        return dataStream.readInt()
    }

    fun readLong(): Long {
        return dataStream.readLong()
    }

    fun readBoolean(): Boolean {
        return readByte() != (0).toByte()
    }

    fun <T : Enum<T>> readEnum(enumClass: Class<T>): T {
        return readEnumUnchecked(enumClass) as T
    }

    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }

    fun readEnumUnchecked(enumClass: Class<*>): Enum<*> {
        return enumClass.enumConstants[readVarInt()] as Enum<*>
    }

    fun <E : Enum<E>> readEnumSet(enumClass: Class<E>): EnumSet<E> {
        val enums = enumClass.enumConstants
        val bitSet = this.readFixedBitSet(enums.size)
        val enumSet = EnumSet.noneOf(enumClass)

        for (i in 0 until enums.size) {
            if (bitSet.get(i)) {
                enumSet.add(enums[i])
            }
        }

        return enumSet
    }

    fun readFixedBitSet(size: Int): BitSet {
        return BitSet.valueOf(dataStream.readNBytes(-Math.floorDiv(-size, 8)))
    }
}