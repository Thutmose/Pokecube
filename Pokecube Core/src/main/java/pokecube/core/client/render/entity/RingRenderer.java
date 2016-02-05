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
import net.minecraft.item.Item;
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

        int n = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                Item item = stack.getItem();
                if (item instanceof ItemMegaring)
                {
                    itemstack = stack;
                    n = i;
                    break;
                }
            }
        }
        if (itemstack != null)
        {
            GlStateManager.pushMatrix();
            if (n == 1 ) ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625F);
            else 
            {
                ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
                GlStateManager.translate(0.1, -0.01, 0);
            }
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            Minecraft minecraft = Minecraft.getMinecraft();

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }
            //TODO actual ring model here.
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
