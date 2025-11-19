package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent

//? if >= 1.20.4 {
/*import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
*///? }

import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
 
@Mod(UnityTranslate.MOD_ID)
class UnityTranslateNeoForge {
    init {
        UnityTranslate()
//? if >= 1.20.4 {
        MOD_BUS.register(this)
//? } else {
        FMLJavaModLoadingContext.get().modEventBus.register(this)
//? }
        NeoForgeEvents.init()
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        UnityTranslateClient()

        ConfigScreenHelper.createConfigScreen()
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientKeybinds(ev: RegisterKeyMappingsEvent) {
        UnityTranslateClient.registerKeys()
    }
}
