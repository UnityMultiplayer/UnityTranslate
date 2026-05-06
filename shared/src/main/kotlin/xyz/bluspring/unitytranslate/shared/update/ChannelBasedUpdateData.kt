package xyz.bluspring.unitytranslate.shared.update

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class ChannelBasedUpdateData(
    val channels: Map<String, UpdateData>
) {
    companion object {
        val CODEC: Codec<ChannelBasedUpdateData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.unboundedMap(Codec.STRING, UpdateData.CODEC)
                    .optionalFieldOf("channels", emptyMap())
                    .forGetter(ChannelBasedUpdateData::channels)
            )
                .apply(instance, ::ChannelBasedUpdateData)
        }
    }
}
