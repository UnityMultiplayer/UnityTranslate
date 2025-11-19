package xyz.bluspring.unitytranslate

import net.minecraft.world.entity.player.Player

import java.nio.file.Path

interface PlatformProxy {
    fun isModLoaded(id: String): Boolean
    fun isClient(): Boolean

    val modVersion: String
    val configDir: Path
    val gameDir: Path
    val isDev: Boolean

    fun hasTranscriptPermission(player: Player): Boolean
}