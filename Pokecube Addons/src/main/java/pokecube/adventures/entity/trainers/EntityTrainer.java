package pokecube.adventures.entity.trainers;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
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
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.ai.helper.PathNavigateTrainer;
import pokecube.adventures.commands.Config;
import pokecube.adventures.commands.GeneralCommands;
import pokecube.adventures.entity.helper.EntityTrainerBase;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs.DefeatEntry;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.handlers.TrainerSpawnHandler;
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
    private boolean randomize   = false;
    public Vector3  location    = null;
    public String   name        = "";
    public String   playerName  = "";
    public String   urlSkin     = "";
    boolean         added       = false;
    public GuardAI  guardAI;
    public long     visibleTime = 0;

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
        for (int i = 0; i < pokemobsCap.getMaxPokemobCount(); i++)
        {
            ItemStack stack = pokemobsCap.getPokemob(i);
            if (PokecubeManager.isFilled(stack))
            {
                IPokemob mon = PokecubeManager.itemToPokemob(stack, getEntityWorld());
                int stat = getBaseStats(mon);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel()) continue;
                UUID trader1 = mon1.getPokemonOwnerID();
                boolean everstone = CompatWrapper.isValid(PokecubeManager.getHeldItem(stack))
                        && Tools.isStack(PokecubeManager.getHeldItem(stack), "everstone");
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
                // Only set target if not already have one.
                if (pokemobsCap.getTarget() == null) pokemobsCap.setTarget((EntityLivingBase) entity);
                if (entity != source.getTrueSource()) return false;
            }
        }
        if (source == DamageSource.DROWN) return false;
        // Apply 0 damage to still count as an "attack"
        if (Config.instance.trainersInvul || aiStates.getAIState(IHasNPCAIStates.INVULNERABLE))
            return super.attackEntityFrom(source, 0.0f);
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
            }
        }
    }

    protected void initAI(Vector3 location, boolean stationary)
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(3, new EntityAIMoveTowardsTarget(this, 0.6, 10));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.tasks.addTask(9,
                new EntityAIWatchClosest2(this, EntityPlayer.class, Config.instance.trainerSightRange, 1.0F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, Config.instance.trainerSightRange));
        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(1, guardAI);
        if (location != null)
        {
            location.moveEntity(this);
            if (stationary) setStationary(location);
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn)
    {
        if (aiStates.getAIState(IHasNPCAIStates.STATIONARY)) return;
        super.collideWithEntity(entityIn);
    }

    @Override
    public void applyEntityCollision(Entity entityIn)
    {
        if (aiStates.getAIState(IHasNPCAIStates.STATIONARY)) return;
        super.applyEntityCollision(entityIn);
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
        if (pokemobsCap.getCooldown() > getEntityWorld().getTotalWorldTime()) next = ItemStack.EMPTY;
        else next = pokemobsCap.getNextPokemob();
        if (CompatWrapper.isValid(next))
        {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, next);
        }
        else
        {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
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
            if (guardAI.capability.getPrimaryTask().getPos() == null) { return; }
            guardAI.capability.getPrimaryTask().setActiveTime(TimePeriod.fullDay);
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
        if (player.capabilities.isCreativeMode && player.isSneaking())
        {
            if (pokemobsCap.getType() != null && !getEntityWorld().isRemote
                    && !CompatWrapper.isValid(player.getHeldItemMainhand()))
            {
                String message = this.getName() + " " + aiStates.getAIState(IHasNPCAIStates.STATIONARY) + " "
                        + pokemobsCap.countPokemon() + " ";
                for (int ind = 0; ind < pokemobsCap.getMaxPokemobCount(); ind++)
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
                if (!this.getEntityWorld().isRemote && aiStates.getAIState(IHasNPCAIStates.TRADES))
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
        playerName = nbt.getString("playerName");
        urlSkin = nbt.getString("urlSkin");
        if (nbt.hasKey("trades")) aiStates.setAIState(IHasNPCAIStates.TRADES, nbt.getBoolean("trades"));
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
        nbt.setBoolean("randomTeam", randomize);
        nbt.setString("name", name);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        return processInteract(player, hand, player.getHeldItem(hand));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn)
    {
        return new PathNavigateTrainer(this, worldIn);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable)
    {
        if (isChild() || this.getGrowingAge() > 0 || !aiStates.getAIState(IHasNPCAIStates.MATES)) return null;
        if (pokemobsCap.getGender() == 2)
        {
            IHasPokemobs other = CapabilityHasPokemobs.getHasPokemobs(ageable);
            IHasNPCAIStates otherAI = CapabilityNPCAIStates.getNPCAIStates(ageable);
            if (other != null && otherAI != null && otherAI.getAIState(IHasNPCAIStates.MATES) && other.getGender() == 1)
            {
                if (location == null) location = Vector3.getNewVector();
                EntityTrainer baby = TrainerSpawnHandler.getInstance().getTrainer(location.set(this), getEntityWorld());
                if (baby != null) baby.setGrowingAge(-24000);
                return baby;
            }
        }
        return null;
    }

    /** This is called when Entity's growing age timer reaches 0 (negative
     * values are considered as a child, positive as an adult) */
    protected void onGrowingAdult()
    {
    }
}
