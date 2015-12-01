package pokecube.modelloader.client.tabula.model.tabula;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.PokedexEntry;
import pokecube.modelloader.client.tabula.components.ModelJson;
import pokecube.modelloader.client.tabula.json.JsonFactory;
import pokecube.modelloader.client.tabula.model.IModelParser;

public class TabulaModelParser implements IModelParser<TabulaModel>
{
    public static Map<PokedexEntry, String> modelJsons = Maps.newHashMap();
    
    @SideOnly(Side.CLIENT)
    public Map<TabulaModel, ModelJson> modelMap;
    
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
        InputStreamReader reader = new InputStreamReader(in);
        tabulaModel = JsonFactory.getGson().fromJson(reader, TabulaModel.class);
        reader.close();
        if (tabulaModel != null)
        {
            modelMap.put(tabulaModel, new ModelJson(tabulaModel));
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
    public void render(TabulaModel model, Entity entity)
    {
        modelMap.get(model).render(entity, 0f, 0f, 0f, 0f, 0f, 0.0625f);
    }

    public Class<TabulaModel> getModelClass()
    {
        return TabulaModel.class;
    }
}
