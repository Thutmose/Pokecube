package pokecube.adventures.entity.helper.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs.DefeatEntry;
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

    public static interface IHasPokemobs
    {
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
            for (int i = 0; i < 6; i++)
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
                                if (Config.instance.trainerslevel)
                                {
                                    found = true;
                                    foundID = i;
                                    PokecubeManager.heal(mob);
                                    setPokemob(i, mob.copy());
                                }
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < 6; i++)
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
            for (int i = 0; i < 6; i++)
            {
                ItemStack stack = getPokemob(i);
                if (!CompatWrapper.isValid(stack))
                {
                    found = true;
                    for (int j = i; j < 5; j++)
                    {
                        setPokemob(j, getPokemob(j + 1));
                        setPokemob(j + 1, CompatWrapper.nullStack);
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

        default void clear()
        {
            for (int i = 0; i < 6; i++)
                setPokemob(i, CompatWrapper.nullStack);
        }

        /** The next pokemob to be sent out */
        default ItemStack getNextPokemob()
        {
            if (getNextSlot() < 0) return CompatWrapper.nullStack;
            for (int i = 0; i < 6; i++)
            {
                ItemStack stack = getPokemob(i);
                if (!CompatWrapper.isValid(stack))
                {
                    for (int j = i; j < 5; j++)
                    {
                        setPokemob(j, getPokemob(j + 1));
                        setPokemob(j + 1, CompatWrapper.nullStack);
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
            for (int i = 0; i < 6; i++)
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
    }

    public static class Storage implements Capability.IStorage<IHasPokemobs>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasPokemobs> capability, IHasPokemobs instance, EnumFacing side)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            for (int index = 0; index < 6; index++)
            {
                ItemStack i = instance.getPokemob(index);
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                if (CompatWrapper.isValid(i))
                {
                    i.writeToNBT(nbttagcompound);
                }
                nbttaglist.appendTag(nbttagcompound);
            }
            nbt.setTag("pokemobs", nbttaglist);
            nbt.setInteger("nextSlot", instance.getNextSlot());
            if (instance.getOutID() != null) nbt.setString("outPokemob", instance.getOutID().toString());
            if (instance.getType() != null) nbt.setString("type", instance.getType().name);
            nbt.setLong("nextBattle", instance.getCooldown());
            nbt.setByte("gender", instance.getGender());
            nbt.setBoolean("megaevolves", instance.canMegaEvolve());
            if (instance instanceof DefaultPokemobs)
            {
                DefaultPokemobs mobs = (DefaultPokemobs) instance;
                if (mobs.battleCooldown < 0) mobs.battleCooldown = Config.instance.trainerCooldown;
                nbt.setInteger("battleCD", mobs.battleCooldown);
                nbttaglist = new NBTTagList();
                for (DefeatEntry entry : mobs.defeaters)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    entry.writeToNBT(nbttagcompound);
                    nbttaglist.appendTag(nbttagcompound);
                }
                nbt.setTag("DefeatList", nbttaglist);
                nbt.setBoolean("notifyDefeat", mobs.notifyDefeat);
                nbt.setLong("resetTime", mobs.resetTime);
                if (mobs.sight != -1) nbt.setInteger("sight", mobs.sight);
                nbt.setInteger("friendly", mobs.friendlyCooldown);
            }
            return nbt;
        }

        @Override
        public void readNBT(Capability<IHasPokemobs> capability, IHasPokemobs instance, EnumFacing side, NBTBase base)
        {
            if (!(base instanceof NBTTagCompound)) return;
            NBTTagCompound nbt = (NBTTagCompound) base;
            if (nbt.hasKey("pokemobs", 9))
            {
                instance.clear();
                NBTTagList nbttaglist = nbt.getTagList("pokemobs", 10);
                if (nbttaglist.tagCount() != 0) for (int i = 0; i < Math.min(nbttaglist.tagCount(), 6); ++i)
                {
                    instance.setPokemob(i, CompatWrapper.fromTag(nbttaglist.getCompoundTagAt(i)));
                }
            }
            instance.setType(TypeTrainer.getTrainer(nbt.getString("type")));
            instance.setCooldown(nbt.getLong("nextBattle"));
            if (nbt.hasKey("outPokemob"))
            {
                instance.setOutID(UUID.fromString(nbt.getString("outPokemob")));
            }
            instance.setNextSlot(nbt.getInteger("nextSlot"));
            instance.setCanMegaEvolve(nbt.getBoolean("megaevolves"));
            if (nbt.hasKey("gender")) instance.setGender(nbt.getByte("gender"));
            if (instance.getNextSlot() >= 6) instance.setNextSlot(0);
            if (instance instanceof DefaultPokemobs)
            {
                DefaultPokemobs mobs = (DefaultPokemobs) instance;
                mobs.sight = nbt.hasKey("sight") ? nbt.getInteger("sight") : -1;
                if (nbt.hasKey("battleCD")) mobs.battleCooldown = nbt.getInteger("battleCD");
                if (mobs.battleCooldown < 0) mobs.battleCooldown = Config.instance.trainerCooldown;

                mobs.defeaters.clear();
                if (nbt.hasKey("resetTime")) mobs.resetTime = nbt.getLong("resetTime");
                if (nbt.hasKey("DefeatList", 9))
                {
                    NBTTagList nbttaglist = nbt.getTagList("DefeatList", 10);
                    for (int i = 0; i < nbttaglist.tagCount(); i++)
                        mobs.defeaters.add(DefeatEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
                }
                mobs.notifyDefeat = nbt.getBoolean("notifyDefeat");
                mobs.friendlyCooldown = nbt.getInteger("friendly");
            }
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
            if (!TypeTrainer.mobTypeMapper.shouldSync(user)) pokecubes = CompatWrapper.makeList(6);
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
            setCooldown(user.getEntityWorld().getTotalWorldTime() + battleCooldown);
        }

        @Override
        public EntityLivingBase getTarget()
        {
            return target;
        }

        @Override
        public void lowerCooldowns()
        {
            if (aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY)) { return; }
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
            if (defeater != null && defeater instanceof EntityPlayer)
            {
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
            }
            if (rewards.getRewards() != null && defeater instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) defeater;
                rewards.giveReward(player, user);
                checkDefeatAchievement(player);
            }
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
                    setCooldown(user.getEntityWorld().getTotalWorldTime() + battleCooldown);
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
                if (nextSlot >= 6 || getNextPokemob() == null) nextSlot = -1;
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
            return (NBTTagCompound) storage.writeNBT(HASPOKEMOBS_CAP, this, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            storage.readNBT(HASPOKEMOBS_CAP, this, null, nbt);
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
    }
}