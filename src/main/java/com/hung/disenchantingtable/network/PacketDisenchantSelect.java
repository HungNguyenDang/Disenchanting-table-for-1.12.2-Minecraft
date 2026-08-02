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
    private int enchantmentLevel;

    // Required for packet deserialization
    public PacketDisenchantSelect() {
    }

    public PacketDisenchantSelect(
            ResourceLocation enchantmentId,
            int enchantmentLevel) {

        this.enchantmentId = enchantmentId.toString();
        this.enchantmentLevel = enchantmentLevel;
    }

    @Override
    public void toBytes(ByteBuf buf) {

        ByteBufUtils.writeUTF8String(
                buf,
                enchantmentId
        );

        buf.writeInt(enchantmentLevel);
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        enchantmentId =
                ByteBufUtils.readUTF8String(buf);

        enchantmentLevel =
                buf.readInt();
    }

    public static class Handler
            implements IMessageHandler<
            PacketDisenchantSelect,
            IMessage> {

        @Override
        public IMessage onMessage(
                PacketDisenchantSelect message,
                MessageContext ctx) {

            EntityPlayerMP player =
                    ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {

                if (message.enchantmentId == null
                        || message.enchantmentId.length() > 256) {

                    return;
                }

                if (message.enchantmentLevel <= 0
                        || message.enchantmentLevel > 32767) {

                    return;
                }

                if (!(player.openContainer
                        instanceof ContainerDisenchantingTable)) {

                    return;
                }

                ResourceLocation enchantmentId;

                try {

                    enchantmentId =
                            new ResourceLocation(
                                    message.enchantmentId
                            );

                } catch (Exception e) {

                    return;
                }

                ContainerDisenchantingTable container =
                        (ContainerDisenchantingTable)
                                player.openContainer;

                container.handleEnchantmentClick(
                        player,
                        enchantmentId,
                        message.enchantmentLevel
                );
            });

            return null;
        }
    }
}