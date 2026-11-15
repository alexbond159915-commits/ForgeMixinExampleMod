package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.tileentity.TileEntitySteamEngine;
import com.example.examplemod.tileentity.TileEntitySteamEngineFluidPort;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;

/**
 * An actual capability-bearing face of the assembled steam engine.
 * output=false -> front steam inlet; output=true -> left condensate outlet.
 */
public class BlockSteamEngineFluidPort extends BlockContainerBase
{
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool OUTPUT =
            PropertyBool.create("output");

    public BlockSteamEngineFluidPort()
    {
        super(Material.IRON, "steam_engine_fluid_port", ExampleMod.MACHINES_TAB);
        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setResistance(6.0F);
        setCreativeTab(null);
        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.SOUTH)
                .withProperty(OUTPUT, false));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, OUTPUT);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex()
                | (state.getValue(OUTPUT) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(OUTPUT, (meta & 4) != 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntitySteamEngineFluidPort();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state,
            net.minecraft.world.IBlockAccess world, BlockPos pos)
    {
        return 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean removedByPlayer(
            IBlockState state, World world, BlockPos pos,
            EntityPlayer player, boolean willHarvest)
    {
        if (!world.isRemote &&
                SteamEngineStructure.removePartByPlayer(world, pos, player))
        {
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (!world.isRemote)
        {
            SteamEngineStructure.removePart(world, pos, true);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void getDrops(
            net.minecraft.util.NonNullList<ItemStack> drops,
            net.minecraft.world.IBlockAccess world,
            BlockPos pos, IBlockState state, int fortune)
    {
        // Internal ports never drop independently from the controller.
    }

    @Override
    public boolean onBlockActivated(
            World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing face,
            float hitX, float hitY, float hitZ)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntitySteamEngineFluidPort))
        {
            return true;
        }
        TileEntitySteamEngine engine =
                ((TileEntitySteamEngineFluidPort) tile).getEngine();
        if (engine == null)
        {
            return true;
        }

        if (world.isRemote)
        {
            return true;
        }

        boolean output = state.getValue(OUTPUT);
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty())
        {
            FluidActionResult result = output
                    ? FluidUtil.tryFillContainer(
                            stack, engine.getCondensateTank(), 1000, player, true)
                    : FluidUtil.tryEmptyContainer(
                            stack, engine.getSteamTank(), 1000, player, true);
            if (result.isSuccess())
            {
                player.setHeldItem(hand, result.getResult());
                engine.markDirty();
                return true;
            }
        }

        int amount = output
                ? engine.getCondensateTank().getFluidAmount()
                : engine.getSteamTank().getFluidAmount();
        int capacity = output
                ? engine.getCondensateTank().getCapacity()
                : engine.getSteamTank().getCapacity();
        player.sendStatusMessage(new TextComponentTranslation(
                output ? "message.examplemod.condensate_output"
                       : "message.examplemod.steam_input",
                amount, capacity), true);
        return true;
    }
}
