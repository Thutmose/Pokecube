package pokecube.adventures.entity.trainers;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.Achievement;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.trainers.AITrainerBattle;
import pokecube.adventures.ai.trainers.AITrainerFindTarget;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
import pokecube.adventures.entity.helper.EntityHasPokemobs;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.ItemBadge;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class EntityTrainer extends EntityHasPokemobs
{
    public static class DefeatEntry
    {
        static DefeatEntry createFromNBT(NBTTagCompound nbt)
        {
            String defeater = nbt.getString("player");
            long time = nbt.getLong("time");
            return new DefeatEntry(defeater, time);
        }

        final String defeater;

        final long   defeatTime;

        public DefeatEntry(String defeater, long time)
        {
            this.defeater = defeater;
            this.defeatTime = time;
        }

        void writeToNBT(NBTTagCompound nbt)
        {
            nbt.setString("player", defeater);
            nbt.setLong("time", defeatTime);
        }
    }

    long                          resetTime    = 0;
    public ArrayList<DefeatEntry> defeaters    = new ArrayList<DefeatEntry>();

    private boolean               randomize    = false;
    public int                    sight        = 0;
    public Vector3                location     = null;
    public String                 name         = "";
    public String                 playerName   = "";
    public String                 urlSkin      = "";
    public boolean                male         = true;
    boolean                       added        = false;
    protected boolean             trades       = true;
    public GuardAI                guardAI;
    public long                   visibleTime  = 0;
    public boolean                notifyDefeat = false;

    public EntityTrainer(World par1World)
    {
        this(par1World, null);
        inventoryHandsDropChances = new float[] { 1, 1 };
        inventoryArmorDropChances = new float[] { 1, 1, 1, 1 };
    }

    public EntityTrainer(World world, TypeTrainer type, int level)
    {
        this(world);
        initTrainer(type, level);
    }

    public EntityTrainer(World world, TypeTrainer type, int level, Vector3 location, boolean stationary)
    {
        this(world, location, true);
        initTrainer(type, level);
    }

    public EntityTrainer(World world, Vector3 location)
    {
        this(world, location, false);
    }

    public EntityTrainer(World par1World, Vector3 location, boolean stationary)
    {
        super(par1World);
        this.resetTime = battleCooldown;
        this.setSize(0.6F, 1.8F);
        initAI(location, stationary);
    }

    public boolean hasDefeated(Entity e)
    {
        if (e == null) return false;
        String name = e.getCachedUniqueIdString();
        for (DefeatEntry s : defeaters)
        {
            if (s.defeater.equals(name))
            {
                if (resetTime > 0)
                {
                    long diff = worldObj.getTotalWorldTime() - s.defeatTime;
                    if (diff > resetTime)
                    {
                        defeaters.remove(s);
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void addMobTrades(ItemStack buy1)
    {
        for (int i = 0; i < pokecubes.size(); i++)
        {
            ItemStack stack = pokecubes.get(i);
            if (PokecubeManager.isFilled(stack))
            {
                IPokemob mon = PokecubeManager.itemToPokemob(stack, worldObj);
                IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, worldObj);
                int stat = getBaseStats(mon);
                int stat1 = getBaseStats(mon1);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel()) continue;
                UUID trader1 = mon1.getPokemonOwnerID();
                boolean everstone = CompatWrapper.isValid(PokecubeManager.getHeldItemMainhand(stack)) && Tools
                        .isSameStack(PokecubeManager.getHeldItemMainhand(stack), PokecubeItems.getStack("everstone"));
                mon.setOriginalOwnerUUID(getUniqueID());
                mon.setPokemonOwner(trader1);
                mon.setTraded(!everstone);
                stack = PokecubeManager.pokemobToItem(mon);
                stack.getTagCompound().setInteger("slotnum", i);
                tradeList.add(new MerchantRecipe(buy1, stack));
            }
        }
    }

    @Override
    protected void addRandomTrades()
    {
        itemList.clear();
        itemList.addAll(type.getRecipes(this));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source.getEntity() != null && (source.getEntity() instanceof EntityLivingBase)
                && !(source.getEntity() instanceof EntityPlayer))
        {
            Entity entity = source.getEntity();
            if (entity instanceof IEntityOwnable)
            {
                if (((IEntityOwnable) entity).getOwner() != null)
                {
                    entity = ((IEntityOwnable) entity).getOwner();
                }
            }
            if (attackCooldown <= 0)
            {
                setTrainerTarget(entity);
                if (entity != source.getEntity()) return false;
            }
        }
        if (source == DamageSource.drown) return false;
        if (Config.instance.trainersInvul) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    /** Drop the equipment for this entity. */
    @Override
    protected void dropEquipment(boolean drop, int looting)
    {
        if (looting > 4) super.dropEquipment(drop, looting);
    }

    @Override
    public EntityLivingBase getAITarget()
    {
        return this.getTarget();
    }

    private int getBaseStats(IPokemob mob)
    {
        PokedexEntry entry = mob.getPokedexEntry();
        return entry.getStatHP() + entry.getStatATT() + entry.getStatDEF() + entry.getStatATTSPE()
                + entry.getStatDEFSPE() + entry.getStatVIT();
    }

    public boolean getShouldRandomize()
    {
        return randomize;
    }

    /** Sets the active target the Task system uses for tracking */
    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entity)
    {
        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            if (mob.getPokemonOwner() != null && getTarget() == null)
            {
                if (attackCooldown <= 0) setTarget(mob.getPokemonOwner());
                this.throwCubeAt(entity);
            }
        }
    }

    protected void initAI(Vector3 location, boolean stationary)
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new AITrainerFindTarget(this, EntityPlayer.class));
        this.tasks.addTask(1, new AITrainerBattle(this));
        this.tasks.addTask(1, new EntityAIMoveTowardsTarget(this, 0.6, 10));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(1, guardAI);
        if (location != null)
        {
            location.moveEntity(this);
            if (stationary) setStationary(location);
        }
    }

    @Override
    public void onDefeated(Entity defeater)
    {
        if (hasDefeated(defeater)) return;
        if (defeater != null)
            defeaters.add(new DefeatEntry(defeater.getCachedUniqueIdString(), worldObj.getTotalWorldTime()));
        if (reward != null && defeater instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) defeater;
            for (ItemStack i : reward)
            {
                if (!CompatWrapper.isValid(i)) continue;
                if (!player.inventory.addItemStackToInventory(i.copy()))
                {
                    EntityItem item = defeater.entityDropItem(i.copy(), 0.5f);
                    if (item == null)
                    {
                        System.out.println("Test" + item + " " + i);
                        continue;
                    }
                    item.setPickupDelay(0);
                }
                checkItemAchievement(i, player);
                ITextComponent text = getMessage(MessageState.GIVEITEM, this.getDisplayName(), i.getDisplayName(),
                        player.getDisplayName());
                defeater.addChatMessage(text);
                doAction(MessageState.GIVEITEM, player);
            }
            checkDefeatAchievement(player);
        }
        if (defeater != null)
        {
            ITextComponent text = getMessage(MessageState.DEFEAT, getDisplayName(), defeater.getDisplayName());
            defeater.addChatMessage(text);
            if (defeater instanceof EntityLivingBase) doAction(MessageState.DEFEAT, (EntityLivingBase) defeater);
            if (notifyDefeat && defeater instanceof EntityPlayerMP)
            {
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                packet.data.setInteger("I", getEntityId());
                packet.data.setLong("L", worldObj.getTotalWorldTime() + resetTime);
                PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) defeater);
            }
        }
        this.setTrainerTarget(null);
    }

    public void checkItemAchievement(ItemStack item, EntityPlayer player)
    {
        Achievement stat = null;
        if (item.getItem() instanceof ItemBadge)
        {
            for (String s : ItemBadge.variants)
            {
                if (Tools.isSameStack(item, PokecubeItems.getStack(s)))
                {
                    stat = PokecubePlayerStats.getAchievement("pokeadv." + s);
                    break;
                }
            }
        }
        if (stat != null)
        {
            player.addStat(stat);
        }
    }

    public void checkDefeatAchievement(EntityPlayer player)
    {
        boolean leader = this instanceof EntityLeader;
        Achievement achieve = PokecubePlayerStats
                .getAchievement(leader ? "pokeadv.defeat.leader" : "pokeadv.defeat.trainer");
        player.addStat(achieve);
    }

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

        if (getTarget() == null && getAIState(INBATTLE))
        {
            setAIState(INBATTLE, false);
        }

        if (!added)
        {
            added = true;
            TrainerSpawnHandler.addTrainerCoord(this);
        }
        ItemStack next;
        if (cooldown > worldObj.getTotalWorldTime()) next = CompatWrapper.nullStack;
        else next = getNextPokemob();
        if (CompatWrapper.isValid(next))
        {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, next);
        }
        else
        {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, CompatWrapper.nullStack);
        }

        if (CompatWrapper.isValid(type.held))
        {
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, type.held);
        }

        EntityLivingBase target = getAITarget() != null ? getAITarget()
                : getAttackTarget() != null ? getAttackTarget() : null;

        if (target != null)
        {
            this.faceEntity(target, 10, 10);
            getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
    }

    @Override
    public void onUpdate()
    {
        if (getNavigator().getPath() != null && getAIState(STATIONARY))
        {
            if (guardAI.capability.getPos().distanceSq(0, 0, 0) == 0)
            {
                setStationary(false);
                return;
            }
            Vector3 loc = Vector3.getNewVector().set(getNavigator().getPath().getFinalPathPoint());
            double d = (guardAI.capability.getPos().getX() - loc.x) * (guardAI.capability.getPos().getX() - loc.x)
                    + (guardAI.capability.getPos().getZ() - loc.z) * (guardAI.capability.getPos().getZ() - loc.z);
            double d1 = guardAI.capability.getRoamDistance() * guardAI.capability.getRoamDistance();
            if (d > d1)
            {
                getNavigator().clearPathEntity();
                getNavigator().tryMoveToXYZ(guardAI.capability.getPos().getX() + 0.5,
                        guardAI.capability.getPos().getY(), guardAI.capability.getPos().getZ() + 0.5, 0.5);
            }
        }
        super.onUpdate();
    }

    @Override
    public void populateBuyingList()
    {
        tradeList = new MerchantRecipeList();
        if (shouldrefresh) itemList = null;
        shouldrefresh = false;
        if (itemList == null && Config.instance.trainersTradeItems)
        {
            itemList = new MerchantRecipeList();
            addRandomTrades();
        }
        if (Config.instance.trainersTradeItems) tradeList.addAll(itemList);
        ItemStack buy = buyingPlayer.getHeldItemMainhand();
        if (PokecubeManager.isFilled(buy) && Config.instance.trainersTradeMobs)
        {
            addMobTrades(buy);
        }
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (player.capabilities.isCreativeMode && player.isSneaking())
        {
            if (getType() != null && !worldObj.isRemote && !CompatWrapper.isValid(player.getHeldItemMainhand()))
            {
                String message = this.getName() + " " + getAIState(STATIONARY) + " " + countPokemon() + " ";
                for (ItemStack i : pokecubes)
                {
                    if (CompatWrapper.isValid(i)) message += i.getDisplayName() + " ";
                }
                player.addChatMessage(new TextComponentString(message));
            }
            else if (!worldObj.isRemote && player.isSneaking() && player.getHeldItemMainhand().getItem() == Items.STICK)
            {
                throwCubeAt(player);
            }
            else if (CompatWrapper.isValid(player.getHeldItemMainhand())
                    && player.getHeldItemMainhand().getItem() == Items.STICK)
                setTarget(player);

            if (CompatWrapper.isValid(player.getHeldItemMainhand())
                    && player.getHeldItemMainhand().getItem() instanceof ItemTrainer)
            {
                player.openGui(PokecubeAdv.instance, PokecubeAdv.GUITRAINER_ID, worldObj, getEntityId(), 0, 0);
            }
        }
        else
        {
            if (CompatWrapper.isValid(player.getHeldItemMainhand()) && friendlyCooldown <= 0)
            {
                if (player.getHeldItemMainhand().getItem() == Item.REGISTRY
                        .getObject(new ResourceLocation("minecraft:emerald")))
                {
                    Item item = Item.REGISTRY.getObject(new ResourceLocation("minecraft:emerald"));
                    player.inventory.clearMatchingItems(item, 0, 1, null);
                    setTrainerTarget(null);
                    for (IPokemob pokemob : currentPokemobs)
                    {
                        pokemob.returnToPokecube();
                    }
                    friendlyCooldown = 2400;
                }
            }
            else if (friendlyCooldown >= 0)
            {
                this.setCustomer(player);
                if (!this.worldObj.isRemote && trades && (getRecipes(player) == null || this.tradeList.size() > 0))
                {
                    player.displayVillagerTradeGui(this);
                    return true;
                }
                this.setCustomer(null);
                return true;
            }
        }
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("trades")) trades = nbt.getBoolean("trades");
        playerName = nbt.getString("playerName");
        urlSkin = nbt.getString("urlSkin");
        randomize = nbt.getBoolean("randomTeam");
        male = nbt.getBoolean("gender");
        name = nbt.getString("name");
        sight = nbt.getInteger("sight");
        setTypes();
        defeaters.clear();
        if (nbt.hasKey("resetTime")) resetTime = nbt.getLong("resetTime");
        if (nbt.hasKey("DefeatList", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("DefeatList", 10);
            for (int i = 0; i < nbttaglist.tagCount(); i++)
                defeaters.add(DefeatEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
        }
        notifyDefeat = nbt.getBoolean("notifyDefeat");
    }

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
        PCEventsHandler.recallAllPokemobs(this);
        super.setDead();
    }

    public void setStationary(boolean stationary)
    {
    }

    public void setStationary(Vector3 location)
    {
        this.location = location;
        if (location == null)
        {
            setAIState(STATIONARY, false);
            guardAI.setPos(new BlockPos(0, 0, 0));
            guardAI.setTimePeriod(new TimePeriod(0, 0));
            return;
        }
        guardAI.setTimePeriod(TimePeriod.fullDay);
        guardAI.setPos(getPosition());
        setAIState(STATIONARY, true);
    }

    public void setTrainerTarget(Entity e)
    {
        setTarget((EntityLivingBase) e);
    }

    public void setTypes()
    {
        if (name.isEmpty())
        {
            int index = getEntityId() % (male ? TypeTrainer.maleNames.size() : TypeTrainer.femaleNames.size());
            name = (male ? TypeTrainer.maleNames.get(index) : TypeTrainer.femaleNames.get(index));
            this.setCustomNameTag(type.name + " " + name);
        }
    }

    @Override
    protected void trade(MerchantRecipe recipe)
    {
        ItemStack poke1 = recipe.getItemToBuy();
        ItemStack poke2 = recipe.getItemToSell();
        if (!(PokecubeManager.isFilled(poke1) && PokecubeManager.isFilled(poke2))) { return; }

        int num = poke2.getTagCompound().getInteger("slotnum");
        EntityLivingBase player2 = this;
        IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, worldObj);
        UUID trader2 = player2.getUniqueID();
        mon1.setPokemonOwner(trader2);
        poke1 = PokecubeManager.pokemobToItem(mon1);
        clear = true;
        setPokemob(num, poke2);
        shouldrefresh = true;
    }

    public void initTrainer(TypeTrainer type, int level)
    {
        this.type = type;
        byte genders = type.genders;
        if (genders == 1) male = true;
        if (genders == 2) male = false;
        if (genders == 3) male = Math.random() < 0.5;
        TypeTrainer.getRandomTeam(this, level, pokecubes, worldObj);
        if (randomize) shouldrefresh = true;
        if (type.hasBag)
        {
            this.setItemStackToSlot(EntityEquipmentSlot.CHEST, type.bag.copy());
        }
        setTypes();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setString("playerName", playerName);
        nbt.setString("urlSkin", urlSkin);
        nbt.setBoolean("trades", trades);
        nbt.setBoolean("gender", male);
        nbt.setBoolean("randomTeam", randomize);
        nbt.setString("name", name);
        nbt.setInteger("sight", sight);
        nbt.setLong("resetTime", resetTime);
        NBTTagList nbttaglist = new NBTTagList();
        for (DefeatEntry entry : defeaters)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entry.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        nbt.setTag("DefeatList", nbttaglist);
        nbt.setBoolean("notifyDefeat", notifyDefeat);
    }

    // TODO new mechant method names.
    public World func_190670_t_()
    {
        return this.worldObj;
    }

    // TODO new mechant method names.
    public BlockPos func_190671_u_()
    {
        return new BlockPos(this);
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        return processInteract(player, hand, player.getHeldItem(hand));
    }
}
