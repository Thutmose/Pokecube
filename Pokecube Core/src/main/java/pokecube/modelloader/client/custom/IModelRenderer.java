package pokecube.modelloader.client.custom;

import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.client.tabula.components.Animation;

public interface IModelRenderer<T extends EntityLiving>
{
    public static final ResourceLocation FRZ = new ResourceLocation(PokecubeMod.ID, "textures/FRZ.png");
    public static final ResourceLocation PAR = new ResourceLocation(PokecubeMod.ID, "textures/PAR.png");

    void doRender(T entity, double d, double d1, double d2, float f, float partialTick);

    void renderStatus(T entity, double d, double d1, double d2, float f, float partialTick);

    void setPhase(String phase);

    IPartTexturer getTexturer();

    HashMap<String, Animation> getAnimations();
    
    boolean hasPhase(String phase);
}
