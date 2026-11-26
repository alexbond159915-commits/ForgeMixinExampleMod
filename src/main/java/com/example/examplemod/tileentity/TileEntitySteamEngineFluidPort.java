package com.example.examplemod.tileentity;

import com.example.examplemod.block.BlockSteamEngine;
import com.example.examplemod.block.BlockSteamEngineFluidPort;
import com.example.examplemod.fluid.IFluidProvider;
import com.example.examplemod.fluid.IFluidReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Pass-through tank capability: no duplicate storage in the port itself.
 * Each port resolves to exactly one matching controller and orientation.
 */
public class TileEntitySteamEngineFluidPort
        extends TileEntity implements IFluidReceiver, IFluidProvider
{
    public TileEntitySteamEngine getEngine()
    {
        if (world == null)
        {
            return null;
        }

        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockSteamEngineFluidPort))
        {
            return null;
        }

        BlockPos controller = com.example.examplemod.block.SteamEngineStructure
                .findController(world, pos);
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
        boolean output = state.getValue(BlockSteamEngineFluidPort.OUTPUT);
        if (state.getValue(BlockSteamEngineFluidPort.FACING) != front)
        {
            return null;
        }

        // Both sockets are on the rear column: upper = steam inlet,
        // lower = condensate outlet.
        BlockPos expected = BlockSteamEngine.getStructurePos(
                controller, front, 0, output ? 0 : 1, 2);
        return expected.equals(pos) ? engine : null;
    }

    private boolean isOutput()
    {
        return world != null
                && world.getBlockState(pos).getBlock()
                   instanceof BlockSteamEngineFluidPort
                && world.getBlockState(pos).getValue(
                   BlockSteamEngineFluidPort.OUTPUT);
    }

    private boolean isExternalFace(EnumFacing side)
    {
        if (side == null)
        {
            return true;
        }
        IBlockState state = world.getBlockState(pos);
        EnumFacing front = state.getValue(BlockSteamEngineFluidPort.FACING);
        return side == front.getOpposite();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return getEngine() != null && isExternalFace(facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                && isExternalFace(facing))
        {
            TileEntitySteamEngine engine = getEngine();
            if (engine != null)
            {
                IFluidHandler handler = isOutput()
                        ? engine.getFluidProvider()
                        : engine.getFluidReceiver();
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                        .cast(handler);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public IFluidHandler getFluidReceiver()
    {
        TileEntitySteamEngine engine = getEngine();
        return engine != null && !isOutput()
                ? engine.getFluidReceiver() : null;
    }

    @Override
    public IFluidHandler getFluidProvider()
    {
        TileEntitySteamEngine engine = getEngine();
        return engine != null && isOutput()
                ? engine.getFluidProvider() : null;
    }

    @Override
    public int getFluidPressure()
    {
        return 0;
    }

    @Override
    public int getFluidPriority()
    {
        return 0;
    }

    @Override
    public int getFluidInputRate()
    {
        return 200;
    }

    @Override
    public int getFluidOutputRate()
    {
        return 200;
    }
}
