package gg.essential.universal.shader

import com.mojang.blaze3d.platform.DestFactor
import com.mojang.blaze3d.platform.SourceFactor
import gg.essential.universal.UGraphics
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL20

data class BlendState(
    val equation: Equation,
    val srcRgb: Param,
    val dstRgb: Param,
    val srcAlpha: Param = srcRgb,
    val dstAlpha: Param = dstRgb,
    val enabled: Boolean = true,
) {
    val separate = srcRgb != srcAlpha || dstRgb != dstAlpha

    @Deprecated("No longer supported on 1.21.5+, see UGraphics.Globals docs")
    fun activate() = applyState()

    internal fun activateWithoutChangingMcBlendState() = applyState()

    @Suppress("DEPRECATION")
    private fun applyState() {
        if (enabled) {
            UGraphics.enableBlend()
        } else {
            UGraphics.disableBlend()
        }
        UGraphics.blendEquation(equation.glId)
        UGraphics.tryBlendFuncSeparate(srcRgb.glId, dstRgb.glId, srcAlpha.glId, dstAlpha.glId)
    }

    companion object {
        @JvmField
        val DISABLED = BlendState(Equation.ADD, Param.ONE, Param.ZERO, enabled = false)
        @JvmField
        @Deprecated("Produces incorrect results when rendering on a non-opaque background.", ReplaceWith("ALPHA"))
        val NORMAL = BlendState(Equation.ADD, Param.SRC_ALPHA, Param.ONE_MINUS_SRC_ALPHA)
        @JvmField
        val ALPHA = BlendState(Equation.ADD, Param.SRC_ALPHA, Param.ONE_MINUS_SRC_ALPHA, Param.ONE, Param.ONE_MINUS_SRC_ALPHA)
        @JvmField
        val PREMULTIPLIED_ALPHA = BlendState(Equation.ADD, Param.ONE, Param.ONE_MINUS_SRC_ALPHA)

        @JvmStatic
        fun active() = BlendState(
            Equation.fromGl(GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB)) ?: Equation.ADD,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB)) ?: Param.ONE,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_DST_RGB)) ?: Param.ZERO,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA)) ?: Param.ONE,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)) ?: Param.ZERO,
            GL11.glGetBoolean(GL11.GL_BLEND),
         )
    }

    enum class Equation(internal val mcStr: String, internal val glId: Int) {
        ADD("add", GL14.GL_FUNC_ADD),
        SUBTRACT("subtract", GL14.GL_FUNC_SUBTRACT),
        REVERSE_SUBTRACT("reverse_subtract", GL14.GL_FUNC_REVERSE_SUBTRACT),
        MIN("min", GL14.GL_MIN),
        MAX("max", GL14.GL_MAX),
        ;

        companion object {
            private val byGlId = values().associateBy { it.glId }
            @JvmStatic
            fun fromGl(glId: Int) = byGlId[glId]
        }
    }

    enum class Param(internal val mcStr: String, internal val glId: Int) {
        ZERO("0", GL11.GL_ZERO),
        ONE("1", GL11.GL_ONE),
        SRC_COLOR("srccolor", GL11.GL_SRC_COLOR),
        ONE_MINUS_SRC_COLOR("1-srccolor", GL11.GL_ONE_MINUS_SRC_COLOR),
        DST_COLOR("dstcolor", GL11.GL_DST_COLOR),
        ONE_MINUS_DST_COLOR("1-dstcolor", GL11.GL_ONE_MINUS_DST_COLOR),
        SRC_ALPHA("srcalpha", GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA("1-srcalpha", GL11.GL_ONE_MINUS_SRC_ALPHA),
        DST_ALPHA("dstalpha", GL11.GL_DST_ALPHA),
        ONE_MINUS_DST_ALPHA("1-dstalpha", GL11.GL_ONE_MINUS_DST_ALPHA),
        ;

        internal val mcSourceFactor: SourceFactor
            get() = when (this) {
                ZERO -> SourceFactor.ZERO
                ONE -> SourceFactor.ONE
                SRC_COLOR -> SourceFactor.SRC_COLOR
                ONE_MINUS_SRC_COLOR -> SourceFactor.ONE_MINUS_SRC_COLOR
                DST_COLOR -> SourceFactor.DST_COLOR
                ONE_MINUS_DST_COLOR -> SourceFactor.ONE_MINUS_DST_COLOR
                SRC_ALPHA -> SourceFactor.SRC_ALPHA
                ONE_MINUS_SRC_ALPHA -> SourceFactor.ONE_MINUS_SRC_ALPHA
                DST_ALPHA -> SourceFactor.DST_ALPHA
                ONE_MINUS_DST_ALPHA -> SourceFactor.ONE_MINUS_DST_ALPHA
            }
        internal val mcDestFactor: DestFactor
            get() = when (this) {
                ZERO -> DestFactor.ZERO
                ONE -> DestFactor.ONE
                SRC_COLOR -> DestFactor.SRC_COLOR
                ONE_MINUS_SRC_COLOR -> DestFactor.ONE_MINUS_SRC_COLOR
                DST_COLOR -> DestFactor.DST_COLOR
                ONE_MINUS_DST_COLOR -> DestFactor.ONE_MINUS_DST_COLOR
                SRC_ALPHA -> DestFactor.SRC_ALPHA
                ONE_MINUS_SRC_ALPHA -> DestFactor.ONE_MINUS_SRC_ALPHA
                DST_ALPHA -> DestFactor.DST_ALPHA
                ONE_MINUS_DST_ALPHA -> DestFactor.ONE_MINUS_DST_ALPHA
            }

        companion object {
            private val byGlId = values().associateBy { it.glId }
            @JvmStatic
            fun fromGl(glId: Int) = byGlId[glId]
        }
    }
}