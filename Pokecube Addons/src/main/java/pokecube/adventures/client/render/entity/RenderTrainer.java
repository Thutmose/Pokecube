package pokecube.adventures.client.render.entity;

import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.entity.villager.EntityTrader;

public class RenderTrainer<T extends EntityLiving> extends RenderBiped<T>
{
	
    public RenderTrainer(RenderManager manager)
    {
        super(manager, new ModelBiped(0.0F), 0.5f);
    }
	
    @SuppressWarnings("unchecked")
    @Override
    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityLiving villager)
    {
        ResourceLocation texture = null;

        if (villager instanceof EntityTrainer)
        {
            TypeTrainer type = ((EntityTrainer) villager).getType();
            texture = type == null ? super.getEntityTexture((T) villager) : type.getTexture(((EntityTrainer) villager));

        }
        else if (villager instanceof EntityTrader)
        {
            texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + ((EntityTrader) villager).texture + ".png");
        }
        else
        {
            texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "male.png");
        }
        
        if(texture!=null)
        {
            try
            {
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(texture);
                InputStream stream = res.getInputStream();
                stream.close();
            }
            catch (Exception e)
            {
                texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "youngster.png");
            }
        }
        else
        {
            texture = super.getEntityTexture(null);
        }

        return texture;
    }
}
