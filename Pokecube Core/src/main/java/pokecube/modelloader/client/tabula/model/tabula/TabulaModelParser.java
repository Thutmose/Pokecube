package pokecube.modelloader.client.tabula.model.tabula;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.PokedexEntry;
import pokecube.modelloader.client.tabula.components.ModelJson;
import pokecube.modelloader.client.tabula.json.JsonFactory;
import pokecube.modelloader.client.tabula.model.IModelParser;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TabulaModelParser implements IModelParser<TabulaModel>
{
    public static Map<PokedexEntry, String> modelJsons = Maps.newHashMap();
    
    @SideOnly(Side.CLIENT)
    public Map<TabulaModel, ModelJson> modelMap;
    public Map<TabulaModel, Integer>   textureMap = Maps.newHashMap();
    
    public String getExtension()
    {
        return "tbl";
    }

    @SideOnly(Side.CLIENT)
    public TabulaModel parse(String json, InputStream tex) throws IOException
    {
        if (modelMap == null)
        {
            modelMap = Maps.newHashMap();
        }
        TabulaModel tabulaModel = null;
        InputStream in = IOUtils.toInputStream(json, "UTF-8");
        tabulaModel = JsonFactory.getGson().fromJson(new InputStreamReader(in), TabulaModel.class);
        BufferedImage texture = null;
        texture = ImageIO.read(tex);
        
        if (tabulaModel != null)
        {
            tabulaModel.texture = texture;
            modelMap.put(tabulaModel, new ModelJson(tabulaModel));
            textureMap.put(tabulaModel,
                    TextureUtil.uploadTextureImage(TextureUtil.glGenTextures(), tabulaModel.texture));
            return tabulaModel;
        }
        else
        {
            new NullPointerException("Cannot load model").printStackTrace();
        }
        return null;
    }

    public void encode(ByteBuf buf, TabulaModel model)
    {
        // todo
    }

    public TabulaModel decode(ByteBuf buf)
    {
        return null; // todo
    }

    @SideOnly(Side.CLIENT)
    public int getTextureId(TabulaModel model)
    {
        return textureMap.get(model);
    }

    @SideOnly(Side.CLIENT)
    public void render(TabulaModel model, Entity entity)
    {
        modelMap.get(model).render(entity, 0f, 0f, 0f, 0f, 0f, 0.0625f);
    }

    public Class<TabulaModel> getModelClass()
    {
        return TabulaModel.class;
    }
}
