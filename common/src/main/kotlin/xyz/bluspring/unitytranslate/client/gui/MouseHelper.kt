package xyz.bluspring.unitytranslate.client.gui

import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy

object MouseHelper {
    private val arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
    private val pointerCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR)
    private val horizontalResizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR)
    private val verticalResizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR)
    private val topLeftToBottomRightResizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR)
    private val topRightToBottomLeftResizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR)
    private val omniResizeCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_ALL_CURSOR)

    private var currentCursor = this.arrowCursor
    private var queuedCursor: Long? = null

    private fun setCursor(cursor: Long) {
        if (cursor == 0L)
            return

        this.queuedCursor = cursor
    }

    fun cursorToDefault() = this.setCursor(this.arrowCursor)
    fun cursorToPointer() = this.setCursor(this.pointerCursor)
    fun cursorToHorizontalResize() = this.setCursor(this.horizontalResizeCursor)
    fun cursorToVerticalResize() = this.setCursor(this.verticalResizeCursor)
    fun cursorToTopLeftToBottomRightResize() = this.setCursor(this.topLeftToBottomRightResizeCursor)
    fun cursorToTopRightToBottomLeftResize() = this.setCursor(this.topRightToBottomLeftResizeCursor)
    fun cursorToOmniResize() = this.setCursor(this.omniResizeCursor)

    fun tick() {
        var queued = this.queuedCursor
        if (queued == null && this.currentCursor != this.arrowCursor) {
            queued = this.arrowCursor
        }

        if (queued != null) {
            GLFW.glfwSetCursor(ClientPlatformProxy.instance.windowHandle, queued)
            this.currentCursor = queued
            this.queuedCursor = null
        }
    }

    fun close() {
        GLFW.glfwDestroyCursor(this.arrowCursor)
        GLFW.glfwDestroyCursor(this.pointerCursor)
        GLFW.glfwDestroyCursor(this.horizontalResizeCursor)
        GLFW.glfwDestroyCursor(this.verticalResizeCursor)
        GLFW.glfwDestroyCursor(this.topLeftToBottomRightResizeCursor)
        GLFW.glfwDestroyCursor(this.topRightToBottomLeftResizeCursor)
        GLFW.glfwDestroyCursor(this.omniResizeCursor)
    }
}
