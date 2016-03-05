package pokecube.core.client.render.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.interfaces.IPokemob;

public class RenderHeldPokemobs implements LayerRenderer<EntityPlayer>
{
    private final RendererLivingEntity<?> livingEntityRenderer;

    public RenderHeldPokemobs(RendererLivingEntity<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        if(player.riddenByEntity == null || !(player.riddenByEntity instanceof IPokemob)) return;
        
        EntityLivingBase entity = (EntityLivingBase) player.riddenByEntity;
        
        IPokemob pokemob = (IPokemob) entity;
        
        if(pokemob.getPokemonAIState(IPokemob.HELD))
        {
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625F);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }
            GlStateManager.translate(0, 0.5, 0);
            GlStateManager.rotate(180, 0, 0, 1);
            
            float rot = 0;
            
            rot = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
            rot -= (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
            
            GlStateManager.rotate(rot + 180, 0, 1, 0);
            RenderPokemobs.getInstance().getRenderer(pokemob.getPokedexEntry()).doRender(entity, 0, 0, 0, 0, partialTicks);
            GlStateManager.popMatrix();
        }
        else
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0, pokemob.getSize()*pokemob.getPokedexEntry().length/4);
            GlStateManager.rotate(180, 0, 0, 1);
            
            float rot = 0;
            
            rot = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
            rot -= (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
            
            GlStateManager.rotate(rot + 180, 0, 1, 0);
            RenderPokemobs.getInstance().getRenderer(pokemob.getPokedexEntry()).doRender(entity, 0, 0, 0, 0, partialTicks);
            GlStateManager.popMatrix();
        }
        
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
