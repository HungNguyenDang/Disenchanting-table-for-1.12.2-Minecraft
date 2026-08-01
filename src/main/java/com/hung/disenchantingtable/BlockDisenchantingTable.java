package com.hung.disenchantingtable;

import com.hung.disenchantingtable.DisenchantingTable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDisenchantingTable extends Block {

    public BlockDisenchantingTable() {
        super(Material.ROCK);
        setUnlocalizedName("disenchanting_table");
        setRegistryName("disenchanting_table");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(3.0F);
        setResistance(5.0F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(DisenchantingTable.instance, com.hung.disenchantingtable.util.ModGuiHandler.GUI_DISENCHANTING_TABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(net.minecraft.block.state.IBlockState state) {
        return true;
    }

    @Override
    public net.minecraft.tileentity.TileEntity createTileEntity(net.minecraft.world.World world, net.minecraft.block.state.IBlockState state) {
        return new com.hung.disenchantingtable.tileentity.TileEntityDisenchantingTable();
    }
}
