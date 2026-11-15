package com.example.examplemod.client;

import com.example.examplemod.client.model.SteamEngineModel;
import com.example.examplemod.tileentity.TileEntitySteamEngine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Steam engine TESR.
 *
 * HBM's RenderSteamEngine renders named moving parts separately:
 * Base, Flywheel, Shaft, Transmission and Piston. We use the same
 * separation and crank/connecting-rod principle, with original
 * geometry compatible with Minecraft 1.12.2 and our 2x2x3 machine.
 */
@SideOnly(Side.CLIENT)
public final class SteamEngineTileRenderer
        extends TileEntitySpecialRenderer<TileEntitySteamEngine>
{
    private final SteamEngineModel model = new SteamEngineModel();

    @Override
    public void render(TileEntitySteamEngine engine,
                       double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha)
    {
        if (engine == null || engine.getWorld() == null)
        {
            return;
        }

        /*
         * Wrap correctly across 359 -> 0 degrees, otherwise the model
         * visibly spins backwards for a single interpolated frame.
         */
        float delta = engine.rotor - engine.lastRotor;
        if (delta > 180.0F)
        {
            delta -= 360.0F;
        }
        else if (delta < -180.0F)
        {
            delta += 360.0F;
        }
        float angle = engine.lastRotor + delta * partialTicks;

        GlStateManager.pushMatrix();
        try
        {
            // Origin = centre of the lower-left FRONT controller block.
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

            // Model is authored facing SOUTH; right-hand blocks extend
            // towards local -X for all four machine orientations.
            switch (engine.getFacing())
            {
                case NORTH:
                    GlStateManager.rotate(180.0F, 0, 1, 0);
                    break;
                case WEST:
                    GlStateManager.rotate(-90.0F, 0, 1, 0);
                    break;
                case EAST:
                    GlStateManager.rotate(90.0F, 0, 1, 0);
                    break;
                case SOUTH:
                default:
                    break;
            }

            GlStateManager.enableRescaleNormal();
            GlStateManager.disableCull();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            model.render(this, angle);
        }
        finally
        {
            GlStateManager.enableCull();
            GlStateManager.disableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
