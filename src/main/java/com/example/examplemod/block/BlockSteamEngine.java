package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.tileentity.TileEntitySteamEngine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSteamEngine extends BlockContainerBase
{
    public static final PropertyDirection FACING =
            PropertyDirection.create(
                    "facing",
                    EnumFacing.Plane.HORIZONTAL
            );

    private static final int WIDTH = 2;
    private static final int HEIGHT = 2;
    private static final int DEPTH = 3;

    public BlockSteamEngine()
    {
        super(
                Material.IRON,
                "steam_engine",
                ExampleMod.MACHINES_TAB
        );

        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setResistance(6.0F);

        setDefaultState(
                blockState.getBaseState()
                        .withProperty(
                                FACING,
                                EnumFacing.NORTH
                        )
        );
    }

    @Override
    public boolean canPlaceBlockAt(
            World world,
            BlockPos pos)
    {
        if (!super.canPlaceBlockAt(world, pos))
        {
            return false;
        }

        /*
         * We require the complete footprint to be free for every
         * horizontal orientation. This avoids placing the core first and
         * overwriting a blocked part of the multiblock afterward.
         */
        return hasFreeStructureSpace(
                world,
                pos,
                EnumFacing.NORTH
        )
                && hasFreeStructureSpace(
                world,
                pos,
                EnumFacing.SOUTH
        )
                && hasFreeStructureSpace(
                world,
                pos,
                EnumFacing.WEST
        )
                && hasFreeStructureSpace(
                world,
                pos,
                EnumFacing.EAST
        );
    }

    @Override
    public IBlockState getStateForPlacement(
            World world,
            BlockPos pos,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ,
            int meta,
            EntityLivingBase placer)
    {
        EnumFacing machineFacing =
                placer == null
                        ? EnumFacing.NORTH
                        : placer.getHorizontalFacing().getOpposite();

        return getDefaultState()
                .withProperty(
                        FACING,
                        machineFacing
                );
    }

    @Override
    public void onBlockPlacedBy(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityLivingBase placer,
            ItemStack stack)
    {
        super.onBlockPlacedBy(
                world,
                pos,
                state,
                placer,
                stack
        );

        if (!world.isRemote)
        {
            buildStructure(
                    world,
                    pos,
                    state.getValue(FACING)
            );
        }
    }

    @Override
    public void breakBlock(
            World world,
            BlockPos pos,
            IBlockState state)
    {
        if (!world.isRemote)
        {
            removeStructureParts(
                    world,
                    pos,
                    state.getValue(FACING)
            );
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ)
    {
        /*
         * GUI/control panel will be added later.
         */
        return true;
    }

    private boolean hasFreeStructureSpace(
            World world,
            BlockPos core,
            EnumFacing facing)
    {
        for (int depth = 0; depth < DEPTH; depth++)
        {
            for (int height = 0; height < HEIGHT; height++)
            {
                for (int width = 0; width < WIDTH; width++)
                {
                    if (width == 0
                            && height == 0
                            && depth == 0)
                    {
                        continue;
                    }

                    if (!world.isAirBlock(
                            getStructurePos(
                                    core,
                                    facing,
                                    width,
                                    height,
                                    depth
                            )))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void buildStructure(
            World world,
            BlockPos core,
            EnumFacing facing)
    {
        for (int depth = 0; depth < DEPTH; depth++)
        {
            for (int height = 0; height < HEIGHT; height++)
            {
                for (int width = 0; width < WIDTH; width++)
                {
                    if (width == 0
                            && height == 0
                            && depth == 0)
                    {
                        continue;
                    }

                    BlockPos target =
                            getStructurePos(
                                    core,
                                    facing,
                                    width,
                                    height,
                                    depth
                            );

                    /*
                     * Energy output block sits above the controller and
                     * forwards Forge Energy from the core.
                     */
                    if (width == 0
                            && height == 1
                            && depth == 0)
                    {
                        world.setBlockState(
                                target,
                                ModBlocks.STEAM_ENGINE_POWER_PORT
                                        .getDefaultState(),
                                2
                        );
                    }
                    else
                    {
                        world.setBlockState(
                                target,
                                ModBlocks.STEAM_ENGINE_CASING
                                        .getDefaultState(),
                                2
                        );
                    }
                }
            }
        }
    }

    private void removeStructureParts(
            World world,
            BlockPos core,
            EnumFacing facing)
    {
        for (int depth = 0; depth < DEPTH; depth++)
        {
            for (int height = 0; height < HEIGHT; height++)
            {
                for (int width = 0; width < WIDTH; width++)
                {
                    if (width == 0
                            && height == 0
                            && depth == 0)
                    {
                        continue;
                    }

                    BlockPos target =
                            getStructurePos(
                                    core,
                                    facing,
                                    width,
                                    height,
                                    depth
                            );

                    net.minecraft.block.Block block =
                            world.getBlockState(target).getBlock();

                    if (block == ModBlocks.STEAM_ENGINE_CASING
                            || block == ModBlocks.STEAM_ENGINE_POWER_PORT)
                    {
                        world.setBlockToAir(target);
                    }
                }
            }
        }
    }

    public static BlockPos getStructurePos(
            BlockPos core,
            EnumFacing facing,
            int width,
            int height,
            int depth)
    {
        EnumFacing right = facing.rotateY();
        EnumFacing back = facing.getOpposite();

        return core
                .offset(right, width)
                .offset(EnumFacing.UP, height)
                .offset(back, depth);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state
                .getValue(FACING)
                .getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        switch (meta)
        {
            case 0:
                return getDefaultState()
                        .withProperty(FACING, EnumFacing.SOUTH);
            case 1:
                return getDefaultState()
                        .withProperty(FACING, EnumFacing.WEST);
            case 2:
                return getDefaultState()
                        .withProperty(FACING, EnumFacing.NORTH);
            case 3:
                return getDefaultState()
                        .withProperty(FACING, EnumFacing.EAST);
            default:
                return getDefaultState()
                        .withProperty(FACING, EnumFacing.NORTH);
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(
                this,
                FACING
        );
    }

    @Override
    public net.minecraft.tileentity.TileEntity createNewTileEntity(
            World world,
            int meta)
    {
        return new TileEntitySteamEngine();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
}
