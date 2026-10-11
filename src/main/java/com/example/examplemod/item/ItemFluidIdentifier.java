package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.fluid.IFluidIdentifierTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Right-click in air (or with no selected fluid on a target): open fluid picker.
 * Right-click a compatible target: assign the selected fluid.
 * Sneak + right-click a target: copy its fluid into the identifier.
 */
public class ItemFluidIdentifier extends ItemBase
{
    private static final String FLUID_ID_TAG = "FluidId";

    public ItemFluidIdentifier()
    {
        super("fluid_identifier", ExampleMod.MACHINES_TAB);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote)
        {
            ExampleMod.PROXY.openFluidIdentifierGui(hand);
        }

        return new ActionResult<ItemStack>(
                EnumActionResult.SUCCESS,
                player.getHeldItem(hand)
        );
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player,
                                            World world,
                                            BlockPos pos,
                                            EnumFacing side,
                                            float hitX, float hitY, float hitZ,
                                            EnumHand hand)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof IFluidIdentifierTarget))
        {
            return EnumActionResult.PASS;
        }

        IFluidIdentifierTarget target = (IFluidIdentifierTarget) tile;
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking())
        {
            if (world.isRemote)
            {
                return EnumActionResult.SUCCESS;
            }

            Fluid fluid = target.getIdentifiedFluid();
            if (fluid == null)
            {
                player.sendMessage(new TextComponentString(
                        TextFormatting.RED + "На объекте жидкость не выбрана."));
                return EnumActionResult.FAIL;
            }

            storeFluid(stack, fluid);
            player.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "Скопирована жидкость: "
                            + TextFormatting.WHITE + fluid.getLocalizedName(new FluidStack(fluid, 1000))));
            return EnumActionResult.SUCCESS;
        }

        Fluid fluid = getStoredFluid(stack);
        if (fluid == null)
        {
            if (world.isRemote)
            {
                ExampleMod.PROXY.openFluidIdentifierGui(hand);
            }
            return EnumActionResult.SUCCESS;
        }

        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        if (!target.setIdentifiedFluid(fluid))
        {
            player.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Невозможно назначить эту жидкость."));
            return EnumActionResult.FAIL;
        }

        player.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "Жидкость установлена: "
                        + TextFormatting.WHITE + fluid.getLocalizedName(new FluidStack(fluid, 1000))));
        return EnumActionResult.SUCCESS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn,
                               List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flagIn)
    {
        Fluid fluid = getStoredFluid(stack);
        if (fluid == null)
        {
            tooltip.add(TextFormatting.GRAY + "Жидкость не выбрана");
        }
        else
        {
            tooltip.add(TextFormatting.GRAY + "Жидкость: " + TextFormatting.WHITE
                    + fluid.getLocalizedName(new FluidStack(fluid, 1000)));
        }

        tooltip.add(TextFormatting.DARK_GRAY + "ПКМ: выбрать жидкость");
        tooltip.add(TextFormatting.DARK_GRAY + "ПКМ по трубе/баку: назначить");
        tooltip.add(TextFormatting.DARK_GRAY + "Shift + ПКМ: скопировать с блока");
    }

    public static Fluid getStoredFluid(ItemStack stack)
    {
        if (stack.isEmpty() || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey(FLUID_ID_TAG))
        {
            return null;
        }

        return FluidRegistry.getFluid(
                stack.getTagCompound().getString(FLUID_ID_TAG));
    }

    public static void storeFluid(ItemStack stack, Fluid fluid)
    {
        if (stack.isEmpty())
        {
            return;
        }

        if (fluid == null)
        {
            if (stack.hasTagCompound())
            {
                stack.getTagCompound().removeTag(FLUID_ID_TAG);
            }
            return;
        }

        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setString(FLUID_ID_TAG, fluid.getName());
    }
}
