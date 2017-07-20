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
                if (CompatWrapper.isValid(getPokecubes().get(i)))
                {
                    if (getPokecubes().get(i).hasTagCompound())
                    {
                        if (getPokecubes().get(i).getTagCompound().hasKey("Pokemob"))
                        {
                            NBTTagCompound nbt = getPokecubes().get(i).getTagCompound().getCompoundTag("Pokemob");
                            uuidLeastTest = nbt.getLong("UUIDLeast");
                            uuidMostTest = nbt.getLong("UUIDMost");
                            if (uuidLeast == uuidLeastTest && uuidMost == uuidMostTest)
                            {
                                if (Config.instance.trainerslevel)
                                {
                                    found = true;
                                    foundID = i;
                                    PokecubeManager.heal(mob);
                                    getPokecubes().set(i, mob.copy());
                                }
                                break;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < 6; i++)
            {
                if (found && foundID == i) if (!CompatWrapper.isValid(getPokecubes().get(i)))
                {
                    PokecubeManager.heal(mob);
                    getPokecubes().set(i, mob.copy());
                    break;
                }
                else if (CompatWrapper.isValid(getPokecubes().get(i)))
                {
                    PokecubeManager.heal(getPokecubes().get(i));
                }
            }
            for (int i = 0; i < 6; i++)
            {
                ItemStack stack = getPokecubes().get(i);
                if (!CompatWrapper.isValid(stack))
                {
                    found = true;
                    for (int j = i; j < 5; j++)
                    {
                        getPokecubes().set(j, getPokecubes().get(j + 1));
                        getPokecubes().set(j + 1, CompatWrapper.nullStack);
                    }
                }
            }
            onAddMob();
            return found;
        }

        /** The next slot to be sent out. */
        int getNextSlot();

        void setNextSlot(int value);

        List<ItemStack> getPokecubes();

        default void clear()
        {
            for (int i = 0; i < 6; i++)
                getPokecubes().set(i, CompatWrapper.nullStack);
        }

        /** The next pokemob to be sent out */
        default ItemStack getNextPokemob()
        {
            if (getNextSlot() < 0) return CompatWrapper.nullStack;
            List<ItemStack> pokecubes = getPokecubes();
            for (int i = 0; i < 6; i++)
            {
                ItemStack stack = pokecubes.get(i);
                if (!CompatWrapper.isValid(stack))
                {
                    for (int j = i; j < 5; j++)
                    {
                        pokecubes.set(j, pokecubes.get(j + 1));
                        pokecubes.set(j + 1, CompatWrapper.nullStack);
                    }
                }
            }
            return pokecubes.get(getNextSlot());
        }

        /** Resets the pokemobs; */
        void resetPokemob();

        default ItemStack getPokemob(int slot)
        {
            return getPokecubes().get(slot);
        }

        default void setPokemob(int slot, ItemStack cube)
        {
            getPokecubes().set(slot, cube);
        }

        default int countPokemon()
        {
            int ret = 0;
            for (ItemStack i : getPokecubes())
            {
                if (PokecubeManager.getPokedexNb(i) != 0) ret++;
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
    }

    public static class Storage implements Capability.IStorage<IHasPokemobs>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasPokemobs> capability, IHasPokemobs instance, EnumFacing side)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            for (ItemStack i : instance.getPokecubes())
            {
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
                    instance.getPokecubes().set(i, CompatWrapper.fromTag(nbttaglist.getCompoundTagAt(i)));
                }
            }
            instance.setType(TypeTrainer.getTrainer(nbt.getString("type")));
            instance.setCooldown(nbt.getLong("nextBattle"));
            if (nbt.hasKey("outPokemob"))
            {
                instance.setOutID(UUID.fromString(nbt.getString("outPokemob")));
            }
            instance.setNextSlot(nbt.getInteger("nextSlot"));
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
            }
        }

    }

    public static class DefaultPokemobs implements IHasPokemobs, ICapabilitySerializable<NBTTagCompound>
    {

        public static class DefeatEntry
        {
            public static DefeatEntry createFromNBT(NBTTagCompound nbt)
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

        public long                   resetTime        = 0;
        public int                    friendlyCooldown = 0;
        public ArrayList<DefeatEntry> defeaters        = new ArrayList<DefeatEntry>();

        // Should the client be notified of the defeat via a packet?
        public boolean                notifyDefeat     = false;

        // This is the reference cooldown.
        public int                    battleCooldown   = -1;
        private byte                  gender           = 1;
        private EntityLivingBase      user;
        private IHasNPCAIStates          aiStates;
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
        private IPokemob              outMob;
        private List<ItemStack>       pokecubes        = CompatWrapper.makeList(6);

        DataParamHolder               holder;
        // DataParameter<String> PARAM;

        public void init(EntityLivingBase user, IHasNPCAIStates aiStates, IHasMessages messages, IHasRewards rewards)
        {
            this.user = user;
            this.aiStates = aiStates;
            this.messages = messages;
            this.rewards = rewards;
            battleCooldown = Config.instance.trainerCooldown;
            resetTime = battleCooldown;
            holder = PAEventsHandler.getParameterHolder(user.getClass());
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
                        long diff = user.getEntityWorld().getTotalWorldTime() - s.defeatTime;
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

        @Override
        public int getNextSlot()
        {
            return nextSlot;
        }

        @Override
        public List<ItemStack> getPokecubes()
        {
            return pokecubes;
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
            if (!user.isServerWorld())
            {
                String t = user.getDataManager().get(holder.TYPE);
                return t.isEmpty() ? null : TypeTrainer.getTrainer(t);
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
            if (defeater != null) defeaters.add(
                    new DefeatEntry(defeater.getCachedUniqueIdString(), user.getEntityWorld().getTotalWorldTime()));
            System.out.println(defeater + " " + rewards.getRewards());
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
            if(!(user instanceof EntityTrainer)) return;
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
            else this.outID = ((Entity) mob).getUniqueID();
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
            if (user.isServerWorld()) user.getDataManager().set(holder.TYPE, type == null ? "" : type.name);
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

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? (T) this : null;
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
    }
}