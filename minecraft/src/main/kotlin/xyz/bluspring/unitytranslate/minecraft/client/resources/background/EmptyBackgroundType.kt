package xyz.bluspring.unitytranslate.minecraft.client.resources.background

import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.serialization.MapCodec

class EmptyBackgroundType private constructor(): BackgroundType {
    override val type: MapCodec<out BackgroundType>
        get() = CODEC

    override fun render(consumer: VertexConsumer, width: Int, height: Int) {
    }

    companion object {
        val INSTANCE = EmptyBackgroundType()
        val CODEC = MapCodec.unit(INSTANCE)
    }
}