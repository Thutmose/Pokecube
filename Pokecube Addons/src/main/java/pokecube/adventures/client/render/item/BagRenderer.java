package pokecube.adventures.client.render.item;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.adventures.client.models.items.ModelBag;
import pokecube.adventures.items.bags.ItemBag;

public class BagRenderer implements LayerRenderer<EntityPlayer>
{
//    private final RendererLivingEntity<?> livingEntityRenderer;

    public BagRenderer(RendererLivingEntity<?> livingEntityRendererIn)
    {
//        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);

        boolean bag = false;

        if (inv.getStackInSlot(3) != null && inv.getStackInSlot(3).getItem() instanceof ItemBag)
        {
            bag = true;
        }
        
        if(bag)
        {
            ModelBag.model.render(0.5f);
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
