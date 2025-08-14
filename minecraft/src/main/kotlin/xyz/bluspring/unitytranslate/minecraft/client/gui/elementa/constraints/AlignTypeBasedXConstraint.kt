package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.constraints

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.XConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig

class AlignTypeBasedXConstraint(val alignType: UnityTranslateClientConfig.HorizontalAlignType, val percentage: Float) : XConstraint {
    override var cachedValue = 0f
    override var constrainTo: UIComponent? = null
    override var recalculate = true

    override fun getXPositionImpl(component: UIComponent): Float {
        ensureConstrainedToWindow(component)

        val percent25 = constrainTo!!.getWidth() * 0.25f
        val percent50 = constrainTo!!.getWidth() * 0.50f

        return when (alignType) {
            UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE ->
                constrainTo!!.getLeft() + (percent25 * percentage)
            UnityTranslateClientConfig.HorizontalAlignType.CENTER ->
                constrainTo!!.getLeft() + percent25 + (percent50 * percentage)
            UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE ->
                constrainTo!!.getRight() - (percent25 * percentage)
        }
    }

    override fun visitImpl(
        visitor: ConstraintVisitor,
        type: ConstraintType
    ) {
        when (type) {
            ConstraintType.X -> visitor.visitParent(ConstraintType.X)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }

    private fun ensureConstrainedToWindow(component: UIComponent) {
        if (constrainTo == null)
            constrainTo = Window.of(component)
    }
}