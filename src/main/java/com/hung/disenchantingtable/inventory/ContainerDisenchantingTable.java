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
    public final List<Integer> availableEnchantmentLevels = new ArrayList<>();
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
        availableEnchantmentLevels.clear();

        ItemStack inputStack =
                this.inventorySlots
                        .get(0)
                        .getStack();

        if (inputStack.isEmpty()) {
            return;
        }

        /*
         * =====================================================
         * ENCHANTED BOOK
         * =====================================================
         *
         * Read the actual StoredEnchantments NBT list.
         *
         * This preserves duplicate enchantments such as:
         *
         * Protection IV
         * Protection I
         */
        if (inputStack.getItem() == Items.ENCHANTED_BOOK) {

            if (!inputStack.hasTagCompound()) {
                return;
            }

            NBTTagCompound tag =
                    inputStack.getTagCompound();

            if (!tag.hasKey(
                    "StoredEnchantments",
                    9)) {

                return;
            }

            NBTTagList enchantmentList =
                    tag.getTagList(
                            "StoredEnchantments",
                            10
                    );

            for (int i = 0;
                 i < enchantmentList.tagCount();
                 i++) {

                NBTTagCompound enchantmentTag =
                        enchantmentList.getCompoundTagAt(i);

                String id =
                        enchantmentTag.getString("id");

                int level =
                        enchantmentTag.getShort("lvl");

                if (id == null || id.isEmpty()) {
                    continue;
                }

                ResourceLocation enchantmentId;

                try {

                    enchantmentId =
                            new ResourceLocation(id);

                } catch (Exception e) {

                    continue;
                }

                Enchantment enchantment =
                        Enchantment.REGISTRY.getObject(
                                enchantmentId
                        );

                if (enchantment == null) {
                    continue;
                }

                if (level <= 0) {
                    continue;
                }

                /*
                 * Add EVERY entry.
                 *
                 * This is what allows:
                 *
                 * Protection IV
                 * Protection I
                 *
                 * to both appear.
                 */
                availableEnchantments.add(
                        enchantment.getTranslatedName(level)
                );

                availableEnchantmentIds.add(
                        enchantmentId
                );

                availableEnchantmentLevels.add(
                        level
                );
            }

            return;
        }

        /*
         * =====================================================
         * NORMAL ITEM
         * =====================================================
         *
         * Normal items can still use EnchantmentHelper because
         * normal items are not supposed to have duplicate copies
         * of the same enchantment.
         */
        Map<Enchantment, Integer> enchantments =
                EnchantmentHelper.getEnchantments(
                        inputStack
                );

        for (Map.Entry<Enchantment, Integer> entry
                : enchantments.entrySet()) {

            Enchantment enchantment =
                    entry.getKey();

            int level =
                    entry.getValue();

            if (enchantment == null
                    || level <= 0) {

                continue;
            }

            ResourceLocation enchantmentId =
                    Enchantment.REGISTRY.getNameForObject(
                            enchantment
                    );

            if (enchantmentId == null) {
                continue;
            }

            availableEnchantments.add(
                    enchantment.getTranslatedName(level)
            );

            availableEnchantmentIds.add(
                    enchantmentId
            );

            availableEnchantmentLevels.add(
                    level
            );
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
            ResourceLocation targetEnchantmentId,
            int targetEnchantmentLevel) {

        // Make sure the player is still allowed to use this table.
        if (!canInteractWith(player)) {
            return;
        }

        if (targetEnchantmentId == null) {
            return;
        }

        if (targetEnchantmentLevel <= 0
                || targetEnchantmentLevel > 32767) {

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
         * Resolve the registry ID sent by the client.
         */
        Enchantment targetEnchantment =
                Enchantment.REGISTRY.getObject(
                        targetEnchantmentId
                );

        if (targetEnchantment == null) {
            return;
        }

        /*
         * =====================================================
         * ENCHANTED BOOK
         * =====================================================
         */
        if (inputStack.getItem() == Items.ENCHANTED_BOOK) {

            if (!inputStack.hasTagCompound()) { return;}

            NBTTagCompound tag =
                    inputStack.getTagCompound();

            if (!tag.hasKey(
                    "StoredEnchantments",
                    9)) {
                return;
            }

            NBTTagList oldList =
                    tag.getTagList(
                            "StoredEnchantments",
                            10
                    );

            /*
             * Find the EXACT entry:
             *
             * enchantment ID + level
             *
             * This is what allows Protection IV and Protection I
             * to coexist.
             */
            int targetIndex = -1;

            for (int i = 0;
                 i < oldList.tagCount();
                 i++) {

                NBTTagCompound enchantmentTag =
                        oldList.getCompoundTagAt(i);

                String id =
                        enchantmentTag.getString("id");

                int level =
                        enchantmentTag.getShort("lvl");

                if (targetEnchantmentId.toString().equals(id)
                        && level == targetEnchantmentLevel) {

                    targetIndex = i;
                    break;
                }
            }

            /*
             * The client may be out of date.
             *
             * If the exact enchantment no longer exists,
             * reject the request.
             */
            if (targetIndex < 0) {
                return;
            }

            /*
             * Create the output book.
             */
            ItemStack enchantedBook =
                    new ItemStack(
                            Items.ENCHANTED_BOOK
                    );

            ItemEnchantedBook.addEnchantment(
                    enchantedBook,
                    new EnchantmentData(
                            targetEnchantment,
                            targetEnchantmentLevel
                    )
            );

            /*
             * Build a new list WITHOUT the selected entry.
             */
            NBTTagList newList =
                    new NBTTagList();

            for (int i = 0;
                 i < oldList.tagCount();
                 i++) {

                /*
                 * Skip ONLY the selected entry.
                 */
                if (i == targetIndex) {
                    continue;
                }

                NBTTagCompound original =
                        oldList.getCompoundTagAt(i);

                /*
                 * Copy the tag so we don't accidentally
                 * modify the old list while iterating.
                 */
                NBTTagCompound copy =
                        original.copy();

                newList.appendTag(copy);
            }

            /*
             * =================================================
             * WHAT REMAINS?
             * =================================================
             */
            if (newList.tagCount() == 0) {

                /*
                 * Nothing remains.
                 *
                 * Enchanted Book -> Normal Book
                 */
                ItemStack normalBook =
                        new ItemStack(
                                Items.BOOK,
                                inputStack.getCount()
                        );

                inputSlot.putStack(
                        normalBook
                );

            } else {

                /*
                 * Other enchantments remain.
                 *
                 * Keep the input as an enchanted book.
                 */
                tag.setTag(
                        "StoredEnchantments",
                        newList
                );

                inputStack.setTagCompound(tag);

                inputSlot.onSlotChanged();
            }

            /*
             * Consume one normal book.
             */
            bookStack.shrink(1);

            if (bookStack.isEmpty()) {

                bookSlot.putStack(
                        ItemStack.EMPTY
                );

            } else {

                bookSlot.onSlotChanged();
            }

            /*
             * Put selected enchantment into output.
             */
            outputSlot.putStack(
                    enchantedBook
            );

            outputSlot.onSlotChanged();

            inputSlot.onSlotChanged();

            tileEntity.markDirty();

            updateEnchantmentList();

            detectAndSendChanges();

            return;
        }

        /*
         * =====================================================
         * NORMAL ITEM
         * =====================================================
         *
         * Normal items use the normal enchantment map.
         */
        Map<Enchantment, Integer> enchantments =
                EnchantmentHelper.getEnchantments(
                        inputStack
                );

        Integer actualLevel =
                enchantments.get(
                        targetEnchantment
                );

        /*
         * Verify that the requested enchantment really exists
         * on the current item.
         */
        if (actualLevel == null
                || actualLevel <= 0) {

            return;
        }

        /*
         * For normal items, the selected level must match.
         */
        if (actualLevel != targetEnchantmentLevel) {
            return;
        }

        /*
         * Create output.
         */
        ItemStack enchantedBook =
                new ItemStack(
                        Items.ENCHANTED_BOOK
                );

        ItemEnchantedBook.addEnchantment(
                enchantedBook,
                new EnchantmentData(
                        targetEnchantment,
                        targetEnchantmentLevel
                )
        );

        /*
         * Remove enchantment.
         */
        enchantments.remove(
                targetEnchantment
        );

        /*
         * If no enchantments remain,
         * the input item simply becomes unenchanted.
         */
        EnchantmentHelper.setEnchantments(
                enchantments,
                inputStack
        );

        inputSlot.onSlotChanged();

        /*
         * Consume one normal book.
         */
        bookStack.shrink(1);

        if (bookStack.isEmpty()) {

            bookSlot.putStack(
                    ItemStack.EMPTY
            );

        } else {

            bookSlot.onSlotChanged();
        }

        /*
         * Output.
         */
        outputSlot.putStack(
                enchantedBook
        );

        outputSlot.onSlotChanged();

        inputSlot.onSlotChanged();

        tileEntity.markDirty();

        updateEnchantmentList();

        detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        // Disable shift-click item movement in this container.
        return ItemStack.EMPTY;
    }
}