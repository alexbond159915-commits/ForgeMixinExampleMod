package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.IFluidIdentifierTarget;
import com.example.examplemod.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.List;

public class ItemFluidIdentifier extends ItemBase
{
    private static final String FLUID_ID_TAG = "FluidId";

    public ItemFluidIdentifier()
    {
        super("fluid_identifier", ExampleMod.MACHINES_TAB);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUseFirst(
            EntityPlayer player,
            World world,
            BlockPos pos,
            EnumFacing side,
            float hitX,
            float hitY,
            float hitZ,
            EnumHand hand)
    {
        TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof IFluidIdentifierTarget))
        {
            return EnumActionResult.PASS;
        }

        if (!player.isSneaking())
        {
            return EnumActionResult.PASS;
        }

        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        ItemStack stack = player.getHeldItem(hand);
        IFluidIdentifierTarget target =
                (IFluidIdentifierTarget) tile;

        Fluid selected = getStoredFluid(stack);

        if (selected == null)
        {
            Fluid targetFluid = target.getIdentifiedFluid();

            if (targetFluid == null)
            {
                return EnumActionResult.PASS;
            }

            storeFluid(stack, targetFluid);

            player.addChatMessage(
                    new TextComponentString(
                            TextFormatting.GRAY
                                    + "Жидкость скопирована: "
                                    + TextFormatting.WHITE
                                    + targetFluid.getName()
                    )
            );

            return EnumActionResult.SUCCESS;
        }

        if (!target.setIdentifiedFluid(selected))
        {
            player.addChatMessage(
                    new TextComponentString(
                            TextFormatting.RED
                                    + "Нельзя идентифицировать бак: "
                                    + "внутри находится другая жидкость."
                    )
            );

            return EnumActionResult.FAIL;
        }

        player.addChatMessage(
                new TextComponentString(
                        TextFormatting.GRAY
                                + "Бак идентифицирован под жидкость: "
                                + TextFormatting.WHITE
                                + selected.getName()
                )
        );

        return EnumActionResult.SUCCESS;
    }

    @Override
    public void addInformation(
            ItemStack stack,
            World worldIn,
            List<String> tooltip,
            net.minecraft.client.util.ITooltipFlag flagIn)
    {
        Fluid fluid = getStoredFluid(stack);

        if (fluid == null)
        {
            tooltip.add(
                    TextFormatting.GRAY
                            + "Жидкость не выбрана"
            );
        }
        else
        {
            tooltip.add(
                    TextFormatting.GRAY
                            + "Жидкость: "
                            + TextFormatting.WHITE
                            + fluid.getName()
            );
        }

        tooltip.add(
                TextFormatting.DARK_GRAY
                        + "Shift + ПКМ по жидкостному баку"
        );
    }

    private static Fluid getStoredFluid(ItemStack stack)
    {
        if (!stack.hasTagCompound()
                || !stack.getTagCompound().hasKey(FLUID_ID_TAG))
        {
            return null;
        }

        String fluidName =
                stack.getTagCompound().getString(FLUID_ID_TAG);

        if (fluidName == null || fluidName.isEmpty())
        {
            return null;
        }

        return FluidRegistry.getFluid(fluidName);
    }

    private static void storeFluid(
            ItemStack stack,
            Fluid fluid)
    {
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(
                    new net.minecraft.nbt.NBTTagCompound()
            );
        }

        stack.getTagCompound().setString(
                FLUID_ID_TAG,
                fluid.getName()
        );
    }
}
