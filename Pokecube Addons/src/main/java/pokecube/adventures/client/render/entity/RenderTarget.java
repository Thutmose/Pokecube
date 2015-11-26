package pokecube.adventures.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;

public class RenderTarget extends RendererLivingEntity
{

    public RenderTarget(ModelBase par1ModelBase, float par2)
    {
        super(Minecraft.getMinecraft().getRenderManager(), par1ModelBase, par2);
    }

    public RenderTarget()
    {
        super(Minecraft.getMinecraft().getRenderManager(), new ModelTarget(), 0);
    }

    @Override
    public void doRender(EntityLivingBase entity, double d, double d1, double d2, float f, float partialTick)
    {
        super.doRender(entity,d,d1,d2,f,partialTick);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return new ResourceLocation(PokecubeAdv.ID, "textures/hologram.png");
    }

    public static class ModelTarget extends ModelBase
    {

        ModelRenderer box;

        public ModelTarget()
        {
            this.textureHeight = 12;
            this.textureWidth = 12;
            box = new ModelRenderer(this, 0, 0);
            box.addBox(0F, 0F, 0F, 5, 5, 5);
        }

        public void renderAll(float f5)
        {
            box.render(f5);
        }

        public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
        {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glTranslated(-0.055, 1.4, -0.055);
            GL11.glScaled(0.24, 0.24, 0.24);
            GL11.glColor4f(1, 1, 1, 0.5f);
            RenderHelper.disableStandardItemLighting();
            renderAll(0.1f);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

    }
}
