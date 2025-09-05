package xyz.bluspring.unitytranslate.gui.menu

import gg.essential.universal.UScreen
import java.util.Stack

object LayeredScreenManager {
    private val screens = Stack<UScreen>()

    fun open(screen: UScreen) {
        if (screens.isNotEmpty()) {
            val oldScreen = screens.peek()

            if (oldScreen is LayeredScreenListener) {
                oldScreen.onScreenLayerClosed()
            }
        }

        screens.push(screen)
        UScreen.displayScreen(screen)

        if (screen is LayeredScreenListener) {
            screen.onScreenLayerOpened()
        }
    }

    fun swap(screen: UScreen) {
        screens.pop()
        screens.push(screen)
        UScreen.displayScreen(screen)

        if (screen is LayeredScreenListener) {
            screen.onScreenLayerSwapped()
        }
    }

    fun close() {
        val oldScreen = screens.pop()

        if (oldScreen is LayeredScreenListener) {
            oldScreen.onScreenLayerClosed()
        }

        if (screens.isNotEmpty()) {
            val screen = screens.peek()

            UScreen.displayScreen(screen)
            if (screen is LayeredScreenListener) {
                screen.onScreenLayerOpened()
            }
        } else
            UScreen.displayScreen(null)
    }
}