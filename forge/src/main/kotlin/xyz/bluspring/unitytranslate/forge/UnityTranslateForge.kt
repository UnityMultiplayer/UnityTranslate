package xyz.bluspring.unitytranslate.forge

import dev.nyon.klf.KotlinModContainer
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.server.permission.nodes.PermissionNode
import net.minecraftforge.server.permission.nodes.PermissionTypes
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

@Mod(UnityTranslate.MOD_ID)
class UnityTranslateForge(val container: KotlinModContainer, val eventBus: EventBus) {
    val instance = UnityTranslate()

    init {
        (this.instance.proxy as ForgePlatformProxy).container = this.container
        ForgeEvents.init()

        this.eventBus.register(this)
        MinecraftForge.EVENT_BUS.register(UnityTranslateForgeClient)
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        ConfigScreenHelper.createConfigScreen(this.container)
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientKeybinds(ev: RegisterKeyMappingsEvent) {
        for (key in UnityTranslateClient.keys) {
            ev.register(key)
        }
    }

    companion object {
        // how did Forge manage to overcomplicate permissions of all things
        val REQUEST_TRANSLATIONS_NODE = PermissionNode(UnityTranslate.MOD_ID, "request_translations", PermissionTypes.BOOLEAN, { _, _, _ -> true })
    }
}
