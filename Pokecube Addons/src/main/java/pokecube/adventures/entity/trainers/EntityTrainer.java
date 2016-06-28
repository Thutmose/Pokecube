package pokecube.adventures.entity.trainers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.trainers.EntityAITrainer;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.ItemTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityTrainer extends EntityAgeable implements IEntityAdditionalSpawnData, INpc, IMerchant
{
    public static class CubeTrade
    {
        final int   cubeId;
        final int[] amtRange = { 0, 0 };
        final int   cost;

        public CubeTrade(int cubeId, int min, int max, int cost)
        {
            this.cubeId = cubeId;
            amtRange[0] = min;
            amtRange[1] = max;
            this.cost = cost;
        }

        public MerchantRecipe getTrade()
        {
            ItemStack sell = new ItemStack(PokecubeItems.getEmptyCube(cubeId));
            sell.stackSize = amtRange[0];
            if (amtRange[0] < amtRange[1])
            {
                sell.stackSize += new Random().nextInt(amtRange[1] - amtRange[0] + 1);
            }
            ItemStack buy1 = new ItemStack(Items.EMERALD);
            buy1.stackSize = (cost & 63);
            ItemStack buy2 = null;
            if (cost > 64)
            {
                buy2 = buy1.copy();
                buy1.stackSize = 64;
                buy2.stackSize = ((cost - 64) & 63);
                if (cost - 64 >= 64) buy2.stackSize = 64;
            }
            else if (cost == 64)
            {
                buy1.stackSize = 64;
            }
            return new MerchantRecipe(buy1, buy2, sell);
        }
    }

    static final DataParameter<Integer> AIACTIONSTATESDW = EntityDataManager.<Integer> createKey(EntityTrainer.class,
            DataSerializers.VARINT);
    public static final int             STATIONARY       = 1;
    public static final int             ANGRY            = 2;
    public static final int             THROWING         = 4;
    public static final int             PERMFRIENDLY     = 8;

    public static ArrayList<CubeTrade>  cubeList         = Lists.newArrayList();
    private int                         battleCooldown   = -1;
    /** This villager's current customer. */
    private EntityPlayer                buyingPlayer;
    /** Initialises the MerchantRecipeList.java */
    private MerchantRecipeList          tradeList;
    /** Initialises the MerchantRecipeList.java */
    protected MerchantRecipeList        itemList;

    private boolean                     randomize        = false;
    public ItemStack[]                  pokecubes        = new ItemStack[6];
    public ItemStack                    reward           = new ItemStack(Items.EMERALD);
    public int[]                        pokenumbers      = new int[6];
    public int[]                        pokelevels       = new int[6];
    public int[]                        attackCooldown   = new int[6];
    public int                          cooldown         = 0;
    public int                          globalCooldown   = 0;
    public int                          friendlyCooldown = 0;
    public List<IPokemob>               currentPokemobs  = new ArrayList<IPokemob>();
    private EntityLivingBase            target;
    public TypeTrainer                  type;
    public Vector3                      location         = null;
    public String                       name             = "";
    public String                       playerName       = "";
    public UUID                         outID;
    public IPokemob                     outMob;
    public boolean                      male             = true;
    boolean                             clear            = false;
    boolean                             shouldrefresh    = false;
    boolean                             added            = false;
    protected boolean                   trades           = true;
    public GuardAI                      guardAI;

    int                                 timercounter     = 0;

    public EntityTrainer(World par1World)
    {
        this(par1World, null);
        if (battleCooldown == -1) battleCooldown = Config.instance.trainerCooldown;
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
                if (stat >= stat1 && mon.getLevel() >= mon1.getLevel()) continue;
                String trader1 = mon1.getPokemonOwnerName();
                mon.setOriginalOwnerUUID(getUniqueID());
                mon.setPokemonOwnerByName(trader1);
                stack = PokecubeManager.pokemobToItem(mon);
                stack.getTagCompound().setInteger("slotnum", i);
                tradeList.add(new MerchantRecipe(buy1, stack));
            }
        }
    }

    private int getBaseStats(IPokemob mob)
    {
        PokedexEntry entry = mob.getPokedexEntry();
        return entry.getStatHP() + entry.getStatATT() + entry.getStatDEF() + entry.getStatATTSPE()
                + entry.getStatDEFSPE() + entry.getStatVIT();
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

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    public int countPokemon()
    {
        if (outID != null && outMob == null)
        {
            for (int i = 0; i < worldObj.loadedEntityList.size(); ++i)
            {
                Entity entity = worldObj.loadedEntityList.get(i);
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

    @Override
    public EntityAgeable createChild(EntityAgeable p_90011_1_)
    {
        return null;
    }

    /** Drop the equipment for this entity. */
    @Override
    protected void dropEquipment(boolean drop, int looting)
    {
        if (looting > 4) super.dropEquipment(drop, looting);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(AIACTIONSTATESDW, 0);// more action states
    }

    public boolean getAIState(int state)
    {

        return (dataManager.get(AIACTIONSTATESDW) & state) != 0;
    }

    @Override
    public EntityLivingBase getAITarget()
    {
        return this.getTarget();
    }

    @Override
    public EntityPlayer getCustomer()
    {
        return buyingPlayer;
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player)
    {
        if (player.openContainer instanceof ContainerMerchant)
        {
            InventoryMerchant inv = ((ContainerMerchant) player.openContainer).getMerchantInventory();
            if (clear)
            {
                inv.removeStackFromSlot(0);
            }
            clear = false;
        }
        if (this.tradeList == null || shouldrefresh)
        {
            shouldrefresh = false;
            this.populateBuyingList();
        }
        return this.tradeList;
    }

    public boolean getShouldRandomize()
    {
        return randomize;
    }

    public EntityLivingBase getTarget()
    {
        return target;
    }

    public TypeTrainer getType()
    {
        return type;
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

    public void lowerCooldowns()
    {
        if (getAIState(PERMFRIENDLY)) { return; }
        if (friendlyCooldown-- >= 0) return;
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

    public void onDefeated(Entity defeater)
    {
        for (int i = 1; i < 5; i++)
        {
            EntityEquipmentSlot slotIn = EntityEquipmentSlot.values()[i];
            ItemStack stack = getItemStackFromSlot(slotIn);
            if (stack != null) this.entityDropItem(stack.copy(), 0.5f);
        }
        if (reward != null)
        {
            EntityItem item = defeater.entityDropItem(reward.copy(), 0.5f);
            item.setPickupDelay(0);
        }
        if (defeater != null)
        {
            ITextComponent text = new TextComponentTranslation("pokecube.trainer.defeat", this.getDisplayName());
            target.addChatMessage(text);
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
        if (worldObj.isRemote) return;

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
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item);
                if (this.getHeldItemOffhand() == null && reward != null)
                    this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, reward);
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

        if (this.countPokemon() == 0 && !getAIState(STATIONARY) && !getAIState(PERMFRIENDLY))
        {
            timercounter++;
            if (timercounter > 50)
            {
                this.setDead();
            }
            return;
        }
        timercounter = 0;
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

    private void populateBuyingList()
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
        for (int n = 0; n < 6; n++)
        {
            NBTBase temp = nbt.getTag("slot" + n);
            if (temp instanceof NBTTagCompound)
            {
                NBTTagCompound tag = (NBTTagCompound) temp;
                pokecubes[n] = ItemStack.loadItemStackFromNBT(tag);
                if (PokecubeManager.getPokedexNb(pokecubes[n]) == 0) pokecubes[n] = null;
                else
                {
                    IPokemob poke = PokecubeManager.itemToPokemob(pokecubes[n], worldObj);
                    if (poke != null)
                    {
                        pokenumbers[n] = poke.getPokedexNb();
                        pokelevels[n] = poke.getLevel();
                    }
                }
            }
        }
        if (nbt.hasKey("Offers", 10))
        {
            NBTTagCompound nbttagcompound = nbt.getCompoundTag("Offers");
            this.itemList = new MerchantRecipeList(nbttagcompound);
        }
        if (nbt.hasKey("reward"))
        {
            NBTTagCompound rewardTag = nbt.getCompoundTag("reward");
            reward = ItemStack.loadItemStackFromNBT(rewardTag);
        }
        if (nbt.hasKey("trades")) trades = nbt.getBoolean("trades");
        dataManager.set(AIACTIONSTATESDW, nbt.getInteger("aiState"));
        playerName = nbt.getString("playerName");
        randomize = nbt.getBoolean("randomTeam");
        type = TypeTrainer.getTrainer(nbt.getString("type"));
        if (nbt.hasKey("outPokemob"))
        {
            outID = UUID.fromString(nbt.getString("outPokemob"));
        }
        if (nbt.hasKey("battleCD")) battleCooldown = nbt.getInteger("battleCD");
        globalCooldown = nbt.getInteger("cooldown");
        attackCooldown = nbt.getIntArray("cooldowns");
        male = nbt.getBoolean("gender");
        name = nbt.getString("name");
        if (attackCooldown.length != 6) attackCooldown = new int[6];

        friendlyCooldown = nbt.getInteger("friendly");

        setTypes();
        checkTradeIntegrity();
    }

    private void checkTradeIntegrity()
    {
        if (itemList == null) return;
        List<MerchantRecipe> toRemove = Lists.newArrayList();
        for (MerchantRecipe r : itemList)
        {
            if (r.getItemToSell() == null || r.getItemToSell().getItem() == null)
            {
                toRemove.add(r);
                continue;
            }
            boolean hasBuy = r.getItemToBuy() != null && r.getItemToBuy().getItem() != null;
            hasBuy = hasBuy || (r.getSecondItemToBuy() != null && r.getSecondItemToBuy().getItem() != null);
            if (!hasBuy)
            {
                toRemove.add(r);
            }
        }
        itemList.removeAll(toRemove);
    }

    @Override
    public void readSpawnData(ByteBuf buff)
    {
        try
        {
            NBTTagCompound tag = new PacketBuffer(buff).readNBTTagCompoundFromBuffer();
            this.readEntityFromNBT(tag);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setAIState(int state, boolean flag)
    {
        int byte0 = dataManager.get(AIACTIONSTATESDW);

        Integer toSet;
        if (flag)
        {
            toSet = Integer.valueOf((byte0 | state));
        }
        else
        {
            toSet = Integer.valueOf((byte0 & -state - 1));
        }
        dataManager.set(AIACTIONSTATESDW, toSet);
    }

    @Override
    public void setCustomer(EntityPlayer player)
    {
        tradeList = null;
        this.buyingPlayer = player;
    }

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
        PCEventsHandler.recallAllPokemobs(this);
        super.setDead();
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

    @Override
    @SideOnly(Side.CLIENT)
    public void setRecipes(MerchantRecipeList recipeList)
    {
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

    public void setTarget(EntityLivingBase target)
    {
        if (target != null && target != this.target)
        {
            cooldown = 100;
            ITextComponent text = new TextComponentTranslation("pokecube.trainer.agress", getDisplayName());
            target.addChatMessage(text);
        }
        this.target = target;
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
        }
        this.setCustomNameTag(type.name + " " + name);
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

                ITextComponent text = new TextComponentTranslation("pokecube.trainer.toss", getDisplayName(),
                        i.getDisplayName());
                target.addChatMessage(text);

                for (int k = j + 1; k < 6; k++)
                {
                    attackCooldown[k] = 20;
                }
                return;
            }
            if (i != null && attackCooldown[j] < 30) { return; }
        }
        if (globalCooldown > 0 && outID == null && outMob == null && !getAIState(THROWING))
        {
            globalCooldown = 0;
            onDefeated(target);
        }
    }

    private void trade(MerchantRecipe recipe)
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
        pokenumbers[num] = mon1.getPokedexNb();
        pokelevels[num] = mon1.getLevel();
        shouldrefresh = true;
    }

    @Override
    public void useRecipe(MerchantRecipe recipe)
    {
        trade(recipe);
        this.livingSoundTime = -this.getTalkInterval();
        this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        int i = 3 + this.rand.nextInt(4);
        if (recipe.getRewardsExp())
        {
            this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY + 0.5D, this.posZ, i));
        }
    }

    @Override
    public void verifySellingItem(ItemStack stack)
    {
        if (!this.worldObj.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20)
        {
            this.livingSoundTime = -this.getTalkInterval();

            if (stack != null)
            {
                this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
            }
            else
            {
                this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
            }
        }
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
        if (this.itemList != null)
        {
            checkTradeIntegrity();
            nbt.setTag("Offers", this.itemList.getRecipiesAsTags());
        }
        nbt.setString("playerName", playerName);
        nbt.setBoolean("trades", trades);
        nbt.setBoolean("gender", male);
        nbt.setInteger("battleCD", battleCooldown);
        nbt.setBoolean("randomTeam", randomize);
        nbt.setString("name", name);
        nbt.setString("type", type.name);
        if (outID != null) nbt.setString("outPokemob", outID.toString());
        nbt.setInteger("aiState", dataManager.get(AIACTIONSTATESDW));
        nbt.setInteger("cooldown", globalCooldown);
        nbt.setIntArray("cooldowns", attackCooldown);
        nbt.setInteger("friendly", friendlyCooldown);
        if (reward != null && reward.getItem() != null)
        {
            NBTTagCompound rewardTag = new NBTTagCompound();
            reward.writeToNBT(rewardTag);
            nbt.setTag("reward", rewardTag);
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeEntityToNBT(tag);
        new PacketBuffer(buffer).writeNBTTagCompoundToBuffer(tag);
    }
}
