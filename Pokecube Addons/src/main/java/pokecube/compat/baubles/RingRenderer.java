package pokecube.compat.baubles;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.client.models.items.ModelRing;
import pokecube.core.items.megastuff.ItemMegawearable;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;

public class RingRenderer implements LayerRenderer<EntityPlayer>
{
    private ModelRing                 ring  = new ModelRing();
    X3dModel                          model;
    X3dModel                          model2;
    ResourceLocation                  belt_1 = new ResourceLocation("pokecube_compat:textures/items/Belt1.png");
    ResourceLocation                  belt_2 = new ResourceLocation("pokecube_compat:textures/items/Belt2.png");

    private final RenderLivingBase<?> livingEntityRenderer;

    public RingRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
        model = new X3dModel(new ResourceLocation("pokecube_compat:models/item/Belt.x3d"));
        model2 = new X3dModel(new ResourceLocation("pokecube_compat:models/item/Belt.x3d"));
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        InventoryBaubles inv = PlayerHandler.getPlayerBaubles(player);
        ItemStack beltStack = null;

        boolean left = false;
        boolean right = false;
        boolean belt = false;

        if (inv.getStackInSlot(1) != null && inv.getStackInSlot(1).getItem() instanceof ItemMegawearable)
        {
            right = true;
        }
        if (inv.getStackInSlot(2) != null && inv.getStackInSlot(2).getItem() instanceof ItemMegawearable)
        {
            left = true;
        }
        if (inv.getStackInSlot(3) != null && inv.getStackInSlot(3).getItem() instanceof ItemMegawearable)
        {
            belt = true;
            beltStack = inv.getStackInSlot(3);
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
                GlStateManager.translate(0.0F, 0.203125F, -0.07F);
            }
            if (thin) GlStateManager.scale(0.75, 1, 0.75);
            else
            {
                GlStateManager.scale(0.85, 1, 0.85);
                GL11.glTranslated(0.02, 0, 0.01);
            }
            ring.stack = inv.getStackInSlot(2);
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
                GlStateManager.translate(0.0F, 0.203125F, -0.07F);
            }
            if (thin)
            {
                GlStateManager.scale(0.75, 1, 0.75);
                GL11.glTranslated(0.02, 0, 0.01);
            }
            else GlStateManager.scale(0.85, 1, 0.85);
            ring.stack = inv.getStackInSlot(1);
            ring.render(player, f, f1, partialTicks, f3, f4, 0.0625f);

            GlStateManager.popMatrix();
        }
        if (belt)
        {

            int brightness = player.getBrightnessForRender(partialTicks);
            // First pass of render
            GL11.glPushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            GL11.glPushMatrix();
            float dx = 0, dy = -.0f, dz = -0.6f;
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslatef(dx, dy, dz);
            float s = 0.525f;
            if (player.getItemStackFromSlot(EntityEquipmentSlot.LEGS) == null)
            {
                s = 0.465f;
            }
            GL11.glScalef(s, s, s);
            this.livingEntityRenderer.bindTexture(belt_1);
            model.renderAll();
            GL11.glPopMatrix();
            // Second pass with colour.
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslatef(dx, dy, dz);
            GL11.glScalef(s, s, s);
            this.livingEntityRenderer.bindTexture(belt_2);
            EnumDyeColor ret = EnumDyeColor.GRAY;
            if (beltStack.hasTagCompound() && beltStack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = beltStack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            Color colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
            int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
            for (IExtendedModelPart part : model2.getParts().values())
            {
                part.setRGBAB(col);
            }
            model2.renderAll();
            GL11.glColor3f(1, 1, 1);
            GL11.glPopMatrix();
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
