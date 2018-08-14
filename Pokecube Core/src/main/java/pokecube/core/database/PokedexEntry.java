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
import java.util.Set;
import java.util.logging.Level;

import javax.vecmath.Vector3f;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.text.WordUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntryLoader.Action;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.PokedexEntryLoader.Evolution;
import pokecube.core.database.PokedexEntryLoader.Interact;
import pokecube.core.database.PokedexEntryLoader.Key;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.DispenseBehaviourInteract;
import pokecube.core.events.SpawnEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.events.handlers.SpawnHandler.Variance;
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
    public static final String TEXTUREPATH = "entity/textures/";

    // Annotation used to specify which fields should be shared to all gender
    // formes.
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CopyToGender
    {
    }

    public static class EvolutionData
    {
        public SpawnBiomeMatcher  matcher      = null;
        public Evolution          data;
        public boolean            dayOnly      = false;
        public final PokedexEntry evolution;
        public String             FX           = "";
        // 1 for male, 2 for female, 0 for either;
        public byte               gender       = 0;
        public boolean            happy        = false;
        // the item it must be holding, if null, any item is fine, or no items
        // is fine
        public ItemStack          item         = ItemStack.EMPTY;
        public String             preset       = null;
        // does it need to grow a level for the item to work
        public boolean            itemLevel    = false;
        public int                level        = -1;
        public String             move         = "";
        public boolean            nightOnly    = false;
        public boolean            dawnOnly     = false;
        public boolean            duskOnly     = false;
        public PokedexEntry       preEvolution;
        public boolean            rainOnly     = false;
        public float              randomFactor = 1.0f;
        public boolean            traded       = false;

        public EvolutionData(PokedexEntry evol)
        {
            evolution = evol;
        }

        public Entity getEvolution(World world)
        {
            if (evolution == null) return null;
            Entity ret = PokecubeMod.core.createPokemob(evolution, world);
            return ret;
        }

        public boolean isInBiome(IPokemob mob)
        {
            if (matcher != null)
            {
                SpawnCheck check = new SpawnCheck(Vector3.getNewVector().set(mob.getEntity()),
                        mob.getEntity().getEntityWorld());
                return matcher.matches(check);
            }
            return true;
        }

        private void parse(Evolution data)
        {
            if (data.level != null) this.level = data.level;
            if (data.location != null) this.matcher = new SpawnBiomeMatcher(data.location);
            if (data.animation != null) this.FX = data.animation;
            if (data.item != null) this.item = Tools.getStack(data.item.values);
            if (data.item_preset != null)
            {
                this.preset = data.item_preset;
                this.item = PokecubeItems.getStack(preset);
            }
            if (data.time != null)
            {
                if (data.time.equalsIgnoreCase("day")) dayOnly = true;
                if (data.time.equalsIgnoreCase("night")) nightOnly = true;
                if (data.time.equalsIgnoreCase("dusk")) duskOnly = true;
                if (data.time.equalsIgnoreCase("dawn")) dawnOnly = true;
            }
            if (data.trade != null) this.traded = data.trade;
            if (data.rain != null) this.rainOnly = data.rain;
            if (data.happy != null) this.happy = data.happy;
            if (data.sexe != null)
            {
                if (data.sexe.equalsIgnoreCase("male")) gender = 1;
                if (data.sexe.equalsIgnoreCase("female")) gender = 2;
            }
            if (data.move != null) this.move = data.move;
            if (data.chance != null) this.randomFactor = data.chance;
            if (level == -1) level = 0;
            if (CompatWrapper.isValid(item)) PokecubeItems.addToEvos(item);
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
            return shouldEvolve(mob, mob.getHeldItem());
        }

        public boolean shouldEvolve(IPokemob mob, ItemStack mobs)
        {
            if (this.level < 0) return false;
            boolean ret = mob.traded() == this.traded || !this.traded;
            Random rand = new Random(mob.getRNGValue());
            if (rand.nextFloat() > randomFactor) return false;
            if (rainOnly)
            {
                World world = mob.getEntity().getEntityWorld();
                boolean rain = world.isRaining();
                if (!rain)
                {
                    TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
                    PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
                    if (teffect != null)
                    {
                        rain = teffect.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] > 0;
                    }
                }
                if (!rain) return false;
            }
            boolean correctItem = true;
            if (preset != null || CompatWrapper.isValid(item))
            {
                correctItem = false;
                if (CompatWrapper.isValid(mobs))
                {
                    if (preset != null)
                    {
                        int id = OreDictionary.getOreID(preset);
                        int[] ores = OreDictionary.getOreIDs(mobs);
                        for (int i = 0; i < ores.length; i++)
                        {
                            if (id == ores[i])
                            {
                                correctItem = true;
                                break;
                            }
                        }
                    }
                    else correctItem = Tools.isSameStack(mobs, item, true);
                }
            }
            if (Tools.isStack(mob.getHeldItem(), "everstone")) { return false; }
            if (Tools.isStack(mobs, "everstone")) { return false; }
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
            boolean rightTime = !dayOnly && !nightOnly && !dawnOnly && !duskOnly;
            if (!rightTime)
            {
                // TODO better way to choose current time.
                double time = (mob.getEntity().getEntityWorld().getWorldTime() % 24000) / 24000d;
                rightTime = dayOnly ? day.contains(time)
                        : nightOnly ? night.contains(time) : duskOnly ? dusk.contains(time) : dawn.contains(time);
            }
            ret = ret && rightTime;
            if (happy)
            {
                ret = ret && mob.getHappiness() >= 220;
            }
            if (ret && matcher != null)
            {
                ret = ret && isInBiome(mob);
            }
            return ret;
        }
    }

    public static class InteractionLogic
    {
        public static class Interaction
        {
            public final ItemStack  key;
            public PokedexEntry     forme;
            public List<ItemStack>  stacks   = Lists.newArrayList();
            public ResourceLocation lootTable;
            public boolean          male     = true;
            public boolean          female   = true;
            public int              cooldown = 100;
            public int              variance = 1;
            public int              hunger   = 100;

            public Interaction(ItemStack key)
            {
                this.key = key;
            }
        }

        static HashMap<PokeType, List<Interact>> defaults = new HashMap<>();

        public static void initDefaults()
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

            if (PokecubeMod.core.getConfig().defaultInteractions)
            {
                defaults.put(PokeType.getType("fire"), Lists.newArrayList(fire));
                defaults.put(PokeType.getType("water"), Lists.newArrayList(water));
            }
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

        private static void cleanInteract(Interact interact)
        {
            Interact defs = new Interact();
            for (Field f : Interact.class.getDeclaredFields())
            {
                f.setAccessible(true);
                try
                {
                    if (f.get(interact) == null) f.set(interact, f.get(defs));
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
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
                cleanInteract(interact);
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
                Interaction interaction = new Interaction(keyStack);
                interaction.male = interact.male;
                interaction.female = interact.female;
                interaction.cooldown = interact.cooldown;
                interaction.variance = Math.max(1, interact.variance);
                interaction.hunger = interact.baseHunger;
                entry.interactionLogic.actions.put(keyStack, interaction);
                if (isForme)
                {
                    PokedexEntry forme = Database.getEntry(action.values.get(new QName("forme")));
                    if (forme != null) interaction.forme = forme;
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
                        if (stack != ItemStack.EMPTY) stacks.add(stack);
                    }
                    interaction.stacks = stacks;
                    if (action.lootTable != null) interaction.lootTable = new ResourceLocation(action.lootTable);
                }
                DispenseBehaviourInteract.registerBehavior(keyStack);
            }
        }

        public HashMap<ItemStack, Interaction> actions = Maps.newHashMap();

        boolean canInteract(ItemStack key)
        {
            return getStackKey(key) != ItemStack.EMPTY;
        }

        public ItemStack getKey(ItemStack held)
        {
            if (held != null) for (ItemStack stack : actions.keySet())
            {
                if (Tools.isSameStack(stack, held)) { return stack; }
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getFormeKey(ItemStack held)
        {
            if (held != null) for (ItemStack stack : actions.keySet())
            {
                if (Tools.isSameStack(stack, held) && actions.get(stack).forme != null) { return stack; }
            }
            return ItemStack.EMPTY;
        }

        private ItemStack getStackKey(ItemStack held)
        {
            if (held != null) for (ItemStack stack : actions.keySet())
            {
                Interaction action = null;
                if (Tools.isSameStack(stack, held) && (!(action = actions.get(stack)).stacks.isEmpty()
                        || action.lootTable != null)) { return stack; }
            }
            return ItemStack.EMPTY;
        }

        boolean interact(EntityPlayer player, IPokemob pokemob, boolean doInteract)
        {
            EntityLiving entity = pokemob.getEntity();
            NBTTagCompound data = entity.getEntityData();
            ItemStack held = player.getHeldItemMainhand();
            ItemStack stack = getStackKey(held);
            if (!CompatWrapper.isValid(stack))
            {
                stack = getFormeKey(held);
                if (!CompatWrapper.isValid(stack)) return false;
                if (!doInteract) return true;
                Interaction action = actions.get(stack);
                PokedexEntry forme = action.forme;
                pokemob.setPokedexEntry(forme);
                return true;
            }
            Interaction action = actions.get(stack);
            if (data.hasKey("lastInteract"))
            {
                long time = data.getLong("lastInteract");
                long diff = entity.getEntityWorld().getTotalWorldTime() - time;
                if (diff < action.cooldown + new Random(time).nextInt(action.variance)) { return false; }
            }
            if (!action.male && pokemob.getSexe() == IPokemob.MALE) return false;
            if (!action.female && pokemob.getSexe() == IPokemob.FEMALE) return false;
            if (action.stacks.isEmpty() && action.lootTable == null) return false;
            if (!doInteract) return true;
            ItemStack result = null;
            if (action.lootTable != null)
            {
                LootTable loottable = pokemob.getEntity().getEntityWorld().getLootTableManager()
                        .getLootTableFromLocation(action.lootTable);
                LootContext.Builder lootcontext$builder = (new LootContext.Builder(
                        (WorldServer) pokemob.getEntity().getEntityWorld())).withLootedEntity(pokemob.getEntity());
                for (ItemStack itemstack : loottable.generateLootForPools(pokemob.getEntity().getRNG(),
                        lootcontext$builder.build()))
                {
                    if (CompatWrapper.isValid(itemstack))
                    {
                        result = itemstack;
                        break;
                    }
                }
            }
            else
            {

                List<ItemStack> results = action.stacks;
                int index = player.getRNG().nextInt(results.size());
                result = results.get(index).copy();
            }
            if (!CompatWrapper.isValid(result)) return false;
            data.setLong("lastInteract", entity.getEntityWorld().getTotalWorldTime());
            int time = pokemob.getHungerTime();
            pokemob.setHungerTime(time + action.hunger);
            held.shrink(1);
            if (held.isEmpty())
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
            return actions.get(getStackKey(key)).stacks;
        }
    }

    public static interface MegaRule
    {
        boolean shouldMegaEvolve(IPokemob mobIn, PokedexEntry entryTo);
    }

    public static class SpawnData
    {
        final PokedexEntry entry;

        public static class SpawnEntry
        {
            int      max      = 4;
            int      min      = 2;
            float    rate     = 0.0f;
            int      level    = -1;
            Variance variance = null;
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
            SpawnEntry entry = matchers.get(matcher);
            return entry == null ? 4 : entry.max;
        }

        public int getMin(SpawnBiomeMatcher matcher)
        {
            SpawnEntry entry = matchers.get(matcher);
            return entry == null ? 2 : entry.min;
        }

        public int getLevel(SpawnBiomeMatcher matcher)
        {
            SpawnEntry entry = matchers.get(matcher);
            return entry == null ? -1 : entry.level;
        }

        public Variance getVariance(SpawnBiomeMatcher matcher)
        {
            SpawnEntry entry = matchers.get(matcher);
            Variance variance = entry == null ? SpawnHandler.DEFAULT_VARIANCE : entry.variance;
            return variance;
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
                matcher.reset();
                matcher.parse();
            }
        }
    }

    public static TimePeriod          dawn  = new TimePeriod(0.85, 0.05);
    public static TimePeriod          day   = new TimePeriod(0.0, 0.5);
    public static TimePeriod          dusk  = new TimePeriod(0.45, 0.65);
    public static TimePeriod          night = new TimePeriod(0.6, 0.9);

    private static final PokedexEntry BLANK = new PokedexEntry(true);

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
    protected ArrayList<String>                 abilities        = Lists.newArrayList();
    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String>                 abilitiesHidden  = Lists.newArrayList();
    /** Times not included here the pokemob will go to sleep when idle. */
    @CopyToGender
    protected List<TimePeriod>                  activeTimes      = new ArrayList<TimePeriod>();
    /** if True, this is considered the "main" form for the type, this is what
     * is returned from any number based lookups. */
    public boolean                              base             = false;
    /** If True, this form won't be registered, this is used for mobs with a
     * single base template form, and then a bunch of alternate ones for things
     * to be copied from. */
    public boolean                              dummy            = false;
    /** If the forme is supposed to have a custom sound, rather than using base,
     * it will be set to this. */
    protected String                            customSound      = null;

    @CopyToGender
    private PokedexEntry                        baseForme        = null;
    /** Initial Happiness of the pokemob */
    @CopyToGender
    protected int                               baseHappiness;
    @CopyToGender
    protected String                            baseName;
    /** base xp given from defeating */
    @CopyToGender
    protected int                               baseXP           = -1;
    @CopyToGender
    public boolean                              breeds           = true;
    @CopyToGender
    public boolean                              canSitShoulder   = false;
    @CopyToGender
    protected int                               catchRate        = -1;
    @CopyToGender
    private PokedexEntry                        childNb          = null;
    /** A map of father pokedexnb : child pokedexNbs */
    @CopyToGender
    protected Map<PokedexEntry, PokedexEntry[]> childNumbers     = Maps.newHashMap();
    /** Will the pokemob try to build colonies with others of it's kind */
    @CopyToGender
    public boolean                              colonyBuilder    = false;
    /** Default value of specialInfo, used to determine default colour of
     * recolourable parts */
    @CopyToGender
    public int                                  defaultSpecial   = 0;
    /** Default value of specialInfo for shiny variants, used to determine
     * default colour of recolourable parts */
    @CopyToGender
    public int                                  defaultSpecials  = 0;
    @CopyToGender
    public Map<ItemStack, Float>                drops            = Maps.newHashMap();
    /** If the IPokemob supports this, then this will be the loot table used for
     * its drops. */
    @CopyToGender
    public ResourceLocation                     lootTable        = null;
    /** indicatees of the specified special texture exists. Index 4 is used for
     * if the mob can be dyed */
    @CopyToGender
    public boolean                              dyeable          = false;
    /** A Set of valid dye colours, if empty, any dye is valid. */
    @CopyToGender
    public Set<EnumDyeColor>                    validDyes        = Sets.newHashSet();
    @CopyToGender
    SoundEvent                                  event;
    @CopyToGender
    public SoundEvent                           replacedEvent;
    /** The relation between xp and level */
    @CopyToGender
    protected int                               evolutionMode    = 1;
    /** The list of pokemon this can evolve into */
    @CopyToGender
    public List<EvolutionData>                  evolutions       = new ArrayList<PokedexEntry.EvolutionData>();

    @CopyToGender
    public EvolutionData                        evolvesBy        = null;
    /** Who this pokemon evolves from. */
    @CopyToGender
    public PokedexEntry                         evolvesFrom      = null;
    @CopyToGender
    public byte[]                               evs;
    protected PokedexEntry                      female           = null;
    /** Inital list of species which are prey */
    @CopyToGender
    protected String[]                          food;
    /** light,<br>
     * rock,<br>
     * power (near redstone blocks),<br>
     * grass,<br>
     * never hungry,<br>
     * berries,<br>
     * water (filter feeds from water) */
    @CopyToGender
    public boolean[]                            foods            = { false, false, false, false, false, true, false };
    @CopyToGender
    protected HashMap<ItemStack, PokedexEntry>  formeItems       = Maps.newHashMap();
    /** Map of forms assosciated with this one. */
    @CopyToGender
    protected Map<String, PokedexEntry>         forms            = new HashMap<String, PokedexEntry>();

    /** Used to stop gender formes from spawning, spawning rate is done by
     * gender ratio of base forme instead. */
    public boolean                              isGenderForme    = false;
    /** Can it megaevolve */
    @CopyToGender
    public boolean                              hasMegaForm      = false;
    @CopyToGender
    public boolean                              hasShiny         = true;
    /** Materials which will hurt or make it despawn. */
    @CopyToGender
    public String[]                             hatedMaterial;

    @CopyToGender
    public float                                height           = -1;
    @CopyToGender
    public boolean                              isMega           = false;
    @CopyToGender
    public boolean                              ridable          = true;

    /** the key is the itemstack, the value is the chance */

    @CopyToGender
    public Map<ItemStack, Float>                held             = Maps.newHashMap();
    /** This is a loot table to be used for held item. if this isn't null, the
     * above held is ignored. */
    @CopyToGender
    public ResourceLocation                     heldTable        = null;
    /** Interactions with items from when player right clicks. */
    @CopyToGender
    public InteractionLogic                     interactionLogic = new InteractionLogic();
    protected boolean                           isFemaleForme    = false;
    protected boolean                           isMaleForme      = false;
    @CopyToGender
    public boolean                              isShadowForme    = false;

    /** Will it protect others. */
    @CopyToGender
    public boolean                              isSocial         = true;

    public boolean                              isStarter        = false;

    @CopyToGender
    public boolean                              isStationary     = false;

    @CopyToGender
    public boolean                              legendary        = false;

    @CopyToGender
    public float                                length           = -1;
    /** Map of Level to Moves learned. */
    @CopyToGender
    private Map<Integer, ArrayList<String>>     lvlUpMoves;
    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String>                 evolutionMoves   = Lists.newArrayList();

    protected PokedexEntry                      male             = null;

    /** Mass of the pokemon in kg. */
    @CopyToGender
    public double                               mass             = -1;
    @CopyToGender
    protected HashMap<PokedexEntry, MegaRule>   megaRules        = Maps.newHashMap();

    /** Movement type for this mob */
    @CopyToGender
    protected PokecubeMod.Type                  mobType          = null;

    /** Mod which owns the pokemob, used for texture location. */
    @CopyToGender
    private String                              modId;
    protected String                            name;

    /** Particle Effects. */
    @CopyToGender
    public String[]                             particleData;
    /** Offset between top of hitbox and where player sits */
    @CopyToGender
    public double[][]                           passengerOffsets = { { 0, 1, 0 } };
    @CopyToGender
    protected int                               pokedexNb;
    /** All possible moves */
    @CopyToGender
    private List<String>                        possibleMoves;
    /** If the above is floating, how high does it try to float */
    @CopyToGender
    public double                               preferedHeight   = 1.5;
    /** Pokemobs with these entries will be hunted. */
    @CopyToGender
    private List<PokedexEntry>                  prey             = new ArrayList<PokedexEntry>();

    /** This list will contain all pokemon that are somehow related to this one
     * via evolution chains */
    @CopyToGender
    private List<PokedexEntry>                  related          = new ArrayList<PokedexEntry>();

    @CopyToGender
    protected int                               sexeRatio        = -1;
    @CopyToGender
    public PokedexEntry                         shadowForme      = null;

    @CopyToGender
    public boolean                              shouldDive       = false;

    @CopyToGender
    public boolean                              shouldFly        = false;

    @CopyToGender
    public boolean                              shouldSurf       = false;

    @CopyToGender
    protected ResourceLocation                  sound;

    @CopyToGender
    /** This is copied to the gender as it will allow specifying where that
     * gender spawns in pokedex. */
    private SpawnData                           spawns;

    /** Used to determine egg group */
    @CopyToGender
    public String[]                             species          = {};
    @CopyToGender
    protected int[]                             stats;

    /** Array used for animated or gender based textures. Index 0 is the male
     * textures, index 1 is the females */
    @CopyToGender
    public String[][]                           textureDetails   = { { "" }, null };
    @CopyToGender
    public String                               texturePath      = TEXTUREPATH;

    @CopyToGender
    protected PokeType                          type1;
    @CopyToGender
    protected PokeType                          type2;

    @CopyToGender
    public float                                width            = -1;

    // This is the actual size of the model, if not null, will be used for
    // scaling of rendering in guis, order is length, height, width
    public Vector3f                             modelSize        = null;

    /** Cached trimmed name. */
    private String                              trimmedName;

    /** This constructor is used for making blank entry for copy comparisons.
     * 
     * @param blank */
    private PokedexEntry(boolean blank)
    {
        // Nothing
    }

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
        if (forms.containsValue(form)) return;
        String key = form.getTrimmedName();
        form.baseName = this.getTrimmedName();
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
        if (!getRelated().contains(toAdd) && toAdd != null && toAdd != this) getRelated().add(toAdd);
    }

    public boolean areRelated(PokedexEntry toTest)
    {
        return toTest == this || getRelated().contains(toTest);
    }

    public boolean canEvolve()
    {
        return evolutions.size() > 0;
    }

    public boolean canEvolve(int level)
    {
        return canEvolve(level, ItemStack.EMPTY);
    }

    public boolean canEvolve(int level, ItemStack stack)
    {
        for (EvolutionData d : evolutions)
        {

            boolean itemCheck = d.item == ItemStack.EMPTY;
            if (!itemCheck && stack != ItemStack.EMPTY)
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
            PokecubeMod.log(Level.WARNING, this + " " + this.baseForme, new IllegalArgumentException());
        }
        if (e.evs == null) e.evs = evs.clone();
        if (e.height == -1) e.height = height;
        if (e.width == -1) e.width = width;
        if (e.length == -1) e.length = length;
        if (e.childNumbers.isEmpty()) e.childNumbers = childNumbers;
        if (e.species == null) e.species = species;
        if (e.mobType == null) e.mobType = mobType;
        if (e.catchRate == -1) e.catchRate = catchRate;
        if (e.sexeRatio == -1) e.sexeRatio = sexeRatio;
        if (e.mass == -1) e.mass = mass;
        if (e.held.isEmpty()) e.held = held;
        if (e.drops.isEmpty()) e.drops = drops;
        for (int i = 0; i < foods.length; i++)
            e.foods[i] = foods[i];
        e.breeds = breeds;
        e.legendary = legendary;
        e.setBaseForme(this);
        this.addForm(e);
    }

    public PokedexEntry createGenderForme(byte gender, String name)
    {
        if (name == null)
        {
            name = this.name;
            String suffix = "";
            if (gender == IPokemob.MALE) suffix = " Male";
            else suffix = " Female";
            name = name + suffix;
        }
        PokedexEntry forme = Database.getEntry(name);
        if (forme == null) forme = new PokedexEntry(pokedexNb, name);
        forme.setBaseForme(this);
        if (gender == IPokemob.MALE)
        {
            forme.isMaleForme = true;
            this.male = forme;
            forme.sexeRatio = 0;
        }
        else
        {
            forme.isFemaleForme = true;
            this.female = forme;
            forme.sexeRatio = 254;
        }
        forme.isGenderForme = true;
        return forme;
    }

    protected void copyToGenderFormes()
    {
        if (male != null)
        {
            copyToForme(male);
        }
        if (female != null)
        {
            copyToForme(female);
        }
    }

    public void copyToForme(PokedexEntry forme)
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
                    if (isSame(f, forme, BLANK)) f.set(forme, f.get(this));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isSame(Field field, Object one, Object two) throws Exception
    {
        if (one == two) return true;
        field.setAccessible(true);
        Object a = field.get(one);
        Object b = field.get(two);
        if (a == b) return true;
        if (a != null) return a.equals(b);
        return false;
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
        if (number < abilities.size()) { return AbilityManager.getAbility(abilities.get(number)); }
        if (number == 2) return getHiddenAbility(pokemob);
        return null;
    }

    public PokedexEntry getBaseForme()
    {
        if (baseForme == null && !base)
        {
            baseForme = Database.getEntry(getPokedexNb());
        }
        if (baseForme == this) baseForme = null;
        return baseForme;
    }

    /** For pokemon with multiple formes
     * 
     * @return the base forme name. */
    public String getBaseName()
    {
        if (baseName == null)
        {
            if (getBaseForme() != null && getBaseForme() != this)
            {
                baseName = getBaseForme().getBaseName();
            }
            else baseName = name;
            if (getBaseForme() == this) PokecubeMod.log(Level.WARNING, "Error with " + this);
        }
        return baseName;
    }

    /** @return the baseXP */
    public int getBaseXP()
    {
        if (baseXP == -1) baseXP = getBaseForme() != null && getBaseForme() != this ? getBaseForme().getBaseXP() : 0;
        return baseXP;
    }

    /** @return the catchRate */
    public int getCatchRate()
    {
        return catchRate;
    }

    public PokedexEntry getChild()
    {
        if (childNb == null)
        {
            for (PokedexEntry e : getRelated())
            {
                for (EvolutionData d : e.evolutions)
                {
                    if (d.evolution == this)
                    {
                        childNb = e.getChild();
                    }
                }
            }
            if (childNb == null) childNb = this;
        }

        return childNb;
    }

    public PokedexEntry getChild(PokedexEntry fatherNb)
    {
        if (childNumbers.containsKey(fatherNb))
        {
            PokedexEntry[] nums = childNumbers.get(fatherNb);
            int index = new Random().nextInt(nums.length);
            return nums[index];
        }
        else if (childNumbers.containsKey(Database.missingno))
        {
            PokedexEntry[] nums = childNumbers.get(Database.missingno);
            int index = new Random().nextInt(nums.length);
            return nums[index];
        }
        return getChild();
    }

    public PokedexEntry getEvo(IPokemob pokemob)
    {
        for (Entry<PokedexEntry, MegaRule> e : megaRules.entrySet())
        {
            MegaRule rule = e.getValue();
            PokedexEntry entry = e.getKey();
            if (rule.shouldMegaEvolve(pokemob, entry)) return entry;
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
        if (!base && isGenderForme && getBaseForme() != null) return getBaseForme().getForGender(gender);
        if (male == null) male = this;
        if (female == null) female = this;
        return gender == IPokemob.MALE ? male : female;
    }

    public int getGen()
    {
        if (pokedexNb < 152) return 1;
        if (pokedexNb < 252) return 2;
        if (pokedexNb < 387) return 3;
        if (pokedexNb < 494) return 4;
        if (pokedexNb < 650) return 5;
        if (pokedexNb < 722) return 6;
        if (pokedexNb < 808) return 7;
        return 0;
    }

    public int getHappiness()
    {
        return baseHappiness;
    }

    public Ability getHiddenAbility(IPokemob pokemob)
    {
        if (abilitiesHidden.isEmpty()) return getAbility(0, pokemob);
        else if (abilitiesHidden.size() == 1) return AbilityManager.getAbility(abilitiesHidden.get(0));
        else if (abilitiesHidden.size() == 2) return pokemob.getSexe() == IPokemob.MALE
                ? AbilityManager.getAbility(abilitiesHidden.get(0)) : AbilityManager.getAbility(abilitiesHidden.get(1));
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

    /** A list of all valid moves for this pokemob */
    public List<String> getMoves()
    {
        return possibleMoves;
    }

    /** Moves to be learned right after evolution. */
    public List<String> getEvolutionMoves()
    {
        return evolutionMoves;
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

        if (oldLevel <= 0) return getMovesForLevel(level);

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
        if (trimmedName != null) return trimmedName;
        return trimmedName = Database.trim(name);
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
        looting = Math.max(looting, 0);
        Random rand = new Random();
        for (ItemStack stack : items)
        {
            if (!CompatWrapper.isValid(stack)) continue;
            float chance = drops.get(stack);
            if (Math.random() < chance)
            {
                ItemStack newStack = stack.copy();
                int size = 1 + rand.nextInt(newStack.getCount() + looting);
                newStack.setCount(size);
                ret.add(newStack);
            }
        }
        return ret;
    }

    public ItemStack getRandomHeldItem(EntityLiving mob)
    {
        if (mob.getEntityWorld().isRemote) return ItemStack.EMPTY;
        if (heldTable != null)
        {
            LootTable loottable = mob.getEntityWorld().getLootTableManager().getLootTableFromLocation(heldTable);
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((WorldServer) mob.getEntityWorld()))
                    .withLootedEntity(mob);
            for (ItemStack itemstack : loottable.generateLootForPools(mob.getRNG(), lootcontext$builder.build()))
            {
                if (!itemstack.isEmpty()) return itemstack;
            }
        }
        if (held.isEmpty()) return ItemStack.EMPTY;
        ItemStack ret = ItemStack.EMPTY;
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
                newStack.setCount(1);
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
        String original = getTrimmedName();
        int index = gender == IPokemob.FEMALE && textureDetails[1] != null ? 1 : 0;
        String[] textureSuffixs = textureDetails[index];
        String suffix = textureSuffixs[0];
        String ret = original + suffix + ".png";
        if (!ret.contains(texturePath)) ret = texturePath + ret;
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
        return forms.containsKey(Database.trim(form));
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
            if (!Pokedex.getInstance().isRegistered(d.evolution)) stale.add(d);
        }
        this.evolutions.removeAll(stale);
        if (!stale.isEmpty())
        {
            System.out.println(stale.size() + " stales for " + this);
        }
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
            for (PokedexEntry d1 : temp.getRelated())
            {
                d1.addRelation(this);
                addRelation(d1);
            }
            addFromEvolution(this, temp);
            addFromEvolution(temp, this);
        }
        for (PokedexEntry e : Pokedex.getInstance().getRegisteredEntries())
        {
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

        Object[] temp = getRelated().toArray();
        Double[] nums = new Double[temp.length];
        for (int i = 0; i < nums.length; i++)
        {
            nums[i] = (double) ((PokedexEntry) temp[i]).getPokedexNb();
        }
        new Cruncher().sort(nums, temp);
        getRelated().clear();
        for (Object o : temp)
        {
            getRelated().add((PokedexEntry) o);
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
        return (type1 != null && type1 == type) || (type2 != null && type2 == type);
    }

    /** to be called after the new stack is applied as held item.
     * 
     * @param oldStack
     * @param newStack
     * @param pokemob */
    public void onHeldItemChange(ItemStack oldStack, ItemStack newStack, IPokemob pokemob)
    {
        if (newStack == ItemStack.EMPTY && oldStack == ItemStack.EMPTY) return;
        PokedexEntry newForme = null;
        if (newStack != ItemStack.EMPTY)
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
        else if (oldStack != ItemStack.EMPTY && getBaseForme() != null)
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
        if (item == null && stack == ItemStack.EMPTY)
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
            toAdd.setCount(count);
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
        if (PokecubeMod.debug && this.modId != null && !this.modId.equals(modId))
        {
            PokecubeMod.log(Level.INFO, "Modid changed to: " + modId + " for " + this + " from " + this.modId,
                    new Exception());
        }
        this.modId = modId;
    }

    /** @param sound */
    public void setSound(String sound)
    {
        boolean mobs = false;
        if (mobs = sound.startsWith("mobs.")) sound = sound.replaceFirst("mobs.", "");
        // Replace all non word chars.
        sound = sound.replaceAll("([\\W])", "");
        modid:
        if (getModId() == null)
        {

            for (PokedexEntry e : Database.getFormes(this))
            {
                if (e.getModId() != null)
                {
                    PokecubeMod.log("Set MODID for Sounds:" + this + " " + e.getModId());
                    this.setModId(e.getModId());
                    break modid;
                }
            }
            this.setModId(PokecubeMod.defaultMod);
        }
        if (mobs) sound = "mobs." + sound;
        this.sound = new ResourceLocation(getModId() + ":" + sound);
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
            try
            {
                possibleMoves = getBaseForme().possibleMoves;
                possibleMoves.isEmpty();
            }
            catch (Exception e)
            {
                throw new RuntimeException(this.toString() + " no moves? " + getBaseForme());
            }
        }
        if (lvlUpMoves == null)
        {
            lvlUpMoves = getBaseForme().lvlUpMoves;
        }

        for (String s : possibleMoves)
        {
            if (MovesUtils.isMoveImplemented(s) && !moves.contains(s))
            {
                moves.add(s);
            }
        }
        List<String> staleEvoMoves = Lists.newArrayList();
        for (String s : evolutionMoves)
        {
            boolean implemented = MovesUtils.isMoveImplemented(s);
            if (implemented && !moves.contains(s))
            {
                moves.add(s);
            }
            else if (!implemented)
            {
                staleEvoMoves.add(s);
            }
        }
        evolutionMoves.removeAll(staleEvoMoves);
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

    public Vector3f getModelSize()
    {
        if (modelSize == null)
        {
            modelSize = new Vector3f(length, height, width);
        }
        return modelSize;
    }

    public List<PokedexEntry> getRelated()
    {
        return related;
    }

    private ITextComponent description;

    @SideOnly(Side.CLIENT)
    public ITextComponent getDescription()
    {
        if (description == null)
        {
            PokedexEntry entry = this;
            String typeString = WordUtils.capitalize(PokeType.getTranslatedName(entry.getType1()));
            if (entry.getType2() != PokeType.unknown)
                typeString += "/" + WordUtils.capitalize(PokeType.getTranslatedName(entry.getType2()));
            String typeDesc = I18n.format("pokemob.description.type", entry.getTranslatedName(), typeString);
            String evoString = null;
            if (entry.canEvolve())
            {
                for (EvolutionData d : entry.evolutions)
                {
                    if (d.evolution == null) continue;
                    PokedexEntry nex = d.evolution;
                    String subEvo = "";
                    if (d.level > 0)
                    {
                        subEvo = I18n.format("pokemob.description.evolve.level", entry.getTranslatedName(),
                                nex.getTranslatedName(), d.level);
                    }
                    else if (!d.item.isEmpty() && d.gender == 0)
                    {
                        if (d.traded)
                        {
                            subEvo = I18n.format("pokemob.description.evolve.traded.item", entry.getTranslatedName(),
                                    nex.getTranslatedName(), d.item.getDisplayName());
                        }
                        else subEvo = I18n.format("pokemob.description.evolve.item", entry.getTranslatedName(),
                                nex.getTranslatedName(), d.item.getDisplayName());
                    }
                    else if (!d.item.isEmpty() && d.gender == 1)
                    {
                        subEvo = I18n.format("pokemob.description.evolve.item.male", entry.getTranslatedName(),
                                nex.getTranslatedName(), d.item.getDisplayName());
                    }
                    else if (!d.item.isEmpty() && d.gender == 2)
                    {
                        subEvo = I18n.format("pokemob.description.evolve.item.female", entry.getTranslatedName(),
                                nex.getTranslatedName(), d.item.getDisplayName());
                    }
                    else if (d.traded && !d.item.isEmpty())
                    {
                        subEvo = I18n.format("pokemob.description.evolve.traded.item", entry.getTranslatedName(),
                                nex.getTranslatedName(), d.item.getDisplayName());
                    }
                    else if (d.happy)
                    {
                        subEvo = I18n.format("pokemob.description.evolve.happy", entry.getTranslatedName(),
                                nex.getTranslatedName());
                    }
                    else if (d.traded)
                    {
                        subEvo = I18n.format("pokemob.description.evolve.traded", entry.getTranslatedName(),
                                nex.getTranslatedName());
                    }
                    else if (d.move != null && !d.move.isEmpty())
                    {
                        subEvo = I18n.format("pokemob.description.evolve.move", entry.getTranslatedName(),
                                nex.getTranslatedName(), MovesUtils.getMoveName(d.move).getUnformattedText());
                    }
                    if (evoString == null) evoString = subEvo;
                    else evoString = evoString + subEvo;
                }
            }
            String descString = typeDesc;
            if (evoString != null) descString = descString + "\n" + evoString;
            if (entry.evolvesFrom != null)
            {
                descString = descString + "\n" + I18n.format("pokemob.description.evolve.from",
                        entry.getTranslatedName(), entry.evolvesFrom.getTranslatedName());
            }
            description = new TextComponentString(descString);
        }
        return description;
    }
}
