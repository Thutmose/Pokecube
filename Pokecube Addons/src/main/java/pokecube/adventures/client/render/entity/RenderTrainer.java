package pokecube.adventures.client.render.entity;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.entity.villager.EntityTrader;

public class RenderTrainer<T extends EntityLiving> extends RenderBiped<T>
{
    private static Map<TypeTrainer, ResourceLocation> males   = Maps.newHashMap();
    private static Map<TypeTrainer, ResourceLocation> females = Maps.newHashMap();

    private ModelBiped                                male;
    private ModelBiped                                female;

    public RenderTrainer(RenderManager manager)
    {
        super(manager, new ModelBiped(0.0F), 0.5f);
        male = new ModelPlayer(0, false);
        female = new ModelPlayer(0, true);
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (((EntityTrainer) entity).male)
        {
            mainModel = male;
        }
        else
        {
            mainModel = female;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    /** Returns the location of an entity's texture. Doesn't seem to be called
     * unless you call Render.bindEntityTexture. */
    protected ResourceLocation getEntityTexture(T villager)
    {
        ResourceLocation texture = null;

        if (villager instanceof EntityTrainer)
        {
            TypeTrainer type = ((EntityTrainer) villager).getType();

            if (((EntityTrainer) villager).male)
            {
                texture = males.get(type);
            }
            else
            {
                texture = females.get(type);
            }
            if (texture == null)
            {
                texture = type == null ? super.getEntityTexture((T) villager)
                        : type.getTexture(((EntityTrainer) villager));

                if (((EntityTrainer) villager).male)
                {
                    males.put(type, texture);
                }
                else
                {
                    females.put(type, texture);
                }
            }
            return texture;
        }
        else if (villager instanceof EntityTrader)
        {
            texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + ((EntityTrader) villager).texture + ".png");
        }
        else
        {
            texture = new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + "male.png");
        }
        return texture;
    }
}
