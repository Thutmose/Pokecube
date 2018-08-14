package pokecube.adventures.entity.helper.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.events.PAEventsHandler;
import pokecube.adventures.events.PAEventsHandler.DataParamHolder;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class CapabilityHasPokemobs
{
    @CapabilityInject(IHasPokemobs.class)
    public static final Capability<IHasPokemobs> HASPOKEMOBS_CAP = null;
    public static Storage                        storage;

    public static IHasPokemobs getHasPokemobs(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        IHasPokemobs pokemobHolder = null;
        if (entityIn.hasCapability(HASPOKEMOBS_CAP, null))
            pokemobHolder = entityIn.getCapability(HASPOKEMOBS_CAP, null);
        else if (entityIn instanceof IHasPokemobs) return (IHasPokemobs) entityIn;
        return pokemobHolder;
    }

    public static interface ITargetWatcher
    {
        boolean validTargetSet(EntityLivingBase target);

        default void onAdded(IHasPokemobs pokemobs)
        {
        }

        default void onRemoved(IHasPokemobs pokemobs)
        {
        }
    }

    public static interface IHasPokemobs
    {
        public static enum LevelMode
        {
            CONFIG, YES, NO;
        }

        default Set<ITargetWatcher> getTargetWatchers()
        {
            return Collections.emptySet();
        }

        default void addTargetWatcher(ITargetWatcher watcher)
        {
            watcher.onAdded(this);
        }

        default void removeTargetWatcher(ITargetWatcher watcher)
        {
            watcher.onRemoved(this);
        }

        /** Adds the pokemob back into the inventory, healing it as needed. */
        default boolean addPokemob(ItemStack mob)
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
            boolean found = false;
            int foundID = -1;
            for (int i = 0; i < getMaxPokemobCount(); i++)
            {
                if (CompatWrapper.isValid(getPokemob(i)))
                {
                    if (getPokemob(i).hasTagCompound())
                    {
                        if (getPokemob(i).getTagCompound().hasKey("Pokemob"))
                        {
                            NBTTagCompound nbt = getPokemob(i).getTagCompound().getCompoundTag("Pokemob");
                            uuidLeastTest = nbt.getLong("UUIDLeast");
                            uuidMostTest = nbt.getLong("UUIDMost");
                            if (uuidLeast == uuidLeastTest && uuidMost == uuidMostTest)
                            {
                                found = true;
                                foundID = i;
                                if (canLevel())
                                {
                                    PokecubeManager.heal(mob);
                                    setPokemob(i, mob.copy());
                                }
                                else
                                {
                                    mob = getPokemob(i);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < getMaxPokemobCount(); i++)
            {
                if (!found && !CompatWrapper.isValid(getPokemob(i)))
                {
                    setPokemob(i, mob.copy());
                    PokecubeManager.heal(getPokemob(i));
                    break;
                }
                if (found && foundID == i) if (!CompatWrapper.isValid(getPokemob(i)))
                {
                    PokecubeManager.heal(mob);
                    setPokemob(i, mob.copy());
                    break;
                }
                else if (CompatWrapper.isValid(getPokemob(i)))
                {
                    PokecubeManager.heal(getPokemob(i));
                }
            }
            for (int i = 0; i < getMaxPokemobCount(); i++)
            {
                ItemStack stack = getPokemob(i);
                if (!CompatWrapper.isValid(stack))
                {
                    found = true;
                    for (int j = i; j < getMaxPokemobCount() - 1; j++)
                    {
                        setPokemob(j, getPokemob(j + 1));
                        setPokemob(j + 1, ItemStack.EMPTY);
                    }
                }
            }
            onAddMob();
            return found;
        }

        void setPokemob(int slot, ItemStack cube);

        ItemStack getPokemob(int slot);

        /** The next slot to be sent out. */
        int getNextSlot();

        void setNextSlot(int value);

        default boolean clearOnLoad()
        {
            return true;
        }

        default void clear()
        {
            for (int i = 0; i < getMaxPokemobCount(); i++)
                setPokemob(i, ItemStack.EMPTY);
        }

        /** The next pokemob to be sent out */
        default ItemStack getNextPokemob()
        {
            if (getNextSlot() < 0) return ItemStack.EMPTY;
            for (int i = 0; i < getMaxPokemobCount(); i++)
            {
                ItemStack stack = getPokemob(i);
                if (!CompatWrapper.isValid(stack))
                {
                    for (int j = i; j < getMaxPokemobCount() - 1; j++)
                    {
                        setPokemob(j, getPokemob(j + 1));
                        setPokemob(j + 1, ItemStack.EMPTY);
                    }
                }
            }
            return getPokemob(getNextSlot());
        }

        /** Resets the pokemobs; */
        void resetPokemob();

        default int countPokemon()
        {
            int ret = 0;
            for (int i = 0; i < getMaxPokemobCount(); i++)
            {
                if (PokecubeManager.getPokedexNb(getPokemob(i)) != 0) ret++;
            }
            return ret;
        }

        EntityLivingBase getTarget();

        void lowerCooldowns();

        void throwCubeAt(Entity target);

        void setTarget(EntityLivingBase target);

        TypeTrainer getType();

        void setType(TypeTrainer type);

        void onDefeated(Entity defeater);

        void onAddMob();

        /** This is the time when the next battle can start. it is in world
         * ticks. */
        long getCooldown();

        void setCooldown(long value);

        /** This is the cooldown for whether a pokemob can be sent out, it ticks
         * downwards, when less than 0, a mob may be thrown out as needed. */
        int getAttackCooldown();

        void setAttackCooldown(int value);

        void setOutMob(IPokemob mob);

        /** If we have a mob out, this should be it. */
        IPokemob getOutMob();

        void setOutID(UUID mob);

        UUID getOutID();

        /** Whether we should look for their target to attack. */
        default boolean isAgressive()
        {
            return true;
        }

        default boolean isAgressive(Entity target)
        {
            return isAgressive();
        }

        /** The distance to see for attacking players */
        default int getAgressDistance()
        {
            return Config.instance.trainerSightRange;
        }

        /** If we are agressive, is this a valid target? */
        boolean canBattle(EntityLivingBase target);

        /** 1 = male 2= female */
        byte getGender();

        /** 1 = male 2= female */
        void setGender(byte value);

        boolean canMegaEvolve();

        void setCanMegaEvolve(boolean flag);

        default int getMaxPokemobCount()
        {
            return 6;
        }

        void setLevelMode(LevelMode type);

        LevelMode getLevelMode();

        default boolean canLevel()
        {
            LevelMode type = getLevelMode();
            if (type == LevelMode.CONFIG) return Config.instance.trainerslevel;
            return type == LevelMode.YES ? true : false;
        }
    }

    public static class Storage implements Capability.IStorage<IHasPokemobs>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasPokemobs> capability, IHasPokemobs instance, EnumFacing side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(Capability<IHasPokemobs> capability, IHasPokemobs instance, EnumFacing side, NBTBase base)
        {
            if (instance instanceof INBTSerializable) ((INBTSerializable) instance).deserializeNBT(base);
        }

    }

    public static class DefaultPokemobs implements IHasPokemobs, ICapabilitySerializable<NBTTagCompound>
    {
        public static class DefeatEntry implements Comparable<DefeatEntry>
        {
            public static DefeatEntry createFromNBT(NBTTagCompound nbt)
            {
                String defeater = nbt.getString("player");
                long time = nbt.getLong("time");
                return new DefeatEntry(defeater, time);
            }

            final String defeater;
            long         defeatTime;

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

            @Override
            public int hashCode()
            {
                return defeater.hashCode();
            }

            @Override
            public boolean equals(Object other)
            {
                if (other instanceof DefeatEntry) { return ((DefeatEntry) other).defeater.equals(defeater); }
                return false;
            }

            @Override
            public int compareTo(DefeatEntry o)
            {
                return defeater.compareTo(o.defeater);
            }
        }

        public long                   resetTime        = 0;
        public int                    friendlyCooldown = 0;
        public ArrayList<DefeatEntry> defeaters        = new ArrayList<DefeatEntry>();

        // Should the client be notified of the defeat via a packet?
        public boolean                notifyDefeat     = false;

        // This is the reference cooldown.
        public int                    battleCooldown   = -1;
        private byte                  gender           = 1;
        private EntityLivingBase      user;
        private IHasNPCAIStates       aiStates;
        private IHasMessages          messages;
        private IHasRewards           rewards;
        private int                   nextSlot;
        // Cooldown between sending out pokemobs
        private int                   attackCooldown   = 0;
        // Cooldown between agression
        private long                  cooldown         = 0;
        private int                   sight            = -1;
        private TypeTrainer           type;
        private EntityLivingBase      target;
        private UUID                  outID;
        private boolean               canMegaEvolve    = false;
        private IPokemob              outMob;
        private List<ItemStack>       pokecubes;
        private LevelMode             levelmode        = LevelMode.CONFIG;
        private Set<ITargetWatcher>   watchers         = Sets.newHashSet();

        DataParamHolder               holder;

        public void init(EntityLivingBase user, IHasNPCAIStates aiStates, IHasMessages messages, IHasRewards rewards)
        {
            this.user = user;
            this.aiStates = aiStates;
            this.messages = messages;
            this.rewards = rewards;
            battleCooldown = Config.instance.trainerCooldown;
            resetTime = battleCooldown;
            holder = PAEventsHandler.getParameterHolder(user.getClass());
            if (!TypeTrainer.mobTypeMapper.shouldSync(user))
                pokecubes = NonNullList.<ItemStack> withSize(6, ItemStack.EMPTY);
        }

        public Set<ITargetWatcher> getTargetWatchers()
        {
            return watchers;
        }

        public void addTargetWatcher(ITargetWatcher watcher)
        {
            IHasPokemobs.super.addTargetWatcher(watcher);
            watchers.add(watcher);
        }

        public void removeTargetWatcher(ITargetWatcher watcher)
        {
            IHasPokemobs.super.removeTargetWatcher(watcher);
            watchers.remove(watcher);
        }

        public boolean hasDefeated(Entity e)
        {
            if (e == null) return false;
            String name = e.getCachedUniqueIdString();
            for (DefeatEntry s : defeaters)
            {
                if (s.defeater.equals(name))
                {
                    long diff = user.getEntityWorld().getTotalWorldTime() - s.defeatTime;
                    if (diff > resetTime) { return false; }
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getNextSlot()
        {
            return nextSlot;
        }

        @Override
        public ItemStack getPokemob(int slot)
        {
            if (pokecubes != null) return pokecubes.get(slot);
            return user.getDataManager().get(holder.pokemobs[slot]);
        }

        @Override
        public void setPokemob(int slot, ItemStack cube)
        {
            if (pokecubes != null)
            {
                pokecubes.set(slot, cube);
                return;
            }
            user.getDataManager().set(holder.pokemobs[slot], cube);
        }

        @Override
        public void resetPokemob()
        {
            setNextSlot(0);
            PCEventsHandler.recallAllPokemobs(user);
            aiStates.setAIState(IHasNPCAIStates.THROWING, false);
            aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
            setOutMob(null);
        }

        @Override
        public EntityLivingBase getTarget()
        {
            return target;
        }

        @Override
        public void lowerCooldowns()
        {
            if (aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY))
            {
                friendlyCooldown = 10;
                return;
            }
            if (friendlyCooldown-- >= 0) return;
            boolean done = getAttackCooldown() <= 0;
            if (done)
            {
                setAttackCooldown(-1);
                setNextSlot(0);
            }
            else if (getOutMob() == null && !aiStates.getAIState(IHasNPCAIStates.THROWING))
            {
                setAttackCooldown(getAttackCooldown() - 1);
            }
            if (aiStates.getAIState(IHasNPCAIStates.INBATTLE)) return;
            if (!done && getTarget() != null)
            {
                setTarget(null);
            }
        }

        @Override
        public void setTarget(EntityLivingBase target)
        {
            Set<ITargetWatcher> watchers = getTargetWatchers();
            if (target != null && !watchers.isEmpty())
            {
                boolean valid = false;
                for (ITargetWatcher watcher : watchers)
                {
                    if (watcher.validTargetSet(target))
                    {
                        valid = true;
                        break;
                    }
                }
                if (!valid) target = null;
            }
            if (!CompatWrapper.isValid(getPokemob(0)))
            {
                target = null;
                aiStates.setAIState(IHasNPCAIStates.THROWING, false);
                aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
                return;
            }
            if (target != null && target != this.target && attackCooldown <= 0)
            {
                attackCooldown = Config.instance.trainerBattleDelay;
                messages.sendMessage(MessageState.AGRESS, target, user.getDisplayName(), target.getDisplayName());
                messages.doAction(MessageState.AGRESS, target);
                aiStates.setAIState(IHasNPCAIStates.INBATTLE, true);
            }
            if (target == null)
            {
                if (this.target != null && aiStates.getAIState(IHasNPCAIStates.INBATTLE))
                {
                    messages.sendMessage(MessageState.DEAGRESS, this.target, user.getDisplayName(),
                            this.target.getDisplayName());
                    messages.doAction(MessageState.DEAGRESS, target);
                }
                aiStates.setAIState(IHasNPCAIStates.THROWING, false);
                aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
            }
            this.target = target;
        }

        @Override
        public TypeTrainer getType()
        {
            if (user.getEntityWorld().isRemote)
            {
                String t = user.getDataManager().get(holder.TYPE);
                return t.isEmpty() ? type : TypeTrainer.getTrainer(t);
            }
            return type;
        }

        @Override
        public int getAgressDistance()
        {
            return sight <= 0 ? Config.instance.trainerSightRange : sight;
        }

        @Override
        public void onDefeated(Entity defeater)
        {
            if (hasDefeated(defeater)) return;
            if (defeater instanceof EntityPlayer)
            {
                setCooldown(user.getEntityWorld().getTotalWorldTime() + battleCooldown);
                DefeatEntry entry = new DefeatEntry(defeater.getCachedUniqueIdString(),
                        user.getEntityWorld().getTotalWorldTime());
                if (defeaters.contains(entry))
                {
                    defeaters.get(defeaters.indexOf(entry)).defeatTime = entry.defeatTime;
                }
                else
                {
                    defeaters.add(entry);
                }
                if (rewards.getRewards() != null)
                {
                    EntityPlayer player = (EntityPlayer) defeater;
                    rewards.giveReward(player, user);
                    checkDefeatAchievement(player);
                }
            }
            else setCooldown(user.getEntityWorld().getTotalWorldTime() + 10);
            if (defeater != null)
            {
                messages.sendMessage(MessageState.DEFEAT, defeater, user.getDisplayName(), defeater.getDisplayName());
                if (defeater instanceof EntityLivingBase)
                    messages.doAction(MessageState.DEFEAT, (EntityLivingBase) defeater);
                if (notifyDefeat && defeater instanceof EntityPlayerMP)
                {
                    PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                    packet.data.setInteger("I", user.getEntityId());
                    packet.data.setLong("L", user.getEntityWorld().getTotalWorldTime() + resetTime);
                    PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) defeater);
                }
            }
            this.setTarget(null);
        }

        public void checkDefeatAchievement(EntityPlayer player)
        {
            if (!(user instanceof EntityTrainer)) return;
            boolean leader = user instanceof EntityLeader;
            if (leader) Triggers.BEATLEADER.trigger((EntityPlayerMP) player, (EntityTrainer) user);
            else Triggers.BEATTRAINER.trigger((EntityPlayerMP) player, (EntityTrainer) user);
        }

        @Override
        public void onAddMob()
        {
            if (getTarget() == null || aiStates.getAIState(IHasNPCAIStates.THROWING) || getOutMob() != null
                    || CompatWrapper.isValid(getNextPokemob()))
                return;
            aiStates.setAIState(IHasNPCAIStates.INBATTLE, false);
            if (getOutMob() == null && !aiStates.getAIState(IHasNPCAIStates.THROWING))
            {
                if (getCooldown() <= user.getEntityWorld().getTotalWorldTime())
                {
                    onDefeated(getTarget());
                    setNextSlot(0);
                }
            }
        }

        @Override
        public void throwCubeAt(Entity target)
        {
            if (target == null || aiStates.getAIState(IHasNPCAIStates.THROWING)) return;
            ItemStack i = getNextPokemob();
            if (CompatWrapper.isValid(i))
            {
                aiStates.setAIState(IHasNPCAIStates.INBATTLE, true);
                IPokecube cube = (IPokecube) i.getItem();
                Vector3 here = Vector3.getNewVector().set(user);
                Vector3 t = Vector3.getNewVector().set(target);
                t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
                cube.throwPokecubeAt(user.getEntityWorld(), user, i, t, null);
                aiStates.setAIState(IHasNPCAIStates.THROWING, true);
                attackCooldown = Config.instance.trainerSendOutDelay;
                messages.sendMessage(MessageState.SENDOUT, target, user.getDisplayName(), i.getDisplayName(),
                        target.getDisplayName());
                if (target instanceof EntityLivingBase)
                    messages.doAction(MessageState.SENDOUT, (EntityLivingBase) target);
                nextSlot++;
                if (nextSlot >= getMaxPokemobCount() || getNextPokemob() == null) nextSlot = -1;
                return;
            }
            nextSlot = -1;
        }

        @Override
        public int getAttackCooldown()
        {
            return attackCooldown;
        }

        @Override
        public void setAttackCooldown(int value)
        {
            this.attackCooldown = value;
        }

        @Override
        public void setNextSlot(int value)
        {
            this.nextSlot = value;
        }

        @Override
        public void setOutMob(IPokemob mob)
        {
            this.outMob = mob;
            if (mob == null) this.outID = null;
            else this.outID = mob.getEntity().getUniqueID();
        }

        @Override
        public IPokemob getOutMob()
        {
            return outMob;
        }

        @Override
        public void setOutID(UUID mob)
        {
            outID = mob;
            if (mob == null) outMob = null;
        }

        @Override
        public UUID getOutID()
        {
            return outID;
        }

        @Override
        public long getCooldown()
        {
            return cooldown;
        }

        @Override
        public void setCooldown(long value)
        {
            this.cooldown = value;
        }

        @Override
        public void setType(TypeTrainer type)
        {
            this.type = type;
            if (!user.getEntityWorld().isRemote) user.getDataManager().set(holder.TYPE, type == null ? "" : type.name);
        }

        @Override
        public boolean canBattle(EntityLivingBase target)
        {
            return !hasDefeated(target);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == HASPOKEMOBS_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? HASPOKEMOBS_CAP.cast(this) : null;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            for (int index = 0; index < getMaxPokemobCount(); index++)
            {
                ItemStack i = this.getPokemob(index);
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                if (CompatWrapper.isValid(i))
                {
                    i.writeToNBT(nbttagcompound);
                }
                nbttaglist.appendTag(nbttagcompound);
            }
            nbt.setTag("pokemobs", nbttaglist);
            nbt.setInteger("nextSlot", this.getNextSlot());
            if (this.getOutID() != null) nbt.setString("outPokemob", this.getOutID().toString());
            if (this.getType() != null) nbt.setString("type", this.getType().name);
            nbt.setLong("nextBattle", this.getCooldown());
            nbt.setByte("gender", this.getGender());

            if (this.battleCooldown < 0) this.battleCooldown = Config.instance.trainerCooldown;
            nbt.setInteger("battleCD", this.battleCooldown);
            nbttaglist = new NBTTagList();
            for (DefeatEntry entry : this.defeaters)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                entry.writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
            nbt.setTag("DefeatList", nbttaglist);
            nbt.setBoolean("notifyDefeat", this.notifyDefeat);
            nbt.setLong("resetTime", this.resetTime);
            if (this.sight != -1) nbt.setInteger("sight", this.sight);
            nbt.setInteger("friendly", this.friendlyCooldown);
            nbt.setString("levelMode", getLevelMode().name());
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            if (nbt.hasKey("pokemobs", 9))
            {
                if (clearOnLoad()) this.clear();
                NBTTagList nbttaglist = nbt.getTagList("pokemobs", 10);
                if (nbttaglist.tagCount() != 0)
                    for (int i = 0; i < Math.min(nbttaglist.tagCount(), getMaxPokemobCount()); ++i)
                    {
                    this.setPokemob(i, new ItemStack(nbttaglist.getCompoundTagAt(i)));
                    }
            }
            this.setType(TypeTrainer.getTrainer(nbt.getString("type")));
            this.setCooldown(nbt.getLong("nextBattle"));
            if (nbt.hasKey("outPokemob"))
            {
                this.setOutID(UUID.fromString(nbt.getString("outPokemob")));
            }
            this.setNextSlot(nbt.getInteger("nextSlot"));
            this.setCanMegaEvolve(nbt.getBoolean("megaevolves"));
            if (nbt.hasKey("gender")) this.setGender(nbt.getByte("gender"));
            if (this.getNextSlot() >= 6) this.setNextSlot(0);
            this.sight = nbt.hasKey("sight") ? nbt.getInteger("sight") : -1;
            if (nbt.hasKey("battleCD")) this.battleCooldown = nbt.getInteger("battleCD");
            if (this.battleCooldown < 0) this.battleCooldown = Config.instance.trainerCooldown;

            this.defeaters.clear();
            if (nbt.hasKey("resetTime")) this.resetTime = nbt.getLong("resetTime");
            if (nbt.hasKey("DefeatList", 9))
            {
                NBTTagList nbttaglist = nbt.getTagList("DefeatList", 10);
                for (int i = 0; i < nbttaglist.tagCount(); i++)
                    this.defeaters.add(DefeatEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
            }
            this.notifyDefeat = nbt.getBoolean("notifyDefeat");
            this.friendlyCooldown = nbt.getInteger("friendly");
            if (nbt.hasKey("levelMode")) this.setLevelMode(LevelMode.valueOf(nbt.getString("levelMode")));
        }

        @Override
        public boolean isAgressive()
        {
            return friendlyCooldown < 0;
        }

        @Override
        public byte getGender()
        {
            return gender;
        }

        @Override
        public void setGender(byte value)
        {
            this.gender = value;
        }

        @Override
        public boolean canMegaEvolve()
        {
            return canMegaEvolve;
        }

        @Override
        public void setCanMegaEvolve(boolean flag)
        {
            canMegaEvolve = flag;
        }

        @Override
        public void setLevelMode(LevelMode type)
        {
            if (type == null) type = LevelMode.CONFIG;
            this.levelmode = type;
        }

        @Override
        public LevelMode getLevelMode()
        {
            return levelmode;
        }
    }
}