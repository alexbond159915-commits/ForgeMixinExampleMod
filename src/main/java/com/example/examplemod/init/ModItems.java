package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)

public final class ModItems {
    public static final Item FLYWHEEL = new Item()
            .setTranslationKey(ExampleMod.MODID + ".flywheel")
            .setRegistryName("flywheel")
            .setCreativeTab(ExampleMod.MACHINES_TAB);

    public static final Item RAW_IRON = new Item()
            .setTranslationKey(ExampleMod.MODID + ".raw_iron")
            .setRegistryName("raw_iron")
            .setCreativeTab(ExampleMod.CREATIVE_TAB);

    public static final Item RAW_COPPER = new Item()
            .setTranslationKey(ExampleMod.MODID + ".raw_copper")
            .setRegistryName("raw_copper")
            .setCreativeTab(ExampleMod.CREATIVE_TAB);

    public static final ItemBlock COPPER_ORE_ITEM = (ItemBlock) new ItemBlock(ModBlocks.COPPER_ORE)
            .setRegistryName(ExampleMod.MODID, "copper_ore")
            .setTranslationKey(ExampleMod.MODID + ".copper_ore")
            .setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

    public static final Item COPPER_INGOT = new Item()
            .setTranslationKey(ExampleMod.MODID + ".copper_ingot")
            .setRegistryName("copper_ingot")
            .setCreativeTab(ExampleMod.CREATIVE_TAB);

    public static final ItemBlock STEAM_ENGINE_CASING_ITEM =
            new ItemBlock(ModBlocks.STEAM_ENGINE_CASING);

    public static final ItemBlock MUFFLE_FURNACE_ITEM =
            new ItemBlock(ModBlocks.MUFFLE_FURNACE);

    static
    {
        STEAM_ENGINE_CASING_ITEM.setRegistryName(
                ExampleMod.MODID,
                "steam_engine_casing"
        );

        STEAM_ENGINE_CASING_ITEM.setTranslationKey(
                ExampleMod.MODID + ".steam_engine_casing"
        );

        STEAM_ENGINE_CASING_ITEM.setCreativeTab(
                ExampleMod.MACHINES_TAB
        );

        MUFFLE_FURNACE_ITEM.setRegistryName(
                ExampleMod.MODID,
                "muffle_furnace"
        );

        MUFFLE_FURNACE_ITEM.setTranslationKey(
                ExampleMod.MODID + ".muffle_furnace"
        );

        MUFFLE_FURNACE_ITEM.setCreativeTab(
                ExampleMod.CREATIVE_TAB
        );
    }

    private ModItems() {}

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(FLYWHEEL);
        event.getRegistry().register(RAW_IRON);
        event.getRegistry().register(RAW_COPPER);
        event.getRegistry().register(COPPER_ORE_ITEM);
        event.getRegistry().register(COPPER_INGOT);
        event.getRegistry().register(STEAM_ENGINE_CASING_ITEM);
        event.getRegistry().register(MUFFLE_FURNACE_ITEM);
    }
}
