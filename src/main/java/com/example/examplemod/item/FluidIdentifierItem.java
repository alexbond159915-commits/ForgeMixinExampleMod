package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.FluidType;
import com.example.examplemod.fluid.Fluids;
import com.example.examplemod.tileentity.TileEntityFluidPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import java.util.ArrayList;

public class FluidIdentifierItem extends ItemBase {
    private static final String NBT_FLUID = "Fluid";

    public FluidIdentifierItem() {
        super("fluid_identifier", ExampleMod.MACHINES_TAB);
        setMaxStackSize(1);
    }

    public static FluidType getFluid(ItemStack stack) {
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_FLUID))
            return Fluids.get(stack.getTagCompound().getString(NBT_FLUID));
        return Fluids.NONE;
    }

    public static void setFluid(ItemStack stack, FluidType fluid) {
        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(NBT_FLUID, fluid == null ? Fluids.NONE.getName() : fluid.getName());
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add("Fluid: " + getFluid(stack).getName());
        tooltip.add("Sneak + right click: change fluid");
        tooltip.add("Right click pipe: set fluid");
        tooltip.add("Sneak + right click pipe: copy fluid");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if(player.isSneaking()) {
            FluidType next = nextFluid(getFluid(stack));
            setFluid(stack, next);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(world.isRemote)
            return EnumActionResult.SUCCESS;

        if(world.getTileEntity(pos) instanceof TileEntityFluidPipe) {
            TileEntityFluidPipe pipe = (TileEntityFluidPipe) world.getTileEntity(pos);

            if(player.isSneaking()) {
                setFluid(stack, pipe.getPipeFluid());
                return EnumActionResult.SUCCESS;
            }

            FluidType selected = getFluid(stack);
            if(selected == Fluids.NONE)
                return EnumActionResult.FAIL;

            pipe.setPipeFluid(selected);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    private static FluidType nextFluid(FluidType current) {
        List<FluidType> fluids = new ArrayList<FluidType>();
        fluids.add(Fluids.WATER);
        fluids.add(Fluids.STEAM);
        fluids.add(Fluids.OIL);

        int index = fluids.indexOf(current);
        return fluids.get((index + 1 + fluids.size()) % fluids.size());
    }
}
