package pokecube.adventures.client.render.entity;

import java.awt.Color;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;

public class BetterCustomHeadLayer extends LayerCustomHead
{
    ResourceLocation belt_1 = new ResourceLocation("pokecube:textures/worn/hat.png");
    ResourceLocation belt_2 = new ResourceLocation("pokecube:textures/worn/hat2.png");
    X3dModel         model;
    X3dModel         model2;
    ModelRenderer    modelRenderer;

    public BetterCustomHeadLayer(ModelRenderer renderer)
    {
        super(renderer);
        this.modelRenderer = renderer;
        model = new X3dModel(new ResourceLocation("pokecube:models/worn/hat.x3d"));
        model2 = new X3dModel(new ResourceLocation("pokecube:models/worn/hat.x3d"));
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        if (itemstack != null && itemstack.getItem() != null)
        {
            Item item = itemstack.getItem();
            Minecraft minecraft = Minecraft.getMinecraft();
            GlStateManager.pushMatrix();

            if (entitylivingbaseIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            boolean flag = entitylivingbaseIn instanceof EntityVillager
                    || entitylivingbaseIn instanceof EntityZombieVillager;

            if (entitylivingbaseIn.isChild() && !(entitylivingbaseIn instanceof EntityVillager))
            {
                float f = 2.0F;
                float f1 = 1.4F;
                GlStateManager.translate(0.0F, 0.5F * scale, 0.0F);
                GlStateManager.scale(f1 / f, f1 / f, f1 / f);
                GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            }

            this.modelRenderer.postRender(0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (item == Items.SKULL)
            {
                float f2 = 1.1875F;
                GlStateManager.scale(f2, -f2, -f2);

                if (flag)
                {
                    GlStateManager.translate(0.0F, 0.0625F, 0.0F);
                }

                GameProfile gameprofile = null;

                if (itemstack.hasTagCompound())
                {
                    NBTTagCompound nbttagcompound = itemstack.getTagCompound();

                    if (nbttagcompound.hasKey("SkullOwner", 10))
                    {
                        gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                    }
                    else if (nbttagcompound.hasKey("SkullOwner", 8))
                    {
                        String s = nbttagcompound.getString("SkullOwner");

                        if (!StringUtils.isNullOrEmpty(s))
                        {
                            gameprofile = TileEntitySkull.updateGameprofile(new GameProfile((UUID) null, s));
                            nbttagcompound.setTag("SkullOwner",
                                    NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
                        }
                    }
                }

                TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F,
                        itemstack.getMetadata(), gameprofile, -1, limbSwing);
            }
            else if (!item.isValidArmor(itemstack, EntityEquipmentSlot.HEAD, entitylivingbaseIn))
            {
                float f3 = 0.625F;
                GlStateManager.translate(0.0F, -0.25F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(f3, -f3, -f3);

                if (flag)
                {
                    GlStateManager.translate(0.0F, 0.1875F, 0.0F);
                }

                minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack,
                        ItemCameraTransforms.TransformType.HEAD);
            }
            else if (item.getUnlocalizedName(itemstack).replace("item.", "").equals("megahat"))
            {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);
                GL11.glTranslated(0, -0.25, 0);
                float s = 0.285f;
                GlStateManager.pushMatrix();
                GL11.glScaled(s, -s, -s);
                minecraft.renderEngine.bindTexture(belt_1);
                model.renderAll();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GL11.glScaled(s * 0.995f, -s * 0.995f, -s * 0.995f);
                minecraft.renderEngine.bindTexture(belt_2);
                EnumDyeColor ret = EnumDyeColor.RED;
                int brightness = entitylivingbaseIn.getBrightnessForRender();
                if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("dyeColour"))
                {
                    int damage = itemstack.getTagCompound().getInteger("dyeColour");
                    ret = EnumDyeColor.byDyeDamage(damage);
                }
                Color colour = new Color(ret.getColorValue() + 0xFF000000);
                int[] col = { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
                for (IExtendedModelPart part : model2.getParts().values())
                {
                    part.setRGBAB(col);
                }
                model2.renderAll();
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();
                GlStateManager.cullFace(GlStateManager.CullFace.BACK);
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }
    }

}
