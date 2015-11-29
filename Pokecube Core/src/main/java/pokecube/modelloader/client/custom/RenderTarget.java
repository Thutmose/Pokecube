package pokecube.modelloader.client.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.modelloader.ModPokecubeML;

@SuppressWarnings("rawtypes")
public class RenderTarget extends Render {

	public static LoadedModel model;
	
	public RenderTarget(LoadedModel model, float par2) {
		super(Minecraft.getMinecraft().getRenderManager());
		RenderTarget.model = model;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(ModPokecubeML.ID, "textures/hologram.png");
	}

	@SuppressWarnings("unchecked")
    @Override
	public void doRender(Entity var1, double var2, double var4, double var6,
			float var8, float var9) {
		model.doRender((EntityLiving) var1, var2, var4, var6, var8, var9);
	}

	
}
