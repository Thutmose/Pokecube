package pokecube.adventures.events;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.helper.AIStuffHolder;
import pokecube.adventures.ai.tasks.AIBattle;
import pokecube.adventures.ai.tasks.AIFindTarget;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.Database;
import pokecube.core.events.PCEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.events.StarterEvent;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.entity.ai.IAIMob;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.lib.CompatWrapper;

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
        int maxXp = SpawnHandler.getSpawnXp(trainer.getEntityWorld(), loc, Database.getEntry(1));
        if (trainer instanceof EntityTrainer)
        {
            EntityTrainer t = (EntityTrainer) trainer;
            t.name = "";
            t.populateBuyingList(null);
            if (mobs.getType() != null) t.initTrainer(mobs.getType(), maxXp);
        }
        else if (mobs.getType() != null)
        {
            mobs.setType(mobs.getType());
            byte genders = mobs.getType().genders;
            if (genders == 1) mobs.setGender((byte) 1);
            if (genders == 2) mobs.setGender((byte) 2);
            if (genders == 3) mobs.setGender((byte) (Math.random() < 0.5 ? 1 : 2));
            TypeTrainer.getRandomTeam(mobs, (EntityLivingBase) trainer, maxXp, trainer.getEntityWorld());
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
    }

    @SubscribeEvent
    public void PlayerStarter(StarterEvent.Pick evt)
    {
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
        if (name == null || !Config.biomeMap.containsKey(name = name.toLowerCase(java.util.Locale.ENGLISH))) { return; }
        int biome = Config.biomeMap.get(name);
        Vector3 pos = Vector3.getNewVector();
        StructureBoundingBox bounds = event.getBoundingBox();
        for (int i = bounds.minX; i <= bounds.maxX; i++)
        {
            for (int j = bounds.minY; j <= bounds.maxY; j++)
            {
                for (int k = bounds.minZ; k < bounds.maxZ; k++)
                {
                    pos.set(i, j, k);
                    TerrainManager.getInstance().getTerrian(event.getWorld(), pos).setBiome(pos, biome);
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
        if (event.getCapabilities().containsKey(POKEMOBSCAP)
                || event.getObject().hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null))
            return;
        DefaultPokemobs mobs = new DefaultPokemobs();
        DefaultRewards rewards = new DefaultRewards();
        AIStuffHolder aiHolder = new AIStuffHolder((EntityLiving) event.getObject());
        rewards.getRewards().add(new ItemStack(Items.EMERALD));
        DefaultAIStates aiStates = new DefaultAIStates();
        DefaultMessager messages = new DefaultMessager();
        mobs.init((EntityLivingBase) event.getObject(), aiStates, messages, rewards);
        event.addCapability(POKEMOBSCAP, mobs);
        event.addCapability(AICAP, aiStates);
        event.addCapability(MESSAGECAP, messages);
        event.addCapability(REWARDSCAP, rewards);
        event.addCapability(AISTUFFCAP, aiHolder);
        if (!event.getObject().getEntityWorld().isRemote) needsAI.add((EntityLiving) event.getObject());
    }

    private List<EntityLiving> needsAI = Lists.newArrayList();

    @SubscribeEvent
    public void onTick(ServerTickEvent event)
    {
        if (!needsAI.isEmpty() && event.side == Side.SERVER && event.phase == Phase.END)
        {
            synchronized (needsAI)
            {
                List<EntityLiving> stale = Lists.newArrayList();
                List<EntityLiving> toProcess = Lists.newArrayList(needsAI);
                for (EntityLiving npc : toProcess)
                {
                    if (npc.ticksExisted == 0) continue;
                    IAIMob mob = npc.getCapability(IAIMob.THUTMOBAI, null);
                    mob.getAI().addAITask(new AIBattle(npc, !(npc instanceof EntityTrainer)).setPriority(0));
                    mob.getAI().addAITask(new AIFindTarget(npc, EntityZombie.class).setPriority(20));
                    if (npc instanceof EntityTrainer)
                        mob.getAI().addAITask(new AIFindTarget(npc, EntityPlayer.class).setPriority(10));
                    stale.add(npc);

                    IGuardAICapability guardCap = npc.getCapability(EventsHandler.GUARDAI_CAP, null);
                    guardAI:
                    if (guardCap != null)
                    {
                        for (EntityAITaskEntry taskentry : npc.tasks.taskEntries)
                        {
                            if (taskentry.action instanceof GuardAI) break guardAI;
                        }
                        GuardAI guard = new GuardAI(npc, guardCap);
                        npc.tasks.addTask(1, guard);
                    }
                    IHasPokemobs mobs = CapabilityHasPokemobs.getHasPokemobs(npc);
                    TypeTrainer newType = TypeTrainer.mobTypeMapper.getType(npc, true);
                    if (newType == null) continue;
                    mobs.setType(newType);
                    int level = SpawnHandler.getSpawnLevel(npc.getEntityWorld(), Vector3.getNewVector().set(npc),
                            Database.getEntry(1));
                    TypeTrainer.getRandomTeam(mobs, npc, level, npc.getEntityWorld());

                }
                needsAI.removeAll(stale);
            }
        }
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

    public static DataParamHolder initDataManager(Entity e)
    {
        DataParamHolder holder = getParameterHolder(e.getClass());
        e.getDataManager().register(holder.TYPE, "");
        for (int i = 0; i < 6; i++)
            e.getDataManager().register(holder.pokemobs[i], CompatWrapper.nullStack);
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