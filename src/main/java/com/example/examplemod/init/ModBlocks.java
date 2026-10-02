package com.example.examplemod.init;

import com.example.examplemod.ExampleMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class ModBlocks {
    public static final Block COPPER_ORE = new BlockOre() {
        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return ModItems.RAW_COPPER;
        }
    }
            .setRegistryName(ExampleMod.MODID, "copper_ore")
            .setUnlocalizedName(ExampleMod.MODID + ".copper_ore")
            .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
            .setHardness(3.0F)
            .setResistance(5.0F)
            .setSoundType(SoundType.STONE)
            .setHarvestLevel("pickaxe", 1);

    private ModBlocks() {}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(COPPER_ORE);
    }
}
