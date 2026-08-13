package xyz.bluspring.unitytranslate.api.v2.util

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * A nice helper for caching stuff that might be getting called frequently.
 */
object CacheUtils {
    private val cacheLookups = ConcurrentHashMap<Pair<Class<*>, Class<*>>, Cache<*, *>>()

    fun <T, U> createCacheLookup(first: Class<T>, second: Class<U>): Cache<T, U> {
        return this.cacheLookups.computeIfAbsent(first to second) {
            CacheBuilder.newBuilder()
                .expireAfterAccess(5.minutes.toJavaDuration())
                .concurrencyLevel(4)
                .maximumSize(10_000)
                .build<T, U>()
        } as Cache<T, U>
    }

    inline fun <reified T : Any, reified U : Any> T.buildOrCached(builder: T.() -> U): U {
        val cache = createCacheLookup(T::class.java, U::class.java)
        var value = cache.getIfPresent(this)
        if (value == null) {
            value = builder(this)
            cache.put(this, value)
        }

        return value
    }
}
