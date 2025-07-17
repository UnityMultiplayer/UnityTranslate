package xyz.bluspring.unitytranslate.minecraft.client.resources

import com.google.gson.JsonParser
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import xyz.bluspring.unitytranslate.minecraft.client.resources.background.BackgroundTypes
import xyz.bluspring.unitytranslate.minecraft.client.resources.background.ImageBackgroundType
import java.io.BufferedReader
import java.io.InputStream

class UTResourceReloadListener : ResourceManagerReloadListener {
    //? if < 1.19 {
    fun Resource.openAsReader(): BufferedReader {
        return this.inputStream.bufferedReader()
    }

    fun Resource.orElseThrow(): Resource {
        return this
    }

    fun Resource.open(): InputStream {
        return this.inputStream
    }
    //? }
    
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        BackgroundTypes.ID_TO_TYPE.clear()

        resourceManager.listResources("unitytranslate/background_types/") { it/*? if >= 1.19 {*//*.path*//*? }*/.endsWith(".json") }.forEach { /*? if >= 1.19 {*//*(id, resource) ->*//*? } else {*/id ->/*? }*/
            //? if < 1.19
            val resource = resourceManager.getResource(id)
            
            val data = BackgroundTypes.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(resource.openAsReader()))
            val background = data.resultOrPartial { throw RuntimeException(it) }
                .orElseThrow()
                .first

            if (background is ImageBackgroundType) {
                resourceManager.getResource(background.texture).orElseThrow().open().use {
                    val image = NativeImage.read(it)

                    background.width = image.width
                    background.height = image.height
                }
            }

            BackgroundTypes.ID_TO_TYPE[ResourceLocation.tryParse(id.toString().removeSuffix(".json").replaceFirst("unitytranslate/background_types/", ""))!!] = background
        }
    }
}