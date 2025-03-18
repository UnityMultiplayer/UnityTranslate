package xyz.bluspring.unitytranslate.forge

//#if FORGE
//$$ import net.minecraftforge.api.distmarker.Dist
//$$ import net.minecraftforge.api.distmarker.OnlyIn
//$$ import net.minecraftforge.eventbus.api.IEventBus
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent
//$$ import net.minecraftforge.fml.common.Mod
//$$ import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
//#elseif NEOFORGE
//$$ import net.neoforged.api.distmarker.Dist
//$$ import net.neoforged.api.distmarker.OnlyIn
//$$ import net.neoforged.bus.api.IEventBus
//$$ import net.neoforged.bus.api.SubscribeEvent
//$$ import net.neoforged.fml.common.Mod
//$$ import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
//#endif
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC

//#if FORGE-LIKE
//$$ @Mod(UnityTranslate.MOD_ID)
//#endif
class UnityTranslateForge {
    val instance = UnityTranslateMC()

    //#if FORGE-LIKE && MC < 1.20.4
    //$$ constructor() {
        //$$ FMLJavaModLoadingContext.get().modEventBus.register(this)
    //$$ }
    //#elseif FORGE-LIKE && MC >= 1.20.4
    //$$ constructor(modEventBus: IEventBus) {
        //$$ modEventBus.register(this)
    //$$ }
    //#endif

    //#if FORGE-LIKE
    //$$ @OnlyIn(Dist.CLIENT)
    //$$ @SubscribeEvent
    //$$ fun onClientLoading(ev: FMLClientSetupEvent) {
        //$$ UnityTranslateMCClient()
        //$$ ConfigScreenHelper.registerConfigScreen()
    //$$ }
    //#endif
}