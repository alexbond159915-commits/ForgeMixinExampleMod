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
import net.minecraftforge.client.model.OBJLoader;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy
        extends CommonProxy
{
    private static final ResourceLocation PIPE_MODEL =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "models/block/pipe_neo.obj"
            );

    private static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "blocks/fluid_pipe"
            );

    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "blocks/fluid_pipe_overlay"
            );

    @Override
    public void preInit(
            FMLPreInitializationEvent event)
    {
        OBJLoader.INSTANCE.addDomain(
                ExampleMod.MODID
        );

        MinecraftForge.EVENT_BUS.register(
                this
        );
    }

    @SubscribeEvent
    public void onModelRegistry(
            ModelRegistryEvent event)
    {
        final ResourceLocation blockLocation =
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
                                blockLocation,
                                "normal"
                        );
                    }
                }
        );

        Item item =
                Item.getItemFromBlock(
                        ModBlocks.FLUID_DUCT
                );

        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(
                    item,
                    0,
                    new ModelResourceLocation(
                            blockLocation,
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
                            PIPE_MODEL
                    );

            event.getModelRegistry().putObject(
                    new ModelResourceLocation(
                            new ResourceLocation(
                                    ExampleMod.MODID,
                                    "fluid_duct"
                            ),
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
                            new ResourceLocation(
                                    ExampleMod.MODID,
                                    "fluid_duct"
                            ),
                            "inventory"
                    ),
                    new FluidDuctBakedModel(
                            obj,
                            base,
                            overlay,
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
