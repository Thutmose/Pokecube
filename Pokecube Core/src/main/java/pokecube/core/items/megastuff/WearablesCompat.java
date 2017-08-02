package pokecube.core.items.megastuff;

import java.awt.Color;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.models.ModelRing;
import pokecube.core.database.PokedexEntry;
import pokecube.core.items.megastuff.MegaCapability.RingChecker;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.lib.CompatWrapper;

public class WearablesCompat
{

    public WearablesCompat()
    {
        MegaCapability.checker = new RingChecker()
        {
            @Override
            public boolean canMegaEvolve(EntityPlayer player, PokedexEntry toEvolve)
            {
                Set<ItemStack> worn = thut.wearables.ThutWearables.getWearables(player).getWearables();
                for (ItemStack stack : worn)
                {
                    if (stack != null)
                    {
                        if (MegaCapability.matches(stack, toEvolve)) return true;
                    }
                }
                for (int i = 0; i < player.inventory.armorInventory.length; i++)
                {
                    ItemStack stack = player.inventory.armorInventory[i];
                    if (stack != null)
                    {
                        if (MegaCapability.matches(stack, toEvolve)) return true;
                    }
                }
                return false;
            }
        };
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<Item> event)
    {
        if (event.getObject() instanceof ItemMegawearable)
        {
            event.addCapability(new ResourceLocation("pokecube:wearable"), new WearableMega());
        }
    }

    public static class WearableMega implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        @Override
        public thut.wearables.EnumWearable getSlot(ItemStack stack)
        {
            String name = stack.getItem().getUnlocalizedName(stack).replace("item.", "");
            return thut.wearables.EnumWearable.valueOf(ItemMegawearable.wearables.get(name));
        }

        // 2 layers of belt rendering for the different colours.
        @SideOnly(Side.CLIENT)
        private X3dModel         beltl;
        @SideOnly(Side.CLIENT)
        private X3dModel         belt2;

        // 2 layers of hat rendering for the different colours.
        @SideOnly(Side.CLIENT)
        X3dModel                 hat1;
        @SideOnly(Side.CLIENT)
        X3dModel                 hat2;

        // rings use own model, so only 1 layer here, ring model handles own
        // textures.
        @SideOnly(Side.CLIENT)
        private ModelRing        ring   = new ModelRing();

        // Textures for each belt layer.
        @SideOnly(Side.CLIENT)
        private ResourceLocation belt_1 = new ResourceLocation(PokecubeCore.ID, "textures/worn/belt1.png");
        @SideOnly(Side.CLIENT)
        private ResourceLocation belt_2 = new ResourceLocation(PokecubeCore.ID, "textures/worn/belt2.png");

        // Textures for each hat layer.
        @SideOnly(Side.CLIENT)
        ResourceLocation         hat_1  = new ResourceLocation(PokecubeCore.ID, "textures/worn/hat.png");
        @SideOnly(Side.CLIENT)
        ResourceLocation         hat_2  = new ResourceLocation(PokecubeCore.ID, "textures/worn/hat2.png");

        @SideOnly(Side.CLIENT)
        @Override
        public void renderWearable(thut.wearables.EnumWearable slot, EntityLivingBase wearer, ItemStack stack,
                float partialTicks)
        {
            if (beltl == null)
            {
                beltl = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/belt.x3d"));
                belt2 = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/belt.x3d"));
                hat1 = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/hat.x3d"));
                hat2 = new X3dModel(new ResourceLocation(PokecubeCore.ID, "models/worn/hat.x3d"));
            }
            switch (slot)
            {
            case WAIST:
                int brightness = wearer.getBrightnessForRender(partialTicks);
                GL11.glPushMatrix();
                float dx = 0, dy = -.0f, dz = -0.6f;
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                float s = 0.525f;
                if (!CompatWrapper.isValid(wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS)))
                {
                    s = 0.465f;
                }
                GL11.glScalef(s, s, s);
                Minecraft.getMinecraft().renderEngine.bindTexture(belt_1);
                beltl.renderAll();
                GL11.glPopMatrix();
                // Second pass with colour.
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(s, s, s);
                Minecraft.getMinecraft().renderEngine.bindTexture(belt_2);
                EnumDyeColor ret = EnumDyeColor.GRAY;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
                {
                    int damage = stack.getTagCompound().getInteger("dyeColour");
                    ret = EnumDyeColor.byDyeDamage(damage);
                }
                Color colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
                int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
                for (IExtendedModelPart part : belt2.getParts().values())
                {
                    part.setRGBAB(col);
                }
                belt2.renderAll();
                GL11.glColor3f(1, 1, 1);
                GL11.glPopMatrix();
                break;
            case ANKLE:
                break;
            case BACK:
                break;
            case EAR:
                break;
            case EYE:
                break;
            case FINGER:
                ring.stack = stack;
                // TODO see if the rotation angles matter, if so, send them
                // too...
                ring.render(wearer, 0, 0, partialTicks, 0, 0, 0.0625f);
                break;
            case HAT:
                Minecraft minecraft = Minecraft.getMinecraft();
                GlStateManager.pushMatrix();
                s = 0.285f;
                GL11.glScaled(s, -s, -s);
                minecraft.renderEngine.bindTexture(hat_1);
                hat1.renderAll();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GL11.glScaled(s * 0.995f, -s * 0.995f, -s * 0.995f);
                minecraft.renderEngine.bindTexture(hat_2);
                ret = EnumDyeColor.RED;
                brightness = wearer.getBrightnessForRender(partialTicks);
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
                {
                    int damage = stack.getTagCompound().getInteger("dyeColour");
                    ret = EnumDyeColor.byDyeDamage(damage);
                }
                colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
                int[] col2 = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
                for (IExtendedModelPart part : hat2.getParts().values())
                {
                    part.setRGBAB(col2);
                }
                hat2.renderAll();
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();
                break;
            case NECK:
                break;
            case WRIST:
                break;
            default:
                break;
            }
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
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot,
                int subIndex)
        {
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot,
                int subIndex)
        {
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot,
                int subIndex)
        {
        }
    }
}
