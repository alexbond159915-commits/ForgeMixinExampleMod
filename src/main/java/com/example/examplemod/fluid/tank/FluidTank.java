package com.example.examplemod.fluid.tank;

import com.example.examplemod.fluid.FluidStack;
import com.example.examplemod.fluid.FluidType;
import com.example.examplemod.fluid.Fluids;
import net.minecraft.nbt.NBTTagCompound;

public class FluidTank {
    private FluidType type;
    private int amount;
    private final int capacity;
    private int pressure;

    public FluidTank(int capacity) {
        this.capacity = capacity;
        this.type = Fluids.NONE;
    }

    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.type == Fluids.NONE || resource.amount <= 0) return 0;
        if (type != Fluids.NONE && type != resource.type) return 0;
        if (pressure != resource.pressure && amount > 0) return 0;

        int accepted = Math.min(capacity - amount, resource.amount);
        if (doFill && accepted > 0) {
            type = resource.type;
            pressure = resource.pressure;
            amount += accepted;
        }
        return accepted;
    }

    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (amount <= 0 || type == Fluids.NONE || maxDrain <= 0) return null;
        int drained = Math.min(amount, maxDrain);
        FluidStack result = new FluidStack(type, drained, pressure);
        if (doDrain) {
            amount -= drained;
            if (amount == 0) {
                type = Fluids.NONE;
                pressure = 0;
            }
        }
        return result;
    }

    public FluidType getFluidType() { return type; }
    public int getFluidAmount() { return amount; }
    public int getCapacity() { return capacity; }
    public int getPressure() { return pressure; }
    public boolean isEmpty() { return amount <= 0 || type == Fluids.NONE; }

    public void setFluid(FluidStack stack) {
        if (stack == null || stack.amount <= 0) {
            type = Fluids.NONE;
            amount = 0;
            pressure = 0;
        } else {
            type = stack.type;
            amount = Math.min(stack.amount, capacity);
            pressure = stack.pressure;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setString("type", type.getName());
        tag.setInteger("amount", amount);
        tag.setInteger("pressure", pressure);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        type = Fluids.get(tag.getString("type"));
        amount = Math.min(tag.getInteger("amount"), capacity);
        pressure = tag.getInteger("pressure");
        if (amount <= 0) {
            type = Fluids.NONE;
            amount = 0;
            pressure = 0;
        }
    }
}
