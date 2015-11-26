package pokecube.core.client.render.blocks;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderPC extends TileEntitySpecialRenderer
{
    public RenderPC()
    {
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double i, double j, double k, float f, int i1)
    {

        int l = 0;
        int m = 0;
        if (tileentity.hasWorldObj())
        {
            // IBlockState state =
            // tileentity.getWorld().getBlockState(tileentity.getPos());
            int meta = tileentity.getBlockMetadata();// state.getBlock().getMetaFromState(state);
            l = meta & 7;
            m = meta & 8;
        }
        short short1 = 0;

        if (l == 3)
        {
            short1 = 180;
        }

        if (l == 2)
        {
            short1 = 0;
        }

        if (l == 4)
        {
            short1 = 90;
        }

        if (l == 5)
        {
            short1 = -90;
        }

        boolean top = m == 8;

        GL11.glPushMatrix();
        GL11.glTranslated(i, j, k);
        GL11.glTranslatef(0.5F, top ? 1.5F - 1 : 1.5f, 0.5F);
        GL11.glRotatef((float) short1 + 180, 0.0F, 1.0F, 0.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        // ResourceLocation texture = new
        // ResourceLocation("pokecube_adventures:textures/blocks/pc_model.png");
        // FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        //
        // if (top)
        // {
        // model.renderTop((float) 0.0625);
        // }
        // else model.renderBase((float) 0.0625);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

}
