package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.DirectionalFluidHandler;
import com.example.examplemod.fluid.IFluidPipe;
import com.example.examplemod.fluid.ModFluidTank;
import com.example.examplemod.fluid.TileEntityFluidPipe;
import com.example.examplemod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class BlockFluidPipe
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

    private static final double CORE_MIN = 5D / 16D;
    private static final double CORE_MAX = 11D / 16D;

    public BlockFluidPipe()
    {
        super(
                Material.IRON,
                "fluid_pipe",
                ExampleMod.MACHINES_TAB
        );

        setHardness(2.0F);
        setResistance(6.0F);

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
                .withProperty(POS_X, canConnect(world, pos, EnumFacing.EAST))
                .withProperty(NEG_X, canConnect(world, pos, EnumFacing.WEST))
                .withProperty(POS_Y, canConnect(world, pos, EnumFacing.UP))
                .withProperty(NEG_Y, canConnect(world, pos, EnumFacing.DOWN))
                .withProperty(POS_Z, canConnect(world, pos, EnumFacing.SOUTH))
                .withProperty(NEG_Z, canConnect(world, pos, EnumFacing.NORTH));
    }

    private boolean canConnect(
            IBlockAccess world,
            BlockPos pos,
            EnumFacing side)
    {
        TileEntity selfTile =
                world.getTileEntity(pos);

        if (!(selfTile instanceof TileEntityFluidPipe))
        {
            return false;
        }

        BlockPos adjacentPos =
                pos.offset(side);

        IBlockState adjacentState =
                world.getBlockState(adjacentPos);

        Block adjacentBlock =
                adjacentState.getBlock();

        /*
         * The geometry of the pipe is based on the physical connection
         * between pipe blocks, not on whether the fluid identifier has
         * already been assigned. The network layer still decides whether
         * fluids are actually allowed to pass.
         */
        if (adjacentBlock == this
                || adjacentBlock == ModBlocks.FLUID_DUCT)
        {
            TileEntity adjacentTile =
                    world.getTileEntity(adjacentPos);

            return adjacentTile instanceof TileEntityFluidPipe;
        }

        Fluid fluid =
                ((TileEntityFluidPipe) selfTile)
                        .getPipeFluid();

        if (fluid == null)
        {
            return false;
        }

        TileEntity adjacentTile =
                world.getTileEntity(adjacentPos);

        if (adjacentTile == null)
        {
            return false;
        }

        Capability<IFluidHandler> capability =
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

        EnumFacing opposite =
                side.getOpposite();

        if (!adjacentTile.hasCapability(
                capability,
                opposite))
        {
            return false;
        }

        IFluidHandler handler =
                adjacentTile.getCapability(
                        capability,
                        opposite
                );

        if (handler == null)
        {
            return false;
        }

        Fluid declaredFluid =
                getDeclaredFluid(handler);

        if (declaredFluid != null)
        {
            return declaredFluid == fluid;
        }

        IFluidTankProperties[] properties =
                handler.getTankProperties();

        if (properties != null)
        {
            for (IFluidTankProperties property :
                    properties)
            {
                if (property == null)
                {
                    continue;
                }

                FluidStack contents =
                        property.getContents();

                if (contents != null
                        && contents.amount > 0)
                {
                    return contents.getFluid() == fluid;
                }
            }
        }

        return handler.fill(
                new FluidStack(fluid, 1),
                false
        ) > 0;
    }

    private Fluid getDeclaredFluid(
            IFluidHandler handler)
    {
        if (handler instanceof DirectionalFluidHandler)
        {
            return ((DirectionalFluidHandler) handler)
                    .getTank()
                    .getAllowedFluid();
        }

        if (handler instanceof ModFluidTank)
        {
            return ((ModFluidTank) handler)
                    .getAllowedFluid();
        }

        return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos)
    {
        boolean posX =
                canConnect(world, pos, EnumFacing.EAST);
        boolean negX =
                canConnect(world, pos, EnumFacing.WEST);
        boolean posY =
                canConnect(world, pos, EnumFacing.UP);
        boolean negY =
                canConnect(world, pos, EnumFacing.DOWN);
        boolean posZ =
                canConnect(world, pos, EnumFacing.SOUTH);
        boolean negZ =
                canConnect(world, pos, EnumFacing.NORTH);

        double minX = CORE_MIN;
        double minY = CORE_MIN;
        double minZ = CORE_MIN;
        double maxX = CORE_MAX;
        double maxY = CORE_MAX;
        double maxZ = CORE_MAX;

        if (negX) minX = 0D;
        if (posX) maxX = 1D;
        if (negY) minY = 0D;
        if (posY) maxY = 1D;
        if (negZ) minZ = 0D;
        if (posZ) maxZ = 1D;

        return new AxisAlignedBB(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

    @Override
    public void addCollisionBoxToList(
            IBlockState state,
            World world,
            BlockPos pos,
            AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes,
            Entity entity,
            boolean isActualState)
    {
        boolean posX =
                canConnect(world, pos, EnumFacing.EAST);
        boolean negX =
                canConnect(world, pos, EnumFacing.WEST);
        boolean posY =
                canConnect(world, pos, EnumFacing.UP);
        boolean negY =
                canConnect(world, pos, EnumFacing.DOWN);
        boolean posZ =
                canConnect(world, pos, EnumFacing.SOUTH);
        boolean negZ =
                canConnect(world, pos, EnumFacing.NORTH);

        // The central six-pixel core.
        addCollisionBoxToList(
                pos,
                entityBox,
                collidingBoxes,
                new AxisAlignedBB(
                        CORE_MIN,
                        CORE_MIN,
                        CORE_MIN,
                        CORE_MAX,
                        CORE_MAX,
                        CORE_MAX
                )
        );

        // Every connection gets its own collision box. This keeps L/T/cross
        // shapes from creating collision in the empty corners between arms.
        if (posX)
        {
            addCollisionBoxToList(
                    pos,
                    entityBox,
                    collidingBoxes,
                    new AxisAlignedBB(
                            CORE_MIN,
                            CORE_MIN,
                            CORE_MIN,
                            1D,
                            CORE_MAX,
                            CORE_MAX
                    )
            );
        }

        if (negX)
        {
            addCollisionBoxToList(
                    pos,
                    entityBox,
                    collidingBoxes,
                    new AxisAlignedBB(
                            0D,
                            CORE_MIN,
                            CORE_MIN,
                            CORE_MAX,
                            CORE_MAX,
                            CORE_MAX
                    )
            );
        }

        if (posY)
        {
            addCollisionBoxToList(
                    pos,
                    entityBox,
                    collidingBoxes,
                    new AxisAlignedBB(
                            CORE_MIN,
                            CORE_MIN,
                            CORE_MIN,
                            CORE_MAX,
                            1D,
                            CORE_MAX
                    )
            );
        }

        if (negY)
        {
            addCollisionBoxToList(
                    pos,
                    entityBox,
                    collidingBoxes,
                    new AxisAlignedBB(
                            CORE_MIN,
                            0D,
                            CORE_MIN,
                            CORE_MAX,
                            CORE_MAX,
                            CORE_MAX
                    )
            );
        }

        if (posZ)
        {
            addCollisionBoxToList(
                    pos,
                    entityBox,
                    collidingBoxes,
                    new AxisAlignedBB(
                            CORE_MIN,
                            CORE_MIN,
                            CORE_MIN,
                            CORE_MAX,
                            CORE_MAX,
                            1D
                    )
            );
        }

        if (negZ)
        {
            addCollisionBoxToList(
                    pos,
                    entityBox,
                    collidingBoxes,
                    new AxisAlignedBB(
                            CORE_MIN,
                            CORE_MIN,
                            0D,
                            CORE_MAX,
                            CORE_MAX,
                            CORE_MAX
                    )
            );
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos)
    {
        return getBoundingBox(
                state,
                world,
                pos
        );
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(
            IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos)
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
    public void neighborChanged(
            IBlockState state,
            World world,
            BlockPos pos,
            Block blockIn,
            BlockPos fromPos)
    {
        super.neighborChanged(
                state,
                world,
                pos,
                blockIn,
                fromPos
        );

        if (!world.isRemote)
        {
            world.notifyBlockUpdate(
                    pos,
                    state,
                    state,
                    3
            );
        }

        world.markBlockRangeForRenderUpdate(
                pos.add(-1, -1, -1),
                pos.add(1, 1, 1)
        );
    }

    @Override
    public int getMetaFromState(
            IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(
            int meta)
    {
        return getDefaultState();
    }

    @Override
    public TileEntity createNewTileEntity(
            World world,
            int meta)
    {
        return new TileEntityFluidPipe();
    }
}
