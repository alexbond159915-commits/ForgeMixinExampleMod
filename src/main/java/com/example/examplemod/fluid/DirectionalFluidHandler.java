package com.example.examplemod.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

/**
 * Side-restricted Forge fluid handler.
 *
 * Used by multiblock machines so their input and output ports are
 * independently visible to the fluid network.
 */
public final class DirectionalFluidHandler implements IFluidHandler
{
    private final ModFluidTank tank;
    private final boolean allowFill;
    private final boolean allowDrain;

    public DirectionalFluidHandler(
            ModFluidTank tank,
            boolean allowFill,
            boolean allowDrain)
    {
        this.tank = tank;
        this.allowFill = allowFill;
        this.allowDrain = allowDrain;
    }

    @Override
    public int fill(
            FluidStack resource,
            boolean doFill)
    {
        if (!allowFill)
        {
            return 0;
        }

        return tank.fill(resource, doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(
            FluidStack resource,
            boolean doDrain)
    {
        if (!allowDrain)
        {
            return null;
        }

        return tank.drain(resource, doDrain);
    }

    @Override
    @Nullable
    public FluidStack drain(
            int maxDrain,
            boolean doDrain)
    {
        if (!allowDrain)
        {
            return null;
        }

        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        FluidStack contents =
                tank.getFluid() == null
                        ? null
                        : tank.getFluid().copy();

        return new IFluidTankProperties[]
        {
                new FluidTankProperties(
                        contents,
                        tank.getCapacity(),
                        allowFill,
                        allowDrain
                )
        };
    }

    public ModFluidTank getTank()
    {
        return tank;
    }
}
