package xyz.bluspring.unitytranslate.api.v2

import net.minecraft.network.chat.MutableComponent
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
interface PlatformAccess {
    fun literal(text: String): MutableComponent
    fun translatable(key: String, vararg args: Any?): MutableComponent
}
