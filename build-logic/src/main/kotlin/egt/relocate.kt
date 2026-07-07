package egt

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.AnnotationNode
import kotlin.metadata.*
import kotlin.metadata.jvm.*

internal class KotlinMetadataRemappingClassVisitor(private val remapper: Remapper, next: ClassVisitor?) : ClassVisitor(Opcodes.ASM9, next) {
    companion object {
        val ANNOTATION_DESCRIPTOR: String = Type.getDescriptor(Metadata::class.java)
    }

    var className: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        this.className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        var result: AnnotationVisitor? = super.visitAnnotation(descriptor, visible)

        if (descriptor == ANNOTATION_DESCRIPTOR && result != null) {
            try {
                result = KotlinClassMetadataRemappingAnnotationVisitor(remapper, result, className)
            } catch (e: Exception) {
                throw RuntimeException("Failed to remap Kotlin metadata annotation in class $className", e)
            }
        }

        return result
    }
}

@OptIn(ExperimentalContextReceivers::class)
class KotlinClassRemapper(private val remapper: Remapper) {
    fun remap(clazz: KmClass): KmClass {
        clazz.name = remap(clazz.name)
        clazz.typeParameters.replaceAll(this::remap)
        clazz.supertypes.replaceAll(this::remap)
        clazz.functions.replaceAll(this::remap)
        clazz.properties.replaceAll(this::remap)
        clazz.typeAliases.replaceAll(this::remap)
        clazz.constructors.replaceAll(this::remap)
        clazz.nestedClasses.replaceAll(this::remap)
        clazz.sealedSubclasses.replaceAll(this::remap)
        clazz.contextReceiverTypes.replaceAll(this::remap)
        clazz.localDelegatedProperties.replaceAll(this::remap)
        return clazz
    }

    fun remap(lambda: KmLambda): KmLambda {
        lambda.function = remap(lambda.function)
        return lambda
    }

    fun remap(pkg: KmPackage): KmPackage {
        pkg.functions.replaceAll(this::remap)
        pkg.properties.replaceAll(this::remap)
        pkg.typeAliases.replaceAll(this::remap)
        pkg.localDelegatedProperties.replaceAll(this::remap)
        return pkg
    }

    private fun remap(name: ClassName): ClassName {
        val local = name.isLocalClassName()
        val remapped = remapper.map(name.toJvmInternalName()).replace('$', '.')

        if (local) {
            return ".$remapped"
        }

        return remapped
    }

    private fun remap(type: KmType): KmType {
        type.classifier =
            when (val classifier = type.classifier) {
                is KmClassifier.Class -> KmClassifier.Class(remap(classifier.name))
                is KmClassifier.TypeParameter -> KmClassifier.TypeParameter(classifier.id)
                is KmClassifier.TypeAlias -> KmClassifier.TypeAlias(remap(classifier.name))
            }
        type.arguments.replaceAll(this::remap)
        type.abbreviatedType = type.abbreviatedType?.let { remap(it) }
        type.outerType = type.outerType?.let { remap(it) }
        type.flexibleTypeUpperBound = type.flexibleTypeUpperBound?.let { remap(it) }
        type.annotations.replaceAll(this::remap)
        return type
    }

    @OptIn(ExperimentalContextParameters::class)
    private fun remap(function: KmFunction): KmFunction {
        function.typeParameters.replaceAll(this::remap)
        function.receiverParameterType = function.receiverParameterType?.let { remap(it) }
        function.contextParameters.replaceAll(this::remap)
        function.valueParameters.replaceAll(this::remap)
        function.returnType = remap(function.returnType)
        function.signature = function.signature?.let { remap(it) }
        return function
    }

    @OptIn(ExperimentalContextParameters::class)
    private fun remap(property: KmProperty): KmProperty {
        property.typeParameters.replaceAll(this::remap)
        property.receiverParameterType = property.receiverParameterType?.let { remap(it) }
        property.contextParameters.replaceAll(this::remap)
        property.setterParameter = property.setterParameter?.let { remap(it) }
        property.returnType = remap(property.returnType)
        property.fieldSignature = property.fieldSignature?.let { remap(it) }
        property.getterSignature = property.getterSignature?.let { remap(it) }
        property.setterSignature = property.setterSignature?.let { remap(it) }
        property.syntheticMethodForAnnotations = property.syntheticMethodForAnnotations?.let { remap(it) }
        property.syntheticMethodForDelegate = property.syntheticMethodForDelegate?.let { remap(it) }
        return property
    }

    private fun remap(typeAlias: KmTypeAlias): KmTypeAlias {
        typeAlias.typeParameters.replaceAll(this::remap)
        typeAlias.underlyingType = remap(typeAlias.underlyingType)
        typeAlias.expandedType = remap(typeAlias.expandedType)
        typeAlias.annotations.replaceAll(this::remap)
        return typeAlias
    }

    private fun remap(constructor: KmConstructor): KmConstructor {
        constructor.valueParameters.replaceAll(this::remap)
        constructor.signature = constructor.signature?.let { remap(it) }
        return constructor
    }

    private fun remap(typeParameter: KmTypeParameter): KmTypeParameter {
        typeParameter.upperBounds.replaceAll(this::remap)
        typeParameter.annotations.replaceAll(this::remap)
        return typeParameter
    }

    private fun remap(typeProjection: KmTypeProjection): KmTypeProjection {
        return KmTypeProjection(typeProjection.variance, typeProjection.type?.let { remap(it) })
    }

    private fun remap(flexibleTypeUpperBound: KmFlexibleTypeUpperBound): KmFlexibleTypeUpperBound {
        return KmFlexibleTypeUpperBound(remap(flexibleTypeUpperBound.type), flexibleTypeUpperBound.typeFlexibilityId)
    }

    private fun remap(valueParameter: KmValueParameter): KmValueParameter {
        valueParameter.type = remap(valueParameter.type)
        valueParameter.varargElementType = valueParameter.varargElementType?.let { remap(it) }
        return valueParameter
    }

    private fun remap(annotation: KmAnnotation): KmAnnotation {
        return KmAnnotation(remap(annotation.className), annotation.arguments)
    }

    private fun remap(signature: JvmMethodSignature): JvmMethodSignature {
        return JvmMethodSignature(signature.name, remapper.mapMethodDesc(signature.descriptor))
    }

    private fun remap(signature: JvmFieldSignature): JvmFieldSignature {
        return JvmFieldSignature(signature.name, remapper.mapDesc(signature.descriptor))
    }
}

internal fun compatibleKotlinMetadataVersion(version: IntArray): JvmMetadataVersion {
    // Upgrade versions older than 1.4 to 1.4 in accordance with https://youtrack.jetbrains.com/issue/KT-41011
    if (version.size < 2 || version[0] < 1 || version[0] <= 1 && version[1] < 4) {
        return JvmMetadataVersion(1, 4)
    }
    return JvmMetadataVersion(version[0], version[1], version[2])
}

internal class KotlinClassMetadataRemappingAnnotationVisitor(private val remapper: Remapper, val next: AnnotationVisitor, val className: String?) :
    AnnotationNode(Opcodes.ASM9, KotlinMetadataRemappingClassVisitor.ANNOTATION_DESCRIPTOR) {

    override fun visit(
        name: String?,
        value: Any?,
    ) {
        super.visit(name, value)
    }

    override fun visitEnd() {
        super.visitEnd()

        val header = readHeader() ?: return
        val metadataVersion = compatibleKotlinMetadataVersion(header.metadataVersion)

        when (val metadata = KotlinClassMetadata.readLenient(header)) {
            is KotlinClassMetadata.Class -> {
                var klass = metadata.kmClass
                klass = KotlinClassRemapper(remapper).remap(klass)
                val remapped = KotlinClassMetadata.Class(klass, metadataVersion, metadata.flags).write()
                writeClassHeader(remapped)
            }
            is KotlinClassMetadata.SyntheticClass -> {
                var klambda = metadata.kmLambda

                if (klambda != null) {
                    klambda = KotlinClassRemapper(remapper).remap(klambda)
                    val remapped = KotlinClassMetadata.SyntheticClass(klambda, metadataVersion, metadata.flags).write()
                    writeClassHeader(remapped)
                } else {
                    accept(next)
                }
            }
            is KotlinClassMetadata.FileFacade -> {
                var kpackage = metadata.kmPackage
                kpackage = KotlinClassRemapper(remapper).remap(kpackage)
                val remapped = KotlinClassMetadata.FileFacade(kpackage, metadataVersion, metadata.flags).write()
                writeClassHeader(remapped)
            }
            is KotlinClassMetadata.MultiFileClassPart -> {
                var kpackage = metadata.kmPackage
                kpackage = KotlinClassRemapper(remapper).remap(kpackage)
                val remapped =
                    KotlinClassMetadata.MultiFileClassPart(
                        kpackage,
                        metadata.facadeClassName,
                        metadataVersion,
                        metadata.flags,
                    ).write()
                writeClassHeader(remapped)
            }
            is KotlinClassMetadata.MultiFileClassFacade, is KotlinClassMetadata.Unknown -> {
                // do nothing
                accept(next)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readHeader(): Metadata? {
        var kind: Int? = null
        var metadataVersion: IntArray? = null
        var data1: Array<String>? = null
        var data2: Array<String>? = null
        var extraString: String? = null
        var packageName: String? = null
        var extraInt: Int? = null

        if (values == null) {
            return null
        }

        values.chunked(2).forEach { (name, value) ->
            when (name) {
                "k" -> kind = value as Int
                "mv" -> metadataVersion = (value as List<Int>).toIntArray()
                "d1" -> data1 = (value as List<String>).toTypedArray()
                "d2" -> data2 = (value as List<String>).toTypedArray()
                "xs" -> extraString = value as String
                "pn" -> packageName = value as String
                "xi" -> extraInt = value as Int
            }
        }

        return Metadata(kind, metadataVersion, data1, data2, extraString, packageName, extraInt)
    }

    private fun writeClassHeader(header: Metadata) {
        val newNode = AnnotationNode(api, desc)
        newNode.values = this.values.toMutableList()

        newNode.run {
            for (i in values.indices step 2) {
                when (values[i]) {
                    "k" -> values[i + 1] = header.kind
                    "mv" -> values[i + 1] = header.metadataVersion.toList()
                    "d1" -> values[i + 1] = header.data1.toList()
                    "d2" -> values[i + 1] = header.data2.toList()
                    "xs" -> values[i + 1] = header.extraString
                    "pn" -> values[i + 1] = header.packageName
                    "xi" -> values[i + 1] = header.extraInt
                }
            }
        }

        newNode.accept(next)
    }
}
