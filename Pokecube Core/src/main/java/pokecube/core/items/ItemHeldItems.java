package pokecube.core.items;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.rewards.XMLRewardsHandler.FreeBookParser.FreeTranslatedReward;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.PokedexInspector.IInspectReward;
import pokecube.core.interfaces.PokecubeMod;

public class ItemHeldItems extends Item
{
    public static ArrayList<String> variants = Lists.newArrayList();

    static
    {
        variants.add("waterstone");
        variants.add("firestone");
        variants.add("leafstone");
        variants.add("thunderstone");
        variants.add("moonstone");
        variants.add("sunstone");
        variants.add("shinystone");
        variants.add("ovalstone");
        variants.add("everstone");
        variants.add("duskstone");
        variants.add("dawnstone");
        variants.add("kingsrock");
        variants.add("dubiousdisc");
        variants.add("electirizer");
        variants.add("magmarizer");
        variants.add("reapercloth");
        variants.add("prismscale");
        variants.add("protector");
        variants.add("upgrade");
        variants.add("metalcoat");
    }

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
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("type"))
        {
            String s = stack.getTagCompound().getString("type");
            list.add(s);
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
        for (String s : variants)
        {
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
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
        String name = super.getUnlocalizedName();
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "???";
            if (tag != null)
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase(java.util.Locale.ENGLISH);
            }
            name = "item." + variant;
        }
        return name;
    }
}
