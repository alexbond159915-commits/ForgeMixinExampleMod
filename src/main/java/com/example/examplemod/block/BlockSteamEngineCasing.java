package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Normal craftable casing outside a multiblock, structural part inside one.
 */
public final class BlockSteamEngineCasing extends BlockBase
{
    /** Only assembled casings are invisible; standalone casings retain their block model. */
    public static final PropertyBool ASSEMBLED = PropertyBool.create("assembled");
    public BlockSteamEngineCasing()
    {
        super(Material.IRON, "steam_engine_casing", ExampleMod.MACHINES_TAB);
        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(blockState.getBaseState().withProperty(ASSEMBLED, false));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return !state.getValue(ASSEMBLED);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return !state.getValue(ASSEMBLED);
    }

    @Override
    public int getLightOpacity(IBlockState state,
            IBlockAccess world, BlockPos pos)
    {
        return state.getValue(ASSEMBLED) ? 0 :
                super.getLightOpacity(state, world, pos);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ASSEMBLED);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(ASSEMBLED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(ASSEMBLED, (meta & 1) != 0);
    }

    @Override
    public boolean removedByPlayer(
            IBlockState state, World world, BlockPos pos,
            EntityPlayer player, boolean willHarvest)
    {
        if (!world.isRemote &&
                SteamEngineStructure.removePartByPlayer(world, pos, player))
        {
            // The whole machine is already gone. Suppress casing drops.
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        if (!world.isRemote)
        {
            // Handles replacement, explosion and command-based removal.
            SteamEngineStructure.removePart(world, pos, true);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops,
                         IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune)
    {
        if (world instanceof World &&
                SteamEngineStructure.findController((World)world, pos) != null)
        {
            return;
        }
        // A freely placed casing still drops its own casing item.
        super.getDrops(drops, world, pos, state, fortune);
    }
}
