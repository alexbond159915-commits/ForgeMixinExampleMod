package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.init.ModItems;
import com.example.examplemod.tileentity.TileEntitySteamEngine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
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
                new TileEntitySteamEngineRenderer()
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


    @net.minecraftforge.fml.relauncher.SideOnly(
            net.minecraftforge.fml.relauncher.Side.CLIENT
    )
    private static final class TileEntitySteamEngineRenderer
            extends net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer<TileEntitySteamEngine>
    {
        private final net.minecraft.item.ItemStack flywheel =
                new net.minecraft.item.ItemStack(ModItems.FLYWHEEL);

        @Override
        public void render(
                TileEntitySteamEngine engine,
                double x,
                double y,
                double z,
                float partialTicks,
                int destroyStage,
                float alpha)
        {
            if (engine == null || engine.getWorld() == null)
            {
                return;
            }

            float angle =
                    engine.lastRotor
                            + (engine.rotor - engine.lastRotor)
                            * partialTicks;

            net.minecraft.client.renderer.GlStateManager.pushMatrix();

            net.minecraft.client.renderer.GlStateManager.translate(
                    x + 0.5D,
                    y + 0.52D,
                    z + 0.5D
            );

            switch (engine.getFacing())
            {
                case NORTH:
                    net.minecraft.client.renderer.GlStateManager.rotate(
                            180F, 0F, 1F, 0F
                    );
                    break;

                case WEST:
                    net.minecraft.client.renderer.GlStateManager.rotate(
                            -90F, 0F, 1F, 0F
                    );
                    break;

                case EAST:
                    net.minecraft.client.renderer.GlStateManager.rotate(
                            90F, 0F, 1F, 0F
                    );
                    break;

                case SOUTH:
                default:
                    break;
            }

            /*
             * Placeholder rotor position. The final HBM-style model can
             * replace this item render without changing the machine logic.
             */
            net.minecraft.client.renderer.GlStateManager.translate(
                    0D,
                    0D,
                    0.53D
            );

            net.minecraft.client.renderer.GlStateManager.rotate(
                    angle,
                    0F,
                    0F,
                    1F
            );

            net.minecraft.client.renderer.GlStateManager.scale(
                    0.80F,
                    0.80F,
                    0.80F
            );

            net.minecraft.client.renderer.RenderHelper
                    .enableStandardItemLighting();

            net.minecraft.client.Minecraft
                    .getMinecraft()
                    .getRenderItem()
                    .renderItem(
                            flywheel,
                            net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED
                    );

            net.minecraft.client.renderer.RenderHelper
                    .disableStandardItemLighting();

            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
    }
}
