package xyz.bluspring.unitytranslate.forge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

@Mod(UnityTranslate.MOD_ID)
class UnityTranslateForge {
    val instance = UnityTranslateMC()

    constructor() {
        FMLJavaModLoadingContext.get().modEventBus.register(this)
    }

    constructor(modEventBus: IEventBus) {
        modEventBus.register(this)
    }

    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        UnityTranslateMCClient()

        ConfigScreenHelper.registerConfigScreen()
    }
}