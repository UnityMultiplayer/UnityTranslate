package xyz.bluspring.unitytranslate.util

class Event<T>(clazz: Class<T>, private val invokerFactory: (List<T>) -> T) {
    private val handlers = mutableListOf<T>()

    fun invoker(): T {
        return invokerFactory.invoke(handlers)
    }

    fun register(handler: T) {
        handlers.add(handler)
    }
}