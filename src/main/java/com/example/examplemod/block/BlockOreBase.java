package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlocks;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.Random;
import java.util.function.Supplier;

public class BlockOreBase extends BlockOre
{
    private final Supplier<Item> drop;

    public BlockOreBase(String name, CreativeTabs tab, Supplier<Item> drop)
    {
        super();
        this.drop = drop;
        setTranslationKey(ExampleMod.MODID + "." + name);
        setRegistryName(ExampleMod.MODID, name);
        setCreativeTab(tab);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return drop.get();
    }
}
