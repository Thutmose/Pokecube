package pokecube.adventures.entity.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public abstract class EntityHasPokemobs extends EntityHasAIStates
{
    private int              battleCooldown   = -1;
    public ItemStack[]       pokecubes        = new ItemStack[6];
    public List<ItemStack>   reward           = Lists.newArrayList(new ItemStack(Items.EMERALD));
    public int               attackCooldown   = 0;
    public int               cooldown         = 0;
    public int               friendlyCooldown = 0;
    public List<IPokemob>    currentPokemobs  = new ArrayList<IPokemob>();
    private EntityLivingBase target;
    public TypeTrainer       type;
    int                      despawncounter   = 0;
    private int              nextSlot         = 0;
    public UUID              outID;
    public IPokemob          outMob;

    public EntityHasPokemobs(World worldIn)
    {
        super(worldIn);
        if (battleCooldown == -1) battleCooldown = Config.instance.trainerCooldown;
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
        if (this.countPokemon() == 0 && !getAIState(STATIONARY) && !getAIState(PERMFRIENDLY))
        {
            despawncounter++;
            if (despawncounter > 50)
            {
                this.setDead();
            }
            return;
        }
        despawncounter = 0;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        // TODO remove this legacy support later
        if (nbt.hasKey("slot" + 0)) for (int n = 0; n < 6; n++)
        {
            if (nbt.hasKey("slot" + n))
            {
                NBTTagCompound tag = nbt.getCompoundTag("slot" + n);
                pokecubes[n] = ItemStack.loadItemStackFromNBT(tag);
                if (PokecubeManager.getPokedexNb(pokecubes[n]) == 0) pokecubes[n] = null;
            }
        }
        else if (nbt.hasKey("pokemobs", 9))
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
        attackCooldown = nbt.getInteger("cooldown");
        if (nbt.hasKey("outPokemob"))
        {
            outID = UUID.fromString(nbt.getString("outPokemob"));
        }
        friendlyCooldown = nbt.getInteger("friendly");
        nextSlot = nbt.getInteger("nextSlot");
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
        if (outID != null) nbt.setString("outPokemob", outID.toString());
        nbt.setString("type", type.name);
        nbt.setInteger("battleCD", battleCooldown);
        nbt.setInteger("cooldown", attackCooldown);
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
        cooldown--;
        if (getAIState(INBATTLE)) return;
        if (done)
        {
            attackCooldown = -1;
            nextSlot = 0;
        }
        else
        {
            attackCooldown--;
        }
    }

    public void setPokemob(int number, int level, int index)
    {
        if (number < 1)
        {
            pokecubes[index] = null;
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
    }

    public void throwCubeAt(Entity target)
    {
        if (target == null || getAIState(THROWING) || outMob != null) return;

        ItemStack i = getNextPokemob();
        if (getNextPokemob() != null)
        {
            this.setAIState(INBATTLE, true);
            IPokecube cube = (IPokecube) i.getItem();
            Vector3 here = Vector3.getNewVector().set(this);
            Vector3 t = Vector3.getNewVector().set(target);
            t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
            cube.throwPokecubeAt(worldObj, this, i, t, null);
            setAIState(THROWING, true);
            cooldown = Config.instance.trainerSendOutDelay;
            pokecubes[nextSlot] = null;
            ITextComponent text = new TextComponentTranslation("pokecube.trainer.toss", getDisplayName(),
                    i.getDisplayName());
            target.addChatMessage(text);
            nextSlot++;
            return;
        }
        else
        {
            attackCooldown = battleCooldown;
            nextSlot = -1;
        }

        this.setAIState(INBATTLE, false);
        if (outID == null && outMob == null && !getAIState(THROWING))
        {
            onDefeated(target);
        }
    }

    public void setTarget(EntityLivingBase target)
    {
        if (target != null && target != this.target)
        {
            cooldown = Config.instance.trainerBattleDelay;
            ITextComponent text = new TextComponentTranslation("pokecube.trainer.agress", getDisplayName());
            target.addChatMessage(text);
        }
        if (target == null)
        {
            if (this.target != null && this.getAIState(INBATTLE))
            {
                this.target.addChatMessage(new TextComponentTranslation("pokecube.trainer.forget", getDisplayName()));
            }
            this.setAIState(THROWING, false);
            this.setAIState(INBATTLE, false);
        }
        this.target = target;
    }

    public ItemStack getNextPokemob()
    {
        if (nextSlot < 0) return null;
        return pokecubes[nextSlot];
    }

    protected void resetPokemob()
    {
        nextSlot = 0;
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
