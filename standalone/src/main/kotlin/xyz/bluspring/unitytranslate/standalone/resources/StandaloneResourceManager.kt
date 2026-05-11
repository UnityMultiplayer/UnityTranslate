package xyz.bluspring.unitytranslate.standalone.resources

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream

object StandaloneResourceManager : ResourceManager {
    override fun getNamespaces(): Set<String> {
        return setOf("minecraft", "unitytranslate")
    }

    override fun getResourceStack(location: Identifier): List<Resource> {
        TODO("Not yet implemented")
    }

    override fun listResources(
        directory: String,
        filter: Predicate<Identifier>
    ): Map<Identifier, Resource> {
        TODO("Not yet implemented")
    }

    override fun listResourceStacks(
        directory: String,
        filter: Predicate<Identifier>
    ): Map<Identifier, List<Resource>> {
        TODO("Not yet implemented")
    }

    override fun listPacks(): Stream<PackResources> {
        TODO("Not yet implemented")
    }

    override fun getResource(location: Identifier): Optional<Resource> {

    }
}
