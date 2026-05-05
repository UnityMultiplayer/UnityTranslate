package gg.essential.universal

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import gg.essential.universal.UMatrixStack.Compat.get
import net.minecraft.util.Mth
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import java.nio.FloatBuffer
import java.util.*

/**
 * A stack of matrices which can be manipulated via common transformations, just like MC's MatrixStack.
 *
 * For MC versions 1.16 and above, methods exist to convert from (via the constructor) and to (via Entry.toMCStack) the
 * vanilla stack type if required.
 * For MC versions below 1.17, the *GlobalState methods can be used to transfer the state of this matrix stack into the
 * global GL state. For 1.17, they transfer state into Mojang's global MatrixStack in RenderSystem.
 */
class UMatrixStack private constructor(
    private val stack: Deque<Entry>
) {

    constructor() : this(ArrayDeque<Entry>().apply {
        add(Entry(
            Matrix4f().apply { identity() },
            Matrix3f().apply { identity() }
        ))
    })

    constructor(mc: PoseStack) : this(mc.last())
    constructor(mc: PoseStack.Pose) : this(ArrayDeque<Entry>().apply {
        add(Entry(mc.pose(), mc.normal()))
    })
    fun toMC() = peek().toMCStack()

    constructor(mc: org.joml.Matrix3x2f) : this() {
        peek().model.apply {
            m00(mc.m00)
            m01(mc.m01)
            m10(mc.m10)
            m11(mc.m11)
            m30(mc.m20)
            m31(mc.m21)
        }
    }
    fun to3x2Joml(dst: org.joml.Matrix3x2f = org.joml.Matrix3x2f()): org.joml.Matrix3x2f {
        val uc = peek().model
        dst.set(uc.m00(), uc.m01(), uc.m10(), uc.m11(), uc.m30(), uc.m31())
        return dst
    }

    fun translate(x: Double, y: Double, z: Double) = translate(x.toFloat(), y.toFloat(), z.toFloat())

    fun translate(x: Float, y: Float, z: Float) {
        if (x == 0f && y == 0f && z == 0f) return
        stack.last.run {
            model.translate(x, y, z)
        }
    }

    fun scale(x: Double, y: Double, z: Double) = scale(x.toFloat(), y.toFloat(), z.toFloat())

    fun scale(x: Float, y: Float, z: Float) {
        if (x == 1f && y == 1f && z == 1f) return
        return stack.last.run {
            model.scale(x, y, z)
            if (x == y && y == z) {
                if (x < 0f) {
                    normal.scale(-1f)
                }
            } else {
                val ix = 1f / x
                val iy = 1f / y
                val iz = 1f / z
                val rt = Mth.fastInvCubeRoot(ix * iy * iz)
                normal.scale(rt * ix, rt * iy, rt * iz)
            }
        }
    }

    @JvmOverloads
    fun rotate(angle: Float, x: Float, y: Float, z: Float, degrees: Boolean = true) {
        if (angle == 0f) return
        stack.last.run {
            val angleRadians = if (degrees) Math.toRadians(angle.toDouble()).toFloat() else angle
            multiply(Quaternionf().rotateAxis(angleRadians, x, y, z))
        }
    }

    fun multiply(quaternion: Quaternionf) {
        stack.last.run {
            model.rotate(quaternion)
            normal.rotate(quaternion)
        }
    }

    fun fork() = UMatrixStack(ArrayDeque<Entry>().apply {
        add(stack.last.deepCopy())
    })

    fun push(): Unit = stack.addLast(stack.last.deepCopy())

    fun pop() {
        stack.removeLast()
    }

    fun peek(): Entry = stack.last

    fun isEmpty(): Boolean = stack.size == 1

    fun applyToGlobalState() {
        RenderSystem.getModelViewStack().mul(stack.last.model)
    }

    fun replaceGlobalState() {
        RenderSystem.getModelViewStack().identity()
        applyToGlobalState()
    }

    fun runWithGlobalState(block: Runnable) = runWithGlobalState { block.run() }

    fun <R> runWithGlobalState(block: () -> R): R  = withGlobalStackPushed {
        applyToGlobalState()
        block()
    }

    fun runReplacingGlobalState(block: Runnable) = runReplacingGlobalState { block.run() }

    fun <R> runReplacingGlobalState(block: () -> R): R = withGlobalStackPushed {
        replaceGlobalState()
        block()
    }

    private inline fun <R> withGlobalStackPushed(block: () -> R) : R {
        val stack = RenderSystem.getModelViewStack()
        stack.pushMatrix()
        return block().also {
            stack.popMatrix()
        }
    }

    data class Entry(val model: Matrix4f, val normal: Matrix3f) {
        fun toMCStack() = PoseStack().also {
            it.last().pose().mul(model)
            it.last().normal().mul(normal)
        }

        fun deepCopy() =
            Entry(Matrix4f(model), Matrix3f(normal))

        /**
         * Returns the model matrix in row-major order.
         */
        val modelAsArray: FloatArray
            get() = with(model) {
                FloatArray(16).also { get(FloatBuffer.wrap(it)) }
            }
    }

    object Compat {
        const val DEPRECATED = """For 1.17 this method requires you pass a UMatrixStack as the first argument.

If you are currently extending this method, you should instead extend the method with the added argument.
Note however for this to be non-breaking, your parent class needs to transition before you do.

If you are calling this method and you cannot guarantee that your target class has been fully updated (such as when
calling an open method on an open class), you should instead call the method with the "Compat" suffix, which will
call both methods, the new and the deprecated one.
If you are sure that your target class has been updated (such as when calling the super method), you should
(for super calls you must!) instead just call the method with the original name and added argument."""

        private val stack = mutableListOf<UMatrixStack>()

        /**
         * To preserve backwards compatibility with old subclasses of UScreen or similar hierarchies,
         * this method allows one to sneak in an artificial matrix stack argument when calling the legacy method
         * which can then later be retrieved via [get] when the base legacy method calls the new one.
         *
         * For an example see [UScreen.onDrawScreenCompat].
         */
        fun <R> runLegacyMethod(matrixStack: UMatrixStack, block: () -> R): R {
            stack.add(matrixStack)
            return block().also {
                stack.removeAt(stack.lastIndex)
            }
        }

        fun get(): UMatrixStack = stack.lastOrNull() ?: UMatrixStack()
    }

    companion object {

        /**
         * Represents an empty matrix stack. That is, a stack with the identity matrix as its sole entry.
         *
         * This stack may be passed to consuming APIs which may then assume that the stack is in fact a unit stack and
         * can therefore skip math that would be redundant in such cases.
         *
         * **This stack must not be modified.**
         * Consumers may compare this stack by reference and ignore its content.
         * Consumers which are not aware of this stack must still behave correctly, so its content must be correct.
         * [fork] is fine, [push] is not!
         */
        @JvmField
        val UNIT = UMatrixStack()
    }
}