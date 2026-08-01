package com.hung.disenchantingtable.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.block.state.IBlockState;

public class TileEntityDisenchantingTable extends TileEntity implements IInventory {
    private NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    @Override
    public int getSizeInventory() { return 3; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) { return inventory.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return net.minecraft.inventory.ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return net.minecraft.inventory.ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        boolean flag = !stack.isEmpty() && stack.isItemEqual(this.inventory.get(index)) && ItemStack.areItemStackTagsEqual(stack, this.inventory.get(index));
        this.inventory.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
        // If slot 0 (input item) changes, mark dirty so container syncs
        if (index == 0 && !flag) {
            this.markDirty();
        }
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return true; }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { inventory.clear(); }
    @Override public String getName() { return "container.disenchanting_table"; }
    @Override public boolean hasCustomName() { return false; }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, inventory);
        return compound;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && !this.world.isRemote) {
            // Force chunk update so block state and tile inventory persist safely
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }
}