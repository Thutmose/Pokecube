package pokecube.core.client.render.entity;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pokecube.core.items.megastuff.ItemMegaring;

public class RingRenderer implements LayerRenderer<EntityPlayer>
{
    private final RendererLivingEntity<?> livingEntityRenderer;

    public RingRenderer(RendererLivingEntity<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float p_177141_2_, float p_177141_3_, float partialTicks,
            float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        ItemStack itemstack = null;

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

        if (left)
        {
            itemstack = inv.getStackInSlot(2);
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.1, -0.01, 0);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            Minecraft minecraft = Minecraft.getMinecraft();

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }
            // TODO actual ring model here.
            minecraft.getItemRenderer().renderItem(player, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.popMatrix();
        }
        if (right)
        {
            itemstack = inv.getStackInSlot(1);
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625F);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            Minecraft minecraft = Minecraft.getMinecraft();

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }
            // TODO actual ring model here.
            minecraft.getItemRenderer().renderItem(player, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
