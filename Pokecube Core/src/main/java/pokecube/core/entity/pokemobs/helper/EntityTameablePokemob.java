/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.database.Database;
import pokecube.core.events.KillEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.interfaces.IBreedingMob;
import pokecube.core.interfaces.IHungrymob;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;
import thut.api.pathing.IPathingMob;

/** @author Manchou */
public abstract class EntityTameablePokemob extends EntityTameable
        implements IPokemob, IMob, IInvBasic, IHungrymob, IPathingMob, IShearable, IBreedingMob, IMobColourable
{
    public static int          EXITCUBEDURATION = 40;

    protected boolean          looksWithInterest;
    protected float            field_25048_b;
    protected float            field_25054_c;
    protected boolean          isPokemonShaking;
    protected boolean          field_25052_g;
    protected float            timePokemonIsShaking;
    protected float            prevTimePokemonIsShaking;
    protected Integer          pokedexNb        = 0;
    public float               length           = 1;
    // protected int hungerTime;
    protected EntityLivingBase owner;

    private String             ownerName        = "";
    private UUID               original         = new UUID(1234, 4321);

    protected Vector3          here             = Vector3.getNewVector();
    protected Vector3          vec              = Vector3.getNewVector();
    protected Vector3          v1               = Vector3.getNewVector();
    protected Vector3          v2               = Vector3.getNewVector();
    protected Vector3          vBak             = Vector3.getNewVector();

    /** @param par1World */
    public EntityTameablePokemob(World world)
    {
        super(world);
        initInventory();
    }

    static final int AIACTIONSTATESDW  = 5;

    static final int DIRECTIONPITCHDW  = 10;
    static final int STATSDW           = 11;

    static final int ATTACKTARGETIDDW  = 13;

    static final int STATMODDW         = 18;
    static final int BOOMSTATEDW       = 19;
    static final int EXPDW             = 20;
    static final int HUNGERDW          = 21;
    static final int NICKNAMEDW        = 22;
    static final int STATUSMOVEINDEXDW = 23;
    static final int EVS1DW            = 24;
    static final int EVS2DV            = 25;

    static final int SPECIALINFO       = 26;

    static final int EVOLNBDW          = 27;
    static final int EVOLTICKDW        = 28;
    static final int HAPPYDW           = 29;
    static final int MOVESDW           = 30;

    /** Moved all of these into Tameable, to keep them together */
    @Override
    protected void entityInit()
    {
        super.entityInit();

        // From EntityStatsPokemob
        dataWatcher.addObject(STATSDW, "0,0,0,0,0,0");// Stats

        dataWatcher.addObject(STATMODDW, new Integer(1717986918));// stat
                                                                  // modifiers
        dataWatcher.addObject(EXPDW, new Integer(0));// exp for level 1
        dataWatcher.addObject(HUNGERDW, new Integer(0));// Hunger time
        // // for sheared status
        dataWatcher.addObject(NICKNAMEDW, "");// nickname
        dataWatcher.addObject(EVS1DW, new Integer(1));// evs
        dataWatcher.addObject(EVS2DV, new Integer(1));// evs
        dataWatcher.addObject(HAPPYDW, new Integer(0));// Happiness

        // From EntityAiPokemob
        this.dataWatcher.addObject(DIRECTIONPITCHDW, Float.valueOf(0));// Direction
                                                                       // pitch
        this.dataWatcher.addObject(ATTACKTARGETIDDW, Integer.valueOf(-1));// Attack
                                                                          // Target
                                                                          // ID
        this.dataWatcher.addObject(AIACTIONSTATESDW, Integer.valueOf(0));// more
                                                                         // action
                                                                         // states

        // from EntityEvolvablePokemob
        dataWatcher.addObject(EVOLNBDW, new Integer(0));// current evolution nb
        dataWatcher.addObject(EVOLTICKDW, new Integer(0));// evolution tick

        // From EntityMovesPokemb
        dataWatcher.addObject(BOOMSTATEDW, Byte.valueOf((byte) -1)); // explosion
                                                                     // state
        dataWatcher.addObject(STATUSMOVEINDEXDW, Integer.valueOf(0));// status
                                                                     // and
                                                                     // moveIndex
        dataWatcher.addObject(MOVESDW, "");// moves

        dataWatcher.addObject(SPECIALINFO, Integer.valueOf(0));// Used for
                                                               // mareep colour

    }

    public void init(int nb)
    {
        looksWithInterest = false;
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
        nbttagcompound.setInteger("PokemobActionState", dataWatcher.getWatchableObjectInt(5));
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

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        pokedexNb = nbttagcompound.getInteger(PokecubeSerializer.POKEDEXNB);
        this.setPokedexEntry(Database.getEntry(pokedexNb));
        this.setSpecialInfo(nbttagcompound.getInteger("specialInfo"));
        dataWatcher.updateObject(5, nbttagcompound.getInteger("PokemobActionState"));
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
        // if (this.isChested())
        {
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
        }

        ItemStack itemstack;

        if (nbttagcompound.hasKey("SaddleItem", 10))
        {
            itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("SaddleItem"));

            if (itemstack != null && itemstack.getItem() == Items.saddle)
            {
                this.pokeChest.setInventorySlotContents(0, itemstack);
            }
        }
        handleArmourAndSaddle();
    }

    boolean named    = false;
    boolean initHome = true;

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
            this.pokeChest.setCustomName(getName());// .func_110133_a(this.getName());
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

    @Override
    public String getPokemonOwnerName()
    {
        if (!ownerName.isEmpty()) { return ownerName; }

        try
        {
            return super.getOwnerId();// .func_152113_b();//super.getOwnerName();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    @Override
    public void setPokemonOwnerByName(String s)
    {
        EntityPlayer player = PokecubeCore.getPlayer(s);

        this.setPokemonOwner(player);
        super.setOwnerId(s);
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

    @SideOnly(Side.CLIENT)
    @Override
    public float getInterestedAngle(float f)
    {
        return (field_25054_c + (field_25048_b - field_25054_c) * f) * 0.15F * (float) Math.PI;
    }

    @Override
    protected boolean isMovementBlocked()
    {
        return field_25052_g || this.getHealth() <= 0.0F || getPokemonAIState(SLEEPING);
    }

    @Override
    public void displayMessageToOwner(String message)
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
            if (owner instanceof EntityPlayer && !this.isDead)
            {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("id", owner.getEntityId());
                nbt.setString("message", message);
                PokecubeClientPacket mess = new PokecubeClientPacket(PokecubeClientPacket.MOVEMESSAGE, nbt);
                PokecubePacketHandler.sendToClient(mess, (EntityPlayer) owner);
            }
        }
    }

    @Override
    public EntityLivingBase getPokemonOwner()
    {
        if (owner == null) return getOwner();
        return owner;
    }

    @Override
    public void setPokemonOwner(EntityLivingBase e)
    {
        if (e == null)
        {
            super.setOwnerId("");
            owner = null;
            ownerName = "";
            this.setPokemonAIState(IPokemob.TAMED, false);
            return;
        }

        owner = e;

        boolean uuidorName = this.getPokemonOwnerName().equalsIgnoreCase(e.getUniqueID().toString())
                || getPokemonOwnerName().equalsIgnoreCase(e.getName());

        if (e instanceof EntityPlayer && !uuidorName)
        {
            ownerName = e.getName();
            this.setPokemonAIState(IPokemob.TAMED, true);
            super.setOwnerId(e.getUniqueID().toString());

            if (original.compareTo(PokecubeMod.fakeUUID) == 0)
            {
                original = e.getUniqueID();
            }
        }
        else
        {
            this.setPokemonAIState(IPokemob.TAMED, true);
            String uuid = e.getUniqueID().toString();
            super.setOwnerId(uuid);
        }
    }

    @Override
    public UUID getOriginalOwnerUUID()
    {
        return original;
    }

    @Override
    public void setOriginalOwnerUUID(UUID original)
    {
        this.original = original;
    }

    @Override
    public EntityLivingBase getOwner()
    {
        if (!this.getPokemonAIState(IPokemob.TAMED)) return null;
        if (owner == null)
        {
            List<Object> entities = null;
            entities = new ArrayList<Object>(worldObj.loadedEntityList);

            if (!ownerName.isEmpty())
            {
                owner = worldObj.getPlayerEntityByName(ownerName);
                return owner;
            }

            for (Object o : entities)
            {
                if (o instanceof EntityLivingBase)
                {
                    EntityLivingBase e = (EntityLivingBase) o;
                    String owneruuid = super.getOwnerId();

                    if (e.getUniqueID().toString().equals(owneruuid))
                    {
                        owner = e;
                        ownerName = owner.getName();
                        return owner;
                    }
                }
            }
        }

        return owner;
    }

    @Override
    public ItemStack getHeldItem()
    {
        return pokeChest != null ? pokeChest.getStackInSlot(1) : null;
    }

    public void setHeldItem(ItemStack itemStack)
    {
        try
        {
            ItemStack oldStack = getHeldItem();
            pokeChest.setInventorySlotContents(1, itemStack);
            getPokedexEntry().onHeldItemChange(oldStack, itemStack, this);
        }
        catch (Exception e)
        {
            // Should not happen anymore
            e.printStackTrace();
        }
    }

    public boolean canBeHeld(ItemStack itemStack)
    {
        return PokecubeItems.isValidHeldItem(itemStack);
    }

    /** Sets the x,y,z of the entity from the given parameters. Also seems to
     * set up a bounding box. */
    @Override
    public void setPosition(double x, double y, double z)
    {
        super.setPosition(x, y, z);
    }

    protected AnimalChest pokeChest;

    @Override
    public void onInventoryChanged(InventoryBasic inventory)
    {
        handleArmourAndSaddle();
    }

    public void openGUI(EntityPlayer player)
    {
        if (!this.worldObj.isRemote && (this.riddenByEntity == null || this.riddenByEntity == player)
                && this.getPokemonAIState(IPokemob.TAMED))
        {
            this.pokeChest.setCustomName(this.getName());
            player.openGui(PokecubeMod.core, Mod_Pokecube_Helper.GUIPOKEMOB_ID, worldObj, getEntityId(), 0, 0);
        }
    }

    private int invSize()
    {
        return 7;
    }

    private void initInventory()
    {
        AnimalChest animalchest = this.pokeChest;
        this.pokeChest = new AnimalChest("PokeChest", this.invSize());

        if (animalchest != null)
        {
            animalchest.func_110132_b(this);
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

        this.pokeChest.func_110134_a(this);
        this.handleArmourAndSaddle();
    }

    protected void handleArmourAndSaddle()
    {
        if (worldObj != null && !this.worldObj.isRemote)
        {
            setPokemonAIState(SADDLED, this.pokeChest.getStackInSlot(0) != null);
        }
    }

    public boolean isChested()
    {
        return true;
    }

    boolean returning = false;

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

            this.setPokemonAIState(IPokemob.ANGRY, false);
            this.setAttackTarget(null);
            if (owner instanceof EntityPlayer && !isShadow())
            {
                ItemStack itemstack = PokecubeManager.pokemobToItem(this);

                boolean added = false;
                if (((EntityPlayer) owner).inventory.getFirstEmptyStack() == -1)
                {
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                }
                else if (itemstack.getItem() != null && (owner.isDead
                        || !(added = ((EntityPlayer) owner).inventory.addItemStackToInventory(itemstack))))
                {
                    ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F), PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                }

                if (added && owner instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) owner).sendContainerToPlayer(((EntityPlayerMP) owner).inventoryContainer);
                }
                if (!owner.isSneaking() && !isDead)
                    ((EntityPlayer) owner).addStat(PokecubeMod.pokemobAchievements.get(pokedexNb), 1);
                String mess = StatCollector.translateToLocalFormatted("pokemob.action.return", getPokemonDisplayName());
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
                    KillEvent evt = new KillEvent(null, this);
                    MinecraftForge.EVENT_BUS.post(evt);
                    if (evt.isCanceled())
                    {
                        ItemStack itemstack = PokecubeManager.pokemobToItem(this);
                        ItemTossEvent toss = new ItemTossEvent(entityDropItem(itemstack, 0F),
                                PokecubeMod.getFakePlayer());
                        MinecraftForge.EVENT_BUS.post(toss);
                        if (!toss.isCanceled()) entityDropItem(itemstack, 0F);
                    }
                }
            }

            this.setDead();
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
    public AnimalChest getPokemobInventory()
    {
        return pokeChest;
    }

    @Override
    public float getHomeDistance()
    {
        return super.getMaximumHomeDistance();// func_110174_bM();
    }

    @Override
    public boolean hasHomeArea()
    {
        return hasHome();
    }

    @Override
    public BlockPos getHome()
    {
        return getHomePosition();
    }

    @Override
    public void setHome(int x, int y, int z, int distance)
    {
        setHomePosAndDistance(new BlockPos(x, y, z), distance);
    }

    @Override
    public void setHp(float min)
    {
        setHealth(min);
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
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements,
            boolean p_180426_10_)
    {
        this.newPosX = x;
        this.newPosY = y;
        this.newPosZ = z;
        this.newRotationYaw = (double) yaw;
        this.newRotationPitch = (double) pitch;
        this.newPosRotationIncrements = posRotationIncrements;
    }

    @Override
    public int getSpecialInfo()
    {
        return dataWatcher.getWatchableObjectInt(SPECIALINFO);
    }

    @Override
    public void setSpecialInfo(int info)
    {
        this.dataWatcher.updateObject(SPECIALINFO, Integer.valueOf(info));
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos)
    {
        /** Checks if the pokedex entry has shears listed, if so, then apply to
         * any mod shears as well. */
        ItemStack key = new ItemStack(Items.shears);
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
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune)
    {
        ItemStack key = new ItemStack(Items.shears);
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
                    if (getPokedexEntry().hasSpecialTextures[4]) toAdd.setItemDamage(15 - getSpecialInfo() & 15);
                    ret.add(toAdd);
                }
            }
            this.playSound("mob.sheep.shear", 1.0F, 1.0F);
            return ret;
        }
        return null;
    }

    /** returns true if a sheeps wool has been sheared */
    public boolean getSheared()
    {
        return getPokemonAIState(SHEARED);
    }

    /** make a sheep sheared if set to true */
    public void setSheared(boolean sheared)
    {
        setPokemonAIState(SHEARED, sheared);
    }

    /** Used to get the state without continually looking up in datawatcher.
     * 
     * @param state
     * @param array
     * @return */
    protected boolean getAIState(int state, int array)
    {
        return (array & state) != 0;
    }
}
