package xyz.bluspring.unitytranslate.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.commands.CommandSourceStack
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.commands.UnityTranslateCommands
import xyz.bluspring.unitytranslate.translator.TranslatorManager

class UnityTranslateFabric : ModInitializer {
    override fun onInitialize() {
        instance = UnityTranslate()

        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            UnityTranslateCommands.register(dispatcher, "unitytranslate", false, CommandSourceStack::sendSystemMessage)
        }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            TranslatorManager.serverStarting(server)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            TranslatorManager.serverStopping()
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            TranslatorManager.playerQuit(handler.player)
        }
    }

    companion object {
        lateinit var instance: UnityTranslate
    }
}
