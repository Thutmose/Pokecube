package pokecube.modelloader.items;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.Evolution;
import pokecube.core.database.PokedexEntryLoader.Key;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.PokedexEntryLoader.StatsNode;
import pokecube.core.database.PokedexEntryLoader.XMLDatabase;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.modelloader.ModPokecubeML;
import thut.lib.CompatWrapper;

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
                            entry = (XMLPokedexEntry) PokedexEntryLoader.getSerializableCopy(XMLPokedexEntry.class,
                                    entry);
                            if (entry.stats != null && entry.stats.baseFriendship != null
                                    && entry.stats.baseFriendship == -1)
                                entry.stats.baseFriendship = null;
                            PokedexEntry pEntry = Database.getEntry(entry.name);
                            if (!pEntry.evolutions.isEmpty())
                            {
                                if (entry.stats == null) entry.stats = new StatsNode();
                                entry.stats.evolAnims = null;
                                entry.stats.evoModes = null;
                                entry.stats.evoTo = null;
                                if (entry.stats.evolutions == null || entry.stats.evolutions.isEmpty())
                                {
                                    entry.stats.evolutions = Lists.newArrayList();
                                    for (EvolutionData d : pEntry.evolutions)
                                    {
                                        Evolution evol = new Evolution();
                                        evol.clear = null;
                                        if (d.level > 0) evol.level = d.level;
                                        evol.name = d.evolution.getName();
                                        if (d.traded) evol.trade = true;
                                        if (d.happy) evol.happy = true;
                                        if (d.dayOnly) evol.time = "day";
                                        if (d.nightOnly) evol.time = "night";
                                        if (d.move != null && !d.move.isEmpty()) evol.move = d.move;
                                        if (d.FX != null && !d.FX.isEmpty() && !d.FX.equals("3") && !d.FX.equals("_"))
                                            evol.animation = d.FX;
                                        if (d.biome != null && !d.biome.isEmpty())
                                        {
                                            evol.location = new SpawnRule();

                                            String types = "";
                                            String blacklist = "";
                                            String biomes = "";

                                            String[] args = d.biome.split("`");
                                            for (String arg : args)
                                            {
                                                if (arg.startsWith("T"))
                                                {
                                                    if (!types.isEmpty()) types += ", ";
                                                    types += arg.substring(1);
                                                }
                                                else if (arg.startsWith("B"))
                                                {
                                                    if (!blacklist.isEmpty()) blacklist += ", ";
                                                    blacklist += arg.substring(1);
                                                }
                                                else
                                                {
                                                    if (!biomes.isEmpty()) biomes += ", ";
                                                    biomes += arg;
                                                }
                                            }

                                            if (!types.isEmpty()) evol.location.values.put(new QName("types"), types);
                                            if (!blacklist.isEmpty())
                                                evol.location.values.put(new QName("typesBlacklist"), blacklist);
                                            if (!biomes.isEmpty())
                                                evol.location.values.put(new QName("biomes"), biomes);
                                        }
                                        if (d.randomFactor != 1) evol.chance = d.randomFactor;
                                        if (d.item != null && CompatWrapper.isValid(d.item))
                                        {
                                            Item item = d.item.getItem();
                                            Key key = new Key();
                                            if (d.item.hasTagCompound())
                                            {
                                                key.values.put(new QName("tag"), d.item.getTagCompound().toString());
                                            }
                                            key.values.put(new QName("id"), item.getRegistryName().toString());
                                            if (d.item.getItemDamage() != 0)
                                                key.values.put(new QName("d"), d.item.getItemDamage() + "");
                                            evol.item = key;
                                        }
                                        entry.stats.evolutions.add(evol);
                                    }
                                }
                                else
                                {
                                    for (Evolution e : entry.stats.evolutions)
                                    {
                                        e.clear = null;
                                    }
                                }
                            }
                            if (entry.moves != null && entry.moves.lvlupMoves != null)
                            {
                                Map<QName, String> values = Maps.newHashMap();
                                for (QName key : entry.moves.lvlupMoves.values.keySet())
                                {
                                    values.put(new QName(key.toString().replace("lvl_", "")),
                                            entry.moves.lvlupMoves.values.get(key));
                                }
                                entry.moves.lvlupMoves.values = values;
                            }
                            newDatabase.pokemon.add(entry);
                        }
                    }
                    Collections.sort(newDatabase.pokemon, new Comparator<XMLPokedexEntry>()
                    {
                        @Override
                        public int compare(XMLPokedexEntry o1, XMLPokedexEntry o2)
                        {
                            int diff = o1.number - o2.number;
                            if (diff == 0)
                            {
                                boolean o1base = o1.base != null ? o1.base : false;
                                boolean o2base = o2.base != null ? o2.base : false;
                                if (o1base && !o2base) diff = -1;
                                else if (o2base && !o1base) diff = 1;
                            }
                            return diff;
                        }
                    });
                    String json = prettyGson.toJson(newDatabase);
                    FileWriter writer = new FileWriter(new File(file));
                    writer.append(json);
                    writer.close();

                    file = "./pokemobs.xml";
                    JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
                    Marshaller output = jaxbContext.createMarshaller();
                    writer = new FileWriter(new File(file));
                    output.marshal(newDatabase, writer);
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
