package com.example.examplemod;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.util.EnumHand;

public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
    }

    public void openFluidIdentifierGui(EnumHand hand)
    {
        // Client-side only. A dedicated server never creates GUI screens.
    }
}
