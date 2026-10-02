package com.example.examplemod;

import com.example.examplemod.world.CopperOreWorldGenerator;
import com.example.examplemod.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = ExampleMod.MODID, name = ExampleMod.NAME, version = ExampleMod.VERSION)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final String NAME = "Example Mod";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        GameRegistry.registerWorldGenerator(new CopperOreWorldGenerator(), 0);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addSmelting(ModItems.RAW_IRON, new ItemStack(Items.IRON_INGOT), 0.7F);
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
