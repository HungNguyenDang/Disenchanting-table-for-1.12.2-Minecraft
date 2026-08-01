package com.hung.disenchantingtable.util;

import com.hung.disenchantingtable.gui.GuiDisenchantingTable;
import com.hung.disenchantingtable.inventory.ContainerDisenchantingTable;
import com.hung.disenchantingtable.tileentity.TileEntityDisenchantingTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {
    public static final int GUI_DISENCHANTING_TABLE = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_DISENCHANTING_TABLE) {
            TileEntityDisenchantingTable tile = (TileEntityDisenchantingTable) world.getTileEntity(new BlockPos(x, y, z));
            return new ContainerDisenchantingTable(player.inventory, tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_DISENCHANTING_TABLE) {
            TileEntityDisenchantingTable tile = (TileEntityDisenchantingTable) world.getTileEntity(new BlockPos(x, y, z));
            return new GuiDisenchantingTable(player.inventory, tile);
        }
        return null;
    }
}