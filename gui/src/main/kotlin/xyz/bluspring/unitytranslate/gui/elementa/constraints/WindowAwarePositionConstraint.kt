package xyz.bluspring.unitytranslate.gui.elementa.constraints

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

class WindowAwarePositionConstraint(private val wantedPos: Float = 0f, private val offset: Float = 0f) : PositionConstraint {
    override var cachedValue = 0f
    override var constrainTo: UIComponent? = null
    override var recalculate: Boolean = true

    override fun visitImpl(
        visitor: ConstraintVisitor,
        type: ConstraintType
    ) {
        //TODO("Not yet implemented")
    }

    override fun getXPositionImpl(component: UIComponent): Float {
        val window = Window.of(component)
        constrainTo = window

        return if (wantedPos + component.getWidth() + offset > window.getWidth()) {
            window.getWidth() - component.getWidth() - offset
        } else (wantedPos + offset)
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        val window = Window.of(component)
        constrainTo = window

        return if (wantedPos + component.getHeight() + offset > window.getHeight()) {
            window.getHeight() - component.getHeight() - offset
        } else (wantedPos + offset)
    }
}