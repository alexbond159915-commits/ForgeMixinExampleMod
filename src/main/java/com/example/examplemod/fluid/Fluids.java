package com.example.examplemod.fluid;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Fluids {
    public static final FluidType NONE = new FluidType("none", 0x000000, 300);
    public static final FluidType WATER = new FluidType("water", 0x3F76E4, 300);
    public static final FluidType STEAM = new FluidType("steam", 0xE8E8E8, 373);
    public static final FluidType OIL = new FluidType("oil", 0x17120D, 350);

    private static final Map<String, FluidType> REGISTRY = new LinkedHashMap<String, FluidType>();

    private Fluids() {}

    public static void init() {
        register(NONE);
        register(WATER);
        register(STEAM);
        register(OIL);
    }

    private static void register(FluidType type) {
        REGISTRY.put(type.getName(), type);
    }

    public static FluidType get(String name) {
        FluidType type = REGISTRY.get(name);
        return type == null ? NONE : type;
    }
}
