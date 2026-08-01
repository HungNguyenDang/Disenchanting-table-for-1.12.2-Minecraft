package com.hung.disenchantingtable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "disenchantingtable")
public class RegistryHandler {

    // Create a public static instance of your block
    public static final Block DISENCHANTING_TABLE = new BlockDisenchantingTable();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(DISENCHANTING_TABLE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // This creates an ItemBlock so you can hold it in your hand and place it
        event.getRegistry().register(new ItemBlock(DISENCHANTING_TABLE).setRegistryName(DISENCHANTING_TABLE.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // FIXED: Maps your inventory ItemBlock to your item JSON model file
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(DISENCHANTING_TABLE),
                0,
                new ModelResourceLocation(java.util.Objects.requireNonNull(DISENCHANTING_TABLE.getRegistryName()), "com/hung/disenchantingtable/inventory")
        );
    }
}