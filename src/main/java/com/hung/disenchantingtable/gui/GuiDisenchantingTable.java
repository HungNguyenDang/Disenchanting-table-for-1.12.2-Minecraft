package com.hung.disenchantingtable.gui;

import com.hung.disenchantingtable.DisenchantingTable;
import com.hung.disenchantingtable.inventory.ContainerDisenchantingTable;
import com.hung.disenchantingtable.network.PacketDisenchantSelect;
import com.hung.disenchantingtable.tileentity.TileEntityDisenchantingTable;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiDisenchantingTable extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("disenchantingtable:textures/gui/disenchanting_table.png");
    private final TileEntityDisenchantingTable tileEntity;
    private final ContainerDisenchantingTable disenchantContainer;

    private int clickCooldown = 0;

    // Display names
    private final List<String> availableEnchantments =
            new ArrayList<>();

    // Actual enchantment registry IDs
    private final List<ResourceLocation> availableEnchantmentIds =
            new ArrayList<>();

    // Scroll state variables for the enchantment list
    private int scrollOffset = 0;
    private boolean isDraggingScrollbar = false;

    // List Panel Bounds in GUI coordinates
    private final int listX = 7;
    private final int listY = 7;
    private final int listWidth = 104;
    private final int listHeight = 71;

    public GuiDisenchantingTable(InventoryPlayer playerInv, TileEntityDisenchantingTable tileEntity) {
        super(new ContainerDisenchantingTable(playerInv, tileEntity));
        this.tileEntity = tileEntity;
        this.disenchantContainer = (ContainerDisenchantingTable) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void updateScreen() {

        super.updateScreen();

        /*
         * The client container is synchronized by Minecraft.
         *
         * We rebuild the GUI's display list from the current
         * client-side container state.
         */
        this.disenchantContainer.updateEnchantmentList();

        this.availableEnchantments.clear();
        this.availableEnchantments.addAll(
                this.disenchantContainer.availableEnchantments
        );

        this.availableEnchantmentIds.clear();
        this.availableEnchantmentIds.addAll(
                this.disenchantContainer.availableEnchantmentIds
        );

        /*
         * Make sure scrolling doesn't point beyond the new list.
         */
        int maxScroll =
                Math.max(0, availableEnchantments.size() - 3);

        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }

        if (clickCooldown > 0) {
            clickCooldown--;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int guiLeftPos = (this.width - this.xSize) / 2;
        int guiTopPos = (this.height - this.ySize) / 2;

        // Check scroll wheel inside list area
        if (wheel != 0
                && mouseX >= guiLeftPos + listX
                && mouseX <= guiLeftPos + listX + listWidth
                && mouseY >= guiTopPos + listY
                && mouseY <= guiTopPos + listY + listHeight) {

            int maxScroll = Math.max(0, availableEnchantments.size() - 3);

            if (wheel > 0 && scrollOffset > 0) {
                scrollOffset--;
            } else if (wheel < 0 && scrollOffset < maxScroll) {
                scrollOffset++;
            }
        }

        // Handle scrollbar dragging
        if (Mouse.isButtonDown(0)) {
            if (!isDraggingScrollbar) {
                if (mouseX >= guiLeftPos + listX + listWidth - 7 && mouseX <= guiLeftPos + listX + listWidth &&
                        mouseY >= guiTopPos + listY && mouseY <= guiTopPos + listY + listHeight) {
                    isDraggingScrollbar = true;
                }
            } else {
                int maxScroll = Math.max(0, availableEnchantments.size() - 3);
                float mouseClickRelativeY = (float)(mouseY - (guiTopPos + listY)) / (float)listHeight;
                scrollOffset = Math.max(0, Math.min(maxScroll, Math.round(mouseClickRelativeY * maxScroll)));
            }
        } else {
            isDraggingScrollbar = false;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        // Draw Custom Scrollbar Thumb Track
        if (!availableEnchantments.isEmpty()) {
            int maxScroll = Math.max(0, availableEnchantments.size() - 3);
            if (maxScroll > 0) {
                int scrollbarHeight = Math.max(10, listHeight / (maxScroll + 1));

                // Define the inner track height: visible panel height minus top/bottom border (1px each)
                int innerTrackHeight = listHeight - 2;
                int availableThumbTravel = innerTrackHeight - scrollbarHeight;
                int scrollbarY = l + listY + 1 + (int)((float)scrollOffset / maxScroll * availableThumbTravel);

                // Draw thumb box handle strictly within bounds
                this.drawRect(k + listX + listWidth - 6, scrollbarY, k + listX + listWidth - 1, scrollbarY + scrollbarHeight, 0xFF424242);
                this.drawRect(k + listX + listWidth - 5, scrollbarY + 1, k + listX + listWidth - 2, scrollbarY + scrollbarHeight - 1, 0xFF808080);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        // 1. Title moved to the top right to avoid overlapping the new list container
        String guiTitle = "Disenchanter";
        GlStateManager.pushMatrix();
        // Scale down to 75% size (change 0.75F to 0.5F if you want it even smaller)
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        // Adjust coordinates because scaling shrinks the position as well (divide by the scale factor)
        int scaledWidth = this.fontRenderer.getStringWidth(guiTitle);
        int xPos = (int)((this.xSize - (scaledWidth * 0.75F) - 6) / 0.75F);
        int yPos = (int)(6 / 0.75F);
        this.fontRenderer.drawString(guiTitle, xPos, yPos, 4210752);
        GlStateManager.popMatrix();

        // Check if player can currently interact (has book in slot 1 and output slot 2 is empty)
        ItemStack bookStack = this.disenchantContainer.inventorySlots.get(1).getStack();
        ItemStack outputStack = this.disenchantContainer.inventorySlots.get(2).getStack();
        boolean canInteract = !bookStack.isEmpty() && bookStack.getItem() == net.minecraft.init.Items.BOOK && outputStack.isEmpty();

        // Render Enchantment List Entries with Item-Table Box Style & Word Wrapping
        int maxVisible = 3;
        int startIdx = Math.max(0, Math.min(scrollOffset, Math.max(0, availableEnchantments.size() - maxVisible)));

        int boxY = listY + 2;
        int boxWidth = listWidth - 8; // leave room for scrollbar on the right

        // Convert global mouse coordinates to GUI-relative coordinates for hover detection
        int guiLeftPos = (this.width - this.xSize) / 2;
        int guiTopPos = (this.height - this.ySize) / 2;
        int relMouseX = mouseX - guiLeftPos;
        int relMouseY = mouseY - guiTopPos;

        for (int i = 0; i < maxVisible; i++) {
            int currentIdx = startIdx + i;
            if (currentIdx >= availableEnchantments.size()) break;

            String enchName = availableEnchantments.get(currentIdx);

            // Check if mouse is hovering over this specific row box
            boolean isHovered = canInteract &&
                    relMouseX >= listX + 2 && relMouseX <= listX + boxWidth &&
                    relMouseY >= boxY && relMouseY <= boxY + 21;

            // Render hover-lit box background or standard background
            int bgColor = isHovered ? 0x90505050 : 0x50000000;

            // Draw individual entry background container box (like vanilla enchantment table rows)
            this.drawRect(listX + 2, boxY, listX + boxWidth, boxY + 21, bgColor);
            this.drawHorizontalLine(listX + 2, listX + boxWidth - 1, boxY, 0xFF373737);
            this.drawHorizontalLine(listX + 2, listX + boxWidth - 1, boxY + 20, 0xFF101010);

            // Text color brightens when hovered and interactive
            int textColor = isHovered ? 0xFFFF55 : 0xFFFFFF;

            // Auto word-wrap text lines if too long to prevent overflowing out of box
            List<String> wrappedLines = this.fontRenderer.listFormattedStringToWidth(enchName, boxWidth - 6);
            int textY = boxY + 3;
            for (String line : wrappedLines) {
                if (textY < boxY + 19) {
                    this.fontRenderer.drawString(line, listX + 4, textY, textColor);
                    textY += 9;
                }
            }
            boxY += 23;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Prevent spamming packets too quickly
        if (clickCooldown > 0) {
            return;
        }

        // Only react to left-click.
        if (mouseButton != 0) {
            return;
        }

        int guiLeftPos = (this.width - this.xSize) / 2;
        int guiTopPos = (this.height - this.ySize) / 2;

        // Check inventory states
        ItemStack inputStack = this.disenchantContainer.inventorySlots.get(0).getStack();
        ItemStack bookStack = this.disenchantContainer.inventorySlots.get(1).getStack();
        ItemStack outputStack = this.disenchantContainer.inventorySlots.get(2).getStack();

        // Ensure input item exists, slot 1 has an empty book, and output is clear
        if (inputStack.isEmpty()
                || bookStack.isEmpty()
                || bookStack.getItem() != Items.BOOK
                || !outputStack.isEmpty()) {
            return;
        }

        // Check if click is inside the list panel area
        if (mouseX >= guiLeftPos + listX
                && mouseX <= guiLeftPos + listX + listWidth
                && mouseY >= guiTopPos + listY
                && mouseY <= guiTopPos + listY + listHeight) {

            int relativeY = mouseY - (guiTopPos + listY + 2);

            if (relativeY < 0) {return;}

                int clickedRow = relativeY / 23; // 23 is box height + gap spacing step

            if (clickedRow >= 0 && clickedRow < 3) {
                int targetIndex = scrollOffset + clickedRow;
                if (targetIndex < availableEnchantments.size()) {
                    ResourceLocation targetEnchantmentId = availableEnchantmentIds.get(targetIndex);

                    if (targetEnchantmentId == null) {
                        return;
                    }

                    // Play click sound feedback
                    this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F));

                    /*
                     * IMPORTANT:
                     *
                     * We now send the actual enchantment ID.
                     *
                     * We do NOT send targetIndex.
                     */
                    DisenchantingTable.NETWORK.sendToServer(
                            new PacketDisenchantSelect(
                                    targetEnchantmentId
                            )
                    );

                    // Set a short 5-tick cooldown to prevent desync packet spam
                    clickCooldown = 5;
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        // This renders the item tooltip if your mouse is hovering over any slot (input, book, output, or player inventory)
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}