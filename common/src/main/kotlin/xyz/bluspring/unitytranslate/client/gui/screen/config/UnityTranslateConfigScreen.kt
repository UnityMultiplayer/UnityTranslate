package xyz.bluspring.unitytranslate.client.gui.screen.config

import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

class UnityTranslateConfigScreen : UTScreen() {
    init {
        // idea: plugins add configs into here
        section("unitytranslate") {
            category("gui") {
                category("default_box_settings") {
                    intColor("text")
                    intColor("shadow")
                    slider("corner_radius", min = 0f, max = 16f, step = 0.1f)
                    entry(PaddingEntry())

                    category("outline") {
                        color("color")
                        slider("thickness", min = 0f, max = 128f, step = 0.5f)
                    }

                    category("background") {
                        entry(BackgroundConfigEntry())
                    }

                    category("header") {
                        entry(HeaderConfigEntry())
                    }

                    category("transcript_display") {
                        entry(TranscriptDisplayEntry())
                    }
                }

                button("transcript_boxes", "configure", openTranscriptBoxesScreen())
                slider("gui_scale", min = 0.0, max = 10.0, step = 0.1) // 0 = MC default
            }

            category("languages") {
                languageSelect("spoken")
            }

            category("transcriber") {

            }
        }

        section("integration") {

        }
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {

    }
}
