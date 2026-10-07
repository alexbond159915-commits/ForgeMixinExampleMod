package com.example.examplemod.fluid;

import net.minecraftforge.fluids.Fluid;

/**
 * HBM-style fluid identification target.
 *
 * A target can store a selected fluid type independently of the current
 * amount in its tank. Implementations may reject a new identification when
 * the stored contents would become incompatible with it.
 */
public interface IFluidIdentifierTarget
{
    Fluid getIdentifiedFluid();

    boolean setIdentifiedFluid(Fluid fluid);
}
