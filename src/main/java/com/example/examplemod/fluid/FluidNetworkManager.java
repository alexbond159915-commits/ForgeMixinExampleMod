package com.example.examplemod.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Typed fluid network inspired by HBM's FluidNetMK2.
 *
 * Every connected component belongs to exactly one Forge Fluid. Pipes do not
 * contain volume themselves; providers and receivers exchange fluid through
 * the network.
 */
public final class FluidNetworkManager
{
    private static final int DEFAULT_TRANSFER_RATE = 200;
    private static final int PROBE_AMOUNT = 1000;

    private FluidNetworkManager()
    {
    }

    public static void tickWorld(World world)
    {
        if (world == null || world.isRemote)
        {
            return;
        }

        Set<BlockPos> visitedPipes =
                new HashSet<BlockPos>();

        for (TileEntity tile : world.loadedTileEntityList)
        {
            if (!(tile instanceof TileEntityFluidPipe))
            {
                continue;
            }

            TileEntityFluidPipe pipe =
                    (TileEntityFluidPipe) tile;

            Fluid fluid =
                    pipe.getPipeFluid();

            if (fluid == null)
            {
                continue;
            }

            if (!visitedPipes.add(tile.getPos()))
            {
                continue;
            }

            NetworkComponent component =
                    collectComponent(
                            world,
                            tile.getPos(),
                            fluid,
                            visitedPipes
                    );

            transfer(
                    component,
                    fluid
            );
        }
    }

    private static NetworkComponent collectComponent(
            World world,
            BlockPos start,
            Fluid fluid,
            Set<BlockPos> visitedPipes)
    {
        NetworkComponent component =
                new NetworkComponent();

        ArrayDeque<BlockPos> queue =
                new ArrayDeque<BlockPos>();

        Set<HandlerKey> seenHandlers =
                new HashSet<HandlerKey>();

        queue.add(start);

        while (!queue.isEmpty())
        {
            BlockPos current =
                    queue.removeFirst();

            for (EnumFacing facing :
                    EnumFacing.VALUES)
            {
                BlockPos next =
                        current.offset(facing);

                TileEntity adjacent =
                        world.getTileEntity(next);

                if (adjacent instanceof TileEntityFluidPipe)
                {
                    Fluid adjacentFluid =
                            ((TileEntityFluidPipe) adjacent)
                                    .getPipeFluid();

                    if (adjacentFluid == fluid
                            && visitedPipes.add(next))
                    {
                        queue.addLast(next);
                    }

                    continue;
                }

                IFluidHandler handler =
                        getFluidHandler(
                                adjacent,
                                facing.getOpposite()
                        );

                if (handler == null)
                {
                    continue;
                }

                HandlerKey key =
                        new HandlerKey(
                                adjacent,
                                facing.getOpposite()
                        );

                if (seenHandlers.add(key))
                {
                    component.handlers.add(
                            new HandlerEndpoint(
                                    adjacent,
                                    facing.getOpposite(),
                                    handler
                            )
                    );
                }
            }
        }

        return component;
    }

    private static IFluidHandler getFluidHandler(
            TileEntity tile,
            EnumFacing side)
    {
        if (tile == null)
        {
            return null;
        }

        Capability<IFluidHandler> capability =
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

        if (!tile.hasCapability(
                capability,
                side
        ))
        {
            return null;
        }

        return tile.getCapability(
                capability,
                side
        );
    }

    private static void transfer(
            NetworkComponent component,
            Fluid fluid)
    {
        List<ProviderState> providers =
                new ArrayList<ProviderState>();

        List<ReceiverState> receivers =
                new ArrayList<ReceiverState>();

        long totalAvailable = 0L;
        long totalDemand = 0L;

        for (HandlerEndpoint endpoint :
                component.handlers)
        {
            int available =
                    getAvailable(
                            endpoint.handler,
                            fluid
                    );

            if (available > 0)
            {
                int limited =
                        Math.min(
                                available,
                                getOutputRate(endpoint.tile)
                        );

                if (limited > 0)
                {
                    providers.add(
                            new ProviderState(
                                    endpoint,
                                    limited
                            )
                    );

                    totalAvailable += limited;
                }
            }

            int demand =
                    getDemand(
                            endpoint.handler,
                            fluid
                    );

            if (demand > 0)
            {
                int limited =
                        Math.min(
                                demand,
                                getInputRate(endpoint.tile)
                        );

                if (limited > 0)
                {
                    receivers.add(
                            new ReceiverState(
                                    endpoint,
                                    limited
                            )
                    );

                    totalDemand += limited;
                }
            }
        }

        if (providers.isEmpty()
                || receivers.isEmpty()
                || totalAvailable <= 0
                || totalDemand <= 0)
        {
            return;
        }

        Collections.sort(
                receivers,
                new Comparator<ReceiverState>()
                {
                    @Override
                    public int compare(
                            ReceiverState a,
                            ReceiverState b)
                    {
                        return Integer.compare(
                                getPriority(b.endpoint.tile),
                                getPriority(a.endpoint.tile)
                        );
                    }
                }
        );

        long totalTransfer =
                Math.min(
                        totalAvailable,
                        totalDemand
                );

        long remainingAllocation =
                totalTransfer;

        long accumulated =
                0L;

        for (int i = 0;
             i < receivers.size();
             i++)
        {
            ReceiverState receiver =
                    receivers.get(i);

            long allocation;

            if (i == receivers.size() - 1)
            {
                allocation =
                        remainingAllocation;
            }
            else
            {
                allocation =
                        totalTransfer
                                * receiver.demand
                                / totalDemand;
            }

            allocation =
                    Math.min(
                            allocation,
                            receiver.demand
                    );

            receiver.remaining =
                    allocation;

            remainingAllocation -=
                    allocation;

            accumulated +=
                    allocation;
        }

        long leftovers =
                totalTransfer - accumulated;

        for (int i = 0;
             leftovers > 0
                     && !receivers.isEmpty();
             i = (i + 1) % receivers.size())
        {
            ReceiverState receiver =
                    receivers.get(i);

            if (receiver.remaining
                    < receiver.demand)
            {
                receiver.remaining++;
                leftovers--;
            }
        }

        int providerCursor = 0;

        for (ReceiverState receiver :
                receivers)
        {
            long needed =
                    receiver.remaining;

            if (needed <= 0)
            {
                continue;
            }

            int attempts =
                    Math.max(
                            providers.size() * 2,
                            1
                    );

            while (needed > 0
                    && attempts-- > 0
                    && !providers.isEmpty())
            {
                ProviderState provider =
                        providers.get(
                                providerCursor
                                        % providers.size()
                        );

                providerCursor =
                        (providerCursor + 1)
                                % providers.size();

                if (provider.remaining <= 0)
                {
                    continue;
                }

                if (provider.endpoint.tile ==
                        receiver.endpoint.tile)
                {
                    continue;
                }

                int offer =
                        (int) Math.min(
                                needed,
                                provider.remaining
                        );

                FluidStack simulatedDrain =
                        provider.endpoint.handler.drain(
                                new FluidStack(
                                        fluid,
                                        offer
                                ),
                                false
                        );

                if (simulatedDrain == null
                        || simulatedDrain.amount <= 0)
                {
                    provider.remaining = 0;
                    continue;
                }

                int simulatedAccepted =
                        receiver.endpoint.handler.fill(
                                new FluidStack(
                                        fluid,
                                        simulatedDrain.amount
                                ),
                                false
                        );

                int amount =
                        Math.min(
                                simulatedDrain.amount,
                                Math.max(
                                        0,
                                        simulatedAccepted
                                )
                        );

                if (amount <= 0)
                {
                    continue;
                }

                FluidStack drained =
                        provider.endpoint.handler.drain(
                                new FluidStack(
                                        fluid,
                                        amount
                                ),
                                true
                        );

                if (drained == null
                        || drained.amount <= 0)
                {
                    continue;
                }

                int accepted =
                        receiver.endpoint.handler.fill(
                                new FluidStack(
                                        fluid,
                                        drained.amount
                                ),
                                true
                        );

                if (accepted < drained.amount)
                {
                    int refund =
                            drained.amount
                                    - Math.max(
                                            0,
                                            accepted
                                    );

                    if (refund > 0)
                    {
                        provider.endpoint.handler.fill(
                                new FluidStack(
                                        fluid,
                                        refund
                                ),
                                true
                        );
                    }
                }

                int actual =
                        Math.min(
                                drained.amount,
                                Math.max(
                                        0,
                                        accepted
                                )
                        );

                provider.remaining -=
                        Math.min(
                                provider.remaining,
                                drained.amount
                        );

                needed -= actual;
            }
        }
    }

    private static int getAvailable(
            IFluidHandler handler,
            Fluid fluid)
    {
        IFluidTankProperties[] properties =
                handler.getTankProperties();

        if (properties == null)
        {
            return 0;
        }

        int total = 0;

        for (IFluidTankProperties property :
                properties)
        {
            if (property == null)
            {
                continue;
            }

            FluidStack contents =
                    property.getContents();

            if (contents != null
                    && contents.amount > 0
                    && contents.getFluid() == fluid)
            {
                total =
                        Math.min(
                                PROBE_AMOUNT,
                                total + contents.amount
                        );
            }
        }

        return total;
    }

    private static int getDemand(
            IFluidHandler handler,
            Fluid fluid)
    {
        return Math.max(
                0,
                handler.fill(
                        new FluidStack(
                                fluid,
                                PROBE_AMOUNT
                        ),
                        false
                )
        );
    }

    private static int getOutputRate(
            TileEntity tile)
    {
        if (tile instanceof IFluidProvider)
        {
            return Math.max(
                    0,
                    ((IFluidProvider) tile)
                            .getFluidOutputRate()
            );
        }

        return DEFAULT_TRANSFER_RATE;
    }

    private static int getInputRate(
            TileEntity tile)
    {
        if (tile instanceof IFluidReceiver)
        {
            return Math.max(
                    0,
                    ((IFluidReceiver) tile)
                            .getFluidInputRate()
            );
        }

        return DEFAULT_TRANSFER_RATE;
    }

    private static int getPriority(
            TileEntity tile)
    {
        if (tile instanceof IFluidReceiver)
        {
            return ((IFluidReceiver) tile)
                    .getFluidPriority();
        }

        return 0;
    }

    private static final class NetworkComponent
    {
        private final List<HandlerEndpoint> handlers =
                new ArrayList<HandlerEndpoint>();
    }

    private static final class HandlerEndpoint
    {
        private final TileEntity tile;
        private final EnumFacing side;
        private final IFluidHandler handler;

        private HandlerEndpoint(
                TileEntity tile,
                EnumFacing side,
                IFluidHandler handler)
        {
            this.tile = tile;
            this.side = side;
            this.handler = handler;
        }
    }

    private static final class ProviderState
    {
        private final HandlerEndpoint endpoint;
        private int remaining;

        private ProviderState(
                HandlerEndpoint endpoint,
                int remaining)
        {
            this.endpoint = endpoint;
            this.remaining = remaining;
        }
    }

    private static final class ReceiverState
    {
        private final HandlerEndpoint endpoint;
        private final int demand;
        private long remaining;

        private ReceiverState(
                HandlerEndpoint endpoint,
                int demand)
        {
            this.endpoint = endpoint;
            this.demand = demand;
        }
    }

    private static final class HandlerKey
    {
        private final TileEntity tile;
        private final EnumFacing side;

        private HandlerKey(
                TileEntity tile,
                EnumFacing side)
        {
            this.tile = tile;
            this.side = side;
        }

        @Override
        public boolean equals(
                Object object)
        {
            if (this == object)
            {
                return true;
            }

            if (!(object instanceof HandlerKey))
            {
                return false;
            }

            HandlerKey other =
                    (HandlerKey) object;

            return tile == other.tile
                    && side == other.side;
        }

        @Override
        public int hashCode()
        {
            int result =
                    System.identityHashCode(tile);

            result =
                    31 * result
                            + side.hashCode();

            return result;
        }
    }
}
