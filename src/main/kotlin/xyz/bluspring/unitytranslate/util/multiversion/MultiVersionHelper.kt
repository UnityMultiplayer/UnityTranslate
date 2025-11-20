package xyz.bluspring.unitytranslate.util.multiversion

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

fun emptyJustInCase() {}

//? if >= 1.21.9 {
/*val ServerPlayer.server: MinecraftServer
    get() {
        return this.level().server
    }
*///?}

//? if >= 1.21.8 {
/*fun ServerPlayer.serverLevel(): ServerLevel {
    return this.level()
}
*///?}