package com.example.examplemod.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Forge FluidTank with HBM-like additions:
 * 1) a mutable fluid filter/identifier;
 * 2) a pressure value reserved for the future fluid-network layer.
 */
public class ModFluidTank extends FluidTank
{
    private Fluid allowedFluid;
    private int pressure;

    public ModFluidTank(
            TileEntity owner,
            int capacity,
            Fluid allowedFluid)
    {
        super(capacity);

        this.allowedFluid = allowedFluid;
        this.pressure = 0;

        setTileEntity(owner);
    }

    public Fluid getAllowedFluid()
    {
        return allowedFluid;
    }

    /**
     * Identifies this tank for one fluid type.
     *
     * A tank may only change its identification while empty or while the
     * new type matches its current contents.
     */
    public boolean setAllowedFluid(Fluid fluid)
    {
        if (fluid == null)
        {
            if (this.fluid != null && this.fluid.amount > 0)
            {
                return false;
            }

            this.allowedFluid = null;
        }
        else
        {
            if (this.fluid != null
                    && this.fluid.amount > 0
                    && this.fluid.getFluid() != fluid)
            {
                return false;
            }

            this.allowedFluid = fluid;
        }

        if (tile != null)
        {
            tile.markDirty();
        }

        return true;
    }

    public int getPressure()
    {
        return pressure;
    }

    public void setPressure(int pressure)
    {
        this.pressure = Math.max(0, pressure);
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid)
    {
        return fluid != null
                && (allowedFluid == null
                || fluid.getFluid() == allowedFluid);
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluid)
    {
        if (fluid == null)
        {
            return false;
        }

        return this.fluid != null
                && this.fluid.getFluid() == fluid.getFluid();
    }

    @Override
    protected void onContentsChanged()
    {
        super.onContentsChanged();

        if (tile != null)
        {
            tile.markDirty();
        }
    }

    public NBTTagCompound writeToNBTWithPressure(
            NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (allowedFluid != null)
        {
            nbt.setString(
                    "IdentifiedFluid",
                    allowedFluid.getName()
            );
        }
        else
        {
            nbt.removeTag("IdentifiedFluid");
        }

        nbt.setInteger(
                "Pressure",
                pressure
        );

        return nbt;
    }

    public ModFluidTank readFromNBTWithPressure(
            NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        if (nbt.hasKey("IdentifiedFluid"))
        {
            Fluid identified =
                    FluidRegistry.getFluid(
                            nbt.getString("IdentifiedFluid")
                    );

            if (identified != null)
            {
                allowedFluid = identified;
            }
        }

        pressure = nbt.getInteger("Pressure");

        return this;
    }
}
