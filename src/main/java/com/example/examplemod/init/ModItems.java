package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.ItemBase;
import com.example.examplemod.item.ItemBlockBase;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
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

    public static final Item COPPER_INGOT =
            new ItemBase("copper_ingot", ExampleMod.CREATIVE_TAB);

    public static final Item WITHERITE =
            new ItemBase("witherite", ExampleMod.CREATIVE_TAB);

    private ModItems()
    {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        for (Block block : ModBlocks.ALL_BLOCKS)
        {
            ItemBlockBase itemBlock = new ItemBlockBase(block);
            itemBlock.setRegistryName(block.getRegistryName());
            event.getRegistry().register(itemBlock);
        }

        for (Item item : ALL_ITEMS)
        {
            event.getRegistry().register(item);
        }
    }
}
