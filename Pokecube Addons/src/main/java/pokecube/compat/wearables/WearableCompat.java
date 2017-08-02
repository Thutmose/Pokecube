package pokecube.compat.wearables;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.bags.ItemBag;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class WearableCompat
{

    @Method(modid = "thut_wearables")
    @CompatClass(phase = Phase.PRE)
    public static void preInitWearables()
    {
        MinecraftForge.EVENT_BUS.register(new WearableCompat());
    }

    public WearableCompat()
    {
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getObject().getItem() instanceof ItemBag)
        {
            event.addCapability(new ResourceLocation("pokecube_adv:wearable"), new WearableBag());
        }
    }

    public static class WearableBag implements IActiveWearable, ICapabilityProvider
    {
        @Override
        public EnumWearable getSlot(ItemStack stack)
        {
            System.out.println(stack);
            return EnumWearable.BACK;
        }

        // One model for each layer.
        @SideOnly(Side.CLIENT)
        X3dModel                 bag1;
        @SideOnly(Side.CLIENT)
        X3dModel                 bag2;

        // One Texture for each layer.
        @SideOnly(Side.CLIENT)
        private ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.ID, "textures/worn/bag_1.png");
        @SideOnly(Side.CLIENT)
        private ResourceLocation BAG_2 = new ResourceLocation(PokecubeAdv.ID, "textures/worn/bag_2.png");

        @SideOnly(Side.CLIENT)
        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            if (bag1 == null)
            {
                bag1 = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/worn/bag.x3d"));
                bag2 = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/worn/bag.x3d"));
            }
            int brightness = wearer.getBrightnessForRender();
            ResourceLocation pass1 = BAG_1;
            ResourceLocation pass2 = BAG_2;
            GL11.glPushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // First pass of render
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslated(0, -0.125, -0.6);
            GL11.glScaled(0.7, 0.7, 0.7);
            Minecraft.getMinecraft().renderEngine.bindTexture(pass1);
            bag1.renderAll();
            GL11.glPopMatrix();

            // Second pass with colour.
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslated(0, -0.125, -0.6);
            GL11.glScaled(0.7, 0.7, 0.7);
            Minecraft.getMinecraft().renderEngine.bindTexture(pass2);
            EnumDyeColor ret = EnumDyeColor.YELLOW;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = stack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            Color colour = new Color(ret.getColorValue() + 0xFF000000);
            int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
            for (IExtendedModelPart part : bag2.getParts().values())
            {
                part.setRGBAB(col);
            }
            bag2.renderAll();
            GL11.glColor3f(1, 1, 1);
            GL11.glPopMatrix();

            GL11.glPopMatrix();
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == WEARABLE_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (WEARABLE_CAP != null && capability == WEARABLE_CAP) return (T) this;
            return null;
        }

        @Override
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }
    }
}
