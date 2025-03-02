package xyz.bluspring.unitytranslate.minecraft

import com.moulberry.mixinconstraints.MixinConstraints
import com.moulberry.mixinconstraints.mixin.MixinConstraintsBootstrap
import org.objectweb.asm.tree.ClassNode
import org.reflections.Reflections
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class UnityTranslateMixinPlugin : IMixinConfigPlugin {
    lateinit var reflections: Reflections
    lateinit var mixinPackage: String

    override fun onLoad(mixinPackage: String) {
        this.mixinPackage = mixinPackage
        MixinConstraintsBootstrap.init(mixinPackage)
        reflections = Reflections(mixinPackage)
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String?): Boolean {
        return MixinConstraints.shouldApplyMixin(mixinClassName)
    }

    override fun acceptTargets(
        myTargets: Set<String?>?,
        otherTargets: Set<String?>?
    ) {
    }

    override fun getMixins(): List<String> {
        return reflections.getTypesAnnotatedWith(Mixin::class.java).map { it.canonicalName.removePrefix("$mixinPackage.") }
    }

    override fun preApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
    }

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
    }
}