package gg.essential.universal.render

import java.nio.ByteBuffer


internal data class ScissorState(val enabled: Boolean, val x: Int, val y: Int, val width: Int, val height: Int) {
    fun activate() {
        active = this
    }

    companion object {
        val DISABLED = ScissorState(false, 0, 0, 0, 0)

        // MC no longer has a global scissor state, so we'll have our own until we've migrated away from it too
        private var active: ScissorState = DISABLED

        // Note: LWJGL2 requires a buffer of 16 elements, even if the property we query only has 4
        private val tmpIntBuffer = ByteBuffer.allocateDirect(16 * Int.SIZE_BYTES).asIntBuffer()

        fun active(): ScissorState {
            return active
        }
    }
}
