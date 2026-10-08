package com.example.examplemod.client.model;

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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public final class FluidDuctBakedModel
        implements IBakedModel
{
    private final ObjModel model;
    private final TextureAtlasSprite baseSprite;
    private final TextureAtlasSprite overlaySprite;
    private final boolean forBlock;
    private final boolean simplePipe;

    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache =
            new List[64];

    private List<BakedQuad> itemQuads;

    public FluidDuctBakedModel(
            ObjModel model,
            TextureAtlasSprite baseSprite,
            TextureAtlasSprite overlaySprite,
            boolean forBlock)
    {
        this.model = model;
        this.baseSprite = baseSprite;
        this.overlaySprite = overlaySprite;
        this.forBlock = forBlock;
        this.simplePipe = false;
    }

    private FluidDuctBakedModel(
            ObjModel model,
            TextureAtlasSprite baseSprite,
            TextureAtlasSprite overlaySprite,
            boolean forBlock,
            boolean simplePipe)
    {
        this.model = model;
        this.baseSprite = baseSprite;
        this.overlaySprite = overlaySprite;
        this.forBlock = forBlock;
        this.simplePipe = simplePipe;
    }

    public static FluidDuctBakedModel forSimplePipe(
            ObjModel model,
            TextureAtlasSprite baseSprite,
            TextureAtlasSprite endSprite,
            boolean forBlock)
    {
        return new FluidDuctBakedModel(
                model,
                baseSprite,
                endSprite,
                forBlock,
                true
        );
    }

    public static ObjModel load(
            ResourceLocation location)
            throws IOException
    {
        InputStream input =
                net.minecraft.client.Minecraft
                        .getMinecraft()
                        .getResourceManager()
                        .getResource(location)
                        .getInputStream();

        try
        {
            return ObjModel.parse(input);
        }
        finally
        {
            input.close();
        }
    }

    @Override
    public List<BakedQuad> getQuads(
            IBlockState state,
            EnumFacing side,
            long rand)
    {
        if (side != null)
        {
            return Collections.emptyList();
        }

        if (simplePipe)
        {
            if (!forBlock)
            {
                if (itemQuads == null)
                {
                    itemQuads = buildSimplePipeQuads(
                            DefaultVertexFormats.ITEM
                    );
                }

                return itemQuads;
            }

            return buildSimplePipeQuads(
                    DefaultVertexFormats.BLOCK
            );
        }

        if (!forBlock)
        {
            if (itemQuads == null)
            {
                itemQuads =
                        buildItemQuads();
            }

            return itemQuads;
        }

        boolean pX = false;
        boolean nX = false;
        boolean pY = false;
        boolean nY = false;
        boolean pZ = false;
        boolean nZ = false;

        if (state != null)
        {
            pX = state.getValue(
                    com.example.examplemod.block.BlockFluidDuct.POS_X
            );
            nX = state.getValue(
                    com.example.examplemod.block.BlockFluidDuct.NEG_X
            );
            pY = state.getValue(
                    com.example.examplemod.block.BlockFluidDuct.POS_Y
            );
            nY = state.getValue(
                    com.example.examplemod.block.BlockFluidDuct.NEG_Y
            );
            pZ = state.getValue(
                    com.example.examplemod.block.BlockFluidDuct.POS_Z
            );
            nZ = state.getValue(
                    com.example.examplemod.block.BlockFluidDuct.NEG_Z
            );
        }

        int mask =
                (pX ? 32 : 0)
                        | (nX ? 16 : 0)
                        | (pY ? 8 : 0)
                        | (nY ? 4 : 0)
                        | (pZ ? 2 : 0)
                        | (nZ ? 1 : 0);

        List<BakedQuad> quads =
                cache[mask];

        if (quads == null)
        {
            quads =
                    buildWorldQuads(
                            pX,
                            nX,
                            pY,
                            nY,
                            pZ,
                            nZ
                    );

            cache[mask] = quads;
        }

        return quads;
    }

    private List<BakedQuad> buildWorldQuads(
            boolean pX,
            boolean nX,
            boolean pY,
            boolean nY,
            boolean pZ,
            boolean nZ)
    {
        int mask =
                (pX ? 32 : 0)
                        | (nX ? 16 : 0)
                        | (pY ? 8 : 0)
                        | (nY ? 4 : 0)
                        | (pZ ? 2 : 0)
                        | (nZ ? 1 : 0);

        return bakeParts(
                getWorldParts(
                        pX,
                        nX,
                        pY,
                        nY,
                        pZ,
                        nZ,
                        mask
                ),
                DefaultVertexFormats.BLOCK,
                true
        );
    }

    private List<String> getWorldParts(
            boolean pX,
            boolean nX,
            boolean pY,
            boolean nY,
            boolean pZ,
            boolean nZ,
            int mask)
    {
        List<String> parts =
                new ArrayList<String>();

        if (mask == 0)
        {
            parts.addAll(
                    Arrays.asList(
                            "pX",
                            "nX",
                            "pY",
                            "nY",
                            "pZ",
                            "nZ"
                    )
            );
        }
        else if (mask == 32
                || mask == 16)
        {
            parts.add("pX");
            parts.add("nX");
        }
        else if (mask == 8
                || mask == 4)
        {
            parts.add("pY");
            parts.add("nY");
        }
        else if (mask == 2
                || mask == 1)
        {
            parts.add("pZ");
            parts.add("nZ");
        }
        else
        {
            if (pX) parts.add("pX");
            if (nX) parts.add("nX");
            if (pY) parts.add("pY");
            if (nY) parts.add("nY");

            if (pZ) parts.add("nZ");
            if (nZ) parts.add("pZ");

            if (!pX && !pY && !pZ)
                parts.add("ppn");

            if (!pX && !pY && !nZ)
                parts.add("ppp");

            if (!nX && !pY && !pZ)
                parts.add("npn");

            if (!nX && !pY && !nZ)
                parts.add("npp");

            if (!pX && !nY && !pZ)
                parts.add("pnn");

            if (!pX && !nY && !nZ)
                parts.add("pnp");

            if (!nX && !nY && !pZ)
                parts.add("nnn");

            if (!nX && !nY && !nZ)
                parts.add("nnp");
        }

        return parts;
    }

    private List<BakedQuad> buildItemQuads()
    {
        return bakeParts(
                Arrays.asList(
                        "pX",
                        "nX",
                        "pZ",
                        "nZ"
                ),
                DefaultVertexFormats.ITEM,
                false
        );
    }

    private List<BakedQuad> buildSimplePipeQuads(
            VertexFormat format)
    {
        List<BakedQuad> result =
                new ArrayList<BakedQuad>();

        for (ObjGroup group : model.groups)
        {
            TextureAtlasSprite sprite;

            if ("Side".equalsIgnoreCase(group.name))
            {
                sprite = baseSprite;
            }
            else if ("Top".equalsIgnoreCase(group.name))
            {
                sprite = overlaySprite;
            }
            else
            {
                continue;
            }

            for (ObjFace face : group.faces)
            {
                result.add(
                        buildQuad(
                                face,
                                format,
                                false,
                                sprite,
                                -1
                        )
                );
            }
        }

        return result;
    }

    private List<BakedQuad> bakeParts(
            Collection<String> partNames,
            VertexFormat format,
            boolean centerToBlock)
    {
        Set<String> filter =
                new HashSet<String>(
                        partNames
                );

        List<BakedQuad> result =
                new ArrayList<BakedQuad>();

        for (ObjGroup group :
                model.groups)
        {
            if (!filter.contains(group.name))
            {
                continue;
            }

            for (ObjFace face :
                    group.faces)
            {
                result.add(
                        buildQuad(
                                face,
                                format,
                                centerToBlock,
                                baseSprite,
                                -1
                        )
                );

                result.add(
                        buildQuad(
                                face,
                                format,
                                centerToBlock,
                                overlaySprite,
                                1
                        )
                );
            }
        }

        return result;
    }

    private BakedQuad buildQuad(
            ObjFace face,
            VertexFormat format,
            boolean centerToBlock,
            TextureAtlasSprite sprite,
            int tintIndex)
    {
        UnpackedBakedQuad.Builder builder =
                new UnpackedBakedQuad.Builder(
                        format
                );

        float[][] positions =
                new float[4][3];

        float[][] uvs =
                new float[4][2];

        for (int i = 0; i < 4; i++)
        {
            int index =
                    face.vertexIndices[
                            Math.min(
                                    i,
                                    face.vertexIndices.length - 1
                            )
                    ];

            ObjVertex vertex =
                    model.vertices.get(index);

            float x = vertex.x;
            float y = vertex.y;
            float z = vertex.z;

            if (centerToBlock)
            {
                x += 0.5F;
                y += 0.5F;
                z += 0.5F;
            }

            positions[i][0] = x;
            positions[i][1] = y;
            positions[i][2] = z;

            int uvIndex =
                    face.textureIndices[
                            Math.min(
                                    i,
                                    face.textureIndices.length - 1
                            )
                    ];

            if (uvIndex >= 0
                    && uvIndex < model.uvs.size())
            {
                ObjUV uv =
                        model.uvs.get(uvIndex);

                uvs[i][0] =
                        uv.u * 16.0F;

                uvs[i][1] =
                        uv.v * 16.0F;
            }
        }

        Vector3f normal =
                getFaceNormal(face);

        builder.setQuadOrientation(
                EnumFacing.getFacingFromVector(
                        normal.x,
                        normal.y,
                        normal.z
                )
        );

        builder.setTexture(
                sprite
        );

        builder.setApplyDiffuseLighting(
                true
        );

        if (tintIndex >= 0)
        {
            builder.setQuadTint(
                    tintIndex
            );
        }

        for (int i = 0; i < 4; i++)
        {
            putVertex(
                    builder,
                    format,
                    positions[i][0],
                    positions[i][1],
                    positions[i][2],
                    uvs[i][0],
                    uvs[i][1],
                    normal,
                    sprite
            );
        }

        return builder.build();
    }

    private Vector3f getFaceNormal(
            ObjFace face)
    {
        if (face.normalIndices.length > 0)
        {
            int ni =
                    face.normalIndices[0];

            if (ni >= 0
                    && ni < model.normals.size())
            {
                ObjVertex n =
                        model.normals.get(ni);

                Vector3f result =
                        new Vector3f(
                                n.x,
                                n.y,
                                n.z
                        );

                result.normalize();

                return result;
            }
        }

        ObjVertex a =
                model.vertices.get(
                        face.vertexIndices[0]
                );

        ObjVertex b =
                model.vertices.get(
                        face.vertexIndices[
                                Math.min(
                                        1,
                                        face.vertexIndices.length - 1
                                )
                        ]
                );

        ObjVertex c =
                model.vertices.get(
                        face.vertexIndices[
                                Math.min(
                                        2,
                                        face.vertexIndices.length - 1
                                )
                        ]
                );

        Vector3f ab =
                new Vector3f(
                        b.x - a.x,
                        b.y - a.y,
                        b.z - a.z
                );

        Vector3f ac =
                new Vector3f(
                        c.x - a.x,
                        c.y - a.y,
                        c.z - a.z
                );

        Vector3f normal =
                new Vector3f();

        normal.cross(
                ab,
                ac
        );

        if (normal.lengthSquared()
                <= 0.000001F)
        {
            normal.set(
                    0F,
                    1F,
                    0F
            );
        }
        else
        {
            normal.normalize();
        }

        return normal;
    }

    private void putVertex(
            UnpackedBakedQuad.Builder builder,
            VertexFormat format,
            float x,
            float y,
            float z,
            float u,
            float v,
            Vector3f normal,
            TextureAtlasSprite sprite)
    {
        int shade =
                computeShade(normal);

        for (int elementIndex = 0;
             elementIndex < format.getElementCount();
             elementIndex++)
        {
            VertexFormatElement element =
                    format.getElement(elementIndex);

            switch (element.getUsage())
            {
                case POSITION:
                    builder.put(
                            elementIndex,
                            x,
                            y,
                            z
                    );
                    break;

                case COLOR:
                    builder.put(
                            elementIndex,
                            shade / 255.0F,
                            shade / 255.0F,
                            shade / 255.0F,
                            1.0F
                    );
                    break;

                case UV:
                    if (element.getIndex() == 0)
                    {
                        builder.put(
                                elementIndex,
                                sprite.getInterpolatedU(u),
                                sprite.getInterpolatedV(v)
                        );
                    }
                    else
                    {
                        builder.put(
                                elementIndex,
                                0.0F,
                                0.0F
                        );
                    }
                    break;

                case NORMAL:
                    builder.put(
                            elementIndex,
                            normal.x,
                            normal.y,
                            normal.z
                    );
                    break;

                case PADDING:
                    builder.put(
                            elementIndex,
                            0.0F
                    );
                    break;

                default:
                    builder.put(
                            elementIndex
                    );
                    break;
            }
        }
    }

    private int computeShade(
            Vector3f normal)
    {
        float brightness =
                (normal.y + 0.7F) * 0.9F
                        - Math.abs(normal.x) * 0.1F
                        + Math.abs(normal.z) * 0.1F;

        brightness =
                Math.max(
                        0.45F,
                        Math.min(
                                1.0F,
                                brightness
                        )
                );

        return Math.max(
                0,
                Math.min(
                        255,
                        (int) (brightness * 255.0F)
                )
        );
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return true;
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
        return baseSprite;
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

    public static final class ObjModel
    {
        private final List<ObjVertex> vertices =
                new ArrayList<ObjVertex>();

        private final List<ObjVertex> normals =
                new ArrayList<ObjVertex>();

        private final List<ObjUV> uvs =
                new ArrayList<ObjUV>();

        private final List<ObjGroup> groups =
                new ArrayList<ObjGroup>();

        private ObjGroup currentGroup;

        private static ObjModel parse(
                InputStream input)
                throws IOException
        {
            ObjModel result =
                    new ObjModel();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(input)
                    );

            String line;

            while ((line = reader.readLine()) != null)
            {
                line =
                        line.trim();

                if (line.isEmpty()
                        || line.startsWith("#"))
                {
                    continue;
                }

                if (line.startsWith("o ")
                        || line.startsWith("g "))
                {
                    result.currentGroup =
                            new ObjGroup(
                                    line.substring(2).trim()
                            );

                    result.groups.add(
                            result.currentGroup
                    );

                    continue;
                }

                if (line.startsWith("v "))
                {
                    String[] parts =
                            line.substring(2)
                                    .trim()
                                    .split("\\s+");

                    result.vertices.add(
                            new ObjVertex(
                                    Float.parseFloat(parts[0]),
                                    Float.parseFloat(parts[1]),
                                    Float.parseFloat(parts[2])
                            )
                    );

                    continue;
                }

                if (line.startsWith("vn "))
                {
                    String[] parts =
                            line.substring(3)
                                    .trim()
                                    .split("\\s+");

                    result.normals.add(
                            new ObjVertex(
                                    Float.parseFloat(parts[0]),
                                    Float.parseFloat(parts[1]),
                                    Float.parseFloat(parts[2])
                            )
                    );

                    continue;
                }

                if (line.startsWith("vt "))
                {
                    String[] parts =
                            line.substring(3)
                                    .trim()
                                    .split("\\s+");

                    result.uvs.add(
                            new ObjUV(
                                    Float.parseFloat(parts[0]),
                                    1.0F - Float.parseFloat(parts[1])
                            )
                    );

                    continue;
                }

                if (line.startsWith("f "))
                {
                    if (result.currentGroup == null)
                    {
                        result.currentGroup =
                                new ObjGroup("Default");

                        result.groups.add(
                                result.currentGroup
                        );
                    }

                    String[] tokens =
                            line.substring(2)
                                    .trim()
                                    .split("\\s+");

                    int[] vi =
                            new int[
                                    tokens.length
                            ];

                    int[] ti =
                            new int[
                                    tokens.length
                            ];

                    int[] ni =
                            new int[
                                    tokens.length
                            ];

                    Arrays.fill(
                            ti,
                            -1
                    );

                    Arrays.fill(
                            ni,
                            -1
                    );

                    for (int i = 0;
                         i < tokens.length;
                         i++)
                    {
                        String[] ref =
                                tokens[i].split(
                                        "/",
                                        -1
                                );

                        vi[i] =
                                parseIndex(
                                        ref[0],
                                        result.vertices.size()
                                );

                        if (ref.length > 1
                                && !ref[1].isEmpty())
                        {
                            ti[i] =
                                    parseIndex(
                                            ref[1],
                                            result.uvs.size()
                                    );
                        }

                        if (ref.length > 2
                                && !ref[2].isEmpty())
                        {
                            ni[i] =
                                    parseIndex(
                                            ref[2],
                                            result.normals.size()
                                    );
                        }
                    }

                    result.currentGroup.faces.add(
                            new ObjFace(
                                    vi,
                                    ti,
                                    ni
                            )
                    );
                }
            }

            reader.close();

            return result;
        }

        private static int parseIndex(
                String token,
                int size)
        {
            int index =
                    Integer.parseInt(
                            token
                    );

            return index < 0
                    ? size + index
                    : index - 1;
        }
    }

    private static final class ObjGroup
    {
        private final String name;
        private final List<ObjFace> faces =
                new ArrayList<ObjFace>();

        private ObjGroup(String name)
        {
            this.name = name;
        }
    }

    private static final class ObjFace
    {
        private final int[] vertexIndices;
        private final int[] textureIndices;
        private final int[] normalIndices;

        private ObjFace(
                int[] vertexIndices,
                int[] textureIndices,
                int[] normalIndices)
        {
            this.vertexIndices =
                    vertexIndices;
            this.textureIndices =
                    textureIndices;
            this.normalIndices =
                    normalIndices;
        }
    }

    private static final class ObjVertex
    {
        private final float x;
        private final float y;
        private final float z;

        private ObjVertex(
                float x,
                float y,
                float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final class ObjUV
    {
        private final float u;
        private final float v;

        private ObjUV(
                float u,
                float v)
        {
            this.u = u;
            this.v = v;
        }
    }
}
