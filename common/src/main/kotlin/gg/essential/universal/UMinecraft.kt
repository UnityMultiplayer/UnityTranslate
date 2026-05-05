package gg.essential.universal

import com.mojang.blaze3d.Blaze3D
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.player.LocalPlayer

object UMinecraft {
    private var guiScaleValue: Int
        get() = getSettings().guiScale().get()
        set(value) { getSettings().guiScale().set(value) }

    @JvmStatic
    var guiScale: Int
        get() = guiScaleValue
        set(value) {
            guiScaleValue = value
        }

    @JvmField
    val isRunningOnMac: Boolean =
        net.minecraft.client.input.InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY

    @JvmStatic
    fun getMinecraft(): Minecraft {
        return Minecraft.getInstance()
    }

    @JvmStatic
    fun getWorld(): ClientLevel? {
        return getMinecraft().level
    }

    @JvmStatic
    fun getNetHandler(): ClientPacketListener? {
        return getMinecraft().connection
    }

    @JvmStatic
    fun getPlayer(): LocalPlayer? {
        return getMinecraft().player
    }

    @JvmStatic
    fun getFontRenderer(): Font {
        return getMinecraft().font
    }

    @JvmStatic
    fun getTime(): Long {
        return (Blaze3D.getTime() * 1000).toLong()
    }

    @JvmStatic
    fun getChatGUI(): ChatComponent? =
        getMinecraft().gui.chat

    @JvmStatic
    fun getSettings(): Options = getMinecraft().options

    @JvmStatic
    var currentScreenObj: Any?
        get() = getMinecraft().screen
        set(value) = getMinecraft().setScreen(value as Screen?)


    @JvmStatic
    fun isCallingFromMinecraftThread(): Boolean {
        return Minecraft.getInstance().isSameThread
    }
}
