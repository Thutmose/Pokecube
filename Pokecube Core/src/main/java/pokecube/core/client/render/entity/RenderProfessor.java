package pokecube.core.client.render.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;

public class RenderProfessor<T extends EntityLiving> extends RenderBiped<EntityLiving>
{
    public RenderProfessor(RenderManager manager)
    {
        super(manager, new ModelBiped(0.0F), 0.5f);
    }

    /** Returns the location of an entity's texture. Doesn't seem to be called
     * unless you call Render.bindEntityTexture. */
    @Override
    protected ResourceLocation getEntityTexture(EntityLiving villager)
    {
        return new ResourceLocation(PokecubeMod.ID + ":textures/Professor.png");
    }
}
