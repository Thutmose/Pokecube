package pokecube.core.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.client.models.ModelRing;
import pokecube.core.items.megastuff.ItemMegaring;

public class RingRenderer implements LayerRenderer<EntityPlayer>
{
    private ModelRing ring = new ModelRing();

    private final RendererLivingEntity<?> livingEntityRenderer;

    public RingRenderer(RendererLivingEntity<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);

        boolean left = false;
        boolean right = false;

        if (inv.getStackInSlot(1) != null && inv.getStackInSlot(1).getItem() instanceof ItemMegaring)
        {
            right = true;
        }
        if (inv.getStackInSlot(2) != null && inv.getStackInSlot(2).getItem() instanceof ItemMegaring)
        {
            left = true;
        }

        boolean thin = ((AbstractClientPlayer) player).getSkinType().equals("slim");

        if (left)
        {
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.1, -0.01, 0);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }
            if (thin) GlStateManager.scale(0.75, 1, 0.75);
            else
            {
                GlStateManager.scale(0.85, 1, 0.85);
                GL11.glTranslated(0.02, 0, 0.01);
            }
            ring.render(player, f, f1, partialTicks, f3, f4, 0.0625f);
            GlStateManager.popMatrix();
        }
        if (right)
        {
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625F);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }
            if (thin)
            {
                GlStateManager.scale(0.75, 1, 0.75);
                GL11.glTranslated(0.02, 0, 0.01);
            }
            else GlStateManager.scale(0.85, 1, 0.85);
            ring.render(player, f, f1, partialTicks, f3, f4, 0.0625f);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
