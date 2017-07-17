package pokecube.modelloader.items;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.StatsNode;
import pokecube.core.database.PokedexEntryLoader.XMLDatabase;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.modelloader.ModPokecubeML;

public class ItemModelReloader extends Item
{
    public static ItemModelReloader instance;

    public ItemModelReloader()
    {
        super();
        instance = this;
    }

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (itemstack.getItemDamage() != 0)
        {
            if (!world.isRemote)
            {

                if (itemstack.getItemDamage() == 3)
                {

                    XMLPokedexEntry one = new XMLPokedexEntry();
                    one.name = "Test";
                    one.number = 5;
                    one.stats = new StatsNode();
                    one.stats.expMode = "testB";

                    XMLPokedexEntry two = new XMLPokedexEntry();
                    two.stats = new StatsNode();
                    two.stats.hatedMaterials = "testA";
                    two.gender = "derp";

                    PokedexEntryLoader.mergeNonDefaults(new XMLPokedexEntry(), one, two);
                    System.out.println(two.toString() + " " + two.stats.hatedMaterials + " " + two.stats.expMode);

                    return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
                }

                try
                {
                    // Gson gson = new Gson();
                    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
                    XMLDatabase newDatabase = new XMLDatabase();
                    List<XMLPokedexEntry> list = Lists.newArrayList(PokedexEntryLoader.database.pokemon);
                    String file = "./pokemobs.json";
                    if (itemstack.getItemDamage() == 1)
                    {
                        for (XMLPokedexEntry entry : list)
                        {
                            XMLPokedexEntry copy = new XMLPokedexEntry();
                            copy.name = entry.name;
                            copy.number = entry.number;
                            newDatabase.pokemon.add((XMLPokedexEntry) PokedexEntryLoader
                                    .getSerializableCopy(XMLPokedexEntry.class, copy));
                            file = "./pokemobs_blank.json";
                        }
                    }
                    else
                    {
                        for (XMLPokedexEntry entry : list)
                        {
                            newDatabase.pokemon.add((XMLPokedexEntry) PokedexEntryLoader
                                    .getSerializableCopy(XMLPokedexEntry.class, entry));
                        }
                    }
                    String json = prettyGson.toJson(newDatabase);
                    FileWriter writer = new FileWriter(new File(file));
                    writer.append(json);
                    writer.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        }
        if (!world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        player.openGui(ModPokecubeML.instance, 0, player.getEntityWorld(), 0, 0, 0);
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
    }
}
