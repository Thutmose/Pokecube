package pokecube.core.client.render.blocks;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import pokecube.core.blocks.tradingTable.BlockTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.interfaces.IPokecube;

@SuppressWarnings({ "rawtypes" })
public class RenderTradingTable extends TileEntitySpecialRenderer
{
    public void renderItem(double x, double y, double z, ItemStack item, int time)
    {
        if (item.getItem() instanceof IPokecube)
        {
            glPushMatrix();

            glPopMatrix();
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int i1)
    {
        TileEntityTradingTable table = (TileEntityTradingTable) tileentity;
        if (!(table.getWorld().getBlockState(table.getPos()).getBlock() instanceof BlockTradingTable)) return;
        if (table.getWorld().getBlockState(table.getPos()).getValue(BlockTradingTable.TMC)) return;

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

        if (l == 5 || l == 4)
        {
            short1 = 180;
            glRotatef((float) short1 + 90, 0.0F, 1.0F, 0.0F);
            glTranslated(0, 0, -1);
        }

        if (l == 3 || l == 2)
        {
            short1 = -90;
            glRotatef((float) short1 + 90, 0.0F, 1.0F, 0.0F);
        }
        if (table.getStackInSlot(0) != null)
        {
            glPushMatrix();
            glTranslatef(-0.375f, 0.2f, 0);
            glTranslatef(0.5F, 0.8f, 0.5F);
            glScalef(0.15f, 0.15f, 0.15f);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            glRotatef(-90, 0, 1, 0);
            RenderHelper.disableStandardItemLighting();
            if (table.getStackInSlot(0).getItem() instanceof IPokecube) Minecraft.getMinecraft().getItemRenderer()
                    .renderItem(Minecraft.getMinecraft().thePlayer, table.getStackInSlot(0), TransformType.NONE);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            RenderHelper.enableStandardItemLighting();
            glPopMatrix();
        }
        if (table.getStackInSlot(1) != null)
        {
            glPushMatrix();
            glTranslatef(0.375f, 0.2f, 0);
            glTranslatef(0.5F, 0.8f, 0.5F);
            glScalef(0.15f, 0.15f, 0.15f);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            glRotatef(90, 0, 1, 0);
            RenderHelper.disableStandardItemLighting();
            if (table.getStackInSlot(1).getItem() instanceof IPokecube) Minecraft.getMinecraft().getItemRenderer()
                    .renderItem(Minecraft.getMinecraft().thePlayer, table.getStackInSlot(1), TransformType.NONE);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            RenderHelper.enableStandardItemLighting();
            glPopMatrix();
        }

        glPopMatrix();

    }
}
