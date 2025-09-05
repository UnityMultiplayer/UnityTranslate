package xyz.bluspring.unitytranslate.common.holders

import java.util.UUID

data class PlayerHolder(
    val uuid: UUID,
    val ref: Any,
    val displayName: String
) {
    companion object {
        @JvmField
        val EMPTY = PlayerHolder(UUID(0L, 0L), Any(), "")
    }
}
