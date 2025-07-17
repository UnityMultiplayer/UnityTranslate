package xyz.bluspring.unitytranslate.common.network

import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets

/*
UnityTranslate Network System

S -> C - SERVER_SUPPORT
C -> S - SET_USED_LANGUAGES
C -> S - SET_CURRENT_LANGUAGE

C -> S - SEND_TRANSCRIPT or SEND_CLIENT_TRANSCRIPT
S -> C - SEND_TRANSCRIPT

C -> S - TRANSLATE_SIGN
S -> C - TRANSLATE_SIGN

S -> C - MARK_INCOMPLETE

S -> C - SET_CLIENT_TRANSLATION
C -> S - SET_CLIENT_TRANSLATION (if no response, assume disabled.)

S -> C - SYNC_SERVER_CONFIG (sent only if the player has the permissions)
C -> S - SYNC_SERVER_CONFIG
 */

object PacketIds {
    // Current protocol version of UnityTranslate.
    // This should only be incremented if any major (breaking) changes have been made
    // to the overall packet structure of existing packets.
    // If new packets are added, they may be documented but must not increment the version.

    // Available versions:
    // (none) / 0 - UnityTranslate v0.1.x and later
    // 1 - UnityTranslate v0.2.x and later
    val definitions = listOf(V1Packets)
    @JvmField val PROTOCOL_VERSION = definitions.size - 1

    fun String.asPacketId(): String {
        return "${UnityTranslate.MOD_ID}:$this"
    }
}