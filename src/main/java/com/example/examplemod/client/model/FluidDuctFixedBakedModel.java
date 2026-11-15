package com.example.examplemod.client.model;

import com.example.examplemod.block.BlockFluidDuct;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reliable eight-sided industrial fluid duct for Forge 1.12.2.
 *
 * The previous HBM OBJ arrangement chose six disconnected half-pipes for an
 * isolated duct and omitted the central junction. Its corner group names and
 * +/-Z orientation also disagreed with the actual geometry in the OBJ file.
 *
 * Here every connection mask builds one centered metal junction and the
 * correct arms. Geometry stays in [0,1] and all fluid overlay surfaces sit
 * just outside the base surface, never on a coplanar face.
 *
 * This model is used ONLY for BlockFluidDuct. The small FluidPipe models are
 * deliberately unchanged.
 */
public final class FluidDuctFixedBakedModel implements IBakedModel
{
    private static final int SIDES = 8;
    private static final float CORE_MIN = 5F / 16F;
    private static final float CORE_MAX = 11F / 16F;
    private static final float RADIUS = 3F / 16F;
    private static final float OVERLAY_R = RADIUS + 0.004F;
    private static final float FLANGE_R = 3.5F / 16F;

    private final TextureAtlasSprite metal;
    private final TextureAtlasSprite overlay;
    private final boolean inventory;

    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] worldQuads = new List[64];
    private List<BakedQuad> inventoryQuads;

    public FluidDuctFixedBakedModel(TextureAtlasSprite metal,
                                   TextureAtlasSprite overlay,
                                   boolean inventory)
    {
        this.metal = metal;
        this.overlay = overlay;
        this.inventory = inventory;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
    {
        if (side != null)
        {
            return Collections.emptyList();
        }

        if (inventory)
        {
            if (inventoryQuads == null)
            {
                // A horizontal two-ended duct makes a useful inventory icon.
                inventoryQuads = makeQuads(32 | 16, DefaultVertexFormats.ITEM);
            }
            return inventoryQuads;
        }

        int mask = 0;
        if (state != null)
        {
            if (state.getValue(BlockFluidDuct.POS_X)) mask |= 32;
            if (state.getValue(BlockFluidDuct.NEG_X)) mask |= 16;
            if (state.getValue(BlockFluidDuct.POS_Y)) mask |= 8;
            if (state.getValue(BlockFluidDuct.NEG_Y)) mask |= 4;
            if (state.getValue(BlockFluidDuct.POS_Z)) mask |= 2;
            if (state.getValue(BlockFluidDuct.NEG_Z)) mask |= 1;
        }

        if (worldQuads[mask] == null)
        {
            worldQuads[mask] = makeQuads(mask, DefaultVertexFormats.BLOCK);
        }
        return worldQuads[mask];
    }

    private List<BakedQuad> makeQuads(int mask, VertexFormat format)
    {
        List<BakedQuad> out = new ArrayList<BakedQuad>();

        // The center is ALWAYS drawn. It closes every join, including cases
        // with a single arm and an unconnected pipe.
        addBox(out, format,
                CORE_MIN, CORE_MIN, CORE_MIN,
                CORE_MAX, CORE_MAX, CORE_MAX);

        if (mask == 0)
        {
            // A disconnected pipe is one complete capped vertical section.
            // Do not render six independent loose arms as the old model did.
            addTube(out, format, 1, 0.16F, 0.84F, RADIUS, metal, -1);
            addTube(out, format, 1, 0.16F, 0.84F, OVERLAY_R, overlay, 1);
            addTube(out, format, 1, 0.16F, 0.23F, FLANGE_R, metal, -1);
            addTube(out, format, 1, 0.77F, 0.84F, FLANGE_R, metal, -1);
            addEnd(out, format, 1, 0.16F, false, metal);
            addEnd(out, format, 1, 0.84F, true, metal);
            return Collections.unmodifiableList(out);
        }

        if ((mask & 32) != 0) addArm(out, format, 0, true);
        if ((mask & 16) != 0) addArm(out, format, 0, false);
        if ((mask & 8) != 0) addArm(out, format, 1, true);
        if ((mask & 4) != 0) addArm(out, format, 1, false);
        if ((mask & 2) != 0) addArm(out, format, 2, true);
        if ((mask & 1) != 0) addArm(out, format, 2, false);
        return Collections.unmodifiableList(out);
    }

    private void addArm(List<BakedQuad> out, VertexFormat format,
                        int axis, boolean positive)
    {
        float end = positive ? 1F : 0F;
        float collarStart = positive ? 0.87F : 0.06F;
        float collarEnd = positive ? 0.94F : 0.13F;
        addTube(out, format, axis, 0.5F, end, RADIUS, metal, -1);
        addTube(out, format, axis, 0.5F, end, OVERLAY_R, overlay, 1);
        addTube(out, format, axis, collarStart, collarEnd,
                FLANGE_R, metal, -1);
        addFlangeFace(out, format, axis, collarStart,
                RADIUS, FLANGE_R, metal);
        addFlangeFace(out, format, axis, collarEnd,
                RADIUS, FLANGE_R, metal);
    }

    // Coordinates on the cross-section. Basis u x v = pipe axis.
    private static float[] point(int axis, float distance,
                                 float r, double angle)
    {
        float c = (float) Math.cos(angle) * r;
        float s = (float) Math.sin(angle) * r;
        if (axis == 0) return new float[]{distance, 0.5F + c, 0.5F + s};
        if (axis == 1) return new float[]{0.5F + s, distance, 0.5F + c};
        return new float[]{0.5F + c, 0.5F + s, distance};
    }

    private void addTube(List<BakedQuad> out, VertexFormat format, int axis,
                         float a, float b, float radius,
                         TextureAtlasSprite sprite, int tint)
    {
        for (int i = 0; i < SIDES; i++)
        {
            double angle0 = i * Math.PI * 2 / SIDES;
            double angle1 = (i + 1) * Math.PI * 2 / SIDES;
            float[] s0 = point(axis, a, radius, angle0);
            float[] s1 = point(axis, a, radius, angle1);
            float[] e0 = point(axis, b, radius, angle0);
            float[] e1 = point(axis, b, radius, angle1);
            // Reverse when traveling along a negative axis. Otherwise all
            // sides face inward, causing missing triangles with culling.
            if (b >= a)
            {
                quad(out, format, sprite, tint, s0, s1, e1, e0,
                        i * 16F / SIDES, 0F,
                        (i + 1) * 16F / SIDES, 16F);
            }
            else
            {
                quad(out, format, sprite, tint, s0, e0, e1, s1,
                        i * 16F / SIDES, 0F,
                        (i + 1) * 16F / SIDES, 16F);
            }
        }
    }

    private void addFlangeFace(List<BakedQuad> out, VertexFormat format,
                               int axis, float distance, float inner,
                               float outer, TextureAtlasSprite sprite)
    {
        for (int i = 0; i < SIDES; i++)
        {
            double a0 = i * Math.PI * 2 / SIDES;
            double a1 = (i + 1) * Math.PI * 2 / SIDES;
            quad(out, format, sprite, -1,
                    point(axis, distance, inner, a0),
                    point(axis, distance, outer, a0),
                    point(axis, distance, outer, a1),
                    point(axis, distance, inner, a1),
                    4F, 4F, 12F, 12F);
        }
    }

    private void addEnd(List<BakedQuad> out, VertexFormat format,
                        int axis, float distance, boolean positive,
                        TextureAtlasSprite sprite)
    {
        float[] center = point(axis, distance, 0, 0);
        for (int i = 0; i < SIDES; i++)
        {
            double a0 = i * Math.PI * 2 / SIDES;
            double a1 = (i + 1) * Math.PI * 2 / SIDES;
            float[] p0 = point(axis, distance, FLANGE_R, a0);
            float[] p1 = point(axis, distance, FLANGE_R, a1);
            if (positive)
            {
                quad(out, format, sprite, -1,
                        center, p0, p1, center, 4F, 4F, 12F, 12F);
            }
            else
            {
                quad(out, format, sprite, -1,
                        center, p1, p0, center, 4F, 4F, 12F, 12F);
            }
        }
    }

    private void addBox(List<BakedQuad> out, VertexFormat fmt,
                        float x, float y, float z,
                        float X, float Y, float Z)
    {
        quad(out, fmt, metal, -1,
                v(x,y,Z),v(X,y,Z),v(X,Y,Z),v(x,Y,Z),2,2,14,14);
        quad(out, fmt, metal, -1,
                v(X,y,z),v(x,y,z),v(x,Y,z),v(X,Y,z),2,2,14,14);
        quad(out, fmt, metal, -1,
                v(x,y,z),v(x,y,Z),v(x,Y,Z),v(x,Y,z),2,2,14,14);
        quad(out, fmt, metal, -1,
                v(X,y,Z),v(X,y,z),v(X,Y,z),v(X,Y,Z),2,2,14,14);
        quad(out, fmt, metal, -1,
                v(x,Y,Z),v(X,Y,Z),v(X,Y,z),v(x,Y,z),2,2,14,14);
        quad(out, fmt, metal, -1,
                v(x,y,z),v(X,y,z),v(X,y,Z),v(x,y,Z),2,2,14,14);
    }

    private static float[] v(float x, float y, float z)
    {
        return new float[]{x,y,z};
    }

    private void quad(List<BakedQuad> out, VertexFormat fmt,
                      TextureAtlasSprite sprite, int tint,
                      float[] a, float[] b, float[] c, float[] d,
                      float u0, float v0, float u1, float v1)
    {
        float[] ab = {b[0]-a[0], b[1]-a[1], b[2]-a[2]};
        float[] ac = {c[0]-a[0], c[1]-a[1], c[2]-a[2]};
        Vector3f n = new Vector3f(
                ab[1]*ac[2]-ab[2]*ac[1],
                ab[2]*ac[0]-ab[0]*ac[2],
                ab[0]*ac[1]-ab[1]*ac[0]);
        if (n.lengthSquared() < 0.000001F) return;
        n.normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(fmt);
        builder.setTexture(sprite);
        builder.setQuadOrientation(
                EnumFacing.getFacingFromVector(n.x, n.y, n.z));
        builder.setApplyDiffuseLighting(true);
        if (tint >= 0) builder.setQuadTint(tint);

        put(builder, fmt, sprite, a, u0, v0, n);
        put(builder, fmt, sprite, b, u1, v0, n);
        put(builder, fmt, sprite, c, u1, v1, n);
        put(builder, fmt, sprite, d, u0, v1, n);
        out.add(builder.build());
    }

    private void put(UnpackedBakedQuad.Builder builder, VertexFormat fmt,
                     TextureAtlasSprite sprite, float[] p,
                     float u, float v, Vector3f n)
    {
        float light = n.y > 0.5F ? 1F :
                n.y < -0.5F ? 0.68F : 0.86F;
        for (int i = 0; i < fmt.getElementCount(); i++)
        {
            VertexFormatElement e = fmt.getElement(i);
            switch (e.getUsage())
            {
                case POSITION:
                    builder.put(i, p[0], p[1], p[2]);
                    break;
                case COLOR:
                    builder.put(i, light, light, light, 1F);
                    break;
                case UV:
                    if (e.getIndex() == 0)
                        builder.put(i, sprite.getInterpolatedU(u),
                                sprite.getInterpolatedV(v));
                    else
                        builder.put(i, 0F, 0F);
                    break;
                case NORMAL:
                    builder.put(i, n.x, n.y, n.z);
                    break;
                case PADDING:
                    builder.put(i, 0F);
                    break;
                default:
                    builder.put(i);
                    break;
            }
        }
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return metal;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides()
    {
        return ItemOverrideList.NONE;
    }
}
