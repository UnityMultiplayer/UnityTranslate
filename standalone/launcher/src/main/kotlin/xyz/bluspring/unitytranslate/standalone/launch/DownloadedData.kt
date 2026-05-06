package xyz.bluspring.unitytranslate.standalone.launch

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder

data class DownloadedData(
    val currentVersion: String,
    val minecraftVersion: String,
) {
    companion object {
        val CODEC: Codec<DownloadedData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("current_version")
                    .forGetter(DownloadedData::currentVersion),
                Codec.STRING.fieldOf("minecraft_version")
                    .forGetter(DownloadedData::minecraftVersion)
            )
                .apply(instance, ::DownloadedData)
        }

        fun get(json: JsonElement): DownloadedData {
            return CODEC.decode(JsonOps.INSTANCE, json).orThrow.first
        }
    }

    fun save(): JsonElement {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).orThrow
    }
}
