package com.hung.disenchantingtable.network;

import com.hung.disenchantingtable.inventory.ContainerDisenchantingTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDisenchantSelect implements IMessage {
    private int enchantmentIndex;

    public PacketDisenchantSelect() {}

    public PacketDisenchantSelect(int index) {
        this.enchantmentIndex = index;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(enchantmentIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enchantmentIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketDisenchantSelect, IMessage> {
        @Override
        public IMessage onMessage(PacketDisenchantSelect message, MessageContext ctx) {
            // Ensure execution happens safely on the main server thread
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                if (player.openContainer instanceof ContainerDisenchantingTable) {
                    ContainerDisenchantingTable container = (ContainerDisenchantingTable) player.openContainer;
                    container.handleEnchantmentClick(player, message.enchantmentIndex);
                }
            });
            return null;
        }
    }
}