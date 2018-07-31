package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.registerItemTexture;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BlockBerryLog;
import pokecube.core.blocks.berries.BlockBerryWood;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.ItemHeldItems;
import pokecube.core.items.ItemTM;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.utils.PokeType;

public class ItemGenerator
{
    public static interface IMoveModifier
    {
        void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held);
    }

    public static Map<Predicate<ItemStack>, IMoveModifier> ITEMMODIFIERS  = Maps.newHashMap();

    public static ArrayList<String>                        variants       = Lists.newArrayList();
    public static ArrayList<String>                        fossilVariants = new ArrayList<>();

    public static void makeHeldItems(Object registry)
    {
        for (String type : variants)
        {
            ItemHeldItems item = new ItemHeldItems(type);
            PokecubeItems.register(item, registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                registerItemTexture(item, 0, new ModelResourceLocation("pokecube:" + type, "inventory"));
            }
            ItemStack stack = new ItemStack(item, 1, 0);
            PokecubeItems.addToHoldables(stack);
        }
    }

    public static void makeFossils(Object registry)
    {
        for (String type : fossilVariants)
        {
            ItemFossil item = new ItemFossil(type);
            PokecubeItems.register(item, registry);
            PokecubeItems.registerFossil(new ItemStack(item), type);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                registerItemTexture(item, 0, new ModelResourceLocation("pokecube:fossil_" + type, "inventory"));
            }
        }
    }

    public static void makeMegaWearables(Object registry)
    {
        for (String type : ItemMegawearable.getWearables())
        {
            ItemMegawearable item = new ItemMegawearable(type, ItemMegawearable.getSlot(type));
            PokecubeItems.register(item, registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                registerItemTexture(item, 0, new ModelResourceLocation("pokecube:mega_" + type, "inventory"));
            }
        }
    }

    public static void makeTMs(Object registry)
    {
        for (PokeType type : PokeType.values())
        {
            Item tm = new ItemTM(type);
            PokecubeItems.register(tm, registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                registerItemTexture(tm, 0, new ModelResourceLocation("pokecube:" + "tm" + type.ordinal(), "inventory"));
            }
            ItemStack stack = new ItemStack(tm, 1, 0);
            PokecubeItems.addToHoldables(stack);
        }
    }

    static final String[]            names  = { "pecha", "oran", "leppa", "sitrus", "enigma", "nanab" };

    public static Map<String, Block> logs   = Maps.newHashMap();
    public static Map<String, Block> barks  = Maps.newHashMap();
    public static Map<String, Block> planks = Maps.newHashMap();

    public static void makeWoodBlocks(Object registry)
    {
        for (String s : names)
        {
            Block log = new BlockBerryLog("log_" + s);
            Block bark = new BlockBerryWood("bark_" + s);
            Block plank = new BlockBerryWood("plank_" + s);
            PokecubeItems.register(log, registry);
            PokecubeItems.register(bark, registry);
            PokecubeItems.register(plank, registry);
            logs.put(s, log);
            barks.put(s, bark);
            planks.put(s, plank);
        }
    }

    public static void makeWoodItems(Object registry)
    {
        for (String s : names)
        {
            ItemHandler.registerItemBlock(logs.get(s), registry);
            ItemHandler.registerItemBlock(barks.get(s), registry);
            ItemHandler.registerItemBlock(planks.get(s), registry);
        }
    }

    public static void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held)
    {
        for (Map.Entry<Predicate<ItemStack>, IMoveModifier> entry : ITEMMODIFIERS.entrySet())
        {
            if (entry.getKey().test(held))
            {
                entry.getValue().processHeldItemUse(moveUse, mob, held);
            }
        }
    }
}
