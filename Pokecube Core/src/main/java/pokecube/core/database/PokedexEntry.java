/**
 *
 */
package pokecube.core.database;

import static thut.api.terrain.BiomeType.NONE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pokecube.core.PokecubeItems;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Cruncher;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;

/** @author Manchou */
public class PokedexEntry
{
    Random                     rand               = new Random();
    protected int              pokedexNb;
    protected String           name;
    protected String           baseName;
    protected PokeType         type1;
    protected PokeType         type2;
    /** The relation between xp and level */
    protected int              evolutionMode      = 1;
    /** base xp given from defeating */
    protected int              baseXP;
    protected int              catchRate          = 3;
    protected int              sexeRatio          = -1;
    protected String           sound;
    private int[]              stats;
    protected byte[]           evs;
    /** Used to determine egg group */
    public String[]            species;
    /** Inital list of species which are prey */
    protected String[]         food;
    /** Array used for animated or gender based textures. Index 0 is the male
     * textures, index 1 is the females */
    public String[][]          textureDetails     = { { "" }, null };
    /** The abilities available to the pokedex entry. */
    protected String[]         abilities          = { null, null, null };
    /** Initial Happiness of the pokemob */
    protected int              baseHappiness;
    /** Mod which owns the pokemob, used for texture location. */
    private String             modId;
    /** Movement type for this mob */
    public PokecubeMod.Type    mobType            = PokecubeMod.Type.NORMAL;
    /** If the above is floating, how high does it try to float */
    public double              preferedHeight     = 1.5;
    /** Offset between top of hitbox and where player sits */
    public double              mountedOffset      = 1;
    /** indicatees of the specified special texture exists. Index 4 is used for
     * if the mob can be dyed */
    public boolean[]           hasSpecialTextures = { false, false, false, false, false };
    /** Default value of specialInfo, used to determine default colour of
     * recolourable parts */
    public int                 defaultSpecial     = 0;
    /** Can it megaevolve */
    public boolean             hasMegaForm        = false;
    /** Materials which will hurt or make it despawn. */
    public String[]            hatedMaterial;
    /** Particle Effects. */
    public String[]            particleData;
    public boolean             canSitShoulder     = false;
    public boolean             shouldFly          = false;
    public boolean             shouldDive         = false;
    /** Mass of the pokemon in kg. */
    public double              mass               = 1000;
    /** Will it protect others. */
    public boolean             isSocial           = true;
    /** light,<br>
     * rock,<br>
     * power (near redstone blocks),<br>
     * grass,<br>
     * never hungry,<br>
     * berries,<br>
     * water (filter feeds from water) */
    public boolean[]           foods              = { false, false, false, false, false, true, false };
    /** Times not included here the pokemob will go to sleep when idle. */
    protected List<TimePeriod> activeTimes        = new ArrayList<TimePeriod>();

    public boolean isStationary = false;

    public static TimePeriod night = new TimePeriod(0.6, 0.9);
    public static TimePeriod day   = new TimePeriod(0.0, 0.5);
    public static TimePeriod dusk  = new TimePeriod(0.5, 0.6);
    public static TimePeriod dawn  = new TimePeriod(0.9, 0.0);

    protected ItemStack foodDrop;

    /** the key is the itemstack, the value is the chance, out of 100, if it is
     * picked. */

    public Map<ItemStack, Integer> rareDrops = new HashMap<ItemStack, Integer>();

    public Map<ItemStack, Integer> commonDrops = new HashMap<ItemStack, Integer>();

    public Map<ItemStack, Integer> heldItems = new HashMap<ItemStack, Integer>();

    /** Map of forms assosciated with this one. */
    public Map<String, PokedexEntry> forms            = new HashMap<String, PokedexEntry>();
    /** A map of father pokedexnb : child pokedexNbs */
    protected Map<Integer, int[]>    childNumbers     = new HashMap<Integer, int[]>();
    /** Interactions with items from when player right clicks. */
    private InteractionLogic         interactionLogic = new InteractionLogic();
    /** Pokemobs with these entries will be hunted. */
    private List<PokedexEntry>       prey             = new ArrayList<PokedexEntry>();

    public float height = -1;
    public float width;
    public float length;

    private int childNb = 0;

    protected boolean hasStats = false;
    protected boolean hasEVXP  = false;

    /** All possible moves */
    private List<String>                    possibleMoves;
    /** Map of Level to Moves learned. */
    private Map<Integer, ArrayList<String>> lvlUpMoves;
    private SpawnData                       spawns;
    /** The list of pokemon this can evolve into */
    public List<EvolutionData>              evolutions  = new ArrayList<PokedexEntry.EvolutionData>();
    /** This list will contain all pokemon that are somehow related to this one
     * via evolution chains */
    public List<PokedexEntry>               related     = new ArrayList<PokedexEntry>();
    /** Who this pokemon evolves from. */
    public PokedexEntry                     evolvesFrom = null;
    public EvolutionData                    evolvesBy   = null;

    public PokedexEntry baseForme = null;

    private PokedexEntry(int nb, String name, List<String> moves, Map<Integer, ArrayList<String>> lvlUpMoves2)
    {
        this(nb, name);
        this.lvlUpMoves = lvlUpMoves2;
        this.possibleMoves = moves;
    }

    protected PokedexEntry(int nb, String name)
    {
        this.name = name;
        this.pokedexNb = nb;
        if (Database.getEntry(name) == null) Database.allFormes.add(this);
        else new NullPointerException("Trying to add another " + name + " " + Database.getEntry(name))
                .printStackTrace();
    }

    protected void addMoves(List<String> moves, Map<Integer, ArrayList<String>> lvlUpMoves2)
    {
        this.lvlUpMoves = lvlUpMoves2;
        this.possibleMoves = moves;
        // System.out.println("Adding moves for "+name);
    }

    private void setBaseStats(int HP, int ATT, int DEF, int ATTSPE, int DEFSPE, int VIT, int catchRate,
            PokeType pokeType, PokeType pokeType2)
    {
        this.hasStats = true;
        this.catchRate = catchRate;
        this.type1 = pokeType;
        this.type2 = pokeType2;
        this.stats = new int[] { HP, ATT, DEF, ATTSPE, DEFSPE, VIT };
    }

    public void copyToForm(PokedexEntry e)
    {
        if (e.baseForme != null && e.baseForme != this)
            throw new IllegalArgumentException("Cannot add a second base form");
        e.pokedexNb = pokedexNb;
        e.possibleMoves = possibleMoves;
        e.lvlUpMoves = lvlUpMoves;
        if (hasStats) e.setBaseStats(getStatHP(), getStatATT(), getStatDEF(), getStatATTSPE(), getStatDEFSPE(),
                getStatVIT(), catchRate, type1, type2);
        if (hasEVXP) e.setEVXP(evs[0], evs[1], evs[2], evs[3], evs[4], evs[5], baseXP, evolutionMode, sexeRatio);

        e.baseForme = this;
        if (e.height == -1)
        {
            e.height = e.baseForme.height;
            e.width = e.baseForme.width;
            e.length = e.baseForme.length;
            e.childNumbers = e.baseForme.childNumbers;
            e.species = e.baseForme.species;
            e.setModId(e.baseForme.getModId());
            e.mobType = e.baseForme.mobType;
            e.catchRate = e.baseForme.catchRate;
            e.mass = e.baseForme.mass;
            e.foodDrop = e.baseForme.foodDrop;
            e.commonDrops = e.baseForme.commonDrops;
            e.rareDrops = e.baseForme.rareDrops;
        }
        if (e.species == null)
        {
            e.childNumbers = e.baseForme.childNumbers;
            e.species = e.baseForme.species;
        }
    }

    public void addStats(String name, int HP, int ATT, int DEF, int ATTSPE, int DEFSPE, int VIT, int catchRate,
            PokeType pokeType, PokeType pokeType2)
    {
        if (!this.hasStats)
        {
            this.setBaseStats(HP, ATT, DEF, ATTSPE, DEFSPE, VIT, catchRate, pokeType, pokeType2);
            addForm(this);
        }
        else
        {
            PokedexEntry form = getForm(name);
            if (form == null)
            {
                form = new PokedexEntry(pokedexNb, name, possibleMoves, lvlUpMoves);
                this.copyToForm(form);
                addForm(form);
                System.out.println("new form for " + this + " as " + form);
            }
            form.setBaseStats(HP, ATT, DEF, ATTSPE, DEFSPE, VIT, catchRate, pokeType, pokeType2);
        }
    }

    public void addEVXP(String name, int HP_EV, int ATT_EV, int DEF_EV, int ATTSPE_EV, int DEFSPE_EV, int VIT_EV,
            int baseXP, int evolutionMode, int sexRatio)
    {
        if (!this.hasEVXP)
        {
            this.setEVXP(HP_EV, ATT_EV, DEF_EV, ATTSPE_EV, DEFSPE_EV, VIT_EV, baseXP, evolutionMode, sexRatio);
            if (!hasForm(name)) addForm(this);
            for (PokedexEntry dbe : forms.values())
            {
                dbe.setEVXP(HP_EV, ATT_EV, DEF_EV, ATTSPE_EV, DEFSPE_EV, VIT_EV, baseXP, evolutionMode, sexRatio);
            }
        }
        else
        {
            if (hasForm(name))
            {
                getForm(name).setEVXP(HP_EV, ATT_EV, DEF_EV, ATTSPE_EV, DEFSPE_EV, VIT_EV, baseXP, evolutionMode,
                        sexRatio);
            }
            else
            {
                PokedexEntry form = new PokedexEntry(pokedexNb, name);
                this.copyToForm(form);
                form.setEVXP(HP_EV, ATT_EV, DEF_EV, ATTSPE_EV, DEFSPE_EV, VIT_EV, baseXP, evolutionMode, sexRatio);
                System.out.println("Adding EVs for " + form);
                this.addForm(form);
            }
            // System.err.println("Attempted to add EV gain for an unknown form
            // of "+this.name+" with the name "+name);
        }
    }

    private void setEVXP(int HP_EV, int ATT_EV, int DEF_EV, int ATTSPE_EV, int DEFSPE_EV, int VIT_EV, int baseXP,
            int evolutionMode, int sexeRatio)
    {
        this.hasEVXP = true;
        this.evs = new byte[] { (byte) HP_EV, (byte) ATT_EV, (byte) DEF_EV, (byte) ATTSPE_EV, (byte) DEFSPE_EV,
                (byte) VIT_EV };
        this.baseXP = baseXP;
        this.sexeRatio = sexeRatio;
        this.evolutionMode = evolutionMode;
    }

    protected void addForm(PokedexEntry form)
    {
        String key = form.name.toLowerCase().trim().replaceAll("forme", "form").replaceAll(" ", "");
        form.baseName = name.split(" ")[0];
        form.baseForme = this;
        forms.put(key, form);
    }

    /** @param form
     * @return the forme of the pokemob with the assosciated name. */
    public PokedexEntry getForm(String form)
    {
        return forms.get(form.toLowerCase().trim().replaceAll("forme", "form").replaceAll(" ", ""));
    }

    public boolean hasForm(String form)
    {
        return forms.containsKey(form.toLowerCase().trim().replaceAll("forme", "form").replaceAll(" ", ""));
    }

    /** Sets the Mod which declares this mob.
     * 
     * @param modId
     *            the modId to set */
    public void setModId(String modId)
    {
        this.modId = modId;
        for (PokedexEntry forme : forms.values())
        {
            if (forme != this) forme.setFormModId(modId);
        }
    }

    private void setFormModId(String modId)
    {
        this.modId = modId;
    }

    /** Gets the Mod which declares this mob.
     * 
     * @return the modId */
    public String getModId()
    {
        return modId;
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
        int index = gender == IPokemob.FEMALE && textureDetails[1] != null ? 1 : 0;
        String[] textureSuffixs = textureDetails[index];
        long suffixIndex = ((time % textureSuffixs.length * 3) / textureSuffixs.length);
        String suffix = textureSuffixs[(int) suffixIndex];
        return "textures/entities/" + original + suffix + ".png";// texture;
    }

    /** @return the pokedexNb */
    public int getPokedexNb()
    {
        return pokedexNb;
    }

    /** @return the name in the language of player */
    public String getTranslatedName()
    {
        String translated = StatCollector.translateToLocal("pkmn." + name + ".name").trim();
        if (translated.contains(".")) { return name; }

        return translated;
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

    public boolean isType(PokeType type)
    {
        return type1 == type || type2 == type;
    }

    /** @return the evolutionMode */
    public int getEvolutionMode()
    {
        return evolutionMode;
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

    /** @return the sexeRatio */
    public int getSexeRatio()
    {
        return sexeRatio;
    }

    /** @return the sound */
    public String getSound()
    {
        return sound;
    }

    /** @param sound */
    public void setSound(String sound)
    {
        this.sound = sound;
    }

    /** @return the EVs earned by enemy at the end of a fight */
    public byte[] getEVs()
    {
        return evs;
    }

    /** @return the stats */
    public int[] getStats()
    {
        return stats.clone();
    }

    public int getStatHP()
    {
        return stats[0];
    }

    public int getStatATT()
    {
        return stats[1];
    }

    public int getStatDEF()
    {
        return stats[2];
    }

    public int getStatATTSPE()
    {
        return stats[3];
    }

    public int getStatDEFSPE()
    {
        return stats[4];
    }

    public int getStatVIT()
    {
        return stats[5];
    }

    public int getHappiness()
    {
        return baseHappiness;
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
            possibleMoves = baseForme.possibleMoves;
        }
        if (lvlUpMoves == null)
        {
            lvlUpMoves = baseForme.lvlUpMoves;
        }

        for (int i = 0; i < possibleMoves.size(); i++)
        {
            String s = possibleMoves.get(i);
            if (Move_Base.instance.isMoveImplemented(s))
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
                if (Move_Base.instance.isMoveImplemented(s))
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

    public List<String> getMoves()
    {
        return possibleMoves;
    }

    public void addMove(String move)
    {
        for (String s : possibleMoves)
        {
            if (s.equals(move)) return;
        }
        possibleMoves.add(move);
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

    public List<TimePeriod> activeTimes()
    {
        if (activeTimes.isEmpty())
        {
            activeTimes.add(TimePeriod.fullDay);
        }

        return activeTimes;
    }

    public String getName()
    {
        return name;
    }

    /** For pokemon with multiple formes
     * 
     * @return the base forme name. */
    public String getBaseName()
    {
        if (baseName == null) baseName = name;
        return baseName;
    }

    public int getGen()
    {
        if (pokedexNb < 152) return 1;
        if (pokedexNb < 252) return 3;
        if (pokedexNb < 387) return 3;
        if (pokedexNb < 494) return 4;
        if (pokedexNb < 650) return 5;
        if (pokedexNb < 722) return 6;

        return 0;
    }

    public boolean swims()
    {
        return mobType == PokecubeMod.Type.WATER;
    }

    public boolean flys()
    {
        return mobType == PokecubeMod.Type.FLYING;
    }

    public boolean floats()
    {
        return mobType == PokecubeMod.Type.FLOATING;
    }

    public void setSpawnData(SpawnData data)
    {
        this.spawns = data;
    }

    public SpawnData getSpawnData()
    {
        return spawns;
    }

    public List<EvolutionData> getEvolutions()
    {
        return evolutions;
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

    public int getChildNb()
    {
        if (childNb == 0)
        {
            for (PokedexEntry e : related)
            {
                for (EvolutionData d : e.evolutions)
                {
                    if (d.evolutionNb == this.pokedexNb)
                    {
                        childNb = e.getChildNb();
                    }
                }
            }
            if (childNb == 0) childNb = this.pokedexNb;
        }

        return childNb;
    }

    public void addEvolution(EvolutionData toAdd)
    {
        evolutions.add(toAdd);
    }

    public boolean hasPrey()
    {
        return prey.size() > 0;
    }

    protected void initRelations()
    {
        addRelation(this);
        for (EvolutionData d : this.evolutions)
        {
            PokedexEntry temp = Pokedex.getInstance().getEntry(d.evolutionNb);

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

    protected void addItems(String toParse, Map<ItemStack, Integer> toAddTo)
    {
        String[] items = toParse.split(" ");
        for (String s : items)
        {
            addItem(s, toAddTo);
        }
    }

    protected void addItem(String toParse, Map<ItemStack, Integer> toAddTo)
    {
        String[] drop = toParse.split(":");
        int chance = 100;
        if (drop.length > 3) chance = Integer.parseInt(drop[3]);
        ItemStack toAdd = parseStack(toParse);
        toAddTo.put(toAdd, chance);
    }

    protected ItemStack parseStack(String toParse)
    {
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
        if (item == null && stack == null)
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
            toAdd.stackSize = count;
        }
        return toAdd;
    }

    private static void addFromEvolution(PokedexEntry a, PokedexEntry b)
    {
        for (EvolutionData d : a.evolutions)
        {
            PokedexEntry c = Pokedex.getInstance().getEntry(d.evolutionNb);
            if (c == null)
            {
                continue;
            }
            b.addRelation(c);
            c.addRelation(b);
        }
    }

    private void addRelation(PokedexEntry toAdd)
    {
        if (!related.contains(toAdd) && toAdd != null) related.add(toAdd);
    }

    public boolean areRelated(PokedexEntry toTest)
    {
        return related.contains(toTest);
    }

    public boolean isFood(PokedexEntry toTest)
    {
        return prey.contains(toTest);
    }

    public boolean canEvolve()
    {
        return evolutions.size() > 0;
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

    /** returns whether the interaction logic has a response listed for the
     * given key.
     * 
     * @param pokemob
     * @return the stack that maps to this key */
    public List<ItemStack> getInteractResult(ItemStack stack)
    {
        return interactionLogic.interact(stack);
    }

    public boolean shouldEvolve(IPokemob mob)
    {
        for (EvolutionData d : evolutions)
        {
            if (d.shouldEvolve(mob)) return true;
        }
        return false;
    }

    public boolean canEvolve(int level)
    {
        return canEvolve(level, null);
    }

    public boolean canEvolve(int level, ItemStack stack)
    {
        for (EvolutionData d : evolutions)
        {

            boolean itemCheck = d.item == null;
            if (!itemCheck && stack != null)
            {
                itemCheck = stack.isItemEqual(d.item);
            }
            if (d.level >= 0 && level >= d.level && itemCheck) return true;
        }

        return false;
    }

    public ItemStack getFoodDrop(int looting)
    {
        if (foodDrop == null) return null;
        ItemStack ret = foodDrop.copy();
        int j = this.rand.nextInt(ret.stackSize + 1);

        if (looting > 0)
        {
            j += this.rand.nextInt(looting + 1);
        }
        ret.stackSize = j;

        return ret;
    }

    public ItemStack getRandomCommonDrop(int looting)
    {
        ItemStack ret = null;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        items.addAll(commonDrops.keySet());
        int index = rand.nextInt(items.size());
        ret = items.get(index);
        int chance = commonDrops.get(ret);
        if (ret == null || rand.nextInt(100) > chance) return null;
        ret = ret.copy();
        int j = 1 + this.rand.nextInt(ret.stackSize);

        if (looting > 0)
        {
            j += this.rand.nextInt(looting + 1);
        }
        ret.stackSize = j;
        return ret;
    }

    public ItemStack getRandomRareDrop(int looting)
    {
        ItemStack ret = null;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        items.addAll(rareDrops.keySet());
        int index = rand.nextInt(items.size());
        ret = items.get(index);
        int chance = rareDrops.get(ret);
        if (ret == null || rand.nextInt(100) > chance) return null;
        ret = ret.copy();
        int j = 1 + this.rand.nextInt(ret.stackSize);

        if (looting > 0)
        {
            j += this.rand.nextInt(looting + 1);
        }
        ret.stackSize = j;
        return ret;
    }

    public ItemStack getRandomHeldItem()
    {
        if (heldItems.size() < 1) return null;

        ItemStack ret = null;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        items.addAll(heldItems.keySet());
        int index = rand.nextInt(items.size());
        ret = items.get(index);
        int chance = heldItems.get(ret);
        if (ret == null || rand.nextInt(100) > chance) return null;
        ret = ret.copy();
        return ret;
    }

    public Ability getAbility(int number)
    {
        if (number < 3) { return AbilityManager.getAbility(abilities[number]); }

        return null;
    }

    public static class EvolutionData
    {
        public final int evolutionNb;
        public int       preEvolutionNb;
        public int       level     = -1;
        // the item it must be holding, if null, any item is fine, or no items
        // is fine
        public ItemStack item      = null;
        // does it need to grow a level for the item to work
        public boolean   itemLevel = false;
        public boolean   dayOnly   = false;
        public boolean   nightOnly = false;
        public boolean   traded    = false;
        public boolean   happy     = false;
        public String    move      = "";
        // 1 for male, 2 for female, 0 for either;
        public byte      gender    = 0;

        public String FX = "";

        private EvolutionData(int number)
        {
            this.evolutionNb = number;
        }

        public EvolutionData(int number, String data, String FX)
        {
            this(number);
            this.FX = FX;
            parse(data);
        }

        private void parse(String data)
        {
            String[] parts = data.split(":");
            String itemname = "";
            if (parts[0].equalsIgnoreCase("trade")) traded = true;
            if (traded && parts.length >= 2)
            {
                this.item = PokecubeItems.getStack(parts[1]);
                if (parts.length > 2)
                {
                    String temp = parts[2];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                }
            }

            if (parts.length >= 2 && parts[0].equalsIgnoreCase("level"))
            {

                try
                {
                    this.level = Integer.parseInt(parts[1]);
                }
                catch (NumberFormatException e)
                {
                    this.item = PokecubeItems.getStack(parts[1]);
                    itemname = parts[1];
                    this.level = 0;
                }
                this.item = PokecubeItems.getStack(parts[1]);
                if (this.item == null)
                {
                    String temp = parts[1];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                }
                else
                {
                    itemname = parts[1];
                }
                if (parts.length > 2)
                {
                    String temp = parts[2];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                }
            }
            if (parts.length >= 2 && parts[0].equalsIgnoreCase("stone"))
            {
                String stoneName = parts[1] + "stone";
                item = PokecubeItems.getStack(stoneName);
                itemname = stoneName;
                if (item == null)
                {
                    item = PokecubeItems.getStack(parts[1]);
                    itemname = parts[1];
                }
                level = 0;
                // System.out.println("Stone added for "+evolutionNb+" "+item+"
                // "+stoneName);
                if (parts.length > 2)
                {
                    String temp = parts[2];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                }
            }
            if (parts.length >= 2 && parts[0].equalsIgnoreCase("move"))
            {
                move = parts[1];
                level = 0;
                if (parts.length > 2)
                {
                    String temp = parts[2];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                }
            }
            if (parts[0].equalsIgnoreCase("happiness")) happy = true;
            if (happy && parts.length >= 2)
            {
                this.item = PokecubeItems.getStack(parts[1]);
                level = 0;
                if (this.item == null)
                {
                    String temp = parts[1];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                    // System.out.println(dayOnly+" "+nightOnly+"
                    // "+evolutionNb);
                }
                else
                {
                    itemname = parts[1];
                }
                if (parts.length > 2)
                {
                    String temp = parts[2];
                    if (temp.equalsIgnoreCase("male")) gender = 1;
                    if (temp.equalsIgnoreCase("female")) gender = 2;
                    if (temp.equalsIgnoreCase("day")) dayOnly = true;
                    if (temp.equalsIgnoreCase("night")) nightOnly = true;
                    // System.out.println(dayOnly+" "+nightOnly+"
                    // "+evolutionNb);
                }
            }

            if (item != null)
            {
                PokecubeItems.addToHoldables(itemname);
            }

            if (traded || happy) this.level = 0;
        }

        public Entity getEvolution(World world)
        {
            if (evolutionNb == 0) return null;

            return PokecubeMod.core.createEntityByPokedexNb(evolutionNb, world);
        }

        public boolean shouldEvolve(IPokemob mob)
        {
            boolean ret = mob.traded() == this.traded || !this.traded;
            // System.out.println("Should EvolveTest");
            if (this.level < 0) return false;

            boolean correctItem = item == null;
            // System.out.println(item);
            if (item != null && mob instanceof EntityLiving)
            {
                ItemStack mobs = ((EntityLiving) mob).getHeldItem();
                if (mobs != null)
                {
                    correctItem = mobs.isItemEqual(item);
                }
            }
            if (mob instanceof EntityLiving && ((EntityLiving) mob).getHeldItem() != null
                    && ((EntityLiving) mob).getHeldItem().isItemEqual(PokecubeItems.getStack("everstone")))
            {
                // System.out.println("Everstone");
                return false;
            }
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
                rightTime = dayOnly ? poke.worldObj.isDaytime() : !poke.worldObj.isDaytime();
                // System.out.println("Is it Day?"+poke.worldObj.isDaytime());
            }

            ret = ret && rightTime;
            if (happy)
            {
                ret = ret && mob.getHappiness() >= 220;
            }
            return ret;
        }

        public boolean shouldEvolve(IPokemob mob, ItemStack mobs)
        {
            boolean ret = mob.traded() == this.traded || !this.traded;
            if (this.level < 0) return false;

            boolean correctItem = item == null;
            if (item != null && mob instanceof EntityLiving)
            {
                if (mobs != null)
                {
                    correctItem = mobs.isItemEqual(item);
                }
            }
            if (mob instanceof EntityLiving && ((EntityLiving) mob).getHeldItem() != null && ((EntityLiving) mob)
                    .getHeldItem().isItemEqual(PokecubeItems.getStack("everstone"))) { return false; }
            if (mobs != null && mobs.isItemEqual(PokecubeItems.getStack("everstone"))) { return false; }
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
                rightTime = dayOnly ? poke.worldObj.isDaytime() : !poke.worldObj.isDaytime();
                // System.out.println("Is it Day?"+poke.worldObj.isDaytime());
            }
            ret = ret && rightTime;
            if (happy)
            {
                ret = ret && mob.getHappiness() >= 220;
            }
            return ret;
        }
    }

    public static class SpawnData
    {
        public static final int DAY   = 0;
        public static final int NIGHT = 1;

        public static final int CAVE = 2;

        public static final int WATER     = 3;
        public static final int WATERPLUS = 4;

        public static final int INDUSTRIAL = 5;
        public static final int VILLAGE    = 6;

        public static final int FOSSIL  = 7;
        public static final int STARTER = 8;

        public static final int LEGENDARY = 9;

        public final boolean[]      types      = new boolean[] { false, false, false, false, false, false, false, false,
                false, false, false, false, false, false, };
        /** Any matches in here is a valid location */
        public List<TypeEntry>      anyTypes   = new ArrayList<TypeEntry>();
        /** Needs all matches in here to be valid */
        public List<TypeEntry>      allTypes   = new ArrayList<TypeEntry>();
        /** Needs no matches in here to be valid */
        public List<Type>           noTypes    = new ArrayList<Type>();
        /** The global spawn rate settings */

        ArrayList<Integer>          biomeTypes = new ArrayList<Integer>();
        HashMap<Integer, TypeEntry> biomes     = new HashMap<Integer, PokedexEntry.SpawnData.TypeEntry>();

        public SpawnData()
        {
        }

        public void postInit()
        {

            biomeTypes.clear();
            biomes.clear();

            for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
            {
                if (b == null) continue;
                if (isValidInit(b.biomeID))
                {
                    addBiomeIfValid(b);
                }
            }
            biomes:
            for (BiomeType b : BiomeType.values())
            {
                if (b == null || b == NONE) continue;
                if (isValidInit(b.getType()))
                {
                    biomeTypes.add(b.getType());
                    for (TypeEntry t : anyTypes)
                    {
                        if (t.isValid(b.getType()))
                        {
                            biomes.put(b.getType(), t);
                            continue biomes;
                        }
                    }
                    for (TypeEntry t : allTypes)
                    {
                        if (t.isValid(b.getType()))
                        {
                            biomes.put(b.getType(), t);
                            continue biomes;
                        }
                    }
                }
            }
        }

        public float getWeight(int biome)
        {
            if (biomes.containsKey(biome)) { return biomes.get(biome).weight; }
            if (biomes.containsKey(BiomeType.ALL.getType())) return biomes.get(BiomeType.ALL.getType()).weight;

            return 0;
        }

        public int getMin(int biome)
        {

            if (biomes.containsKey(biome)) { return biomes.get(biome).groupMin; }
            if (biomes.containsKey(BiomeType.ALL.getType())) return biomes.get(BiomeType.ALL.getType()).groupMin;

            return 2;
        }

        public int getMax(int biome)
        {
            if (biomes.containsKey(biome)) { return biomes.get(biome).groupMax; }
            if (biomes.containsKey(BiomeType.ALL.getType())) return biomes.get(BiomeType.ALL.getType()).groupMax;
            return 4;
        }

        private boolean isValidInit(int b)
        {
            if (b < BiomeGenBase.getBiomeGenArray().length) for (Type t : noTypes)
            {
                if (BiomeDatabase.contains(BiomeGenBase.getBiomeGenArray()[b], t)) return false;
            }

            for (TypeEntry t : anyTypes)
            {
                if (t.isValid(b)) { return true; }
            }
            for (TypeEntry ta : allTypes)
            {
                if (ta.isValid(b)) { return true; }
            }

            return false;
        }

        /** Only checks one biome type for vailidity
         * 
         * @param b
         * @return */
        public boolean isValid(int b)
        {
            if (biomes.containsKey(BiomeType.ALL.getType())) return true;
            return biomes.containsKey(b);
        }

        /** Checks for cases with biome/subbiome
         * 
         * @param b
         * @param b1
         * @return */
        public boolean isValid(int biome, int subbiome)
        {
            if (biome < 0 || subbiome < 0)
            {
                // new Exception().printStackTrace();
                return false;
            }
            if (biomes.containsKey(biome))
            {
                if (biomes.containsKey(subbiome)) return true;
                for (TypeEntry t : anyTypes)
                {
                    if (t.isValid(subbiome)) { return true; }
                }
                for (TypeEntry ta : allTypes)
                {
                    if (ta.isValid(subbiome)) { return true; }
                }
                return false;
            }
            if (biomes.containsKey(subbiome) || biomeTypes.contains(subbiome))
            {
                for (TypeEntry t : anyTypes)
                {
                    boolean val = t.types.contains(subbiome) && (t.biomes.isEmpty() || t.types.contains(biome));
                    if (val) return true;
                }
                for (TypeEntry t : allTypes)
                {
                    boolean val = t.types.contains(subbiome) && (t.biomes.isEmpty() || t.types.contains(biome));
                    if (val) return true;
                }
            }
            return false;
        }

        @Override
        public String toString()
        {
            String ret = "";
            ret += System.getProperty("line.separator");
            ret += allTypes;
            ret += System.getProperty("line.separator");
            ret += anyTypes;
            ret += System.getProperty("line.separator");
            ret += noTypes;
            return ret;
        }

        private void addBiomeIfValid(BiomeGenBase biome)
        {
            biomeTypes.add(biome.biomeID);
            for (TypeEntry t : anyTypes)
            {
                if (t.isValid(biome))
                {
                    biomes.put(biome.biomeID, t);
                    return;
                }
            }
            for (TypeEntry t : allTypes)
            {
                if (t.isValid(biome))
                {
                    biomes.put(biome.biomeID, t);
                    return;
                }
            }
        }

        public TypeEntry addBiome(BiomeGenBase biome, float weight)
        {
            TypeEntry ent = new TypeEntry();
            ent.weight = weight;
            ent.valid.add(biome);
            ent.types.add(biome.biomeID);
            biomeTypes.add(biome.biomeID);
            biomes.put(biome.biomeID, ent);
            return ent;
        }

        public static class TypeEntry
        {
            ArrayList<Type>             biomes   = new ArrayList<Type>();
            ArrayList<BiomeGenBase>     valid    = new ArrayList<BiomeGenBase>();
            HashSet<Integer>            types    = new HashSet<Integer>();
            public ArrayList<BiomeType> biome2   = new ArrayList<BiomeType>();
            float                       weight;
            public int                  groupMax = 4;
            public int                  groupMin = 2;

            public TypeEntry()
            {
            };

            public boolean isValid(int biome)
            {
                if (types.contains(BiomeType.ALL.getType())) return true;

                if (types.contains(biome)) return true;
                else if (biome < BiomeGenBase.getBiomeGenArray().length && !biomes.isEmpty())
                    return isValid(BiomeGenBase.getBiomeGenArray()[biome]);

                for (BiomeType t : biome2)
                {
                    if (t.getType() == biome) types.add(biome);
                }
                if (types.contains(biome)) return true;
                if (types.contains(BiomeType.ALL.getType())) return true;

                return false;
            }

            public boolean isValid(BiomeGenBase b)
            {
                boolean ret = biomes.size() > 0;

                if (b == null) return false;

                if (valid.contains(b)) return true;

                for (Type t : biomes)
                {
                    ret = ret && BiomeDatabase.contains(b, t);
                }
                if (ret) valid.add(b);

                return ret;
            }

            @Override
            public String toString()
            {
                return biomes + " " + biome2 + " " + weight + " " + groupMin + "-" + groupMax;
            }
        }
    }

    public static class InteractionLogic
    {
        static HashMap<PokeType, String> defaults = new HashMap<>();

        static
        {
            defaults.put(PokeType.fire, "stick`torch");
            defaults.put(PokeType.water, "bucket`water_bucket");
        }

        HashMap<ItemStack, List<ItemStack>> stacks = new HashMap<ItemStack, List<ItemStack>>();

        boolean canInteract(ItemStack key)
        {
            return getKey(key) != null;
        }

        List<ItemStack> interact(ItemStack key)
        {
            return stacks.get(getKey(key));
        }

        boolean interact(EntityPlayer player, IPokemob pokemob, boolean doInteract)
        {
            EntityLiving entity = (EntityLiving) pokemob;
            NBTTagCompound data = entity.getEntityData();
            if (data.hasKey("lastInteract"))
            {
                long time = data.getLong("lastInteract");
                long diff = entity.worldObj.getTotalWorldTime() - time;
                if (diff < 100) { return false; }
            }
            ItemStack stack = getKey(player.getHeldItem());
            if (!doInteract) return stack != null;
            data.setLong("lastInteract", entity.worldObj.getTotalWorldTime());
            if (stack != null)
            {
                List<ItemStack> results = stacks.get(stack);
                int index = player.getRNG().nextInt(results.size());
                ItemStack result = results.get(index).copy();
                if (player.getHeldItem().stackSize-- == 1)
                {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
                }
                else if (!player.inventory.addItemStackToInventory(result))
                {
                    player.dropPlayerItemWithRandomChoice(result, false);
                }
                if (player != pokemob.getPokemonOwner())
                {
                    entity.setAttackTarget(player);
                }
                return true;
            }
            return false;
        }

        private ItemStack getKey(ItemStack held)
        {
            if (held != null) for (ItemStack stack : stacks.keySet())
            {
                if (stack.isItemEqual(held)) { return stack; }
            }
            return null;
        }

        protected static void initForEntry(PokedexEntry entry, String fromDatabase)
        {
            String[] entries = fromDatabase.trim().split(" ");
            for (String s : entries)
            {
                String[] args = s.split("`");
                String key = args[0];

                if (key.equals("null")) return;

                String[] vals = new String[args.length - 1];
                for (int i = 0; i < vals.length; i++)
                {
                    vals[i] = args[i + 1];
                }
                ItemStack keyStack = parseStack(key);
                List<ItemStack> stacks = Lists.newArrayList();
                for (String s1 : vals)
                {
                    ItemStack temp = parseStack(s1);
                    if (temp != null)
                    {
                        stacks.add(temp);
                    }
                }
                if (keyStack != null && !stacks.isEmpty())
                {
                    InteractionLogic interact = entry.interactionLogic;
                    interact.stacks.put(keyStack, stacks);
                }
            }
        }

        protected static void initForEntry(PokedexEntry entry)
        {
            String val = "";
            for (PokeType t : defaults.keySet())
            {
                if (entry.isType(t))
                {
                    if (val.isEmpty())
                    {
                        val = defaults.get(t);
                    }
                    else
                    {
                        val += " " + defaults.get(t);
                    }
                }
            }
            if (!val.isEmpty())
            {
                initForEntry(entry, val);
            }
        }

        private static ItemStack parseStack(String info)
        {
            ItemStack ret = null;

            String modid;
            String name;
            int damage = 0;
            if (info.contains(":"))
            {
                modid = info.split(":")[0];
                name = info.split(":")[1];
            }
            else
            {
                modid = "minecraft";
                name = info;
            }
            if (name.contains("#"))
            {
                name = info.split("#")[0];
                damage = Integer.parseInt(info.split("#")[1]);
            }
            Item item = GameRegistry.findItem(modid, name);
            if (item != null) ret = new ItemStack(item, 1, damage);
            else new NullPointerException("Errored Item for " + info);
            return ret;
        }
    }
}
