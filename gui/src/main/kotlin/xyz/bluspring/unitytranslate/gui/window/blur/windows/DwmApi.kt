package xyz.bluspring.unitytranslate.gui.window.blur.windows

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.PointerType
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.ptr.IntByReference

interface DwmApi : Library {
    companion object {
        val INSTANCE: DwmApi = Native.load("dwmapi", DwmApi::class.java)

        const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20L
        const val DWMWA_SYSTEMBACKDROP_TYPE = 38L
        const val DWMWA_BORDER_COLOR = 34L

        fun setAcrylicBackground(hwnd: WinDef.HWND) {
            INSTANCE.DwmSetWindowAttribute(hwnd, WinDef.UINT(DWMWA_SYSTEMBACKDROP_TYPE), IntByReference(3), 4)
        }
    }

    fun DwmSetWindowAttribute(hwnd: WinDef.HWND, dwAttribute: WinDef.UINT, pvAttribute: PointerType, cbAttribute: Int): Int
}