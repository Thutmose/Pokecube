package pokecube.core.client.render.entity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class RenderPokemobs extends RenderPokemob {

	private static Map<String, ModelBase> models = new HashMap();
	private static Map<String, ModelBase> statusModels = new HashMap();
	private static Map<String, Render> renderMap = new HashMap();
	
	private static RenderPokemobs instance;
	
	private RenderPokemobs(RenderManager m) {
		super(m, null, 0);
	}
	
    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
	public void doRender(EntityLiving entity, double x, double y, double z, float par8, float par9)
    {
    	if(entity instanceof IPokemob)
		{
			IPokemob mob = (IPokemob) entity;
			int nb = mob.getPokedexNb();
			
			if(!PokecubeMod.registered.get(nb))
			{
				System.err.println("attempting to render an un-registed pokemon "+entity);
				return;
			}
			if(mob.getTransformedTo() instanceof IPokemob)
			{
				mob = (IPokemob) mob.getTransformedTo();
			}
			if(mob.getPokedexEntry().canSitShoulder && mob.getPokemonAIState(IPokemob.SHOULDER) && ((Entity)mob).ridingEntity!=null)
			{
				Entity riding = ((Entity)mob).ridingEntity;
//				System.out.println(y+" "+entity.posY+" "+riding.posY);
//				y = 0;
			}
			String nbm =mob.getPokedexEntry().getName() +  mob.getPokedexEntry().getModId();

			PokedexEntry entry = mob.getPokedexEntry();
			this.scale = (entry.height * mob.getSize());
			this.shadowSize = entry.width * mob.getSize();
	//		System.out.println(nbm+" "+renderMap.get(nbm));
			
			if(renderMap.get(nbm)==null)
			{
				renderMap.put(nbm, getInstance());
			}
			
			if(renderMap.get(nbm) == instance)
			{
				setModel(nbm);
				if(this.mainModel==null)
				{
					return;
				}
				super.doRender(entity, x, y, z, par8, par9);
			}
			else if(renderMap.get(nbm)!=null)
			{
//				renderMap.get(nbm)..setRenderManager(renderManager);//TODO see if binding render manager was needed
				renderMap.get(nbm).doRender(entity, x, y, z, par8, par9);
			}
			else
			{
				nbm = mob.getPokedexEntry().getBaseName() +  mob.getPokedexEntry().getModId();
				if(renderMap.get(nbm) == instance)
				{
					setModel(nbm);
					if(this.mainModel==null)
					{
						return;
					}
					super.doRender(entity, x, y, z, par8, par9);
				}
				else if(renderMap.get(nbm)!=null)
				{
//					renderMap.get(nbm).setRenderManager(renderManager);
					renderMap.get(nbm).doRender(entity, x, y, z, par8, par9);
				}
			}
		}
    }
    
    @Override
	protected ResourceLocation getPokemobTexture(IPokemob entity){

		IPokemob mob = (IPokemob) entity;
		
		if(mob.getTransformedTo() instanceof IPokemob)
		{
			int num = mob.getPokedexNb();
			
			if(num==132)
			{
				if(((EntityLiving)mob).getEntityData().getBoolean("dittotag"))
				{
			    	return super.getPokemobTexture(mob);
				}
			}
			mob = (IPokemob) mob.getTransformedTo();
		}
    	return super.getPokemobTexture(mob);
    }
	
	public void setModel(String nb)
	{
		this.mainModel = models.get(nb);
		this.modelStatus = statusModels.get(nb);
		if(this.modelStatus==null && mainModel!=null)
		try {
			this.modelStatus = mainModel.getClass().getConstructor().newInstance();
			statusModels.put(nb, modelStatus);
		} catch (Exception e) {
			System.out.println(nb);
			e.printStackTrace();
		} 
	}
	
	public static ModelBase[] getModels(PokedexEntry entry)
	{
		String nbm =entry.getName() +  entry.getModId();
		ModelBase[] ret = new ModelBase[2];
		ret[0] = models.get(nbm);
		if(statusModels.get(nbm)==null)
		{
			try {
				statusModels.put(nbm, ret[0].getClass().getConstructor().newInstance());
			} catch (Exception e) {
				System.out.println(nbm);
				e.printStackTrace();
			} 
		}
		ret[1] = statusModels.get(nbm);
		
		return ret;
	}
	
	public static void addModel(String name, ModelBase model)
	{
		models.put(name, model);
		renderMap.put(name, instance);
	}
	
	public static void addCustomRenderer(String name, Render renderer)
	{
		renderMap.put(name, renderer);
	}
	
	public static RenderPokemobs getInstance() {
		if(instance==null)
			instance = new RenderPokemobs(Minecraft.getMinecraft().getRenderManager());
		if(instance.renderManager==null)
		{
			System.out.println(Minecraft.getMinecraft().getRenderManager());
			new Exception().printStackTrace();
		}
		return instance;
	}
}
