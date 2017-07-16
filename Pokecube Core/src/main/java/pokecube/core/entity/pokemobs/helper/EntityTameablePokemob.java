/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.utils.PokemobDataManager;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.events.MoveMessageEvent;
import pokecube.core.events.PCEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.TagNames;
import thut.api.entity.IBreedingMob;
import thut.api.entity.IHungrymob;
import thut.api.entity.IMobColourable;
import thut.api.entity.ai.IAIMob;
import thut.api.maths.Vector3;
import thut.api.pathing.IPathingMob;
import thut.lib.CompatWrapper;

/** @author Manchou */
public abstract class EntityTameablePokemob extends EntityAnimal
        implements IPokemob, IMob, IInventoryChangedListener, IHungrymob, IPathingMob, IShearable, IBreedingMob,
        IMobColourable, IRangedAttackMob, IEntityOwnable, IAIMob, IEntityAdditionalSpawnData
{
    public static int                          EXITCUBEDURATION = 40;

    static final DataParameter<Integer>        AIACTIONSTATESDW = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        ATTACKTARGETIDDW = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        EXPDW            = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        HUNGERDW         = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Byte>           STATUSDW         = EntityDataManager
            .<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE);
    static final DataParameter<Integer>        STATUSTIMERDW    = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Byte>           MOVEINDEXDW      = EntityDataManager
            .<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE);
    static final DataParameter<Integer>        SPECIALINFO      = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        EVOLNBDW         = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        EVOLTICKDW       = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        HAPPYDW          = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer>        ATTACKCOOLDOWN   = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);

    static final DataParameter<String>         MOVESDW          = EntityDataManager
            .<String> createKey(EntityTameablePokemob.class, DataSerializers.STRING);
    static final DataParameter<String>         NICKNAMEDW       = EntityDataManager
            .<String> createKey(EntityTameablePokemob.class, DataSerializers.STRING);
    static final DataParameter<String>         LASTMOVE         = EntityDataManager
            .<String> createKey(EntityTameablePokemob.class, DataSerializers.STRING);

    static final DataParameter<Byte>           BOOMSTATEDW      = EntityDataManager
            .<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE);
    static final DataParameter<Integer>        ZMOVECD          = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);

    static final DataParameter<Float>          DIRECTIONPITCHDW = EntityDataManager
            .<Float> createKey(EntityTameablePokemob.class, DataSerializers.FLOAT);

    static final DataParameter<Integer>        TRANSFORMEDTODW  = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);

    static final DataParameter<ItemStack>      HELDITEM         = EntityDataManager
            .<ItemStack> createKey(EntityTameablePokemob.class, DataSerializers.OPTIONAL_ITEM_STACK);
    static final DataParameter<Optional<UUID>> OWNER_ID         = EntityDataManager
            .<Optional<UUID>> createKey(EntityTameablePokemob.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    static final DataParameter<Optional<UUID>> OT_ID            = EntityDataManager
            .<Optional<UUID>> createKey(EntityTameablePokemob.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    @SuppressWarnings("unchecked")
    static final DataParameter<Integer>[]      FLAVOURS         = new DataParameter[] {
            EntityDataManager.<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT),
            EntityDataManager.<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT),
            EntityDataManager.<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT),
            EntityDataManager.<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT),
            EntityDataManager.<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT) };

    @SuppressWarnings("unchecked")
    static final DataParameter<Byte>[]         EVS              = new DataParameter[] {
            EntityDataManager.<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE),
            EntityDataManager.<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE),
            EntityDataManager.<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE),
            EntityDataManager.<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE),
            EntityDataManager.<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE),
            EntityDataManager.<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE) };

    protected boolean                          looksWithInterest;
    protected float                            headRotation;
    protected float                            headRotationOld;
    protected boolean                          isPokemonShaking;
    protected boolean                          isPokemonWet;
    protected float                            timePokemonIsShaking;
    protected float                            prevTimePokemonIsShaking;
    public float                               length           = 1;
    protected Vector3                          here             = Vector3.getNewVector();
    protected Vector3                          vec              = Vector3.getNewVector();
    protected Vector3                          v1               = Vector3.getNewVector();
    protected Vector3                          v2               = Vector3.getNewVector();
    protected Vector3                          vBak             = Vector3.getNewVector();
    boolean                                    named            = false;
    boolean                                    initHome         = true;
    protected AnimalChest                      pokeChest;
    boolean                                    returning        = false;
    protected boolean                          players          = false;
    private String                             team             = "";

    /** @param par1World */
    public EntityTameablePokemob(World world)
    {
        super(world);
        initInventory();
    }

    public boolean canBeHeld(ItemStack itemStack)
    {
        return PokecubeItems.isValidHeldItem(itemStack);
    }

    @Override
    public Entity changeDimension(int dimensionIn)
    {
        returning = true;
        Entity ret = super.changeDimension(dimensionIn);
        return ret;
    }

    @Override
    public void displayMessageToOwner(ITextComponent message)
    {
        if (!this.isServerWorld())
        {
            Entity owner = this.getPokemonOwner();
            if (owner == PokecubeCore.proxy.getPlayer((String) null))
            {
                GuiInfoMessages.addMessage(message);
            }
        }
        else
        {
            Entity owner = this.getPokemonOwner();
            if (owner instanceof EntityPlayerMP && !this.isDead)
            {
                MoveMessageEvent event = new MoveMessageEvent(this, message);
                MinecraftForge.EVENT_BUS.post(event);
                PacketPokemobMessage.sendMessage((EntityPlayer) owner, getEntityId(), event.message);
            }
        }
    }

    /** Moved all of these into Tameable, to keep them together */
    @Override
    protected void entityInit()
    {
        super.entityInit();

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
        dataManager.register(EVOLNBDW, new Integer(0));// current evolution nb
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
        dataManager.register(HELDITEM, CompatWrapper.nullStack);

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
        // ID of the owner.
        dataManager.register(OWNER_ID, Optional.<UUID> absent());
        // ID of the OT
        dataManager.register(OT_ID, Optional.<UUID> absent());
        PokemobDataManager manager = new PokemobDataManager(this);
        dataManager = manager;
        manager.manualSyncSet.add(EXPDW);
        manager.manualSyncSet.add(MOVEINDEXDW);
        manager.manualSyncSet.add(MOVESDW);
        manager.manualSyncSet.add(TRANSFORMEDTODW);
        manager.manualSyncSet.add(STATUSDW);
        manager.manualSyncSet.add(EVOLTICKDW);
        manager.manualSyncSet.add(NICKNAMEDW);
    }

    /** Used to get the state without continually looking up in dataManager.
     * 
     * @param state
     * @param array
     * @return */
    protected boolean getAIState(int state, int array)
    {
        return (array & state) != 0;
    }

    @Override
    public String getPokemobTeam()
    {
        if (team.isEmpty())
        {
            team = TeamManager.getTeam(this);
        }
        return team;
    }

    @Override
    public void setPokemobTeam(String team)
    {
        this.team = team;
    }

    @Override
    public ItemStack getHeldItemMainhand()
    {
        return dataManager.get(HELDITEM);
    }

    @Override
    public BlockPos getHome()
    {
        return getHomePosition();
    }

    @Override
    public float getHomeDistance()
    {
        return super.getMaximumHomeDistance();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getInterestedAngle(float f)
    {
        return (headRotationOld + (headRotation - headRotationOld) * f) * 0.15F * (float) Math.PI;
    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        return this.dataManager.get(OT_ID).orNull();
    }

    @Override
    public EntityLivingBase getOwner()
    {
        UUID ownerID = this.getOwnerId();
        if (ownerID == null) return null;
        try
        {
            EntityPlayer o = worldObj.getPlayerEntityByUUID(ownerID);
            players = o != null;
            if (o != null) return o;
        }
        catch (Exception e)
        {

        }
        List<Object> entities = null;
        entities = new ArrayList<Object>(worldObj.loadedEntityList);
        for (Object o : entities)
        {
            if (o instanceof EntityLivingBase)
            {
                EntityLivingBase e = (EntityLivingBase) o;
                players = o instanceof EntityPlayer;
                if (e.getUniqueID().equals(ownerID)) { return e; }
            }
        }
        return null;
    }

    @Override
    public AnimalChest getPokemobInventory()
    {
        return pokeChest;
    }

    @Override
    public EntityLivingBase getPokemonOwner()
    {
        return getOwner();
    }

    @Override
    public UUID getPokemonOwnerID()
    {
        return this.dataManager.get(OWNER_ID).orNull();
    }

    @Override
    public UUID getOwnerId()
    {
        return getPokemonOwnerID();
    }

    public boolean getPokemonShaking()
    {
        return isPokemonShaking;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getShakeAngle(float f, float f1)
    {
        float f2 = (prevTimePokemonIsShaking + (timePokemonIsShaking - prevTimePokemonIsShaking) * f + f1) / 1.8F;

        if (f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        else if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }

        return MathHelper.sin(f2 * (float) Math.PI) * MathHelper.sin(f2 * (float) Math.PI * 11F) * 0.15F
                * (float) Math.PI;
    }

    /** returns true if a sheeps wool has been sheared */
    public boolean getSheared()
    {
        return getPokemonAIState(SHEARED);
    }

    @Override
    public int getSpecialInfo()
    {
        return dataManager.get(SPECIALINFO);
    }

    @Override
    public Team getTeam()
    {
        if (getOwner() == this) { return this.worldObj.getScoreboard().getPlayersTeam(this.getCachedUniqueIdString()); }
        return super.getTeam();
    }

    protected void handleArmourAndSaddle()
    {
        if (worldObj != null && !this.worldObj.isRemote)
        {
            setPokemonAIState(SADDLED, this.pokeChest.getStackInSlot(0) != CompatWrapper.nullStack);
            dataManager.set(HELDITEM, this.pokeChest.getStackInSlot(1));
        }
    }

    @Override
    public boolean hasHomeArea()
    {
        return hasHome();
    }

    public void init(int nb)
    {
        looksWithInterest = false;
    }

    protected void initInventory()
    {
        AnimalChest animalchest = this.pokeChest;
        this.pokeChest = new AnimalChest("PokeChest", this.invSize());
        if (animalchest != null)
        {
            animalchest.removeInventoryChangeListener(this);
            int i = Math.min(animalchest.getSizeInventory(), this.pokeChest.getSizeInventory());

            for (int j = 0; j < i; ++j)
            {
                ItemStack itemstack = animalchest.getStackInSlot(j);

                if (itemstack != CompatWrapper.nullStack)
                {
                    this.pokeChest.setInventorySlotContents(j, itemstack.copy());
                }
            }
            animalchest = null;
        }
        this.pokeChest.addInventoryChangeListener(this);
        this.handleArmourAndSaddle();
    }

    private int invSize()
    {
        return 7;
    }

    public boolean isChested()
    {
        return true;
    }

    @Override
    protected boolean isMovementBlocked()
    {
        return isPokemonWet || this.getHealth() <= 0.0F;
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos)
    {
        /** Checks if the pokedex entry has shears listed, if so, then apply to
         * any mod shears as well. */
        ItemStack key = new ItemStack(Items.SHEARS);
        if (getPokedexEntry().interact(key))
        {
            long last = getEntityData().getLong("lastSheared");

            if (last < worldObj.getTotalWorldTime() - 800 && !worldObj.isRemote)
            {
                setSheared(false);
            }

            return !getSheared();
        }
        return false;
    }

    // 1.11
    public void onInventoryChanged(IInventory inventory)
    {
        handleArmourAndSaddle();
    }

    // 1.10
    public void onInventoryChanged(InventoryBasic inventory)
    {
        handleArmourAndSaddle();
    }

    @Override
    /** Called frequently so the entity can update its state every tick as
     * required. For example, zombies and skeletons use this to react to
     * sunlight and start to burn. */
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune)
    {
        ItemStack key = new ItemStack(Items.SHEARS);
        if (getPokedexEntry().interact(key))
        {
            ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
            setSheared(true);

            getEntityData().setLong("lastSheared", worldObj.getTotalWorldTime());

            int i = 1 + rand.nextInt(3);
            List<ItemStack> list = getPokedexEntry().getInteractResult(key);

            for (int j = 0; j < i; j++)
            {
                for (ItemStack stack : list)
                {
                    ItemStack toAdd = stack.copy();
                    if (getPokedexEntry().dyeable) toAdd.setItemDamage(15 - getSpecialInfo() & 15);
                    ret.add(toAdd);
                }
            }
            this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
            return ret;
        }
        return null;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        portalCounter = -1000;// TODO replace this with actual dupe fix for
                              // nether portals.
        if (initHome)
        {
            initHome = false;
            if (getHome() != null)
            {
                TileEntity te = worldObj.getTileEntity(getHome());
                if (te != null && te instanceof TileEntityNest)
                {
                    TileEntityNest nest = (TileEntityNest) te;
                    nest.addResident(this);
                }
            }
        }
        if (!named && getPokedexEntry() != null)
        {
            this.pokeChest.setCustomName(getName());
            named = true;
        }
        for (int i = 0; i < this.pokeChest.getSizeInventory(); i++)
        {
            ItemStack stack;
            if ((stack = this.pokeChest.getStackInSlot(i)) != CompatWrapper.nullStack)
            {
                stack.getItem().onUpdate(stack, worldObj, this, i, false);
            }
        }
    }

    public void openGUI(EntityPlayer player)
    {
        if (!this.worldObj.isRemote && (!this.isBeingRidden()) && this.getPokemonAIState(IMoveConstants.TAMED))
        {
            this.pokeChest.setCustomName(this.getDisplayName().getFormattedText());
            player.openGui(PokecubeMod.core, Config.GUIPOKEMOB_ID, worldObj, getEntityId(), 0, 0);
        }
    }

    @Override
    public void popFromPokecube()
    {
        fallDistance = 0;
        this.extinguish();
        this.setFlag(0, false);
        this.setPokemonAIState(EVOLVING, false);
    }

    @Override
    public void returnToPokecube()
    {
        if (returning) return;
        returning = true;
        if (PokecubeCore.isOnClientSide())
        {
            try
            {
                MessageServer packet = new MessageServer(MessageServer.RETURN, getEntityId());
                PokecubePacketHandler.sendToServer(packet);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            if (this.getTransformedTo() != null)
            {
                this.setTransformedTo(null);
            }
            RecallEvent pre = new RecallEvent.Pre(this);
            MinecraftForge.EVENT_BUS.post(pre);
            if (pre.isCanceled()) return;
            RecallEvent evtrec = new RecallEvent(this);
            MinecraftForge.EVENT_BUS.post(evtrec);
            if (getHealth() > 0 && evtrec.isCanceled()) { return; }

            Entity owner = getPokemonOwner();

            if (getPokemonAIState(MEGAFORME) || getPokedexEntry().isMega)
            {
                this.setPokemonAIState(MEGAFORME, false);
                IPokemob base = megaEvolve(getPokedexEntry().getBaseForme());
                if (base == this) returning = false;
                if (getEntityData().hasKey(TagNames.ABILITY))
                    base.setAbility(AbilityManager.getAbility(getEntityData().getString(TagNames.ABILITY)));
                base.returnToPokecube();
                return;
            }
            this.setPokemonAIState(IMoveConstants.NOMOVESWAP, false);
            this.setPokemonAIState(IMoveConstants.ANGRY, false);
            this.setAttackTarget(null);
            this.captureDrops = true;
            if (owner instanceof EntityPlayer && !isShadow())
            {
                ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                EntityPlayer player = (EntityPlayer) owner;
                boolean noRoom = false;
                if (player.isDead || player.getHealth() <= 0 || player.inventory.getFirstEmptyStack() == -1)
                {
                    noRoom = true;
                }
                if (noRoom)
                {
                    this.captureDrops = true;
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    noRoom = !toss.isCanceled();
                    if (noRoom)
                    {
                        EntityPokecube entity = new EntityPokecube(worldObj, (EntityLivingBase) owner, itemstack);
                        Vector3 temp = Vector3.getNewVector().set(this);
                        temp.moveEntity(entity);
                        temp.clear().setVelocities(entity);
                        entity.targetEntity = null;
                        entity.targetLocation.clear();
                        worldObj.spawnEntityInWorld(entity);
                    }
                }
                else
                {
                    boolean added = player.inventory.addItemStackToInventory(itemstack);
                    if (!added)
                    {
                        ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F),
                                PokecubeMod.getFakePlayer());
                        MinecraftForge.EVENT_BUS.post(toss);
                        added = toss.isCanceled();
                    }
                }
                if (!owner.isSneaking() && !isDead)
                {
                    boolean has = StatsCollector.getCaptured(getPokedexEntry(), ((EntityPlayer) owner)) > 0;
                    has = has || StatsCollector.getHatched(getPokedexEntry(), ((EntityPlayer) owner)) > 0;
                    if (!has)
                    {
                        StatsCollector.addCapture(this);
                    }
                }
                ITextComponent mess = new TextComponentTranslation("pokemob.action.return", getPokemonDisplayName());
                displayMessageToOwner(mess);
            }
            else if (getPokemonOwnerID() != null)
            {
                if (owner == null)
                {
                    ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    if (!toss.isCanceled())
                    {
                        EntityPokecube entity = new EntityPokecube(worldObj, (EntityLivingBase) owner, itemstack);
                        Vector3 temp = Vector3.getNewVector().set(this);
                        temp.moveEntity(entity);
                        temp.clear().setVelocities(entity);
                        entity.targetEntity = null;
                        entity.targetLocation.clear();
                        worldObj.spawnEntityInWorld(entity);
                    }
                }
                else
                {
                    ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                    PCEvent event = new PCEvent(itemstack, getPokemonOwner());
                    MinecraftForge.EVENT_BUS.post(event);
                    if (!event.isCanceled())
                    {
                        ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F),
                                PokecubeMod.getFakePlayer());
                        MinecraftForge.EVENT_BUS.post(toss);
                        if (!toss.isCanceled())
                        {
                            EntityPokecube entity = new EntityPokecube(worldObj, (EntityLivingBase) owner, itemstack);
                            Vector3 temp = Vector3.getNewVector().set(this);
                            temp.moveEntity(entity);
                            temp.clear().setVelocities(entity);
                            entity.targetEntity = null;
                            entity.targetLocation.clear();
                            worldObj.spawnEntityInWorld(entity);
                        }
                    }
                }
            }
            this.capturedDrops.clear();
            this.captureDrops = false;
            this.setDead();
        }
    }

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
        if (!this.returning && this.addedToChunk && !worldObj.isRemote) returnToPokecube();
        super.setDead();
    }

    @Override
    public void setHeldItem(ItemStack itemStack)
    {
        try
        {
            ItemStack oldStack = getHeldItemMainhand();
            pokeChest.setInventorySlotContents(1, itemStack);
            getPokedexEntry().onHeldItemChange(oldStack, itemStack, this);
            dataManager.set(HELDITEM, itemStack);
        }
        catch (Exception e)
        {
            // Should not happen anymore
            e.printStackTrace();
        }
    }

    @Override
    public void setHome(int x, int y, int z, int distance)
    {
        BlockPos pos = new BlockPos(x, y, z);
        setHomePosAndDistance(pos, distance);
    }

    @Override
    public void setHp(float min)
    {
        setHealth(min);
    }

    @Override
    public void setOriginalOwnerUUID(UUID original)
    {
        this.dataManager.set(OT_ID, Optional.fromNullable(original));
    }

    @Override
    public void setPokemonOwner(EntityLivingBase e)
    {
        if (e == null)
        {
            setPokemonOwner((UUID) null);
            this.setPokemonAIState(IMoveConstants.TAMED, false);
            return;
        }
        this.setPokemonAIState(IMoveConstants.TAMED, true);
        setPokemonOwner(e.getUniqueID());
        if (getOriginalOwnerUUID() == null)
        {
            setOriginalOwnerUUID(e.getUniqueID());
        }
    }

    @Override
    public void setPokemonOwner(UUID owner)
    {
        this.dataManager.set(OWNER_ID, Optional.fromNullable(owner));
        this.team = TeamManager.getTeam(this);
    }

    /** make a sheep sheared if set to true */
    public void setSheared(boolean sheared)
    {
        setPokemonAIState(SHEARED, sheared);
    }

    @Override
    public void setSpecialInfo(int info)
    {
        this.dataManager.set(SPECIALINFO, Integer.valueOf(info));
    }

    @Override
    public void specificSpawnInit()
    {
        this.setHeldItem(this.wildHeldItem());
        setSpecialInfo(getPokedexEntry().defaultSpecial);
    }

    @Override
    public boolean isPlayerOwned()
    {
        return this.getPokemonAIState(IMoveConstants.TAMED) && players;
    }
}
