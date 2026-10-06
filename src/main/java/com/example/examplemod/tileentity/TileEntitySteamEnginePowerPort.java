package com.example.examplemod.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntitySteamEnginePowerPort
        extends TileEntity
{
    private TileEntitySteamEngine getEngine()
    {
        if (world == null)
        {
            return null;
        }

        TileEntity tile =
                world.getTileEntity(
                        pos.down()
                );

        if (tile instanceof TileEntitySteamEngine)
        {
            return (TileEntitySteamEngine) tile;
        }

        return null;
    }

    @Override
    public boolean hasCapability(
            Capability<?> capability,
            EnumFacing facing)
    {
        if (capability ==
                CapabilityEnergy.ENERGY)
        {
            return getEngine() != null;
        }

        return super.hasCapability(
                capability,
                facing
        );
    }

    @Override
    public <T> T getCapability(
            Capability<T> capability,
            EnumFacing facing)
    {
        if (capability ==
                CapabilityEnergy.ENERGY)
        {
            TileEntitySteamEngine engine =
                    getEngine();

            if (engine != null)
            {
                return CapabilityEnergy
                        .ENERGY
                        .cast(
                                engine.getCapability(
                                        CapabilityEnergy.ENERGY,
                                        null
                                )
                        );
            }
        }

        return super.getCapability(
                capability,
                facing
        );
    }
}
