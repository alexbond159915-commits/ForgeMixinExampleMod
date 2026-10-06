package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.ItemBase;
import com.example.examplemod.item.ItemBlockBase;
import net.minecraft.block.Block;
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

    // ItemBlock representations of the ferrite magnet blocks.
    public static final Item FERRITE_MAGNET_BLANK =
            new ItemBlockBase(ModBlocks.FERRITE_MAGNET_BLANK);

    public static final Item FERRITE_MAGNET =
            new ItemBlockBase(ModBlocks.FERRITE_MAGNET);

    private ModItems()
    {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        for (Block block : ModBlocks.ALL_BLOCKS)
        {
            // These two blocks have explicit ItemBlock registrations below.
            if (block == ModBlocks.FERRITE_MAGNET_BLANK ||
                block == ModBlocks.FERRITE_MAGNET)
            {
                continue;
            }

            ItemBlockBase itemBlock = new ItemBlockBase(block);
            event.getRegistry().register(itemBlock);
        }

        event.getRegistry().register(FERRITE_MAGNET_BLANK);
        event.getRegistry().register(FERRITE_MAGNET);

        for (Item item : ALL_ITEMS)
        {
            event.getRegistry().register(item);
        }
    }
}
