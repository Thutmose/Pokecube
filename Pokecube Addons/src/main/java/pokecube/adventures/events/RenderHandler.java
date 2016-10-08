package pokecube.adventures.events;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

@SideOnly(Side.CLIENT)
public class RenderHandler
{
    public static float partialTicks = 0.0F;

    public RenderHandler()
    {
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent evt)
    {
        EntityPlayer player = evt.getEntityPlayer();
        ItemStack stack = evt.getItemStack();
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().getBoolean("isapokebag"))
        {
            evt.getToolTip().add("PokeBag");
        }
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            String colour = I18n.format(
                    EnumDyeColor.byDyeDamage(stack.getTagCompound().getInteger("dyeColour")).getUnlocalizedName());
            boolean has = false;
            for (String s : evt.getToolTip())
            {
                has = s.equals(colour);
                if (has) break;
            }
            if (!has) evt.getToolTip().add(colour);
        }
        if (player == null || player.openContainer == null || stack == null) return;
        if (player.openContainer instanceof ContainerCloner && stack.getItem() instanceof ItemPokemobEgg)
        {
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("ivs"))
            {
                evt.getToolTip().add("" + stack.getTagCompound().getLong("ivs") + ":"
                        + stack.getTagCompound().getFloat("size") + ":" + stack.getTagCompound().getByte("nature"));
            }
        }
    }
}
