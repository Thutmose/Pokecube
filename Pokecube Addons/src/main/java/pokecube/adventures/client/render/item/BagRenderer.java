package pokecube.adventures.client.render.item;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.bags.ItemBag;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;

public class BagRenderer implements LayerRenderer<EntityPlayer>
{
    private static BagChecker         checker = new BagChecker(null);
    private final RenderLivingBase<?> renderer;
    X3dModel                          model;
    X3dModel                          model2;
    private ResourceLocation          BAG_1   = new ResourceLocation("pokecube_adventures:textures/Bag_1.png");
    private ResourceLocation          BAG_2   = new ResourceLocation("pokecube_adventures:textures/Bag_2.png");

    public static BagChecker getChecker()
    {
        return checker;
    }

    public BagRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        renderer = livingEntityRendererIn;
        model = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/item/Bag.x3d"));
        model2 = new X3dModel(new ResourceLocation(PokecubeAdv.ID, "models/item/Bag.x3d"));
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        boolean bag = getChecker().isWearingBag(player);
        if (bag)
        {
            int brightness = player.getBrightnessForRender(partialTicks);
            // First pass of render
            GL11.glPushMatrix();
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslated(0, -0.125, -0.6);
            GL11.glScaled(0.7, 0.7, 0.7);
            this.renderer.bindTexture(BAG_1);
            model.renderAll();
            GL11.glPopMatrix();
            // Second pass with colour.
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslated(0, -0.125, -0.6);
            GL11.glScaled(0.7, 0.7, 0.7);
            this.renderer.bindTexture(BAG_2);
            Color colour = new Color(
                    getChecker().getBagColour(getChecker().getBag(player)).getMapColor().colorValue + 0xFF000000);
            int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
            for (IExtendedModelPart part : model2.getParts().values())
            {
                part.setRGBAB(col);
            }
            model2.renderAll();
            GL11.glColor3f(1, 1, 1);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

    public static class BagChecker
    {
        final BagChecker defaults;

        public BagChecker(BagChecker defaults)
        {
            this.defaults = defaults;
        }

        protected boolean hasBag(EntityPlayer player)
        {
            ItemStack armour = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (armour != null) return armour.getItem() instanceof ItemBag
                    || (armour.hasTagCompound() && armour.getTagCompound().getBoolean("isapokebag"));
            return false;
        }

        public boolean isWearingBag(EntityPlayer player)
        {
            boolean ret;
            if (!(ret = hasBag(player)) && defaults != null) return defaults.isWearingBag(player);
            return ret;
        }

        public ItemStack getBag(EntityPlayer player)
        {
            ItemStack armour = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (armour != null)
            {
                if (armour.getItem() instanceof ItemBag) return armour;
                if (armour.hasTagCompound() && armour.getTagCompound().getBoolean("isapokebag")) return armour;
            }
            return null;
        }

        public EnumDyeColor getBagColour(ItemStack bag)
        {
            EnumDyeColor ret = EnumDyeColor.YELLOW;
            if (bag.hasTagCompound() && bag.getTagCompound().hasKey("dyeColour"))
            {
                int damage = bag.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            return ret;
        }
    }

    public static void setChecker(Class<? extends BagChecker> checkerIn)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException
    {
        BagChecker newchecker = checkerIn.getConstructor(BagChecker.class).newInstance(checker);
        checker = newchecker;
    }
}
