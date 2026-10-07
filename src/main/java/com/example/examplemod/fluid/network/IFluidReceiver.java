package com.example.examplemod.fluid.network;

import com.example.examplemod.fluid.FluidStack;

public interface IFluidReceiver {
    int getFluidDemand();
    int fillFluid(FluidStack stack, boolean doFill);
}
