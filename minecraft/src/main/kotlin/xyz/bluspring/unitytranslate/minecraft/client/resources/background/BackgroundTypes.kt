package xyz.bluspring.unitytranslate.minecraft.client.resources.background

import com.mojang.serialization.Codec
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

object BackgroundTypes {
    val ID_TO_TYPE = ConcurrentHashMap<ResourceLocation, BackgroundType>()
    val REGISTRY = MappedRegistry<MapCodec<out BackgroundType>>(ResourceKey.createRegistryKey(MinecraftProxy.id("background_type")), Lifecycle.stable(), null)

    val EMPTY = Registry.register(REGISTRY, MinecraftProxy.id("empty"), EmptyBackgroundType.CODEC)
    val IMAGE = Registry.register(REGISTRY, MinecraftProxy.id("image"), ImageBackgroundType.CODEC)

    val CODEC: Codec<BackgroundType> = REGISTRY.byNameCodec()
        .dispatch(
            BackgroundType::type,
            Function { it.codec() }
        )

    fun init() {}
}