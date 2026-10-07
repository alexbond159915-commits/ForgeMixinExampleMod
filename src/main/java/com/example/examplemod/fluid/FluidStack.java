package com.example.examplemod.fluid;

public class FluidStack {
    public FluidType type;
    public int amount;
    public int pressure;

    public FluidStack(FluidType type, int amount) {
        this(type, amount, 0);
    }

    public FluidStack(FluidType type, int amount, int pressure) {
        this.type = type;
        this.amount = amount;
        this.pressure = pressure;
    }

    public FluidStack copy() {
        return new FluidStack(type, amount, pressure);
    }
}
