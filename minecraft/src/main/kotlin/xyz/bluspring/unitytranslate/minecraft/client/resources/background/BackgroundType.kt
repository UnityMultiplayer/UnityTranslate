package xyz.bluspring.unitytranslate.minecraft.client.resources.background

import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.serialization.MapCodec

interface BackgroundType {
    val type: MapCodec<out BackgroundType>

    fun render(consumer: VertexConsumer, width: Int, height: Int)
}