package pokecube.core.client.render.blocks;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import pokecube.core.blocks.berries.BlockBerryFruit;
import pokecube.core.client.models.block.berries.ModelBerry;
import pokecube.core.client.models.block.berries.ModelEnigma;
import pokecube.core.client.models.block.berries.ModelLeppa;
import pokecube.core.client.models.block.berries.ModelNanab;
import pokecube.core.client.models.block.berries.ModelOran;
import pokecube.core.client.models.block.berries.ModelPecha;
import pokecube.core.client.models.block.berries.ModelSitrus;

public class RenderBerries extends TileEntitySpecialRenderer {
	
	private HashMap<Integer, ModelBerry> berryModels = new HashMap<Integer, ModelBerry>();
	
	public RenderBerries()
	{
		berryModels.put(7, new ModelOran());
		berryModels.put(6, new ModelLeppa());
		berryModels.put(10, new ModelSitrus());
		berryModels.put(3, new ModelPecha());
		berryModels.put(60, new ModelEnigma());
		berryModels.put(18, new ModelNanab());
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d0, double d1,
			double d2, float f, int i1) {
		Block block = tileentity.getBlockType();
		if(block instanceof BlockBerryFruit)
		{
			BlockBerryFruit fruit = (BlockBerryFruit)block;
			if(!berryModels.containsKey(fruit.berryIndex))
				return;
			ModelBerry berry = berryModels.get(fruit.berryIndex);
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glTranslated(d0+0.5, d1+1.25, d2+0.5);
			GL11.glScaled(0.25, -0.25, 0.25);
			berry.render(0.1f);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glPopMatrix();
		}
	}


}

/*

 */
