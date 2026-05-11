package xyz.bluspring.unitytranslate.standalone.resources

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GlyphSource
import net.minecraft.client.gui.font.FontSet
import net.minecraft.client.gui.font.glyphs.EffectGlyph
import net.minecraft.network.chat.FontDescription

object FontManager : Font.Provider {
    override fun glyphs(font: FontDescription): GlyphSource {
        FontSet
        TODO("Not yet implemented")
    }

    override fun effect(): EffectGlyph {
        TODO("Not yet implemented")
    }
}