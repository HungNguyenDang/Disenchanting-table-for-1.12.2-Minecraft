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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerDisenchantingTable extends Container {
    private final TileEntityDisenchantingTable tileEntity;
    public final List<String> availableEnchantments = new ArrayList<>();

    public ContainerDisenchantingTable(InventoryPlayer playerInventory, TileEntityDisenchantingTable tileEntity) {
        this.tileEntity = tileEntity;

        // Slot 1: Input Item (Left) - index 0
        this.addSlotToContainer(new Slot(tileEntity, 0, 117, 33));

        // Slot 2: Empty Book (Middle) - index 1
        this.addSlotToContainer(new Slot(tileEntity, 1, 152, 33) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.BOOK;
            }
        });

        // Slot 3: Output Enchanted Book (Right) - index 2
        this.addSlotToContainer(new Slot(tileEntity, 2, 134, 60) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false; // Output slot only
            }
        });

        // Bind Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Bind Player Hotbar
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
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

    public void updateEnchantmentList() {
        availableEnchantments.clear();
        ItemStack inputStack = this.inventorySlots.get(0).getStack(); // Slot 0 is the input item

        if (!inputStack.isEmpty()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(inputStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment ench = entry.getKey();
                int level = entry.getValue();
                if (ench != null) {
                    // Format the enchantment name and level (e.g., "Sharpness IV")
                    String displayName = ench.getTranslatedName(level);
                    availableEnchantments.add(displayName);
                }
            }
        }
    }

    public void handleEnchantmentClick(net.minecraft.entity.player.EntityPlayer player, int index) {
        // Check bounds and make sure index is valid
        if (index < 0 || index >= availableEnchantments.size()) return;

        Slot inputSlot = this.inventorySlots.get(0);   // Input item
        Slot bookSlot = this.inventorySlots.get(1);    // Empty book
        Slot outputSlot = this.inventorySlots.get(2);  // Output slot

        ItemStack inputStack = inputSlot.getStack();
        ItemStack bookStack = bookSlot.getStack();
        ItemStack outputStack = outputSlot.getStack();

        // Must have an item in slot 0, an empty book in slot 1, and output slot must be empty
        if (inputStack.isEmpty() || bookStack.isEmpty() || bookStack.getItem() != Items.BOOK || !outputStack.isEmpty()) {
            return;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(inputStack);
        List<Enchantment> enchKeys = new ArrayList<>(enchantments.keySet());

        if (index >= enchKeys.size()) {
            return;
        }

        Enchantment targetEnch = enchKeys.get(index);
        int targetLevel = enchantments.get(targetEnch);

        // 1. Create the output Enchanted Book
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantedBook.addEnchantment(enchantedBook, new EnchantmentData(targetEnch, targetLevel));

        // 2. Remove enchantment from input item
        enchantments.remove(targetEnch);

        // If it's an enchanted book and no enchantments are left, convert it into a normal book!
        if (inputStack.getItem() == Items.ENCHANTED_BOOK && enchantments.isEmpty()) {
            ItemStack normalBook = new ItemStack(Items.BOOK, inputStack.getCount());
            inputSlot.putStack(normalBook);
        } else {
            // Otherwise, clear tags and re-apply remaining enchantments normally...
            if (inputStack.hasTagCompound()) {
                // After modifying the inputStack enchantments:
                net.minecraft.nbt.NBTTagCompound tagCompound = inputStack.getTagCompound();

                // Force slot and tile entity updates
                inputSlot.onSlotChanged();
                tileEntity.markDirty();

                // Send changes to the client container tracking
                this.detectAndSendChanges();

                if (tagCompound.hasKey("StoredEnchantments", 9)) {
                    tagCompound.removeTag("StoredEnchantments");
                }
                if (tagCompound.hasKey("ench", 9)) {
                    tagCompound.removeTag("ench");
                }
            }

            // Re-apply remaining enchantments (if any are left) using standard helper methods
            if (!enchantments.isEmpty()) {
                if (inputStack.getItem() instanceof net.minecraft.item.ItemEnchantedBook) {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        ItemEnchantedBook.addEnchantment(inputStack, new EnchantmentData(entry.getKey(), entry.getValue()));
                    }
                } else {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        inputStack.addEnchantment(entry.getKey(), entry.getValue());
                    }
                }
            }
            inputSlot.onSlotChanged();
        }

        // 4. Consume 1 empty book from slot 1
        bookStack.shrink(1);
        if (bookStack.getCount() <= 0) {
            bookSlot.putStack(ItemStack.EMPTY);
        } else {
            bookSlot.onSlotChanged();
        }

        // 5. Place the finished enchanted book into the output slot (Slot 2)
        outputSlot.putStack(enchantedBook);
        outputSlot.onSlotChanged();

        // 6. Force input slot to flag changes so server syncs it to the client
        inputSlot.onSlotChanged();

        // 7. Refresh GUI list and push changes across the network container tracker
        updateEnchantmentList();

        tileEntity.markDirty();
    }
}