package com.example.examplemod.fluid.network;

import com.example.examplemod.fluid.FluidType;
import com.example.examplemod.fluid.Fluids;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FluidNetworkManager {
    private static final Map<World, Map<BlockPos, FluidNode>> NODES = new HashMap<World, Map<BlockPos, FluidNode>>();

    private FluidNetworkManager() {}

    private static Map<BlockPos, FluidNode> getNodes(World world) {
        Map<BlockPos, FluidNode> nodes = NODES.get(world);
        if (nodes == null) {
            nodes = new HashMap<BlockPos, FluidNode>();
            NODES.put(world, nodes);
        }
        return nodes;
    }

    public static void addNode(World world, FluidNode node) {
        getNodes(world).put(node.pos, node);
        rebuild(world);
    }

    public static void removeNode(World world, BlockPos pos) {
        Map<BlockPos, FluidNode> nodes = NODES.get(world);
        if (nodes != null) {
            nodes.remove(pos);
            rebuild(world);
        }
    }

    public static void updateNodeFluid(World world, BlockPos pos, FluidType type) {
        FluidNode node = getNode(world, pos);
        if (node != null) {
            node.type = type == null ? Fluids.NONE : type;
            rebuild(world);
        }
    }

    public static FluidNode getNode(World world, BlockPos pos) {
        Map<BlockPos, FluidNode> nodes = NODES.get(world);
        return nodes == null ? null : nodes.get(pos);
    }

    public static boolean canConnect(World world, BlockPos pos, EnumFacing side, FluidType type) {
        FluidNode node = getNode(world, pos);
        if (node == null || (type != Fluids.NONE && node.type != type)) return false;
        FluidNode other = getNode(world, pos.offset(side));
        return other != null && (type == Fluids.NONE || other.type == type);
    }

    public static void rebuild(World world) {
        Map<BlockPos, FluidNode> nodes = getNodes(world);
        for (FluidNode node : nodes.values()) {
            node.network = null;
        }

        for (FluidNode start : new ArrayList<FluidNode>(nodes.values())) {
            if (start.network != null) continue;

            FluidNetwork network = new FluidNetwork();
            List<FluidNode> queue = new ArrayList<FluidNode>();
            queue.add(start);

            for (int i = 0; i < queue.size(); i++) {
                FluidNode current = queue.get(i);
                if (current.network != null) continue;
                current.network = network;
                network.addNode(current);

                for (EnumFacing side : current.connections) {
                    FluidNode next = nodes.get(current.pos.offset(side));
                    if (next == null || next.network != null) continue;
                    if (current.type != Fluids.NONE && next.type != Fluids.NONE && current.type != next.type) continue;
                    if (hasConnection(next, side.getOpposite())) queue.add(next);
                }
            }
        }
    }

    private static boolean hasConnection(FluidNode node, EnumFacing side) {
        for (EnumFacing connection : node.connections) {
            if (connection == side) return true;
        }
        return false;
    }
}
