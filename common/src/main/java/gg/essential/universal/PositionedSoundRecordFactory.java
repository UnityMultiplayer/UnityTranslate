package gg.essential.universal;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

class PositionedSoundRecordFactory {
    public static SimpleSoundInstance makeRecord(SoundEvent event, float volume, float pitch) {
        return SimpleSoundInstance.forUI(event, pitch, volume);
    }
}
