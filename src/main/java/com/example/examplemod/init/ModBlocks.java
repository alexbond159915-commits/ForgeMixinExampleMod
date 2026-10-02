package com.example.examplemod.init;

import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import java.util.Random;
import com.example.examplemod.ExampleMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.example.examplemod.block.BlockMuffleFurnace;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class ModBlocks {
    public static final Block MUFFLE_FURNACE =
            new BlockMuffleFurnace();
    public static final Block COPPER_ORE = new BlockOre()
    {
        {
            setSoundType(SoundType.STONE);
        }

        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune)
        {
            return ModItems.RAW_COPPER;
        }
    }
            .setTranslationKey(ExampleMod.MODID + ".copper_ore")
            .setRegistryName("copper_ore")
            .setHardness(3.0F)
            .setCreativeTab(ExampleMod.CREATIVE_TAB);
    static {
        COPPER_ORE.setHarvestLevel("pickaxe", 1);
    }

    private ModBlocks() {}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(COPPER_ORE);
        event.getRegistry().register(MUFFLE_FURNACE);
    }
}
