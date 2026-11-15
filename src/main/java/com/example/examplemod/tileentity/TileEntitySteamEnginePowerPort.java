package com.example.examplemod.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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

        /*
         * The power port sits above the front-right casing. The
         * controller is one block DOWN and one block LEFT from it,
         * relative to the machine's front. It is NOT directly below.
         */
        for (EnumFacing facing : EnumFacing.HORIZONTALS)
        {
            BlockPos controllerPos = pos.down()
                    .offset(facing.rotateY().getOpposite());
            TileEntity tile = world.getTileEntity(controllerPos);
            if (!(tile instanceof TileEntitySteamEngine))
            {
                continue;
            }

            TileEntitySteamEngine engine =
                    (TileEntitySteamEngine) tile;
            if (engine.getFacing() == facing
                    && com.example.examplemod.block.BlockSteamEngine
                    .getStructurePos(controllerPos, facing, 1, 1, 0)
                    .equals(pos))
            {
                return engine;
            }
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
