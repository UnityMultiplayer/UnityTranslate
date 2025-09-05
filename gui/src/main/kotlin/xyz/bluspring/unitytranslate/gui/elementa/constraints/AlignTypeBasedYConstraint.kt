package xyz.bluspring.unitytranslate.gui.elementa.constraints

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.YConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import xyz.bluspring.unitytranslate.gui.config.UnityTranslateClientConfig

class AlignTypeBasedYConstraint(val alignType: UnityTranslateClientConfig.VerticalAlignType, val percentage: Float) : YConstraint {
    override var cachedValue = 0f
    override var constrainTo: UIComponent? = null
    override var recalculate = true

    override fun getYPositionImpl(component: UIComponent): Float {
        ensureConstrainedToWindow(component)

        val percent25 = constrainTo!!.getHeight() * 0.25f
        val percent50 = constrainTo!!.getHeight() * 0.50f

        return when (alignType) {
            UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE ->
                constrainTo!!.getTop() + (percent25 * percentage)
            UnityTranslateClientConfig.VerticalAlignType.CENTER ->
                constrainTo!!.getTop() + percent25 + (percent50 * percentage)
            UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE ->
                constrainTo!!.getBottom() - (percent25 * percentage)
        }
    }

    override fun visitImpl(
        visitor: ConstraintVisitor,
        type: ConstraintType
    ) {
        when (type) {
            ConstraintType.Y -> visitor.visitParent(ConstraintType.Y)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }

    private fun ensureConstrainedToWindow(component: UIComponent) {
        if (constrainTo == null)
            constrainTo = Window.of(component)
    }
}