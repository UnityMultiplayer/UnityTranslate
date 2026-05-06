package xyz.bluspring.unitytranslate.standalone.launch

object FilteredClassLoader : ClassLoader() {
    init {
        registerAsParallelCapable()
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        if (
            (name.startsWith("xyz.bluspring.unitytranslate.") || name.startsWith("xyz/bluspring/unitytranslate/"))
                && !(name.startsWith("xyz.bluspring.unitytranslate.library.") || name.startsWith("xyz/bluspring/unitytranslate/library/"))
                && !(name.startsWith("xyz.bluspring.unitytranslate.transcriber.") || name.startsWith("xyz/bluspring/unitytranslate/transcriber/"))
        )
            throw ClassNotFoundException(name)

        return super.loadClass(name, resolve)
    }
}