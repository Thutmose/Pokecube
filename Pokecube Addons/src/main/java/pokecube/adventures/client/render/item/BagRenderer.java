package pokecube.adventures.client.render.item;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import pokecube.adventures.client.models.items.ModelBag;
import pokecube.adventures.items.bags.ItemBag;

public class BagRenderer implements LayerRenderer<EntityPlayer>
{
    private static BagChecker checker = new BagChecker(null);

    public static BagChecker getChecker()
    {
        return checker;
    }

    public BagRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        boolean bag = checker.isWearingBag(player);
        if (bag)
        {
            ModelBag.model.render(0.5f);
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
    }

    public static void setChecker(Class<? extends BagChecker> checkerIn)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException
    {
        BagChecker newchecker = checkerIn.getConstructor(BagChecker.class).newInstance(checker);
        checker = newchecker;
    }
}
