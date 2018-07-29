package pokecube.core.items.vitamins;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.Item;
import pokecube.core.interfaces.PokecubeMod;

public class ItemVitamin extends Item
{
    public static List<String> vitamins = Lists.newArrayList();

    public final String        type;

    public ItemVitamin(String type)
    {
        super();
        this.type = type;
        this.setRegistryName(PokecubeMod.ID, "vitamin_" + type);
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setUnlocalizedName(this.getRegistryName().getResourcePath());
    }

}
