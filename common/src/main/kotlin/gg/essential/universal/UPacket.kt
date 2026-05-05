package gg.essential.universal

import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket


object UPacket {
    @JvmStatic
    fun sendChatMessage(message: Component) {
        UMinecraft.getNetHandler()!!.handleSystemChat(ClientboundSystemChatPacket(
            message,
            false,
        ))
    }

    @JvmStatic
    fun sendActionBarMessage(message: Component) {
        UMinecraft.getNetHandler()!!.handleSystemChat(ClientboundSystemChatPacket(
            message,
            true,
        ))
    }
}
