package pokecube.modelloader.client.custom.obj;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class BakedRenderer
{
    static Map<String, IFlexibleBakedModel> loadedModels = new HashMap<>();

    // A vertex format with normals that doesn't break the OBJ loader.
    // FIXME: Replace with DefaultvertexFormats.POSITION_TEX_COLOR_NORMAL when
    // it works.
    public static final VertexFormat CUSTOM_FORMAT;

    static
    {
        CUSTOM_FORMAT = DefaultVertexFormats.ITEM;
    }

    public static void init()
    {
//        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
//        if (rm instanceof IReloadableResourceManager)
//        {
//            ((IReloadableResourceManager) rm).registerReloadListener(new IResourceManagerReloadListener()
//            {
//                @Override
//                public void onResourceManagerReload(IResourceManager __)
//                {
//                    loadedModels.clear();
//                }
//            });
//        }
    }

    public static void renderModel(IFlexibleBakedModel model)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
        //TODO heirarcheal way to do this.
        if(model instanceof OBJBakedModel)
        {
            renderModelParts((OBJBakedModel) model);
        }
        else
        {
            int color = 0xFFFFFFFF;
            for (BakedQuad bakedquad : model.getGeneralQuads())
            {
                LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
            }
        }
        
        tessellator.draw();
    }
    
    static void renderModelParts(OBJBakedModel model)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int color = 0xFFFFFFFF;
        
        for (BakedQuad bakedquad : model.getGeneralQuads())
        {
            LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
        }
        
    }

    public static IFlexibleBakedModel loadModel(String resourceName)
    {
        IFlexibleBakedModel model = loadedModels.get(resourceName);
        if (model != null) return model;

        try
        {
            final TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();
            IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation(resourceName));
            model = mod.bake(mod.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT,
                    new Function<ResourceLocation, TextureAtlasSprite>()
                    {
                        @Override
                        public TextureAtlasSprite apply(ResourceLocation location)
                        {
                            return textures.getAtlasSprite(location.toString());
                        }
                    });
            loadedModels.put(resourceName, model);
            return model;
        }
        catch (IOException e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }
}
