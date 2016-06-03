/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.Database;
import pokecube.core.events.MoveMessageEvent;
import pokecube.core.events.PCEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.entity.IBreedingMob;
import thut.api.entity.IHungrymob;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.api.pathing.IPathingMob;

/** @author Manchou */
public abstract class EntityTameablePokemob extends EntityTameable implements IPokemob, IMob, IInventoryChangedListener,
        IHungrymob, IPathingMob, IShearable, IBreedingMob, IMobColourable, IRangedAttackMob
{
    public static int                   EXITCUBEDURATION  = 40;

    static final DataParameter<Integer> AIACTIONSTATESDW  = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> ATTACKTARGETIDDW  = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> STATMODDW         = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> EXPDW             = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> HUNGERDW          = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> STATUSMOVEINDEXDW = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> EVS1DW            = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> EVS2DV            = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> SPECIALINFO       = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> EVOLNBDW          = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> EVOLTICKDW        = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);
    static final DataParameter<Integer> HAPPYDW           = EntityDataManager
            .<Integer> createKey(EntityTameablePokemob.class, DataSerializers.VARINT);

    static final DataParameter<String>  MOVESDW           = EntityDataManager
            .<String> createKey(EntityTameablePokemob.class, DataSerializers.STRING);
    static final DataParameter<String>  STATSDW           = EntityDataManager
            .<String> createKey(EntityTameablePokemob.class, DataSerializers.STRING);
    static final DataParameter<String>  NICKNAMEDW        = EntityDataManager
            .<String> createKey(EntityTameablePokemob.class, DataSerializers.STRING);

    static final DataParameter<Byte>    BOOMSTATEDW       = EntityDataManager
            .<Byte> createKey(EntityTameablePokemob.class, DataSerializers.BYTE);

    static final DataParameter<Float>   DIRECTIONPITCHDW  = EntityDataManager
            .<Float> createKey(EntityTameablePokemob.class, DataSerializers.FLOAT);

    protected boolean                   looksWithInterest;

    protected float                     field_25048_b;

    protected float                     field_25054_c;
    protected boolean                   isPokemonShaking;

    protected boolean                   field_25052_g;

    protected float                     timePokemonIsShaking;
    protected float                     prevTimePokemonIsShaking;
    protected Integer                   pokedexNb         = 0;
    public float                        length            = 1;
    private UUID                        original          = new UUID(1234, 4321);
    protected Vector3                   here              = Vector3.getNewVector();

    protected Vector3                   vec               = Vector3.getNewVector();

    protected Vector3                   v1                = Vector3.getNewVector();
    protected Vector3                   v2                = Vector3.getNewVector();
    protected Vector3                   vBak              = Vector3.getNewVector();
    boolean                             named             = false;

    boolean                             initHome          = true;

    protected AnimalChest               pokeChest;

    boolean                             returning         = false;
    protected int                       abilityIndex      = 0;

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
    public void displayMessageToOwner(ITextComponent message)
    {
        if (!this.isServerWorld())
        {
            Entity owner = this.getPokemonOwner();

            if (owner instanceof EntityPlayer)
            {
                GuiInfoMessages.addMessage(message);
            }
        }
        else
        {
            Entity owner = this.getPokemonOwner();
            MoveMessageEvent event = new MoveMessageEvent(this, message);
            MinecraftForge.EVENT_BUS.post(event);
            if (owner instanceof EntityPlayerMP && !this.isDead)
            {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(10));
                buffer.writeByte(PokecubeClientPacket.MOVEMESSAGE);
                buffer.writeInt(getEntityId());
                buffer.writeTextComponent(event.message);
                PokecubeClientPacket mess = new PokecubeClientPacket(buffer);
                PokecubePacketHandler.sendToClient(mess, (EntityPlayer) owner);
            }
        }
    }

    /** Moved all of these into Tameable, to keep them together */
    @Override
    protected void entityInit()
    {
        super.entityInit();

        // From EntityStatsPokemob
        dataManager.register(STATSDW, "0,0,0,0,0,0");// Stats

        dataManager.register(STATMODDW, new Integer(1717986918));
        dataManager.register(EXPDW, new Integer(0));// exp for level 1
        dataManager.register(HUNGERDW, new Integer(0));// Hunger time
        // // for sheared status
        dataManager.register(NICKNAMEDW, "");// nickname
        dataManager.register(EVS1DW, new Integer(1));// evs
        dataManager.register(EVS2DV, new Integer(1));// evs
        dataManager.register(HAPPYDW, new Integer(0));// Happiness

        // From EntityAiPokemob
        this.dataManager.register(DIRECTIONPITCHDW, Float.valueOf(0));
        this.dataManager.register(ATTACKTARGETIDDW, Integer.valueOf(-1));
        this.dataManager.register(AIACTIONSTATESDW, Integer.valueOf(0));

        // from EntityEvolvablePokemob
        dataManager.register(EVOLNBDW, new Integer(0));// current evolution nb
        dataManager.register(EVOLTICKDW, new Integer(0));// evolution tick

        // From EntityMovesPokemb
        dataManager.register(BOOMSTATEDW, Byte.valueOf((byte) -1));
        dataManager.register(STATUSMOVEINDEXDW, Integer.valueOf(0));
        dataManager.register(MOVESDW, "");// moves

        dataManager.register(SPECIALINFO, Integer.valueOf(0));

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
    public ItemStack getHeldItemMainhand()
    {
        return pokeChest != null ? pokeChest.getStackInSlot(1) : null;
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
        return (field_25054_c + (field_25048_b - field_25054_c) * f) * 0.15F * (float) Math.PI;
    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        return original;
    }

    @Override
    public EntityLivingBase getOwner()
    {
        if (!this.getPokemonAIState(IMoveConstants.TAMED)) return null;

        UUID ownerID = super.getOwnerId();
        if (ownerID == null) return null;
        try
        {
            EntityLivingBase o;
            if ((o = worldObj.getPlayerEntityByUUID(ownerID)) != null) return o;
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
    public String getPokemonOwnerName()
    {
        try
        {
            return super.getOwnerId().toString();
        }
        catch (Exception e)
        {
            return "";
        }
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

    protected void handleArmourAndSaddle()
    {
        if (worldObj != null && !this.worldObj.isRemote)
        {
            setPokemonAIState(SADDLED, this.pokeChest.getStackInSlot(0) != null);
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

    private void initInventory()
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

                if (itemstack != null)
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
        return field_25052_g || this.getHealth() <= 0.0F || getPokemonAIState(SLEEPING);
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

    @Override
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
            if ((stack = this.pokeChest.getStackInSlot(i)) != null)
            {
                stack.getItem().onUpdate(stack, worldObj, this, i, false);
            }
        }
    }

    public void openGUI(EntityPlayer player)
    {
        if (!this.worldObj.isRemote && (!this.isBeingRidden()) && this.getPokemonAIState(IMoveConstants.TAMED))
        {
            this.pokeChest.setCustomName(this.getName());
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
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        pokedexNb = nbttagcompound.getInteger(PokecubeSerializer.POKEDEXNB);
        abilityIndex = nbttagcompound.getInteger("abilityIndex");
        this.setPokedexEntry(Database.getEntry(pokedexNb));
        this.setSpecialInfo(nbttagcompound.getInteger("specialInfo"));
        dataManager.set(AIACTIONSTATESDW, nbttagcompound.getInteger("PokemobActionState"));
        setHungerTime(nbttagcompound.getInteger("hungerTime"));
        int[] home = nbttagcompound.getIntArray("homeLocation");
        if (home.length == 4)
        {
            setHome(home[0], home[1], home[2], home[3]);
        }
        if (nbttagcompound.hasKey("OT"))
        {
            try
            {
                original = UUID.fromString(nbttagcompound.getString("OT"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        this.initInventory();

        NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 1 && j < this.pokeChest.getSizeInventory())
            {
                this.pokeChest.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
            }
        }

        ItemStack itemstack;

        if (nbttagcompound.hasKey("SaddleItem", 10))
        {
            itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("SaddleItem"));

            if (itemstack != null && itemstack.getItem() == Items.SADDLE)
            {
                this.pokeChest.setInventorySlotContents(0, itemstack);
            }
        }
        handleArmourAndSaddle();
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
            RecallEvent evtrec = new RecallEvent(this);
            MinecraftForge.EVENT_BUS.post(evtrec);
            if (getHealth() > 0 && evtrec.isCanceled()) { return; }

            Entity owner = getPokemonOwner();
            if (getPokemonAIState(MEGAFORME))
            {
                this.setPokemonAIState(MEGAFORME, false);
                IPokemob base = megaEvolve(getPokedexEntry().getBaseName());
                if (base == this) returning = false;
                base.returnToPokecube();
                return;
            }

            this.setPokemonAIState(IMoveConstants.ANGRY, false);
            this.setAttackTarget(null);
            if (owner instanceof EntityPlayer && !isShadow())
            {
                ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                EntityPlayer player = (EntityPlayer) owner;
                boolean noRoom = false;
                if (player.inventory.getFirstEmptyStack() == -1)
                {
                    noRoom = true;
                }
                if (noRoom)
                {
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    noRoom = toss.isCanceled();
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
                boolean added = player.inventory.addItemStackToInventory(itemstack);
                if (!added)
                {
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    added = toss.isCanceled();
                }
                if (!owner.isSneaking() && !isDead)
                    ((EntityPlayer) owner).addStat(PokecubeMod.pokemobAchievements.get(pokedexNb), 1);
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.return", "green",
                        getPokemonDisplayName());
                displayMessageToOwner(mess);
            }
            else if (getPokemonOwnerName() != null && !getPokemonOwnerName().isEmpty())
            {
                if (owner == null)
                {
                    ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    if (!toss.isCanceled()) entityDropItem(itemstack, 0F);
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
                        if (!toss.isCanceled()) entityDropItem(itemstack, 0F).setPickupDelay(1000);
                        ;
                    }
                }
            }

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
        this.original = original;
    }

    @Override
    public void setPokemonOwner(EntityLivingBase e)
    {
        if (e == null)
        {
            super.setOwnerId(null);
            this.setPokemonAIState(IMoveConstants.TAMED, false);
            return;
        }

        boolean uuidorName = this.getPokemonOwnerName().equalsIgnoreCase(e.getUniqueID().toString())
                || getPokemonOwnerName().equalsIgnoreCase(e.getName());

        if (e instanceof EntityPlayer && !uuidorName)
        {
            this.setPokemonAIState(IMoveConstants.TAMED, true);
            super.setOwnerId(e.getUniqueID());

            if (original.compareTo(PokecubeMod.fakeUUID) == 0)
            {
                original = e.getUniqueID();
            }
        }
        else
        {
            this.setPokemonAIState(IMoveConstants.TAMED, true);
            super.setOwnerId(e.getUniqueID());
        }
    }

    @Override
    public void setPokemonOwnerByName(String s)
    {
        EntityPlayer player = PokecubeCore.getPlayer(s);
        this.setPokemonOwner(player);
        super.setOwnerId(player.getUniqueID());
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
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger(PokecubeSerializer.POKEDEXNB, pokedexNb);
        nbttagcompound.setInteger("abilityIndex", abilityIndex);
        nbttagcompound.setInteger("PokemobActionState", dataManager.get(AIACTIONSTATESDW));
        nbttagcompound.setInteger("hungerTime", getHungerTime());
        nbttagcompound.setInteger("specialInfo", getSpecialInfo());
        nbttagcompound.setIntArray("homeLocation",
                new int[] { getHome().getX(), getHome().getY(), getHome().getZ(), (int) getHomeDistance() });

        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.pokeChest.getSizeInventory(); ++i)
        {
            ItemStack itemstack = this.pokeChest.getStackInSlot(i);

            if (itemstack != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);

        nbttagcompound.setString("OT", original.toString());

        if (this.pokeChest.getStackInSlot(0) != null)
        {
            nbttagcompound.setTag("SaddleItem", this.pokeChest.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }
    }
}
