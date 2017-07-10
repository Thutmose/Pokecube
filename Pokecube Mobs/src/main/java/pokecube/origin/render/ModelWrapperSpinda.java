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
    private static final ResourceLocation normalh = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotsh.png");
    private static final ResourceLocation shinyh  = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotshs.png");
    private static final ResourceLocation normale = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotse.png");
    private static final ResourceLocation shinye  = new ResourceLocation("pokecube_mobs",
            "gen_3/entity/textures/spindaspotses.png");

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
                    Random rand = new Random(((IPokemob) entityIn).getRNGValue());
                    ((IRetexturableModel) part).setTexturer(null);
                    for (int i = 0; i < 4; i++)
                    {
                        float dx = rand.nextFloat();
                        float dy = rand.nextFloat() / 2 + 0.5f;
                        GlStateManager.pushMatrix();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glTranslatef(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        Minecraft.getMinecraft().getTextureManager()
                                .bindTexture(((IPokemob) entityIn).isShiny() ? shinyh : normalh);
                        part.renderOnly("Head");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        dx = rand.nextFloat();
                        dy = rand.nextFloat() / 2 + 0.5f;
                        GL11.glTranslatef(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        Minecraft.getMinecraft().getTextureManager()
                                .bindTexture(((IPokemob) entityIn).isShiny() ? shinye : normale);
                        part.renderOnly("Left_ear");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        dx = rand.nextFloat();
                        dy = rand.nextFloat() / 2 + 0.5f;
                        GL11.glTranslatef(dx, dy, 0.0F);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        part.renderOnly("Right_ear");
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GlStateManager.popMatrix();
                    }
                    if (!statusRender) ((IRetexturableModel) part).setTexturer(renderer.texturer);
                    GlStateManager.pushMatrix();
                    part.renderAll();
                    GlStateManager.popMatrix();
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
