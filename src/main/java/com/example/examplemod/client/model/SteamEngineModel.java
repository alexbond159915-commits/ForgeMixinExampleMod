package com.example.examplemod.client.model;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * A real animated, textured 3D model, not a spinning 2D item.
 *
 * The fixed 2x2x3 structure still uses normal JSON block models; this
 * model draws raised machine parts over its exterior:
 *
 *   Base         - riveted front structural frame and boiler pipes
 *   Flywheel     - 16-segment ring, eight spokes and heavy hub
 *   Shaft        - rotating gearbox/shaft coupling
 *   Transmission - articulated rod joining the crank to the piston
 *   Piston       - sliding ram moving inside a fixed cylinder
 *
 * Geometry is intentionally original, though its separation and
 * animation approach follow the HBM RenderSteamEngine design.
 *
 * Local coordinates are in Minecraft blocks. The controller occupies
 * [-0.5,+0.5] on X and Z, the second front block extends to -1.5 X,
 * the second story extends to +1.5 Y, the rear extends towards -Z.
 */
@SideOnly(Side.CLIENT)
public final class SteamEngineModel
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "textures/blocks/steam_engine_atlas.png");

    // Slots in the existing 4x4 / 256x256 industrial machine atlas.
    private static final int COPPER = 2;
    private static final int STEEL = 6;
    private static final int BRASS = 9;
    private static final int VENT = 1;

    private static final float FRONT = 0.51F;
    private static final float WHEEL_X = -1.00F;
    private static final float WHEEL_Y = -0.075F;
    private static final float WHEEL_R = 0.395F;
    private static final float CRANK_R = 0.235F;
    private static final int SEGMENTS = 16;

    public void render(TileEntitySpecialRenderer<?> renderer, float angle)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

        renderBase();
        renderFlywheel(angle);
        renderShaft(angle);
        renderTransmission(angle);
        renderPiston(angle);
    }

    private void renderBase()
    {
        // Thick raised front bezel over both front-facing machine blocks.
        box(-1.50F, -0.50F, FRONT,
             0.50F, -0.395F, 0.66F, STEEL);
        box(-1.50F, 1.395F, FRONT,
             0.50F, 1.50F, 0.66F, STEEL);
        box(-1.50F, -0.50F, FRONT,
            -1.395F, 1.50F, 0.66F, STEEL);
        box(0.395F, -0.50F, FRONT,
            0.50F, 1.50F, 0.66F, STEEL);
        box(-0.545F, -0.50F, FRONT,
            -0.455F, 1.50F, 0.615F, STEEL);
        box(-1.50F, 0.465F, FRONT,
             0.50F, 0.54F, 0.605F, STEEL);

        // Upper front copper/steel couplings: the top-left is the steam
        // port, the top-right the electrical port.
        box(-0.10F, 0.79F, 0.64F,
             0.12F, 1.30F, 0.72F, COPPER);
        box(-0.23F, 1.22F, 0.63F,
             0.23F, 1.33F, 0.76F, COPPER);
        box(-1.22F, 1.23F, 0.63F,
            -0.78F, 1.34F, 0.74F, BRASS);

        // Pipework over the roof of the three-block-deep boiler.
        // Above y=1.5 so it remains visible over the casing.
        box(-1.16F, 1.53F, -2.34F,
            -0.99F, 1.70F, 0.36F, COPPER);
        box(-0.09F, 1.53F, -2.34F,
             0.08F, 1.70F, 0.36F, COPPER);

        // Copper clamps and reinforcement on the roof.
        for (int i = 0; i < 3; ++i)
        {
            float z = -1.95F + i * 0.93F;
            box(-1.21F, 1.53F, z,
                -0.94F, 1.73F, z + 0.10F, BRASS);
            box(-0.15F, 1.53F, z,
                 0.14F, 1.73F, z + 0.10F, BRASS);
        }

        // Physical wheel socket and four heavy square mounting plates.
        ring(WHEEL_X, WHEEL_Y, 0.475F, 0.370F,
             0.670F, 0.725F, STEEL);
        for (int dx = -1; dx <= 1; dx += 2)
        {
            for (int dy = -1; dy <= 1; dy += 2)
            {
                float x = WHEEL_X + dx * 0.445F;
                float y = WHEEL_Y + dy * 0.33F;
                box(x - 0.050F, y - 0.050F, 0.605F,
                    x + 0.050F, y + 0.050F, 0.745F, BRASS);
            }
        }

        // Right-hand mechanical piston cylinder, with a dark recessed bay.
        box(-0.435F, -0.32F, 0.58F,
             0.345F, 0.25F, 0.685F, STEEL);
        box(-0.40F, -0.23F, 0.686F,
             0.32F, 0.17F, 0.715F, VENT);
        box(-0.455F, -0.32F, 0.69F,
            -0.365F, 0.27F, 0.775F, BRASS);
        box(0.265F, -0.32F, 0.69F,
            0.355F, 0.27F, 0.775F, BRASS);
    }

    private void renderFlywheel(float angle)
    {
        GlStateManager.pushMatrix();
        try
        {
            GlStateManager.translate(WHEEL_X, WHEEL_Y, 0.79F);
            GlStateManager.rotate(angle, 0.0F, 0.0F, 1.0F);

            // Thick ring: 16 actual polygon segments (front, back,
            // outer and inner walls), rather than a flat sprite.
            ring(0, 0, WHEEL_R, 0.310F,
                 -0.052F, 0.050F, COPPER);

            // Eight dimensional radial spokes.
            for (int i = 0; i < 8; ++i)
            {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(i * 45.0F, 0, 0, 1);
                box(0.075F, -0.039F, -0.058F,
                    0.345F, 0.039F, 0.064F, STEEL);
                GlStateManager.popMatrix();
            }

            // Machined central axle and brass retention bolt.
            ring(0, 0, 0.115F, 0.0F,
                 -0.090F, 0.096F, STEEL);
            ring(0, 0, 0.066F, 0.0F,
                 0.096F, 0.132F, BRASS);

            // Crank pin offset from the central bearing.
            ring(CRANK_R, 0, 0.053F, 0,
                 0.068F, 0.115F, BRASS);
        }
        finally
        {
            GlStateManager.popMatrix();
        }
    }

    private void renderShaft(float angle)
    {
        // A second, compact coupling above the cylinder makes the
        // mechanical gear train visibly rotate at twice flywheel speed.
        GlStateManager.pushMatrix();
        try
        {
            GlStateManager.translate(0.085F, 0.365F, 0.735F);
            GlStateManager.rotate(-angle * 2.0F, 0, 0, 1);
            ring(0, 0, 0.115F, 0.059F,
                 -0.035F, 0.046F, BRASS);
            for (int i = 0; i < 4; ++i)
            {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(i * 90.0F, 0, 0, 1);
                box(0.035F, -0.025F, 0.040F,
                    0.155F, 0.025F, 0.087F, COPPER);
                GlStateManager.popMatrix();
            }
            ring(0, 0, 0.039F, 0.0F, 0.078F, 0.11F, STEEL);
        }
        finally
        {
            GlStateManager.popMatrix();
        }
    }

    private void renderTransmission(float angle)
    {
        double radians = Math.toRadians(angle);
        float crankX = WHEEL_X + (float) Math.cos(radians) * CRANK_R;
        float crankY = WHEEL_Y + (float) Math.sin(radians) * CRANK_R;

        // Slotted slider moves horizontally; crank pin travels in circle.
        float sliderX = 0.075F + (float) Math.cos(radians) * 0.108F;
        float sliderY = -0.045F;
        float dx = sliderX - crankX;
        float dy = sliderY - crankY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float rodAngle = (float) Math.toDegrees(Math.atan2(dy, dx));

        GlStateManager.pushMatrix();
        try
        {
            GlStateManager.translate(crankX, crankY, 0.932F);
            GlStateManager.rotate(rodAngle, 0, 0, 1);
            box(0.0F, -0.042F, -0.032F,
                length, 0.042F, 0.032F, STEEL);
            box(0.07F, -0.024F, 0.032F,
                Math.max(0.08F, length - 0.06F), 0.024F, 0.046F, BRASS);
        }
        finally
        {
            GlStateManager.popMatrix();
        }

        ring(crankX, crankY, 0.073F, 0.0F,
             0.938F, 0.977F, BRASS);
        ring(sliderX, sliderY, 0.065F, 0.0F,
             0.927F, 0.964F, STEEL);
    }

    private void renderPiston(float angle)
    {
        float offset = (float) Math.cos(Math.toRadians(angle)) * 0.108F;
        box(-0.28F + offset, -0.157F, 0.756F,
             0.26F + offset, 0.086F, 0.825F, STEEL);
        box(-0.15F + offset, -0.105F, 0.825F,
             0.18F + offset, 0.038F, 0.851F, COPPER);
        box(0.075F + offset, -0.13F, 0.852F,
             0.19F + offset, 0.059F, 0.919F, BRASS);
    }

    /**
     * Cube prism using vanilla BufferBuilder quads and the stitched
     * 4x4 industrial texture atlas. No OBJLoader or third-party mod
     * is needed for this model.
     */
    private void box(float x0, float y0, float z0,
                     float x1, float y1, float z1, int tile)
    {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder b = tess.getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        quad(b, tile, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1, 0,0,1);
        quad(b, tile, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0, 0,0,-1);
        quad(b, tile, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0, -1,0,0);
        quad(b, tile, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1, 1,0,0);
        quad(b, tile, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, 0,1,0);
        quad(b, tile, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, 0,-1,0);
        tess.draw();
    }

    /**
     * Solid annular cylindrical mesh, oriented along Z. Front, back,
     * inner and outer walls are actual polygons. inner=0 makes a hub.
     */
    private void ring(float cx, float cy, float outer, float inner,
                      float back, float front, int tile)
    {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder b = tess.getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int i = 0; i < SEGMENTS; i++)
        {
            double a0 = Math.PI * 2.0D * i / SEGMENTS;
            double a1 = Math.PI * 2.0D * (i + 1) / SEGMENTS;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float x0=cx+outer*c0, y0=cy+outer*s0;
            float x1=cx+outer*c1, y1=cy+outer*s1;
            float ix0=cx+inner*c0, iy0=cy+inner*s0;
            float ix1=cx+inner*c1, iy1=cy+inner*s1;

            quad(b,tile,ix0,iy0,front,x0,y0,front,x1,y1,front,ix1,iy1,front,0,0,1);
            quad(b,tile,ix1,iy1,back,x1,y1,back,x0,y0,back,ix0,iy0,back,0,0,-1);
            quad(b,tile,x0,y0,back,x1,y1,back,x1,y1,front,x0,y0,front,
                 (c0+c1)*0.5F,(s0+s1)*0.5F,0);
            if (inner > 0.0F)
            {
                quad(b,tile,ix1,iy1,back,ix0,iy0,back,ix0,iy0,front,ix1,iy1,front,
                     -(c0+c1)*0.5F,-(s0+s1)*0.5F,0);
            }
        }
        tess.draw();
    }

    private void quad(BufferBuilder b, int tile,
                      float ax,float ay,float az,
                      float bx,float by,float bz,
                      float cx,float cy,float cz,
                      float dx,float dy,float dz,
                      float nx,float ny,float nz)
    {
        vertex(b,tile,ax,ay,az, 0,0,nx,ny,nz);
        vertex(b,tile,bx,by,bz, 1,0,nx,ny,nz);
        vertex(b,tile,cx,cy,cz, 1,1,nx,ny,nz);
        vertex(b,tile,dx,dy,dz, 0,1,nx,ny,nz);
    }

    private void vertex(BufferBuilder b, int tile, float x,float y,float z,
                        float u,float v,float nx,float ny,float nz)
    {
        float tu = ((tile & 3) + u) * 0.25F;
        float tv = ((tile >> 2) + v) * 0.25F;
        b.pos(x,y,z).tex(tu,tv).normal(nx,ny,nz).endVertex();
    }
}
