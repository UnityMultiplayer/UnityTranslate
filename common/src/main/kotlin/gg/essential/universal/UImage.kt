package gg.essential.universal

import com.mojang.blaze3d.platform.NativeImage

class UImage(val nativeImage: NativeImage) {

    fun copyFrom(other: UImage) {
        val otherNative = other.nativeImage
        nativeImage.copyFrom(otherNative)
    }

    fun copy(): UImage {
        return UImage(NativeImage(getWidth(), getHeight(), false)).also { it.copyFrom(this) }
    }

    fun getPixelRGBA(x: Int, y: Int): Int {
        return Integer.rotateLeft(nativeImage.getPixel(x, y), 8) // Convert ARGB to RGBA
    }

    fun setPixelRGBA(x: Int, y: Int, color: Int) {
        nativeImage.setPixel(x, y, Integer.rotateRight(color, 8)) // Convert RGBA to ARGB
    }

    fun getWidth() = nativeImage.width

    fun getHeight() = nativeImage.height

    companion object {
        @JvmStatic
        @JvmOverloads
        fun ofSize(width: Int, height: Int, clear: Boolean = true): UImage {
            return UImage(NativeImage(width, height, clear))
        }
    }
}
