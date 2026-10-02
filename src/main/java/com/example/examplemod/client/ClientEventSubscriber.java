package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Side.CLIENT)
public final class ClientEventSubscriber {
    private ClientEventSubscriber() {}

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerItemModel(ModItems.RAW_IRON);
        registerItemModel(ModItems.RAW_COPPER);
        registerItemModel(ModItems.COPPER_ORE_ITEM);
        registerItemModel(ModItems.COPPER_INGOT);
        registerItemModel(ModItems.MUFFLE_FURNACE_ITEM);
    }

    private static void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
