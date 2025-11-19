package xyz.bluspring.unitytranslate.network.payloads

import io.netty.buffer.ByteBuf
import net.minecraft.network.FriendlyByteBuf
import xyz.bluspring.modernnetworking.api.*
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketDefinitions
import java.util.*

data class MarkIncompletePayload(
    val from: Language,
    val to: Language,
    val uuid: UUID,
    val index: Int,
    var isIncomplete: Boolean
) : NetworkPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = PacketDefinitions.MARK_INCOMPLETE

    companion object {
        val CODEC: NetworkCodec<MarkIncompletePayload, FriendlyByteBuf> = CompositeCodecs.composite(
            Language.NETWORK_CODEC, MarkIncompletePayload::from,
            Language.NETWORK_CODEC, MarkIncompletePayload::to,
            NetworkCodecs.UUID, MarkIncompletePayload::uuid,
            NetworkCodecs.VAR_INT, MarkIncompletePayload::index,
            NetworkCodecs.BOOL, MarkIncompletePayload::isIncomplete,

            ::MarkIncompletePayload
        )
    }
}