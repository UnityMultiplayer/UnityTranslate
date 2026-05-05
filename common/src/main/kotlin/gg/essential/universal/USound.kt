package gg.essential.universal


import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents


object USound {
    fun playSoundStatic(event: SoundEvent, volume: Float, pitch: Float) {

        // sound handler can be null whenever switching devices or openal fails to
        // initialize the sound handler correctly, which is very common, so protect against that
        val soundHandler = UMinecraft.getMinecraft().soundManager ?: return

        PositionedSoundRecordFactory.makeRecord(event, volume, pitch)?.let { soundHandler.play(it) }
    }

    fun playSoundStatic(registryEntry: Holder<SoundEvent>, volume: Float, pitch: Float) {
        playSoundStatic(registryEntry.value(), volume, pitch)
    }

    @JvmOverloads
    fun playButtonPress(volume: Float = 0.25f) {
        playSoundStatic(SoundEvents.UI_BUTTON_CLICK, volume, 1.0f)
    }

    fun playExpSound() {
        playSoundStatic(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.25F, 1.0f)
    }

    fun playLevelupSound() {
        playSoundStatic(SoundEvents.PLAYER_LEVELUP, 0.25F, 1.0f)
    }

    fun playPlingSound() {
        playSoundStatic(SoundEvents.NOTE_BLOCK_PLING, 0.25F, 1.0f)
    }


}