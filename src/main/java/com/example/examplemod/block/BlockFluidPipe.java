package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.IFluidPipe;
import com.example.examplemod.fluid.TileEntityFluidPipe;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

public class BlockFluidPipe
        extends BlockContainerBase
        implements IFluidPipe
{
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing");

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
                        .withProperty(
                                FACING,
                                EnumFacing.NORTH
                        )
        );
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
        return getDefaultState().withProperty(
                FACING,
                facing
        );
    }

    @Override
    public int getMetaFromState(
            IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(
            int meta)
    {
        EnumFacing facing =
                EnumFacing.getFront(meta);

        if (facing == null)
        {
            facing = EnumFacing.NORTH;
        }

        return getDefaultState().withProperty(
                FACING,
                facing
        );
    }

    @Override
    public TileEntity createNewTileEntity(
            World world,
            int meta)
    {
        return new TileEntityFluidPipe();
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
}