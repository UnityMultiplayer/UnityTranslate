package gg.essential.universal.wrappers

import gg.essential.universal.UMinecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import java.util.*

object UPlayer {
    @JvmStatic
    fun getPlayer(): LocalPlayer? {
        return UMinecraft.getMinecraft().player
    }

    @JvmStatic
    fun hasPlayer() = getPlayer() != null


    @JvmStatic
    fun sendClientSideMessage(message: Component) {
//        UMinecraft.getMinecraft().gui.hud.chat.addClientSystemMessage(message)
    }

    @JvmStatic
    fun getUUID(): UUID {
        return UMinecraft.getMinecraft().user.profileId // misnamed, should not actually ever be null
    }

    @JvmStatic
    fun getPosX(): Double {
        return getPlayer()?.x
            ?: throw NullPointerException("UPlayer.getPosX() called with no existing Player")
    }

    @JvmStatic
    fun getPosY(): Double {
        return getPlayer()?.y
            ?: throw NullPointerException("UPlayer.getPosY() called with no existing Player")
    }

    @JvmStatic
    fun getPosZ(): Double {
        return getPlayer()?.z
            ?: throw NullPointerException("UPlayer.getPosZ() called with no existing Player")
    }

    @JvmStatic
    fun getPrevPosX(): Double {
        return getPlayer()?.xo
            ?: throw NullPointerException("UPlayer.getPrevPosX() called with no existing Player")
    }

    @JvmStatic
    fun getPrevPosY(): Double {
        return getPlayer()?.yo
            ?: throw NullPointerException("UPlayer.getPrevPosY() called with no existing Player")
    }

    @JvmStatic
    fun getPrevPosZ(): Double {
        return getPlayer()?.zo
            ?: throw NullPointerException("UPlayer.getPrevPosZ() called with no existing Player")
    }
}
