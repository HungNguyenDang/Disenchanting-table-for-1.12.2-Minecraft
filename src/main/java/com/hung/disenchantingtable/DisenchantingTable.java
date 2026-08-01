package com.hung.disenchantingtable;

import com.hung.disenchantingtable.network.PacketDisenchantSelect;
import com.hung.disenchantingtable.util.ModGuiHandler;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DisenchantingTable.MODID, name = DisenchantingTable.NAME, version = DisenchantingTable.VERSION)
public class DisenchantingTable
{
    public static final String MODID = "disenchantingtable";
    public static final String NAME = "disenchanting_table";
    public static final String VERSION = "1.0";

    // Add a static instance reference so your block can call playerIn.openGui(...) easily
    @Mod.Instance(value = MODID)
    public static DisenchantingTable instance;

    // Declare the network wrapper instance globally
    public static SimpleNetworkWrapper NETWORK;
    private static int packetId = 0;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        // Initialize the network channel and register your packet during preInit
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NETWORK.registerMessage(
                PacketDisenchantSelect.Handler.class,
                PacketDisenchantSelect.class,
                packetId++,
                Side.SERVER
        );
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        // Register your GUI handler here
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGuiHandler());

        net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(
                com.hung.disenchantingtable.tileentity.TileEntityDisenchantingTable.class,
                new net.minecraft.util.ResourceLocation("disenchantingtable", "disenchanting_table")
        );
    }
}