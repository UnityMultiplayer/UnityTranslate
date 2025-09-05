package xyz.bluspring.unitytranslate.gui.menu

interface LayeredScreenListener {
    fun onScreenLayerOpened() {}
    fun onScreenLayerSwapped() {}
    fun onScreenLayerClosed() {}
}