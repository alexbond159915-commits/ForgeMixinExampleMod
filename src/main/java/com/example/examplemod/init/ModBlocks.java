package com.example.examplemod.init;

import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import java.util.Random;
import com.example.examplemod.ExampleMod;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.example.examplemod.block.BlockMuffleFurnace;

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

    public static final Block STEAM_ENGINE_CASING = new Block(Material.IRON)
            .setTranslationKey(ExampleMod.MODID + ".steam_engine_casing")
            .setRegistryName("steam_engine_casing")
            .setHardness(3.0F)
            .setResistance(6.0F)
            .setSoundType(SoundType.METAL)
            .setCreativeTab(ExampleMod.MACHINES_TAB);

    static {
        COPPER_ORE.setHarvestLevel("pickaxe", 1);
        STEAM_ENGINE_CASING.setHarvestLevel("pickaxe", 1);
    }

    private ModBlocks() {}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(COPPER_ORE);
        event.getRegistry().register(MUFFLE_FURNACE);
        event.getRegistry().register(STEAM_ENGINE_CASING);
    }
}
