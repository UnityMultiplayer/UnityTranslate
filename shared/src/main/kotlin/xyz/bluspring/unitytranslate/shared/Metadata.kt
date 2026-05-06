package xyz.bluspring.unitytranslate.shared

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class Metadata(
    val version: String,
    val minecraftVersion: String,
    val buildTime: Long,
    val buildHash: String
) {
    companion object {
        val CODEC: Codec<Metadata> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("version")
                    .forGetter(Metadata::version),
                Codec.STRING.fieldOf("minecraft_version")
                    .forGetter(Metadata::minecraftVersion),
                Codec.LONG.fieldOf("build_time")
                    .forGetter(Metadata::buildTime),
                Codec.STRING.fieldOf("build_has")
                    .forGetter(Metadata::buildHash),
            )
                .apply(instance, ::Metadata)
        }

        fun parse(json: JsonElement): Metadata {
            return CODEC.decode(JsonOps.INSTANCE, json).orThrow.first
        }
    }
}

