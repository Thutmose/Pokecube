/**
 *
 */
package pokecube.core.interfaces;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

/** @author Manchou */
public interface IPokemob extends IMoveConstants
{
    /*
     * Genders of pokemobs
     */
    byte MALE         = 1;
    byte FEMALE       = 2;
    byte NOSEXE       = -1;
    byte SEXLEGENDARY = -2;

    int TYPE_CRIT = 2;

    /** Called to init the mob after it went out of its pokecube. */
    void popFromPokecube();

    /** Called when the mob spawns naturally. Used to set held item for
     * example. */
    void specificSpawnInit();

    /** Sets the pokecube id to know whether its a greatcube, ultracube...
     * 
     * @param pokeballId */
    void setPokecubeId(int pokeballId);

    /** Returns the pokecube id to know whether its a greatcube, ultracube...
     * 
     * @return the shifted index of the item */
    int getPokecubeId();

    /** Pokecube catch rate.
     *
     * @return the catch rate */
    int getCatchRate();

    /** Evolve the pokemob.
     *
     * @param showAnimation
     *            true if we want to display the evolution animation
     * @return the evolution or null if the evolution failed */
    IPokemob evolve(boolean showAnimation);

    /** Evolve the pokemob via handed item.
     *
     * @param showAnimation
     *            true if we want to display the evolution animation
     * @return the evolution or null if the evolution failed */
    IPokemob evolve(boolean showAnimation, ItemStack item);

    /** Called when give item. to override when the pokemob evolve with a stone.
     *
     * @param itemId
     *            the shifted index of the item
     * @return whether should evolve */
    boolean canEvolve(ItemStack stack);

    /** Allows to set the evolution in some specific rare case.
     * 
     * @param name
     *            of the entity this mob should evolve to */
    void setEvolution(String evolution);

    /** The evolution tick will be set when the mob evolves and then is
     * decreased each tick. It is used to render a special effect.
     * 
     * @param evolutionTicks
     *            the evolutionTicks to set */
    void setEvolutionTicks(int evolutionTicks);

    /** The evolution tick will be set when the mob evolves and then is
     * decreased each tick. It is used to render a special effect.
     * 
     * @return the evolutionTicks */
    int getEvolutionTicks();

    /** from wolf code */
    EntityLivingBase getPokemonOwner();

    /** from wolf code */
    void setPokemonOwner(EntityLivingBase e);

    /** from wolf code */
    String getPokemonOwnerName();

    /** @return UUID of original Trainer, used to prevent nicknaming of traded
     *         pokemobs */
    UUID getOriginalOwnerUUID();

    /** Sets owner uuid
     * 
     * @param original
     *            trainer's UUID */
    void setOriginalOwnerUUID(UUID original);

    /** from wolf code */
    void setPokemonOwnerByName(String s);

    /** @param state
     * @return the value of the AI state state. */
    boolean getPokemonAIState(int state);

    /*
     * Sets AI state state to flag.
     */
    void setPokemonAIState(int state, boolean flag);

    @SideOnly(Side.CLIENT)
    /** from wolf code
     *
     * @return the float interested angle */
    float getInterestedAngle(float f);

    @SideOnly(Side.CLIENT)
    /** from wolf code
     *
     * @return the float shake angle */
    float getShakeAngle(float f, float f1);

    /** Returns 1st type.
     * 
     * @see PokeType
     * @return the byte type */
    PokeType getType1();

    /** Returns 2nd type.
     * 
     * @see PokeType
     * @return the byte type */
    PokeType getType2();

    /** Displays a message in the console of the owner player (if this pokemob
     * is tamed).
     * 
     * @param message */
    void displayMessageToOwner(String message);

    /** The mob returns to its pokecube. */
    void returnToPokecube();

    /** Computes an attack strength from stats. Only used against non-poke-mobs.
     *
     * @return the attack strength */
    float getAttackStrength();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stats */
    int[] getBaseStats();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the pokedex stats */
    int[] getActualStats();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return set stats, for use with moves that cause different effects */
    void setStats(int[] stats);

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evs
     *            the Effort Values */
    void setEVs(byte[] evs);

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the Effort Values */
    byte[] getEVs();

    /** At the end of a fight as a XP. {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evsToAdd
     *            the Effort Values to add */
    void addEVs(byte[] evsToAdd);

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @param evs
     *            the Individual Values */
    void setIVs(byte[] ivs);

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT}
     *
     * @return the Individual Values */
    byte[] getIVs();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT, Accuracy, Evasion} note: HP is never
     * used.
     * 
     * @return the Modifiers on stats */
    byte[] getModifiers();

    /** {HP, ATT, DEF, ATTSPE, DEFSPE, VIT, Accuracy, Evasion}
     *
     * @return the Modifiers on stats */
    void setModifiers(byte[] modifiers);

    /** @return the level 1-100 */
    int getLevel();

    /** Called when the level is up. Should be overridden to handle level up
     * events like evolution or move learning.
     * 
     * @param level
     *            the new level */
    void levelUp(int level);

    /** @return the int pokedex number */
    int getPokedexNb();

    /** @return all the experience */
    int getExp();

    /** Sets the experience.
     *
     * @param exp
     * @param notifyLevelUp
     *            should be false in an initialize step and true in a true exp
     *            earning
     * @param newlySpawned
     *            true if called by a spawner, false otherwise */
    void setExp(int exp, boolean notifyLevelUp, boolean newlySpawned);

    /** To compute exp at the end of a fight.
     *
     * @return in base XP */
    int getBaseXP();

    /** 0, 1, 2, or 3 {@link Tools#xpToLevel(int, int)}
     *
     * @return in evolution mode */
    int getExperienceMode();

    /** {@link #MALE} or {@link #FEMALE} or {@link #NOSEXE}
     *
     * @return the byte sexe */
    byte getSexe();

    /** {@link #MALE} or {@link #FEMALE} or {@link #NOSEXE}
     *
     * @param sexe
     *            the byte sexe */
    void setSexe(byte sexe);

    /** Will be called by the mother before she lays to know what baby to put in
     * the egg.
     *
     * @param male
     *            the male
     * @return the pokedex number of the child */
    int getChildSpecies(IPokemob male);

    /** Which entity is this pokemob trying to breed with
     * 
     * @return */
    Entity getLover();

    /** Sets the entity to try to breed with
     * 
     * @param lover */
    void setLover(Entity lover);

    /** resets the status of being in love */
    void resetLoveStatus();

    /** Returns the name to display in any GUI. Can be the nickname or the
     * Pokemob translated name.
     *
     * @return the name to display */
    String getPokemonDisplayName();

    /** Sets the nickname */
    void setPokemonNickname(String nickname);

    /** @return the String nickname */
    String getPokemonNickname();

    /** Gets the {@link String} id of the specified move.
     *
     * @param i
     *            from 0 to 3
     * @return the String name of the move */
    String getMove(int i);

    /** Sets the {@link String} id of the specified move.
     *
     * @param i
     *            from 0 to 3
     * @param moveName */
    void setMove(int i, String moveName);

    /** Used by Gui Pokedex. Exchange the two moves.
     *
     * @param moveIndex0
     *            index of 1st move
     * @param moveIndex1
     *            index of 2nd move */
    void exchangeMoves(int moveIndex0, int moveIndex1);

    /** The pokemob learns the specified move. It will be set to an available
     * position or erase an existing one if non are available.
     *
     * @param moveName
     *            an existing move (registered in {@link MovesUtils}) */
    void learn(String moveName);

    /** Returns all the 4 available moves name.
     *
     * @return an array of 4 {@link String} */
    String[] getMoves();

    /** Called by attackEntity(Entity entity, float f). Executes the move it's
     * supposed to do according to his trainer command or a random one if it's
     * wild.
     * 
     * @param target
     *            the Entity to attack
     * @param f
     *            the float parameter of the attackEntity method */
    void executeMove(Entity target, Vector3 targetLocation, float f);

    /** Returns the index of the move to be executed in executeMove method.
     * 
     * @return the index from 0 to 3; */
    public int getMoveIndex();

    /** Sets the move index.
     * 
     * @param i
     *            must be a value from 0 to 3 */
    public void setMoveIndex(int i);

    /** @return the {@link PokedexEntry} of the species of this Pokemob */
    PokedexEntry getPokedexEntry();

    /** @return the {@link PokedexEntry} of the species of this Pokemob */
    void setPokedexEntry(PokedexEntry newEntry);

    /** Removes the current status. */
    void healStatus();

    /** Statuses: {@link IMoveConstants#STATUS_PSN} for example. The set can
     * fail because the mob is immune against this status (a fire-type Pokemon
     * can't be burned for example) or because it already have a status. If so,
     * the method returns false.
     * 
     * @param status
     *            the status to set
     * @return whether the status has actually been set */
    boolean setStatus(byte status);

    /** Statuses: {@link IMoveConstants#STATUS_PSN} for example.
     *
     * @return the status */
    byte getStatus();

    /** Sets the initial status timer. The timer will be decreased until 0. The
     * timer for SLP. When reach 0, the mob wakes up.
     * 
     * @param timer
     *            the initial value to set */
    void setStatusTimer(short timer);

    /** The timer for SLP. When reach 0, the mob wakes up.
     * 
     * @return the actual value of the timer. */
    short getStatusTimer();

    /** @param change
     *            the changes to set */
    void removeChanges(int changes);

    /** Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example.
     *
     * @return the change state */
    int getChanges();

    /** Changes: {@link IMoveConstants#CHANGE_CONFUSED} for example. The set can
     * fail because the mob is immune against this change or because it already
     * has the change. If so, the method returns false.
     * 
     * @param change
     *            the change to add
     * @return whether the change has actually been added */
    boolean addChange(int change);

    /** Computes and returns an integer with protected properties (pokedexNb,
     * stats, moves...).
     * 
     * @return the checkSum for this mob */
    int computeCheckSum();

    /** Whether this mob can use the item HMSurf to be ridden on water.
     * 
     * @return whether this mob can be ridden with HMSurf */
    boolean canUseSurf();

    /** Whether this mob can use the item HMDive to be ridden underwater.
     * 
     * @return whether this mob can be ridden with HMDive */
    boolean canUseDive();

    /** Whether this mob can use the item HMFly to be ridden in the air.
     * 
     * @return whether this mob can be ridden with HMFly */
    boolean canUseFly();

    /** Returns the texture path.
     * 
     * @return */
    String getTexture();

    /** Has pokemob been traded
     * 
     * @return */
    boolean traded();

    void setTraded(boolean traded);

    /** Sets a Move_Base and as an ongoing effect for moves which cause effects
     * over time
     * 
     * @param effect */
    void addOngoingEffect(Move_Base effect);

    /** List of modifiers of natures
     * 
     * @return */
    byte[] getNatureModifiers();

    /** This is called during move use to both the attacker and the attacked
     * entity, in that order. This can be used to add in abilities, In
     * EntityMovesPokemob, this is used for accounting for moves like curse,
     * detect, protect, etc, moves which either have different effects per
     * pokemon type, or moves that prevent damage.
     * 
     * @param move */
    void onMoveUse(MovePacket move);

    /** {@link IMoveConstants#HARDY} for an example of a nature byte
     * 
     * @return the nature */
    byte getNature();

    /** Sets the pokemobs's nature {@link IMoveConstants#HARDY} for an example
     * of a nature byte
     * 
     * @param nature */
    void setNature(byte nature);

    /** Returns the held item this pokemob should have when found wild.
     * 
     * @return */
    ItemStack wildHeldItem();

    /**
     * 
     * @return how happy is the pokemob, see {@link HappinessType}
     */
    int getHappiness();

    /**
     * 
     * @return rgb colours in that order.
     */
    byte[] getColours();

    void setColours(byte[] colours);

    boolean isShiny();

    void setShiny(boolean shiny);

    float getSize();

    void setSize(float size);
    /**
     * 
     * adds to how happy is the pokemob, see {@link HappinessType}
     */
    void addHappiness(int toAdd);

    boolean isShadow();

    void setShadow(boolean toSet);

    boolean isAncient();

    void setAncient(boolean toSet);

    int getPokemonUID();

    IPokemob megaEvolve(String forme);

    IPokemob changeForme(String forme);

    void eat(Entity eaten);

    Entity getTransformedTo();

    void setTransformedTo(Entity to);

    boolean isType(PokeType type);

    boolean attackEntityFrom(DamageSource generic, float damage);

    void setHp(float min);

    boolean getOnGround();

    void setDirectionPitch(float pitch);

    float getDirectionPitch();

    double getWeight();

    HashMap<Move_Ongoing, Integer> getOngoingEffects();

    AnimalChest getPokemobInventory();

    boolean hasHomeArea();

    BlockPos getHome();

    void setHome(int x, int y, int z, int distance);

    float getHomeDistance();

    void setHeldItem(ItemStack Item);

    EntityAIBase getGuardAI();

    Team getPokemobTeam();

    /** Used by moves such as vine whip to set the pokemob as using something.
     * 
     * @param index
     * @param weapon */
    void setWeapon(int index, Entity weapon);

    Entity getWeapon(int index);

    String getSound();

    void setLeaningMoveIndex(int num);

    PokemobMoveStats getMoveStats();

    int getExplosionState();

    void setExplosionState(int i);

    double getMovementSpeed();

    /** Currently used for mareep colour, can be used for other things if needed
     * 
     * @return */
    int getSpecialInfo();

    /** first 4 bits are used for colour, can be used for other things if needed
     * 
     * @return */
    void setSpecialInfo(int info);

    public static enum HappinessType
    {
        TIME(2, 2, 1), LEVEL(5, 3, 2), BERRY(3, 2, 1), EVBERRY(10, 5, 2), FAINT(-1, -1, -1), TRADE(0, 0, 0);

        final int low;
        final int mid;
        final int high;

        private HappinessType(int low, int mid, int high)
        {
            this.low = low;
            this.mid = mid;
            this.high = high;
        }

        public static void applyHappiness(IPokemob mob, HappinessType type)
        {
            int current = mob.getHappiness();

            if (type == BERRY && mob.getStatus() != STATUS_NON) { return; }

            if (type != TRADE) if (current < 100)
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
            else
            {
                mob.addHappiness(-(current - mob.getPokedexEntry().getHappiness()));
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
        public double        stabFactor = 1.5;
        public boolean       hit        = false;
        /** Is the move packet before of after damage is done */
        public final boolean pre;
        /** Detect, Protect, wonder guard will set this true. */
        public boolean       canceled   = false;

        /** False swipe, sturdy ability and focus items would set this true. */
        public boolean noFaint = false;

        /** Used in the protection moves, accounts their accuracy via this
         * variable */
        public boolean failed = false;

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
        }

        public Move_Base getMove()
        {
            return Move_Base.instance.getMove(attack);
        }

    }

    public static class PokemobMoveStats
    {
        public Entity weapon1;
        public Entity weapon2;

        public static final int TYPE_CRIT = 2;

        public int     TOXIC_COUNTER      = 0;
        public int     ROLLOUTCOUNTER     = 0;
        public int     FURYCUTTERCOUNTER  = 0;
        public int     DEFENSECURLCOUNTER = 0;
        public boolean Exploding          = false;

        public int SPECIALCOUNTER   = 0;
        /** Used for cooldown of self stat raising moves */
        public int SELFRAISECOUNTER = 0;
        /** Used for cooldown of crit chance moves */
        public int SPECIALTYPE      = 0;

        /** Used for cooldown of stat lowering moves */
        public int TARGETLOWERCOUNTER = 0;

        /** Used for moves such as bide/counter/mirror coat */
        public int PHYSICALDAMAGETAKENCOUNTER = 0;
        public int SPECIALDAMAGETAKENCOUNTER  = 0;

        /** Number of times detect, protect or similar has worked. */
        public int     BLOCKCOUNTER = 0;
        public int     blockTimer   = 0;
        public boolean blocked      = false;

        public boolean biding = false;

        public float substituteHP = 0;

        public Ability                        ability;
        /** Moves which have on-going effects, like leech seed, firespin, bind,
         * etc */
        public HashMap<Move_Ongoing, Integer> ongoingEffects = new HashMap<Move_Ongoing, Integer>();

        public int changes = CHANGE_NONE;

        /** Time when this creeper was last in an active state (Messed up code
         * here, probably causes creeper animation to go weird) */
        public int lastActiveTime;

        /** The amount of time since the creeper was close enough to the player
         * to ignite */
        public int timeSinceIgnited;
        public int fuseTime = 30;
        public int num      = 0;
        public int newMoves = 0;
    }
}
