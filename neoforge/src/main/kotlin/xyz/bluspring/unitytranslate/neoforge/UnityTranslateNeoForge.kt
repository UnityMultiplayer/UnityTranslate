package xyz.bluspring.unitytranslate.neoforge

import dev.nyon.klf.KotlinModContainer
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.EventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes

import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
 
@Mod(UnityTranslate.MOD_ID)
class UnityTranslateNeoForge(val container: KotlinModContainer, val eventBus: EventBus) {
    val instance = UnityTranslate()

    init {
        (this.instance.proxy as NeoForgePlatformProxy).container = this.container
        NeoForgeEvents.init()

        this.eventBus.register(this)
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        NeoForge.EVENT_BUS.register(UnityTranslateNeoForgeClient)
        ConfigScreenHelper.createConfigScreen()
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
