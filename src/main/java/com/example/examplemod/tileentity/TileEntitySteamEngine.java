package com.example.examplemod.tileentity;

import com.example.examplemod.block.BlockSteamEngine;
import com.example.examplemod.fluid.DirectionalFluidHandler;
import com.example.examplemod.fluid.IFluidProvider;
import com.example.examplemod.fluid.IFluidReceiver;
import com.example.examplemod.fluid.ModFluidTank;
import com.example.examplemod.fluid.ModFluids;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

/**
 * Simplified implementation of the smallest HBM steam-engine principle:
 * consume steam, produce spent/condensed fluid, generate power, and spin
 * a rotor while working.
 */
public class TileEntitySteamEngine
        extends TileEntity
        implements ITickable, IFluidReceiver, IFluidProvider
{
    private static final int STEAM_CAPACITY = 2000;
    private static final int CONDENSATE_CAPACITY = 20;

    private static final int STEAM_PER_OPERATION = 100;
    private static final int CONDENSATE_PER_OPERATION = 1;
    private static final int RAW_ENERGY_PER_OPERATION = 200;
    private static final double EFFICIENCY = 0.85D;

    private static final int ENERGY_PER_OPERATION =
            (int) (RAW_ENERGY_PER_OPERATION * EFFICIENCY);

    private static final int ENERGY_CAPACITY = 1000000;
    private static final int ENERGY_MAX_EXTRACT = 10000;

    private static final int ROTOR_MAX_ACCELERATION = 40;

    private final ModFluidTank steamTank =
            new ModFluidTank(
                    this,
                    STEAM_CAPACITY,
                    ModFluids.STEAM.getForgeFluid()
            );

    private final ModFluidTank condensateTank =
            new ModFluidTank(
                    this,
                    CONDENSATE_CAPACITY,
                    ModFluids.CONDENSATE.getForgeFluid()
            );

    /*
     * HBM-style separate fluid connections:
     * front = steam input, left = condensate output.
     */
    private final DirectionalFluidHandler steamHandler =
            new DirectionalFluidHandler(
                    steamTank,
                    true,
                    false
            );

    private final DirectionalFluidHandler condensateHandler =
            new DirectionalFluidHandler(
                    condensateTank,
                    false,
                    true
            );

    private final IFluidHandler allFluidHandler =
            new FluidHandlerConcatenate(
                    steamHandler,
                    condensateHandler
            );

    /*
     * FE buffer is deliberately output-only for now.
     */
    private final OutputEnergyStorage energyStorage =
            new OutputEnergyStorage(
                    ENERGY_CAPACITY,
                    ENERGY_MAX_EXTRACT
            );

    public float rotor;
    public float lastRotor;

    /*
     * The TESR draws the flywheel and linkage across the 2x2 front
     * and two copper rails over the long boiler.
     * Make sure Forge does not cull the renderer when the controller
     * alone is outside the camera frustum.
     */
    @net.minecraftforge.fml.relauncher.SideOnly(
            net.minecraftforge.fml.relauncher.Side.CLIENT)
    @Override
    public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox()
    {
        return new net.minecraft.util.math.AxisAlignedBB(
                pos.add(-3, -1, -3),
                pos.add(3, 4, 3)
        );
    }

    private float synchronizedRotor;
    private float acceleration;

    // Existing pre-redesign multiblocks get the new ports on first server tick.
    private boolean portLayoutChecked;

    private void markAssembledCasingModels()
    {
        EnumFacing facing = getFacing();
        for (int depth = 0; depth < 3; depth++)
        {
            for (int height = 0; height < 2; height++)
            {
                for (int width = 0; width < 2; width++)
                {
                    net.minecraft.util.math.BlockPos part =
                            BlockSteamEngine.getStructurePos(
                                    pos, facing, width, height, depth);
                    if (!world.isBlockLoaded(part))
                    {
                        continue;
                    }
                    IBlockState partState = world.getBlockState(part);
                    if (partState.getBlock() ==
                            com.example.examplemod.init.ModBlocks.STEAM_ENGINE_CASING
                            && !partState.getValue(
                                    com.example.examplemod.block.BlockSteamEngineCasing.ASSEMBLED))
                    {
                        world.setBlockState(part, partState.withProperty(
                                com.example.examplemod.block.BlockSteamEngineCasing.ASSEMBLED,
                                true), 2);
                    }
                }
            }
        }
    }

    @Override
    public void update()
    {
        if (world == null)
        {
            return;
        }

        if (world.isRemote)
        {
            lastRotor = rotor;

            float delta =
                    synchronizedRotor - rotor;

            if (delta > 180F)
            {
                delta -= 360F;
            }
            else if (delta < -180F)
            {
                delta += 360F;
            }

            rotor += delta / 3F;

            if (rotor >= 360F)
            {
                rotor -= 360F;
            }
            else if (rotor < 0F)
            {
                rotor += 360F;
            }

            return;
        }

        // Existing worlds can contain casings saved before ASSEMBLED metadata
        // existed. Convert only known machine casings; leave loose blocks alone.
        if ((world.getTotalWorldTime() % 40L) == 0L)
        {
            markAssembledCasingModels();
        }

        if (!portLayoutChecked)
        {
            portLayoutChecked = true;
            upgradeLegacyPorts();
        }

        int availableOperations =
                steamTank.getFluidAmount()
                        / STEAM_PER_OPERATION;

        int outputSpace =
                (CONDENSATE_CAPACITY
                        - condensateTank.getFluidAmount())
                        / CONDENSATE_PER_OPERATION;

        int energySpace =
                (energyStorage.getMaxEnergyStored()
                        - energyStorage.getEnergyStored())
                        / ENERGY_PER_OPERATION;

        int operations =
                Math.min(
                        availableOperations,
                        Math.min(
                                outputSpace,
                                energySpace
                        )
                );

        boolean running = operations > 0;

        if (running)
        {
            int steamAmount =
                    operations * STEAM_PER_OPERATION;

            steamTank.drain(
                    new FluidStack(
                            ModFluids.STEAM.getForgeFluid(),
                            steamAmount
                    ),
                    true
            );

            condensateTank.fill(
                    new FluidStack(
                            ModFluids.CONDENSATE.getForgeFluid(),
                            operations
                    ),
                    true
            );

            energyStorage.addEnergy(
                    operations * ENERGY_PER_OPERATION
            );
        }

        if (running)
        {
            acceleration += 0.1F;
        }
        else
        {
            acceleration -= 0.1F;
        }

        acceleration =
                MathHelper.clamp(
                        acceleration,
                        0F,
                        (float) ROTOR_MAX_ACCELERATION
                );

        rotor += acceleration;

        while (rotor >= 360F)
        {
            rotor -= 360F;
        }

        if (rotor < 0F)
        {
            rotor = 0F;
        }

        markDirty();

        /*
         * Server -> client rotor sync for the future animated flywheel.
         */
        if ((world.getTotalWorldTime() % 3L) == 0L)
        {
            IBlockState state =
                    world.getBlockState(pos);

            world.notifyBlockUpdate(
                    pos,
                    state,
                    state,
                    3
            );
        }
    }

    /**
     * Keep previously placed 2x2x3 engines working after upgrading the mod.
     * Only replaces known casing blocks; never overwrites arbitrary blocks.
     */
    private void upgradeLegacyPorts()
    {
        EnumFacing front = getFacing();

        net.minecraft.util.math.BlockPos steamPos =
                BlockSteamEngine.getStructurePos(pos, front, 0, 1, 0);
        net.minecraft.util.math.BlockPos condensatePos =
                BlockSteamEngine.getStructurePos(pos, front, 0, 0, 1);
        net.minecraft.util.math.BlockPos powerPos =
                BlockSteamEngine.getStructurePos(pos, front, 1, 1, 0);

        if (world.getBlockState(steamPos).getBlock()
                == com.example.examplemod.init.ModBlocks.STEAM_ENGINE_CASING)
        {
            world.setBlockState(
                    steamPos,
                    com.example.examplemod.init.ModBlocks.STEAM_ENGINE_FLUID_PORT
                            .getDefaultState()
                            .withProperty(
                                    com.example.examplemod.block.BlockSteamEngineFluidPort.FACING,
                                    front)
                            .withProperty(
                                    com.example.examplemod.block.BlockSteamEngineFluidPort.OUTPUT,
                                    false),
                    3);
        }

        if (world.getBlockState(condensatePos).getBlock()
                == com.example.examplemod.init.ModBlocks.STEAM_ENGINE_CASING)
        {
            world.setBlockState(
                    condensatePos,
                    com.example.examplemod.init.ModBlocks.STEAM_ENGINE_FLUID_PORT
                            .getDefaultState()
                            .withProperty(
                                    com.example.examplemod.block.BlockSteamEngineFluidPort.FACING,
                                    front)
                            .withProperty(
                                    com.example.examplemod.block.BlockSteamEngineFluidPort.OUTPUT,
                                    true),
                    3);
        }

        IBlockState powerState = world.getBlockState(powerPos);
        if (powerState.getBlock()
                == com.example.examplemod.init.ModBlocks.STEAM_ENGINE_POWER_PORT
                && powerState.getValue(
                        com.example.examplemod.block.BlockSteamEnginePowerPort.FACING)
                != front)
        {
            world.setBlockState(powerPos,
                    powerState.withProperty(
                            com.example.examplemod.block.BlockSteamEnginePowerPort.FACING,
                            front),
                    3);
        }
    }

    @Override
    public IFluidHandler getFluidReceiver()
    {
        return steamHandler;
    }

    @Override
    public IFluidHandler getFluidProvider()
    {
        return condensateHandler;
    }

    @Override
    public int getFluidPressure()
    {
        /*
         * Both IFluidReceiver and IFluidProvider define this default
         * method, so the machine must resolve the interface conflict
         * explicitly. Pressure 0 is the normal base network channel.
         */
        return 0;
    }

    @Override
    public int getFluidPriority()
    {
        /*
         * Resolve the same Java 8 default-method conflict for priority.
         */
        return 0;
    }

    @Override
    public int getFluidInputRate()
    {
        return 200;
    }

    @Override
    public int getFluidOutputRate()
    {
        return 200;
    }

    public ModFluidTank getSteamTank()
    {
        return steamTank;
    }

    public ModFluidTank getCondensateTank()
    {
        return condensateTank;
    }

    public int getEnergyStored()
    {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored()
    {
        return energyStorage.getMaxEnergyStored();
    }

    public EnumFacing getFacing()
    {
        if (world == null)
        {
            return EnumFacing.NORTH;
        }

        return world
                .getBlockState(pos)
                .getValue(
                        BlockSteamEngine.FACING
                );
    }

    public static boolean isSteamInputSide(
            TileEntitySteamEngine engine,
            EnumFacing side)
    {
        return engine.getFacing() == side;
    }

    public static boolean isCondensateOutputSide(
            TileEntitySteamEngine engine,
            EnumFacing side)
    {
        return engine.getFacing()
                .rotateYCCW()
                == side;
    }

    @Override
    public boolean hasCapability(
            Capability<?> capability,
            EnumFacing facing)
    {
        if (capability ==
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return facing == null || isFluidSide(facing);
        }

        /*
         * The energy capability is intentionally exposed to the
         * dedicated structural power port instead of the core block.
         */
        if (capability == CapabilityEnergy.ENERGY)
        {
            return facing == null;
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
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            if (facing == null)
            {
                return CapabilityFluidHandler
                        .FLUID_HANDLER_CAPABILITY
                        .cast(allFluidHandler);
            }

            if (isSteamInputSide(this, facing))
            {
                return CapabilityFluidHandler
                        .FLUID_HANDLER_CAPABILITY
                        .cast(steamHandler);
            }

            if (isCondensateOutputSide(this, facing))
            {
                return CapabilityFluidHandler
                        .FLUID_HANDLER_CAPABILITY
                        .cast(condensateHandler);
            }
        }

        if (capability == CapabilityEnergy.ENERGY
                && facing == null)
        {
            return CapabilityEnergy.ENERGY.cast(
                    energyStorage
            );
        }

        return super.getCapability(
                capability,
                facing
        );
    }

    private boolean isFluidSide(EnumFacing side)
    {
        return isSteamInputSide(this, side)
                || isCondensateOutputSide(this, side);
    }

    @Override
    public NBTTagCompound writeToNBT(
            NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        // Separate NBT compounds: the old code overwrote steam with condensate.
        compound.setTag("SteamTank",
                steamTank.writeToNBTWithPressure(new NBTTagCompound()));
        compound.setTag("CondensateTank",
                condensateTank.writeToNBTWithPressure(new NBTTagCompound()));
        energyStorage.writeToNBTCustom(compound);

        compound.setFloat(
                "Rotor",
                rotor
        );

        compound.setFloat(
                "Acceleration",
                acceleration
        );

        return compound;
    }

    @Override
    public void readFromNBT(
            NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("SteamTank", 10))
        {
            steamTank.readFromNBTWithPressure(
                    compound.getCompoundTag("SteamTank"));
        }
        if (compound.hasKey("CondensateTank", 10))
        {
            condensateTank.readFromNBTWithPressure(
                    compound.getCompoundTag("CondensateTank"));
        }
        else
        {
            // Legacy saves stored only one tank in root NBT. The last
            // write was condensate; preserve it when possible.
            condensateTank.readFromNBTWithPressure(compound);
        }
        energyStorage.readFromNBTCustom(compound);

        rotor =
                compound.getFloat("Rotor");

        synchronizedRotor =
                rotor;

        acceleration =
                compound.getFloat("Acceleration");
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound tag =
                super.getUpdateTag();

        tag.setFloat(
                "Rotor",
                rotor
        );

        return tag;
    }

    @Override
    public void handleUpdateTag(
            NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);

        synchronizedRotor =
                tag.getFloat("Rotor");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(
                pos,
                0,
                getUpdateTag()
        );
    }

    @Override
    public void onDataPacket(
            NetworkManager net,
            SPacketUpdateTileEntity packet)
    {
        handleUpdateTag(
                packet.getNbtCompound()
        );
    }

    private static final class OutputEnergyStorage
            extends EnergyStorage
    {
        private OutputEnergyStorage(
                int capacity,
                int maxExtract)
        {
            super(
                    capacity,
                    0,
                    maxExtract
            );
        }

        @Override
        public int receiveEnergy(
                int maxReceive,
                boolean simulate)
        {
            return 0;
        }

        @Override
        public boolean canReceive()
        {
            return false;
        }

        private void addEnergy(int amount)
        {
            energy =
                    Math.min(
                            capacity,
                            energy + Math.max(0, amount)
                    );
        }

        private void writeToNBTCustom(
                NBTTagCompound compound)
        {
            compound.setInteger(
                    "Energy",
                    energy
            );
        }

        private void readFromNBTCustom(
                NBTTagCompound compound)
        {
            energy =
                    Math.max(
                            0,
                            Math.min(
                                    capacity,
                                    compound.getInteger("Energy")
                            )
                    );
        }
    }
}
