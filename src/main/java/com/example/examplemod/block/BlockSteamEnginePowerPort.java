package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.tileentity.TileEntitySteamEnginePowerPort;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSteamEnginePowerPort extends BlockContainerBase
{
    public BlockSteamEnginePowerPort()
    {
        super(
                Material.IRON,
                "steam_engine_power_port",
                ExampleMod.MACHINES_TAB
        );

        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setResistance(6.0F);

        /*
         * This is an internal structural block; the finished machine is
         * the normal player-facing block.
         */
        setCreativeTab(null);
    }

    @Override
    public net.minecraft.tileentity.TileEntity createNewTileEntity(
            World world,
            int meta)
    {
        return new TileEntitySteamEnginePowerPort();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ)
    {
        return true;
    }
}
