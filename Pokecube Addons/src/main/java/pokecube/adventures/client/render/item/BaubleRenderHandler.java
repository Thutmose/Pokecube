package pokecube.adventures.client.render.item;

import org.lwjgl.opengl.GL11;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.item.IBaubleRender.RenderType;
import vazkii.botania.api.item.ICosmeticAttachable;

public final class BaubleRenderHandler implements LayerRenderer<EntityPlayer>
{

    @Override
    public void doRenderLayer(EntityPlayer player, float p_177141_2_, float p_177141_3_, float partialTicks,
            float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);

        dispatchRenders(inv, player, RenderType.BODY, partialTicks);

        float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
        float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.rotate(yawOffset, 0, -1, 0);
        GlStateManager.rotate(yaw - 270, 0, 1, 0);
        GlStateManager.rotate(pitch, 0, 0, 1);
        dispatchRenders(inv, player, RenderType.HEAD, partialTicks);

        GlStateManager.popMatrix();
    }

    private void dispatchRenders(InventoryBaubles inv, EntityPlayer player, RenderType type, float partialTicks) {
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if(stack != null) {
                Item item = stack.getItem();

                if(item instanceof ICosmeticAttachable) {
                    ICosmeticAttachable attachable = (ICosmeticAttachable) item;
                    ItemStack cosmetic = attachable.getCosmeticItem(stack);
                    if(cosmetic != null) {
                        GlStateManager.pushMatrix();
                        GL11.glColor3ub(((byte) 255), ((byte) 255), ((byte) 255)); // Some of the baubles use this so we must restore it manually as well
                        GlStateManager.color(1F, 1F, 1F, 1F);
                        ((IBaubleRender) cosmetic.getItem()).onPlayerBaubleRender(cosmetic, player, type, partialTicks);
                        GlStateManager.popMatrix();
                        continue;
                    }
                }

                if(item instanceof IBaubleRender) {
                    GlStateManager.pushMatrix();
                    GL11.glColor3ub(((byte) 255), ((byte) 255), ((byte) 255)); // Some of the baubles use this so we must restore it manually as well
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    ((IBaubleRender) stack.getItem()).onPlayerBaubleRender(stack, player, type, partialTicks);
                    GlStateManager.popMatrix();
                }
            }
        }
    }
    
    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
