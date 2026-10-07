package com.example.examplemod.tileentity;

import com.example.examplemod.fluid.FluidType;
import com.example.examplemod.fluid.Fluids;
import com.example.examplemod.fluid.network.FluidNetworkManager;
import com.example.examplemod.fluid.network.FluidNode;
import com.example.examplemod.fluid.network.IFluidPipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityFluidPipe extends TileEntity implements IFluidPipe {
    private FluidType fluid=Fluids.NONE;
    @Override public void onLoad(){
        super.onLoad();
        if(world!=null&&!world.isRemote) FluidNetworkManager.addNode(world,new FluidNode(pos,fluid,EnumFacing.values()));
    }
    @Override public void invalidate(){
        if(world!=null&&!world.isRemote) FluidNetworkManager.removeNode(world,pos);
        super.invalidate();
    }
    @Override public FluidType getPipeFluid(){return fluid;}
    public void setPipeFluid(FluidType type){
        fluid=type==null?Fluids.NONE:type;
        markDirty();
        if(world!=null&&!world.isRemote) {
            FluidNetworkManager.updateNodeFluid(world,pos,fluid);
            world.notifyBlockUpdate(pos,world.getBlockState(pos),world.getBlockState(pos),3);
        }
    }
    @Override public boolean canConnect(EnumFacing side,FluidType type){return type==Fluids.NONE||fluid==Fluids.NONE||fluid==type;}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag); tag.setString("fluid",fluid.getName()); return tag;
    }
    @Override public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag); fluid=Fluids.get(tag.getString("fluid"));
    }
}
