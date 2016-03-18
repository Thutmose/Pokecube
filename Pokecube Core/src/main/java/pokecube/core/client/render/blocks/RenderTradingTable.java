package pokecube.core.client.render.blocks;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.interfaces.IPokecube;

@SuppressWarnings({ "rawtypes", "deprecation" })
public class RenderTradingTable extends TileEntitySpecialRenderer
{
    public void renderItem(double x, double y, double z, ItemStack item, int time)
    {
        if (item.getItem() instanceof IPokecube)
        {
            glPushMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            glTranslatef((float) x, (float) y, (float) z);
            glPushMatrix();
            glTranslatef(0.5F, 1.0f, 0.5F);
            // glRotatef(-45, 1.0F, 0.0F, 1.0F);
            // glRotatef(time * 2, 0.0F, 1.0F, 0.0F);
            glScalef(0.25f, 0.25f, 0.25f);
            GL11.glColor4f(1, 1, 1, 0.25f);
            RenderHelper.disableStandardItemLighting();
            if (item.getItem() instanceof IPokecube) Minecraft.getMinecraft().getItemRenderer()
                    .renderItem(Minecraft.getMinecraft().thePlayer, item, TransformType.NONE);

            glPopMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            RenderHelper.enableStandardItemLighting();
            glPopMatrix();
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int i1)
    {
        TileEntityTradingTable table = (TileEntityTradingTable) tileentity;
        
        if(table.renderpass==0) return;
        
        if (!table.getWorld().isRemote) System.err.println("not remote");

        glPushMatrix();
        glTranslated(x, y, z);

        glPushMatrix();
        glTranslated(0.5, 0.375, 0.5);
        float scale = 0.1f;
        glScalef(scale, scale * 0.75f, scale);
        
        glPopMatrix();

        int l = 0;

        if (tileentity.hasWorldObj())
        {
            l = tileentity.getBlockMetadata();
        }
        short short1 = 0;

        if (l == 3 || l == 2)
        {
            short1 = 180;
            glRotatef((float) short1 + 90, 0.0F, 1.0F, 0.0F);
            glTranslated(0, 0, -1);
        }

        if (l == 5 || l == 4)
        {
            short1 = -90;
            glRotatef((float) short1 + 90, 0.0F, 1.0F, 0.0F);
        }
        if (table.getStackInSlot(0) != null)
        {
            renderItem(0.25, 0.2, 0, table.getStackInSlot(0), table.time);
        }
        if (table.getStackInSlot(1) != null)
        {
            renderItem(-0.25, 0.2, 0, table.getStackInSlot(1), table.time);
        }

        glPopMatrix();

    }
}
