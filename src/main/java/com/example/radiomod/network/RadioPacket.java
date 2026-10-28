package com.example.radiomod.network;

import com.example.radiomod.block.RadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RadioPacket {
    private final BlockPos pos;
    private final int action;
    private final int value;

    public RadioPacket(BlockPos pos, int action, int value) {
        this.pos = pos;
        this.action = action;
        this.value = value;
    }

    public static void encode(RadioPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeByte(msg.action);
        buf.writeInt(msg.value);
    }

    public static RadioPacket decode(FriendlyByteBuf buf) {
        return new RadioPacket(buf.readBlockPos(), buf.readByte(), buf.readInt());
    }

    public static void handle(RadioPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (!player.level().isLoaded(msg.pos)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (be instanceof RadioBlockEntity radio) {
                radio.handleAction(msg.action, msg.value);
            }
        });
        ctx.setPacketHandled(true);
    }
}
