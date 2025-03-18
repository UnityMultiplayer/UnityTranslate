package xyz.bluspring.unitytranslate.minecraft.client.resources

import com.google.gson.JsonParser
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import xyz.bluspring.unitytranslate.minecraft.client.resources.background.BackgroundTypes
import xyz.bluspring.unitytranslate.minecraft.client.resources.background.ImageBackgroundType

class UTResourceReloadListener : ResourceManagerReloadListener {
    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        BackgroundTypes.ID_TO_TYPE.clear()

        resourceManager.listResources("unitytranslate/background_types/") { it.endsWith(".json") }.forEach { id ->
            val resource = resourceManager.getResource(id) ?: throw IllegalStateException()
            val data = BackgroundTypes.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(resource.inputStream.bufferedReader()))
            val background = data.resultOrPartial { throw RuntimeException(it) }
                .orElseThrow()
                .first

            if (background is ImageBackgroundType) {
                resourceManager.getResource(background.texture).inputStream.use {
                    val image = NativeImage.read(it)

                    background.width = image.width
                    background.height = image.height
                }
            }

            BackgroundTypes.ID_TO_TYPE[ResourceLocation.tryParse(id.toString().removeSuffix(".json").replaceFirst("unitytranslate/background_types/", ""))!!] = background
        }
    }
}