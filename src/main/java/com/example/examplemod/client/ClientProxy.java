package com.example.examplemod.client;

import com.example.examplemod.CommonProxy;
import com.example.examplemod.ExampleMod;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        OBJLoader.INSTANCE.addDomain(ExampleMod.MODID);
    }
}
