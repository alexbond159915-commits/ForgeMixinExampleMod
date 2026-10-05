package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockBase extends Block
{
    public BlockBase(Material material, String name, CreativeTabs tab)
    {
        super(material);
        setTranslationKey(ExampleMod.MODID + "." + name);
        setRegistryName(ExampleMod.MODID, name);
        setCreativeTab(tab);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    public BlockBase setSoundType(SoundType soundType)
    {
        super.setSoundType(soundType);
        return this;
    }
}
