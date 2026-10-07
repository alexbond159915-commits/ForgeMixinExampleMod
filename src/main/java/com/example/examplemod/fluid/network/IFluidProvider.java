package com.example.examplemod.fluid.network;

import com.example.examplemod.fluid.FluidStack;

public interface IFluidProvider {
    int getFluidAmount();
    FluidStack drainFluid(int amount, boolean doDrain);
}
