package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.tileentity.TileEntitySteamEnginePowerPort;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSteamEnginePowerPort extends BlockContainerBase
{
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public BlockSteamEnginePowerPort()
    {
        super(
                Material.IRON,
                "steam_engine_power_port",
                ExampleMod.MACHINES_TAB
        );

        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setResistance(6.0F);

        /*
         * This is an internal structural block; the finished machine is
         * the normal player-facing block.
         */
        setCreativeTab(null);
        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.SOUTH));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public net.minecraft.tileentity.TileEntity createNewTileEntity(
            World world,
            int meta)
    {
        return new TileEntitySteamEnginePowerPort();
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
            net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops,
            net.minecraft.world.IBlockAccess world,
            BlockPos pos, IBlockState state, int fortune)
    {
        // Internal energy port never drops independently.
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
        if (!world.isRemote)
        {
            net.minecraft.tileentity.TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySteamEnginePowerPort)
            {
                net.minecraftforge.energy.IEnergyStorage power =
                        tile.getCapability(
                                net.minecraftforge.energy.CapabilityEnergy.ENERGY,
                                facing);
                if (power != null)
                {
                    player.sendStatusMessage(
                            new net.minecraft.util.text.TextComponentTranslation(
                                    "message.examplemod.energy_output",
                                    power.getEnergyStored(),
                                    power.getMaxEnergyStored()),
                            true);
                }
            }
        }
        return true;
    }
}
