package xyz.bluspring.unitytranslate.fabric.client

//? if fabric {
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ClientModInitializer
//? if < 1.19 {
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
//? } else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
*///? }
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1ServerboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.fabric.network.UTClientNetworkHandler
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        mcClientInstance = UnityTranslateMCClient()

        //? if <= 1.20.4 {
        UTClientNetworkHandler.init()
        //?}

        var index = 0

        //? if < 1.19 {
        run {
            val dispatcher = ClientCommandManager.DISPATCHER
        //? } else {
        /*ClientCommandRegistrationCallback.EVENT.register { dispatcher ->
        *///? }
            dispatcher.register(ClientCommandManager.literal("utc")
                .then(
                    ClientCommandManager.literal("debug")
                        .then(
                            ClientCommandManager.argument("text", StringArgumentType.greedyString())
                                .executes { ctx ->
                                    val text = StringArgumentType.getString(ctx, "text")

                                    UnityTranslate.instance.proxy.sendPacketClient(V1ServerboundSendTranscriptPacket(
                                        UnityTranslateMCClient.clientConfig.spokenLanguage,
                                        index++,
                                        System.currentTimeMillis(),
                                        text,
                                    ))

                                    UnityTranslateMCClient.transcriptHolders[UnityTranslateMCClient.clientConfig.spokenLanguage]!!.updateTranscript(
                                        Minecraft.getInstance().player!!,
                                        text, UnityTranslateMCClient.clientConfig.spokenLanguage,
                                        index++, System.currentTimeMillis(),
                                        false
                                    )

                                    1
                                }
                        )
                )
            )
        }

        ClientLifecycleEvents.CLIENT_STARTED.register {
            UnityTranslateMCClient.instance.onClientStarted()
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            UnityTranslateMCClient.instance.onClientStopping()
        }

        ClientPlayConnectionEvents.JOIN.register { handler, _, _ ->
            UnityTranslateMCClient.instance.onClientPlayerJoin(Minecraft.getInstance().player!!)
        }

        HudRenderCallback.EVENT.register { poseStack, tickDelta ->
            UnityTranslateMCClient.instance.onRenderHud(poseStack, tickDelta)
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            UnityTranslateMCClient.instance.onClientEndTick()
        }
    }

    companion object {
        lateinit var mcClientInstance: UnityTranslateMCClient
    }
}
//?}