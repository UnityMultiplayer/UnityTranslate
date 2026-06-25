package xyz.bluspring.unitytranslate.client.renderer.ui.texture

abstract class CloseableTextureReference(type: String) : TextureReference(type), AutoCloseable {
}
