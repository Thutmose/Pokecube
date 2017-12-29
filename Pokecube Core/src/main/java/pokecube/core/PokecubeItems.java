package pokecube.core;

import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import net.minecraft.util.ResourceLocation;
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
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemFossil;
import pokecube.core.items.ItemHeldItems;
import pokecube.core.items.ItemLuckyEgg;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.ItemTM;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.megastuff.ItemMegastone;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.items.revive.ItemRevive;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class PokecubeItems extends Items
{
    private static HashMap<String, ItemStack>        itemstacks     = new HashMap<String, ItemStack>();
    private static HashMap<String, Item>             items          = new HashMap<String, Item>();

    private static HashMap<String, Block>            blocks         = new HashMap<String, Block>();

    private static HashMap<ResourceLocation, Item[]> pokecubes      = new HashMap<ResourceLocation, Item[]>();

    /** Items that are allowed to be held by pokemobs */
    public static HashSet<ItemStack>                 heldItems      = new HashSet<ItemStack>();

    private static HashSet<Block>                    allBlocks      = new HashSet<Block>();

    /** Items which will be considered for evolution by pokemobs */
    public static HashSet<ItemStack>                 evoItems       = new HashSet<ItemStack>();

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

    public static Item                               held           = new ItemHeldItems()
            .setRegistryName(PokecubeMod.ID, "held");
    public static Item                               luckyEgg       = new ItemLuckyEgg().setUnlocalizedName("luckyegg")
            .setCreativeTab(creativeTabPokecube);
    public static Item                               pokemobEgg     = new ItemPokemobEgg()
            .setUnlocalizedName("pokemobegg");
    public static Item                               pokedex        = (new ItemPokedex()).setUnlocalizedName("pokedex");
    public static Item                               berryJuice;
    public static Item                               berries        = new ItemBerry()
            .setCreativeTab(PokecubeMod.creativeTabPokecubeBerries).setUnlocalizedName("berry")
            .setRegistryName(PokecubeMod.ID, "berry");
    public static Item                               megastone      = (new ItemMegastone())
            .setUnlocalizedName("megastone");
    public static Item                               megaring       = (new ItemMegawearable())
            .setUnlocalizedName("megaring");
    public static Item                               fossil         = new ItemFossil().setRegistryName(PokecubeMod.ID,
            "fossil");

    public static Item                               revive         = (new ItemRevive()).setUnlocalizedName("revive");
    public static Item                               tm             = (new ItemTM()).setUnlocalizedName("tm");
    public static Block                              pokecenter     = (new BlockHealTable())
            .setUnlocalizedName("pokecenter").setCreativeTab(creativeTabPokecubeBlocks);
    public static Block                              repelBlock     = new BlockRepel();
    public static Block                              tableBlock     = new BlockPokecubeTable();
    public static Block                              nest           = new BlockNest().setUnlocalizedName("pokemobnest");
    // .setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
    public static Block                              pc             = (new BlockPC()).setUnlocalizedName("pc");

    public static Block                              tradingtable   = (new BlockTradingTable())
            .setUnlocalizedName("tradingtable");
    public static Block                              fossilStone    = (new BlockFossilStone()).setHardness(3F)
            .setResistance(4F).setUnlocalizedName("fossilstone").setRegistryName(PokecubeMod.ID, "fossilstone");

    public static boolean                            resetTimeTags  = false;

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
        if (items.containsKey(name.toLowerCase(java.util.Locale.ENGLISH).trim())) return;

        if (item instanceof ItemStack)
        {
            itemstacks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), (ItemStack) item);
            Item i = ((ItemStack) item).getItem();
            items.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), i);
            Block b = Block.getBlockFromItem(i);
            if (b != null) blocks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), b);
        }
        if (item instanceof Item)
        {
            items.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), (Item) item);
            itemstacks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), new ItemStack((Item) item));
            if (Block.getBlockFromItem((Item) item) != null)
                blocks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), Block.getBlockFromItem((Item) item));
        }
        if (item instanceof Block)
        {
            blocks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), (Block) item);
            itemstacks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), new ItemStack((Block) item));
            items.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), Item.getItemFromBlock((Block) item));
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
        itemstacks.put(name.toLowerCase(java.util.Locale.ENGLISH).trim(), item);
    }

    public static void addToEvos(ItemStack stack)
    {
        if (!isValidHeldItem(stack)) addToHoldables(stack);
        if (CompatWrapper.isValid(stack) && !isValidEvoItem(stack)) evoItems.add(stack);
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
            OreDictionary.registerOre("pokemob_held", stack);
            heldItems.add(stack);
        }
    }

    public static void addToHoldables(String item)
    {
        ItemStack stack = getStack(item);
        addToHoldables(stack);
    }

    public static boolean contains(String name)
    {
        return getBlock(name.toLowerCase(java.util.Locale.ENGLISH).trim()) != null
                || getItem(name.toLowerCase(java.util.Locale.ENGLISH).trim()) != null
                || getStack(name.toLowerCase(java.util.Locale.ENGLISH).trim()) != null;
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

    public static HashSet<Block> getAllBlocks()
    {
        if (allBlocks.size() == 0)
        {
            initAllBlocks();
        }
        return allBlocks;
    }

    public static Block getBlock(String name)
    {
        return blocks.get(name.toLowerCase(java.util.Locale.ENGLISH).trim());
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

        return items.get(name.toLowerCase(java.util.Locale.ENGLISH).trim());
    }

    public static ItemStack getRandomMeteorDrop()
    {
        if (meteorDrops.size() == 0) return CompatWrapper.nullStack;
        Collections.shuffle(meteorDrops);
        return meteorDrops.get(0);
    }

    public static ItemStack getRandomSpawnerDrop()
    {
        if (spawnerDrops.size() == 0) return CompatWrapper.nullStack;
        Collections.shuffle(spawnerDrops);
        return spawnerDrops.get(0);
    }

    public static ItemStack getStack(String name)
    {
        return getStack(name, true);
    }

    public static ItemStack getStack(String name, boolean stacktrace)
    {
        if (name == null) return CompatWrapper.nullStack;
        name = name.toLowerCase(java.util.Locale.ENGLISH).trim();
        if (itemstacks.containsKey(name) && CompatWrapper.isValid(itemstacks.get(name)))
            return itemstacks.get(name).copy();

        String key = "";
        int n = 0;
        for (String s : itemstacks.keySet())
        {
            String[] args = s.split(":");
            if (args.length > 1)
            {
                if (args[1].equals(name))
                {
                    n++;
                    key = s;
                }
            }
        }
        if (n > 1)
        {
            System.err.println("Multiple instances of " + name + " Please specify with modid");
        }
        if (!key.isEmpty())
        {
            itemstacks.put(name, itemstacks.get(key));
            return itemstacks.get(name).copy();
        }
        System.err.println(name + " Not found in list of items.");
        if (stacktrace)
        {
            Thread.dumpStack();
        }
        return CompatWrapper.nullStack;
    }

    public static void init()
    {
        initVanillaHeldItems();

        for (int i = 0; i < Short.MAX_VALUE; i++)
        {
            Item item = Item.getItemById(i);
            if (item != null)
            {
                addGeneric(item.getRegistryName().toString(), item);
            }

        }
        grasses.clear();
        for (Block b : getAllBlocks())
        {
            if (grasses.contains(b)) continue;

            if (b.getDefaultState().getMaterial() == Material.GRASS) grasses.add(b);
            if (b.getDefaultState().getMaterial() == Blocks.RED_FLOWER.getDefaultState().getMaterial()) grasses.add(b);
            if (b.getDefaultState().getMaterial() == Blocks.TALLGRASS.getDefaultState().getMaterial())
                PokecubeItems.grasses.add(b);
            if (b.getDefaultState().getMaterial() == Blocks.WHEAT.getDefaultState().getMaterial())
                PokecubeItems.grasses.add(b);
        }
        postInitFossils();
    }

    public static void initAllBlocks()
    {
        allBlocks.clear();
        for (int i = 0; i < 4096; i++)
        {
            if (Block.getBlockById(i) != null) allBlocks.add(Block.getBlockById(i));
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
        boolean ret = false;
        if (!CompatWrapper.isValid(stack)) return false;
        for (ItemStack s : evoItems)
        {
            if (CompatWrapper.isValid(s) && Tools.isSameStack(s, stack)) return true;
        }
        return ret;
    }

    public static boolean isValidHeldItem(ItemStack stack)
    {
        boolean ret = false;
        if (!CompatWrapper.isValid(stack)) return false;
        boolean hasANull = false;
        for (ItemStack s : heldItems)
        {
            if (CompatWrapper.isValid(s) && Tools.isSameStack(s, stack)) return true;
            hasANull = hasANull || !CompatWrapper.isValid(s);
        }
        while (hasANull)
        {
            for (ItemStack s : heldItems)
            {
                hasANull = false;
                if (!CompatWrapper.isValid(s))
                {
                    heldItems.remove(s);
                    hasANull = true;
                    break;
                }
            }
        }
        hasANull = false;
        for (ItemStack s : evoItems)
        {
            if (CompatWrapper.isValid(s) && Tools.isSameStack(s, stack)) return true;
            hasANull = hasANull || !CompatWrapper.isValid(s);
        }
        while (hasANull)
        {
            for (ItemStack s : heldItems)
            {
                hasANull = false;
                if (!CompatWrapper.isValid(s))
                {
                    heldItems.remove(s);
                    hasANull = true;
                    break;
                }
            }
        }
        return ret;
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
        if (!CompatWrapper.isValid(candy)) return CompatWrapper.nullStack;

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

    private static void postInitFossils()
    {
        List<ItemStack> toRemove = Lists.newArrayList();
        for (String s : HeldItemHandler.fossilVariants)
        {
            ItemStack stack = new ItemStack(PokecubeItems.fossil);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("pokemon", s);
            PokecubeItems.addSpecificItemStack(s, stack);
            PokecubeItems.registerFossil(stack, s);
        }
        for (ItemStack s : fossils.keySet())
        {
            PokedexEntry num = fossils.get(s);
            if (!PokecubeMod.registered.get(num.getPokedexNb()))
            {
                toRemove.add(s);
                System.out.println("Should remove " + Database.getEntry(num.getPokedexNb()) + " " + s.getDisplayName());
            }
        }
        for (ItemStack s : toRemove)
        {
            fossils.remove(s);
        }
    }

    @SuppressWarnings("unchecked")
    public static void register(Object o, Object registry)
    {
        String name = null;
        if (o instanceof Item && registry instanceof IForgeRegistry<?>)
        {
            ((IForgeRegistry<Item>) registry).register((Item) o);
            name = ((Item) o).getRegistryName().getResourcePath();
        }
        else if (o instanceof Block && registry instanceof IForgeRegistry<?>)
        {
            ((IForgeRegistry<Block>) registry).register((Block) o);
            name = ((Block) o).getRegistryName().getResourcePath();
        }
        if (name != null) addGeneric(name, o);
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

    public static void removeFromEvos(String item)
    {
        ItemStack stack = getStack(item);
        evoItems.remove(stack);
    }

    public static void removeFromHoldables(String item)
    {
        ItemStack stack = getStack(item);
        heldItems.remove(stack);
    }

    public static void saveTime(NBTTagCompound nbt)
    {
        Long[] i = times.toArray(new Long[0]);

        int num = 0;
        if (nbt == null || i == null)
        {
            System.err.println("No Data");
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
