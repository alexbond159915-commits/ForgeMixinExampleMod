package com.example.examplemod.block;

import com.example.examplemod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/** Server-side disassembly and ownership checks for the 2x2x3 engine. */
public final class SteamEngineStructure
{
    private static final Set<String> DISMANTLING = new HashSet<String>();

    private SteamEngineStructure() {}

    private static String key(World world, BlockPos pos)
    {
        return world.provider.getDimension() + ":" + pos.toLong();
    }

    @Nullable
    public static BlockPos findController(World world, BlockPos part)
    {
        if (world == null || !world.isBlockLoaded(part)) return null;
        for (EnumFacing facing : EnumFacing.HORIZONTALS)
        {
            EnumFacing right = facing.rotateY();
            for (int d = 0; d < 3; d++)
            for (int h = 0; h < 2; h++)
            for (int w = 0; w < 2; w++)
            {
                if (d == 0 && h == 0 && w == 0) continue;
                BlockPos core = part.offset(right.getOpposite(), w)
                        .down(h).offset(facing, d);
                if (!world.isBlockLoaded(core)) continue;
                IBlockState state = world.getBlockState(core);
                if (state.getBlock() == ModBlocks.STEAM_ENGINE
                        && state.getValue(BlockSteamEngine.FACING) == facing
                        && BlockSteamEngine.getStructurePos(
                                core, facing, w, h, d).equals(part))
                {
                    return core;
                }
            }
        }
        return null;
    }

    public static boolean removePart(World world, BlockPos part,
                                     boolean dropEngine)
    {
        if (world == null || world.isRemote) return false;
        BlockPos core = findController(world, part);
        if (core == null) return false;
        String id = key(world, core);
        if (DISMANTLING.contains(id)) return true;
        DISMANTLING.add(id);
        try
        {
            IBlockState state = world.getBlockState(core);
            if (state.getBlock() != ModBlocks.STEAM_ENGINE) return false;
            EnumFacing facing = state.getValue(BlockSteamEngine.FACING);
            world.setBlockToAir(core); // No vanilla drops.
            clearParts(world, core, facing);
            if (dropEngine)
            {
                Block.spawnAsEntity(world, core,
                        new ItemStack(ModBlocks.STEAM_ENGINE));
            }
            return true;
        }
        finally
        {
            DISMANTLING.remove(id);
        }
    }

    public static boolean removePartByPlayer(
            World world, BlockPos part, EntityPlayer player)
    {
        return removePart(world, part,
                player == null || !player.capabilities.isCreativeMode);
    }

    public static void removeController(
            World world, BlockPos core, EnumFacing facing)
    {
        if (world == null || world.isRemote) return;
        String id = key(world, core);
        if (!DISMANTLING.add(id)) return;
        try
        {
            clearParts(world, core, facing);
        }
        finally
        {
            DISMANTLING.remove(id);
        }
    }

    private static void clearParts(World world, BlockPos core,
                                   EnumFacing facing)
    {
        for (int d = 0; d < 3; d++)
        for (int h = 0; h < 2; h++)
        for (int w = 0; w < 2; w++)
        {
            if (d == 0 && h == 0 && w == 0) continue;
            BlockPos part = BlockSteamEngine.getStructurePos(
                    core, facing, w, h, d);
            if (!world.isBlockLoaded(part)) continue;
            Block block = world.getBlockState(part).getBlock();
            if (block == ModBlocks.STEAM_ENGINE_CASING
                    || block == ModBlocks.STEAM_ENGINE_POWER_PORT
                    || block == ModBlocks.STEAM_ENGINE_FLUID_PORT)
            {
                world.setBlockToAir(part);
            }
        }
    }
}
