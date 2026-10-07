package com.example.examplemod.fluid;

import net.minecraft.util.ResourceLocation;

public class FluidType {
    private final String name;
    private final int color;
    private final int temperature;
    private ResourceLocation texture;

    public FluidType(String name, int color, int temperature) {
        this.name = name;
        this.color = color;
        this.temperature = temperature;
    }

    public String getName() { return name; }
    public int getColor() { return color; }
    public int getTemperature() { return temperature; }
    public ResourceLocation getTexture() { return texture; }
    public FluidType setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
