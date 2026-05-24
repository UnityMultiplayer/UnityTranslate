package xyz.bluspring.unitytranslate.api.v2.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.alpha
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.blue
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.green
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.red
import java.util.*

/**
 * Some additional codecs that may be useful.
 */
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
                    it.alpha(),
                    it.red(),
                    it.green(),
                    it.blue(),
                )
            })
    )

    /**
     * A quick and easy codec for handling enums.
     */
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

    /**
     * Represents the same behaviour as a normal [Codec.optionalFieldOf], except uses a getter for the default value
     * for any objects that may be mutable.
     */
    @JvmStatic
    fun <T : Any> Codec<T>.optionalFieldOf(name: String, defaultGetter: () -> T): MapCodec<T> {
        return Codec.optionalField(name, this, false)
            .xmap({ it.orElse(defaultGetter()) }, Optional<T>::of)
    }
}
