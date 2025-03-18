package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0SetUsedLanguagesPacket
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import java.util.*

class EditTranscriptBoxesScreen(val boxes: MutableList<UnityTranslateClientConfig.TranscriptBoxConfig>, val parent: Screen? = null) : Screen(MinecraftProxy.literal("")) {
    val CLOSE_BUTTON = MinecraftProxy.id("textures/gui/close.png")
    var shouldDisableHudAfter = false

    private val arrowCursor: Long = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
    private var currentShape: Int = GLFW.GLFW_ARROW_CURSOR
    private var currentCursor: Long = arrowCursor

    override fun init() {
        if (!UnityTranslateMCClient.shouldRenderBoxes) {
            shouldDisableHudAfter = true
            UnityTranslateMCClient.shouldRenderBoxes = true
        }

        this.addRenderableWidget(
            ButtonBuilder(CommonComponents.GUI_DONE) {
                this.onClose()
            }
                .pos(this.width / 2 - (UTConfigScreen.BUTTON_WIDTH / 2), this.height - 50)
                .build()
        )

        this.addRenderableWidget(
            ButtonBuilder(MinecraftProxy.literal("+")) {
                Minecraft.getInstance().setScreen(LanguageSelectScreen(this, true))
            }
                .pos(this.width / 2 - (UTConfigScreen.BUTTON_WIDTH / 2) - UTConfigScreen.BUTTON_HEIGHT, this.height - 50)
                .width(UTConfigScreen.BUTTON_HEIGHT)
                .build()
        )
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)

        if (shouldDisableHudAfter)
            UnityTranslateMCClient.shouldRenderBoxes = false

        UnityTranslateMCClient.instance.saveConfig()
        UnityTranslateMCClient.instance.updateConfig()

        if (UnityTranslateMCClient.clientConfig.transcriptBoxes.isNotEmpty()) {
            val languages = UnityTranslateMCClient.clientConfig.transcriptBoxes.map { it.language }.toMutableList()

            if (!languages.contains(UnityTranslateMCClient.clientConfig.spokenLanguage)) {
                languages.add(UnityTranslateMCClient.clientConfig.spokenLanguage)
            }

            UnityTranslate.instance.proxy.sendPacketClient(V0SetUsedLanguagesPacket(EnumSet.copyOf(languages)))
        }

        // make sure that the cursor is reset
        GLFW.glfwSetCursor(this.minecraft!!.window.window, arrowCursor)
    }

    private var boxEditContext: BoxEditContext? = null

    private fun assignCursor(shape: Int): Long {
        if (shape == GLFW.GLFW_ARROW_CURSOR)
            return arrowCursor

        if (currentShape != shape) {
            val cursor = GLFW.glfwCreateStandardCursor(shape)

            if (currentCursor != arrowCursor)
                GLFW.glfwDestroyCursor(currentCursor)

            currentCursor = cursor
            currentShape = shape
        }

        return currentCursor
    }

    val screenWidth: Int
        get() = Minecraft.getInstance().window.guiScaledWidth
    val screenHeight: Int
        get() = Minecraft.getInstance().window.guiScaledHeight


    override fun render(
        //#if MC >= 1.20.1
        //$$ guiGraphics: GuiGraphics,
        //#else
        poseStack: PoseStack,
        mouseX: Int, mouseY: Int, partialTick: Float) {
        var inAnyBox = false

        if (Minecraft.getInstance().player == null) { // assume user is currently configuring in the config screen
            //#if MC >= 1.20.4
            //$$ this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
            //#elseif MC >= 1.20.1
            //$$ this.renderBackground(guiGraphics)
            //#else
            this.renderBackground(poseStack)
            //#endif

            UnityTranslateMCClient.transcriptRenderer.render(poseStack)
        }

        if (this.children().none { it.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) }) {
            val widthDiv = screenWidth / 3
            val heightDiv = screenHeight / 3

            for (box in boxes) {
                val minX = when (box.horizontalAlignType) {
                    UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE -> (box.offsetX * widthDiv)
                    UnityTranslateClientConfig.HorizontalAlignType.CENTER -> (box.offsetX * widthDiv) + (screenWidth / 2)
                    UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE -> screenWidth - (box.offsetX * widthDiv)
                }.toInt()

                val minY = when (box.verticalAlignType) {
                    UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE -> box.offsetY * heightDiv
                    UnityTranslateClientConfig.VerticalAlignType.CENTER -> (box.offsetY * heightDiv) + (screenHeight / 2)
                    UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE -> screenHeight - (box.offsetY * heightDiv)
                }.toInt()

                val width = (box.width * screenWidth).toInt()
                val height = (box.height * screenHeight).toInt()
                
                if (mouseX >= minX - 1 && mouseY >= minY - 1 && mouseX <= minX + width + 1 && mouseY <= minY + height + 1) {
                    if (mouseX >= minX - 1 && mouseX <= minX + 1) {
                        if (mouseY >= minY - 1 && mouseY <= minY + 1) {
                            fill(poseStack, minX, minY - 1, minX + width, minY + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            //#if MC >= 1.20.1
                            //$$ GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR))
                            //#else
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                            //#endif
                        } else if (mouseY >= minY + height - 1 && mouseY <= minY + height + 1) {
                            fill(poseStack, minX, minY + height - 1, minX + width, minY + height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            //#if MC >= 1.20.1
                            //$$ GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NESW_CURSOR))
                            //#else
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                            //#endif
                        } else {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                        }

                        fill(poseStack, minX - 1, minY, minX + 1, minY + height, FastColor.ARGB32.color(255, 255, 255, 255))
                    } else if (mouseX >= minX + width - 1 && mouseX <= minX + width + 1) {
                        if (mouseY >= minY - 1 && mouseY <= minY + 1) {
                            fill(poseStack, minX, minY - 1, minX + width, minY + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            //#if MC >= 1.20.1
                            //$$ GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NESW_CURSOR))
                            //#else
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                            //#endif
                        } else if (mouseY >= minY + height - 1 && mouseY <= minY + height + 1) {
                            fill(poseStack, minX, minY + height - 1, minX + width, minY + height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                            //#if MC >= 1.20.1
                            //$$ GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR))
                            //#else
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                            //#endif
                        } else {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HRESIZE_CURSOR))
                        }

                        fill(poseStack, minX + width - 1, minY, minX + width + 1, minY + height, FastColor.ARGB32.color(255, 255, 255, 255))
                    } else if (mouseY >= minY - 1 && mouseY <= minY + 1) {
                        fill(poseStack, minX, minY - 1, minX + width, minY + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_VRESIZE_CURSOR))
                    } else if (mouseY >= minY + height - 1 && mouseY <= minY + height + 1) {
                        fill(poseStack, minX, minY + height - 1, minX + width, minY + height + 1, FastColor.ARGB32.color(255, 255, 255, 255))
                        GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_VRESIZE_CURSOR))
                    } else {
                        //guiGraphics.renderOutline(minX, minY, width, height, FastColor.ARGB32.color(255, 255, 255, 255))

                        val offset = 5
                        if (mouseX >= minX + offset + 1 && mouseY >= minY + offset + 1 && mouseX <= minX + offset + 16 && mouseY <= minY + offset + 16) {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, arrowCursor)
                            fill(poseStack, minX + offset, minY + offset, minX + offset + 16, minY + offset + 16, FastColor.ARGB32.color(95, 255, 0, 0))
                            //guiGraphics.renderOutline(minX + offset, minY + offset, 16, 16, FastColor.ARGB32.color(95, 255, 255, 255))
                        } else {
                            GLFW.glfwSetCursor(this.minecraft!!.window.window, assignCursor(GLFW.GLFW_HAND_CURSOR))
                        }

                        RenderSystem.setShaderTexture(0, CLOSE_BUTTON)
                        blit(poseStack, minX + offset, minY + offset, 0f, 0f, 16, 16, 16, 16)
                    }

                    inAnyBox = true
                    break
                }
            }
        }
        

        if (!inAnyBox) {
            GLFW.glfwSetCursor(this.minecraft!!.window.window, arrowCursor)
        }

        super.render(
            //#if MC >= 1.20.1
            //$$ guiGraphics,
            //#else
            poseStack,
            //#endif
            mouseX, mouseY, partialTick)

        UnityTranslateMCClient.renderCreditText(
            //#if MC >= 1.20.1
            //$$ guiGraphics
            //#else
            poseStack
            //#endif
        )
    }

    //#if MC >= 1.20.4
    //$$ override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    //$$     if (Minecraft.getInstance().player == null) {
    //$$         super.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
    //$$     }
    //$$ }
    //#endif

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)

        if (result)
            return true

        val widthDiv = screenWidth / 3
        val heightDiv = screenHeight / 3

        for ((index, box) in boxes.toList().withIndex()) {
            val minX = when (box.horizontalAlignType) {
                UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE -> (box.offsetX * widthDiv)
                UnityTranslateClientConfig.HorizontalAlignType.CENTER -> (box.offsetX * widthDiv) + (screenWidth / 2)
                UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE -> screenWidth - (box.offsetX * widthDiv)
            }.toDouble()

            val minY = when (box.verticalAlignType) {
                UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE -> box.offsetY * heightDiv
                UnityTranslateClientConfig.VerticalAlignType.CENTER -> (box.offsetY * heightDiv) + (screenHeight / 2)
                UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE -> screenHeight - (box.offsetY * heightDiv)
            }.toDouble()
            
            val width = (box.width * screenWidth).toDouble()
            val height = (box.height * screenHeight).toDouble()

            if (mouseX >= minX - 1 && mouseY >= minY - 1 && mouseX <= minX + width + 1 && mouseY <= minY + height + 1) {
                val offset = 5
                if (mouseX >= minX + offset + 1 && mouseY >= minY + offset + 1 && mouseX <= minX + offset + 16 && mouseY <= minY + offset + 16) {
                    boxes.removeAt(index)

                    return true
                }

                boxEditContext = if (mouseX >= minX - 1 && mouseX <= minX + 1) {
                    if (mouseY >= minY - 1 && mouseY <= minY + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.START_X, MoveMode.START_Y), mouseX, mouseY, minX, minY, width, height)
                    } else if (mouseY >= minY + height - 1 && mouseY <= minY + height + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.START_X, MoveMode.END_Y), mouseX, mouseY, minX, minY, width, height)
                    } else {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.START_X), mouseX, mouseY, minX, minY, width, height)
                    }
                } else if (mouseX >= minX + width - 1 && mouseX <= minX + width + 1) {
                    if (mouseY >= minY - 1 && mouseY <= minY + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.END_X, MoveMode.START_Y), mouseX, mouseY, minX, minY, width, height)
                    } else if (mouseY >= minY + height - 1 && mouseY <= minY + height + 1) {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.END_X, MoveMode.END_Y), mouseX, mouseY, minX, minY, width, height)
                    } else {
                        BoxEditContext(index, box, EnumSet.of(MoveMode.END_X), mouseX, mouseY, minX, minY, width, height)
                    }
                } else if (mouseY >= minY - 1 && mouseY <= minY + 1) {
                    BoxEditContext(index, box, EnumSet.of(MoveMode.START_Y), mouseX, mouseY, minX, minY, width, height)
                } else if (mouseY >= minY + height - 1 && mouseY <= minY + height + 1) {
                    BoxEditContext(index, box, EnumSet.of(MoveMode.END_Y), mouseX, mouseY, minX, minY, width, height)
                } else {
                    BoxEditContext(index, box, EnumSet.of(MoveMode.START_X, MoveMode.START_Y, MoveMode.END_X, MoveMode.END_Y), mouseX, mouseY, minX, minY, width, height)
                }

                if (boxEditContext!!.newX <= 0) {
                    boxEditContext!!.newX = 0.0
                }

                if (boxEditContext!!.newY <= 0) {
                    boxEditContext!!.newY = 0.0
                }

                if (boxEditContext!!.newWidth >= this.minecraft!!.window.guiScaledWidth) {
                    boxEditContext!!.newWidth = this.minecraft!!.window.guiScaledWidth.toDouble()
                }

                if (boxEditContext!!.newHeight >= this.minecraft!!.window.guiScaledHeight) {
                    boxEditContext!!.newHeight = this.minecraft!!.window.guiScaledHeight.toDouble()
                }

                updateTransforms(boxEditContext!!)

                return true
            }
        }

        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (boxEditContext != null) {
            boxEditContext = null
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mx: Double, my: Double) {
        val mouseX = Mth.clamp(mx, 0.0, this.minecraft!!.window.guiScaledWidth.toDouble())
        val mouseY = Mth.clamp(my, 0.0, this.minecraft!!.window.guiScaledHeight.toDouble())

        if (boxEditContext != null) {
            val ctx = boxEditContext!!
            val mode = ctx.mode

            if (mode.contains(MoveMode.START_X)) {
                ctx.newX += mouseX - ctx.lastMouseX
                ctx.newX = Mth.clamp(ctx.newX, 0.0, this.minecraft!!.window.guiScaledWidth.toDouble() - ctx.newWidth)

                if (!mode.contains(MoveMode.END_X)) {
                    ctx.newWidth -= mouseX - ctx.lastMouseX
                    ctx.newWidth = Mth.clamp(ctx.newWidth, 12.0, this.minecraft!!.window.guiScaledWidth.toDouble())
                }
            }

            if (mode.contains(MoveMode.END_X) && !mode.contains(MoveMode.START_X)) {
                ctx.newWidth += mouseX - ctx.lastMouseX
                ctx.newWidth = Mth.clamp(ctx.newWidth, 12.0, this.minecraft!!.window.guiScaledWidth.toDouble())
            }

            if (mode.contains(MoveMode.START_Y)) {
                ctx.newY += mouseY - ctx.lastMouseY
                ctx.newY = Mth.clamp(ctx.newY, 0.0, this.minecraft!!.window.guiScaledHeight.toDouble() - ctx.newHeight)

                if (!mode.contains(MoveMode.END_Y)) {
                    ctx.newHeight -= mouseY - ctx.lastMouseY
                    ctx.newHeight = Mth.clamp(ctx.newHeight, 12.0, this.minecraft!!.window.guiScaledHeight.toDouble())
                }
            }

            if (mode.contains(MoveMode.END_Y) && !mode.contains(MoveMode.START_Y)) {
                ctx.newHeight += mouseY - ctx.lastMouseY
                ctx.newHeight = Mth.clamp(ctx.newHeight, 12.0, this.minecraft!!.window.guiScaledHeight.toDouble())
            }

            updateTransforms(ctx)
            boxes[ctx.index] = ctx.box

            ctx.lastMouseX = mouseX
            ctx.lastMouseY = mouseY
        }
    }

    private fun updateTransforms(ctx: BoxEditContext) {
        val widthDiv = (screenWidth / 3).toDouble()
        val heightDiv = (screenHeight / 3).toDouble()

        // Fix broken values
        if (ctx.newWidth >= screenWidth)
            ctx.newWidth = screenWidth.toDouble()

        if (ctx.newX + ctx.newWidth >= screenWidth)
            ctx.newX = screenWidth - ctx.newWidth

        if (ctx.newX < 0)
            ctx.newX = 0.0

        if (ctx.newHeight >= screenHeight)
            ctx.newHeight = screenHeight.toDouble()

        if (ctx.newY + ctx.newHeight >= screenHeight)
            ctx.newHeight = screenHeight - ctx.newHeight

        if (ctx.newY < 0)
            ctx.newY = 0.0

        // Align all offsets
        if (ctx.newX >= widthDiv && ctx.newX <= screenWidth - widthDiv) { // Center align
            ctx.box.offsetX = (ctx.newX - (screenWidth / 2)) / widthDiv
            ctx.box.horizontalAlignType = UnityTranslateClientConfig.HorizontalAlignType.CENTER
        } else if (ctx.newX >= screenWidth - widthDiv) { // Right edge align
            ctx.box.offsetX = (ctx.newX - screenWidth) / widthDiv
            ctx.box.horizontalAlignType = UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE
        } else { // Left edge align
            ctx.box.offsetX = ctx.newX / widthDiv
            ctx.box.horizontalAlignType = UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE
        }

        if (ctx.newY >= heightDiv && ctx.newY <= screenHeight - heightDiv) { // Center align
            ctx.box.offsetY = (ctx.newY - (screenHeight / 2)) / heightDiv
            ctx.box.verticalAlignType = UnityTranslateClientConfig.VerticalAlignType.CENTER
        } else if (ctx.newY >= screenHeight - heightDiv) { // Bottom edge align
            ctx.box.offsetY = (ctx.newY - screenHeight) / heightDiv
            ctx.box.verticalAlignType = UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE
        } else { // Top edge align
            ctx.box.offsetY = ctx.newY / heightDiv
            ctx.box.verticalAlignType = UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE
        }

        ctx.box.width = ctx.newWidth / screenWidth
        ctx.box.height = ctx.newHeight / screenHeight
    }

    private data class BoxEditContext(
        val index: Int,
        val box: UnityTranslateClientConfig.TranscriptBoxConfig,
        val mode: EnumSet<MoveMode>,
        var lastMouseX: Double,
        var lastMouseY: Double,

        var newX: Double,
        var newY: Double,
        var newWidth: Double,
        var newHeight: Double
    )

    private enum class MoveMode {
        START_X, START_Y,
        END_X, END_Y
    }
}