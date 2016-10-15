package pokecube.adventures.entity.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;

public abstract class EntityHasPokemobs extends EntityHasMessages
{
    protected int            battleCooldown   = -1;
    public ItemStack[]       pokecubes        = new ItemStack[6];
    public List<ItemStack>   reward           = Lists.newArrayList(new ItemStack(Items.EMERALD));
    // Cooldown between sending out pokemobs
    public int               attackCooldown   = 0;
    // Cooldown between agression
    public long              cooldown         = 0;
    // Cooldown for trading timer
    public int               friendlyCooldown = 0;
    public List<IPokemob>    currentPokemobs  = new ArrayList<IPokemob>();
    private EntityLivingBase target;
    public TypeTrainer       type;
    int                      despawncounter   = 0;
    private int              nextSlot         = 0;
    private UUID             outID;
    public IPokemob          outMob;

    public EntityHasPokemobs(World worldIn)
    {
        super(worldIn);
        if (battleCooldown == -1) battleCooldown = Config.instance.trainerCooldown;
    }

    public void addPokemob(ItemStack mob)
    {
        long uuidLeast = 0;
        long uuidMost = 0;

        if (mob.hasTagCompound())
        {
            if (mob.getTagCompound().hasKey("Pokemob"))
            {
                NBTTagCompound nbt = mob.getTagCompound().getCompoundTag("Pokemob");
                uuidLeast = nbt.getLong("UUIDLeast");
                uuidMost = nbt.getLong("UUIDMost");
            }
        }
        long uuidLeastTest = -1;
        long uuidMostTest = -1;
        for (int i = 0; i < 6; i++)
        {
            if (pokecubes[i] != null)
            {
                if (pokecubes[i].hasTagCompound())
                {
                    if (pokecubes[i].getTagCompound().hasKey("Pokemob"))
                    {
                        NBTTagCompound nbt = pokecubes[i].getTagCompound().getCompoundTag("Pokemob");
                        uuidLeastTest = nbt.getLong("UUIDLeast");
                        uuidMostTest = nbt.getLong("UUIDMost");
                        if (uuidLeast == uuidLeastTest && uuidMost == uuidMostTest)
                        {
                            if (Config.instance.trainerslevel)
                            {
                                PokecubeManager.heal(mob);
                                pokecubes[i] = mob.copy();
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < 6; i++)
        {
            if (pokecubes[i] == null)
            {
                PokecubeManager.heal(mob);
                pokecubes[i] = mob.copy();
                break;
            }
        }
        for (int i = 0; i < 6; i++)
        {
            ItemStack stack = pokecubes[i];
            if (stack == null)
            {
                for (int j = i; j < 5; j++)
                {
                    pokecubes[j] = pokecubes[j + 1];
                    pokecubes[j + 1] = null;
                }
            }
        }
        if (target == null || getAIState(THROWING) || outMob != null || getNextPokemob() != null) return;
        this.setAIState(INBATTLE, false);
        if (outMob == null && !getAIState(THROWING))
        {
            if (cooldown <= worldObj.getTotalWorldTime())
            {
                onDefeated(target);
                cooldown = worldObj.getTotalWorldTime() + battleCooldown;
                nextSlot = 0;
            }
        }
    }

    public int countPokemon()
    {
        int ret = 0;
        for (ItemStack i : pokecubes)
        {
            if (i != null && PokecubeManager.getPokedexNb(i) != 0) ret++;
        }
        return ret;
    }

    public int countThrownCubes()
    {
        final Entity owner = this;
        Predicate<EntityPokecube> matcher = new Predicate<EntityPokecube>()
        {
            @Override
            public boolean apply(EntityPokecube input)
            {
                boolean isOwner = false;
                if (PokecubeManager.isFilled(input.getEntityItem()))
                {
                    String name = PokecubeManager.getOwner(input.getEntityItem());
                    isOwner = name.equals(owner.getCachedUniqueIdString());
                    System.out.println(name + " " + owner.getCachedUniqueIdString() + " " + isOwner);

                }
                return isOwner;
            }
        };
        List<EntityPokecube> cubes = worldObj.getEntities(EntityPokecube.class, matcher);
        return cubes.size();
    }

    public EntityLivingBase getTarget()
    {
        return target;
    }

    public TypeTrainer getType()
    {
        return type;
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (!isServerWorld()) return;
        if (outID != null && outMob == null)
        {
            outMob = (IPokemob) getServer().worldServerForDimension(dimension).getEntityFromUuid(outID);
            if (outMob == null) outID = null;
        }
        if (this.countPokemon() == 0 && !getAIState(STATIONARY) && !getAIState(PERMFRIENDLY))
        {
            despawncounter++;
            if (despawncounter > 50)
            {
                this.setDead();
            }
            return;
        }
        if (this.ticksExisted % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0)
        {
            this.setHealth(Math.min(this.getHealth() + 1, this.getMaxHealth()));
        }
        despawncounter = 0;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("pokemobs", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("pokemobs", 10);
            for (int i = 0; i < Math.min(nbttaglist.tagCount(), 6); ++i)
            {
                pokecubes[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
            }
        }
        if (nbt.hasKey("reward", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("reward", 10);
            if (reward == null) reward = Lists.newArrayList();
            reward.clear();
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                this.reward.add(ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i)));
            }
        }
        type = TypeTrainer.getTrainer(nbt.getString("type"));
        if (nbt.hasKey("battleCD")) battleCooldown = nbt.getInteger("battleCD");
        if (battleCooldown < 0) battleCooldown = Config.instance.trainerCooldown;
        cooldown = nbt.getLong("nextBattle");
        if (nbt.hasKey("outPokemob"))
        {
            outID = UUID.fromString(nbt.getString("outPokemob"));
        }
        friendlyCooldown = nbt.getInteger("friendly");
        nextSlot = nbt.getInteger("nextSlot");
        if (nextSlot >= 6) nextSlot = 0;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        NBTTagList nbttaglist = new NBTTagList();
        for (ItemStack i : pokecubes)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            if (i != null)
            {
                i.writeToNBT(nbttagcompound);
            }
            nbttaglist.appendTag(nbttagcompound);
        }
        nbt.setTag("pokemobs", nbttaglist);
        if (battleCooldown < 0) battleCooldown = Config.instance.trainerCooldown;
        if (outMob != null) nbt.setString("outPokemob", ((Entity) outMob).getCachedUniqueIdString());
        nbt.setString("type", type.name);
        nbt.setInteger("battleCD", battleCooldown);
        nbt.setLong("nextBattle", cooldown);
        nbt.setInteger("friendly", friendlyCooldown);
        nbt.setInteger("nextSlot", nextSlot);
        nbttaglist = new NBTTagList();
        if (reward != null) for (int i = 0; i < this.reward.size(); ++i)
        {
            if (this.reward.get(i) != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                this.reward.get(i).writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }
        nbt.setTag("reward", nbttaglist);
    }

    public abstract void onDefeated(Entity defeater);

    public void lowerCooldowns()
    {
        if (getAIState(PERMFRIENDLY)) { return; }
        if (friendlyCooldown-- >= 0) return;
        boolean done = attackCooldown <= 0;
        if (done)
        {
            attackCooldown = -1;
            nextSlot = 0;
        }
        else if (outMob == null && !getAIState(THROWING))
        {
            attackCooldown--;
        }
        if (getAIState(INBATTLE)) return;
        if (!done && getTarget() != null)
        {
            setTarget(null);
        }
    }

    public void throwCubeAt(Entity target)
    {
        if (target == null || getAIState(THROWING)) return;
        ItemStack i = getNextPokemob();
        if (i != null)
        {
            this.setAIState(INBATTLE, true);
            IPokecube cube = (IPokecube) i.getItem();
            Vector3 here = Vector3.getNewVector().set(this);
            Vector3 t = Vector3.getNewVector().set(target);
            t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
            cube.throwPokecubeAt(worldObj, this, i, t, null);
            setAIState(THROWING, true);
            attackCooldown = Config.instance.trainerSendOutDelay;
            ITextComponent text = getMessage(MessageState.SENDOUT, getDisplayName(), i.getDisplayName(),
                    target.getDisplayName());
            target.addChatMessage(text);
            nextSlot++;
            if (nextSlot >= 6 || getNextPokemob() == null) nextSlot = -1;
            return;
        }
        nextSlot = -1;
    }

    public void setTarget(EntityLivingBase target)
    {
        if (target != null && target != this.target && attackCooldown <= 0)
        {
            attackCooldown = Config.instance.trainerBattleDelay;
            ITextComponent text = getMessage(MessageState.AGRESS, getDisplayName(), target.getDisplayName());
            target.addChatMessage(text);
            this.setAIState(INBATTLE, true);
        }
        if (target == null)
        {
            if (this.target != null && this.getAIState(INBATTLE))
            {
                this.target.addChatMessage(
                        getMessage(MessageState.DEAGRESS, getDisplayName(), this.target.getDisplayName()));
            }
            this.setAIState(THROWING, false);
            this.setAIState(INBATTLE, false);
        }
        this.target = target;
    }

    public ItemStack getNextPokemob()
    {
        if (nextSlot < 0) return null;
        for (int i = 0; i < 6; i++)
        {
            ItemStack stack = pokecubes[i];
            if (stack == null)
            {
                for (int j = i; j < 5; j++)
                {
                    pokecubes[j] = pokecubes[j + 1];
                    pokecubes[j + 1] = null;
                }
            }
        }
        return pokecubes[nextSlot];
    }

    public void resetPokemob()
    {
        nextSlot = 0;
        PCEventsHandler.recallAllPokemobs(this);
        this.setAIState(THROWING, false);
        this.setAIState(INBATTLE, false);
        outMob = null;
        cooldown = worldObj.getTotalWorldTime() + battleCooldown;
    }

    public ItemStack getPokemob(int slot)
    {
        return pokecubes[slot];
    }

    public void setPokemob(int slot, ItemStack cube)
    {
        pokecubes[slot] = cube;
    }

}