package xyz.bluspring.unitytranslate.mixin.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.unitytranslate.common.network.PacketIds;
import xyz.bluspring.unitytranslate.common.network.UTPacket;
import xyz.bluspring.unitytranslate.minecraft.mc12006.UTModernNetworkHelper;

@Mixin(UTPacket.class)
public interface UTPacketMixin extends CustomPacketPayload {
    @Override
    default Type<? extends CustomPacketPayload> type() {
        return UTModernNetworkHelper.INSTANCE.getDefinitionToTypeMap().get(PacketIds.INSTANCE.getPacketDefinition((UTPacket) this));
    }
}
