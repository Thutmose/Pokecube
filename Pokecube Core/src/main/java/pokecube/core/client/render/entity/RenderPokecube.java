package pokecube.core.client.render.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.thread.logicRunnables.LogicMiscUpdate;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;

public class RenderPokecube<T extends EntityLiving> extends RenderLiving<T>
{
    public static class ModelPokecube extends ModelBase
    {

        public ModelPokecube()
        {
        }

        @Override
        public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
        {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glTranslated(-0.0, 1.4, -0.0);
            double scale = 0.3;
            GL11.glScaled(scale, scale, scale);
            GL11.glColor4f(1, 1, 1, 1f);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glRotated(entity.rotationYaw, 0, 1, 0);

            EntityPokecube cube = (EntityPokecube) entity;

            if (cube.isReleasing())
            {
                Entity mob = cube.getReleased();
                IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                if (pokemob != null)
                {
                    RenderPokemob.renderEffect(pokemob, f2 - cube.ticksExisted, LogicMiscUpdate.EXITCUBEDURATION,
                            false);
                }
            }

            if (PokecubeManager.getTilt(cube.getItem()) > 0)
            {
                float rotateY = MathHelper.cos(MathHelper.abs((float) (Math.PI * f2) / 12)) * (180F / (float) Math.PI);// getRotationX(entityItem);
                GL11.glRotatef(rotateY, 0.0F, 0.0F, 1.0F);
            }
            ItemStack renderStack = cube.getItem();
            if (renderStack == null || !(renderStack.getItem() instanceof IPokecube))
            {
                renderStack = PokecubeItems.getStack("pokecube");
            }

            EntityPlayer player = Minecraft.getMinecraft().player;
            Minecraft.getMinecraft().getItemRenderer().renderItem(player, renderStack, TransformType.NONE);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        /** This is a helper function from Tabula to set the rotation of model
         * parts */
        public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
        {
            modelRenderer.rotateAngleX = x;
            modelRenderer.rotateAngleY = y;
            modelRenderer.rotateAngleZ = z;
        }
    }

    public static HashMap<ResourceLocation, Render<Entity>> pokecubeRenderers = new HashMap<ResourceLocation, Render<Entity>>();

    public RenderPokecube(RenderManager renderManager)
    {
        super(renderManager, new ModelPokecube(), 0);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float f, float f1)
    {
        EntityPokecube pokecube = (EntityPokecube) entity;

        long time = pokecube.reset;
        long world = pokecube.getEntityWorld().getTotalWorldTime();
        if (time > world) return;

        ResourceLocation num = PokecubeItems.getCubeId(pokecube.getItem());
        if (pokecubeRenderers.containsKey(num))
        {
            pokecubeRenderers.get(num).doRender(entity, x, y, z, f, f1);
            return;
        }
        super.doRender(entity, x, y, z, f, f1);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return new ResourceLocation(PokecubeMod.ID, "textures/items/pokecubefront.png");
    }

}
