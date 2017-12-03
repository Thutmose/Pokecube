package pokecube.adventures.entity.trainers;

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
import pokecube.adventures.entity.helper.EntityTrainerBase;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs.DefeatEntry;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.ItemTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class EntityTrainer extends EntityTrainerBase
{
    private boolean   randomize   = false;
    public Vector3    location    = null;
    public String     name        = "";
    public String     playerName  = "";
    public String     urlSkin     = "";
    boolean           added       = false;
    protected boolean trades      = true;
    public GuardAI    guardAI;
    public long       visibleTime = 0;

    public EntityTrainer(World par1World)
    {
        this(par1World, null);
        inventoryHandsDropChances = new float[] { 1, 1 };
        inventoryArmorDropChances = new float[] { 1, 1, 1, 1 };
    }

    public EntityTrainer(World world, TypeTrainer type, int level)
    {
        this(world);
        if (!world.isRemote) initTrainer(type, level);
    }

    public EntityTrainer(World world, TypeTrainer type, int level, Vector3 location, boolean stationary)
    {
        this(world, location, true);
        if (!world.isRemote) initTrainer(type, level);
    }

    public EntityTrainer(World world, Vector3 location)
    {
        this(world, location, false);
    }

    public EntityTrainer(World par1World, Vector3 location, boolean stationary)
    {
        super(par1World);
        this.setSize(0.6F, 1.8F);
        initAI(location, stationary);
    }

    private void addMobTrades(ItemStack buy1)
    {
        ItemStack buy = buy1.copy();
        IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, getEntityWorld());
        int stat1 = getBaseStats(mon1);
        for (int i = 0; i < 6; i++)
        {
            ItemStack stack = pokemobsCap.getPokemob(i);
            if (PokecubeManager.isFilled(stack))
            {
                IPokemob mon = PokecubeManager.itemToPokemob(stack, getEntityWorld());
                int stat = getBaseStats(mon);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel()) continue;
                UUID trader1 = mon1.getPokemonOwnerID();
                boolean everstone = CompatWrapper.isValid(PokecubeManager.getHeldItem(stack))
                        && Tools.isSameStack(PokecubeManager.getHeldItem(stack), PokecubeItems.getStack("everstone"));
                mon.setOriginalOwnerUUID(getUniqueID());
                mon.setPokemonOwner(trader1);
                mon.setTraded(!everstone);
                stack = PokecubeManager.pokemobToItem(mon);
                stack.getTagCompound().setInteger("slotnum", i);
                tradeList.add(new MerchantRecipe(buy, stack));
            }
        }
    }

    @Override
    protected void addRandomTrades()
    {
        itemList.clear();
        itemList.addAll(pokemobsCap.getType().getRecipes(this));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source.getTrueSource() != null && (source.getTrueSource() instanceof EntityLivingBase)
                && !(source.getTrueSource() instanceof EntityPlayer))
        {
            Entity entity = source.getTrueSource();
            if (entity instanceof IEntityOwnable)
            {
                if (((IEntityOwnable) entity).getOwner() != null)
                {
                    entity = ((IEntityOwnable) entity).getOwner();
                }
            }
            if (pokemobsCap.getAttackCooldown() <= 0)
            {
                pokemobsCap.setTarget((EntityLivingBase) entity);
                if (entity != source.getTrueSource()) return false;
            }
        }
        if (source == DamageSource.DROWN) return false;
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
    public EntityLivingBase getAttackTarget()
    {
        return pokemobsCap.getTarget();
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
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            if (mob.getPokemonOwner() != null && pokemobsCap.getTarget() == null)
            {
                if (pokemobsCap.getAttackCooldown() <= 0) pokemobsCap.setTarget(mob.getPokemonOwner());
                pokemobsCap.throwCubeAt(entity);
            }
        }
    }

    protected void initAI(Vector3 location, boolean stationary)
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
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
    public void onLivingUpdate()
    {
        if (GeneralCommands.TRAINERSDIE)
        {
            this.setDead();
            return;
        }

        super.onLivingUpdate();
        if (getEntityWorld().isRemote || pokemobsCap.getType() == null) return;

        if (pokemobsCap.getTarget() == null && aiStates.getAIState(IHasNPCAIStates.INBATTLE))
        {
            aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
        }

        if (!added)
        {
            added = true;
            TrainerSpawnHandler.addTrainerCoord(this);
        }
        ItemStack next;
        if (pokemobsCap.getCooldown() > getEntityWorld().getTotalWorldTime()) next = CompatWrapper.nullStack;
        else next = pokemobsCap.getNextPokemob();
        if (CompatWrapper.isValid(next))
        {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, next);
        }
        else
        {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, CompatWrapper.nullStack);
        }

        if (CompatWrapper.isValid(pokemobsCap.getType().held))
        {
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, pokemobsCap.getType().held);
        }

        EntityLivingBase target = getAttackTarget() != null ? getAttackTarget()
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
        if (getNavigator().getPath() != null && aiStates.getAIState(IHasNPCAIStates.STATIONARY))
        {
            if (guardAI.capability.getPos() == null || guardAI.capability.getPos().equals(BlockPos.ORIGIN))
            {
                setStationary(false);
                return;
            }
            guardAI.capability.setActiveTime(TimePeriod.fullDay);
        }
        super.onUpdate();
    }

    @Override
    public void populateBuyingList(EntityPlayer player)
    {
        tradeList = new MerchantRecipeList();
        if (shouldrefresh) itemList = null;
        shouldrefresh = false;
        if (itemList == null && Config.instance.trainersTradeItems)
        {
            itemList = new MerchantRecipeList();
            addRandomTrades();
        }
        if (player != null)
        {
            ItemStack buy = player.getHeldItemMainhand();
            if (PokecubeManager.isFilled(buy) && Config.instance.trainersTradeMobs)
            {
                addMobTrades(buy);
            }
        }
        if (Config.instance.trainersTradeItems) tradeList.addAll(itemList);
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (stack.getItem() instanceof ItemTrainer && player.capabilities.isCreativeMode)
        {
            player.openGui(PokecubeAdv.instance, PokecubeAdv.GUITRAINER_ID, getEntityWorld(), getEntityId(), 0, 0);
            return true;
        }
        if (player.capabilities.isCreativeMode && player.isSneaking())
        {
            if (pokemobsCap.getType() != null && !getEntityWorld().isRemote
                    && !CompatWrapper.isValid(player.getHeldItemMainhand()))
            {
                String message = this.getName() + " " + aiStates.getAIState(IHasNPCAIStates.STATIONARY) + " "
                        + pokemobsCap.countPokemon() + " ";
                for (int ind = 0; ind < 6; ind++)
                {
                    ItemStack i = pokemobsCap.getPokemob(ind);
                    if (CompatWrapper.isValid(i)) message += i.getDisplayName() + " ";
                }
                player.sendMessage(new TextComponentString(message));
            }
            else if (!getEntityWorld().isRemote && player.isSneaking()
                    && player.getHeldItemMainhand().getItem() == Items.STICK)
            {
                pokemobsCap.throwCubeAt(player);
            }
            else if (CompatWrapper.isValid(player.getHeldItemMainhand())
                    && player.getHeldItemMainhand().getItem() == Items.STICK)
                pokemobsCap.setTarget(player);
        }
        else
        {
            if (CompatWrapper.isValid(player.getHeldItemMainhand()) && pokemobsCap.friendlyCooldown <= 0)
            {
                if (player.getHeldItemMainhand().getItem() == Item.REGISTRY
                        .getObject(new ResourceLocation("minecraft:emerald")))
                {
                    Item item = Item.REGISTRY.getObject(new ResourceLocation("minecraft:emerald"));
                    player.inventory.clearMatchingItems(item, 0, 1, null);
                    pokemobsCap.setTarget(null);
                    for (IPokemob pokemob : currentPokemobs)
                    {
                        pokemob.returnToPokecube();
                    }
                    pokemobsCap.friendlyCooldown = 2400;
                }
                return true;
            }
            else if (pokemobsCap.friendlyCooldown >= 0)
            {
                if (!this.getEntityWorld().isRemote && trades)
                {
                    this.setCustomer(player);
                    player.displayVillagerTradeGui(this);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("trades")) trades = nbt.getBoolean("trades");
        playerName = nbt.getString("playerName");
        urlSkin = nbt.getString("urlSkin");
        randomize = nbt.getBoolean("randomTeam");
        name = nbt.getString("name");
        setTypes();
        if (nbt.hasKey("DefeatList"))
        {
            pokemobsCap.defeaters.clear();
            pokemobsCap.setGender((byte) (nbt.getBoolean("gender") ? 1 : 2));
            if (nbt.hasKey("resetTime")) pokemobsCap.resetTime = nbt.getLong("resetTime");
            if (nbt.hasKey("DefeatList", 9))
            {
                NBTTagList nbttaglist = nbt.getTagList("DefeatList", 10);
                for (int i = 0; i < nbttaglist.tagCount(); i++)
                    pokemobsCap.defeaters.add(DefeatEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
            }
            pokemobsCap.notifyDefeat = nbt.getBoolean("notifyDefeat");
        }
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
            aiStates.setAIState(IHasNPCAIStates.STATIONARY, false);
            guardAI.setPos(new BlockPos(0, 0, 0));
            guardAI.setTimePeriod(new TimePeriod(0, 0));
            return;
        }
        guardAI.setTimePeriod(TimePeriod.fullDay);
        guardAI.setPos(getPosition());
        aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
    }

    public void setTypes()
    {
        if (name.isEmpty())
        {
            int index = getEntityId()
                    % (pokemobsCap.getGender() == 1 ? TypeTrainer.maleNames.size() : TypeTrainer.femaleNames.size());
            name = (pokemobsCap.getGender() == 1 ? TypeTrainer.maleNames.get(index)
                    : TypeTrainer.femaleNames.get(index));
            this.setCustomNameTag(pokemobsCap.getType().name + " " + name);
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
        IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, getEntityWorld());
        UUID trader2 = player2.getUniqueID();
        mon1.setPokemonOwner(trader2);
        poke1 = PokecubeManager.pokemobToItem(mon1);
        clear = true;
        pokemobsCap.setPokemob(num, poke1);
        shouldrefresh = true;
    }

    public void initTrainer(TypeTrainer type, int level)
    {
        pokemobsCap.setType(type);
        byte genders = type.genders;
        if (genders == 1) pokemobsCap.setGender((byte) 1);
        if (genders == 2) pokemobsCap.setGender((byte) 2);
        if (genders == 3) pokemobsCap.setGender((byte) (Math.random() < 0.5 ? 1 : 2));
        TypeTrainer.getRandomTeam(pokemobsCap, this, level, getEntityWorld());
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
        nbt.setBoolean("randomTeam", randomize);
        nbt.setString("name", name);
    }

    // TODO new mechant method names.
    public World func_190670_t_()
    {
        return this.getEntityWorld();
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
