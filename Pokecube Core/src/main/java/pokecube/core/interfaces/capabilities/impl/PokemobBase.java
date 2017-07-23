package pokecube.core.interfaces.capabilities.impl;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.ai.utils.AISaveHandler.PokemobAI;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatWrapper;

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

        for (int i = 0; i < 6; i++)
        {
            params.EVS[i] = EntityDataManager.<Byte> createKey(clazz, DataSerializers.BYTE);
        }
        for (int i = 0; i < 5; i++)
        {
            params.FLAVOURS[i] = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        }
        params.AIACTIONSTATESDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.ATTACKTARGETIDDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.EXPDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HUNGERDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.STATUSDW = EntityDataManager.<Byte> createKey(clazz, DataSerializers.BYTE);
        params.STATUSTIMERDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.MOVEINDEXDW = EntityDataManager.<Byte> createKey(clazz, DataSerializers.BYTE);
        params.SPECIALINFO = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.EVOLTICKDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HAPPYDW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.ATTACKCOOLDOWN = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.MOVESDW = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        params.NICKNAMEDW = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        params.LASTMOVE = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        params.BOOMSTATEDW = EntityDataManager.<Byte> createKey(clazz, DataSerializers.BYTE);
        params.ZMOVECD = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.DIRECTIONPITCHDW = EntityDataManager.<Float> createKey(clazz, DataSerializers.FLOAT);
        params.TRANSFORMEDTODW = EntityDataManager.<Integer> createKey(clazz, DataSerializers.VARINT);
        params.HELDITEM = EntityDataManager.<Optional<ItemStack>> createKey(clazz, DataSerializers.OPTIONAL_ITEM_STACK);
        return params;
    }

    @SuppressWarnings("unchecked")
    public static class DataParameters
    {
        public final DataParameter<Byte>[]        EVS      = new DataParameter[6];
        public final DataParameter<Integer>[]     FLAVOURS = new DataParameter[5];
        public DataParameter<Optional<ItemStack>> HELDITEM;
        public DataParameter<Integer>             EVOLTICKDW;
        public DataParameter<Integer>             HAPPYDW;
        public DataParameter<Integer>             ATTACKCOOLDOWN;
        public DataParameter<String>              MOVESDW;
        public DataParameter<String>              NICKNAMEDW;
        public DataParameter<String>              LASTMOVE;
        public DataParameter<Byte>                BOOMSTATEDW;
        public DataParameter<Integer>             ZMOVECD;
        public DataParameter<Float>               DIRECTIONPITCHDW;
        public DataParameter<Integer>             TRANSFORMEDTODW;
        public DataParameter<Integer>             AIACTIONSTATESDW;
        public DataParameter<Integer>             ATTACKTARGETIDDW;
        public DataParameter<Integer>             EXPDW;
        public DataParameter<Integer>             HUNGERDW;
        public DataParameter<Byte>                STATUSDW;
        public DataParameter<Integer>             STATUSTIMERDW;
        public DataParameter<Byte>                MOVEINDEXDW;
        public DataParameter<Integer>             SPECIALINFO;

        public EntityDataManager register(EntityDataManager dataManager, EntityLiving entity)
        {
            dataManager.register(EXPDW, new Integer(0));// exp for level 1
            dataManager.register(HUNGERDW, new Integer(0));// Hunger time
            // // for sheared status
            dataManager.register(NICKNAMEDW, "");// nickname
            dataManager.register(HAPPYDW, new Integer(0));// Happiness

            // From EntityAiPokemob
            dataManager.register(DIRECTIONPITCHDW, Float.valueOf(0));
            dataManager.register(ATTACKTARGETIDDW, Integer.valueOf(-1));
            dataManager.register(AIACTIONSTATESDW, Integer.valueOf(0));

            // from EntityEvolvablePokemob
            // dataManager.register(EVOLNBDW, new Integer(0));// current
            // evolution
            // nb
            dataManager.register(EVOLTICKDW, new Integer(0));// evolution tick

            // From EntityMovesPokemb
            dataManager.register(BOOMSTATEDW, Byte.valueOf((byte) -1));
            dataManager.register(STATUSDW, Byte.valueOf((byte) -1));
            dataManager.register(MOVEINDEXDW, Byte.valueOf((byte) -1));
            dataManager.register(STATUSTIMERDW, Integer.valueOf(0));
            dataManager.register(ATTACKCOOLDOWN, Integer.valueOf(0));

            dataManager.register(MOVESDW, "");
            dataManager.register(SPECIALINFO, Integer.valueOf(0));
            dataManager.register(TRANSFORMEDTODW, Integer.valueOf(-1));

            dataManager.register(LASTMOVE, "");
            dataManager.register(ZMOVECD, Integer.valueOf(-1));

            // Held item sync
            dataManager.register(HELDITEM, Optional.<ItemStack> absent());

            // Flavours for various berries eaten.
            for (int i = 0; i < 5; i++)
            {
                dataManager.register(FLAVOURS[i], Integer.valueOf(0));
            }

            // Flavours for various berries eaten.
            for (int i = 0; i < 6; i++)
            {
                dataManager.register(EVS[i], Byte.MIN_VALUE);
            }

            // TODO manual sync these? overwrite default datamanager?
            // PokemobDataManager manager = new PokemobDataManager(entity);
            // dataManager = manager;
            // manager.manualSyncSet.add(EXPDW);
            // manager.manualSyncSet.add(MOVEINDEXDW);
            // manager.manualSyncSet.add(MOVESDW);
            // manager.manualSyncSet.add(TRANSFORMEDTODW);
            // manager.manualSyncSet.add(STATUSDW);
            // manager.manualSyncSet.add(EVOLTICKDW);
            // manager.manualSyncSet.add(NICKNAMEDW);
            return dataManager;
        }
    }

    /** Inventory of the pokemob. */
    protected AnimalChest          pokeChest;
    protected boolean              named            = false;
    protected boolean              initHome         = true;
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
    protected boolean              wasShadow        = false;
    protected boolean              isAncient        = false;
    /** Number used as seed for various RNG things. */
    protected int                  personalityValue = 0;
    protected int                  killCounter      = 0;
    protected int                  resetTick        = 0;
    /** Modifiers on stats. */
    protected StatModifiers        modifiers        = new StatModifiers();
    /** Egg we are trying to protect. */
    protected Entity               egg              = null;
    /** Mob to breed with */
    protected Entity               lover;
    protected int                  loveTimer;
    protected Vector<IBreedingMob> males            = new Vector<>();
    protected int                  uid              = -1;
    protected ItemStack            pokecube         = CompatWrapper.nullStack;
    protected int[]                flavourAmounts   = new int[5];
    /** Tracker for things related to moves. */
    protected PokemobMoveStats     moveInfo         = new PokemobMoveStats();
    /** Used for size when pathing */
    protected Vector3              sizes            = Vector3.getNewVector();
    protected int                  moveIndexCounter = 0;
    /** Cooldown for hunger AI */
    protected int                  hungerCooldown   = 0;

    Alleles                        genesSize;
    Alleles                        genesIVs;
    Alleles                        genesEVs;
    Alleles                        genesMoves;
    Alleles                        genesNature;
    Alleles                        genesAbility;
    Alleles                        genesColour;
    Alleles                        genesShiny;
    Alleles                        genesSpecies;

    protected ItemStack            stack            = CompatWrapper.nullStack;

    protected GuardAI              guardAI;
    protected PokemobAIUtilityMove utilMoveAI;
    protected LogicMountedControl  controller;
    protected AIStuff              aiStuff;

    protected PokeNavigator        navi;
    protected PokemobMoveHelper    mover;
    protected boolean              initAI           = true;
    protected boolean              popped           = false;
    protected PokemobAI            aiObject;
    protected boolean              isAFish          = false;
    protected Vector3              here             = Vector3.getNewVector();
    public TerrainSegment          currentTerrain   = null;

    /** The Entity this IPokemob is attached to. */
    protected EntityLiving         entity;
    /** RNG used, should be entity.getRNG() */
    protected Random               rand;
    /** Data manager used for syncing data, this should be identical to
     * entity.getDataManager() */
    protected EntityDataManager    dataManager;
    /** Holds the data parameters used for syncing our stuff. */
    protected DataParameters       params;

    protected UUID                 ownerID;
    protected UUID                 OTID;

    int                            homeDistance;
    BlockPos                       homePos;

    List<AxisAlignedBB>            aabbs            = null;
    protected Matrix3              mainBox          = new Matrix3();
    protected Vector3              offset           = Vector3.getNewVector();

    protected float                length;
    // Essential Capabilites Objects
    /** The IMobGenetics used to store our genes. */
    public IMobGenetics            genes;

    @Override
    public void setEntity(EntityLiving entityIn)
    {
        entity = entityIn;
        if (this.dataManager != entity.getDataManager())
        {
            this.params = getParameters(entity.getClass());
            this.dataManager = params.register(entity.getDataManager(), entity);
        }
    }

    @Override
    public EntityLiving getEntity()
    {
        return entity;
    }
}
