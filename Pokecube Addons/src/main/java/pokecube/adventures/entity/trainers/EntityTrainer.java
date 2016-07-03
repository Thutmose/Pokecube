package pokecube.adventures.entity.trainers;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.trainers.EntityAITrainer;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
import pokecube.adventures.entity.helper.EntityHasPokemobs;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.ItemTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.ItemTM;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class EntityTrainer extends EntityHasPokemobs
{

    private boolean   randomize  = false;

    public Vector3    location   = null;
    public String     name       = "";
    public String     playerName = "";
    public boolean    male       = true;
    boolean           added      = false;
    protected boolean trades     = true;
    public GuardAI    guardAI;

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

        this.setSize(0.6F, 1.8F);
        initAI(location, stationary);
    }

    private void addMobTrades(ItemStack buy1)
    {
        for (int i = 0; i < pokecubes.length; i++)
        {
            ItemStack stack = pokecubes[i];
            if (stack != null && PokecubeManager.isFilled(stack))
            {
                IPokemob mon = PokecubeManager.itemToPokemob(stack, worldObj);
                IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, worldObj);
                int stat = getBaseStats(mon);
                int stat1 = getBaseStats(mon1);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel()) continue;
                String trader1 = mon1.getPokemonOwnerName();
                mon.setOriginalOwnerUUID(getUniqueID());
                mon.setPokemonOwnerByName(trader1);
                stack = PokecubeManager.pokemobToItem(mon);
                stack.getTagCompound().setInteger("slotnum", i);
                tradeList.add(new MerchantRecipe(buy1, stack));
            }
        }
    }

    protected void addRandomTrades()
    {
        itemList.clear();
        int num = rand.nextInt(3);
        Set<Object> added = Sets.newHashSet();
        for (int i = 0; i < num; i++)
        {
            String name = HeldItemHandler.megaVariants.get(rand.nextInt(HeldItemHandler.megaVariants.size()));
            if (!added.contains(name))
            {
                ItemStack output = PokecubeItems.getStack(name);
                if (output == null) continue;
                added.add(name);
                int size = Config.instance.megaCost;
                if (name.endsWith("orb")) size = Config.instance.orbCost;
                else if (name.endsWith("charm")) size = Config.instance.shinyCost;
                if (size == -1) continue;
                ItemStack buy1 = new ItemStack(Items.EMERALD);
                buy1.stackSize = (size & 63);
                ItemStack buy2 = null;
                if (size > 64)
                {
                    buy2 = buy1.copy();
                    buy1.stackSize = 64;
                    buy2.stackSize = ((size - 64) & 63);
                    if (size - 64 >= 64) buy2.stackSize = 64;
                }
                else if (size == 64)
                {
                    buy1.stackSize = 64;
                }
                itemList.add(new MerchantRecipe(buy1, buy2, output));
            }
        }
        added.clear();
        num = rand.nextInt(3);
        ArrayList<String> moves = Lists.newArrayList(MovesUtils.moves.keySet());
        int randNum = rand.nextInt(moves.size());
        for (int i = 0; i < num; i++)
        {
            int index = (randNum + i) % moves.size();
            String name = moves.get(index);
            if (added.contains(name)) continue;
            added.add(name);
            ItemStack tm = PokecubeItems.getStack("tm");
            ItemStack in = new ItemStack(Items.EMERALD);
            in.stackSize = Config.instance.tmCost;
            if (in.stackSize == -1) continue;
            ItemTM.addMoveToStack(name, tm);
            itemList.add(new MerchantRecipe(in, tm));
        }
        added.clear();
        num = rand.nextInt(4);
        if (!cubeList.isEmpty()) for (int i = 0; i < num; i++)
        {
            CubeTrade trade = cubeList.get(rand.nextInt(cubeList.size()));
            if (added.contains(trade)) continue;
            added.add(trade);
            itemList.add(trade.getTrade());
        }
        if (Math.random() > 0.99)
        {
            PokeType type = PokeType.values()[rand.nextInt(PokeType.values().length)];
            if (type == PokeType.unknown) return;
            ItemStack badge = PokecubeItems.getStack("badge" + type);
            if (badge != null)
            {
                ItemStack in1 = new ItemStack(Items.EMERALD);
                int size = Config.instance.badgeCost;
                if (size == -1) return;
                in1.stackSize = (size & 63);
                ItemStack in2 = null;
                if (size > 64)
                {
                    in2 = in1.copy();
                    in1.stackSize = 64;
                    in2.stackSize = ((size - 64) & 63);
                    if (size - 64 >= 64) in2.stackSize = 64;
                }
                else if (size == 64)
                {
                    in1.stackSize = 64;
                }
                itemList.add(new MerchantRecipe(in1, in2, badge));
            }
        }
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
            else
            {
                return super.attackEntityFrom(source, amount);
            }
        }

        if (Config.instance.trainersInvul) return false;

        if (friendlyCooldown > 0) return false;

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
        this.tasks.addTask(1, new EntityAITrainer(this, EntityPlayer.class));
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

    public void onDefeated(Entity defeater)
    {
        if (reward != null && defeater instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) defeater;
            for (ItemStack i : reward)
            {
                if (i == null || i.getItem() == null) continue;
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
                ITextComponent text = new TextComponentTranslation("pokecube.trainer.drop", this.getDisplayName(),
                        i.getDisplayName());
                defeater.addChatMessage(text);
            }
        }
        if (defeater != null)
        {
            ITextComponent text = new TextComponentTranslation("pokecube.trainer.defeat", this.getDisplayName());
            defeater.addChatMessage(text);
        }
        this.setTrainerTarget(null);
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

        for (int i = 0; i < 6; i++)
        {
            ItemStack item = pokecubes[i];
            if (item != null && attackCooldown <= 0)
            {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item);
                if (this.getHeldItemOffhand() == null && reward != null && reward.size() > 0)
                    this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, reward.get(0));
                break;
            }
            if (i == 5)
            {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
                this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, null);
            }
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

    protected void populateBuyingList()
    {
        tradeList = new MerchantRecipeList();
        if (itemList == null && Config.instance.trainersTradeItems)
        {
            itemList = new MerchantRecipeList();
            addRandomTrades();
        }
        if (Config.instance.trainersTradeItems) tradeList.addAll(itemList);
        ItemStack buy = buyingPlayer.getHeldItemMainhand();
        if (buy != null && PokecubeManager.isFilled(buy) && Config.instance.trainersTradeMobs)
        {
            addMobTrades(buy);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (player.capabilities.isCreativeMode && player.isSneaking())
        {
            if (getType() != null && !worldObj.isRemote && player.getHeldItemMainhand() == null)
            {
                String message = this.getName() + " " + getAIState(STATIONARY) + " " + countPokemon() + " ";
                for (ItemStack i : pokecubes)
                {
                    if (i != null) message += i.getDisplayName() + " ";
                }
                player.addChatMessage(new TextComponentString(message));
            }
            else if (!worldObj.isRemote && player.isSneaking() && player.getHeldItemMainhand().getItem() == Items.STICK)
            {
                throwCubeAt(player);
            }
            else if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == Items.STICK)
                setTarget(player);

            if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemTrainer)
            {
                player.openGui(PokecubeAdv.instance, PokecubeAdv.GUITRAINER_ID, worldObj, getEntityId(), 0, 0);
            }
        }
        else
        {
            if (player.getHeldItemMainhand() != null && friendlyCooldown <= 0)
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
                else this.setCustomer(null);
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
        randomize = nbt.getBoolean("randomTeam");
        male = nbt.getBoolean("gender");
        name = nbt.getString("name");
        setTypes();
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

    protected void trade(MerchantRecipe recipe)
    {
        ItemStack poke1 = recipe.getItemToBuy();
        ItemStack poke2 = recipe.getItemToSell();
        if (!(PokecubeManager.isFilled(poke1) && PokecubeManager.isFilled(poke2))) { return; }

        int num = poke2.getTagCompound().getInteger("slotnum");
        EntityLivingBase player2 = this;
        IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, worldObj);
        String trader2 = player2.getUniqueID().toString();
        mon1.setPokemonOwnerByName(trader2);
        poke1 = PokecubeManager.pokemobToItem(mon1);
        clear = true;
        pokecubes[num] = poke1;
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
        setTypes();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setString("playerName", playerName);
        nbt.setBoolean("trades", trades);
        nbt.setBoolean("gender", male);
        nbt.setBoolean("randomTeam", randomize);
        nbt.setString("name", name);
    }
}
