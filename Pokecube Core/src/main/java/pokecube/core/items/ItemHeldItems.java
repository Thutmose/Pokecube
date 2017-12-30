package pokecube.core.items;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;
import pokecube.core.interfaces.PokecubeMod;

public class ItemHeldItems extends Item
{
    public static ArrayList<String> variants = Lists.newArrayList();

    public ItemHeldItems()
    {
        super();
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool)
    {
        if (stack.getItemDamage() < variants.size())
        {
            list.add(variants.get(stack.getItemDamage()));
        }
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        if (tab != getCreativeTab()) return;
        ItemStack stack;
        for (int i = 0; i < variants.size(); i++)
        {
            stack = new ItemStack(itemIn, 1, i);
            subItems.add(stack);
        }
        for (IInspectReward reward : PokedexInspector.rewards)
        {
            if (reward instanceof FreeTranslatedReward)
            {
                ItemStack book = ((FreeTranslatedReward) reward).getInfoBook(
                        Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
                subItems.add(book);
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        if (stack.getItemDamage() >= variants.size()) return super.getUnlocalizedName();
        String name = "item." + variants.get(stack.getItemDamage());
        return name;
    }
}
