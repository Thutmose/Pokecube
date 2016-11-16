/**
 *
 */
package pokecube.core.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntryLoader.Action;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.PokedexEntryLoader.Interact;
import pokecube.core.database.PokedexEntryLoader.Key;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatWrapper;

/** @author Manchou */
public class PokedexEntry
{
    // Annotation used to specify which fields should be shared to all gender
    // formes.
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CopyToGender
    {
    }

    public static class EvolutionData
    {
        public String             biome        = "";
        public String             data;
        public boolean            dayOnly      = false;
        public final PokedexEntry evolution;
        public String             FX           = "";
        // 1 for male, 2 for female, 0 for either;
        public byte               gender       = 0;
        public boolean            happy        = false;
        // the item it must be holding, if null, any item is fine, or no items
        // is fine
        public ItemStack          item         = CompatWrapper.nullStack;
        // does it need to grow a level for the item to work
        public boolean            itemLevel    = false;
        public int                level        = -1;
        public String             move         = "";
        public boolean            nightOnly    = false;
        public PokedexEntry       preEvolution;
        public boolean            rainOnly     = false;
        public float              randomFactor = 1.0f;
        public boolean            traded       = false;

        private EvolutionData(PokedexEntry evol)
        {
            evolution = evol;
        }

        public EvolutionData(PokedexEntry evol, String data, String FX)
        {
            this(evol);
            this.FX = FX;
            this.data = data;
        }

        private boolean checkNormal(IPokemob mob, String biome)
        {
            int type = -1;
            for (BiomeType b : BiomeType.values())
            {
                if (b.name.replaceAll(" ", "").equalsIgnoreCase(biome)) type = b.getType() + 256;
            }
            if (type == -1)
            {
                for (ResourceLocation key : Biome.REGISTRY.getKeys())
                {
                    Biome b = Biome.REGISTRY.getObject(key);
                    if (b != null)
                    {
                        if (b.getBiomeName().replaceAll(" ", "").equalsIgnoreCase(biome))
                        {
                            type = Biome.getIdForBiome(b);
                        }
                    }
                }
            }
            Vector3 v = Vector3.getNewVector().set(mob);
            World world = ((EntityLiving) mob).getEntityWorld();
            if (type == -1)
            {
                Biome b = v.getBiome(world);
                for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                {
                    if (t.toString().equalsIgnoreCase(biome)) { return BiomeDictionary.isBiomeOfType(b, t); }
                }
            }
            else
            {
                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity((Entity) mob);
                int tb = t.getBiome(v);
                int vb = v.getBiomeID(world);
                if (tb == type || vb == type) return true;
            }
            return false;
        }

        private boolean checkPerType(IPokemob mob, String biome)
        {
            String[] args = biome.split("\'");
            List<BiomeDictionary.Type> neededTypes = Lists.newArrayList();
            List<BiomeDictionary.Type> bannedTypes = Lists.newArrayList();
            for (String s : args)
            {
                String name = s.substring(1);
                if (s.startsWith("B"))
                {
                    for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                    {
                        if (t.toString().equalsIgnoreCase(name))
                        {
                            bannedTypes.add(t);
                        }
                    }
                }
                else if (s.startsWith("W"))
                {
                    for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                    {
                        if (t.toString().equalsIgnoreCase(name))
                        {
                            neededTypes.add(t);
                        }
                    }
                }
            }
            Vector3 v = Vector3.getNewVector().set(mob);
            World world = ((EntityLiving) mob).getEntityWorld();
            Biome b = v.getBiome(world);
            boolean correctType = true;
            boolean bannedType = false;
            for (BiomeDictionary.Type t : neededTypes)
            {
                correctType = correctType && BiomeDictionary.isBiomeOfType(b, t);
            }
            for (BiomeDictionary.Type t : bannedTypes)
            {
                bannedType = bannedType || BiomeDictionary.isBiomeOfType(b, t);
            }
            return correctType && !bannedType;
        }

        public Entity getEvolution(World world)
        {
            if (evolution == null) return null;
            Entity ret = PokecubeMod.core.createPokemob(evolution, world);
            return ret;
        }

        public boolean isInBiome(IPokemob mob)
        {
            String[] biomes = biome.split(",");
            for (String biome : biomes)
            {
                if (biome.startsWith("T"))
                {
                    if (checkPerType(mob, biome.substring(1))) return true;
                }
                else if (checkNormal(mob, biome)) return true;
            }
            return false;
        }

        private void parse(String data)
        {
            String[] parts = data.split(":");
            String itemName = "";

            for (String s : parts)
            {
                String arg1 = s.substring(0, 1);
                String arg2 = "";
                if (s.length() > 1)
                {
                    arg2 = s.substring(1);
                }
                if (arg1.equals("L"))
                {
                    try
                    {
                        level = Integer.parseInt(arg2);
                    }
                    catch (NumberFormatException e)
                    {
                        if (level == -1) this.level = 0;
                        itemName = arg2;
                    }
                }
                else if (arg1.equals("I"))
                {
                    itemName = arg2;
                    if (level == -1) this.level = 0;
                }
                else if (arg1.equals("T"))
                {
                    if (arg2.equalsIgnoreCase("day")) dayOnly = true;
                    if (arg2.equalsIgnoreCase("night")) nightOnly = true;
                }
                else if (arg1.equals("H"))
                {
                    happy = true;
                    if (level == -1) this.level = 0;
                }
                else if (arg1.equals("X"))
                {
                    traded = true;
                    if (level == -1) this.level = 0;
                }
                else if (arg1.equals("G"))
                {
                    if (arg2.equalsIgnoreCase("male")) gender = 1;
                    if (arg2.equalsIgnoreCase("female")) gender = 2;
                }
                else if (arg1.equals("R"))
                {
                    itemLevel = true;
                    if (level == -1) this.level = 0;
                }
                else if (arg1.equals("M"))
                {
                    move = arg2;
                    if (level == -1) this.level = 0;
                }
                else if (arg1.equals("B"))
                {
                    biome = arg2;
                    if (level == -1) this.level = 0;
                }
                else if ((arg1 + arg2).equals("rain"))
                {
                    rainOnly = true;
                    if (level == -1) this.level = 0;
                }
                else if (arg1.equals("P"))
                {
                    this.randomFactor = Float.parseFloat(arg2);
                }
                else
                {
                    System.out.println(data);
                    Thread.dumpStack();
                }
            }
            if (!itemName.isEmpty())
            {
                item = PokecubeItems.getStack(itemName);
            }
            if (item != CompatWrapper.nullStack)
            {
                PokecubeItems.addToHoldables(itemName);
                PokecubeItems.addToEvos(itemName);
            }
        }

        protected void postInit()
        {
            try
            {
                if (data != null) parse(data);
            }
            catch (Exception e)
            {
                System.out.println(this);
                e.printStackTrace();
            }
            data = null;
        }

        public boolean shouldEvolve(IPokemob mob)
        {
            return shouldEvolve(mob, ((EntityLiving) mob).getHeldItemMainhand());
        }

        public boolean shouldEvolve(IPokemob mob, ItemStack mobs)
        {
            if (this.level < 0) return false;
            boolean ret = mob.traded() == this.traded || !this.traded;
            Random rand = new Random(mob.getRNGValue());
            if (rand.nextFloat() > randomFactor) return false;
            if (rainOnly)
            {
                World world = ((EntityLiving) mob).getEntityWorld();
                boolean rain = world.isRaining();
                if (!rain)
                {
                    TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity((Entity) mob);
                    PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
                    if (teffect != null)
                    {
                        rain = teffect.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] > 0;
                    }
                }
                if (!rain) return false;
            }
            boolean correctItem = item == null;
            if (item != CompatWrapper.nullStack && mob instanceof EntityLiving)
            {
                if (mobs != CompatWrapper.nullStack)
                {
                    correctItem = Tools.isSameStack(mobs, item);
                }
            }
            if (mob instanceof EntityLiving && Tools.isSameStack(((EntityLiving) mob).getHeldItemMainhand(),
                    PokecubeItems.getStack("everstone"))) { return false; }
            if (Tools.isSameStack(mobs, PokecubeItems.getStack("everstone"))) { return false; }
            ret = ret && correctItem;
            boolean correctLevel = mob.getLevel() >= level;
            ret = ret && correctLevel;
            boolean rightGender = gender == 0;
            if (!rightGender)
            {
                rightGender = mob.getSexe() == gender;
            }
            ret = ret && rightGender;
            boolean rightMove = move.equals("");
            if (!rightMove)
            {
                String[] moves = mob.getMoves();
                for (String s : moves)
                {
                    if (s != null) if (s.equalsIgnoreCase(move))
                    {
                        rightMove = true;
                        break;
                    }
                }
            }
            ret = ret && rightMove;
            boolean rightTime = dayOnly == nightOnly;
            if (!rightTime && mob instanceof Entity)
            {
                Entity poke = (Entity) mob;
                rightTime = dayOnly ? poke.getEntityWorld().isDaytime() : !poke.getEntityWorld().isDaytime();
            }
            ret = ret && rightTime;
            if (happy)
            {
                ret = ret && mob.getHappiness() >= 220;
            }
            if (ret && !biome.isEmpty())
            {
                ret = ret && isInBiome(mob);
            }
            return ret;
        }
    }

    public static class InteractionLogic
    {
        static HashMap<PokeType, List<Interact>> defaults = new HashMap<>();

        static
        {
            Interact fire = new Interact();
            fire.key = new Key();
            fire.action = new Action();
            fire.key.values.put(new QName("id"), "minecraft:stick");
            fire.action.values.put(new QName("type"), "item");
            Drop firedrop = new Drop();
            firedrop.values.put(new QName("id"), "minecraft:torch");
            fire.action.drops.add(firedrop);

            Interact water = new Interact();
            water.key = new Key();
            water.action = new Action();
            water.key.values.put(new QName("id"), "minecraft:bucket");
            water.action.values.put(new QName("type"), "item");
            Drop waterdrop = new Drop();
            waterdrop.values.put(new QName("id"), "minecraft:water_bucket");
            water.action.drops.add(waterdrop);

            defaults.put(PokeType.fire, Lists.newArrayList(fire));
            defaults.put(PokeType.water, Lists.newArrayList(water));
        }

        protected static void initForEntry(PokedexEntry entry)
        {
            List<Interact> val = Lists.newArrayList();
            for (PokeType t : defaults.keySet())
            {
                if (entry.isType(t))
                {
                    val.addAll(defaults.get(t));
                }
            }
            if (!val.isEmpty())
            {
                initForEntry(entry, val);
            }
        }

        protected static void initForEntry(PokedexEntry entry, List<Interact> data)
        {
            if (data == null || data.isEmpty())
            {
                initForEntry(entry);
                return;
            }
            for (Interact interact : data)
            {
                Key key = interact.key;
                Action action = interact.action;
                boolean isForme = action.values.get(new QName("type")).equals("forme");
                Map<QName, String> values = key.values;
                if (key.tag != null)
                {
                    QName name = new QName("tag");
                    values.put(name, key.tag);
                }
                ItemStack keyStack = Tools.getStack(values);
                if (isForme)
                {
                    PokedexEntry forme = Database.getEntry(action.values.get(new QName("forme")));
                    if (forme != null) entry.interactionLogic.formes.put(keyStack, forme);
                }
                else
                {
                    List<ItemStack> stacks = Lists.newArrayList();
                    for (Drop d : action.drops)
                    {
                        values = d.values;
                        if (d.tag != null)
                        {
                            QName name = new QName("tag");
                            values.put(name, d.tag);
                        }
                        ItemStack stack = Tools.getStack(values);
                        if (stack != CompatWrapper.nullStack) stacks.add(stack);
                    }
                    entry.interactionLogic.stacks.put(keyStack, stacks);
                }
            }
        }

        HashMap<ItemStack, PokedexEntry>    formes = new HashMap<>();
        HashMap<ItemStack, List<ItemStack>> stacks = new HashMap<ItemStack, List<ItemStack>>();

        boolean canInteract(ItemStack key)
        {
            return getStackKey(key) != CompatWrapper.nullStack;
        }

        private ItemStack getFormeKey(ItemStack held)
        {
            if (held != null) for (ItemStack stack : formes.keySet())
            {
                if (Tools.isSameStack(stack, held)) { return stack; }
            }
            return CompatWrapper.nullStack;
        }

        private ItemStack getStackKey(ItemStack held)
        {
            if (held != null) for (ItemStack stack : stacks.keySet())
            {
                if (Tools.isSameStack(stack, held)) { return stack; }
            }
            return CompatWrapper.nullStack;
        }

        boolean interact(EntityPlayer player, IPokemob pokemob, boolean doInteract)
        {
            EntityLiving entity = (EntityLiving) pokemob;
            NBTTagCompound data = entity.getEntityData();
            ItemStack held = player.getHeldItemMainhand();
            if (data.hasKey("lastInteract"))
            {
                long time = data.getLong("lastInteract");
                long diff = entity.worldObj.getTotalWorldTime() - time;
                if (diff < 100) { return false; }
            }
            ItemStack stack = getStackKey(held);

            if (stack == CompatWrapper.nullStack)
            {
                stack = getFormeKey(held);
                if (!doInteract) return stack != CompatWrapper.nullStack;
                if (stack != CompatWrapper.nullStack)
                {
                    PokedexEntry forme = formes.get(stack);
                    pokemob.setPokedexEntry(forme);
                    return true;
                }
                return false;
            }
            if (!doInteract) return true;
            data.setLong("lastInteract", entity.worldObj.getTotalWorldTime());
            List<ItemStack> results = stacks.get(stack);
            int index = player.getRNG().nextInt(results.size());
            ItemStack result = results.get(index).copy();
            if (CompatWrapper.increment(held, -1) == 1)
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
            }
            else if (!player.inventory.addItemStackToInventory(result))
            {
                player.dropItem(result, false);
            }
            if (player != pokemob.getPokemonOwner())
            {
                entity.setAttackTarget(player);
            }
            return true;
        }

        List<ItemStack> interact(ItemStack key)
        {
            return stacks.get(getStackKey(key));
        }
    }

    public static interface MegaRule
    {
        boolean shouldMegaEvolve(IPokemob mobIn);
    }

    public static class SpawnData
    {
        final PokedexEntry entry;

        public static class SpawnEntry
        {
            int   max  = 4;
            int   min  = 2;
            float rate = 0.0f;
        }

        public Map<SpawnBiomeMatcher, SpawnEntry> matchers = Maps.newHashMap();

        public SpawnData(PokedexEntry entry)
        {
            this.entry = entry;
        }

        public SpawnBiomeMatcher getMatcher(World world, Vector3 location)
        {
            SpawnCheck checker = new SpawnCheck(location, world);
            return getMatcher(checker);
        }

        public SpawnBiomeMatcher getMatcher(SpawnCheck checker, boolean forSpawn)
        {
            for (SpawnBiomeMatcher matcher : matchers.keySet())
            {
                SpawnEvent.Check evt = new SpawnEvent.Check(entry, checker.location, checker.world, forSpawn);
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) continue;
                if (evt.getResult() == Result.ALLOW) return matcher;
                if (matcher.matches(checker)) return matcher;
            }
            return null;
        }

        public SpawnBiomeMatcher getMatcher(SpawnCheck checker)
        {
            return getMatcher(checker, true);
        }

        public int getMax(SpawnBiomeMatcher matcher)
        {
            return 4;
        }

        public int getMin(SpawnBiomeMatcher matcher)
        {
            return 2;
        }

        public float getWeight(SpawnBiomeMatcher matcher)
        {
            SpawnEntry entry = matchers.get(matcher);
            return entry == null ? 0 : entry.rate;
        }

        public boolean isValid(Biome biome)
        {
            for (SpawnBiomeMatcher matcher : matchers.keySet())
            {
                if (matcher.validBiomes.contains(biome)) return true;
            }
            return false;
        }

        public boolean isValid(BiomeType biome)
        {
            for (SpawnBiomeMatcher matcher : matchers.keySet())
            {
                if (matcher.validSubBiomes.contains(biome)) return true;
            }
            return false;
        }

        /** Only checks one biome type for vailidity
         * 
         * @param b
         * @return */
        public boolean isValid(World world, Vector3 location)
        {
            return getMatcher(world, location) != null;
        }

        public boolean isValid(SpawnCheck checker)
        {
            return getMatcher(checker) != null;
        }

        public void postInit()
        {
            for (SpawnBiomeMatcher matcher : matchers.keySet())
            {
                matcher.parse();
            }
        }
    }

    public static TimePeriod dawn  = new TimePeriod(0.9, 0.0);
    public static TimePeriod day   = new TimePeriod(0.0, 0.5);
    public static TimePeriod dusk  = new TimePeriod(0.5, 0.6);
    public static TimePeriod night = new TimePeriod(0.6, 0.9);

    private static void addFromEvolution(PokedexEntry a, PokedexEntry b)
    {
        for (EvolutionData d : a.evolutions)
        {
            d.postInit();
            PokedexEntry c = d.evolution;
            if (c == null)
            {
                continue;
            }
            b.addRelation(c);
            c.addRelation(b);
        }
    }

    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String>                abilities        = Lists.newArrayList();
    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String>                abilitiesHidden  = Lists.newArrayList();
    /** Times not included here the pokemob will go to sleep when idle. */
    @CopyToGender
    protected List<TimePeriod>                 activeTimes      = new ArrayList<TimePeriod>();
    public boolean                             base             = false;
    @CopyToGender
    private PokedexEntry                       baseForme        = null;
    /** Initial Happiness of the pokemob */
    @CopyToGender
    protected int                              baseHappiness;
    @CopyToGender
    protected String                           baseName;
    /** base xp given from defeating */
    @CopyToGender
    protected int                              baseXP;
    @CopyToGender
    public boolean                             breeds           = true;
    @CopyToGender
    public boolean                             canSitShoulder   = false;
    @CopyToGender
    protected int                              catchRate        = -1;
    @CopyToGender
    private int                                childNb          = 0;
    /** A map of father pokedexnb : child pokedexNbs */
    @CopyToGender
    protected Map<Integer, int[]>              childNumbers     = new HashMap<Integer, int[]>();
    /** Will the pokemob try to build colonies with others of it's kind */
    @CopyToGender
    public boolean                             colonyBuilder    = false;
    /** Default value of specialInfo, used to determine default colour of
     * recolourable parts */
    @CopyToGender
    public int                                 defaultSpecial   = 0;
    @CopyToGender
    public Map<ItemStack, Float>               drops            = Maps.newHashMap();
    /** indicatees of the specified special texture exists. Index 4 is used for
     * if the mob can be dyed */
    @CopyToGender
    public boolean                             dyeable          = false;
    @CopyToGender
    SoundEvent                                 event;
    @CopyToGender
    public SoundEvent                          replacedEvent;
    /** The relation between xp and level */
    @CopyToGender
    protected int                              evolutionMode    = 1;
    /** The list of pokemon this can evolve into */
    @CopyToGender
    public List<EvolutionData>                 evolutions       = new ArrayList<PokedexEntry.EvolutionData>();

    @CopyToGender
    public EvolutionData                       evolvesBy        = null;
    /** Who this pokemon evolves from. */
    @CopyToGender
    public PokedexEntry                        evolvesFrom      = null;
    @CopyToGender
    protected byte[]                           evs;
    protected PokedexEntry                     female           = null;
    /** Inital list of species which are prey */
    @CopyToGender
    protected String[]                         food;
    /** light,<br>
     * rock,<br>
     * power (near redstone blocks),<br>
     * grass,<br>
     * never hungry,<br>
     * berries,<br>
     * water (filter feeds from water) */
    @CopyToGender
    public boolean[]                           foods            = { false, false, false, false, false, true, false };
    @CopyToGender
    protected HashMap<ItemStack, PokedexEntry> formeItems       = Maps.newHashMap();
    /** Map of forms assosciated with this one. */
    @CopyToGender
    public Map<String, PokedexEntry>           forms            = new HashMap<String, PokedexEntry>();

    /** Used to stop gender formes from spawning, spawning rate is done by
     * gender ratio of base forme instead. */
    public boolean                             isGenderForme    = false;
    /** Can it megaevolve */
    @CopyToGender
    public boolean                             hasMegaForm      = false;
    @CopyToGender
    public boolean                             hasShiny         = true;
    /** Materials which will hurt or make it despawn. */
    @CopyToGender
    public String[]                            hatedMaterial;

    @CopyToGender
    public float                               height           = -1;
    @CopyToGender
    public boolean                             isMega           = false;

    /** the key is the itemstack, the value is the chance */

    @CopyToGender
    public Map<ItemStack, Float>               held             = Maps.newHashMap();
    /** Interactions with items from when player right clicks. */
    @CopyToGender
    protected InteractionLogic                 interactionLogic = new InteractionLogic();
    protected boolean                          isFemaleForme    = false;
    protected boolean                          isMaleForme      = false;
    @CopyToGender
    public boolean                             isShadowForme    = false;

    /** Will it protect others. */
    @CopyToGender
    public boolean                             isSocial         = true;

    public boolean                             isStarter        = false;

    @CopyToGender
    public boolean                             isStationary     = false;

    @CopyToGender
    public boolean                             legendary        = false;

    @CopyToGender
    public float                               length           = -1;
    /** Map of Level to Moves learned. */
    @CopyToGender
    private Map<Integer, ArrayList<String>>    lvlUpMoves;
    protected PokedexEntry                     male             = null;

    /** Mass of the pokemon in kg. */
    @CopyToGender
    public double                              mass             = -1;
    @CopyToGender
    protected HashMap<PokedexEntry, MegaRule>  megaRules        = Maps.newHashMap();

    /** Movement type for this mob */
    @CopyToGender
    protected PokecubeMod.Type                 mobType          = null;

    /** Mod which owns the pokemob, used for texture location. */
    @CopyToGender
    private String                             modId;
    protected String                           name;

    /** Particle Effects. */
    @CopyToGender
    public String[]                            particleData;
    /** Offset between top of hitbox and where player sits */
    @CopyToGender
    public double[][]                          passengerOffsets = { { 0, 1, 0 } };
    @CopyToGender
    protected int                              pokedexNb;
    /** All possible moves */
    @CopyToGender
    private List<String>                       possibleMoves;
    /** If the above is floating, how high does it try to float */
    @CopyToGender
    public double                              preferedHeight   = 1.5;
    /** Pokemobs with these entries will be hunted. */
    @CopyToGender
    private List<PokedexEntry>                 prey             = new ArrayList<PokedexEntry>();

    /** This list will contain all pokemon that are somehow related to this one
     * via evolution chains */
    @CopyToGender
    public List<PokedexEntry>                  related          = new ArrayList<PokedexEntry>();

    @CopyToGender
    protected int                              sexeRatio        = -1;
    @CopyToGender
    public PokedexEntry                        shadowForme      = null;

    @CopyToGender
    public boolean                             shouldDive       = false;

    @CopyToGender
    public boolean                             shouldFly        = false;

    @CopyToGender
    public boolean                             shouldSurf       = false;

    @CopyToGender
    protected ResourceLocation                 sound;

    @CopyToGender
    /** This is copied to the gender as it will allow specifying where that
     * gender spawns in pokedex. */
    private SpawnData                          spawns;

    /** Used to determine egg group */
    @CopyToGender
    public String[]                            species;
    @CopyToGender
    protected int[]                            stats;

    /** Array used for animated or gender based textures. Index 0 is the male
     * textures, index 1 is the females */
    @CopyToGender
    public String[][]                          textureDetails   = { { "" }, null };
    @CopyToGender
    public String                              texturePath      = "textures/entities/";

    @CopyToGender
    protected PokeType                         type1;
    @CopyToGender
    protected PokeType                         type2;

    @CopyToGender
    public float                               width            = -1;

    public PokedexEntry(int nb, String name)
    {
        this.name = name;
        this.pokedexNb = nb;
        if (Database.getEntry(name) == null) Database.allFormes.add(this);
        else new NullPointerException("Trying to add another " + name + " " + Database.getEntry(name))
                .printStackTrace();
    }

    public List<TimePeriod> activeTimes()
    {
        if (activeTimes.isEmpty())
        {
            activeTimes.add(TimePeriod.fullDay);
        }

        return activeTimes;
    }

    public void addEvolution(EvolutionData toAdd)
    {
        evolutions.add(toAdd);
    }

    protected void addEVXP(byte[] evs, int baseXP, int evolutionMode, int sexRatio)
    {
        this.evs = evs;
        this.baseXP = baseXP;
        this.evolutionMode = evolutionMode;
        this.sexeRatio = sexRatio;
    }

    protected void addForm(PokedexEntry form)
    {
        String key = form.name.toLowerCase(java.util.Locale.ENGLISH).trim().replaceAll("forme", "form").replaceAll(" ",
                "");
        form.baseName = name.split(" ")[0];
        form.setBaseForme(this);
        forms.put(key, form);
    }

    protected void addItem(String toParse, Map<ItemStack, Integer> toAddTo)
    {
        String[] drop = toParse.split(":");
        int chance = 100;
        if (drop.length > 3) chance = Integer.parseInt(drop[3]);
        ItemStack toAdd = parseStack(toParse);
        toAddTo.put(toAdd, chance);
    }

    protected void addItems(String toParse, Map<ItemStack, Integer> toAddTo)
    {
        if (toParse == null) return;
        String[] items = toParse.split(" ");
        for (String s : items)
        {
            addItem(s, toAddTo);
        }
    }

    public void addMove(String move)
    {
        for (String s : possibleMoves)
        {
            if (s.equals(move)) return;
        }
        possibleMoves.add(move);
    }

    protected void addMoves(List<String> moves, Map<Integer, ArrayList<String>> lvlUpMoves2)
    {
        this.lvlUpMoves = lvlUpMoves2;
        this.possibleMoves = moves;
        // System.out.println("Adding moves for "+name);
    }

    private void addRelation(PokedexEntry toAdd)
    {
        if (!related.contains(toAdd) && toAdd != null) related.add(toAdd);
    }

    public boolean areRelated(PokedexEntry toTest)
    {
        return related.contains(toTest);
    }

    public boolean canEvolve()
    {
        return evolutions.size() > 0;
    }

    public boolean canEvolve(int level)
    {
        return canEvolve(level, CompatWrapper.nullStack);
    }

    public boolean canEvolve(int level, ItemStack stack)
    {
        for (EvolutionData d : evolutions)
        {

            boolean itemCheck = d.item == CompatWrapper.nullStack;
            if (!itemCheck && stack != CompatWrapper.nullStack)
            {
                itemCheck = stack.isItemEqual(d.item);
            }
            if (d.level >= 0 && level >= d.level && itemCheck) return true;
        }

        return false;
    }

    public void copyToForm(PokedexEntry e)
    {
        if (e.baseForme != null && e.baseForme != this)
            throw new IllegalArgumentException("Cannot add a second base form");
        e.pokedexNb = pokedexNb;

        if (e.possibleMoves == null) e.possibleMoves = possibleMoves;
        if (e.lvlUpMoves == null) e.lvlUpMoves = lvlUpMoves;
        if (e.stats == null) e.stats = stats.clone();
        if (evs == null)
        {
            System.out.println(this + " " + this.baseForme);
            Thread.dumpStack();
        }
        if (e.evs == null) e.evs = evs.clone();
        if (e.height == -1) e.height = height;
        if (e.width == -1) e.width = width;
        if (e.length == -1) e.length = length;
        if (e.childNumbers.isEmpty()) e.childNumbers = childNumbers;
        if (e.species == null) e.species = species;
        if (e.mobType == null) e.mobType = mobType;
        if (e.catchRate == -1) e.catchRate = catchRate;
        if (e.mass == -1) e.mass = mass;
        if (e.held.isEmpty()) e.held = held;
        if (e.drops.isEmpty()) e.drops = drops;
        e.breeds = breeds;
        e.legendary = legendary;
        e.setBaseForme(this);
        this.addForm(e);
    }

    public PokedexEntry createGenderForme(byte gender)
    {
        String suffix = "";
        if (gender == IPokemob.MALE) suffix = " Male";
        else suffix = " Female";
        PokedexEntry forme = new PokedexEntry(pokedexNb, name + suffix);

        forme.setBaseForme(this);
        if (gender == IPokemob.MALE)
        {
            forme.isMaleForme = true;
            this.male = forme;
        }
        else
        {
            forme.isFemaleForme = true;
            this.female = forme;
        }
        forme.isGenderForme = true;
        return forme;
    }

    protected void copyToGenderFormes()
    {
        if (male != null || female != null)
        {
            Class<?> me = getClass();
            CopyToGender c;
            for (Field f : me.getDeclaredFields())
            {
                c = f.getAnnotation(CopyToGender.class);
                if (c != null)
                {
                    try
                    {
                        f.setAccessible(true);
                        if (male != null) f.set(male, f.get(this));
                        if (female != null) f.set(female, f.get(this));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean floats()
    {
        return mobType == PokecubeMod.Type.FLOATING;
    }

    public boolean flys()
    {
        return mobType == PokecubeMod.Type.FLYING;
    }

    public Ability getAbility(int number, IPokemob pokemob)
    {
        if (number < 2) { return AbilityManager.getAbility(abilities.get(number)); }
        if (number == 2) return getHiddenAbility(pokemob);
        return null;
    }

    public PokedexEntry getBaseForme()
    {
        if (baseForme == null && !base)
        {
            baseForme = Database.getEntry(getPokedexNb());
        }
        return baseForme;
    }

    /** For pokemon with multiple formes
     * 
     * @return the base forme name. */
    public String getBaseName()
    {
        if (baseName == null) baseName = name;
        return baseName;
    }

    /** @return the baseXP */
    public int getBaseXP()
    {
        return baseXP;
    }

    /** @return the catchRate */
    public int getCatchRate()
    {
        return catchRate;
    }

    public int getChildNb()
    {
        if (childNb == 0)
        {
            for (PokedexEntry e : related)
            {
                for (EvolutionData d : e.evolutions)
                {
                    if (d.evolution.getPokedexNb() == this.pokedexNb)
                    {
                        childNb = e.getChildNb();
                    }
                }
            }
            if (childNb == 0) childNb = this.pokedexNb;
        }

        return childNb;
    }

    public int getChildNb(int fatherNb)
    {
        if (childNumbers.containsKey(fatherNb))
        {
            int[] nums = childNumbers.get(fatherNb);
            int index = new Random().nextInt(nums.length);
            return nums[index];
        }
        else if (childNumbers.containsKey(0))
        {
            int[] nums = childNumbers.get(0);
            int index = new Random().nextInt(nums.length);
            return nums[index];
        }
        return getChildNb();
    }

    public PokedexEntry getEvo(IPokemob pokemob)
    {
        for (Entry<PokedexEntry, MegaRule> e : megaRules.entrySet())
        {
            MegaRule rule = e.getValue();
            PokedexEntry entry = e.getKey();
            if (rule.shouldMegaEvolve(pokemob)) return entry;
        }
        return null;
    }

    /** @return the evolutionMode */
    public int getEvolutionMode()
    {
        if (getBaseForme() != null) return getBaseForme().evolutionMode;
        return evolutionMode;
    }

    public List<EvolutionData> getEvolutions()
    {
        return evolutions;
    }

    /** @return the EVs earned by enemy at the end of a fight */
    public byte[] getEVs()
    {
        return evs;
    }

    public PokedexEntry getForGender(byte gender)
    {
        if (male != null || female != null) { return gender == IPokemob.MALE ? male == null ? male = this : male
                : female == null ? female = this : female; }
        return this;
    }

    /** @param form
     * @return the forme of the pokemob with the assosciated name. */
    public PokedexEntry getForm(String form)
    {
        return forms
                .get(form.toLowerCase(java.util.Locale.ENGLISH).trim().replaceAll("forme", "form").replaceAll(" ", ""));
    }

    public int getGen()
    {
        if (pokedexNb < 152) return 1;
        if (pokedexNb < 252) return 2;
        if (pokedexNb < 387) return 3;
        if (pokedexNb < 494) return 4;
        if (pokedexNb < 650) return 5;
        if (pokedexNb < 722) return 6;
        return 0;
    }

    public int getHappiness()
    {
        return baseHappiness;
    }

    public Ability getHiddenAbility(IPokemob pokemob)
    {
        if (abilitiesHidden.isEmpty()) return null;
        else if (abilitiesHidden.size() == 1) return AbilityManager.getAbility(abilitiesHidden.get(0));
        else if (abilitiesHidden.size() == 1) return pokemob.getSexe() == IPokemob.MALE
                ? AbilityManager.getAbility(abilitiesHidden.get(1)) : AbilityManager.getAbility(abilitiesHidden.get(1));
        return null;

    }

    /** returns whether the interaction logic has a response listed for the
     * given key.
     * 
     * @param pokemob
     * @return the stack that maps to this key */
    public List<ItemStack> getInteractResult(ItemStack stack)
    {
        return interactionLogic.interact(stack);
    }

    /** Gets the Mod which declares this mob.
     * 
     * @return the modId */
    public String getModId()
    {
        if (modId == null && getBaseForme() != null) modId = getBaseForme().modId;
        return modId;
    }

    public List<String> getMoves()
    {
        return possibleMoves;
    }

    public List<String> getMovesForLevel(int level)
    {
        List<String> ret = new ArrayList<String>();

        if (lvlUpMoves == null) return ret;

        for (int i = 0; i <= level; i++)
        {
            if (lvlUpMoves.get(i) == null) continue;
            for (String s : lvlUpMoves.get(i))
            {
                ret.add(s);
            }
        }

        return ret;
    }

    public List<String> getMovesForLevel(int level, int oldLevel)
    {
        List<String> ret = new ArrayList<String>();

        if (lvlUpMoves == null) return ret;

        if (oldLevel == 0) return getMovesForLevel(level);

        for (int i = oldLevel; i < level; i++)
        {
            if (lvlUpMoves.get(i + 1) == null) continue;
            for (String s : lvlUpMoves.get(i + 1))
            {
                ret.add(s);
            }
        }

        return ret;
    }

    public String getName()
    {
        return name;
    }

    /** Returns the name in a format that will work for files, ie no . at the
     * end.
     * 
     * @return */
    public String getTrimmedName()
    {
        String name = this.name;
        if (name.endsWith(".")) name = name.substring(0, name.length() - 1);
        return name;
    }

    /** @return the pokedexNb */
    public int getPokedexNb()
    {
        return pokedexNb;
    }

    public List<ItemStack> getRandomDrops(int looting)
    {
        if (drops.isEmpty()) return Lists.newArrayList();
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        items.addAll(drops.keySet());
        Random rand = new Random();
        for (ItemStack stack : items)
        {
            float chance = drops.get(stack);
            if (Math.random() < chance)
            {
                ItemStack newStack = stack.copy();
                int size = 1 + rand.nextInt(CompatWrapper.getStackSize(newStack) + looting);
                CompatWrapper.setStackSize(newStack, size);
                ret.add(newStack);
            }
        }
        return ret;
    }

    public ItemStack getRandomHeldItem()
    {
        if (held.isEmpty()) return CompatWrapper.nullStack;
        ItemStack ret = CompatWrapper.nullStack;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        items.addAll(held.keySet());
        Random rand = new Random();
        for (ItemStack stack : items)
        {
            float chance = held.get(stack);
            float rn = rand.nextFloat();
            if (rn < chance)
            {
                ItemStack newStack = stack.copy();
                CompatWrapper.setStackSize(newStack, 1);
                list.add(newStack);
            }
        }
        if (!list.isEmpty())
        {
            ret = list.get(rand.nextInt(list.size()));
        }
        return ret;
    }

    /** @return the sexeRatio */
    public int getSexeRatio()
    {
        return sexeRatio;
    }

    public SoundEvent getSoundEvent()
    {
        if (replacedEvent != null) return replacedEvent;
        if (event == null)
        {
            if (getBaseForme() != null && getBaseForme() != this)
            {
                event = getBaseForme().getSoundEvent();
                sound = getBaseForme().sound;
            }
            else
            {
                if (sound == null) setSound(getName());
                event = new SoundEvent(sound);
                ReflectionHelper.setPrivateValue(IForgeRegistryEntry.Impl.class, event, sound, "registryName");
                GameRegistry.register(event);
            }
        }
        return event;
    }

    public SpawnData getSpawnData()
    {
        return spawns;
    }

    public int getStatATT()
    {
        return stats[1];
    }

    public int getStatATTSPE()
    {
        return stats[3];
    }

    public int getStatDEF()
    {
        return stats[2];
    }

    public int getStatDEFSPE()
    {
        return stats[4];
    }

    public int getStatHP()
    {
        return stats[0];
    }

    /** @return the stats */
    public int[] getStats()
    {
        return stats.clone();
    }

    public int getStatVIT()
    {
        return stats[5];
    }

    public String getTexture(byte gender)
    {
        return getTexture(gender, 0);
    }

    public String getTexture(byte gender, long time)
    {
        return getTexture(name, gender, time);
    }

    public String getTexture(String original, byte gender, long time)
    {
        if (original == null) original = getTrimmedName();
        if (original.endsWith(".")) original = original.substring(0, original.length() - 1);
        int index = gender == IPokemob.FEMALE && textureDetails[1] != null ? 1 : 0;
        String[] textureSuffixs = textureDetails[index];
        long suffixIndex = ((time % textureSuffixs.length * 3) / textureSuffixs.length);
        String suffix = textureSuffixs[(int) suffixIndex];
        String ret = texturePath + original + suffix + ".png";
        ret = ret.toLowerCase(Locale.ENGLISH);
        return ret;
    }

    /** @return the type1 */
    public PokeType getType1()
    {
        return type1;
    }

    /** @return the type2 */
    public PokeType getType2()
    {
        return type2;
    }

    /** @return the name to be fed to the language formatter */
    public String getUnlocalizedName()
    {
        String name = this.name;
        if (this.isFemaleForme || this.isMaleForme) name = this.getBaseName();
        String translated = "pkmn." + name + ".name";
        return translated;
    }

    @SideOnly(Side.CLIENT)
    public String getTranslatedName()
    {
        return I18n.format(getUnlocalizedName());
    }

    public boolean hasForm(String form)
    {
        return forms.containsKey(
                form.toLowerCase(java.util.Locale.ENGLISH).trim().replaceAll("forme", "form").replaceAll(" ", ""));
    }

    public boolean hasPrey()
    {
        return prey.size() > 0;
    }

    protected void initPrey()
    {
        if (food == null) return;
        List<String> foodList = new ArrayList<String>();
        for (String s : food)
            foodList.add(s);
        poke:
        for (PokedexEntry e : Database.data.values())
        {
            if (e.species != null) for (String s : e.species)
            {
                if (foodList.contains(s))
                {
                    prey.add(e);
                    continue poke;
                }

            }
        }
    }

    protected void initRelations()
    {
        List<EvolutionData> stale = Lists.newArrayList();
        for (EvolutionData d : this.evolutions)
        {
            if (Pokedex.getInstance().getEntry(d.evolution.pokedexNb) == null) stale.add(d);
        }
        this.evolutions.removeAll(stale);
        addRelation(this);
        for (EvolutionData d : this.evolutions)
        {
            d.postInit();

            PokedexEntry temp = d.evolution;

            if (temp == null)
            {
                continue;
            }
            temp.evolvesFrom = this;
            temp.evolvesBy = d;
            temp.addRelation(this);
            addRelation(temp);
            for (PokedexEntry d1 : temp.related)
            {
                d1.addRelation(this);
                addRelation(d1);
            }
            addFromEvolution(this, temp);
            addFromEvolution(temp, this);
        }
        for (Integer i : Pokedex.getInstance().getEntries())
        {
            PokedexEntry e = Pokedex.getInstance().getEntry(i);
            if (e != null && e.species != null && species != null)
            {
                for (String s : species)
                {
                    for (String s1 : e.species)
                    {
                        if (s.equals(s1)) addRelation(e);
                    }
                }
            }
        }

        Object[] temp = related.toArray();
        Double[] nums = new Double[temp.length];
        for (int i = 0; i < nums.length; i++)
        {
            nums[i] = (double) ((PokedexEntry) temp[i]).getPokedexNb();
        }
        new Cruncher().sort(nums, temp);
        related.clear();
        for (Object o : temp)
        {
            related.add((PokedexEntry) o);
        }
    }

    /** Call whenever player right clicks a pokemob to run special interaction
     * logic
     * 
     * @param player
     * @param pokemob
     * @param doInteract
     *            - if false, will not actually do anything.
     * @return */
    public boolean interact(EntityPlayer player, IPokemob pokemob, boolean doInteract)
    {
        return interactionLogic.interact(player, pokemob, doInteract);
    }

    /** returns whether the interaction logic has a response listed for the
     * given key.
     * 
     * @param pokemob
     * @param doInteract
     *            - if false, will not actually do anything.
     * @return */
    public boolean interact(ItemStack stack)
    {
        return interactionLogic.canInteract(stack);
    }

    public boolean isFood(PokedexEntry toTest)
    {
        return prey.contains(toTest);
    }

    public boolean isType(PokeType type)
    {
        return type1 == type || type2 == type;
    }

    /** to be called after the new stack is applied as held item.
     * 
     * @param oldStack
     * @param newStack
     * @param pokemob */
    public void onHeldItemChange(ItemStack oldStack, ItemStack newStack, IPokemob pokemob)
    {
        if (newStack == CompatWrapper.nullStack && oldStack == CompatWrapper.nullStack) return;
        PokedexEntry newForme = null;
        if (newStack != CompatWrapper.nullStack)
        {
            for (ItemStack stack : formeItems.keySet())
                if (Tools.isSameStack(stack, newStack))
                {
                    newForme = formeItems.get(stack);
                    break;
                }
            if (newForme == null && getBaseForme() != null)
            {
                for (ItemStack stack : getBaseForme().formeItems.keySet())
                    if (Tools.isSameStack(stack, newStack))
                    {
                        newForme = getBaseForme().formeItems.get(stack);
                        break;
                    }
            }
        }
        else if (oldStack != CompatWrapper.nullStack && getBaseForme() != null)
        {
            for (ItemStack stack : getBaseForme().formeItems.keySet())
                if (Tools.isSameStack(stack, newStack))
                {
                    newForme = getBaseForme().formeItems.get(stack);
                    break;
                }
        }
        if (newForme != null)
        {
            pokemob.setPokedexEntry(newForme);
        }
    }

    protected ItemStack parseStack(String toParse)
    {
        if (toParse == null) return null;
        String[] drop = toParse.split(":");
        if (drop.length < 2) return null;
        int count = Integer.parseInt(drop[0]);
        String name = drop[1];
        int meta = 0;
        try
        {
            if (drop.length > 2) meta = Integer.parseInt(drop[2]);
        }
        catch (NumberFormatException e)
        {
        }

        Item item = PokecubeItems.getItem(name);
        ItemStack stack = PokecubeItems.getStack(name);
        ItemStack toAdd;
        if (item == null && stack == CompatWrapper.nullStack)
        {
            System.err.println("Problem with item " + name + " for " + this.getName());
            return null;
        }
        if (item != null)
        {
            toAdd = new ItemStack(item, count, meta);
        }
        else
        {
            toAdd = stack;
            CompatWrapper.setStackSize(toAdd, count);
        }
        return toAdd;
    }

    public void setBaseForme(PokedexEntry baseForme)
    {
        this.baseForme = baseForme;
    }

    /** Sets the Mod which declares this mob.
     * 
     * @param modId
     *            the modId to set */
    public void setModId(String modId)
    {
        this.modId = modId;
    }

    /** @param sound */
    public void setSound(String sound)
    {
        if (sound.endsWith(".")) sound = sound.substring(0, sound.length() - 1);
        this.sound = new ResourceLocation(getModId() + ":" + sound.toLowerCase(Locale.US));
    }

    public void setSpawnData(SpawnData data)
    {
        this.spawns = data;
    }

    public boolean shouldEvolve(IPokemob mob)
    {
        for (EvolutionData d : evolutions)
        {
            if (d.shouldEvolve(mob)) return true;
        }
        return false;
    }

    public boolean swims()
    {
        return mobType == PokecubeMod.Type.WATER;
    }

    @Override
    public String toString()
    {
        String ret = name;
        return ret;
    }

    public void updateMoves()
    {
        List<String> moves = new ArrayList<String>();

        if (possibleMoves == null)
        {
            System.out.println(this);
            possibleMoves = getBaseForme().possibleMoves;
        }
        if (lvlUpMoves == null)
        {
            lvlUpMoves = getBaseForme().lvlUpMoves;
        }

        for (int i = 0; i < possibleMoves.size(); i++)
        {
            String s = possibleMoves.get(i);
            if (MovesUtils.isMoveImplemented(s))
            {
                moves.add(s);
            }
        }
        possibleMoves.clear();
        possibleMoves.addAll(moves);
        List<Integer> toRemove = new ArrayList<Integer>();
        for (int level : lvlUpMoves.keySet())
        {
            moves.clear();
            List<String> lvls = lvlUpMoves.get(level);
            for (int i = 0; i < lvls.size(); i++)
            {
                String s = lvls.get(i);
                if (MovesUtils.isMoveImplemented(s))
                {
                    moves.add(s);
                }
            }
            lvls.clear();
            lvls.addAll(moves);
            if (lvls.size() == 0) toRemove.add(level);

        }
        for (int i : toRemove)
        {
            lvlUpMoves.remove(i);
        }
    }
}
