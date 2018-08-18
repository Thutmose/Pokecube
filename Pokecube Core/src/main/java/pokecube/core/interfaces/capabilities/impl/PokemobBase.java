package pokecube.core.interfaces.capabilities.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.thread.logicRunnables.LogicCollision;
import pokecube.core.ai.thread.logicRunnables.LogicFloatFlySwim;
import pokecube.core.ai.thread.logicRunnables.LogicInLiquid;
import pokecube.core.ai.thread.logicRunnables.LogicInMaterials;
import pokecube.core.ai.thread.logicRunnables.LogicMiscUpdate;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.ai.thread.logicRunnables.LogicMovesUpdates;
import pokecube.core.ai.utils.PokemobDataManager;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.animations.EntityMoveUse;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public abstract class PokemobBase implements IPokemob
{
    private static final Map<Class<? extends Entity>, DataParameters> paramsMap = Maps.newHashMap();

    public static DataParameters getParameters(Class<? extends Entity> clazz)
    {
        DataParameters params = paramsMap.get(clazz);
        if (params == null)
        {
            paramsMap.put(clazz, params = createParams(clazz));
        }
        return params;
    }

    private static DataParameters createParams(Class<? extends Entity> clazz)
    {
        DataParameters params = new DataParameters();
        for (int i = 0; i < 5; i++)
        {
            params.FLAVOURS[i] = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        }
        for (int i = 0; i < 4; i++)
        {
            params.DISABLE[i] = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        }
        params.GENERALSTATESDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.LOGICSTATESDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.COMBATSTATESDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.ATTACKTARGETIDDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HUNGERDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.STATUSDW = EntityDataManager.<Byte> createKey(clazz, DataSerializers.BYTE);
        params.STATUSTIMERDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.MOVEINDEXDW = EntityDataManager.<Byte> createKey(clazz, DataSerializers.BYTE);
        params.SPECIALINFO = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.EVOLTICKDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HAPPYDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.ATTACKCOOLDOWN = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.NICKNAMEDW = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        params.ZMOVECD = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HEADINGDW = EntityDataManager.<Float> createKey(clazz, DataSerializers.FLOAT);
        params.DIRECTIONPITCHDW = EntityDataManager.<Float> createKey(clazz, DataSerializers.FLOAT);
        params.TRANSFORMEDTODW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HELDITEM = EntityDataManager.<ItemStack> createKey(clazz, DataSerializers.ITEM_STACK);
        params.TYPE1DW = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        params.TYPE2DW = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        return params;
    }

    @SuppressWarnings("unchecked")
    public static class DataParameters
    {
        public final DataParameter<Integer>[] FLAVOURS = new DataParameter[5];
        public DataParameter<ItemStack>       HELDITEM;
        public DataParameter<Integer>         EVOLTICKDW;
        public DataParameter<Integer>         HAPPYDW;
        public DataParameter<Integer>         ATTACKCOOLDOWN;
        public DataParameter<String>          NICKNAMEDW;
        public DataParameter<Integer>         ZMOVECD;
        public DataParameter<Float>           DIRECTIONPITCHDW;
        public DataParameter<Float>           HEADINGDW;
        public DataParameter<Integer>         TRANSFORMEDTODW;
        public DataParameter<Integer>         GENERALSTATESDW;
        public DataParameter<Integer>         LOGICSTATESDW;
        public DataParameter<Integer>         COMBATSTATESDW;
        public DataParameter<Integer>         ATTACKTARGETIDDW;
        public DataParameter<Integer>         HUNGERDW;
        public DataParameter<Byte>            STATUSDW;
        public DataParameter<Integer>         STATUSTIMERDW;
        public DataParameter<Byte>            MOVEINDEXDW;
        public DataParameter<Integer>         SPECIALINFO;
        public DataParameter<String>          TYPE1DW;
        public DataParameter<String>          TYPE2DW;
        public final DataParameter<Integer>[] DISABLE  = new DataParameter[4];

        public PokemobDataManager register(EntityDataManager dataManager, EntityLiving entity)
        {
            dataManager.register(HUNGERDW, new Integer(0));// Hunger time
            // // for sheared status
            dataManager.register(NICKNAMEDW, "");// nickname
            dataManager.register(HAPPYDW, new Integer(0));// Happiness
            dataManager.register(TYPE1DW, "");// overriden type1
            dataManager.register(TYPE2DW, "");// overriden type2

            // From EntityAiPokemob
            dataManager.register(DIRECTIONPITCHDW, Float.valueOf(0));
            dataManager.register(HEADINGDW, Float.valueOf(0));
            dataManager.register(ATTACKTARGETIDDW, Integer.valueOf(-1));
            dataManager.register(GENERALSTATESDW, Integer.valueOf(0));
            dataManager.register(LOGICSTATESDW, Integer.valueOf(0));
            dataManager.register(COMBATSTATESDW, Integer.valueOf(0));

            // from EntityEvolvablePokemob
            dataManager.register(EVOLTICKDW, new Integer(0));// evolution tick

            // From EntityMovesPokemb
            dataManager.register(STATUSDW, Byte.valueOf((byte) -1));
            dataManager.register(MOVEINDEXDW, Byte.valueOf((byte) -1));
            dataManager.register(STATUSTIMERDW, Integer.valueOf(0));
            dataManager.register(ATTACKCOOLDOWN, Integer.valueOf(0));

            dataManager.register(SPECIALINFO, Integer.valueOf(-1));
            dataManager.register(TRANSFORMEDTODW, Integer.valueOf(-1));

            dataManager.register(ZMOVECD, Integer.valueOf(-1));

            // Held item sync
            dataManager.register(HELDITEM, ItemStack.EMPTY);

            // Flavours for various berries eaten.
            for (int i = 0; i < 5; i++)
            {
                dataManager.register(FLAVOURS[i], Integer.valueOf(0));
            }

            // Flavours for various berries eaten.
            for (int i = 0; i < 4; i++)
            {
                dataManager.register(DISABLE[i], Integer.valueOf(0));
            }

            PokemobDataManager manager = new PokemobDataManager(entity);
            manager.manualSyncSet.add(TRANSFORMEDTODW);
            manager.manualSyncSet.add(STATUSDW);
            manager.manualSyncSet.add(EVOLTICKDW);
            manager.manualSyncSet.add(NICKNAMEDW);
            manager.manualSyncSet.add(MOVEINDEXDW);
            manager.manualSyncSet.add(ATTACKCOOLDOWN);
            manager.manualSyncSet.add(HUNGERDW);
            manager.manualSyncSet.add(TYPE1DW);
            manager.manualSyncSet.add(TYPE2DW);
            return manager;
        }
    }

    /** Inventory of the pokemob. */
    protected AnimalChest          pokeChest;
    /** Prevents duplication on returning to pokecubes */
    protected boolean              returning        = false;
    /** Is this owned by a player? */
    protected boolean              players          = false;
    /** Cached Team for this Pokemob */
    protected String               team             = "";
    protected double               moveSpeed;
    /** Cached Pokedex Entry for this pokemob. */
    protected PokedexEntry         entry;

    /** The happiness value of the pokemob */
    protected int                  bonusHappiness   = 0;
    /** Tracks whether this was a shadow mob at some point. */
    protected boolean              wasShadow        = false;
    /** Number used as seed for various RNG things. */
    protected int                  personalityValue = 0;
    /** Modifiers on stats. */
    protected StatModifiers        modifiers        = new StatModifiers();
    /** Egg we are trying to protect. */
    protected Entity               egg              = null;
    /** Mob to breed with */
    protected Entity               lover;
    /** Timer for determining whether wants to breed, will only do so if this is
     * greater than 0 */
    protected int                  loveTimer;
    /** List of nearby male mobs to breed with */
    protected Vector<IBreedingMob> males            = new Vector<>();
    /** Simpler UID for some client sync things. */
    protected int                  uid              = -1;
    /** The pokecube this mob is "in" */
    protected ItemStack            pokecube         = ItemStack.EMPTY;
    /** Tracker for things related to moves. */
    protected PokemobMoveStats     moveInfo         = new PokemobMoveStats();
    /** The current move being used, this is used to track whether the mob can
     * launch a new move, only allows sending a new move if this returns true
     * for isDone() */
    protected EntityMoveUse        activeMove;
    /** Used for size when pathing */
    protected Vector3              sizes            = Vector3.getNewVector();
    /** Cooldown for hunger AI */
    protected int                  hungerCooldown   = 0;

    // Here we have all of the genes currently used.
    Alleles                        genesSize;
    Alleles                        genesIVs;
    Alleles                        genesEVs;
    Alleles                        genesMoves;
    Alleles                        genesNature;
    Alleles                        genesAbility;
    Alleles                        genesColour;
    Alleles                        genesShiny;
    Alleles                        genesSpecies;

    /** Stack which will be used for evolution */
    protected ItemStack            stack            = ItemStack.EMPTY;
    /** Location to try to attack. */
    protected Vector3              target;
    /** Manages mounted control */
    public LogicMountedControl     controller;
    /** Holder for all the custom AI stuff */
    protected AIStuff              aiStuff;

    /** Custom navigator */
    public PathNavigate            navi;
    /** Custom move helper. */
    public PokemobMoveHelper       mover;

    /** Used for various cases where things at mobs location need checking */
    protected Vector3              here             = Vector3.getNewVector();

    /** The Entity this IPokemob is attached to. */
    protected EntityLiving         entity;
    /** RNG used, should be entity.getRNG() */
    protected Random               rand             = new Random();
    /** Data manager used for syncing data, this should be identical to
     * entity.getDataManager() */
    public PokemobDataManager      dataManager;
    /** Holds the data parameters used for syncing our stuff. */
    protected DataParameters       params;

    /** Our owner. */
    protected UUID                 ownerID;
    /** Our original owner. */
    protected UUID                 OTID;

    /** Used for maintaining/storing homes and routes. */
    protected IGuardAICapability   guardCap;

    // Things here are used for collision stuff.
    List<AxisAlignedBB>            aabbs            = null;
    public Matrix3                 mainBox          = new Matrix3();
    public Vector3                 offset           = Vector3.getNewVector();

    /** How long the mob is */
    protected float                length;

    /** The IMobGenetics used to store our genes. */
    public IMobGenetics            genes;

    /** Used to cache current texture for quicker lookups, array to include any
     * animated textures */
    protected ResourceLocation[]   textures;

    @Override
    public void setEntity(EntityLiving entityIn)
    {
        entity = entityIn;
        if (!isSameDatamanager(entity.getDataManager()))
        {
            this.params = getParameters(entity.getClass());
            this.dataManager = params.register(entity.getDataManager(), entity);
            this.aiStuff = new AIStuff(entity);
            // Controller is done separately for ease of locating it for
            // controls.
            this.getAI().addAILogic(controller = new LogicMountedControl(this));

            // Add in the various logic AIs that are needed on both client and
            // server, so it is done here instead of in initAI.
            this.getAI().addAILogic(new LogicInLiquid(this));
            this.getAI().addAILogic(new LogicCollision(this));
            this.getAI().addAILogic(new LogicMovesUpdates(this));
            this.getAI().addAILogic(new LogicInMaterials(this));
            this.getAI().addAILogic(new LogicFloatFlySwim(this));
            this.getAI().addAILogic(new LogicMiscUpdate(this));
        }
        rand = entity.getRNG();
    }

    @Override
    public EntityLiving getEntity()
    {
        return entity;
    }

    @Override
    public EntityDataManager getDataManager()
    {
        return this.dataManager;
    }

    private boolean isSameDatamanager(EntityDataManager toTest)
    {
        if (this.dataManager == null) return false;
        return toTest == this.dataManager.wrappedManager;
    }

    protected void setMaxHealth(float maxHealth)
    {
        getEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
    }

    /** Handles health update.
     * 
     * @param level */
    public void updateHealth()
    {
        float old = getEntity().getMaxHealth();
        float maxHealth = getStat(Stats.HP, false);
        float health = getEntity().getHealth();

        if (maxHealth > old)
        {
            float damage = old - health;
            health = maxHealth - damage;

            if (health > maxHealth)
            {
                health = maxHealth;
            }
        }

        setMaxHealth(maxHealth);
        getEntity().setHealth(health);
    }
}
