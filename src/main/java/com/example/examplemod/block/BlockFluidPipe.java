package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.FluidType;
import com.example.examplemod.fluid.Fluids;
import com.example.examplemod.fluid.network.FluidNetworkManager;
import com.example.examplemod.fluid.network.IFluidPipe;
import com.example.examplemod.init.ModBlocks;
import com.example.examplemod.tileentity.TileEntityFluidPipe;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFluidPipe extends Block implements ITileEntityProvider, IFluidPipe {
    public static final PropertyBool POS_X=PropertyBool.create("pos_x"), NEG_X=PropertyBool.create("neg_x");
    public static final PropertyBool POS_Y=PropertyBool.create("pos_y"), NEG_Y=PropertyBool.create("neg_y");
    public static final PropertyBool POS_Z=PropertyBool.create("pos_z"), NEG_Z=PropertyBool.create("neg_z");

    public BlockFluidPipe() {
        super(Material.IRON);
        setRegistryName(ExampleMod.MODID,"fluid_pipe");
        setTranslationKey(ExampleMod.MODID+".fluid_pipe");
        setCreativeTab(ExampleMod.MACHINES_TAB);
        setHardness(2.0F); setResistance(4.0F);
        setDefaultState(blockState.getBaseState().withProperty(POS_X,false).withProperty(NEG_X,false)
                .withProperty(POS_Y,false).withProperty(NEG_Y,false).withProperty(POS_Z,false).withProperty(NEG_Z,false));
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,POS_X,NEG_X,POS_Y,NEG_Y,POS_Z,NEG_Z);
    }
    @Override public int getMetaFromState(IBlockState state){return 0;}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState();}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityFluidPipe();}
    @Override public boolean hasTileEntity(IBlockState state){return true;}

    public IBlockState getConnectionState(World world,BlockPos pos){
        IBlockState state=getDefaultState();
        for(EnumFacing side:EnumFacing.values()){
            TileEntity tile=world.getTileEntity(pos.offset(side));
            if(tile instanceof IFluidPipe && ((IFluidPipe)tile).canConnect(side.getOpposite(),Fluids.NONE))
                state=state.withProperty(propertyFor(side),true);
        }
        return state;
    }
    private PropertyBool propertyFor(EnumFacing side){
        switch(side){
            case EAST:return POS_X; case WEST:return NEG_X; case UP:return POS_Y;
            case DOWN:return NEG_Y; case SOUTH:return POS_Z; case NORTH:return NEG_Z;
            default:return POS_X;
        }
    }
    @Override public FluidType getPipeFluid(){return Fluids.NONE;}
    @Override public boolean canConnect(EnumFacing side,FluidType type){return true;}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){
        FluidNetworkManager.removeNode(world,pos); super.breakBlock(world,pos,state);
    }
}
