package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.constraints

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.PaddingConstraint
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import java.lang.IllegalArgumentException

class CramAwareChildBasedSizeConstraint(val padding: Float = 0f) : SizeConstraint {
    override var cachedValue = 0f
    override var constrainTo: UIComponent? = null
    override var recalculate = true

    override fun getWidthImpl(component: UIComponent): Float {
        val holder = (constrainTo ?: component)
        var current = 0f

        for (child in holder.children) {
            val padding = (child.constraints.y as? PaddingConstraint)?.getHorizontalPadding(child) ?: 0f
            if (child.getRight() + padding > current) {
                current = child.getRight() + padding
            }
        }

        return (current - holder.getLeft() + (holder.children.size - 1).coerceAtLeast(0) * padding).coerceAtLeast(0f)
    }

    override fun getHeightImpl(component: UIComponent): Float {
        val holder = (constrainTo ?: component)
        var current = 0f

        for (child in holder.children) {
            val padding = (child.constraints.y as? PaddingConstraint)?.getVerticalPadding(child) ?: 0f
            if (child.getBottom() + padding > current) {
                current = child.getBottom() + padding
            }
        }

        return (current - holder.getTop() + (holder.children.size - 1).coerceAtLeast(0) * padding).coerceAtLeast(0f)
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return (constrainTo ?: component).children.sumOf { it.getHeight().toDouble() }.toFloat() * 2f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        when (type) {
            ConstraintType.WIDTH -> visitor.visitChildren(ConstraintType.WIDTH)
            ConstraintType.HEIGHT -> visitor.visitChildren(ConstraintType.HEIGHT)
            ConstraintType.RADIUS -> visitor.visitChildren(ConstraintType.HEIGHT)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}