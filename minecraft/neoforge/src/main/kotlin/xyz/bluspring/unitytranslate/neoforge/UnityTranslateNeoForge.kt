package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

@Mod(UnityTranslate.MOD_ID)
class UnityTranslateNeoForge(modEventBus: IEventBus) {
    val instance = UnityTranslateMC()

    init {
        modEventBus.register(this)
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        UnityTranslateMCClient()
        ConfigScreenHelper.registerConfigScreen()
    }
}