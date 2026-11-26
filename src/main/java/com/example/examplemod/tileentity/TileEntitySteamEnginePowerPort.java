package com.example.examplemod.tileentity;

import com.example.examplemod.block.BlockSteamEngine;
import com.example.examplemod.block.BlockSteamEnginePowerPort;
import com.example.examplemod.block.SteamEngineStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Copper cable output on the same rear face as both fluid connectors.
 * The energy buffer remains on the controller.
 */
public class TileEntitySteamEnginePowerPort extends TileEntity
{
    private TileEntitySteamEngine getEngine()
    {
        if (world == null)
        {
            return null;
        }

        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockSteamEnginePowerPort))
        {
            return null;
        }

        BlockPos controller = SteamEngineStructure.findController(world, pos);
        if (controller == null)
        {
            return null;
        }

        TileEntity tile = world.getTileEntity(controller);
        if (!(tile instanceof TileEntitySteamEngine))
        {
            return null;
        }

        TileEntitySteamEngine engine = (TileEntitySteamEngine) tile;
        EnumFacing front = engine.getFacing();
        BlockPos expected = BlockSteamEngine.getStructurePos(
                controller, front, 1, 1, 2);
        return expected.equals(pos)
                && state.getValue(BlockSteamEnginePowerPort.FACING) == front
                ? engine : null;
    }

    private boolean isCableFace(TileEntitySteamEngine engine,
                                EnumFacing side)
    {
        return side == null || side == engine.getFacing().getOpposite();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
        {
            TileEntitySteamEngine engine = getEngine();
            return engine != null && isCableFace(engine, facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
        {
            TileEntitySteamEngine engine = getEngine();
            if (engine != null && isCableFace(engine, facing))
            {
                IEnergyStorage buffer =
                        engine.getCapability(CapabilityEnergy.ENERGY, null);
                if (buffer != null)
                {
                    return CapabilityEnergy.ENERGY.cast(buffer);
                }
            }
        }
        return super.getCapability(capability, facing);
    }
}
