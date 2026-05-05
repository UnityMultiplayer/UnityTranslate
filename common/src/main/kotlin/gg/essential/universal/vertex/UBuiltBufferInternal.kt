package gg.essential.universal.vertex

import com.mojang.blaze3d.vertex.MeshData

internal interface UBuiltBufferInternal : UBuiltBuffer {
    val mc: MeshData
    fun closedExternally()
}
