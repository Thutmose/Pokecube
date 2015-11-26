package pokecube.adventures.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.entity.villager.EntityTrader;

public class RenderTrainer extends RenderBiped
{
	
    public RenderTrainer()
    {
        super(Minecraft.getMinecraft().getRenderManager(), new ModelBiped(0.0F), 0.5f);
    }
	
    @Override
    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityLiving villager)
    {
    	if(villager instanceof EntityTrainer)
    	{
    		TypeTrainer type = ((EntityTrainer)villager).getType();
    		return type==null?super.getEntityTexture(villager):type.getTexture(((EntityTrainer)villager));
    		
    	}
    	else if(villager instanceof EntityTrader)
    	{
    		return new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+((EntityTrader)villager).texture+".png");
    	}
		return new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH+"male.png");
    }
}
