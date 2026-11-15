package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.tileentity.TileEntitySteamEngine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        value = Side.CLIENT
)
public final class ClientEventSubscriber
{
    private ClientEventSubscriber()
    {
    }

    @SubscribeEvent
    public static void registerPipeTextures(TextureStitchEvent.Pre event)
    {
        TextureMap map = event.getMap();
        map.registerSprite(new ResourceLocation(ExampleMod.MODID, "blocks/fluid_pipe"));
        map.registerSprite(new ResourceLocation(ExampleMod.MODID, "blocks/fluid_pipe_end"));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        for (Item item : ModItems.ALL_ITEMS)
        {
            registerItemModel(item);
        }

        for (Block block : ModBlocks.ALL_BLOCKS)
        {
            Item item = Item.getItemFromBlock(block);

            if (item != null)
            {
                registerItemModel(item);
            }
        }

        ClientRegistry.bindTileEntitySpecialRenderer(
                TileEntitySteamEngine.class,
                new SteamEngineTileRenderer()
        );
    }

    private static void registerItemModel(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(
                        item.getRegistryName(),
                        "inventory"
                )
        );
    }
}
