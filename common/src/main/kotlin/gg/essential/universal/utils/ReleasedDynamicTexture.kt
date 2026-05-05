package gg.essential.universal.utils

import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.*
import net.minecraft.client.renderer.texture.AbstractTexture
import java.io.Closeable
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class ReleasedDynamicTexture private constructor(
    val width: Int,
    val height: Int,
    textureData: NativeImage?,
) : AbstractTexture() {

    private var resources = Resources(this)

    init {
        resources.textureData = textureData ?: NativeImage(width, height, true)
    }
    private var textureData by resources::textureData

    var uploaded: Boolean = false

    constructor(width: Int, height: Int) : this(width, height, null)

    constructor(nativeImage: NativeImage) : this(nativeImage.width, nativeImage.height, nativeImage)


    fun updateDynamicTexture() {
        uploadTexture()
    }

    fun uploadTexture() {
        if (!uploaded) {
            val device = RenderSystem.getDevice()
            val usage = GpuTexture.USAGE_TEXTURE_BINDING or GpuTexture.USAGE_COPY_SRC or GpuTexture.USAGE_COPY_DST
            val texture = device.createTexture(null as String?, usage, TextureFormat.RGBA8, width, height, 1, 1)
            sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.NEAREST, true);
            device.createCommandEncoder().writeToTexture(texture, textureData!!)
            textureData = null
            uploaded = true
            resources.gpuTexture = texture
            this.texture = texture
            val view = device.createTextureView(texture)
            resources.gpuTextureView = view
            this.textureView = view
            Resources.drainCleanupQueue()
        }
    }


    val dynamicGlId: Int
        get() {
            uploadTexture()
            return (resources.gpuTexture as GlTexture?)?.glId() ?: -1
        }

    override fun getTextureView(): GpuTextureView {
        uploadTexture()
        return super.getTextureView()
    }



    override fun getTexture(): GpuTexture {
        uploadTexture()
        return super.getTexture()
    }

    override fun close() {
        super.close()
        resources.close()
    }

    private class Resources(referent: ReleasedDynamicTexture) : PhantomReference<ReleasedDynamicTexture>(referent, referenceQueue), Closeable {
        var gpuTexture: GpuTexture? = null
           set(value) {
               field?.close()
               field = value
           }

        var gpuTextureView: GpuTextureView? = null
            set(value) {
                field?.close()
                field = value
            }

        var textureData: NativeImage? = null
           set(value) {
               field?.close()
               field = value
           }

        init {
            toBeCleanedUp.add(this)
        }

        override fun close() {
            toBeCleanedUp.remove(this)

            gpuTexture = null
            gpuTextureView = null

            textureData = null
        }

        companion object {
            val referenceQueue: ReferenceQueue<ReleasedDynamicTexture> = ReferenceQueue()
            val toBeCleanedUp: MutableSet<Resources> = Collections.newSetFromMap(ConcurrentHashMap())

            fun drainCleanupQueue() {
                while (true) {
                    ((referenceQueue.poll() ?: break) as Resources).close()
                }
            }
        }
    }
}
