package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.ItemBase;
import com.example.examplemod.item.ItemBlockBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class ModItems
{
    public static final List<Item> ALL_ITEMS =
            new ArrayList<Item>();

    public static final Item FLYWHEEL =
            new ItemBase("flywheel", ExampleMod.MACHINES_TAB);

    public static final Item RAW_IRON =
            new ItemBase("raw_iron", ExampleMod.CREATIVE_TAB);

    public static final Item RAW_COPPER =
            new ItemBase("raw_copper", ExampleMod.CREATIVE_TAB);

    public static final ItemBlock COPPER_ORE_ITEM =
            new ItemBlockBase(
                    ModBlocks.COPPER_ORE,
                    "copper_ore",
                    CreativeTabs.BUILDING_BLOCKS
            );

    public static final Item COPPER_INGOT =
            new ItemBase("copper_ingot", ExampleMod.CREATIVE_TAB);

    public static final ItemBlock STEAM_ENGINE_CASING_ITEM =
            new ItemBlockBase(
                    ModBlocks.STEAM_ENGINE_CASING,
                    "steam_engine_casing",
                    ExampleMod.MACHINES_TAB
            );

    public static final ItemBlock MUFFLE_FURNACE_ITEM =
            new ItemBlockBase(
                    ModBlocks.MUFFLE_FURNACE,
                    "muffle_furnace",
                    ExampleMod.CREATIVE_TAB
            );

    public static final ItemBlock RAW_IRON_BLOCK_ITEM =
            new ItemBlockBase(
                    ModBlocks.RAW_IRON_BLOCK,
                    "raw_iron_block",
                    ExampleMod.CREATIVE_TAB
            );

    public static final Item WITHERITE =
            new ItemBase("witherite", ExampleMod.CREATIVE_TAB);

    public static final ItemBlock WITHERITE_ORE_ITEM =
            new ItemBlockBase(
                    ModBlocks.WITHERITE_ORE,
                    "witherite_ore",
                    ExampleMod.CREATIVE_TAB
            );

    public static final Item FERRITE_MAGNET_BLANK =
            new ItemBase(
                    "ferrite_magnet_blank",
                    ExampleMod.MACHINES_TAB
            );

    public static final ItemBlock FERRITE_MAGNET_BLANK_ITEM =
            new ItemBlockBase(
                    ModBlocks.FERRITE_MAGNET_BLANK,
                    "ferrite_magnet_blank",
                    ExampleMod.MACHINES_TAB
            );

    public static final Item FERRITE_MAGNET =
            new ItemBase("ferrite_magnet", ExampleMod.MACHINES_TAB);

    public static final ItemBlock FERRITE_MAGNET_ITEM_BLOCK =
            new ItemBlockBase(
                    ModBlocks.FERRITE_MAGNET,
                    "ferrite_magnet",
                    ExampleMod.MACHINES_TAB
            );

    private ModItems()
    {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        for (Item item : ALL_ITEMS)
        {
            event.getRegistry().register(item);
        }
    }
}
