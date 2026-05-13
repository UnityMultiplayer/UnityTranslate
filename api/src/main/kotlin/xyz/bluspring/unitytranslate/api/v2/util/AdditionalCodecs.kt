package xyz.bluspring.unitytranslate.api.v2.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import java.util.*

object AdditionalCodecs {
    /**
     * A color codec that allows both an ARGB integer and an RGB or ARGB int array.
     */
    @JvmField val COLOR_ARGB: Codec<Int> = Codec.withAlternative(
        Codec.INT,
        Codec.INT.listOf(3, 4) // ARGB
            .xmap({
                if (it.size == 3)
                    ARGBHelper.color(ARGBHelper.MAX_COMPONENT_SIZE, it[0], it[1], it[2])
                else
                    ARGBHelper.color(it[0], it[1], it[2], it[3])
            }, {
                listOf(
                    ARGBHelper.alpha(it),
                    ARGBHelper.red(it),
                    ARGBHelper.green(it),
                    ARGBHelper.blue(it),
                )
            })
    )

    @JvmStatic
    fun <E : Enum<E>> enumCodec(nameGetter: (String) -> E): Codec<E> {
        return Codec.STRING.comapFlatMap({ name ->
            try {
                DataResult.success(nameGetter(name.uppercase()))
            } catch (e: Throwable) {
                DataResult.error { "Could not get enum by name $name: ${e.message}" }
            }
        }, { value ->
            value.name.lowercase()
        })
    }

    @JvmStatic
    fun <T : Any> Codec<T>.optionalFieldOf(name: String, defaultGetter: () -> T): MapCodec<T> {
        return Codec.optionalField(name, this, false)
            .xmap({ it.orElse(defaultGetter()) }, Optional<T>::of)
    }
}
