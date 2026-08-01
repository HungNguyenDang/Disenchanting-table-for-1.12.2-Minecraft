package com.hung.disenchantingtable.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

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
    public ItemStack decrStackSize(
            int index,
            int count) {

        ItemStack result =
                ItemStackHelper.getAndSplit(
                        inventory,
                        index,
                        count
                );

        if (!result.isEmpty()) {
            markDirty();
        }

        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {

        ItemStack result =
                ItemStackHelper.getAndRemove(
                        inventory,
                        index
                );

        if (!result.isEmpty()) {
            markDirty();
        }

        return result;
    }

    @Override
    public void setInventorySlotContents(
            int index,
            ItemStack stack) {

        inventory.set(
                index,
                stack
        );

        if (!stack.isEmpty()
                && stack.getCount() > getInventoryStackLimit()) {

            stack.setCount(
                    getInventoryStackLimit()
            );
        }

        markDirty();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}

    /**
     * Slot validation.
     *
     * Slot 0 = input item
     * Slot 1 = normal book
     * Slot 2 = output only
     */
    @Override
    public boolean isItemValidForSlot(
            int index,
            ItemStack stack) {

        switch (index) {

            case 0:
                // Input item
                return true;

            case 1:
                // Must be a normal book
                return stack.getItem() == Items.BOOK;

            case 2:
                // Output slot
                return false;

            default:
                return false;
        }
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

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
}