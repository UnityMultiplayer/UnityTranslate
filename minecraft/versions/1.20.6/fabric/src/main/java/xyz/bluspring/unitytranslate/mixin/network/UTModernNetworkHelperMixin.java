package xyz.bluspring.unitytranslate.mixin.network;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.common.network.PacketBuilder;
import xyz.bluspring.unitytranslate.common.network.UTPacket;
import xyz.bluspring.unitytranslate.minecraft.mc12006.UTModernNetworkHelper;

import java.util.Map;

@Mixin(UTModernNetworkHelper.class)
@IfModLoaded("fabric-networking-api-v1")
public abstract class UTModernNetworkHelperMixin {
    @Shadow
    public abstract Map<PacketBuilder<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> getDefinitionToCodecMap();

    @Shadow
    public abstract Map<PacketBuilder<?>, CustomPacketPayload.Type<?>> getDefinitionToTypeMap();

    @Dynamic
    @Inject(method = "init", at = @At("TAIL"))
    private void registerPacketsFabric(CallbackInfo ci) {
        getDefinitionToTypeMap().forEach((definition, type) -> {
            var codec = this.getDefinitionToCodecMap().get(definition);

            if (definition.getDirection() != PacketBuilder.Direction.CLIENTBOUND) { // serverbound
                PayloadTypeRegistry.playC2S().register((CustomPacketPayload.Type) type, (StreamCodec) codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (packet, ctx) -> {
                    ((UTPacket) packet).handleServer(ctx.player().getUUID());
                });
            }

            if (definition.getDirection() != PacketBuilder.Direction.SERVERBOUND) { // serverbound
                PayloadTypeRegistry.playS2C().register((CustomPacketPayload.Type) type, (StreamCodec) codec);
            }
        });
    }

    @Environment(EnvType.CLIENT)
    @Dynamic
    @Inject(method = "init", at = @At("TAIL"))
    private void registerClientPacketsFabric(CallbackInfo ci) {
        getDefinitionToTypeMap().forEach((definition, type) -> {
            if (definition.getDirection() != PacketBuilder.Direction.SERVERBOUND) { // serverbound
                ClientPlayNetworking.registerGlobalReceiver(type, (packet, ctx) -> {
                    ((UTPacket) packet).handleServer(ctx.player().getUUID());
                });
            }
        });
    }
}