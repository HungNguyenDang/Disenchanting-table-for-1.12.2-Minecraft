package com.hung.disenchantingtable.network;

import com.hung.disenchantingtable.inventory.ContainerDisenchantingTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDisenchantSelect implements IMessage {

    private String enchantmentId;

    // Required for packet deserialization
    public PacketDisenchantSelect() {
    }

    public PacketDisenchantSelect(ResourceLocation enchantmentId) {
        this.enchantmentId = enchantmentId.toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, enchantmentId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enchantmentId = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketDisenchantSelect, IMessage> {

        @Override
        public IMessage onMessage(PacketDisenchantSelect message, MessageContext ctx) {

            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {

                // Basic protection against malformed packets
                if (message.enchantmentId == null || message.enchantmentId.length() > 256) {
                    return;
                }

                if (!(player.openContainer instanceof ContainerDisenchantingTable)) {
                    return;
                }

                ResourceLocation enchantmentId;

                try {
                    enchantmentId = new ResourceLocation(message.enchantmentId);
                } catch (Exception e) {
                    return;
                }

                ContainerDisenchantingTable container =
                        (ContainerDisenchantingTable) player.openContainer;

                container.handleEnchantmentClick(player, enchantmentId);
            });

            return null;
        }
    }
}