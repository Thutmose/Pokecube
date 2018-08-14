package pokecube.modelloader.client.render;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import pokecube.core.ai.thread.logicRunnables.LogicMiscUpdate;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.modelloader.client.render.TabulaPackLoader.TabulaModelSet;
import thut.api.maths.Vector3;
import thut.core.client.render.model.IModel;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.wrappers.TabulaWrapper;

public class TabulaModelRenderer<T extends EntityLiving> extends AbstractModelRenderer<T>
{
    public TabulaModelSet set;
    public TabulaWrapper  model;

    public TabulaModelRenderer(TabulaModelSet set)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, 0);
        this.set = set;
        this.model = new TabulaWrapper(set.model, set.parser, this);
        mainModel = model;
        this.setAnimationChanger(set);
        this.setTexturer(set.texturer);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        PokedexEntry entry = null;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob != null) entry = pokemob.getPokedexEntry();
        else return;
        if (set == null)
        {
            PokecubeMod.log(Level.WARNING, "Error with model for " + entry);
            set = TabulaPackLoader.modelMap.get(entry.getBaseForme());
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public boolean hasAnimation(String phase, Entity entity)
    {
        ModelJson modelj = null;
        if (set != null) modelj = set.parser.modelMap.get(set.model);
        return set.loadedAnimations.containsKey(phase) || (modelj != null && modelj.animationMap.containsKey(phase));
    }

    @Override
    public HashMap<String, List<Animation>> getAnimations()
    {
        return set.loadedAnimations;
    }

    @Override
    public Vector3 getScale()
    {
        return set.scale;
    }

    @Override
    public Vector3 getRotationOffset()
    {
        return set.shift;
    }

    @Override
    public Vector5 getRotations()
    {
        return set.rotation;
    }

    @Override
    public void scaleEntity(Entity entity, IModel model, float partialTick)
    {
        float s = 1;
        float sx = (float) getScale().x;
        float sy = (float) getScale().y;
        float sz = (float) getScale().z;
        float dx = (float) getRotationOffset().x;
        float dy = (float) getRotationOffset().y;
        float dz = (float) getRotationOffset().z;
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            s = (mob.getSize());
            if (partialTick <= 1 && mob.getGeneralState(GeneralStates.EXITINGCUBE))
            {
                int ticks = -mob.getEvolutionTicks() + 50 + LogicMiscUpdate.EXITCUBEDURATION;
                if (ticks <= LogicMiscUpdate.EXITCUBEDURATION / 2)
                {
                    float max = LogicMiscUpdate.EXITCUBEDURATION / 2;
                    s *= (ticks) / max;
                }
            }
            sx *= s;
            sy *= s;
            sz *= s;
        }
        dy += (1 - sy) * 1.5;
        getRotations().rotations.glRotate();
        GlStateManager.translate(dx, dy, dz);
        GlStateManager.scale(sx, sy, sz);
    }
}
