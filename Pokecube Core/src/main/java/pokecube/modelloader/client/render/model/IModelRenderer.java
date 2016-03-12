package pokecube.modelloader.client.render.model;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;

public interface IModelRenderer<T extends EntityLiving>
{
    public static final ResourceLocation FRZ = new ResourceLocation(PokecubeMod.ID, "textures/FRZ.png");
    public static final ResourceLocation PAR = new ResourceLocation(PokecubeMod.ID, "textures/PAR.png");

    void doRender(T entity, double d, double d1, double d2, float f, float partialTick);

    IPartTexturer getTexturer();

    boolean hasPhase(String phase);

    void renderStatus(T entity, double d, double d1, double d2, float f, float partialTick);

    void setPhase(String phase);
}
