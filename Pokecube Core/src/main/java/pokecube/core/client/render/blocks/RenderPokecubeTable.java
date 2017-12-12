package pokecube.core.client.render.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
//import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.utils.PokecubeSerializer;


@SuppressWarnings({ "rawtypes" })
public class RenderPokecubeTable extends TileEntitySpecialRenderer {

	//static ModelPokecubeTable model = new ModelPokecubeTable();

	 //The model of your block
    //private final BlockPokecubeTable block;
    
    public RenderPokecubeTable() {
    }
    
    @Override
    public void render(TileEntity te, double x, double y, double z, float scale, int i, float f) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x + 0.5F, (float) y + 1.15F, (float) z + 0.5F);

                    
            GL11.glPushMatrix();
            GL11.glScaled(0.25, 0.25, 0.25);
            
            EntityPlayer player = Minecraft.getMinecraft().player;
            
            boolean starter = PokecubeSerializer.getInstance().hasStarter(player);
            
            if(!starter)
            {
                try
                {
                    ItemStack item = new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE));
                    Minecraft.getMinecraft().getItemRenderer().renderItem(player, item, TransformType.NONE);
                }
                catch (Exception e)
                {
                    //No no pokecubes
                }
            }
            
            GL11.glPopMatrix();
            GL11.glPopMatrix();
    }
}
