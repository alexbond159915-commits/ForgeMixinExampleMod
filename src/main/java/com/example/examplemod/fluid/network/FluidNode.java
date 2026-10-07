package com.example.examplemod.fluid.network;

import com.example.examplemod.fluid.FluidType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class FluidNode {
    public final BlockPos pos;
    public final EnumFacing[] connections;
    public FluidNetwork network;
    public FluidType type;

    public FluidNode(BlockPos pos, FluidType type, EnumFacing... connections) {
        this.pos = pos;
        this.type = type;
        this.connections = connections;
    }
}
