package com.example.examplemod.fluid.network;

import com.example.examplemod.fluid.FluidStack;
import com.example.examplemod.fluid.FluidType;
import com.example.examplemod.fluid.Fluids;
import java.util.ArrayList;
import java.util.List;

public class FluidNetwork {
    private final List<FluidNode> nodes = new ArrayList<FluidNode>();
    private final List<IFluidProvider> providers = new ArrayList<IFluidProvider>();
    private final List<IFluidReceiver> receivers = new ArrayList<IFluidReceiver>();
    private FluidType type = Fluids.NONE;

    public void addNode(FluidNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            node.network = this;
            if (type == Fluids.NONE) type = node.type;
        }
    }

    public void addProvider(IFluidProvider provider) {
        if (!providers.contains(provider)) providers.add(provider);
    }

    public void addReceiver(IFluidReceiver receiver) {
        if (!receivers.contains(receiver)) receivers.add(receiver);
    }

    public List<FluidNode> getNodes() { return nodes; }

    public void update() {
        if (type == Fluids.NONE) return;

        for (IFluidReceiver receiver : receivers) {
            int demand = receiver.getFluidDemand();
            if (demand <= 0) continue;

            for (IFluidProvider provider : providers) {
                if (demand <= 0 || provider.getFluidAmount() <= 0) break;
                FluidStack preview = provider.drainFluid(Math.min(demand, provider.getFluidAmount()), false);
                if (preview == null || preview.type != type) continue;

                int accepted = receiver.fillFluid(preview, true);
                if (accepted > 0) {
                    provider.drainFluid(accepted, true);
                    demand -= accepted;
                }
            }
        }
    }
}
