package com.example.examplemod.client.model;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HBM-style animated OBJ machine renderer. The geometry is original and fills
 * the entire 2 x 2 x 3 engine footprint instead of painting a cube facade.
 *
 * The OBJ contains separately named groups:
 * Base, Flywheel, Shaft, Transmission and Piston.
 *
 * Geometry coordinates are relative to the front lower-left controller:
 *  X [-1.5, +0.5], Y [-0.5, +1.5], Z [-2.5, +0.5].
 * The front mechanical parts project slightly past the Z=+0.5 face.
 *
 * No Forge OBJLoader or third-party dependency is required: the tiny parser
 * understands ordinary triangulated v/vt/o/f Wavefront OBJ geometry.
 */
@SideOnly(Side.CLIENT)
public final class SteamEngineModel
{
    private static final ResourceLocation MODEL =
            new ResourceLocation(ExampleMod.MODID,
                    "models/machines/steam_engine.obj");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ExampleMod.MODID,
                    "textures/blocks/steam_engine_materials.png");

    // Mathematically exact crank-slider linkage in the front X/Y plane.
    private static final float WHEEL_X = -1.01F;
    private static final float WHEEL_Y = -0.08F;
    private static final float WHEEL_Z = 0.80F;
    private static final float CRANK_RADIUS = 0.23F;
    private static final float ROD_LENGTH = 1.08F;
    private static final float SLIDER_Y = -0.10F;

    // Each float[8] is x,y,z,u,v,nx,ny,nz for one triangle vertex.
    private Map<String, List<float[]>> mesh;

    public void render(float rotorAngle)
    {
        ensureLoaded();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);

        renderPart("Base");

        GlStateManager.pushMatrix();
        GlStateManager.translate(WHEEL_X, WHEEL_Y, WHEEL_Z);
        GlStateManager.rotate(rotorAngle, 0F, 0F, 1F);
        renderPart("Flywheel");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.19F, 0.32F, 0.75F);
        GlStateManager.rotate(-2.0F * rotorAngle, 0F, 0F, 1F);
        renderPart("Shaft");
        GlStateManager.popMatrix();

        double theta = Math.toRadians(rotorAngle);
        double crankX = WHEEL_X + CRANK_RADIUS * Math.cos(theta);
        double crankY = WHEEL_Y + CRANK_RADIUS * Math.sin(theta);
        double dy = SLIDER_Y - crankY;
        // Rod is constant length; do not fake variable-length linkage.
        double sliderX = crankX + Math.sqrt(
                Math.max(0D, ROD_LENGTH * ROD_LENGTH - dy * dy));
        float rodAngle = (float) Math.toDegrees(
                Math.atan2(dy, sliderX - crankX));

        GlStateManager.pushMatrix();
        GlStateManager.translate(crankX, crankY, 0.94D);
        GlStateManager.rotate(rodAngle, 0F, 0F, 1F);
        renderPart("Transmission");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(sliderX, SLIDER_Y, 0.75D);
        renderPart("Piston");
        GlStateManager.popMatrix();
    }

    private void renderPart(String name)
    {
        List<float[]> vertices = mesh.get(name);
        if (vertices == null || vertices.isEmpty())
        {
            return;
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder builder = tess.getBuffer();
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (float[] v : vertices)
        {
            builder.pos(v[0], v[1], v[2])
                    .tex(v[3], v[4])
                    .normal(v[5], v[6], v[7])
                    .endVertex();
        }
        tess.draw();
    }

    private void ensureLoaded()
    {
        if (mesh != null)
        {
            return;
        }
        try
        {
            mesh = parseModel();
            for (String part : new String[]{
                    "Base", "Flywheel", "Shaft", "Transmission", "Piston"})
            {
                if (!mesh.containsKey(part) || mesh.get(part).isEmpty())
                {
                    throw new IOException("Missing OBJ group: " + part);
                }
            }
        }
        catch (IOException | RuntimeException e)
        {
            throw new IllegalStateException("Cannot load steam engine OBJ " + MODEL, e);
        }
    }

    private static Map<String, List<float[]>> parseModel() throws IOException
    {
        List<float[]> positions = new ArrayList<float[]>();
        List<float[]> texCoords = new ArrayList<float[]>();
        Map<String, List<float[]>> groups =
                new LinkedHashMap<String, List<float[]>>();
        List<float[]> current = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Minecraft.getMinecraft().getResourceManager()
                                .getResource(MODEL).getInputStream(),
                        StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#')
                {
                    continue;
                }

                String[] fields = line.split("\\s+");
                if ("v".equals(fields[0]) && fields.length >= 4)
                {
                    positions.add(new float[]{
                            Float.parseFloat(fields[1]),
                            Float.parseFloat(fields[2]),
                            Float.parseFloat(fields[3])});
                }
                else if ("vt".equals(fields[0]) && fields.length >= 3)
                {
                    // OBJ V has bottom origin, Minecraft's texture V has top.
                    texCoords.add(new float[]{
                            Float.parseFloat(fields[1]),
                            1F - Float.parseFloat(fields[2])});
                }
                else if ("o".equals(fields[0]) && fields.length >= 2)
                {
                    String name = fields[1];
                    current = groups.get(name);
                    if (current == null)
                    {
                        current = new ArrayList<float[]>();
                        groups.put(name, current);
                    }
                }
                else if ("f".equals(fields[0]) && fields.length >= 4)
                {
                    if (current == null)
                    {
                        throw new IOException("OBJ face has no named group");
                    }

                    // Triangles (and standard quads via fan triangulation).
                    for (int i = 2; i + 1 < fields.length; i++)
                    {
                        float[][] p = new float[3][];
                        float[][] uv = new float[3][];
                        String[] refs = {fields[1], fields[i], fields[i + 1]};
                        for (int j = 0; j < 3; j++)
                        {
                            String[] indices = refs[j].split("/");
                            p[j] = positions.get(index(indices[0], positions.size()));
                            uv[j] = texCoords.get(index(indices[1], texCoords.size()));
                        }

                        float abx = p[1][0] - p[0][0];
                        float aby = p[1][1] - p[0][1];
                        float abz = p[1][2] - p[0][2];
                        float acx = p[2][0] - p[0][0];
                        float acy = p[2][1] - p[0][1];
                        float acz = p[2][2] - p[0][2];
                        float nx = aby * acz - abz * acy;
                        float ny = abz * acx - abx * acz;
                        float nz = abx * acy - aby * acx;
                        float length = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
                        if (length > 0.000001F)
                        {
                            nx /= length;
                            ny /= length;
                            nz /= length;
                        }

                        for (int j = 0; j < 3; j++)
                        {
                            current.add(new float[]{
                                    p[j][0], p[j][1], p[j][2],
                                    uv[j][0], uv[j][1], nx, ny, nz});
                        }
                    }
                }
            }
        }
        return groups;
    }

    private static int index(String text, int size)
    {
        int id = Integer.parseInt(text);
        return id > 0 ? id - 1 : size + id;
    }
}
