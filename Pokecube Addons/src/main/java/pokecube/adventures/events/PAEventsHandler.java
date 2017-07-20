package pokecube.adventures.events;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
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
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.trainers.AITrainerBattle;
import pokecube.adventures.ai.trainers.AITrainerFindTarget;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates.DefaultAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates.IHasAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityMessages.DefaultMessager;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.database.Database;
import pokecube.core.events.PCEvent;
import pokecube.core.events.SpawnEvent.SendOut;
import pokecube.core.events.StarterEvent;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

public class PAEventsHandler
{
    final ResourceLocation POKEMOBSCAP = new ResourceLocation(PokecubeAdv.ID, "pokemobs");
    final ResourceLocation AICAP       = new ResourceLocation(PokecubeAdv.ID, "ai");
    final ResourceLocation MESSAGECAP  = new ResourceLocation(PokecubeAdv.ID, "messages");
    final ResourceLocation REWARDSCAP  = new ResourceLocation(PokecubeAdv.ID, "rewards");

    public static void randomizeTrainerTeam(EntityTrainer trainer)
    {
        Vector3 loc = Vector3.getNewVector().set(trainer);
        int maxXp = SpawnHandler.getSpawnXp(trainer.getEntityWorld(), loc, Database.getEntry(1));
        trainer.name = "";
        trainer.initTrainer(trainer.getType(), maxXp);
        trainer.populateBuyingList();
        System.out.println("Randomized " + trainer.name);
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
        IHasPokemobs pokemobHolder = null;
        if (owner.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null))
            pokemobHolder = owner.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        else if (owner instanceof IHasPokemobs) pokemobHolder = (IHasPokemobs) owner;
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
        if (!(event.getEntity() instanceof EntityTrainer)) return;
        EntityTrainer trainer = (EntityTrainer) event.getEntity();
        if (trainer.getShouldRandomize())
        {
            randomizeTrainerTeam(trainer);
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
        IHasPokemobs pokemobHolder = null;
        if (owner.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null))
            pokemobHolder = owner.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
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
            IHasAIStates aiStates = null;
            if (owner.hasCapability(CapabilityAIStates.AISTATES_CAP, null))
                aiStates = owner.getCapability(CapabilityAIStates.AISTATES_CAP, null);
            if (aiStates != null) aiStates.setAIState(IHasAIStates.THROWING, false);
        }
    }

    @SubscribeEvent
    public void livingHurtEvent(LivingHurtEvent evt)
    {
        IHasPokemobs pokemobHolder = null;
        if (evt.getEntityLiving().hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null))
            pokemobHolder = evt.getEntityLiving().getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        if (pokemobHolder != null && pokemobHolder.getTarget() == null
                && evt.getSource().getTrueSource() instanceof EntityLivingBase)
        {
            pokemobHolder.setTarget((EntityLivingBase) evt.getSource().getTrueSource());
        }
    }

    @SubscribeEvent
    public void livingSetTargetEvent(LivingSetAttackTargetEvent evt)
    {
        IHasPokemobs pokemobHolder = null;
        if (evt.getTarget() == null) return;
        if (evt.getTarget().hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null))
            pokemobHolder = evt.getTarget().getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        if (pokemobHolder != null && pokemobHolder.getTarget() == null)
        {
            pokemobHolder.setTarget((EntityLivingBase) evt.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void TrainerWatchEvent(StartTracking event)
    {
        if (event.getTarget() instanceof EntityTrainer && event.getEntityPlayer() instanceof EntityPlayerMP)
        {
            EntityTrainer trainer = (EntityTrainer) event.getTarget();
            if (trainer.notifyDefeat)
            {
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGENOTIFYDEFEAT);
                packet.data.setInteger("I", trainer.getEntityId());
                packet.data.setBoolean("V", trainer.hasDefeated(event.getEntityPlayer()));
                PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (!((event.getObject() instanceof INpc) && (event.getObject() instanceof EntityLiving))) return;
        if (!Config.instance.npcsAreTrainers && !(event.getObject() instanceof EntityTrainer)) return;
        if (event.getCapabilities().containsKey(POKEMOBSCAP)) return;
        DefaultPokemobs mobs = new DefaultPokemobs();
        DefaultRewards rewards = new DefaultRewards();
        rewards.getRewards().add(new ItemStack(Items.EMERALD));
        DefaultAIStates aiStates = new DefaultAIStates();
        DefaultMessager messages = new DefaultMessager();
        mobs.init((EntityLivingBase) event.getObject(), aiStates, messages, rewards);
        event.addCapability(POKEMOBSCAP, mobs);
        event.addCapability(AICAP, aiStates);
        event.addCapability(MESSAGECAP, messages);
        event.addCapability(REWARDSCAP, rewards);
    }

    public static final Map<Class<? extends Entity>, DataParameter<String>> parameters = Maps.newHashMap();

    @SubscribeEvent
    public void onConstruct(EntityConstructing event)
    {
        if (!((event.getEntity() instanceof INpc) && (event.getEntity() instanceof EntityLiving))) return;
        if (!Config.instance.npcsAreTrainers && !(event.getEntity() instanceof EntityTrainer)) return;

        // TODO this should be found via class checking the entitylist, and
        // initialize thise at startup.
        if (!parameters.containsKey(event.getEntity().getClass()))
        {
            DataParameter<String> value = EntityDataManager.<String> createKey(event.getEntity().getClass(),
                    DataSerializers.STRING);
            parameters.put(event.getEntity().getClass(), value);
        }
        event.getEntity().getDataManager().register(parameters.get(event.getEntity().getClass()), "");
    }

    @SubscribeEvent
    public void joinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        if (!event.getEntity().hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null)) return;
        IHasPokemobs mobs = event.getEntity().getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        if (mobs.getType() != null) return;
        EntityLiving npc = (EntityLiving) event.getEntity();
        TypeTrainer newType = TypeTrainer.mobTypeMapper.getType(npc);
        if (newType == null) return;
        mobs.setType(newType);
        int level = SpawnHandler.getSpawnLevel(npc.getEntityWorld(), Vector3.getNewVector().set(npc),
                Database.getEntry(1));
        TypeTrainer.getRandomTeam(mobs, npc, level, npc.getEntityWorld());
        npc.tasks.addTask(2, new AITrainerBattle(npc));
        npc.tasks.addTask(2, new AITrainerFindTarget(npc, EntityZombie.class));
    }
}