package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.config.DependsOn
import xyz.bluspring.unitytranslate.common.config.Hidden
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.duck.ScrollableWidget
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation

class UTConfigScreen(private val parent: Screen?) : Screen(MinecraftProxy.literal("UnityTranslate")) {
    companion object {
        const val SMALL_WIDTH = 100
        const val BUTTON_WIDTH = 200
        const val BUTTON_HEIGHT = 20

        private fun createButton(text: Component, x: Int, y: Int, width: Int, height: Int, handler: Button.OnPress): Button {
            return Button(x, y, width, height, text, handler)
        }
    }

    override fun init() {
        val width = (this.width / 4).coerceAtLeast(250)

        addWidget(createButton(
            MinecraftProxy.translatable("gui.unitytranslate.config.client"),
            this.width / 2 - (width / 2), 75,
            width, BUTTON_HEIGHT
        ) {
            Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.instance.config.client::class, UnityTranslate.instance.config.client, "client"))
        })

        addWidget(createButton(
            MinecraftProxy.translatable("gui.unitytranslate.config.common"),
            this.width / 2 - (width / 2), 75 + BUTTON_HEIGHT + 5,
            width, BUTTON_HEIGHT
        ) {
            Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.instance.config.common::class, UnityTranslate.instance.config.common, "common"))
        })

        addWidget(createButton(
            MinecraftProxy.translatable("gui.unitytranslate.config.server"),
            this.width / 2 - (width / 2), 75 + ((BUTTON_HEIGHT + 5) * 2),
            width, BUTTON_HEIGHT
        ) {
            Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.instance.config.server::class, UnityTranslate.instance.config.server, "server"))
        })

        addWidget(createButton(
            MinecraftProxy.translatable("gui.done"),
            this.width / 2 - (BUTTON_WIDTH / 2), this.height - 20 - 15,
            width, BUTTON_HEIGHT
        ) {
            onClose()
        })
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.renderBackground(poseStack)
        super.render(poseStack, mouseX, mouseY, partialTicks)

        fill(poseStack, 0, 50, width, height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
        Minecraft.getInstance().font.draw(poseStack, this.title, (this.width / 2 - (Minecraft.getInstance().font.width(this.title) / 2)).toFloat(), 20f, 16777215)

        UnityTranslateMCClient.renderCreditText(poseStack)
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }

    inner class UTConfigSubScreen<T : Any>(val configClass: KClass<out T>, val instance: T, val type: String) : Screen(MinecraftProxy.translatable("gui.unitytranslate.config.$type")) {
        lateinit var doneButton: Button
        private var scrollAmount = 0.0
        private var scrolling = false

        val scrollbarPosition: Int
            get() {
                return this.width - 3
            }

        var maxPosition = 0
        val maxScroll: Int
            get() {
                return (this.maxPosition - ((this.height - 50) - 50 - 4)).coerceAtLeast(0)
            }

        override fun init() {
            var y = 75

            for (member in configClass.declaredMembers) {
                if (member.name == "component1")
                    break

                if (member !is KMutableProperty<*> || member.visibility != KVisibility.PUBLIC)
                    continue

                val dependent = member.getter.findAnnotation<DependsOn>()
                if (dependent != null && (configClass.declaredMembers.first { it.name == dependent.configName } as KMutableProperty<*>).getter.call(instance) != true) {
                    continue
                }

                if (member.getter.findAnnotation<Hidden>() != null)
                    continue

                val name = StringWidget(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}"), font)
                name.x = 35
                name.y = y
                name.alignLeft()
                name.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                (name as ScrollableWidget).updateInitialPosition()

                addWidget(name)

                val value = member.getter.call(instance)

                if (value is Boolean) {
                    var current: Boolean = value

                    val button = createButton(MinecraftProxy.translatable("unitytranslate.value.$current")
                        .withStyle(if (current) ChatFormatting.GREEN else ChatFormatting.RED),
                        this.width - SMALL_WIDTH - 20, y - (BUTTON_HEIGHT / 2) + 3,
                        BUTTON_WIDTH, BUTTON_HEIGHT
                    ) { btn ->
                        current = !current
                        member.setter.call(instance, current)
                        rebuildWidgets()

                        btn.message = MinecraftProxy.translatable("unitytranslate.value.$current")
                            .withStyle(if (current) ChatFormatting.GREEN else ChatFormatting.RED)
                    }
                        button.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))

                    addWidget(button)
                } else if (value is Enum<*>) {
                    val enumClass = value::class.java as Class<Enum<*>>
                    val valueOf = enumClass.getDeclaredMethod("values")
                    val values = valueOf.invoke(null) as Array<Enum<*>>
                    var current = value.ordinal

                    val button = Button.builder(MinecraftProxy.literal(values[current].name.propercase())) { btn ->
                        if (hasShiftDown()) {
                            current -= 1
                            if (current < 0)
                                current = values.size - 1
                        } else {
                            current += 1
                            if (current >= values.size)
                                current = 0
                        }

                        btn.message = MinecraftProxy.literal(values[current].name.propercase())
                    }
                        .pos(this.width - Button.SMALL_WIDTH - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                        .width(Button.SMALL_WIDTH)
                        .tooltip(Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc")))
                        .build()

                    addWidget(button)
                } else if (value is MutableList<*> && member.name == "offloadServers") { // special case
                    val actualValue = value as MutableList<UnityTranslateConfig.OffloadedLibreTranslateServer>

                    addWidget(Button.builder(MinecraftProxy.literal("+")) {
                        actualValue.add(UnityTranslateConfig.OffloadedLibreTranslateServer(""))
                        this.rebuildWidgets()
                    }
                        .pos(this.width - Button.DEFAULT_HEIGHT - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                        .width(Button.DEFAULT_HEIGHT)
                        .build())

                    for ((index, server) in actualValue.withIndex()) {
                        y += 30

                        val boxWidth = (Button.DEFAULT_WIDTH + 20).coerceAtMost(this.width / 3)
                        addWidget(EditBox(font, this.width / 2 - boxWidth - 5, y - (Button.DEFAULT_HEIGHT / 2) + 3, boxWidth, Button.DEFAULT_HEIGHT, MinecraftProxy.translatable("unitytranslate.value.none")).apply {
                            this.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.website_url.desc"))
                            this.value = server.url
                            this.setResponder {
                                server.url = it
                            }
                        })

                        addWidget(EditBox(font, this.width / 2 + 5, y - (Button.DEFAULT_HEIGHT / 2) + 3, boxWidth, Button.DEFAULT_HEIGHT, MinecraftProxy.translatable("unitytranslate.value.none")).apply {
                            this.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.api_key.desc"))
                            this.value = server.authKey ?: ""
                            this.setResponder {
                                server.authKey = it
                            }
                        })

                        addWidget(Button.builder(MinecraftProxy.literal("-")) {
                            actualValue.removeAt(index)
                            this.rebuildWidgets()
                        }
                            .pos(this.width - Button.DEFAULT_HEIGHT - 20, y - (Button.DEFAULT_HEIGHT / 2) + 3)
                            .width(Button.DEFAULT_HEIGHT)
                            .build())

                        addArrows(this@UTConfigSubScreen.width - 17, y, index, actualValue)
                    }
                } else if (value is MutableList<*> && member.name == "translatePriority") { // special case
                    val actualValue = value as MutableList<UnityTranslateConfig.TranslationPriority>

                    for ((index, priority) in actualValue.withIndex()) {
                        y += 30

                        val text = MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.${priority.name.lowercase()}")
                        val priorityName = StringWidget(text, font)
                        priorityName.x = this.width / 2 - (font.width(text) / 2)
                        priorityName.y = y
                        priorityName.alignCenter()
                        priorityName.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.${priority.name.lowercase()}.desc"))
                        (priorityName as ScrollableWidget).updateInitialPosition()

                        addWidget(priorityName)
                        addArrows((this.width / 2 + 50).coerceAtMost(this.width - 10), y, index, actualValue)
                    }
                } else if (value is Float) {
                    val range = member.getter.findAnnotation<FloatRange>() ?: throw IllegalStateException("Missing range!")

                    val min = range.from
                    val max = range.to

                    addWidget(EditBox(font, this.width - Button.SMALL_WIDTH - 20, y - (Button.DEFAULT_HEIGHT / 2) + 4, Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT, Component.empty())
                        .apply {
                            this.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                            this.value = value.toString()
                            this.setFilter { it.toFloatOrNull() != null || it.isBlank() || it.contains('.') } // TODO: make adjustable via annotation
                            this.setResponder {
                                member.setter.call(instance, Mth.clamp((
                                        if (it.startsWith('.'))
                                            "0$it"
                                        else if (it.endsWith('.'))
                                            "${it}0"
                                        else
                                            it
                                        ).toFloatOrNull() ?: 0.0f, min, max
                                ))
                            }
                        })

                    //#if MC >= 1.20.4
                    //$$ addWidget(SpriteIconButton.builder(Component.empty(), {
                    //#else
                    addWidget(TextAndImageButton.builder(Component.empty(), ARROW_UP) {
                        //#endif
                        member.setter.call(instance, Mth.clamp(value + range.increment, min, max))
                        this.rebuildWidgets()
                        //#if MC >= 1.20.4
                        //$$ }, true)
                        //#else
                    }
                        //#endif
                        //#if MC >= 1.20.4
                        //$$ .sprite(ARROW_UP, 8, 8)
                        //$$ .size(12, 12)
                        //#else
                        .offset(0, 2)
                        .texStart(0, 0)
                        .textureSize(8, 8)
                        .usedTextureSize(8, 8)
                        //#endif
                        .build()
                        .apply {
                            this.x = this@UTConfigSubScreen.width - 18
                            this.y = y - 8
                            this.width = 12
                            (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                            (this as ScrollableWidget).updateInitialPosition()

                            if (value >= max) {
                                this.active = false
                            }
                        }
                    )

                    //#if MC >= 1.20.4
                    //$$ addWidget(SpriteIconButton.builder(Component.empty(), {
                    //#else
                    addWidget(TextAndImageButton.builder(Component.empty(), ARROW_DOWN) {
                        //#endif
                        member.setter.call(instance, Mth.clamp(value - range.increment, min, max))
                        this.rebuildWidgets()
                        //#if MC >= 1.20.4
                        //$$ }, true)
                        //#else
                    }
                        //#endif
                        //#if MC >= 1.20.4
                        //$$ .sprite(ARROW_DOWN, 8, 8)
                        //$$ .size(12, 12)
                        //#else
                        .offset(0, 2)
                        .texStart(0, 0)
                        .textureSize(8, 8)
                        .usedTextureSize(8, 8)
                        //#endif
                        .build()
                        .apply {
                            this.x = this@UTConfigSubScreen.width - 18
                            this.y = y + 4
                            this.width = 12
                            (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                            (this as ScrollableWidget).updateInitialPosition()

                            if (value <= min) {
                                this.active = false
                            }
                        }
                    )
                } else if (value is Int) {
                    val range = member.getter.findAnnotation<IntRange>() ?: throw IllegalStateException("Missing range!")

                    val min = range.from
                    val max = range.to.run {
                        if (member.name == "libreTranslateThreads") {
                            Mth.clamp(this, 1, Runtime.getRuntime().availableProcessors() - 2)
                        } else this
                    }

                    addWidget(EditBox(font, this.width - Button.SMALL_WIDTH - 20, y - (Button.DEFAULT_HEIGHT / 2) + 4, Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT, Component.empty())
                        .apply {
                            this.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                            this.value = value.toString()
                            this.setFilter { it.toIntOrNull() != null || it.isBlank() } // TODO: make adjustable via annotation
                            this.setResponder {
                                member.setter.call(instance, Mth.clamp(it.toIntOrNull() ?: min, min, max))
                            }
                        })

                    //#if MC >= 1.20.4
                    //$$ addWidget(SpriteIconButton.builder(Component.empty(), {
                    //#else
                    addWidget(TextAndImageButton.builder(Component.empty(), ARROW_UP) {
                        //#endif
                        member.setter.call(instance, Mth.clamp(value + range.increment, min, max))
                        this.rebuildWidgets()
                        //#if MC >= 1.20.4
                        //$$ }, true)
                        //#else
                    }
                        //#endif
                        //#if MC >= 1.20.4
                        //$$ .sprite(ARROW_UP, 8, 8)
                        //$$ .size(12, 12)
                        //#else
                        .offset(0, 2)
                        .texStart(0, 0)
                        .textureSize(8, 8)
                        .usedTextureSize(8, 8)
                        //#endif
                        .build()
                        .apply {
                            this.x = this@UTConfigSubScreen.width - 18
                            this.y = y - 8
                            this.width = 12
                            (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                            (this as ScrollableWidget).updateInitialPosition()

                            if (value >= max) {
                                this.active = false
                            }
                        }
                    )

                    //#if MC >= 1.20.4
                    //$$ addWidget(SpriteIconButton.builder(Component.empty(), {
                    //#else
                    addWidget(TextAndImageButton.builder(Component.empty(), ARROW_DOWN) {
                        //#endif
                        member.setter.call(instance, Mth.clamp(value - range.increment, min, max))
                        this.rebuildWidgets()
                        //#if MC >= 1.20.4
                        //$$ }, true)
                        //#else
                    }
                        //#endif
                        //#if MC >= 1.20.4
                        //$$ .sprite(ARROW_DOWN, 8, 8)
                        //$$ .size(12, 12)
                        //#else
                        .offset(0, 2)
                        .texStart(0, 0)
                        .textureSize(8, 8)
                        .usedTextureSize(8, 8)
                        //#endif
                        .build()
                        .apply {
                            this.x = this@UTConfigSubScreen.width - 18
                            this.y = y + 4
                            this.width = 12
                            (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                            (this as ScrollableWidget).updateInitialPosition()

                            if (value <= min) {
                                this.active = false
                            }
                        }
                    )
                }

                y += 30
            }

            if (type == "client") { // Special case
                addWidget(Button.builder(MinecraftProxy.translatable("unitytranslate.configure_boxes")) {
                    Minecraft.getInstance().setScreen(EditTranscriptBoxesScreen(UnityTranslateClient.languageBoxes, this@UTConfigSubScreen))
                }
                    .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), y)
                    .build()
                    .apply {
                        (this as ScrollableWidget).updateInitialPosition()
                    }
                )

                y += 30

                addWidget(Button.builder(MinecraftProxy.translatable("unitytranslate.set_spoken_language")) {
                    Minecraft.getInstance().setScreen(LanguageSelectScreen(this@UTConfigSubScreen, false))
                }
                    .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), y)
                    .build()
                    .apply {
                        (this as ScrollableWidget).updateInitialPosition()
                    }
                )

                y += 30
            }

            maxPosition = y

            doneButton = addWidget(
                Button.builder(CommonComponents.GUI_DONE) {
                    this.onClose()
                }
                    .pos(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 20 - 15)
                    .build()
            )
        }
    }
}