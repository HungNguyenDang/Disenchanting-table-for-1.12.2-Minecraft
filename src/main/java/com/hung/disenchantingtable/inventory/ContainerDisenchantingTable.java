package com.hung.disenchantingtable.inventory;

import com.hung.disenchantingtable.tileentity.TileEntityDisenchantingTable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerDisenchantingTable extends Container {

    private final TileEntityDisenchantingTable tileEntity;

    /*
     * These are used by the GUI.
     *
     * availableEnchantments:
     *     Human-readable names shown to the player.
     *
     * availableEnchantmentIds:
     *     Actual registry IDs corresponding to the displayed enchantments.
     *
     * IMPORTANT:
     * The server does NOT trust either list when performing the operation.
     * The server always checks the current input ItemStack again.
     */
    public final List<String> availableEnchantments = new ArrayList<>();
    public final List<ResourceLocation> availableEnchantmentIds = new ArrayList<>();

    public ContainerDisenchantingTable(
            InventoryPlayer playerInventory,
            TileEntityDisenchantingTable tileEntity) {

        this.tileEntity = tileEntity;

        // Slot 0: Input item
        this.addSlotToContainer(
                new Slot(tileEntity, 0, 117, 33)
        );

        // Slot 1: Empty book
        this.addSlotToContainer(
                new Slot(tileEntity, 1, 152, 33) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return stack.getItem() == Items.BOOK;
                    }
                }
        );

        // Slot 2: Output enchanted book
        this.addSlotToContainer(
                new Slot(tileEntity, 2, 134, 60) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeStack(EntityPlayer playerIn) {
                        return true;
                    }
                }
        );

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(
                        new Slot(
                                playerInventory,
                                j + i * 9 + 9,
                                8 + j * 18,
                                84 + i * 18
                        )
                );
            }
        }

        // Player hotbar
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(
                    new Slot(
                            playerInventory,
                            k,
                            8 + k * 18,
                            142
                    )
            );
        }

        updateEnchantmentList();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileEntity.isUsableByPlayer(playerIn);
    }

    @Override
    public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventoryIn) {
        super.onCraftMatrixChanged(inventoryIn);
        updateEnchantmentList();
    }

    /**
     * Rebuild the enchantment list from the current input ItemStack.
     *
     * This method is primarily for GUI display.
     * The server NEVER relies on the list when performing a disenchant.
     */
    public void updateEnchantmentList() {

        availableEnchantments.clear();
        availableEnchantmentIds.clear();

        ItemStack inputStack = this.inventorySlots.get(0).getStack();

        if (inputStack.isEmpty()) {
            return;
        }

        Map<Enchantment, Integer> enchantments =
                EnchantmentHelper.getEnchantments(inputStack);

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {

            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            if (enchantment == null) {
                continue;
            }

            ResourceLocation enchantmentId =
                    Enchantment.REGISTRY.getNameForObject(enchantment);

            if (enchantmentId == null) {
                continue;
            }

            availableEnchantments.add(
                    enchantment.getTranslatedName(level)
            );

            availableEnchantmentIds.add(enchantmentId);
        }
    }

    /**
     * Called by the server when the player clicks an enchantment.
     *
     * IMPORTANT:
     * The client sends the enchantment's registry ID rather than
     * an array index. The server then looks at the CURRENT input item
     * and verifies that the enchantment actually exists on it.
     */
    public void handleEnchantmentClick(
            EntityPlayer player,
            ResourceLocation targetEnchantmentId) {

        // Make sure the player is still allowed to use this table.
        if (!canInteractWith(player)) {
            return;
        }

        if (targetEnchantmentId == null) {
            return;
        }

        // Get the actual current slots from the SERVER.
        Slot inputSlot = this.inventorySlots.get(0);
        Slot bookSlot = this.inventorySlots.get(1);
        Slot outputSlot = this.inventorySlots.get(2);

        ItemStack inputStack = inputSlot.getStack();
        ItemStack bookStack = bookSlot.getStack();
        ItemStack outputStack = outputSlot.getStack();

        // Validate current server-side state.
        if (inputStack.isEmpty()) {
            return;
        }

        if (bookStack.isEmpty() || bookStack.getItem() != Items.BOOK) {
            return;
        }

        if (!outputStack.isEmpty()) {
            return;
        }

        /*
         * Get the enchantments from the CURRENT server-side item.
         */
        Map<Enchantment, Integer> enchantments =
                EnchantmentHelper.getEnchantments(inputStack);

        /*
         * Resolve the registry ID sent by the client.
         */
        Enchantment targetEnchantment =
                Enchantment.REGISTRY.getObject(targetEnchantmentId);

        if (targetEnchantment == null) {
            return;
        }

        /*
         * Verify that the enchantment is actually present
         * on the current input item.
         */
        Integer targetLevel =
                enchantments.get(targetEnchantment);

        if (targetLevel == null || targetLevel <= 0) {
            return;
        }

        /*
         * Create the enchanted book that will contain
         * the selected enchantment.
         */
        ItemStack enchantedBook =
                new ItemStack(Items.ENCHANTED_BOOK);

        ItemEnchantedBook.addEnchantment(
                enchantedBook,
                new EnchantmentData(
                        targetEnchantment,
                        targetLevel
                )
        );

        /*
         * Remove the selected enchantment from our working map.
         */
        enchantments.remove(targetEnchantment);

        /*
         * =====================================================
         * SPECIAL CASE: INPUT IS ALREADY AN ENCHANTED BOOK
         * =====================================================
         *
         * Enchanted books use:
         *
         *     StoredEnchantments
         *
         * instead of the normal item's:
         *
         *     ench
         *
         * Therefore we need to write the remaining enchantments
         * back to StoredEnchantments manually.
         */
        if (inputStack.getItem() == Items.ENCHANTED_BOOK) {

            if (enchantments.isEmpty()) {

                /*
                 * No enchantments remain.
                 *
                 * Convert the input enchanted book
                 * into a normal book.
                 */
                ItemStack normalBook =
                        new ItemStack(
                                Items.BOOK,
                                inputStack.getCount()
                        );

                inputSlot.putStack(normalBook);

            } else {

                /*
                 * Keep the item as an enchanted book.
                 */
                NBTTagCompound tag =
                        inputStack.hasTagCompound()
                                ? inputStack.getTagCompound()
                                : new NBTTagCompound();

                /*
                 * Build a completely new StoredEnchantments list.
                 *
                 * This avoids leaving the selected enchantment behind.
                 */
                NBTTagList storedEnchantments =
                        new NBTTagList();

                for (Map.Entry<Enchantment, Integer> entry
                        : enchantments.entrySet()) {

                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();

                    ResourceLocation enchantmentId =
                            Enchantment.REGISTRY.getNameForObject(
                                    enchantment
                            );

                    if (enchantmentId == null) {
                        continue;
                    }

                    net.minecraft.nbt.NBTTagCompound enchantmentTag =
                            new net.minecraft.nbt.NBTTagCompound();

                    enchantmentTag.setString(
                            "id",
                            enchantmentId.toString()
                    );

                    enchantmentTag.setShort(
                            "lvl",
                            (short) level
                    );

                    storedEnchantments.appendTag(
                            enchantmentTag
                    );
                }

                /*
                 * Replace the old StoredEnchantments list.
                 */
                tag.setTag(
                        "StoredEnchantments",
                        storedEnchantments
                );

                inputStack.setTagCompound(tag);

                inputSlot.onSlotChanged();
            }

        } else {

            /*
             * =====================================================
             * NORMAL ITEM
             * =====================================================
             *
             * Sword, shovel, pickaxe, armor, bow, etc.
             *
             * For normal items EnchantmentHelper.setEnchantments()
             * is appropriate.
             */
            EnchantmentHelper.setEnchantments(
                    enchantments,
                    inputStack
            );

            inputSlot.onSlotChanged();
        }

        /*
         * Consume ONE normal book.
         */
        bookStack.shrink(1);

        if (bookStack.isEmpty()) {
            bookSlot.putStack(ItemStack.EMPTY);
        } else {
            bookSlot.onSlotChanged();
        }

        /*
         * Put the generated enchanted book into the output.
         */
        outputSlot.putStack(enchantedBook);
        outputSlot.onSlotChanged();

        /*
         * Mark inventory changes.
         */
        inputSlot.onSlotChanged();
        tileEntity.markDirty();

        /*
         * Rebuild the display list on the server.
         */
        updateEnchantmentList();

        /*
         * Synchronize the container AFTER all modifications are finished.
         */
        detectAndSendChanges();
    }
}