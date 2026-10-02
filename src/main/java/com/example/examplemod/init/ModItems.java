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
    public static final Item RAW_IRON = new Item()
            .setRegistryName(ExampleMod.MODID, "raw_iron")
            .setUnlocalizedName(ExampleMod.MODID + ".raw_iron")
            .setCreativeTab(CreativeTabs.MATERIALS);

    public static final Item RAW_COPPER = new Item()
            .setRegistryName(ExampleMod.MODID, "raw_copper")
            .setUnlocalizedName(ExampleMod.MODID + ".raw_copper")
            .setCreativeTab(CreativeTabs.MATERIALS);

    public static final ItemBlock COPPER_ORE_ITEM = (ItemBlock) new ItemBlock(ModBlocks.COPPER_ORE)
            .setRegistryName(ExampleMod.MODID, "copper_ore")
            .setUnlocalizedName(ExampleMod.MODID + ".copper_ore")
            .setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

    private ModItems() {}

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(RAW_IRON);
        event.getRegistry().register(RAW_COPPER);
        event.getRegistry().register(COPPER_ORE_ITEM);
    }
}
