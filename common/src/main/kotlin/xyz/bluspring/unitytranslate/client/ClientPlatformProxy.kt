package xyz.bluspring.unitytranslate.client

import java.util.*

interface ClientPlatformProxy {
    companion object {
        val instance: ClientPlatformProxy = ServiceLoader.load(ClientPlatformProxy::class.java).first()
    }
}
