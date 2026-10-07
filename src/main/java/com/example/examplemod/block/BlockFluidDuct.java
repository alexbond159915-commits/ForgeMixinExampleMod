package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.IFluidPipe;
import com.example.examplemod.fluid.TileEntityFluidPipe;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

/**
 * Small HBM-style fluid duct.
 *
 * The center is 6x6x6 px and each connected direction gets a 6x6 px arm.
 * Connectivity is calculated from nearby ducts/pipes and Forge fluid
 * capabilities, so the model changes automatically as machines are attached.
 */
public class BlockFluidDuct
        extends BlockContainerBase
        implements IFluidPipe
{
    public static final PropertyBool POS_X =
            PropertyBool.create("posx");
    public static final PropertyBool NEG_X =
            PropertyBool.create("negx");
    public static final PropertyBool POS_Y =
            PropertyBool.create("posy");
    public static final PropertyBool NEG_Y =
            PropertyBool.create("negy");
    public static final PropertyBool POS_Z =
            PropertyBool.create("posz");
    public static final PropertyBool NEG_Z =
            PropertyBool.create("negz");

    private static final double MIN = 5D / 16D;
    private static final double MAX = 11D / 16D;

    private static final AxisAlignedBB CENTER =
            new AxisAlignedBB(MIN, MIN, MIN, MAX, MAX, MAX);

    public BlockFluidDuct()
    {
        super(
                Material.IRON,
                "fluid_duct",
                ExampleMod.MACHINES_TAB
        );

        setSoundType(SoundType.METAL);
        setHardness(2.0F);
        setResistance(6.0F);
        setLightOpacity(0);
        useNeighborBrightness = true;

        setDefaultState(
                blockState.getBaseState()
                        .withProperty(POS_X, false)
                        .withProperty(NEG_X, false)
                        .withProperty(POS_Y, false)
                        .withProperty(NEG_Y, false)
                        .withProperty(POS_Z, false)
                        .withProperty(NEG_Z, false)
        );
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(
                this,
                POS_X,
                NEG_X,
                POS_Y,
                NEG_Y,
                POS_Z,
                NEG_Z
        );
    }

    @Override
    public IBlockState getActualState(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos)
    {
        return state
                .withProperty(
                        POS_X,
                        canConnect(world, pos, EnumFacing.EAST)
                )
                .withProperty(
                        NEG_X,
                        canConnect(world, pos, EnumFacing.WEST)
                )
                .withProperty(
                        POS_Y,
                        canConnect(world, pos, EnumFacing.UP)
                )
                .withProperty(
                        NEG_Y,
                        canConnect(world, pos, EnumFacing.DOWN)
                )
                .withProperty(
                        POS_Z,
                        canConnect(world, pos, EnumFacing.SOUTH)
                )
                .withProperty(
                        NEG_Z,
                        canConnect(world, pos, EnumFacing.NORTH)
                );
    }

    private boolean canConnect(
            IBlockAccess world,
            BlockPos pos,
            EnumFacing side)
    {
        BlockPos adjacent = pos.offset(side);
        IBlockState adjacentState =
                world.getBlockState(adjacent);

        Block block = adjacentState.getBlock();

        /*
         * Both the new small duct and the existing network pipe
         * are direct pipe-to-pipe connections.
         */
        if (block instanceof BlockFluidDuct)
        {
            return true;
        }

        /*
         * Existing large/basic fluid pipe uses the same network TE.
         */
        if (block == com.example.examplemod.init.ModBlocks.FLUID_PIPE)
        {
            return true;
        }

        TileEntity tile =
                world.getTileEntity(adjacent);

        if (tile == null)
        {
            return false;
        }

        Capability<net.minecraftforge.fluids.capability.IFluidHandler> capability =
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

        return tile.hasCapability(
                capability,
                side.getOpposite()
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos)
    {
        boolean px = canConnect(world, pos, EnumFacing.EAST);
        boolean nx = canConnect(world, pos, EnumFacing.WEST);
        boolean py = canConnect(world, pos, EnumFacing.UP);
        boolean ny = canConnect(world, pos, EnumFacing.DOWN);
        boolean pz = canConnect(world, pos, EnumFacing.SOUTH);
        boolean nz = canConnect(world, pos, EnumFacing.NORTH);

        return new AxisAlignedBB(
                nx ? 0D : MIN,
                ny ? 0D : MIN,
                nz ? 0D : MIN,
                px ? 1D : MAX,
                py ? 1D : MAX,
                pz ? 1D : MAX
        );
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos)
    {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(
            IBlockAccess world,
            IBlockState state,
            BlockPos pos,
            EnumFacing face)
    {
        return BlockFaceShape.CENTER;
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(
            World world,
            int meta)
    {
        return new TileEntityFluidPipe();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }
}
