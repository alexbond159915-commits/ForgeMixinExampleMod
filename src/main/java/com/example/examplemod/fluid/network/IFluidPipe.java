package com.example.examplemod.fluid.network;

import com.example.examplemod.fluid.FluidType;
import net.minecraft.util.EnumFacing;

public interface IFluidPipe {
    FluidType getPipeFluid();
    boolean canConnect(EnumFacing side, FluidType type);
}
