package xyz.bluspring.unitytranslate.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.translator.TranslatorManager

object UnityTranslateCommands {
    fun <S> register(dispatcher: CommandDispatcher<S>, root: String, isClient: Boolean, sender: (S, Component) -> Unit) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<S>(root)
                .then(LiteralArgumentBuilder.literal<S>("info")
                    .executes { ctx ->
                        sender.invoke(ctx.source, ComponentUtils.formatList(listOf(
                            Component.literal("UnityTranslate v${UnityTranslate.instance.proxy.modVersion}"),
                            Component.literal("- Total instances loaded: ${TranslatorManager.instances.size}"),
                            Component.literal("- Queued translations: ${TranslatorManager.queuedTranslations.size}"),
                            Component.empty(),
                            Component.literal("- Supports local translation: ${LocalLibreTranslateInstance.canRunLibreTranslate()}"),
                            Component.literal("- Is local translation server running: ${LocalLibreTranslateInstance.hasStarted}"),
                            Component.literal("- Supports CUDA: ${TranslatorManager.checkSupportsCuda()}"),
                        ), Component.literal("\n")))

                        if (isClient) {
                            sender.invoke(ctx.source, ComponentUtils.formatList(listOf(
                                Component.empty(),
                                Component.literal("Client Info:"),
                                Component.literal("- Enabled: ${UnityTranslate.config.client.enabled}"),
                                Component.literal("- Current transcriber: ${UnityTranslate.config.client.transcriber}"),
                                Component.literal("- Spoken language: ${UnityTranslate.config.client.language}"),
                                Component.literal("- Balloon language: ${UnityTranslate.config.client.balloonLanguage}"),
                                Component.literal("- Server supports UnityTranslate: ${UnityTranslateClient.connectedServerHasSupport}"),
                            ), Component.literal("\n")))
                        }

                        1
                    })
                .then(LiteralArgumentBuilder.literal<S>("clearqueue")
                    .executes { ctx ->
                        TranslatorManager.queuedTranslations.clear()
                        sender.invoke(ctx.source, Component.literal("Forcefully cleared translation queue."))

                        1
                    })
                .then(LiteralArgumentBuilder.literal<S>("debugreload")
                    .executes { ctx ->
                        TranslatorManager.installLibreTranslate()
                        sender.invoke(ctx.source, Component.literal("Restarted timer!"))

                        1
                    })
                .apply {
                    if (isClient) {
                        then(
                            LiteralArgumentBuilder.literal<S>("checktranscriber")
                                .executes { ctx ->
                                    if (UnityTranslateClient.transcriber is BrowserSpeechTranscriber) {
                                        (UnityTranslateClient.transcriber as BrowserSpeechTranscriber).openWebsite()
                                    }

                                    sender.invoke(ctx.source, Component.literal("Reopening browser transcriber if not opened"))

                                    1
                                }
                        )
                    }
                }
        )
    }
}