package gg.essential.universal

import gg.essential.universal.wrappers.UPlayer
import net.minecraft.network.chat.Component
import java.util.regex.Pattern

object UChat {
    private val ampersandPattern = Pattern.compile("(?<!\\\\)&(?![^0-9a-fklmnor]|$)")

    /**
     * Prints a message to chat. Accepts a String, UMessage, UTextComponent,
     * or any version-specific text component. If the object is of an unrecognized type,
     * it's toString() method will be called.
     *
     * This function is client side.
     */
    @JvmStatic
    fun chat(obj: Any) {
        if (!UPlayer.hasPlayer()) return
        UPlayer.sendClientSideMessage(obj as? Component ?: Component.literal(obj.toString()))
    }

    /**
     * Prints a message to the action bar. Accepts a String, UMessage,
     * UTextComponent, or any version-specific text component. If the
     * object is of an unrecognized type, it's toString() method will be called.
     *
     * This function is client side.
     */
    @JvmStatic
    fun actionBar(obj: Any) {
        UPacket.sendActionBarMessage(obj as? Component ?: Component.literal(obj.toString()))
    }

    /**
     * Sends a String to the server.
     */
    @JvmStatic
    fun say(text: String) {
        UPlayer.getPlayer()!!.connection.sendChat(text)
    }

    /**
     * Replaces ampersand color codes with section symbol color codes
     */
    @JvmStatic
    fun addColor(message: String): String {
        return ampersandPattern.matcher(message).replaceAll("\u00a7")
    }
}
