package xyz.bluspring.unitytranslate.fabric.client

//#if FABRIC
import com.mojang.brigadier.arguments.StringArgumentType
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0ServerboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.fabric.network.UTClientNetworkHandler
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        mcClientInstance = UnityTranslateMCClient()

        //#if MC <= 1.20.4
        UTClientNetworkHandler.init()
        //#endif

        var index = 0

        ClientCommandRegistrationEvent.EVENT.register { dispatcher ->
            dispatcher.register(ClientCommandRegistrationEvent.literal("utc")
                .then(
                    ClientCommandRegistrationEvent.literal("debug")
                        .then(
                            ClientCommandRegistrationEvent.argument("text", StringArgumentType.greedyString())
                                .executes { ctx ->
                                    val text = StringArgumentType.getString(ctx, "text")

                                    UnityTranslate.instance.proxy.sendPacketClient(V0ServerboundSendTranscriptPacket(
                                        UnityTranslateMCClient.clientConfig.spokenLanguage,
                                        text,
                                        index++,
                                        System.currentTimeMillis()
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
    }

    companion object {
        lateinit var mcClientInstance: UnityTranslateMCClient
    }
}
//#endif