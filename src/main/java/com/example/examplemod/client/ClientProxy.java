package com.example.examplemod.client;

import com.example.examplemod.CommonProxy;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.model.FluidDuctBakedModel;
import com.example.examplemod.fluid.TileEntityFluidPipe;
import com.example.examplemod.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy
        extends CommonProxy
{
    /*
     * Use the actual HBM standard fluid duct texture.
     * The old fluid_pipe texture belongs to the separate legacy pipe.
     */
    private static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "blocks/fluid_duct"
            );

    /*
     * HBM-style transparent overlay used for the colored fluid layer.
     */
    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "blocks/fluid_duct_overlay"
            );

    private static final ResourceLocation PIPE_TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "blocks/fluid_pipe"
            );

    private static final ResourceLocation PIPE_END_TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "blocks/fluid_pipe_end"
            );

    @Override
    public void preInit(
            FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(
                this
        );
    }

    @SubscribeEvent
    public void onModelRegistry(
            ModelRegistryEvent event)
    {
        final ResourceLocation ductLocation =
                new ResourceLocation(
                        ExampleMod.MODID,
                        "fluid_duct"
                );

        ModelLoader.setCustomStateMapper(
                ModBlocks.FLUID_DUCT,
                new StateMapperBase()
                {
                    @Override
                    protected ModelResourceLocation
                    getModelResourceLocation(
                            IBlockState state)
                    {
                        return new ModelResourceLocation(
                                ductLocation,
                                "normal"
                        );
                    }
                }
        );

        Item ductItem =
                Item.getItemFromBlock(
                        ModBlocks.FLUID_DUCT
                );

        if (ductItem != null)
        {
            ModelLoader.setCustomModelResourceLocation(
                    ductItem,
                    0,
                    new ModelResourceLocation(
                            ductLocation,
                            "inventory"
                    )
            );
        }

        /*
         * Explicitly register the tank block/item model.
         * This avoids relying on automatic state mapping for this
         * TileEntity block.
         */
        final ResourceLocation tankLocation =
                new ResourceLocation(
                        ExampleMod.MODID,
                        "fluid_tank"
                );

        ModelLoader.setCustomStateMapper(
                ModBlocks.FLUID_TANK,
                new StateMapperBase()
                {
                    @Override
                    protected ModelResourceLocation
                    getModelResourceLocation(
                            IBlockState state)
                    {
                        return new ModelResourceLocation(
                                tankLocation,
                                "normal"
                        );
                    }
                }
        );

        Item tankItem =
                Item.getItemFromBlock(
                        ModBlocks.FLUID_TANK
                );

        if (tankItem != null)
        {
            ModelLoader.setCustomModelResourceLocation(
                    tankItem,
                    0,
                    new ModelResourceLocation(
                            tankLocation,
                            "inventory"
                    )
            );
        }


        /*
         * The fluid pipe is a simple fixed-shape block, so always map
         * every block state to the custom baked "normal" model.
         * This avoids any dependency on multipart/property state JSON.
         */
        final ResourceLocation pipeLocation =
                new ResourceLocation(
                        ExampleMod.MODID,
                        "fluid_pipe"
                );

        ModelLoader.setCustomStateMapper(
                ModBlocks.FLUID_PIPE,
                new StateMapperBase()
                {
                    @Override
                    protected ModelResourceLocation
                    getModelResourceLocation(
                            IBlockState state)
                    {
                        return new ModelResourceLocation(
                                pipeLocation,
                                "normal"
                        );
                    }
                }
        );

        Item pipeItem =
                Item.getItemFromBlock(
                        ModBlocks.FLUID_PIPE
                );

        if (pipeItem != null)
        {
            ModelLoader.setCustomModelResourceLocation(
                    pipeItem,
                    0,
                    new ModelResourceLocation(
                            pipeLocation,
                            "inventory"
                    )
            );
        }
    }

    @SubscribeEvent
    public void onTextureStitch(
            TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(
                BASE_TEXTURE
        );

        event.getMap().registerSprite(
                OVERLAY_TEXTURE
        );

        event.getMap().registerSprite(
                PIPE_TEXTURE
        );

        event.getMap().registerSprite(
                PIPE_END_TEXTURE
        );
    }

    @SubscribeEvent
    public void onModelBake(
            ModelBakeEvent event)
    {
        try
        {
            TextureMap map =
                    Minecraft.getMinecraft()
                            .getTextureMapBlocks();

            TextureAtlasSprite base =
                    map.getAtlasSprite(
                            BASE_TEXTURE.toString()
                    );

            TextureAtlasSprite overlay =
                    map.getAtlasSprite(
                            OVERLAY_TEXTURE.toString()
                    );

            FluidDuctBakedModel.ObjModel obj =
                    FluidDuctBakedModel.load(
                            new ResourceLocation(
                                    ExampleMod.MODID,
                                    "models/block/pipe_neo.obj"
                            )
                    );

            ResourceLocation ductLocation =
                    new ResourceLocation(
                            ExampleMod.MODID,
                            "fluid_duct"
                    );

            event.getModelRegistry().putObject(
                    new ModelResourceLocation(
                            ductLocation,
                            "normal"
                    ),
                    new FluidDuctBakedModel(
                            obj,
                            base,
                            overlay,
                            true
                    )
            );

            event.getModelRegistry().putObject(
                    new ModelResourceLocation(
                            ductLocation,
                            "inventory"
                    ),
                    new FluidDuctBakedModel(
                            obj,
                            base,
                            overlay,
                            false
                    )
            );

            TextureAtlasSprite pipeBase =
                    map.getAtlasSprite(
                            PIPE_TEXTURE.toString()
                    );

            TextureAtlasSprite pipeEnd =
                    map.getAtlasSprite(
                            PIPE_END_TEXTURE.toString()
                    );

            ResourceLocation pipeLocation =
                    new ResourceLocation(
                            ExampleMod.MODID,
                            "fluid_pipe"
                    );

            event.getModelRegistry().putObject(
                    new ModelResourceLocation(
                            pipeLocation,
                            "normal"
                    ),
                    FluidDuctBakedModel.forSimplePipe(
                            null,
                            pipeBase,
                            pipeEnd,
                            true
                    )
            );

            event.getModelRegistry().putObject(
                    new ModelResourceLocation(
                            pipeLocation,
                            "inventory"
                    ),
                    FluidDuctBakedModel.forSimplePipe(
                            null,
                            pipeBase,
                            pipeEnd,
                            false
                    )
            );
        }
        catch (Exception exception)
        {
            throw new RuntimeException(
                    "Failed to bake HBM-style fluid duct model",
                    exception
            );
        }
    }

    @SubscribeEvent
    public void onBlockColor(
            ColorHandlerEvent.Block event)
    {
        IBlockColor colorHandler =
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(
                            IBlockState state,
                            net.minecraft.world.IBlockAccess world,
                            net.minecraft.util.math.BlockPos pos,
                            int tintIndex)
                    {
                        if (tintIndex != 1
                                || world == null
                                || pos == null)
                        {
                            return 0xFFFFFF;
                        }

                        net.minecraft.tileentity.TileEntity tile =
                                world.getTileEntity(pos);

                        if (!(tile instanceof TileEntityFluidPipe))
                        {
                            return 0xFFFFFF;
                        }

                        net.minecraftforge.fluids.Fluid fluid =
                                ((TileEntityFluidPipe) tile)
                                        .getPipeFluid();

                        if (fluid == null)
                        {
                            return 0xFFFFFF;
                        }

                        return fluid.getColor()
                                & 0xFFFFFF;
                    }
                };

        event.getBlockColors().registerBlockColorHandler(
                colorHandler,
                ModBlocks.FLUID_DUCT
        );
    }
}
