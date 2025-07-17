package xyz.bluspring.unitytranslate.forge

//? if forge {
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
//?} else if neoforge {
/*import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import xyz.bluspring.unitytranslate.common.UnityTranslate
*///?}
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

//? if forge {
@Mod(UnityTranslate.MOD_ID)
//?}
class UnityTranslateForge {
    val instance = UnityTranslateMC()

    //? if (forge || neoforge) && < 1.20.4 {
    constructor() {
        FMLJavaModLoadingContext.get().modEventBus.register(this)
    }
    //?} else if (forge || neoforge) && >= 1.20.4 {
    /*constructor(modEventBus: IEventBus) {
        modEventBus.register(this)
    }
    *///?}

    //? if forge || neoforge {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun onClientLoading(ev: FMLClientSetupEvent) {
        UnityTranslateMCClient()
        ConfigScreenHelper.registerConfigScreen()
    }
    //?}
}