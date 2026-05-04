package xyz.bluspring.unitytranslate.api.v2.plugin

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * The metadata of a UnityTranslate plugin.
 */
@JvmRecord
data class PluginMetadata(
    val group: String,
    val id: String,
    val name: String,
    val version: String,
    val authors: List<String>,
) {
    val fullId: String
        get() = "$group.$id"

    companion object {
        val CODEC: Codec<PluginMetadata> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("group")
                    .forGetter(PluginMetadata::group),
                Codec.STRING.fieldOf("id")
                    .forGetter(PluginMetadata::id),
                Codec.STRING.fieldOf("name")
                    .forGetter(PluginMetadata::name),
                Codec.STRING.fieldOf("version")
                    .forGetter(PluginMetadata::version),
                Codec.STRING.listOf().fieldOf("authors")
                    .forGetter(PluginMetadata::authors)
            )
                .apply(instance, ::PluginMetadata)
        }
    }
}
