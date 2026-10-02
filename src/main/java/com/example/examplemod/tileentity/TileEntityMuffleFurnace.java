package com.example.examplemod.tileentity;

import com.example.examplemod.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.ForgeEventFactory;

public class TileEntityMuffleFurnace extends TileEntity implements IInventory, ITickable
{
    private final NonNullList<ItemStack> inventory =
            NonNullList.withSize(4, ItemStack.EMPTY);

    private int burnTime = 0;
    private int currentItemBurnTime = 0;
    private int cookTime = 0;
    private final int totalCookTime = 200;

    @Override
    public void update()
    {
        if (world == null || world.isRemote)
        {
            return;
        }

        if (world.getTotalWorldTime() % 20 == 0)
        {
            System.out.println(
                    "[MUFFLE] input=" + inventory.get(0) +
                            " coal=" + inventory.get(1) +
                            " fuel=" + inventory.get(2) +
                            " output=" + inventory.get(3) +
                            " burn=" + burnTime +
                            " cook=" + cookTime
            );
        }

        boolean dirty = false;


        // Если топливо закончилось — пытаемся взять новое
        if (burnTime <= 0 && canSmelt())
        {
            ItemStack fuel = inventory.get(2);

            int fuelTime = getFuelBurnTime(fuel);

            if (fuelTime > 0)
            {
                burnTime = fuelTime;
                currentItemBurnTime = fuelTime;

                fuel.shrink(1);

                if (fuel.isEmpty())
                {
                    inventory.set(2, ItemStack.EMPTY);
                }

                dirty = true;
            }
        }

        // Плавка
        if (burnTime > 0 && canSmelt())
        {

            burnTime--;
            cookTime++;

            dirty = true;

            if (cookTime >= totalCookTime)
            {
                cookTime = 0;
                smeltItem();
                dirty = true;
            }
        }
        else if (burnTime <= 0)
        {
            if (cookTime != 0)
            {
                cookTime = 0;
                dirty = true;
            }
        }

        if (dirty)
        {
            markDirty();
        }
    }
    private int getFuelBurnTime(ItemStack fuel)
    {
        if (fuel.isEmpty())
        {
            return 0;
        }

        if (fuel.getItem() == Items.COAL && fuel.getMetadata() == 0)
        {
            return 1600;
        }

        return ForgeEventFactory.getItemBurnTime(fuel);
    }

    private boolean canSmelt()
    {
        ItemStack input = inventory.get(0);
        ItemStack coal = inventory.get(1);
        ItemStack output = inventory.get(3);

        // Нет сырья
        if (input.isEmpty())
        {
            return false;
        }

        // Нет угля как ингредиента
        if (coal.isEmpty())
        {
            return false;
        }

        // Разрешён только обычный уголь
        if (coal.getItem() != Items.COAL || coal.getMetadata() != 0)
        {
            return false;
        }

        ItemStack result;

        if (input.getItem() == ModItems.RAW_IRON)
        {
            result = new ItemStack(Items.IRON_INGOT);
        }
        else if (input.getItem() == ModItems.RAW_COPPER)
        {
            result = new ItemStack(ModItems.COPPER_INGOT);
        }
        else
        {
            return false;
        }

        // Выход пуст
        if (output.isEmpty())
        {
            return true;
        }

        // В выходе другой предмет
        if (output.getItem() != result.getItem())
        {
            return false;
        }

        // Не помещается новый слиток
        return output.getCount() + result.getCount() <= 64;
    }

    private void smeltItem()
    {
        if (!canSmelt())
        {
            return;
        }

        ItemStack input = inventory.get(0);
        ItemStack coal = inventory.get(1);
        ItemStack output = inventory.get(3);

        ItemStack result;

        if (input.getItem() == ModItems.RAW_IRON)
        {
            result = new ItemStack(Items.IRON_INGOT);
        }
        else
        {
            result = new ItemStack(ModItems.COPPER_INGOT);
        }

        // Забираем сырьё
        input.shrink(1);
        coal.shrink(1);

        // Кладём результат
        if (output.isEmpty())
        {
            inventory.set(3, result);
        }
        else
        {
            output.grow(1);
        }

        markDirty();
    }

    public boolean isBurning()
    {
        return burnTime > 0;
    }

    public int getBurnTime()
    {
        return burnTime;
    }

    public int getCurrentItemBurnTime()
    {
        return currentItemBurnTime;
    }

    public int getCookTime()
    {
        return cookTime;
    }

    public int getTotalCookTime()
    {
        return totalCookTime;
    }

    @Override
    public int getSizeInventory()
    {
        return 4;
    }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack stack : inventory)
        {
            if (!stack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inventory.set(index, stack);

        if (stack.getCount() > 64)
        {
            stack.setCount(64);
        }

        markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if (index == 0)
        {
            return stack.getItem() == ModItems.RAW_IRON
                    || stack.getItem() == ModItems.RAW_COPPER;
        }

        if (index == 1)
        {
            return stack.getItem() == Items.COAL
                    && stack.getMetadata() == 0;
        }

        if (index == 2)
        {
            return getFuelBurnTime(stack) > 0;
        }

        return false;
    }

    @Override
    public String getName()
    {
        return "container.examplemod.muffle_furnace";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation(getName());
    }

    @Override
    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return burnTime;

            case 1:
                return currentItemBurnTime;

            case 2:
                return cookTime;

            case 3:
                return totalCookTime;

            default:
                return 0;
        }
    }

    @Override
    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                burnTime = value;
                break;

            case 1:
                currentItemBurnTime = value;
                break;

            case 2:
                cookTime = value;
                break;

            default:
                break;
        }
    }

    @Override
    public int getFieldCount()
    {
        return 4;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < inventory.size(); i++)
        {
            inventory.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        ItemStackHelper.saveAllItems(compound, inventory);

        compound.setInteger("BurnTime", burnTime);
        compound.setInteger("CurrentItemBurnTime", currentItemBurnTime);
        compound.setInteger("CookTime", cookTime);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        ItemStackHelper.loadAllItems(compound, inventory);

        burnTime = compound.getInteger("BurnTime");
        currentItemBurnTime =
                compound.getInteger("CurrentItemBurnTime");
        cookTime =
                compound.getInteger("CookTime");
    }
}