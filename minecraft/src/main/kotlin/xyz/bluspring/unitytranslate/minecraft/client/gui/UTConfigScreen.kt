package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.config.*
import xyz.bluspring.unitytranslate.common.config.IntRange
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.duck.ScrollableWidget
import xyz.bluspring.unitytranslate.minecraft.mixin.AbstractWidgetAccessor
import kotlin.math.absoluteValue
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

        //#if MC >= 1.20.2
        //$$ val ARROW_UP = MinecraftProxy.id("arrow_up")
        //$$ val ARROW_DOWN = MinecraftProxy.id("arrow_down")
        //#else
        val ARROW_UP = MinecraftProxy.id("textures/gui/sprites/arrow_up.png")
        val ARROW_DOWN = MinecraftProxy.id("textures/gui/sprites/arrow_down.png")
        //#endif

        private fun createButton(text: Component, x: Int, y: Int, width: Int, height: Int, handler: Button.OnPress): Button {
            return Button(x, y, width, height, text, handler)
        }
    }

    //#if MC <= 1.18.2
    fun rebuildWidgets() {
        this.clearWidgets()
        this.init()
    }
    //#endif

    override fun init() {
        val width = (this.width / 4).coerceAtLeast(250)

        addRenderableWidget(createButton(
            MinecraftProxy.translatable("gui.unitytranslate.config.client"),
            this.width / 2 - (width / 2), 75,
            width, BUTTON_HEIGHT
        ) {
            Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslateClientConfig::class, UnityTranslateMCClient.clientConfig, "client"))
        })

        addRenderableWidget(createButton(
            MinecraftProxy.translatable("gui.unitytranslate.config.common"),
            this.width / 2 - (width / 2), 75 + BUTTON_HEIGHT + 5,
            width, BUTTON_HEIGHT
        ) {
            Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.instance.config.common::class, UnityTranslate.instance.config.common, "common"))
        })

        addRenderableWidget(createButton(
            MinecraftProxy.translatable("gui.unitytranslate.config.server"),
            this.width / 2 - (width / 2), 75 + ((BUTTON_HEIGHT + 5) * 2),
            width, BUTTON_HEIGHT
        ) {
            Minecraft.getInstance().setScreen(UTConfigSubScreen(UnityTranslate.instance.config.server::class, UnityTranslate.instance.config.server, "server"))
        })

        addRenderableWidget(createButton(
            MinecraftProxy.translatable("gui.done"),
            this.width / 2 - (BUTTON_WIDTH / 2), this.height - 20 - 15,
            width, BUTTON_HEIGHT
        ) {
            onClose()
        })
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.renderBackground(poseStack)
        fill(poseStack, 0, 50, width, height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
        super.render(poseStack, mouseX, mouseY, partialTicks)

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

        //#if MC <= 1.18.2
        fun rebuildWidgets() {
            this.clearWidgets()
            this.init()
        }
        //#endif

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

                val name = StringWidget(35, y, MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}"), font)
                name.x = 35
                name.y = y
                name.alignLeft()
                //#if MC >= 1.20.1
                //$$ name.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                //#else
                name.tooltip = MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc")
                //#endif
                (name as ScrollableWidget).updateInitialPosition()

                addRenderableWidget(name)

                val value = member.getter.call(instance)

                when (value) {
                    is Boolean -> {
                        var current: Boolean = value

                        val button = createButton(
                            MinecraftProxy.translatable("unitytranslate.value.$current")
                                .withStyle(if (current) ChatFormatting.GREEN else ChatFormatting.RED),
                            this.width - SMALL_WIDTH - 20, y - (BUTTON_HEIGHT / 2) + 3,
                            SMALL_WIDTH, BUTTON_HEIGHT
                        ) { btn ->
                            current = !current
                            member.setter.call(instance, current)
                            rebuildWidgets()

                            btn.message = MinecraftProxy.translatable("unitytranslate.value.$current")
                                .withStyle(if (current) ChatFormatting.GREEN else ChatFormatting.RED)
                        }

                        button as ScrollableWidget
                        //#if MC >= 1.20.1
                        //$$ button.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                        //#else
                        button.tooltip = MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc")
                        //#endif

                        addRenderableWidget(button)

                        Unit
                    }

                    is Enum<*> -> {
                        val enumClass = value::class.java as Class<Enum<*>>
                        val valueOf = enumClass.getDeclaredMethod("values")
                        val values = valueOf.invoke(null) as Array<Enum<*>>
                        var current = value.ordinal

                        val button = ButtonBuilder.builder(MinecraftProxy.literal(values[current].name.propercase())) { btn ->
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
                            .pos(this.width - SMALL_WIDTH - 20, y - (BUTTON_HEIGHT / 2) + 3)
                            .width(SMALL_WIDTH)
                            //#if MC >= 1.20.1
                            //$$ .tooltip(Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc")))
                            //#else
                            .tooltip(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                            //#endif
                            .build()

                        addRenderableWidget(button)

                        Unit
                    }

                    is MutableList<*> -> {
                        if (member.name == "offloadServers") {
                            val actualValue = value as MutableList<UnityTranslateConfig.OffloadedLibreTranslateServer>

                            addRenderableWidget(ButtonBuilder.builder(MinecraftProxy.literal("+")) {
                                actualValue.add(UnityTranslateConfig.OffloadedLibreTranslateServer(""))
                                rebuildWidgets()
                            }
                                .pos(this.width - BUTTON_HEIGHT - 20, y - (BUTTON_HEIGHT / 2) + 3)
                                .width(BUTTON_HEIGHT)
                                .build())

                            for ((index, server) in actualValue.withIndex()) {
                                y += 30

                                val boxWidth = (BUTTON_WIDTH + 20).coerceAtMost(this.width / 3)
                                addRenderableWidget(EditBox(font, this.width / 2 - boxWidth - 5, y - (BUTTON_HEIGHT / 2) + 3, boxWidth, BUTTON_HEIGHT, MinecraftProxy.translatable("unitytranslate.value.none")).apply {
                                    (this as ScrollableWidget).tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.website_url.desc"))
                                    this.value = server.url
                                    this.setResponder {
                                        server.url = it
                                    }
                                })

                                addRenderableWidget(EditBox(font, this.width / 2 + 5, y - (BUTTON_HEIGHT / 2) + 3, boxWidth, BUTTON_HEIGHT, MinecraftProxy.translatable("unitytranslate.value.none")).apply {
                                    (this as ScrollableWidget).tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.api_key.desc"))
                                    this.value = server.authKey ?: ""
                                    this.setResponder {
                                        server.authKey = it
                                    }
                                })

                                addRenderableWidget(ButtonBuilder.builder(MinecraftProxy.literal("-")) {
                                    actualValue.removeAt(index)
                                    this@UTConfigScreen.rebuildWidgets()
                                }
                                    .pos(this.width - BUTTON_HEIGHT - 20, y - (BUTTON_HEIGHT / 2) + 3)
                                    .width(BUTTON_HEIGHT)
                                    .build())

                                addArrows(this@UTConfigSubScreen.width - 17, y, index, actualValue)
                            }
                        } else if (member.name == "translatePriority") {
                            val actualValue = value as MutableList<UnityTranslateConfig.TranslationPriority>

                            for ((index, priority) in actualValue.withIndex()) {
                                y += 30

                                val text = MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.${priority.name.lowercase()}")
                                val priorityName = StringWidget(this.width / 2 - (font.width(text) / 2), y, text, font)
                                priorityName.alignCenter()
                                priorityName.tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.${priority.name.lowercase()}.desc"))
                                (priorityName as ScrollableWidget).updateInitialPosition()

                                addRenderableWidget(priorityName)
                                addArrows((this.width / 2 + 50).coerceAtMost(this.width - 10), y, index, actualValue)
                            }
                        }

                        Unit
                    }

                    is Float -> {
                        val range = member.getter.findAnnotation<FloatRange>() ?: throw IllegalStateException("Missing range!")

                        val min = range.from
                        val max = range.to

                        addRenderableWidget(EditBox(font, this.width - SMALL_WIDTH - 20, y - (BUTTON_HEIGHT / 2) + 4, SMALL_WIDTH, BUTTON_HEIGHT, MinecraftProxy.literal(""))
                            .apply {
                                (this as ScrollableWidget).tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
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

                        addRenderableWidget(
                            //#if MC >= 1.20.4
                            //$$ SpriteIconButton.builder(Component.empty(), {
                            //#elseif MC >= 1.20.1
                            //$$ TextAndImageButton.builder(Component.empty(), ARROW_UP) {
                            //#else
                            ImageButton(0, 2, 12, 12, 0, 0, 0, ARROW_UP, 8, 8) {
                            //#endif
                                member.setter.call(instance, Mth.clamp(value + range.increment, min, max))
                                rebuildWidgets()
                            //#if MC >= 1.20.4
                            //$$ }, true)
                            //#else
                            }
                            //#endif
                                //#if MC >= 1.20.4
                                //$$ .sprite(ARROW_UP, 8, 8)
                                //$$ .size(12, 12)
                                //$$ .build()
                                //#elseif MC >= 1.20.1
                                //$$ .offset(0, 2)
                                //$$ .texStart(0, 0)
                                //$$ .textureSize(8, 8)
                                //$$ .usedTextureSize(8, 8)
                                //$$ .build()
                                //#endif
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

                        addRenderableWidget(
                            //#if MC >= 1.20.4
                            //$$ SpriteIconButton.builder(Component.empty(), {
                            //#elseif MC >= 1.20.1
                            //$$ TextAndImageButton.builder(Component.empty(), ARROW_DOWN) {
                            //#else
                            ImageButton(0, 2, 12, 12, 0, 0, 0, ARROW_DOWN, 8, 8) {
                                //#endif
                                member.setter.call(instance, Mth.clamp(value - range.increment, min, max))
                                rebuildWidgets()
                                //#if MC >= 1.20.4
                                //$$ }, true)
                                //#else
                            }
                                //#endif
                                //#if MC >= 1.20.4
                                //$$ .sprite(ARROW_DOWN, 8, 8)
                                //$$ .size(12, 12)
                                //$$ .build()
                                //#elseif MC >= 1.20.1
                                //$$ .offset(0, 2)
                                //$$ .texStart(0, 0)
                                //$$ .textureSize(8, 8)
                                //$$ .usedTextureSize(8, 8)
                                //$$ .build()
                                //#endif
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

                        Unit
                    }

                    is Int -> {
                        val range = member.getter.findAnnotation<IntRange>() ?: throw IllegalStateException("Missing range!")

                        val min = range.from
                        val max = range.to.run {
                            if (member.name == "libreTranslateThreads") {
                                Mth.clamp(this, 1, Runtime.getRuntime().availableProcessors() - 2)
                            } else this
                        }

                        addRenderableWidget(EditBox(font, this.width - SMALL_WIDTH - 20, y - (BUTTON_HEIGHT / 2) + 4, SMALL_WIDTH, BUTTON_HEIGHT,
                            MinecraftProxy.literal(""))
                            .apply {
                                (this as ScrollableWidget).tooltip = Tooltip.create(MinecraftProxy.translatable("config.unitytranslate.$type.${member.name}.desc"))
                                this.value = value.toString()
                                this.setFilter { it.toIntOrNull() != null || it.isBlank() } // TODO: make adjustable via annotation
                                this.setResponder {
                                    member.setter.call(instance, Mth.clamp(it.toIntOrNull() ?: min, min, max))
                                }
                            })

                        addRenderableWidget(
                            //#if MC >= 1.20.4
                            //$$ SpriteIconButton.builder(Component.empty(), {
                            //#elseif MC >= 1.20.1
                            //$$ TextAndImageButton.builder(Component.empty(), ARROW_UP) {
                            //#else
                            ImageButton(0, 2, 12, 12, 0, 0, 0, ARROW_UP, 8, 8) {
                                //#endif
                                member.setter.call(instance, Mth.clamp(value + range.increment, min, max))
                                rebuildWidgets()
                                //#if MC >= 1.20.4
                                //$$ }, true)
                                //#else
                            }
                                //#endif
                                //#if MC >= 1.20.4
                                //$$ .sprite(ARROW_UP, 8, 8)
                                //$$ .size(12, 12)
                                //$$ .build()
                                //#elseif MC >= 1.20.1
                                //$$ .offset(0, 2)
                                //$$ .texStart(0, 0)
                                //$$ .textureSize(8, 8)
                                //$$ .usedTextureSize(8, 8)
                                //$$ .build()
                                //#endif
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

                        addRenderableWidget(
                            //#if MC >= 1.20.4
                            //$$ SpriteIconButton.builder(Component.empty(), {
                            //#elseif MC >= 1.20.1
                            //$$ TextAndImageButton.builder(Component.empty(), ARROW_DOWN) {
                            //#else
                            ImageButton(0, 2, 12, 12, 0, 0, 0, ARROW_DOWN, 8, 8) {
                                //#endif
                                member.setter.call(instance, Mth.clamp(value - range.increment, min, max))
                                rebuildWidgets()
                                //#if MC >= 1.20.4
                                //$$ }, true)
                                //#else
                            }
                                //#endif
                                //#if MC >= 1.20.4
                                //$$ .sprite(ARROW_DOWN, 8, 8)
                                //$$ .size(12, 12)
                                //$$ .build()
                                //#elseif MC >= 1.20.1
                                //$$ .offset(0, 2)
                                //$$ .texStart(0, 0)
                                //$$ .textureSize(8, 8)
                                //$$ .usedTextureSize(8, 8)
                                //$$ .build()
                                //#endif
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

                        Unit
                    }
                }

                y += 30
            }

            if (type == "client") { // Special case
                addRenderableWidget(ButtonBuilder.builder(MinecraftProxy.translatable("unitytranslate.configure_boxes")) {
                    Minecraft.getInstance().setScreen(EditTranscriptBoxesScreen(UnityTranslateMCClient.clientConfig.transcriptBoxes, this@UTConfigSubScreen))
                }
                    .pos(this.width / 2 - (BUTTON_WIDTH / 2), y)
                    .build()
                    .apply {
                        (this as ScrollableWidget).updateInitialPosition()
                    }
                )

                y += 30

                addRenderableWidget(ButtonBuilder.builder(MinecraftProxy.translatable("unitytranslate.set_spoken_language")) {
                    Minecraft.getInstance().setScreen(LanguageSelectScreen(this@UTConfigSubScreen, false))
                }
                    .pos(this.width / 2 - (BUTTON_WIDTH / 2), y)
                    .build()
                    .apply {
                        (this as ScrollableWidget).updateInitialPosition()
                    }
                )

                y += 30
            }

            maxPosition = y

            doneButton = addRenderableWidget(
                ButtonBuilder.builder(CommonComponents.GUI_DONE) {
                    this.onClose()
                }
                    .pos(this.width / 2 - (BUTTON_WIDTH / 2), this.height - 20 - 15)
                    .build()
            )
        }

        //#if MC >= 1.20.1
        //$$ override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        //#else
        override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        //#endif
            //#if MC >= 1.20.4
            //$$ this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
            //#elseif MC >= 1.20.1
            //$$ this.renderBackground(guiGraphics)
            //#else
            this.renderBackground(poseStack)
            //#endif

            //#if MC >= 1.20.1
            //$$ guiGraphics.fill(0, 50, this.width, this.height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
            //$$ guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215)
            //#else
            fill(poseStack, 0, 50, this.width, this.height - 50, FastColor.ARGB32.color(150, 0, 0, 0))
            drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215)
            //#endif

            //#if MC >= 1.20.1
            //$$ guiGraphics.enableScissor(0, 50, this.width, this.height - 50)
            //$$ super.render(guiGraphics, mouseX, mouseY, partialTick)
            //$$ guiGraphics.disableScissor()
            //#else
            super.render(poseStack, mouseX, mouseY, partialTick)
            //#endif

            if (maxScroll.absoluteValue > 0) {
                var scrollbarHeight = ((this.height - 50 - 50) * (this.height - 50 - 50)) / this.maxPosition
                scrollbarHeight = Mth.clamp(scrollbarHeight, 32, this.height - 50 - 50 - 8)

                var scrollbarPosY = (this.scrollAmount * (this.height - 50 - 50 - scrollbarHeight) / maxScroll + 50).toInt()
                if (scrollbarPosY < 50) {
                    scrollbarPosY = 50
                }

                //#if MC >= 1.20.1
                //$$ guiGraphics.fill(this.scrollbarPosition, 50, scrollbarPosition + 2, this.height - 50, -16777216)
                //$$ guiGraphics.fill(this.scrollbarPosition, scrollbarPosY, scrollbarPosition + 2, scrollbarPosY + scrollbarHeight, -8355712)
                //$$ guiGraphics.fill(this.scrollbarPosition, scrollbarPosY, scrollbarPosition + 2 - 1, scrollbarPosY + scrollbarHeight - 1, -4144960)
                //#else
                fill(poseStack, this.scrollbarPosition, 50, scrollbarPosition + 2, this.height - 50, -16777216)
                fill(poseStack, this.scrollbarPosition, scrollbarPosY, scrollbarPosition + 2, scrollbarPosY + scrollbarHeight, -8355712)
                fill(poseStack, this.scrollbarPosition, scrollbarPosY, scrollbarPosition + 2 - 1, scrollbarPosY + scrollbarHeight - 1, -4144960)
                //#endif
            }

            doneButton.render(
                //#if MC >= 1.20.1
                //$$ guiGraphics,
                //#else
                poseStack,
                //#endif
                mouseX, mouseY, partialTick
            )

            UnityTranslateMCClient.renderCreditText(poseStack)
        }

        private fun <T> addArrows(x: Int, y: Int, index: Int, actualValue: MutableList<T>) {
            addRenderableWidget(
                //#if MC >= 1.20.4
                //$$ SpriteIconButton.builder(Component.empty(), {
                //#elseif MC >= 1.20.1
                //$$ TextAndImageButton.builder(Component.empty(), ARROW_UP) {
                //#else
                ImageButton(x, y - 8, 12, 12, 0, 0, 0, ARROW_UP, 8, 8) {
                //#endif
                    val oldValue = actualValue[index]
                    val oldPrevValue = actualValue[index + 1]
                    actualValue[index + 1] = oldValue
                    actualValue[index] = oldPrevValue
                    this.rebuildWidgets()
                //#if MC >= 1.20.4
                //$$ }, true)
                //#else
                }
                //#endif
                    //#if MC >= 1.20.4
                    //$$ .sprite(ARROW_UP, 8, 8)
                    //$$ .size(12, 12)
                    //$$ .build()
                    //#elseif MC >= 1.20.1
                    //$$ .offset(0, 2)
                    //$$ .texStart(0, 0)
                    //$$ .textureSize(8, 8)
                    //$$ .usedTextureSize(8, 8)
                    //$$ .build()
                    //#endif
                    .apply {
                        this.x = x
                        this.y = y - 8
                        this.width = 12
                        (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                        (this as ScrollableWidget).updateInitialPosition()

                        this.active = index > 0
                    }
            )

            addRenderableWidget(
                //#if MC >= 1.20.4
                //$$ SpriteIconButton.builder(Component.empty(), {
                //#elseif MC >= 1.20.1
                //$$ TextAndImageButton.builder(Component.empty(), ARROW_DOWN) {
                //#else
                ImageButton(x, y + 4, 12, 12, 0, 0, 0, ARROW_DOWN, 8, 8) {
                    //#endif
                    val oldValue = actualValue[index]
                    val oldPrevValue = actualValue[index + 1]
                    actualValue[index + 1] = oldValue
                    actualValue[index] = oldPrevValue
                    this.rebuildWidgets()
                //#if MC >= 1.20.4
                //$$ }, true)
                //#else
                }
                //#endif
                    //#if MC >= 1.20.4
                    //$$ .sprite(ARROW_DOWN, 8, 8)
                    //$$ .size(12, 12)
                    //$$ .build()
                    //#elseif MC >= 1.20.1
                    //$$ .offset(0, 2)
                    //$$ .texStart(0, 0)
                    //$$ .textureSize(8, 8)
                    //$$ .usedTextureSize(8, 8)
                    //$$ .build()
                    //#endif
                    .apply {
                        this.x = x
                        this.y = y + 4
                        this.width = 12
                        (this as AbstractWidgetAccessor).setHeight(12) // :mojank:
                        (this as ScrollableWidget).updateInitialPosition()

                        this.active = index < actualValue.size - 1

                    }
            )
        }

        fun updateScroll() {
            for (child in this.children()) {
                if (child == doneButton)
                    continue

                if (child is AbstractWidget) {
                    child.y = (child as ScrollableWidget).initialY - scrollAmount.toInt()
                }
            }
        }

        override fun resize(minecraft: Minecraft, width: Int, height: Int) {
            super.resize(minecraft, width, height)

            if (this.scrollAmount > this.maxScroll)
                this.scrollAmount = this.maxScroll.toDouble()

            updateScroll()
        }

        override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
            if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true
            } else if (button == 0 && scrolling) {
                if (mouseY < 50) {
                    this.scrollAmount = 0.0
                } else if (mouseY > this.height - 50) {
                    this.scrollAmount = this.maxScroll.toDouble()
                } else {
                    val max = this.maxScroll
                    val height = (this.height - 50) - 50
                    val scrollHeight = Mth.clamp(((height * height).toFloat() / this.maxPosition.toFloat()).toInt(), 32, height - 8)
                    val scrollDelta = (max / (height - scrollHeight).toDouble()).coerceAtMost(1.0)
                    this.scrollAmount = Mth.clamp(this.scrollAmount + dragY * scrollDelta, 0.0, this.maxScroll.toDouble())
                }

                updateScroll()
                return true
            }

            return false
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true
            }

            this.scrolling = button == 0 && mouseX >= this.scrollbarPosition && mouseX < this.scrollbarPosition + 6

            if (mouseY > 50 && mouseY <= this.height - 50) {
                return this.scrolling
            }

            return false
        }

        override fun mouseScrolled(mouseX: Double, mouseY: Double,
            //#if MC >= 1.20.4
            //$$ scrollX: Double, delta: Double
            //#else
                                   delta: Double
            //#endif
        ): Boolean {
            this.scrollAmount = Mth.clamp(this.scrollAmount - delta * (this.maxPosition / 4.0), 0.0, this.maxScroll.toDouble())
            updateScroll()
            return true
        }

        override fun onClose() {
            Minecraft.getInstance().setScreen(this@UTConfigScreen)
            UnityTranslate.instance.saveConfig()
            UnityTranslateMCClient.instance.saveConfig()
            UnityTranslateMCClient.instance.updateConfig()
        }
    }
    
    //#if MC < 1.20.1
    object Tooltip {
        fun create(text: Component): Component {
            return text
        }
    }
    //#endif

    private fun String.propercase(): String {
        return "${this[0].uppercaseChar()}${this.lowercase().substring(1)}"
    }
}