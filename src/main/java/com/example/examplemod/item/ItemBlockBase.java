package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

public class ItemBlockBase extends ItemBlock
{
    public ItemBlockBase(Block block, String name, CreativeTabs tab)
    {
        super(block);
        setRegistryName(ExampleMod.MODID, name);
        setTranslationKey(ExampleMod.MODID + "." + name);
        setCreativeTab(tab);
        ModItems.ALL_ITEMS.add(this);
    }
}
