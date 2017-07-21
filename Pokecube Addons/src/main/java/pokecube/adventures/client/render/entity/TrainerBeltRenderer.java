package pokecube.adventures.client.render.entity;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.Tools;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.lib.CompatWrapper;

public class TrainerBeltRenderer implements LayerRenderer<EntityLivingBase>
{
    X3dModel                          model;
    X3dModel                          model2;
    ResourceLocation                  belt_1 = new ResourceLocation(PokecubeCore.ID, "textures/worn/belt1.png");
    ResourceLocation                  belt_2 = new ResourceLocation(PokecubeCore.ID, "textures/worn/belt2.png");

    private final RenderLivingBase<?> livingEntityRenderer;
    private IHasPokemobs              pokemobCap;

    public TrainerBeltRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
        model = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/belt.x3d"));
        model2 = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/belt.x3d"));
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.pokemobCap = CapabilityHasPokemobs.getHasPokemobs(entitylivingbaseIn);
        if (pokemobCap.countPokemon() <= 0) return;

        int brightness = entitylivingbaseIn.getBrightnessForRender();
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
        if (!CompatWrapper.isValid(entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.LEGS)))
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
        Color colour = new Color(ret.getColorValue() + 0xFF000000);
        int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
        for (IExtendedModelPart part : model2.getParts().values())
        {
            part.setRGBAB(col);
        }
        model2.renderAll();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(90, 0, 0, 1);
        float rx = 0, ry = -90, rz = 0;
        dx = dy = dz = 0;
        dz = -0.25f;
        dx = 0.25f;
        for (int i = 0; i < 6; i++)
        {
            ItemStack stack = pokemobCap.getPokemob(i);
            if (CompatWrapper.isValid(stack) && !Tools.isSameStack(stack, entitylivingbaseIn.getHeldItemMainhand()))
            {
                GlStateManager.pushMatrix();
                if (i < 3)
                {
                    if (i == 2)
                    {
                        dz = -0.5f;
                        ry = 0;
                        dx = 0;
                    }
                    else
                    {
                        dz = -0.25f * (i + 1);
                        dx = 0.25f;
                        ry = -90;
                    }
                }
                else
                {
                    if (i == 5)
                    {
                        dz = 0.5f;
                        ry = 180;
                        dx = 0;
                    }
                    else
                    {
                        dz = 0.25f * (i - 2);
                        dx = 0.25f;
                        ry = -90;
                    }
                }
                GlStateManager.translate(dx, dy, dz);
                GlStateManager.rotate(rx, 1, 0, 0);
                GlStateManager.rotate(ry, 0, 1, 0);
                GlStateManager.rotate(rz, 0, 0, 1);
                GlStateManager.scale(0.15, 0.15, 0.15);
                Minecraft.getMinecraft().getItemRenderer().renderItem(entitylivingbaseIn, stack, null);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        GL11.glColor3f(1, 1, 1);
        GL11.glPopMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
