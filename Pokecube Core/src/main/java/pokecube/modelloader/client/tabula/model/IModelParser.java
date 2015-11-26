package pokecube.modelloader.client.tabula.model;

import com.google.common.annotations.Beta;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Beta
public interface IModelParser<T extends IModel> {

    String getExtension();

    @SideOnly(Side.CLIENT)
    T parse(String json, InputStream tex) throws IOException;

    void encode(ByteBuf buf, T model);

    T decode(ByteBuf buf);

    @SideOnly(Side.CLIENT)
    int getTextureId(T model);

    @SideOnly(Side.CLIENT)
    void render(T model, Entity entity);

    Class<T> getModelClass();
}
