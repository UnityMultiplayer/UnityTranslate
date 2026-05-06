package xyz.bluspring.unitytranslate.shared

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class LaunchConfig(
    val channel: String = "stable",
    val updateUrl: String = "https://meta.bluspring.xyz/updates/unitytranslate.json"
) {
    companion object {
        @JvmField
        val CODEC: Codec<LaunchConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("channel")
                    .forGetter(LaunchConfig::channel)
            )
                .apply(instance, ::LaunchConfig)
        }
    }
}
