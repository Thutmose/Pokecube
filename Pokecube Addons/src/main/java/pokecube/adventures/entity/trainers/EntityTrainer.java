package pokecube.adventures.entity.trainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.trainers.EntityAITrainer;
import pokecube.adventures.handlers.GeneralCommands;
import pokecube.adventures.handlers.PASaveHandler;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.ItemTrainer;
import pokecube.compat.Config;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityTrainer extends EntityAgeable implements IEntityAdditionalSpawnData
{

    public static final int  STATIONARY       = 1;
    public static final int  ANGRY            = 2;
    public static final int  THROWING         = 4;

    public static int        ATTACKCOOLDOWN   = 10000;
    private int              battleCooldown   = ATTACKCOOLDOWN;

    private boolean          randomize        = false;
    public ItemStack[]       pokecubes        = new ItemStack[6];
    public int[]             pokenumbers      = new int[6];
    public int[]             pokelevels       = new int[6];
    public int[]             attackCooldown   = new int[6];
    public int               cooldown         = 0;
    public int               globalCooldown   = 0;
    public int               friendlyCooldown = 0;
    public List<IPokemob>    currentPokemobs  = new ArrayList<IPokemob>();
    private EntityLivingBase target;
    public TypeTrainer       type;
    private int              id;
    public String            name             = "";
    public UUID              outID;
    public IPokemob          outMob;
    public boolean           male             = true;

    public EntityTrainer(World par1World)
    {
        this(par1World, null);
        this.equipmentDropChances = new float[] { 1, 1, 1, 1, 1 };
    }

    public EntityTrainer(World world, Vector3 location)
    {
        this(world, location, false);
    }

    public EntityTrainer(World par1World, Vector3 location, boolean stationary)
    {
        super(par1World);

        this.setSize(0.6F, 1.8F);
        this.renderDistanceWeight = 4;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAITrainer(this, EntityPlayer.class));
        this.tasks.addTask(1, new EntityAIMoveTowardsTarget(this, 0.6, 10));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        if (location != null)
        {
            location.moveEntity(this);
            if (stationary) setStationary(location);
        }
    }

    public void setStationary(Vector3 location)
    {
        if (location == null)
        {
            setAIState(STATIONARY, false);
            for (Object o : this.tasks.taskEntries)
                if (o instanceof GuardAI) this.tasks.removeTask((EntityAIBase) o);
            return;
        }
        IGuardAICapability capability = getCapability(EventsHandler.GUARDAI_CAP, null);
        if (capability != null)
        {
            capability.setActiveTime(TimePeriod.fullDay);
            capability.setPos(getPosition());
            tasks.addTask(2, new GuardAI(this, capability));
        }
        setAIState(STATIONARY, true);
    }

    public void setStationary(boolean stationary)
    {
        if (stationary && !getAIState(STATIONARY)) setStationary(Vector3.getNewVector().set(this));
        else if (!stationary && getAIState(STATIONARY))
        {
            for (Object o : this.tasks.taskEntries)
                if (o instanceof GuardAI) this.tasks.removeTask((EntityAIBase) o);
            setAIState(STATIONARY, false);
        }
    }

    public boolean getShouldRandomize()
    {
        return randomize;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(5, Integer.valueOf(0));// more action states
    }

    public boolean getAIState(int state)
    {

        return (dataWatcher.getWatchableObjectInt(5) & state) != 0;
    }

    public void setAIState(int state, boolean flag)
    {
        int byte0 = dataWatcher.getWatchableObjectInt(5);

        if (flag)
        {
            dataWatcher.updateObject(5, Integer.valueOf((byte0 | state)));
        }
        else
        {
            dataWatcher.updateObject(5, Integer.valueOf((byte0 & -state - 1)));
        }
    }

    public EntityTrainer(World world, TypeTrainer type, int level)
    {
        this(world);
        setId(PASaveHandler.getInstance().getNewId());
        initTrainer(type, level);
    }

    public EntityTrainer(World world, TypeTrainer type, int level, Vector3 location, boolean stationary)
    {
        this(world, location, true);
        setId(PASaveHandler.getInstance().getNewId());
        initTrainer(type, level);
    }

    public void initTrainer(TypeTrainer type, int level)
    {
        this.type = type;
        byte genders = type.genders;
        if (genders == 1) male = true;
        if (genders == 2) male = false;
        if (genders == 3) male = Math.random() < 0.5;

        TypeTrainer.getRandomTeam(this, level, pokecubes, worldObj);

        for (int i = 0; i < 6; i++)
        {
            if (pokecubes[i] != null)
            {
                IPokemob poke = PokecubeManager.itemToPokemob(pokecubes[i], worldObj);
                if (poke != null)
                {
                    pokenumbers[i] = poke.getPokedexNb();
                    pokelevels[i] = poke.getLevel();
                }
                else
                {
                    pokenumbers[i] = 0;
                    pokelevels[i] = 0;
                }
            }
            else
            {
                pokenumbers[i] = 0;
                pokelevels[i] = 0;
            }
        }

        setTypes();
    }

    public void setTypes()
    {
        if (name.isEmpty())
        {
            int index = getId() % (male ? TypeTrainer.maleNames.size() : TypeTrainer.femaleNames.size());
            name = (male ? TypeTrainer.maleNames.get(index) : TypeTrainer.femaleNames.get(index));
        }
        this.setCustomNameTag(type.name + " " + name);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        int n = 0;
        for (ItemStack i : pokecubes)
        {
            if (i != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                i.writeToNBT(tag);
                nbt.setTag("slot" + n, tag);
                n++;
            }
        }
        nbt.setBoolean("gender", male);
        nbt.setInteger("battleCD", battleCooldown);
        nbt.setBoolean("randomTeam", randomize);
        nbt.setString("name", name);
        nbt.setString("type", type.name);
        nbt.setInteger("uniqueid", getId());
        if (outID != null) nbt.setString("outPokemob", outID.toString());
        nbt.setInteger("aiState", dataWatcher.getWatchableObjectInt(5));
        nbt.setInteger("cooldown", globalCooldown);
        nbt.setIntArray("cooldowns", attackCooldown);
        nbt.setInteger("friendly", friendlyCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        for (int n = 0; n < 6; n++)
        {
            NBTBase temp = nbt.getTag("slot" + n);
            if (temp instanceof NBTTagCompound)
            {
                NBTTagCompound tag = (NBTTagCompound) temp;
                pokecubes[n] = ItemStack.loadItemStackFromNBT(tag);
                if (PokecubeManager.getPokedexNb(pokecubes[n]) == 0) pokecubes[n] = null;
            }
        }
        dataWatcher.updateObject(5, nbt.getInteger("aiState"));
        randomize = nbt.getBoolean("randomTeam");
        type = TypeTrainer.getTrainer(nbt.getString("type"));
        setId(nbt.getInteger("uniqueid"));
        if (nbt.hasKey("outPokemob"))
        {
            outID = UUID.fromString(nbt.getString("outPokemob"));
        }
        if (nbt.hasKey("battleCD")) battleCooldown = nbt.getInteger("battleCD");
        setAIState(STATIONARY, nbt.getBoolean("stationary"));
        globalCooldown = nbt.getInteger("cooldown");
        attackCooldown = nbt.getIntArray("cooldowns");
        male = nbt.getBoolean("gender");
        name = nbt.getString("name");
        if (attackCooldown.length != 6) attackCooldown = new int[6];

        friendlyCooldown = nbt.getInteger("friendly");

        setTypes();
    }

    protected void setId(int id)
    {
        this.id = id;
        PASaveHandler.getInstance().trainers.put(id, this);
    }

    public int countPokemon()
    {
        if (outID != null && outMob == null)
        {
            for (int i = 0; i < worldObj.getLoadedEntityList().size(); ++i)
            {
                Entity entity = (Entity) worldObj.getLoadedEntityList().get(i);
                if (entity instanceof IPokemob && outID.equals(entity.getUniqueID()))
                {
                    outMob = (IPokemob) entity;
                    break;
                }
            }
        }
        if (outMob != null && ((Entity) outMob).isDead)
        {
            outID = null;
            outMob = null;
        }
        int ret = outMob == null ? 0 : 1;

        if (ret == 0 && getAIState(THROWING)) ret++;

        for (ItemStack i : pokecubes)
        {
            if (i != null && PokecubeManager.getPokedexNb(i) != 0) ret++;
        }
        return ret;
    }

    public void lowerCooldowns()
    {
        boolean done = attackCooldown[0] <= 0;
        cooldown--;
        if (done)
        {
            for (int i = 0; i < 6; i++)
            {
                attackCooldown[i] = -1;
            }
        }
        else
        {
            for (int i = 0; i < 6; i++)
            {
                attackCooldown[i]--;
            }
        }
    }

    public void throwCubeAt(Entity target)
    {
        if (target == null) return;
        for (int j = 0; j < 6; j++)
        {
            ItemStack i = pokecubes[j];
            if (i != null && attackCooldown[j] < 0)
            {
                EntityPokecube entity = new EntityPokecube(worldObj, this, i.copy());

                Vector3 here = Vector3.getNewVector().set(this);
                Vector3 t = Vector3.getNewVector().set(target);
                t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
                entity.targetLocation.set(t);
                setAIState(THROWING, true);
                worldObj.spawnEntityInWorld(entity);
                attackCooldown[j] = battleCooldown;
                globalCooldown = 1000;
                pokecubes[j] = null;
                for (int k = j + 1; k < 6; k++)
                {
                    attackCooldown[k] = 20;
                }
                return;
            }
            if (i != null && attackCooldown[j] < 30) { return; }
        }
        if (globalCooldown > 0 && outID == null)
        {
            globalCooldown = 0;
            onDefeated(target);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
                && !(source.getEntity() instanceof EntityPlayer))
        {
            setTrainerTarget(source.getEntity());
        }

        if (Config.instance.trainersInvul) return false;

        if (friendlyCooldown > 0) return false;

        return super.attackEntityFrom(source, i);
    }

    boolean added = false;

    @Override
    public void onLivingUpdate()
    {
        if (GeneralCommands.TRAINERSDIE)
        {
            this.setDead();
            return;
        }

        super.onLivingUpdate();
        if (worldObj.isRemote) return;

        if (this.getEquipmentInSlot(1) == null) type.initTrainerItems(this);

        if (!added)
        {
            added = true;
            TrainerSpawnHandler.addTrainerCoord(this);
        }

        for (int i = 0; i < 6; i++)
        {
            ItemStack item = pokecubes[i];
            if (item != null && attackCooldown[i] <= 0)
            {
                this.setCurrentItemOrArmor(0, item);
                break;
            }
            if (i == 5) this.setCurrentItemOrArmor(0, null);
        }

        EntityLivingBase target = getAITarget() != null ? getAITarget()
                : getAttackTarget() != null ? getAttackTarget() : null;

        if (target != null)
        {
            this.faceEntity(target, 10, 10);
            getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }

        if (this.countPokemon() == 0 && !getAIState(STATIONARY))
        {
            timercounter++;
            if (timercounter > 50)
            {
                this.setDead();
            }
            return;
        }
        timercounter = 0;

        friendlyCooldown--;
    }

    int timercounter = 0;

    public void setTrainerTarget(Entity e)
    {
        setTarget((EntityLivingBase) e);
    }

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
        PCEventsHandler.recallAllPokemobs(this);
        super.setDead();
    }

    @Override
    public EntityLivingBase getAITarget()
    {
        return this.getTarget();
    }

    @Override
    public boolean interact(EntityPlayer entityplayer)
    {
        setTarget(entityplayer);
        if (entityplayer.capabilities.isCreativeMode)
        {
            if (getType() != null && !worldObj.isRemote && entityplayer.isSneaking()
                    && entityplayer.getHeldItem() == null)
            {
                String message = this.getName() + " " + getAIState(STATIONARY) + " " + countPokemon() + " ";
                for (ItemStack i : pokecubes)
                {
                    if (i != null) message += i.getDisplayName() + " ";
                }
                for (int i = 0; i < 5; i++)
                {
                    ItemStack item = getEquipmentInSlot(i);
                    if (item != null) message += item.getDisplayName() + " ";
                }
                entityplayer.addChatMessage(new ChatComponentText(message));
            }
            else if (!worldObj.isRemote && entityplayer.isSneaking()
                    && entityplayer.getHeldItem().getItem() == Items.stick)
            {
                throwCubeAt(entityplayer);
            }

            if (entityplayer.getHeldItem() != null && entityplayer.getHeldItem().getItem() instanceof ItemTrainer)
            {
                entityplayer.openGui(PokecubeAdv.instance, PokecubeAdv.GUITRAINER_ID, worldObj, getId(), 0, 0);
            }
        }
        else
        {
            if (entityplayer.getHeldItem() != null)
            {
                if (entityplayer.getHeldItem()
                        .getItem() == (Item) Item.itemRegistry.getObject(new ResourceLocation("minecraft:emerald")))
                {
                    entityplayer.inventory.consumeInventoryItem(
                            (Item) Item.itemRegistry.getObject(new ResourceLocation("minecraft:emerald")));
                    setTrainerTarget(null);
                    for (IPokemob pokemob : currentPokemobs)
                    {
                        pokemob.returnToPokecube();
                    }

                    friendlyCooldown = 2400;
                }
            }
            else
            {
                // System.out.println("HeldItem is null");
            }
        }

        return false;// super.interact(entityplayer);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    public TypeTrainer getType()
    {
        return type;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {

        buffer.writeInt(type.name.length());
        buffer.writeBytes(type.name.getBytes());
        buffer.writeInt(name.length());
        buffer.writeBytes(name.getBytes());
        buffer.writeBoolean(male);
        buffer.writeInt(getId());
        for (int i = 0; i < 6; i++)
        {
            if (pokecubes[i] != null)
            {

                if (PokecubeManager.getPokedexNb(pokecubes[i]) == 0)
                {
                    buffer.writeInt(0);
                    buffer.writeInt(0);
                    pokecubes[i] = null;
                }
                else
                {
                    IPokemob mob = (IPokemob) PokecubeManager.itemToPokemob(pokecubes[i], worldObj);
                    if (mob == null)
                    {
                        buffer.writeInt(0);
                        buffer.writeInt(0);
                    }
                    else
                    {
                        buffer.writeInt(mob.getPokedexNb());
                        buffer.writeInt(mob.getLevel());
                    }
                }
            }
            else
            {
                buffer.writeInt(0);
                buffer.writeInt(0);
            }
        }
    }

    @Override
    public void readSpawnData(ByteBuf buff)
    {
        int num = buff.readInt();
        byte[] string = new byte[num];
        for (int n = 0; n < num; n++)
        {
            string[n] = buff.readByte();
        }
        type = TypeTrainer.getTrainer(new String(string));
        num = buff.readInt();
        string = new byte[num];
        for (int n = 0; n < num; n++)
        {
            string[n] = buff.readByte();
        }
        name = new String(string);
        male = buff.readBoolean();
        setId(buff.readInt());
        for (int i = 0; i < 6; i++)
        {
            pokenumbers[i] = buff.readInt();
            pokelevels[i] = buff.readInt();
        }
    }

    public void addPokemob(ItemStack mob)
    {
        for (int i = 0; i < 6; i++)
        {
            if (pokecubes[i] == null)
            {
                InventoryPC.heal(mob);
                pokecubes[i] = mob.copy();
                return;
            }
        }
    }

    public void onDefeated(Entity defeater)
    {
        for (int i = 1; i < 5; i++)
        {
            ItemStack stack = getEquipmentInSlot(i);
            if (stack != null) this.entityDropItem(stack.copy(), 0.5f);
        }
        if (defeater != null)
        {
            String text = StatCollector.translateToLocal("pokecube.trainer.defeat");
            IChatComponent message;
            IChatComponent name = getDisplayName();
            name.getChatStyle().setColor(EnumChatFormatting.RED);
            text = EnumChatFormatting.RED + text;
            message = name.appendSibling(IChatComponent.Serializer.jsonToComponent("[\" " + text + "\"]"));
            target.addChatMessage(message);
        }
    }

    /** Drop the equipment for this entity. */
    @Override
    protected void dropEquipment(boolean drop, int looting)
    {
        if (looting > 4) super.dropEquipment(drop, looting);
    }

    public void setPokemob(int number, int level, int index)
    {
        if (number < 1)
        {
            pokecubes[index] = null;
            attackCooldown[index] = 0;
            pokenumbers[index] = 0;
            pokelevels[index] = 0;
            return;
        }

        IPokemob pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(number, worldObj);
        if (pokemob == null)
        {
            pokecubes[index] = null;
            return;
        }
        pokemob.setExp(Tools.levelToXp(pokemob.getExperienceMode(), level), false, true);
        pokemob.setPokemonOwner(this);
        ItemStack mob = PokecubeManager.pokemobToItem(pokemob);
        ((Entity) pokemob).setDead();
        InventoryPC.heal(mob);
        pokecubes[index] = mob.copy();
        pokenumbers[index] = number;
        pokelevels[index] = level;
        attackCooldown[index] = 0;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable p_90011_1_)
    {
        return null;
    }

    public EntityLivingBase getTarget()
    {
        return target;
    }

    public void setTarget(EntityLivingBase target)
    {
        if (target != null && target != this.target)
        {
            cooldown = 100;
            String text = StatCollector.translateToLocal("pokecube.trainer.agress");
            IChatComponent message;
            IChatComponent name = getDisplayName();
            name.getChatStyle().setColor(EnumChatFormatting.RED);
            text = EnumChatFormatting.RED + text;
            message = name.appendSibling(IChatComponent.Serializer.jsonToComponent("[\" " + text + "\"]"));
            target.addChatMessage(message);
        }
        this.target = target;
    }

}
