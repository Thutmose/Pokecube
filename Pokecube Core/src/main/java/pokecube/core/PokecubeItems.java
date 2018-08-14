package pokecube.core;

import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.blocks.fossil.BlockFossilStone;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.blocks.nests.BlockNest;
import pokecube.core.blocks.pc.BlockPC;
import pokecube.core.blocks.pokecubeTable.BlockPokecubeTable;
import pokecube.core.blocks.repel.BlockRepel;
import pokecube.core.blocks.tradingTable.BlockTradingTable;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemLuckyEgg;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.loot.functions.MakeBerry;
import pokecube.core.items.loot.functions.MakeFossil;
import pokecube.core.items.loot.functions.MakeHeldItem;
import pokecube.core.items.loot.functions.MakeMegastone;
import pokecube.core.items.loot.functions.MakeVitamin;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.items.revive.ItemRevive;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class PokecubeItems extends Items
{
    private static Set<String>                       items          = Sets.newHashSet();

    private static HashMap<ResourceLocation, Item[]> pokecubes      = new HashMap<ResourceLocation, Item[]>();

    /** Items that are allowed to be held by pokemobs */
    public static HashSet<ItemStack>                 heldItems      = new HashSet<ItemStack>();

    /** contains pokecubes that should be rendered using the default renderer */
    private static Set<ResourceLocation>             cubeIds        = new HashSet<>();

    /** Items to be considered for re-animation, mapped to the pokedex number to
     * reanimate to. */
    public static HashMap<ItemStack, PokedexEntry>   fossils        = new HashMap<ItemStack, PokedexEntry>();
    /** Various meteor related drops, the map is the rate of each drop */
    private static List<ItemStack>                   meteorDrops    = new ArrayList<ItemStack>();

    public static HashMap<ItemStack, Integer>        meteorDropMap  = new HashMap<ItemStack, Integer>();
    /** to be used if mob spawners are to get a replacement. */
    private static List<ItemStack>                   spawnerDrops   = new ArrayList<ItemStack>();

    public static HashMap<ItemStack, Integer>        spawnerDropMap = new HashMap<ItemStack, Integer>();

    public static Vector<Long>                       times          = new Vector<Long>();

    /** List of grass blocks for pokemobs to eat. */
    public static HashSet<Block>                     grasses        = new HashSet<Block>();

    public static Item                               luckyEgg       = new ItemLuckyEgg().setUnlocalizedName("luckyegg")
            .setCreativeTab(creativeTabPokecube);
    public static Item                               pokemobEgg     = new ItemPokemobEgg()
            .setRegistryName(PokecubeMod.ID, "pokemobegg").setUnlocalizedName("pokemobegg");
    public static Item                               pokedex        = (new ItemPokedex(false, true));
    public static Item                               pokewatch      = (new ItemPokedex(true, true));
    public static Item                               berryJuice;
    public static Item                               nullberry      = new ItemBerry("null", 0, 0, 0, 0, 0, 0, null);

    public static Item                               revive         = (new ItemRevive()).setUnlocalizedName("revive");
    public static Block                              pokecenter     = (new BlockHealTable())
            .setUnlocalizedName("pokecenter").setCreativeTab(creativeTabPokecubeBlocks);
    public static Block                              repelBlock     = new BlockRepel();
    public static Block                              tableBlock     = new BlockPokecubeTable();
    public static Block                              nest           = new BlockNest().setUnlocalizedName("pokemobnest");
    // .setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
    public static Block                              pc_top         = (new BlockPC(true));
    public static Block                              pc_base        = (new BlockPC(false));

    public static Block                              trading_table  = (new BlockTradingTable(true));
    public static Block                              tm_machine     = (new BlockTradingTable(false));
    public static Block                              fossilStone    = (new BlockFossilStone()).setHardness(3F)
            .setResistance(4F).setUnlocalizedName("fossilstone").setRegistryName(PokecubeMod.ID, "fossilstone");

    public static boolean                            resetTimeTags  = false;

    static
    {
        LootFunctionManager.registerFunction(new MakeBerry.Serializer());
        LootFunctionManager.registerFunction(new MakeMegastone.Serializer());
        LootFunctionManager.registerFunction(new MakeHeldItem.Serializer());
        LootFunctionManager.registerFunction(new MakeFossil.Serializer());
        LootFunctionManager.registerFunction(new MakeVitamin.Serializer());
    }

    /** Registers a pokecube id, the Object[] is an array with the item or block
     * assosicated with the unfilled and filled cubes. example: Object cubes =
     * new Object[] { pokecube, pokecubeFilled}; where pokecube is the unfilled
     * pokecube block, and pokecubeFilled is the filled one. defaults are: 0 -
     * pokecube 1 - greatcube 2 - ultracube 3 - mastercube
     * 
     * @param id
     * @param cubes */
    public static void addCube(ResourceLocation id, Object[] cubes)
    {
        addCube(id, cubes, true);
    }

    /** Registers a pokecube id, the Object[] is an array with the item or block
     * assosicated with the unfilled and filled cubes. example: Object cubes =
     * new Object[] { pokecube, pokecubeFilled}; where pokecube is the unfilled
     * pokecube block, and pokecubeFilled is the filled one. defaults are: 0 -
     * pokecube 1 - greatcube 2 - ultracube 3 - mastercube
     * 
     * @param id
     * @param cubes */
    public static void addCube(ResourceLocation id, Object[] cubes, boolean defaultRenderer)
    {
        if (pokecubes.containsKey(id))
        {
            System.err.println("Pokecube Id " + id + " Has already been registered as " + getEmptyCube(id));
        }

        if (cubes.length == 1)
        {
            cubes = new Object[] { cubes[0], cubes[0] };
        }

        Item[] items = new Item[2];
        if (cubes[0] instanceof Item) items[0] = (Item) cubes[0];
        else if (cubes[0] instanceof Block) items[0] = Item.getItemFromBlock((Block) cubes[0]);
        if (cubes[1] instanceof Item) items[1] = (Item) cubes[1];
        else if (cubes[1] instanceof Block) items[1] = Item.getItemFromBlock((Block) cubes[1]);

        addSpecificItemStack(id.getResourcePath() + "cube", new ItemStack(items[0]));
        addSpecificItemStack(id.getResourcePath() + "cube", new ItemStack(items[1], 1, OreDictionary.WILDCARD_VALUE));

        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(items[0], new DispenserBehaviorPokecube());
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(items[1], new DispenserBehaviorPokecube());

        if (defaultRenderer) cubeIds.add(id);

        pokecubes.put(id, items);
    }

    /** Used for generic adding of itemstacks, items or blocks. if inputting an
     * itemstack, it adds the corresponding item and block to the other maps. if
     * inputting block or item, it adds a stack of size 1, meta 0 to the
     * itemstack map.
     * 
     * @param name
     * @param item */
    public static void addGeneric(String name, Object item)
    {
        name = name.toLowerCase(java.util.Locale.ENGLISH).trim();
        if (items.contains(name)) return;

        items.add(name);
        if (item instanceof ItemStack)
        {
            OreDictionary.registerOre(name, ((ItemStack) item).copy());
        }
        if (item instanceof Item)
        {
            OreDictionary.registerOre(name, (Item) item);
        }
        if (item instanceof Block)
        {
            OreDictionary.registerOre(name, (Block) item);
        }
    }

    public static void addMeteorDrop(ItemStack stack, int weight)
    {
        meteorDropMap.put(stack, weight);
        for (int i = 0; i < weight; i++)
            meteorDrops.add(stack);
    }

    public static void addSpawnerDrop(ItemStack stack, int weight)
    {
        spawnerDropMap.put(stack, weight);
        for (int i = 0; i < weight; i++)
            spawnerDrops.add(stack);
    }

    /** Use this for a specific itemstack, make sure the name differs from other
     * names of itemstacks added. this one only adds to the itemstack list, use
     * addGeneric to add to all valid lists.
     * 
     * @param name
     * @param item */
    public static void addSpecificItemStack(String name, ItemStack item)
    {
        OreDictionary.registerOre(name.toLowerCase(java.util.Locale.ENGLISH).trim(), item.copy());
    }

    public static void addToEvos(ItemStack stack)
    {
        if (!isValidHeldItem(stack)) addToHoldables(stack);
        if (CompatWrapper.isValid(stack) && !isValidEvoItem(stack))
            OreDictionary.registerOre("pokemob_evo", stack.copy());
    }

    public static void addToEvos(String item)
    {
        ItemStack stack = getStack(item);
        addToEvos(stack);
    }

    public static void addToHoldables(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack))
            System.out.println(new NullPointerException("Cannot add null stack to holdables " + stack));
        else
        {
            OreDictionary.registerOre("pokemob_held", stack.copy());
            heldItems.clear();
            heldItems.addAll(OreDictionary.getOres("pokemob_held"));
        }
    }

    public static void addToHoldables(String item)
    {
        ItemStack stack = getStack(item);
        addToHoldables(stack);
    }

    public static void deValidate(ItemStack stack)
    {
        if (stack.hasTagCompound())
        {
            long time = stack.getTagCompound().getLong("time");
            times.remove(time);
            stack.setTagCompound(null);
            stack.splitStack(1);
        }
    }

    public static Block getBlock(String name)
    {
        Item item = getItem(name);
        return item == null ? null : Block.getBlockFromItem(item);
    }

    public static ResourceLocation getCubeId(Block item)
    {
        return getCubeId(new ItemStack(item));
    }

    public static ResourceLocation getCubeId(Item item)
    {
        return getCubeId(new ItemStack(item));
    }

    /** defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you pass in a non- pokecube stack, it returns 0, defaults to a pokecube.
     * 
     * @param stack
     * @return */
    public static ResourceLocation getCubeId(ItemStack stack)
    {
        if (CompatWrapper.isValid(stack)) for (ResourceLocation i : pokecubes.keySet())
        {
            Item[] cubes = pokecubes.get(i);
            for (Item cube : cubes)
                if (cube == stack.getItem()) return i;
        }
        return null;
    }

    /** defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you request a non-registerd id, it returns pokecube. */
    public static Item getEmptyCube(ResourceLocation id)
    {
        Item ret = null;

        if (pokecubes.containsKey(id)) ret = pokecubes.get(id)[0];

        if (ret == null)
        {
            try
            {
                ret = pokecubes.get(PokecubeBehavior.DEFAULTCUBE)[0];
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.SEVERE, "No Cubes Registered!", e);
                return Items.STONE_HOE;
            }
        }

        return ret;
    }

    public static Item getEmptyCube(ItemStack stack)
    {
        return getEmptyCube(getCubeId(stack));
    }

    /** defaults are: 0 - pokecube 1 - greatcube 2 - ultracube 3 - mastercube if
     * you request a non-registerd id, it returns pokecube. */
    public static Item getFilledCube(ResourceLocation id)
    {
        Item ret = null;

        if (pokecubes.containsKey(id)) ret = pokecubes.get(id)[1];

        if (ret == null)
        {
            ret = pokecubes.get(PokecubeBehavior.DEFAULTCUBE)[1];
            if (id != null) System.err.println("Could not find filled cube for id " + id);
        }

        return ret;
    }

    public static Item getFilledCube(ItemStack stack)
    {
        return getFilledCube(getCubeId(stack));
    }

    public static PokedexEntry getFossilEntry(ItemStack fossil)
    {
        if (!CompatWrapper.isValid(fossil)) return null;
        PokedexEntry ret = null;
        for (ItemStack s : fossils.keySet())
        {
            if (Tools.isSameStack(fossil, s))
            {
                ret = fossils.get(s);
                break;
            }
        }
        return ret;
    }

    public static Item getItem(String name)
    {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
        if (item != null) return item;
        name = name.toLowerCase(Locale.ENGLISH).trim();
        NonNullList<ItemStack> stack = OreDictionary.getOres(name);
        if (stack.size() > 0) item = stack.get(0).getItem();
        if (item == null)
        {
            for (ResourceLocation l : Item.REGISTRY.getKeys())
            {
                if (l.getResourcePath().equals(name))
                {
                    item = Item.REGISTRY.getObject(l);
                    addSpecificItemStack(name, new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
                    return item;
                }
            }
        }
        return item;
    }

    public static ItemStack getRandomMeteorDrop()
    {
        if (meteorDrops.size() == 0) return ItemStack.EMPTY;
        return meteorDrops.get(new Random().nextInt(meteorDrops.size())).copy();
    }

    public static ItemStack getRandomSpawnerDrop()
    {
        if (spawnerDrops.size() == 0) return ItemStack.EMPTY;
        return spawnerDrops.get(new Random().nextInt(spawnerDrops.size())).copy();
    }

    public static ItemStack getStack(String name)
    {
        return getStack(name, true);
    }

    public static ItemStack getStack(String name, boolean stacktrace)
    {
        if (name == null) return ItemStack.EMPTY;
        NonNullList<ItemStack> stacks = OreDictionary.getOres(name);
        if (!stacks.isEmpty()) return stacks.get(0).copy();
        Item item = getItem(name);
        if (item != null) { return new ItemStack(item); }
        if (PokecubeMod.debug && stacktrace)
        {
            PokecubeMod.log(Level.WARNING, name + " Not found in list of items.", new NullPointerException());
        }
        return ItemStack.EMPTY;
    }

    public static void init()
    {
        initVanillaHeldItems();
        grasses.clear();
        Iterator<Block> iter = Block.REGISTRY.iterator();
        while (iter.hasNext())
        {
            Block b = iter.next();
            if (grasses.contains(b)) continue;
            if (b.getDefaultState().getMaterial() == Material.GRASS) grasses.add(b);
            if (b.getDefaultState().getMaterial() == Blocks.RED_FLOWER.getDefaultState().getMaterial()) grasses.add(b);
            if (b.getDefaultState().getMaterial() == Blocks.TALLGRASS.getDefaultState().getMaterial())
                PokecubeItems.grasses.add(b);
            if (b.getDefaultState().getMaterial() == Blocks.WHEAT.getDefaultState().getMaterial())
                PokecubeItems.grasses.add(b);
        }
    }

    private static void initVanillaHeldItems()
    {
        addGeneric("ice", Blocks.PACKED_ICE);
        addGeneric("mossStone", Blocks.MOSSY_COBBLESTONE);
        addGeneric("icestone", Blocks.PACKED_ICE);

        addGeneric("razorfang", Items.IRON_PICKAXE);
        addGeneric("razorclaw", Items.IRON_AXE);

        addGeneric("dragonscale", Items.EMERALD);
        addGeneric("deepseascale", Items.FEATHER);
        addGeneric("deepseatooth", Items.FLINT);

        addToEvos("ice");
        addToEvos("mossStone");
        addToEvos("razorfang");
        addToEvos("razorclaw");
        addToEvos("dragonscale");
        addToEvos("deepseascale");
        addToEvos("deepseatooth");
        addToEvos("icestone");
    }

    public static boolean isValid(ItemStack stack)
    {
        if (stack.hasTagCompound())
        {
            long time = stack.getTagCompound().getLong("time");
            return times.contains(time);
        }
        return false;
    }

    public static boolean isValidEvoItem(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack)) return false;
        return Tools.isStack(stack, "pokemob_evo");
    }

    public static boolean isValidHeldItem(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack)) return false;
        if (stack.hasCapability(IPokemobUseable.USABLEITEM_CAP, null)) return true;
        return Tools.isStack(stack, "pokemob_held");
    }

    public static void loadTime(NBTTagCompound nbt)
    {
        if (resetTimeTags)
        {
            resetTimeTags = false;
            return;
        }
        times.clear();
        int num = nbt.getInteger("count");
        for (int i = 0; i < num; i++)
        {
            if (Long.valueOf(nbt.getLong("" + i)) != 0) times.add(Long.valueOf(nbt.getLong("" + i)));
        }
    }

    public static ItemStack makeCandyStack()
    {
        ItemStack candy = PokecubeItems.getStack("rarecandy");
        if (!CompatWrapper.isValid(candy)) return ItemStack.EMPTY;
        makeStackValid(candy);
        candy.setStackDisplayName("Rare Candy");
        return candy;
    }

    public static void makeStackValid(ItemStack stack)
    {
        long time = System.nanoTime();
        if (isValid(stack)) deValidate(stack);
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        times.add(time);
        stack.getTagCompound().setLong("time", time);
    }

    @SuppressWarnings("unchecked")
    public static void register(Object o, Object registry)
    {
        if (o instanceof Item && registry instanceof IForgeRegistry<?>)
        {
            ((IForgeRegistry<Item>) registry).register((Item) o);
        }
        else if (o instanceof Block && registry instanceof IForgeRegistry<?>)
        {
            ((IForgeRegistry<Block>) registry).register((Block) o);
        }
    }

    public static void registerFossil(ItemStack fossil, int number)
    {
        fossils.put(fossil.copy(), Database.getEntry(number));
    }

    public static void registerFossil(ItemStack fossil, String pokemonName)
    {
        if (Database.entryExists(pokemonName))
        {
            fossils.put(fossil.copy(), Database.getEntry(pokemonName));
        }
    }

    public static void registerItemTexture(Item item, int meta, ModelResourceLocation loc)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, loc);
    }

    public static void saveTime(NBTTagCompound nbt)
    {
        Long[] i = times.toArray(new Long[0]);

        int num = 0;
        if (nbt == null || i == null)
        {
            PokecubeMod.log(Level.WARNING, "No Data to save for Item Validations.");
            return;
        }
        for (Long l : i)
        {
            if (l != null)
            {
                nbt.setLong("" + num, l.longValue());
                num++;
            }
        }
        nbt.setInteger("count", num);
    }

    public static Predicate<IBlockState> getState(String arguments)
    {
        String[] args = arguments.split(" ");

        String[] resource = args[0].split(":");
        final String modid = resource[0];
        final String blockName = resource[1];
        String keyTemp = null;
        String valTemp = null;

        if (args.length > 1)
        {
            String[] state = args[1].split("=");
            keyTemp = state[0];
            valTemp = state[1];
        }
        final String key = keyTemp;
        final String val = valTemp;
        return new Predicate<IBlockState>()
        {
            final Pattern                  modidPattern = Pattern.compile(modid);
            final Pattern                  blockPattern = Pattern.compile(blockName);
            Map<ResourceLocation, Boolean> checks       = Maps.newHashMap();

            @Override
            public boolean apply(IBlockState input)
            {
                if (input == null || input.getBlock() == null) return false;
                Block block = input.getBlock();
                ResourceLocation name = block.getRegistryName();
                if (checks.containsKey(name) && !checks.get(name)) return false;
                else if (!checks.containsKey(name))
                {
                    if (!modidPattern.matcher(name.getResourceDomain()).matches())
                    {
                        checks.put(name, false);
                        return false;
                    }
                    if (!blockPattern.matcher(name.getResourcePath()).matches())
                    {
                        checks.put(name, false);
                        return false;
                    }
                    checks.put(name, true);
                }
                if (key == null) return true;
                for (IProperty<?> prop : input.getPropertyKeys())
                {
                    if (prop.getName().equals(key))
                    {
                        Object inputVal = input.getValue(prop);
                        return inputVal.toString().equalsIgnoreCase(val);
                    }
                }
                return false;
            }
        };
    }
}
