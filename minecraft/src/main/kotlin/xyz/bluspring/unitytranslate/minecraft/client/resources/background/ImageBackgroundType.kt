package xyz.bluspring.unitytranslate.minecraft.client.resources.background

import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable
import kotlin.properties.Delegates

class ImageBackgroundType(
    val resizeType: ResizeType,
    val texture: ResourceLocation
) : BackgroundType {
    var width by Delegates.notNull<Int>()
    var height by Delegates.notNull<Int>()

    override val type: MapCodec<out BackgroundType>
        get() = CODEC

    override fun render(consumer: VertexConsumer, width: Int, height: Int) {

    }

    enum class ResizeType : StringRepresentable {
        COVER_LEFT,
        COVER_CENTER,
        COVER_RIGHT,
        COVER_TOP,
        COVER_BOTTOM,
        STRETCH;

        override fun getSerializedName(): String {
            return this.name.lowercase()
        }

        companion object {
            fun get(name: String): ResizeType {
                return ResizeType.valueOf(name.uppercase())
            }

            val CODEC = StringRepresentable.fromEnum(ResizeType::values, ResizeType::get)
        }
    }

    companion object {
        val CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResizeType.CODEC
                    .fieldOf("resize")
                    .forGetter(ImageBackgroundType::resizeType),
                ResourceLocation.CODEC
                    .fieldOf("texture")
                    .forGetter(ImageBackgroundType::texture)
            )
                .apply(instance, ::ImageBackgroundType)
        }
    }
}