package pokecube.origin.render;

import pokecube.core.interfaces.IPokemob;
import pokecube.modelloader.client.render.AnimationLoader.Model;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.modelloader.client.render.DefaultIModelRenderer;
import pokecube.modelloader.client.render.wrappers.ModelWrapper;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IRetexturableModel;

public class ModelWrapperSpinda extends ModelWrapper
{

    public ModelWrapperSpinda(Model model, DefaultIModelRenderer<?> renderer)
    {
        super(model, renderer);
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale)
    {
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        GlStateManager.pushMatrix();
        if (renderer.animator != null) renderer.currentPhase = renderer.animator
                .modifyAnimation((EntityLiving) entityIn, partialTick, renderer.currentPhase);
        GlStateManager.disableCull();
        transformGlobal(renderer.currentPhase, entityIn, Minecraft.getMinecraft().getRenderPartialTicks(), netHeadYaw,
                headPitch);
        updateAnimation(entityIn, renderer.currentPhase, partialTick, netHeadYaw, headPitch, limbSwing);
        for (String partName : renderer.parts.keySet())
        {
            IExtendedModelPart part = imodel.getParts().get(partName);
            if (part == null) continue;
            try
            {
                if (renderer.texturer != null && part instanceof IRetexturableModel)
                {
                    renderer.texturer.bindObject(entityIn);
                    if (!statusRender) ((IRetexturableModel) part).setTexturer(renderer.texturer);
                    else((IRetexturableModel) part).setTexturer(null);
                }
                if (part.getParent() == null)
                {
                    GlStateManager.pushMatrix();
                    part.renderAll();
                    GlStateManager.popMatrix();
                    Random rand = new Random(((IPokemob) entityIn).getRNGValue());
                    for (int i = 0; i < 4; i++)
                    {
                        float dx = rand.nextFloat();
                        float dy = rand.nextFloat();
                        GlStateManager.pushMatrix();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glTranslatef(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GlStateManager.scale(1.001, 1.001, 1.001);
                        ((IRetexturableModel) part).setTexturer(null);
                        Minecraft.getMinecraft().getTextureManager()
                                .bindTexture(new ResourceLocation("pokecube_ml:textures/entities/spindaspots4.png"));
                        // part.renderAll();
                        part.renderOnly("Head", "Left_ear", "Right_ear");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GlStateManager.popMatrix();
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
