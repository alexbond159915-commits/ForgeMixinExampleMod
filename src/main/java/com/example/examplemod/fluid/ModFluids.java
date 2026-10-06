package com.example.examplemod.fluid;

import com.example.examplemod.ExampleMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Central registry for the mod's fluids.
 *
 * Add new ModFluidType entries here first, then register the matching Forge
 * Fluid in registerFluids().
 */
public final class ModFluids
{
    public static final ModFluidType PROCESS_WATER =
            new ModFluidType(
                    "examplemod_process_water",
                    0xFF4A90E2,
                    300
            );

    private ModFluids()
    {
    }

    public static final ModFluidType STEAM =
            new ModFluidType(
                    "steam",
                    0xFFFFFFFF,
                    373
            );

    public static final ModFluidType CONDENSATE =
            new ModFluidType(
                    "condensate",
                    0xFF4A90E2,
                    300
            );

    public static void registerFluids()
    {
        registerFluid(PROCESS_WATER, "process_water", 300);
        registerFluid(STEAM, "steam", 373);
        registerFluid(CONDENSATE, "condensate", 300);
    }

    private static void registerFluid(
            ModFluidType type,
            String translationName,
            int temperature)
    {
        if (FluidRegistry.getFluid(type.getName()) != null)
        {
            return;
        }

        Fluid fluid = new Fluid(
                type.getName(),
                new ResourceLocation(
                        "minecraft",
                        "blocks/water_still"
                ),
                new ResourceLocation(
                        "minecraft",
                        "blocks/water_flow"
                )
        )
                .setUnlocalizedName(
                        ExampleMod.MODID + "." + translationName
                )
                .setColor(type.getColor())
                .setTemperature(temperature)
                .setDensity(1000)
                .setViscosity(1000);

        if (FluidRegistry.registerFluid(fluid))
        {
            FluidRegistry.addBucketForFluid(fluid);
        }
    }
}
