package com.example.examplemod.network;

import com.example.examplemod.init.ModItems;
import com.example.examplemod.item.ItemFluidIdentifier;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Only the selected fluid id is sent. The server checks the held item
 * and resolves the name against the registered fluids itself.
 */
public class PacketFluidIdentifierSelection implements IMessage
{
    private int handIndex;
    private String fluidName = "";

    public PacketFluidIdentifierSelection()
    {
    }

    public PacketFluidIdentifierSelection(EnumHand hand, String fluidName)
    {
        this.handIndex = hand == EnumHand.OFF_HAND ? 1 : 0;
        this.fluidName = fluidName == null ? "" : fluidName;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        handIndex = buf.readUnsignedByte();
        fluidName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(handIndex);
        ByteBufUtils.writeUTF8String(buf, fluidName);
    }

    public static class Handler implements IMessageHandler<PacketFluidIdentifierSelection, IMessage>
    {
        @Override
        public IMessage onMessage(final PacketFluidIdentifierSelection message, MessageContext context)
        {
            final EntityPlayerMP player = context.getServerHandler().player;

            player.getServerWorld().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    if (message.handIndex != 0 && message.handIndex != 1)
                    {
                        return;
                    }

                    if (message.fluidName == null || message.fluidName.length() > 128)
                    {
                        return;
                    }

                    EnumHand hand = message.handIndex == 1
                            ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                    ItemStack stack = player.getHeldItem(hand);

                    if (stack.isEmpty() || stack.getItem() != ModItems.FLUID_IDENTIFIER)
                    {
                        return;
                    }

                    Fluid fluid = message.fluidName.isEmpty()
                            ? null : FluidRegistry.getFluid(message.fluidName);

                    if (!message.fluidName.isEmpty() && fluid == null)
                    {
                        return;
                    }

                    ItemFluidIdentifier.storeFluid(stack, fluid);
                    player.inventory.markDirty();
                    player.inventoryContainer.detectAndSendChanges();
                }
            });

            return null;
        }
    }
}
