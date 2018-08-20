package pokecube.adventures.events;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.helper.AIStuffHolder;
import pokecube.adventures.ai.tasks.AIBattle;
import pokecube.adventures.ai.tasks.AIFindTarget;
import pokecube.adventures.ai.tasks.AIMate;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.EntityTrainerBase;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.items.ItemTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.PCEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.moves.TerrainDamageSource;
import pokecube.core.utils.PokeType;
import thut.api.entity.ai.IAIMob;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;

public class PAEventsHandler
{
    final ResourceLocation POKEMOBSCAP = new ResourceLocation(PokecubeAdv.ID, "pokemobs");
    final ResourceLocation AICAP       = new ResourceLocation(PokecubeAdv.ID, "ai");
    final ResourceLocation MESSAGECAP  = new ResourceLocation(PokecubeAdv.ID, "messages");
    final ResourceLocation REWARDSCAP  = new ResourceLocation(PokecubeAdv.ID, "rewards");
    final ResourceLocation AISTUFFCAP  = new ResourceLocation(PokecubeAdv.ID, "aiStuff");

    public static void randomizeTrainerTeam(Entity trainer, IHasPokemobs mobs)
    {
        Vector3 loc = Vector3.getNewVector().set(trainer);
        // Set level based on what wild pokemobs have.
        int level = SpawnHandler.getSpawnLevel(trainer.getEntityWorld(), loc, Pokedex.getInstance().getFirstEntry());
        if (trainer instanceof EntityLeader)
        {
            // Gym leaders are 10 lvls higher than others.
            level += 10;
            // Randomize badge for leader.
            if (((EntityLeader) trainer).randomBadge())
            {
                IHasRewards rewardsCap = ((EntityLeader) trainer).rewardsCap;
                PokeType type = PokeType.values()[new Random().nextInt(PokeType.values().length)];
                Item item = Item.getByNameOrId(PokecubeAdv.ID + ":badge_" + type);
                if (item != null)
                {
                    ItemStack badge = new ItemStack(item);
                    if (!rewardsCap.getRewards().isEmpty()) rewardsCap.getRewards().set(0, new Reward(badge));
                    else rewardsCap.getRewards().add(new Reward(badge));
                    ((EntityLeader) trainer).setHeldItem(EnumHand.OFF_HAND, rewardsCap.getRewards().get(0).stack);
                }
            }
        }
        // Randomize team.
        if (trainer instanceof EntityTrainer)
        {
            EntityTrainer t = (EntityTrainer) trainer;
            t.name = "";
            // Reset their trades, as this will randomize them when trades are
            // needed later.
            t.populateBuyingList(null);
            // Init for trainers randomizes their teams
            if (mobs.getType() != null) t.initTrainer(mobs.getType(), level);
        }
        else if (mobs.getType() != null)
        {
            mobs.setType(mobs.getType());
            byte genders = mobs.getType().genders;
            if (genders == 1) mobs.setGender((byte) 1);
            if (genders == 2) mobs.setGender((byte) 2);
            if (genders == 3) mobs.setGender((byte) (Math.random() < 0.5 ? 1 : 2));
            TypeTrainer.getRandomTeam(mobs, (EntityLivingBase) trainer, level, trainer.getEntityWorld());
        }
    }

    @SubscribeEvent
    public void TrainerPokemobPC(PCEvent evt)
    {
        if (evt.owner instanceof EntityTrainer)
        {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent(receiveCanceled = false)
    public void TrainerRecallEvent(pokecube.core.events.RecallEvent evt)
    {
        IPokemob recalled = evt.recalled;
        EntityLivingBase owner = recalled.getPokemonOwner();
        if (owner == null) return;
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(owner);
        if (pokemobHolder != null)
        {
            if (recalled == pokemobHolder.getOutMob())
            {
                pokemobHolder.setOutMob(null);
            }
            pokemobHolder.addPokemob(PokecubeManager.pokemobToItem(recalled));
        }
    }

    @SubscribeEvent
    public void StructureSpawn(StructureEvent.SpawnEntity event)
    {
        IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(event.getEntity());
        if (mobs == null) return;
        boolean randomize = event.getEntity().getEntityData().getBoolean("randomizeTeam");
        if (event.getEntity() instanceof EntityTrainer)
        {
            randomize = ((EntityTrainer) event.getEntity()).getShouldRandomize();
        }
        if (randomize)
        {
            randomizeTrainerTeam(event.getEntity(), mobs);
        }
    }

    @SubscribeEvent
    public void StructureBuild(StructureEvent.BuildStructure event)
    {
        String name = event.getStructure();
        int biome;
        if (event.getBiomeType() != null)
        {
            biome = BiomeType.getBiome(event.getBiomeType()).getType();
        }
        else
        {
            if (name == null
                    || !Config.biomeMap.containsKey(name = name.toLowerCase(java.util.Locale.ENGLISH))) { return; }
            biome = Config.biomeMap.get(name);
        }
        Vector3 pos = Vector3.getNewVector();
        StructureBoundingBox bounds = event.getBoundingBox();
        for (int i = bounds.minX; i <= bounds.maxX; i++)
        {
            for (int k = bounds.minZ; k <= bounds.maxZ; k++)
                if (event.getWorld().isChunkGeneratedAt(i >> 4, k >> 4))
                {
                    for (int j = bounds.minY; j <= bounds.maxY; j++)
                    {
                        {
                            pos.set(i, j, k);
                            TerrainManager.getInstance().getTerrian(event.getWorld(), pos).setBiome(pos, biome);
                        }
                    }
                }
        }
    }

    @SubscribeEvent
    public void TrainerSendOutEvent(SendOut.Post evt)
    {
        IPokemob sent = evt.pokemob;
        EntityLivingBase owner = sent.getPokemonOwner();
        if (owner == null || owner instanceof EntityPlayer) return;
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(owner);
        if (pokemobHolder != null)
        {
            if (pokemobHolder.getOutMob() != null && pokemobHolder.getOutMob() != evt.pokemob)
            {
                pokemobHolder.getOutMob().returnToPokecube();
                pokemobHolder.setOutMob(evt.pokemob);
            }
            else
            {
                pokemobHolder.setOutMob(evt.pokemob);
            }
            IHasNPCAIStates aiStates = CapabilityNPCAIStates.getNPCAIStates(owner);
            if (aiStates != null) aiStates.setAIState(IHasNPCAIStates.THROWING, false);
        }
    }

    @SubscribeEvent
    public void livingHurtEvent(LivingHurtEvent evt)
    {
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(evt.getEntityLiving());
        IHasMessages messages = CapabilityNPCMessages.getMessages(evt.getEntityLiving());

        if (evt.getEntityLiving() instanceof INpc && !Config.instance.pokemobsHarmNPCs
                && (evt.getSource() instanceof PokemobDamageSource || evt.getSource() instanceof TerrainDamageSource))
        {
            evt.setAmount(0);
        }

        if (evt.getSource().getTrueSource() instanceof EntityLivingBase)
        {
            if (messages != null)
            {
                messages.sendMessage(MessageState.HURT, evt.getSource().getTrueSource(),
                        evt.getEntityLiving().getDisplayName(), evt.getSource().getTrueSource().getDisplayName());
                messages.doAction(MessageState.HURT, (EntityLivingBase) evt.getSource().getTrueSource());
            }
            if (pokemobHolder != null && pokemobHolder.getTarget() == null)
            {
                pokemobHolder.setTarget((EntityLivingBase) evt.getSource().getTrueSource());
            }
        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.EntityInteractSpecific evt)
    {
        if (evt.getWorld().isRemote) return;
        String ID = "LastSuccessInteractEvent";
        long time = evt.getTarget().getEntityData().getLong(ID);
        if (time == evt.getTarget().getEntityWorld().getTotalWorldTime()) return;
        processInteract(evt, evt.getTarget());
        evt.getTarget().getEntityData().setLong(ID, evt.getTarget().getEntityWorld().getTotalWorldTime());
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.EntityInteract evt)
    {
        if (evt.getWorld().isRemote) return;
        String ID = "LastSuccessInteractEvent";
        long time = evt.getTarget().getEntityData().getLong(ID);
        if (time == evt.getTarget().getEntityWorld().getTotalWorldTime()) return;
        processInteract(evt, evt.getTarget());
        evt.getTarget().getEntityData().setLong(ID, evt.getTarget().getEntityWorld().getTotalWorldTime());
    }

    public void processInteract(PlayerInteractEvent evt, Entity target)
    {
        IHasMessages messages = CapabilityNPCMessages.getMessages(target);
        IHasPokemobs pokemobs = CapabilityHasPokemobs.getHasPokemobs(target);
        if (!target.isSneaking() && pokemobs != null && evt.getItemStack().getItem() instanceof ItemTrainer)
        {
            evt.setCanceled(true);
            if (evt.getEntityPlayer() instanceof EntityPlayerMP)
            {
                PacketTrainer.sendEditOpenPacket(target, (EntityPlayerMP) evt.getEntityPlayer());
            }
            return;
        }
        if (messages != null)
        {
            messages.sendMessage(MessageState.INTERACT, evt.getEntityPlayer(), target.getDisplayName(),
                    evt.getEntityPlayer().getDisplayName());
            messages.doAction(MessageState.INTERACT, evt.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public void livingSetTargetEvent(LivingSetAttackTargetEvent evt)
    {
        if (evt.getTarget() == null) return;
        IHasPokemobs pokemobHolder = CapabilityHasPokemobs.getHasPokemobs(evt.getTarget());
        if (pokemobHolder != null && pokemobHolder.getTarget() == null)
        {
            pokemobHolder.setTarget((EntityLivingBase) evt.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void TrainerWatchEvent(StartTracking event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(event.getEntity());
        if (!(mobs instanceof DefaultPokemobs)) return;
        DefaultPokemobs pokemobs = (DefaultPokemobs) mobs;
        if (event.getEntityPlayer() instanceof EntityPlayerMP)
        {
            EntityTrainer trainer = (EntityTrainer) event.getTarget();
            if (pokemobs.notifyDefeat)
            {
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                packet.data.setInteger("I", trainer.getEntityId());
                packet.data.setBoolean("V", pokemobs.hasDefeated(event.getEntityPlayer()));
                PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof EntityLiving) || event.getObject().getEntityWorld() == null
                || TypeTrainer.mobTypeMapper.getType((EntityLivingBase) event.getObject(), false) == null)
            return;
        if (hasCap(event)) return;
        DefaultPokemobs mobs = new DefaultPokemobs();
        DefaultRewards rewards = new DefaultRewards();

        ItemStack stack = ItemStack.EMPTY;
        try
        {
            stack = fromString(PokecubeAdv.conf.defaultReward, event.getObject());
        }
        catch (CommandException e)
        {
            PokecubeMod.log(Level.WARNING, "Error with default trainer rewards " + PokecubeAdv.conf.defaultReward, e);
        }
        if (!stack.isEmpty()) rewards.getRewards().add(new Reward(stack));
        DefaultAIStates aiStates = new DefaultAIStates();
        DefaultMessager messages = new DefaultMessager();
        mobs.init((EntityLivingBase) event.getObject(), aiStates, messages, rewards);
        event.addCapability(POKEMOBSCAP, mobs);
        event.addCapability(AICAP, aiStates);
        event.addCapability(MESSAGECAP, messages);
        event.addCapability(REWARDSCAP, rewards);
        for (ICapabilityProvider p : event.getCapabilities().values())
        {
            if (p.hasCapability(IAIMob.THUTMOBAI, null)) return;
        }
        AIStuffHolder aiHolder = new AIStuffHolder((EntityLiving) event.getObject());
        event.addCapability(AISTUFFCAP, aiHolder);
    }

    private boolean hasCap(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(POKEMOBSCAP)) return true;
        for (ICapabilityProvider provider : event.getCapabilities().values())
        {
            if (provider.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (!(event.getEntity() instanceof EntityLivingBase)) return;
        EntityLivingBase npc = (EntityLivingBase) event.getEntity();
        IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(npc);
        if (mobs == null || !npc.hasCapability(IAIMob.THUTMOBAI, null)) return;
        IAIMob mob = npc.getCapability(IAIMob.THUTMOBAI, null);

        // All can battle, but only trainers will path during battle.
        mob.getAI().addAITask(new AIBattle(npc, !(npc instanceof EntityTrainer)).setPriority(0));

        // All attack zombies.
        mob.getAI().addAITask(new AIFindTarget(npc, EntityZombie.class).setPriority(20));
        // Only trainers specifically target players.
        if (npc instanceof EntityTrainerBase)
        {
            mob.getAI().addAITask(new AIFindTarget(npc, EntityPlayer.class).setPriority(10));
            mob.getAI()
                    .addAITask(new AIMate(npc, (Class<? extends EntityAgeable>) ((EntityTrainerBase) npc).getClass()));
        }
        // 5% chance of battling a random nearby pokemob if they see it.
        mob.getAI().addAITask(new AIFindTarget(npc, 0.05f, EntityPokemob.class).setPriority(20));

        // 1% chance of battling another of same class if seen
        mob.getAI().addAITask(new AIFindTarget(npc, 0.01f, npc.getClass()).setPriority(20));

        TypeTrainer newType = TypeTrainer.mobTypeMapper.getType(npc, true);
        if (newType == null) return;
        mobs.setType(newType);
        int level = SpawnHandler.getSpawnLevel(npc.getEntityWorld(), Vector3.getNewVector().set(npc),
                Database.getEntry(1));
        TypeTrainer.getRandomTeam(mobs, npc, level, npc.getEntityWorld());
    }

    private static final Map<Class<? extends Entity>, DataParamHolder> parameters = Maps.newHashMap();

    @SubscribeEvent
    public void onConstruct(EntityConstructing event)
    {
        if (!(event.getEntity() instanceof EntityLivingBase)
                || TypeTrainer.mobTypeMapper.getType((EntityLivingBase) event.getEntity(), false) == null)
            return;
        initDataManager(event.getEntity());
    }

    public static ItemStack fromString(String arg, ICommandSender sender) throws CommandException
    {
        String[] args = arg.split(" ");
        Item item = CommandBase.getItemByText(sender, args[0]);
        int i = 1;
        int j = args.length >= 3 ? CommandBase.parseInt(args[2].trim()) : 0;
        ItemStack itemstack = new ItemStack(item, i, j);
        if (args.length >= 4)
        {
            String s = CommandBase.buildString(args, 3);

            try
            {
                itemstack.setTagCompound(JsonToNBT.getTagFromJson(s));
            }
            catch (NBTException nbtexception)
            {
                throw new CommandException("commands.give.tagError", new Object[] { nbtexception.getMessage() });
            }
        }
        if (args.length >= 2)
            itemstack.setCount(CommandBase.parseInt(args[1].trim(), 1, item.getItemStackLimit(itemstack)));
        return itemstack;
    }

    public static DataParamHolder initDataManager(Entity e)
    {
        DataParamHolder holder = getParameterHolder(e.getClass());
        e.getDataManager().register(holder.TYPE, "");
        for (int i = 0; i < 6; i++)
            e.getDataManager().register(holder.pokemobs[i], ItemStack.EMPTY);
        return holder;
    }

    public static DataParamHolder getParameterHolder(Class<? extends Entity> clazz)
    {
        // TODO this should be found via class checking the entitylist, and
        // initialize thise at startup.
        if (parameters.containsKey(clazz)) return parameters.get(clazz);
        DataParameter<String> value = EntityDataManager.<String> createKey(clazz, DataSerializers.STRING);
        DataParamHolder holder = new DataParamHolder(value);
        for (int i = 0; i < 6; i++)
        {
            DataParameter<ItemStack> CUBE = EntityDataManager.<ItemStack> createKey(clazz, DataSerializers.ITEM_STACK);

            holder.pokemobs[i] = CUBE;
        }
        parameters.put(clazz, holder);
        return holder;
    }

    public static class DataParamHolder
    {
        public final DataParameter<String>      TYPE;
        @SuppressWarnings({ "unchecked" })
        public final DataParameter<ItemStack>[] pokemobs = new DataParameter[6];

        DataParamHolder(DataParameter<String> type)
        {
            this.TYPE = type;
        }

    }
}