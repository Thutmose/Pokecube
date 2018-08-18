/**
 *
 */
package pokecube.core.interfaces;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.entity.pokemobs.EntityPokemobPart;
import pokecube.core.events.AttackEvent;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasMobAIStates;
import pokecube.core.interfaces.pokemob.IHasMoves;
import pokecube.core.interfaces.pokemob.IHasOwner;
import pokecube.core.interfaces.pokemob.IHasStats;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.entity.IBreedingMob;
import thut.api.entity.IHungrymob;
import thut.api.pathing.IPathingMob;

/** @author Manchou */
public interface IPokemob extends IHasMobAIStates, IHasMoves, ICanEvolve, IHasOwner, IHasStats, IHungrymob,
        IBreedingMob, IHasCommands, IPathingMob
{
    @CapabilityInject(IPokemob.class)
    public static final Capability<IPokemob> POKEMOB_CAP = null;

    public static enum HappinessType
    {
        TIME(2, 2, 1), LEVEL(5, 3, 2), BERRY(3, 2, 1), EVBERRY(10, 5, 2), FAINT(-1, -1, -1), TRADE(0, 0, 0);

        public static void applyHappiness(IPokemob mob, HappinessType type)
        {
            int current = mob.getHappiness();
            if (type == BERRY && mob.getStatus() != STATUS_NON) { return; }
            if (type != TRADE)
            {
                if (current < 100)
                {
                    mob.addHappiness(type.low);
                }
                else if (current < 200)
                {
                    mob.addHappiness(type.mid);
                }
                else
                {
                    mob.addHappiness(type.high);
                }
            }
            else
            {
                mob.addHappiness(-(current - mob.getPokedexEntry().getHappiness()));
            }
        }

        public final int low;
        public final int mid;
        public final int high;

        private HappinessType(int low, int mid, int high)
        {
            this.low = low;
            this.mid = mid;
            this.high = high;
        }
    }

    public static enum Stats
    {
        HP, ATTACK, DEFENSE, SPATTACK, SPDEFENSE, VIT, ACCURACY, EVASION,
    }

    public static interface IStatsModifiers
    {
        /** Is the result of getModifier a percantage or a flat value?
         * 
         * @return */
        boolean isFlat();

        /** Priority of application of these stats modifiers, higher numbers go
         * later, the default modifiers (such as from growl) will be given
         * priority of 100, so set yours accordingly.
         * 
         * @return */
        int getPriority();

        /** Returns the effective value of the modifier, either a percantage, or
         * a flat amount, based on isFlat
         * 
         * @param stat
         * @return */
        float getModifier(Stats stat);

        /** Returns the raw value for the modifier, this should match whatever
         * is set in setModifier.
         * 
         * @param stat
         * @return */
        float getModifierRaw(Stats stat);

        void setModifier(Stats stat, float value);

        /** Is this modifier saved with the pokemob, and persists outside of
         * battle
         * 
         * @return */
        boolean persistant();

        default void reset()
        {
            for (Stats stat : Stats.values())
                setModifier(stat, 0);
        }
    }

    public static class StatModifiers
    {
        public static class DefaultModifiers implements IStatsModifiers
        {
            public DefaultModifiers()
            {
            }

            public float[] values = new float[Stats.values().length];

            @Override
            public boolean persistant()
            {
                return false;
            }

            @Override
            public boolean isFlat()
            {
                return false;
            }

            @Override
            public int getPriority()
            {
                return 100;
            }

            @Override
            public float getModifier(Stats stat)
            {
                return modifierToRatio((byte) values[stat.ordinal()], stat.ordinal() > 5);
            }

            @Override
            public void setModifier(Stats stat, float value)
            {
                values[stat.ordinal()] = value;
            }

            @Override
            public float getModifierRaw(Stats stat)
            {
                return values[stat.ordinal()];
            }

            public float modifierToRatio(byte mod, boolean accuracy)
            {
                float modifier = 1;
                if (mod == 0) modifier = 1;
                else if (mod == 1) modifier = !accuracy ? 1.5f : 4 / 3f;
                else if (mod == 2) modifier = !accuracy ? 2 : 5 / 3f;
                else if (mod == 3) modifier = !accuracy ? 2.5f : 2;
                else if (mod == 4) modifier = !accuracy ? 3 : 7 / 3f;
                else if (mod == 5) modifier = !accuracy ? 3.5f : 8 / 3f;
                else if (mod == 6) modifier = !accuracy ? 4 : 3;
                else if (mod == -1) modifier = !accuracy ? 2 / 3f : 3 / 4f;
                else if (mod == -2) modifier = !accuracy ? 1 / 2f : 3 / 5f;
                else if (mod == -3) modifier = !accuracy ? 2 / 5f : 3 / 6f;
                else if (mod == -4) modifier = !accuracy ? 1 / 3f : 3 / 7f;
                else if (mod == -5) modifier = !accuracy ? 2 / 7f : 3 / 8f;
                else if (mod == -6) modifier = !accuracy ? 1 / 4f : 3 / 9f;
                return modifier;
            }
        };

        public static final String                                  DEFAULTMODIFIERS = "default";
        public static Map<String, Class<? extends IStatsModifiers>> modifierClasses  = Maps.newHashMap();

        static
        {
            modifierClasses.put(DEFAULTMODIFIERS, DefaultModifiers.class);
        }

        public static void registerModifier(String name, Class<? extends IStatsModifiers> modclass)
        {
            if (!modifierClasses.containsKey(name)) modifierClasses.put(name, modclass);
            else throw new IllegalArgumentException(name + " is already registered as a modifier.");
        }

        final Map<String, IStatsModifiers> modifiers       = Maps.newHashMap();
        public List<IStatsModifiers>       sortedModifiers = Lists.newArrayList();
        public Map<String, Integer>        indecies        = Maps.newHashMap();
        /** This are types which may be modified via abilities or moves. */
        public PokeType                    type1, type2;
        DefaultModifiers                   defaultmods;

        public StatModifiers()
        {
            for (String s : modifierClasses.keySet())
            {
                try
                {
                    modifiers.put(s, modifierClasses.get(s).newInstance());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            defaultmods = getModifiers(DEFAULTMODIFIERS, DefaultModifiers.class);
            sortedModifiers.addAll(modifiers.values());
            Collections.sort(sortedModifiers, new Comparator<IStatsModifiers>()
            {
                @Override
                public int compare(IStatsModifiers o1, IStatsModifiers o2)
                {
                    int comp = o1.getPriority() - o2.getPriority();
                    if (comp == 0)
                    {
                        comp = o1.getClass().getName().compareTo(o2.getClass().getName());
                    }
                    return comp;
                }
            });
            outer:
            for (int i = 0; i < sortedModifiers.size(); i++)
            {
                for (String key : modifiers.keySet())
                {
                    if (modifiers.get(key) == sortedModifiers.get(i))
                    {
                        indecies.put(key, i);
                        continue outer;
                    }
                }
            }
        }

        public float getStat(IHasStats pokemob, Stats stat, boolean modified)
        {
            if (modified && stat == Stats.HP) { return pokemob.getEntity().getHealth(); }
            int index = stat.ordinal();
            byte nature = 0;
            if (index < 6) nature = pokemob.getNature().stats[index];
            float natureMod = (nature * 10f + 100) / 100f;
            int baseStat = pokemob.getBaseStat(stat);
            float actualStat = 1;
            if (index < 6)
            {
                int IV = pokemob.getIVs()[stat.ordinal()];
                int EV = pokemob.getEVs()[stat.ordinal()] - Byte.MIN_VALUE;
                int level = pokemob.getLevel();
                if (stat == Stats.HP)
                {
                    if (baseStat != 1)
                    {
                        actualStat = level + 10 + (2 * baseStat + IV + EV / 4) * level / 100;
                    }
                    else actualStat = 1;
                }
                else
                {
                    actualStat = 5 + level * (2 * baseStat + IV + EV / 4) / 100;
                    actualStat *= natureMod;
                }
            }
            if (modified) for (IStatsModifiers mods : sortedModifiers)
            {
                if (mods.isFlat()) actualStat += mods.getModifier(stat);
                else actualStat *= mods.getModifier(stat);
            }
            return actualStat;
        }

        public IStatsModifiers getModifiers(String name)
        {
            return modifiers.get(name);
        }

        public <T extends IStatsModifiers> T getModifiers(String name, Class<T> type)
        {
            return type.cast(modifiers.get(name));
        }

        public DefaultModifiers getDefaultMods()
        {
            return defaultmods;
        }

        public void outOfCombatReset()
        {
            defaultmods.reset();
            for (IStatsModifiers mods : sortedModifiers)
            {
                if (!mods.persistant()) mods.reset();
            }
        }
    }

    public static class MovePacket
    {
        public IPokemob      attacker;
        public Entity        attacked;
        public String        attack;
        public PokeType      attackType;
        public int           PWR;
        public int           criticalLevel;
        public byte          statusChange;
        public byte          changeAddition;
        public float         stabFactor        = 1.5f;
        public float         critFactor        = 1.5f;
        public boolean       stab              = false;
        public boolean       hit               = false;
        public int           damageDealt       = 0;
        /** Is the move packet before of after damage is done */
        public final boolean pre;
        /** Detect, Protect, wonder guard will set this true. */
        public boolean       canceled          = false;
        /** Did the move crit */
        public boolean       didCrit           = false;
        /** False swipe, sturdy ability and focus items would set this true. */
        public boolean       noFaint           = false;
        /** Used in the protection moves, accounts their accuracy via this
         * variable */
        public boolean       failed            = false;
        /** Move has failed for some unspecified reason, will not give failure
         * message, will not process past preAttack */
        public boolean       denied            = false;
        /** does target get infatuated */
        public boolean       infatuateTarget   = false;
        /** does attacker get infatuated */
        public boolean       infatuateAttacker = false;
        /** Whether or not to apply ongoing, this can be set to false to use
         * these during ongoing effects */
        public boolean       applyOngoing      = true;
        /** Stat modifications for target */
        public int[]         attackedStatModification;
        /** Stat modifications for attacker */
        public int[]         attackerStatModification;
        /** Stat modifications chance for target */
        public float         attackedStatModProb;
        /** Stat modifications chance for attacker */
        public float         attackerStatModProb;
        /** modifies supereffectiveness */
        public float         superEffectMult   = 1;
        /** Stat multpliers */
        public float[]       statMults         = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };

        public MovePacket(IPokemob attacker, Entity attacked, Move_Base move)
        {
            this(attacker, attacked, move.name, move.getType(attacker), move.getPWR(), move.move.crit,
                    move.move.statusChange, move.move.change);
        }

        public MovePacket(IPokemob attacker, Entity attacked, String attack, PokeType type, int PWR, int criticalLevel,
                byte statusChange, byte changeAddition)
        {
            this(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition, true);
        }

        public MovePacket(IPokemob attacker, Entity attacked, String attack, PokeType type, int PWR, int criticalLevel,
                byte statusChange, byte changeAddition, boolean pre)
        {
            this.attacker = attacker;
            this.attacked = attacked;
            this.attack = attack;
            this.attackType = type;
            this.PWR = PWR;
            this.criticalLevel = criticalLevel;
            this.statusChange = statusChange;
            this.changeAddition = changeAddition;
            this.pre = pre;
            Move_Base move = getMove();
            this.attackedStatModification = move.move.attackedStatModification;
            this.attackerStatModification = move.move.attackerStatModification;
            this.attackedStatModProb = move.move.attackedStatModProb;
            this.attackerStatModProb = move.move.attackerStatModProb;

            PokecubeCore.MOVE_BUS.post(new AttackEvent(this));
        }

        public Move_Base getMove()
        {
            return MovesUtils.getMoveFromName(attack);
        }

    }

    public static class PokemobMoveStats
    {
        private static final PokemobMoveStats defaults = new PokemobMoveStats();
        private static final Set<String>      IGNORE   = Sets.newHashSet();
        static
        {
            IGNORE.add("ongoingEffects");
            IGNORE.add("moves");
            IGNORE.add("newMoves");
            IGNORE.add("num");
            IGNORE.add("exp");
            IGNORE.add("disableTimers");
        }
        public Entity       weapon1;

        public Entity       weapon2;

        public Entity       infatuateTarget;

        // Timers used for various move types.
        public int          TOXIC_COUNTER              = 0;
        public int          ROLLOUTCOUNTER             = 0;
        public int          FURYCUTTERCOUNTER          = 0;
        public int          DEFENSECURLCOUNTER         = 0;

        public boolean      Exploding                  = false;
        public int          boomState                  = -1;

        public int          SPECIALCOUNTER             = 0;
        /** Used for cooldown of crit chance moves */
        public int          SPECIALTYPE                = 0;

        /** Used for moves such as bide/counter/mirror coat */
        public int          PHYSICALDAMAGETAKENCOUNTER = 0;
        public int          SPECIALDAMAGETAKENCOUNTER  = 0;

        /** Number of times detect, protect or similar has worked. */
        public int          BLOCKCOUNTER               = 0;
        public int          blockTimer                 = 0;
        public boolean      blocked                    = false;

        public boolean      biding                     = false;

        public float        substituteHP               = 0;

        public int          changes                    = CHANGE_NONE;

        /** Time when this creeper was last in an active state (Messed up code
         * here, probably causes creeper animation to go weird) */
        public int          lastActiveTime;

        /** Entity ID of the mob we are transformed to */
        public int          transformedTo              = -1;

        /** The amount of time since the creeper was close enough to the player
         * to ignite */
        public int          timeSinceIgnited;
        public int          fuseTime                   = 30;

        /** The Previous lvl, used to determine which moves to try to learn. */
        public int          oldLevel                   = 0;

        /** The array of moves. */
        public String[]     moves                      = new String[4];
        /** Moves it is trying to learn. */
        public List<String> newMoves                   = Lists.newArrayList();
        /** Index of new move to learn from newMoves. */
        public int          num                        = 0;
        /** The last move we used. */
        public String       lastMove;
        /** Storing exp in here as well. */
        public int          exp                        = 0;

        public void reset()
        {
            for (Field f : getClass().getFields())
            {
                try
                {
                    if (!IGNORE.contains(f.getName())) f.set(this, f.get(defaults));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Genders of pokemobs
     */
    byte MALE         = 1;

    byte FEMALE       = 2;

    byte NOSEXE       = -1;

    byte SEXLEGENDARY = -2;

    int  TYPE_CRIT    = 2;

    /** Whether this mob can use the item HMDive to be ridden underwater.
     * 
     * @return whether this mob can be ridden with HMDive */
    default boolean canUseDive()
    {
        return (getPokedexEntry().shouldDive && PokecubeCore.core.getConfig().diveEnabled && canUseSurf());
    }

    /** Whether this mob can use the item HMFly to be ridden in the air.
     * 
     * @return whether this mob can be ridden with HMFly */
    default boolean canUseFly()
    {
        return (getPokedexEntry().shouldFly || getPokedexEntry().flys()) && !isGrounded();
    }

    /** Whether this mob can use the item HMSurf to be ridden on water.
     * 
     * @return whether this mob can be ridden with HMSurf */
    default boolean canUseSurf()
    {
        return getPokedexEntry().shouldSurf || getPokedexEntry().shouldDive || getPokedexEntry().swims()
                || isType(PokeType.getType("water"));
    }

    void eat(Entity eaten);

    /** See IMultiplePassengerEntity.getPitch() TODO remove this infavour of the
     * IMultiplePassengerentity implementation
     * 
     * @return */
    float getDirectionPitch();

    /** The evolution tick will be set when the mob evolves and then is
     * decreased each tick. It is used to render a special effect.
     * 
     * @return the evolutionTicks */
    int getEvolutionTicks();

    /** 1 for about to explode, -1 for not exploding, this should probably be
     * changed to a boolean. */
    int getExplosionState();

    /** @return how happy is the pokemob, see {@link HappinessType} */
    int getHappiness();

    BlockPos getHome();

    float getHomeDistance();

    default double getMovementSpeed()
    {
        return getEntity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
    }

    boolean getOnGround();

    AnimalChest getPokemobInventory();

    /** Returns the name to display in any GUI. Can be the nickname or the
     * Pokemob translated name.
     *
     * @return the name to display */
    default ITextComponent getPokemonDisplayName()
    {
        if (this.getPokemonNickname().isEmpty())
            return new TextComponentTranslation(getPokedexEntry().getUnlocalizedName());
        return new TextComponentString(this.getPokemonNickname());
    }

    int getPokemonUID();

    /** {@link #MALE} or {@link #FEMALE} or {@link #NOSEXE}
     *
     * @return the byte sexe */
    byte getSexe();

    default SoundEvent getSound()
    {
        return getPokedexEntry().getSoundEvent();
    }

    /** Currently used for mareep colour, can be used for other things if needed
     * 
     * @return */
    int getSpecialInfo();

    /** Statuses: {@link IMoveConstants#STATUS_PSN} for example.
     *
     * @return the status */
    byte getStatus();

    /** The timer for SLP. When reach 0, the mob wakes up.
     * 
     * @return the actual value of the timer. */
    short getStatusTimer();

    /** Returns the texture path.
     * 
     * @return */
    @SideOnly(Side.CLIENT)
    ResourceLocation getTexture();

    boolean hasHomeArea();

    /** Removes the current status. */
    void healStatus();

    /** Returns modified texture to account for shininess, animation, etc.
     * 
     * @return */
    @SideOnly(Side.CLIENT)
    ResourceLocation modifyTexture(ResourceLocation texture);

    /** This method should only be used to update any Alleles objects that are
     * stored for the mob's genes. */
    default void onGenesChanged()
    {

    }

    /** Called to init the mob after it went out of its pokecube. */
    void popFromPokecube();

    /** The mob returns to its pokecube. */
    void returnToPokecube();

    void setDirectionPitch(float pitch);

    /** Sets the experience.
     *
     * @param exp
     * @param notifyLevelUp
     *            should be false in an initialize step and true in a true exp
     *            earning */
    default IPokemob setForSpawn(int exp)
    {
        return setForSpawn(exp, true);
    }

    IPokemob setForSpawn(int exp, boolean evolve);

    /** 1 for about to explode, -1 for reset.
     * 
     * @param i */
    void setExplosionState(int i);

    default void setHeldItem(ItemStack stack)
    {
        getEntity().setHeldItem(EnumHand.MAIN_HAND, stack);
    }

    default ItemStack getHeldItem()
    {
        return getEntity().getHeldItemMainhand();
    }

    /** Sets the default home location and roam distance. This is probably
     * better managed via the IGuardAICapability.
     * 
     * @param x
     * @param y
     * @param z
     * @param distance */
    void setHome(int x, int y, int z, int distance);

    /** {@link #MALE} or {@link #FEMALE} or {@link #NOSEXE}
     *
     * @param sexe
     *            the byte sexe */
    void setSexe(byte sexe);

    void setShiny(boolean shiny);

    /** first 4 bits are used for colour, can be used for other things if needed
     * 
     * @return */
    void setSpecialInfo(int info);

    /** Called when the mob spawns naturally. Used to set held item for
     * example. */
    IPokemob specificSpawnInit();

    /** Returns the held item this pokemob should have when found wild.
     * 
     * @param mob
     * @return */
    default ItemStack wildHeldItem(EntityLiving mob)
    {
        return this.getPokedexEntry().getRandomHeldItem(mob);
    }

    /** The personality value for the pokemob, used to determine nature,
     * ability, etc.<br>
     * http://bulbapedia.bulbagarden.net/wiki/Personality_value
     * 
     * @return */
    int getRNGValue();

    /** sets the personality value for the pokemob, see getRNGValue() */
    void setRNGValue(int value);

    default void setSubParts(EntityPokemobPart[] subParts)
    {

    }

    /** @param index
     * @return the value of the flavour amount for this mob, this will be used
     *         for particle effects, and possibly for boosts based on how much
     *         the mob likes the flavour */
    int getFlavourAmount(int index);

    /** Sets the flavour amount for that index.
     * 
     * @param index
     * @param amount */
    void setFlavourAmount(int index, int amount);

    void readPokemobData(NBTTagCompound tag);

    NBTTagCompound writePokemobData();

    /** If this is larger than 0, the pokemob shouldn't be allowed to attack. */
    int getAttackCooldown();

    /** Sets the value obtained by getAttackCooldown() */
    void setAttackCooldown(int timer);

    /** Last attack used by this pokemob; */
    String getLastMoveUsed();

    default boolean moveToShoulder(EntityPlayer player)
    {
        return false;
    }

    default EntityDataManager getDataManager()
    {
        return getEntity().getDataManager();
    }

    boolean isGrounded();
}
