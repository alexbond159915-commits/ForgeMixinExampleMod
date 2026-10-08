package com.example.examplemod.fluid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Fluid pipe node.
 *
 * The pipe itself stores no volume. It only stores the fluid type that
 * identifies the network it belongs to, just like HBM's pipe nodes.
 */
public class TileEntityFluidPipe
        extends TileEntity
        implements IFluidIdentifierTarget, ITickable
{
    private Fluid pipeFluid;

    @Override
    public void update()
    {
        // Network membership is resolved centrally by FluidNetworkManager.
    }

    public Fluid getPipeFluid()
    {
        return pipeFluid;
    }

    @Override
    public Fluid getIdentifiedFluid()
    {
        return pipeFluid;
    }

    @Override
    public boolean setIdentifiedFluid(Fluid fluid)
    {
        if (pipeFluid == fluid)
        {
            return true;
        }

        pipeFluid = fluid;
        markDirty();
        syncClient();

        return true;
    }

    private void syncClient()
    {
        if (world == null || world.isRemote)
        {
            return;
        }

        IBlockState state = world.getBlockState(pos);

        world.notifyBlockUpdate(
                pos,
                state,
                state,
                3
        );

        world.markBlockRangeForRenderUpdate(
                pos.add(-1, -1, -1),
                pos.add(1, 1, 1)
        );
    }

    @Override
    public NBTTagCompound writeToNBT(
            NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (pipeFluid != null)
        {
            nbt.setString(
                    "PipeFluid",
                    pipeFluid.getName()
            );
        }
        else
        {
            nbt.removeTag("PipeFluid");
        }

        return nbt;
    }

    @Override
    public void readFromNBT(
            NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        pipeFluid = null;

        if (nbt.hasKey("PipeFluid"))
        {
            pipeFluid =
                    FluidRegistry.getFluid(
                            nbt.getString("PipeFluid")
                    );
        }
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(
                super.getUpdateTag()
        );
    }

    @Override
    public void handleUpdateTag(
            NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);

        if (world != null)
        {
            world.markBlockRangeForRenderUpdate(
                    pos,
                    pos
            );
        }
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
}
