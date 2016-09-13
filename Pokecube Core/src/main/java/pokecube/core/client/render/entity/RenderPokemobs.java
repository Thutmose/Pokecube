package pokecube.core.client.render.entity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.EntityTools;

@SuppressWarnings("rawtypes")
public class RenderPokemobs extends RenderPokemob
{

    private static Map<String, ModelBase> models       = new HashMap<String, ModelBase>();
    private static Map<String, ModelBase> statusModels = new HashMap<String, ModelBase>();
    public static Map<String, Render>     renderMap    = new HashMap<String, Render>();

    private static RenderPokemobs         instance;

    public static void addCustomRenderer(String name, Render renderer)
    {
        renderMap.put(name, renderer);
    }

    public static void addModel(String name, ModelBase model)
    {
        models.put(name, model);
        renderMap.put(name, instance);
    }

    public static RenderPokemobs getInstance()
    {
        if (instance == null) instance = new RenderPokemobs(Minecraft.getMinecraft().getRenderManager());
        if (instance.renderManager == null)
        {
            System.out.println(Minecraft.getMinecraft().getRenderManager());
            new Exception().printStackTrace();
        }
        return instance;
    }

    public static RenderPokemobs getInstance(RenderManager manager)
    {
        if (instance == null) instance = new RenderPokemobs(manager);
        if (instance.renderManager == null)
        {
            System.out.println(Minecraft.getMinecraft().getRenderManager());
            new Exception().printStackTrace();
        }
        return instance;
    }

    public static ModelBase[] getModels(PokedexEntry entry)
    {
        String nbm = entry.getName() + entry.getModId();
        ModelBase[] ret = new ModelBase[2];
        ret[0] = models.get(nbm);
        if (statusModels.get(nbm) == null)
        {
            try
            {
                statusModels.put(nbm, ret[0].getClass().getConstructor().newInstance());
            }
            catch (Exception e)
            {
                System.out.println(nbm);
                e.printStackTrace();
            }
        }
        ret[1] = statusModels.get(nbm);

        return ret;
    }

    private RenderPokemobs(RenderManager m)
    {
        super(m, null, 0);
    }

    /** Actually renders the given argument. This is a synthetic bridge method,
     * always casting down its argument and then handing it off to a worker
     * function which does the actual work. In all probabilty, the class Render
     * is generic (Render<T extends Entity) and this method has signature public
     * void doRender(T entity, double d, double d1, double d2, float f, float
     * f1). But JAD is pre 1.5 so doesn't do that. */
    @SuppressWarnings("unchecked")
    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float par8, float par9)
    {
        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            int nb = mob.getPokedexNb();
            if (!PokecubeMod.registered.get(nb))
            {
                System.err.println("attempting to render an un-registed pokemon " + entity);
                return;
            }
            if (mob.getTransformedTo() instanceof IPokemob)
            {
                mob = (IPokemob) mob.getTransformedTo();
            }
            else if (mob.getTransformedTo() != null && mob.getTransformedTo() instanceof EntityLivingBase)
            {
                EntityLivingBase from = (EntityLivingBase) mob.getTransformedTo();
                try
                {
                    Class<? extends EntityLivingBase> claz = from.getClass();
                    EntityLivingBase to;
                    if (claz == EntityPlayerSP.class)
                    {
                        claz = EntityOtherPlayerMP.class;
                        to = new EntityOtherPlayerMP(entity.worldObj, (((EntityPlayerSP) from).getGameProfile()));
                    }
                    else
                    {
                        to = claz.getConstructor(World.class).newInstance(from.worldObj);
                    }
                    EntityTools.copyEntityData(to, from);
                    EntityTools.copyEntityTransforms(to, entity);
                    Minecraft.getMinecraft().getRenderManager().doRenderEntity(to, x, y, z, par8, par9, false);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                return;
            }

            GlStateManager.pushMatrix();
            // // Handle held/shoulder, whenever entities can mount players...

            PokedexEntry entry = mob.getPokedexEntry();
            this.scale = (entry.height * mob.getSize());
            // this.shadowSize = entry.width * mob.getSize()/2;//TODO decide if
            // shadows are needed.
            // System.out.println(nbm+" "+renderMap.get(nbm));
            Render render = getRenderer(mob.getPokedexEntry());
            if (render == instance)
            {
                String nbm = entry.getTrimmedName() + entry.getModId();
                setModel(nbm);
                if (this.mainModel == null)
                {
                    GlStateManager.popMatrix();
                    return;
                }
                super.doRender(entity, x, y, z, par8, par9);
            }
            else
            {
                render.doRender(entity, x, y, z, par8, par9);
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected ResourceLocation getPokemobTexture(IPokemob entity)
    {
        IPokemob mob = entity;

        if (mob.getTransformedTo() instanceof IPokemob)
        {
            int num = mob.getPokedexNb();

            if (num == 132)
            {
                if (((EntityLiving) mob).getEntityData()
                        .getBoolean("dittotag")) { return super.getPokemobTexture(mob); }
            }
            mob = (IPokemob) mob.getTransformedTo();
        }
        return super.getPokemobTexture(mob);
    }

    public Render getRenderer(PokedexEntry entry)
    {
        String nbm = entry.getTrimmedName() + entry.getModId();
        Render ret;
        if ((ret = renderMap.get(nbm)) == null)
        {
            String nbm2 = entry.getBaseName() + entry.getModId();
            if ((ret = renderMap.get(nbm2)) == null)
            {
                renderMap.put(nbm2, getInstance());
                renderMap.put(nbm, getInstance());
                return instance;
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void setModel(String nb)
    {
        this.mainModel = models.get(nb);
        this.modelStatus = statusModels.get(nb);
        if (this.modelStatus == null && mainModel != null) try
        {
            this.modelStatus = mainModel.getClass().getConstructor().newInstance();
            statusModels.put(nb, modelStatus);
        }
        catch (Exception e)
        {
            System.out.println(nb);
            e.printStackTrace();
        }
    }
}
