package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.vecmath.Vector3f;

import org.nfunk.jep.JEP;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.properties.GuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.thread.aiRunnables.combat.AIFindTarget;
import pokecube.core.ai.utils.AIEventHandler;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.blocks.nests.TileEntityBasePortal;
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob;
import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.KillEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.AICapWrapper;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityAffected.DefaultAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.capabilities.impl.PokemobGenes;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.megastuff.IMegaCapability;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.ai.IAIMob;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.lib.CompatWrapper;

public class EventsHandler
{
    public static class MeteorAreaSetter
    {
        Map<Integer, List<BlockPos>> toProcess = Maps.newHashMap();

        public MeteorAreaSetter()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tickEvent(WorldTickEvent evt)
        {
            if (evt.phase == Phase.END && evt.side != Side.CLIENT)
            {
                List<BlockPos> thisTick = toProcess.get(evt.world.provider.getDimension());
                if (thisTick == null || thisTick.isEmpty()) return;
                int i = 0;
                int num = 0;
                for (i = 0; i < Math.min(1000, thisTick.size()); i++)
                {
                    BlockPos pos = thisTick.get(i);
                    TerrainManager.getInstance().getTerrain(evt.world, pos).setBiome(pos, BiomeType.METEOR.getType());
                    num = i + 1;
                }
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Processed " + num + " blocks as meteor.");
                for (i = 0; i < Math.min(num, thisTick.size()); i++)
                    thisTick.remove(0);
            }
        }

        public void addBlocks(Collection<BlockPos> toAdd, int dimension)
        {
            List<BlockPos> blocks = toProcess.get(dimension);
            if (blocks == null) toProcess.put(dimension, blocks = Lists.newArrayList());
            blocks.addAll(toAdd);
        }

        public void clear()
        {
            toProcess.clear();
        }
    }

    public static class ChooseFirst
    {
        final EntityPlayer player;

        public ChooseFirst(EntityPlayer player)
        {
            this.player = player;
            if (!SpawnHandler.canSpawnInWorld(player.getEntityWorld())) return;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player == player && player.ticksExisted > 0)
            {
                PacketChoose packet;
                packet = new PacketChoose(PacketChoose.OPENGUI);
                boolean hasStarter = PokecubeSerializer.getInstance().hasStarter(player);
                if (hasStarter)
                {
                    packet.data.setBoolean("C", false);
                    packet.data.setBoolean("H", hasStarter);
                }
                else
                {
                    Contributor contrib = ContributorManager.instance().getContributor(player.getGameProfile());
                    boolean special = false;
                    boolean pick = false;
                    if (PokecubePacketHandler.specialStarters.containsKey(contrib))
                    {
                        special = true;
                        pick = PacketChoose.canPick(player.getGameProfile());
                    }
                    packet = PacketChoose.createOpenPacket(special, pick, PokecubeMod.core.getStarters());
                }
                PokecubePacketHandler.sendToClient(packet, event.player);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static final ResourceLocation               POKEMOBCAP  = new ResourceLocation(PokecubeMod.ID, "pokemob");
    public static final ResourceLocation               AFFECTEDCAP = new ResourceLocation(PokecubeMod.ID, "affected");

    @CapabilityInject(IGuardAICapability.class)
    public static final Capability<IGuardAICapability> GUARDAI_CAP = null;

    public static IGuardAICapability.Storage           storage;
    static double                                      max         = 0;
    static int                                         count       = 0;
    static int                                         countAbove  = 0;
    static double                                      mean        = 0;

    static long                                        starttime   = 0;

    static boolean                                     notified    = false;

    // 4 = 1 per 10mins, 2 = 1 per 10s, 5 = 1 per 48 hours
    public static double                               candyChance = 4.5;

    public static double                               juiceChance = 3.5;

    public static List<IPokemob> getPokemobs(EntityLivingBase owner, double distance)
    {
        List<IPokemob> ret = new ArrayList<IPokemob>();

        AxisAlignedBB box = new AxisAlignedBB(owner.posX, owner.posY, owner.posZ, owner.posX, owner.posY, owner.posZ)
                .grow(distance, distance, distance);

        List<EntityLivingBase> pokemobs = owner.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase o : pokemobs)
        {
            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null)
            {
                if (mob.getPokemonOwner() == owner)
                {
                    ret.add(mob);
                }
            }
        }

        return ret;
    }

    public static int getShadowPokemonNb(Entity hostile)
    {
        String temp = hostile.getName().toLowerCase(java.util.Locale.ENGLISH).trim().replace(" ", "");

        PokedexEntry entry = null;

        ArrayList<PokedexEntry> list = Database.mobReplacements.get(temp);
        if (list != null)
        {
            Collections.shuffle(list);
            entry = list.get(0);
            while (Pokedex.getInstance().getEntry(entry.getPokedexNb()) == null && list.size() > 0)
            {
                list.remove(0);
                entry = list.get(0);
            }
            if (list.size() == 0)
            {
                Database.mobReplacements.remove(temp);
            }
        }
        return entry == null ? 249 : entry.getPokedexNb();
    }

    public static void recallAllPokemobsExcluding(EntityPlayer player, IPokemob excluded)
    {
        List<Entity> pokemobs = new ArrayList<Entity>(player.getEntityWorld().loadedEntityList);
        for (Entity o : pokemobs)
        {
            // Check to see if the mob has recenlty unloaded, or isn't added to
            // chunk for some reason. This is to hopefully prevent dupes when
            // the player has died far from the loaded area.
            if (player.getEntityWorld().unloadedEntityList.contains(o)) continue;
            if (!o.addedToChunk) continue;

            IPokemob pokemob = CapabilityPokemob.getPokemobFor(o);
            if (pokemob != null)
            {
                if (pokemob != excluded && pokemob.getPokemonOwner() == player
                        && !pokemob.getGeneralState(GeneralStates.STAYING))
                {
                    pokemob.returnToPokecube();
                }
            }
            else if (o instanceof EntityPokecube)
            {
                EntityPokecube mob = (EntityPokecube) o;
                if (CompatWrapper.isValid(mob.getItem()))
                {
                    String name = PokecubeManager.getOwner(mob.getItem());
                    if (name != null && (name.equalsIgnoreCase(player.getName())
                            || name.equals(player.getCachedUniqueIdString())))
                    {
                        EntityLivingBase out = mob.sendOut();
                        IPokemob poke = CapabilityPokemob.getPokemobFor(out);
                        if (poke != null) poke.returnToPokecube();
                    }
                }
            }
        }
    }

    public static void setFromNBT(IPokemob pokemob, NBTTagCompound tag)
    {
        NBTTagCompound pokemobTag = TagNames.getPokecubePokemobTag(tag);
        NBTBase genesTag = TagNames.getPokecubeGenesTag(tag);
        pokemobTag.removeTag(TagNames.INVENTORYTAG);
        pokemobTag.removeTag(TagNames.AITAG);
        pokemobTag.removeTag(TagNames.MOVESTAG);
        pokemob.readPokemobData(pokemobTag);
        if (pokemob instanceof DefaultPokemob)
        {
            try
            {
                DefaultPokemob poke = (DefaultPokemob) pokemob;
                IMobGenetics.GENETICS_CAP.readNBT(poke.genes, null, genesTag);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        pokemob.onGenesChanged();
    }

    public MeteorAreaSetter meteorprocessor;

    public EventsHandler()
    {
        CapabilityManager.INSTANCE.register(IGuardAICapability.class, storage = new IGuardAICapability.Storage(),
                GuardAICapability::new);
        CapabilityManager.INSTANCE.register(IPokemob.class, new CapabilityPokemob.Storage(), DefaultPokemob::new);
        CapabilityManager.INSTANCE.register(IOngoingAffected.class, new CapabilityAffected.Storage(),
                DefaultAffected::new);
        CapabilityManager.INSTANCE.register(IMegaCapability.class, new Capability.IStorage<IMegaCapability>()
        {
            @Override
            public NBTBase writeNBT(Capability<IMegaCapability> capability, IMegaCapability instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IMegaCapability> capability, IMegaCapability instance, EnumFacing side,
                    NBTBase nbt)
            {
            }
        }, MegaCapability.Default::new);
        CapabilityManager.INSTANCE.register(IPokemobUseable.class, new IPokemobUseable.Storage(),
                IPokemobUseable.Default::new);
        MinecraftForge.EVENT_BUS.register(new StatsHandler());
        MinecraftForge.EVENT_BUS.register(new GeneticsManager());
        MinecraftForge.EVENT_BUS.register(this);
        meteorprocessor = new MeteorAreaSetter();
        new SpawnEventsHandler();
        new AIEventHandler();
    }

    @SubscribeEvent
    public void breakSpeedCheck(PlayerEvent.BreakSpeed evt)
    {
        Entity ridden = evt.getEntityLiving().getRidingEntity();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(ridden);
        if (pokemob != null)
        {
            boolean aqua = evt.getEntityPlayer().isInWater();
            if (aqua)
            {
                aqua = !EnchantmentHelper.getAquaAffinityModifier(evt.getEntityPlayer());
            }
            if (aqua)
            {
                evt.setNewSpeed(evt.getOriginalSpeed() / 0.04f);
            }
            else
            {
                evt.setNewSpeed(evt.getOriginalSpeed() / 0.2f);
            }
        }
    }

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        if (evt.getState().getBlock() == Blocks.MOB_SPAWNER)
        {
            ItemStack stack = PokecubeItems.getRandomSpawnerDrop();
            if (!CompatWrapper.isValid(stack)) return;
            EntityItem item = new EntityItem(evt.getWorld(), evt.getPos().getX() + 0.5, evt.getPos().getY() + 0.5,
                    evt.getPos().getZ() + 0.5, stack);
            evt.getWorld().spawnEntity(item);
        }
        if (evt.getState().getBlock() == PokecubeItems.pokecenter)
        {
            int meta = evt.getState().getBlock().getMetaFromState(evt.getState());
            if (meta == 1 && !evt.getPlayer().capabilities.isCreativeMode) evt.setCanceled(true);
        }
        TileEntity tile;
        if ((tile = evt.getWorld().getTileEntity(evt.getPos())) instanceof TileEntityBasePortal)
        {
            if (!((TileEntityBasePortal) tile).canEdit(evt.getPlayer()))
            {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void clearNetherBridge(InitMapGenEvent evt)
    {
        if (PokecubeMod.core.getConfig().deactivateMonsters && evt.getType() == InitMapGenEvent.EventType.NETHER_BRIDGE)
        {
            ((MapGenNetherBridge) evt.getNewGen()).getSpawnList().clear();
        }
    }

    @SubscribeEvent
    public void EntityJoinWorld(EntityJoinWorldEvent evt)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (PokecubeMod.core.getConfig().disableVanillaMonsters && pokemob == null && evt.getEntity() instanceof IMob
                && !(evt.getEntity() instanceof EntityDragon || evt.getEntity() instanceof MultiPartEntityPart)
                && evt.getEntity().getClass().getName().contains("net.minecraft"))
        {
            evt.getEntity().setDead();
            // TODO maybe replace stuff here
            evt.setCanceled(true);
            return;
        }
        if (PokecubeMod.core.getConfig().disableVanillaAnimals && pokemob == null && evt.getEntity() instanceof IAnimals
                && !(evt.getEntity() instanceof IMob) && !(evt.getEntity() instanceof INpc)
                && !(evt.getEntity() instanceof IMerchant)
                && evt.getEntity().getClass().getName().contains("net.minecraft"))
        {
            evt.getEntity().setDead();
            // TODO maybe replace stuff here
            evt.setCanceled(true);
            return;
        }
        if (evt.getEntity() instanceof IPokemob && evt.getEntity().getEntityData().getBoolean("onShoulder"))
        {
            ((IPokemob) evt.getEntity()).setLogicState(LogicStates.SITTING, false);
            evt.getEntity().getEntityData().removeTag("onShoulder");
        }
        if (evt.getEntity() instanceof EntityCreeper)
        {
            EntityAIAvoidEntity<EntityPokemobBase> avoidAI;
            EntityCreeper creeper = (EntityCreeper) evt.getEntity();
            avoidAI = new EntityAIAvoidEntity<EntityPokemobBase>(creeper, EntityPokemobBase.class,
                    new Predicate<EntityPokemobBase>()
                    {
                        @Override
                        public boolean apply(EntityPokemobBase input)
                        {
                            IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
                            return pokemob.isType(PokeType.getType("psychic"));
                        }
                    }, 6.0F, 1.0D, 1.2D);
            creeper.tasks.addTask(3, avoidAI);
        }
    }

    @SubscribeEvent
    public void explosionEvents(ExplosionEvent.Detonate evt)
    {
        if (evt.getExplosion() instanceof ExplosionCustom)
        {
            ExplosionCustom boom = (ExplosionCustom) evt.getExplosion();
            if (!boom.meteor) return;
            if (PokecubeMod.debug)
            {
                PokecubeMod.log(Level.INFO, "Adding " + evt.getAffectedBlocks().size() + " for meteor processing.");
            }
            meteorprocessor.addBlocks(evt.getAffectedBlocks(), evt.getWorld().provider.getDimension());
        }
    }

    @SubscribeEvent
    public void interactEventLeftClick(PlayerInteractEvent.LeftClickBlock evt)
    {
        if (CompatWrapper.isValid(evt.getEntityPlayer().getHeldItemMainhand())
                && evt.getEntityPlayer().getHeldItemMainhand().getItem() == Items.STICK)
        {
            TileEntity te = evt.getWorld().getTileEntity(evt.getPos());
            if (te instanceof TileEntityOwnable)
            {
                IBlockState state = evt.getWorld().getBlockState(evt.getPos());
                TileEntityOwnable tile = (TileEntityOwnable) te;
                if (tile.canEdit(evt.getEntity()) && tile.shouldBreak())
                {
                    Block b = state.getBlock();
                    b.dropBlockAsItem(evt.getWorld(), evt.getPos(), state, 0);
                    evt.getWorld().setBlockToAir(evt.getPos());
                }
            }
        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.RightClickBlock evt)
    {
        String ID = "LastSuccessInteractEvent";
        long time = evt.getEntityPlayer().getEntityData().getLong(ID);
        if (time == evt.getEntityPlayer().getEntityWorld().getTotalWorldTime())
        {
            evt.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.RightClickItem evt)
    {
        String ID = "LastSuccessInteractEvent";
        long time = evt.getEntityPlayer().getEntityData().getLong(ID);
        if (time == evt.getEntityPlayer().getEntityWorld().getTotalWorldTime())
        {
            evt.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.EntityInteractSpecific evt)
    {
        String ID = "LastSuccessInteractEvent";
        long time = evt.getEntityPlayer().getEntityData().getLong(ID);
        if (time == evt.getEntityPlayer().getEntityWorld().getTotalWorldTime())
        {
            evt.setCanceled(true);
            return;
        }
        processInteract(evt, evt.getTarget());
        if (evt.isCanceled())
        {
            evt.getEntityPlayer().getEntityData().setLong(ID,
                    evt.getEntityPlayer().getEntityWorld().getTotalWorldTime());
        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent.EntityInteract evt)
    {
        String ID = "LastSuccessInteractEvent";
        long time = evt.getEntityPlayer().getEntityData().getLong(ID);
        if (time == evt.getEntityPlayer().getEntityWorld().getTotalWorldTime())
        {
            evt.setCanceled(true);
            return;
        }
        processInteract(evt, evt.getTarget());
        if (evt.isCanceled())
        {
            evt.getEntityPlayer().getEntityData().setLong(ID,
                    evt.getEntityPlayer().getEntityWorld().getTotalWorldTime());
        }
    }

    public void processInteract(PlayerInteractEvent evt, Entity target)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(target);
        if (pokemob != null && !evt.getWorld().isRemote)
        {
            EntityPlayer player = evt.getEntityPlayer();
            EnumHand hand = evt.getHand();
            ItemStack held = player.getHeldItem(hand);
            EntityLiving entity = pokemob.getEntity();

            // Item has custom entity interaction, let that run instead.
            if (held.getItem().itemInteractionForEntity(held, player, entity, hand))
            {
                evt.setCanceled(true);
                evt.setCancellationResult(EnumActionResult.SUCCESS);
                return;
            }

            PokedexEntry entry = pokemob.getPokedexEntry();

            // Check Pokedex Entry defined Interaction for player.
            if (entry.interact(player, pokemob, true))
            {
                evt.setCanceled(true);
                evt.setCancellationResult(EnumActionResult.SUCCESS);
                return;
            }

            boolean isOwner = false;
            if (pokemob.getGeneralState(GeneralStates.TAMED) && pokemob.getOwner() != null)
            {
                isOwner = pokemob.getOwner().getEntityId() == player.getEntityId();
            }
            // Owner only interactions phase 1
            if (isOwner)
            {
                // Either push pokemob around, or if sneaking, make it try to
                // climb
                // on shoulder
                if ((held.getItem() == Items.STICK || held.getItem() == Item.getItemFromBlock(Blocks.TORCH)))
                {
                    if (player.isSneaking())
                    {
                        pokemob.moveToShoulder(player);
                        return;
                    }
                    Vector3 look = Vector3.getNewVector().set(player.getLookVec()).scalarMultBy(1);
                    look.y = 0.2;
                    look.addVelocities(target);
                    return;
                }
                // Debug thing to maximize happiness
                if (held.getItem() == Items.APPLE)
                {
                    if (player.capabilities.isCreativeMode && player.isSneaking())
                    {
                        pokemob.addHappiness(255);
                    }
                }
                // Debug thing to increase hunger time
                if (held.getItem() == Items.GOLDEN_HOE)
                {
                    if (player.capabilities.isCreativeMode && player.isSneaking())
                    {
                        pokemob.setHungerTime(pokemob.getHungerTime() + 4000);
                    }
                }
                // Use shiny charm to make shiny
                if (Tools.isSameStack(held, PokecubeItems.getStack("shiny_charm")))
                {
                    if (player.isSneaking())
                    {
                        pokemob.setShiny(!pokemob.isShiny());
                        if (!player.capabilities.isCreativeMode) held.splitStack(1);
                    }
                    evt.setCanceled(true);
                    evt.setCancellationResult(EnumActionResult.SUCCESS);
                    return;
                }
            }

            // is Dyeable
            if (!held.isEmpty() && entry.dyeable)
            {
                int[] ids = OreDictionary.getOreIDs(held);
                EnumDyeColor dye = null;
                ids:
                for (int i : ids)
                {
                    String dict = OreDictionary.getOreName(i);
                    for (EnumDyeColor colour : EnumDyeColor.values())
                    {
                        if (dict.toLowerCase(Locale.ENGLISH)
                                .equals("dye" + colour.getUnlocalizedName().toLowerCase(Locale.ENGLISH)))
                        {
                            dye = colour;
                            break ids;
                        }
                    }
                }
                if (dye != null && (entry.validDyes.isEmpty() || entry.validDyes.contains(dye)))
                {
                    pokemob.setSpecialInfo(dye.getDyeDamage());
                    if (!player.capabilities.isCreativeMode) held.shrink(1);
                    evt.setCanceled(true);
                    evt.setCancellationResult(EnumActionResult.SUCCESS);
                    return;
                }
                else if (held.getItem() == Items.SHEARS) { return; }
            }

            boolean deny = pokemob.getCombatState(CombatStates.NOITEMUSE);
            if (deny && entity.getAttackTarget() == null)
            {
                deny = false;
                pokemob.setCombatState(CombatStates.NOITEMUSE, false);
            }

            if (deny)
            {
                // Add message here about cannot use items right now
                player.sendMessage(new TextComponentTranslation("pokemob.action.cannotuse"));
                return;
            }

            boolean saddleCheck = !player.isSneaking() && (!CompatWrapper.isValid(held))
                    && (isOwner || (pokemob.getEntity() instanceof EntityMountablePokemob
                            && ((EntityMountablePokemob) pokemob.getEntity()).canFitPassenger(player)))
                    && handleHmAndSaddle(player, pokemob);

            // Check if favourte berry and sneaking, if so, do breeding stuff.
            if (isOwner || player instanceof FakePlayer)
            {
                int fav = Nature.getFavouriteBerryIndex(pokemob.getNature());
                if (PokecubeCore.instance.getConfig().berryBreeding
                        && (player.isSneaking() || player instanceof FakePlayer) && entity.getAttackTarget() == null
                        && held.getItem() instanceof ItemBerry && (fav == -1 || fav == held.getItemDamage()))
                {
                    if (!player.capabilities.isCreativeMode)
                    {
                        held.shrink(1);
                        if (held.isEmpty())
                        {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                        }
                    }
                    pokemob.setLoveTimer(0);
                    entity.setAttackTarget(null);
                    entity.getEntityWorld().setEntityState(entity, (byte) 18);
                    evt.setCanceled(true);
                    evt.setCancellationResult(EnumActionResult.SUCCESS);
                    return;
                }
            }

            // Owner only interactions phase 2
            if (isOwner)
            {
                if (!held.isEmpty())
                {
                    // Check if it should evolve from item, do so if yes.
                    if (PokecubeItems.isValidEvoItem(held) && pokemob.canEvolve(held))
                    {
                        IPokemob evolution = pokemob.evolve(true, false, held);
                        if (evolution != null)
                        {
                            if (!player.capabilities.isCreativeMode)
                            {
                                held.shrink(1);
                                if (held.isEmpty())
                                {
                                    player.inventory.setInventorySlotContents(player.inventory.currentItem,
                                            ItemStack.EMPTY);
                                }
                            }
                        }
                        evt.setCanceled(true);
                        evt.setCancellationResult(EnumActionResult.SUCCESS);
                        return;
                    }
                    // Otherwise check if useable item.
                    IPokemobUseable usable = IPokemobUseable.getUsableFor(held);
                    if (usable != null)
                    {
                        ActionResult<ItemStack> result = usable.onUse(pokemob, held, player);
                        if (result.getType() == EnumActionResult.SUCCESS)
                        {
                            player.setHeldItem(hand, result.getResult());
                            pokemob.setCombatState(CombatStates.NOITEMUSE, true);
                            evt.setCanceled(true);
                            evt.setCancellationResult(EnumActionResult.SUCCESS);
                            return;
                        }
                    }
                    // Try to hold the item.
                    if (PokecubeItems.isValidHeldItem(held))
                    {
                        ItemStack heldItem = pokemob.getHeldItem();
                        if (!heldItem.isEmpty())
                        {
                            dropItem(pokemob);
                        }
                        ItemStack toSet = held.copy();
                        toSet.setCount(1);
                        pokemob.setHeldItem(toSet);
                        pokemob.setCombatState(CombatStates.NOITEMUSE, true);
                        if (!player.capabilities.isCreativeMode) held.shrink(1);
                        {
                            if (held.isEmpty())
                            {
                                player.inventory.setInventorySlotContents(player.inventory.currentItem,
                                        ItemStack.EMPTY);
                            }
                        }
                        evt.setCanceled(true);
                        evt.setCancellationResult(EnumActionResult.SUCCESS);
                        return;
                    }
                }
                // Open Gui
                pokemob.getPokemobInventory().setCustomName(entity.getDisplayName().getFormattedText());
                if (!saddleCheck)
                {
                    player.openGui(PokecubeMod.core, Config.GUIPOKEMOB_ID, entity.getEntityWorld(),
                            entity.getEntityId(), 0, 0);
                    evt.setCanceled(true);
                    evt.setCancellationResult(EnumActionResult.SUCCESS);
                    return;
                }
            }

            // Check saddle for riding.
            if (saddleCheck)
            {
                entity.setJumping(false);
                evt.setCanceled(true);
                evt.setCancellationResult(EnumActionResult.SUCCESS);
                return;
            }
        }
    }

    private boolean isRidable(Entity rider, IPokemob pokemob)
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        if (entry == null)
        {
            System.err.println("Null Entry for " + pokemob);
            return false;
        }
        if (!entry.ridable || pokemob.getCombatState(CombatStates.GUARDING)) return false;
        if (!CompatWrapper.isValid(pokemob.getPokemobInventory().getStackInSlot(0))) return false;

        if (rider instanceof EntityPlayerMP && rider == pokemob.getOwner())
        {
            EntityPlayer player = (EntityPlayer) rider;
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            Config config = PokecubeCore.core.getConfig();
            if (config.permsRide && !handler.hasPermission(player.getGameProfile(), Permissions.RIDEPOKEMOB, context))
                return false;
            if (config.permsRideSpecific
                    && !handler.hasPermission(player.getGameProfile(), Permissions.RIDESPECIFIC.get(entry), context))
                return false;
        }
        float scale = pokemob.getSize();
        Vector3f dims = pokemob.getPokedexEntry().getModelSize();
        return (dims.y * scale + dims.x * scale) > rider.width && Math.max(dims.x, dims.z) * scale > rider.width * 1.8;
    }

    private boolean handleHmAndSaddle(EntityPlayer entityplayer, IPokemob pokemob)
    {
        if (isRidable(entityplayer, pokemob))
        {
            if (entityplayer.isServerWorld()) entityplayer.startRiding(pokemob.getEntity());
            return true;
        }
        return false;
    }

    private void dropItem(IPokemob dropper)
    {
        ItemStack toDrop = dropper.getHeldItem();
        if (!CompatWrapper.isValid(toDrop)) return;
        Entity entity = dropper.getEntity();
        EntityItem drop = new EntityItem(entity.getEntityWorld(), entity.posX, entity.posY + 0.5, entity.posZ, toDrop);
        entity.getEntityWorld().spawnEntity(drop);
        dropper.setHeldItem(ItemStack.EMPTY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dropEvent(LivingDropsEvent event)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            event.getEntityLiving().captureDrops = true;
            if (!pokemob.getGeneralState(GeneralStates.TAMED))
            {
                for (int i = 0; i < pokemob.getPokemobInventory().getSizeInventory(); i++)
                {
                    ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) event.getEntityLiving().entityDropItem(stack.copy(), 0.0f);
                    pokemob.getPokemobInventory().setInventorySlotContents(i, ItemStack.EMPTY);
                }
                if (pokemob.getPokedexEntry().lootTable == null)
                {
                    List<ItemStack> drops = pokemob.getPokedexEntry().getRandomDrops(event.getLootingLevel());
                    for (ItemStack stack : drops)
                    {
                        if (event.getEntityLiving().isBurning() && stack != ItemStack.EMPTY)
                        {
                            ItemStack newDrop = FurnaceRecipes.instance().getSmeltingResult(stack);
                            if (!newDrop.isEmpty()) stack = newDrop.copy();
                        }
                        if (!stack.isEmpty()) event.getEntityLiving().entityDropItem(stack, 0.5f);
                    }
                }
            }
            else
            {
                event.getDrops().clear();
            }
            event.getEntityLiving().captureDrops = false;
        }
    }

    @SubscribeEvent
    public void KillEvent(pokecube.core.events.KillEvent evt)
    {
        IPokemob killer = evt.killer;
        IPokemob killed = evt.killed;

        if (killer != null && evt.giveExp)
        {
            EntityLivingBase owner = killer.getPokemonOwner();
            ItemStack stack = killer.getHeldItem();
            if (Tools.isStack(stack, "luckyegg"))
            {
                int exp = killer.getExp() + Tools.getExp(PokecubeCore.core.getConfig().expScaleFactor,
                        killed.getBaseXP(), killed.getLevel());
                killer.setExp(exp, true);
            }
            if (owner != null)
            {
                List<IPokemob> pokemobs = PCEventsHandler.getOutMobs(owner);
                for (IPokemob mob : pokemobs)
                {
                    if (mob != null)
                    {
                        IPokemob poke = mob;
                        if (Tools.isStack(mob.getHeldItem(), "exp_share"))
                        {
                            int exp = poke.getExp() + Tools.getExp(PokecubeCore.core.getConfig().expScaleFactor,
                                    killed.getBaseXP(), killed.getLevel());
                            poke.setExp(exp, true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void livingHurtEvent(LivingHurtEvent evt)
    {
        // No harming invalid targets.
        if (!AIFindTarget.validTargets.apply(evt.getEntity()))
        {
            evt.setCanceled(true);
            return;
        }

        // Prevent suffocating the player if they are in wall while riding
        // pokemob.
        if (evt.getEntityLiving() instanceof EntityPlayer && evt.getSource() == DamageSource.IN_WALL)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntityLiving().getRidingEntity());
            if (pokemob != null) evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingDeath(LivingDeathEvent evt)
    {
        DamageSource damageSource = evt.getSource();
        if (damageSource instanceof PokemobDamageSource)
        {
            damageSource.getImmediateSource().onKillEntity(evt.getEntityLiving());
        }
        IPokemob attacker = CapabilityPokemob.getPokemobFor(damageSource.getImmediateSource());
        if (attacker != null && damageSource.getImmediateSource() instanceof EntityLiving)
        {
            handleExp((EntityLiving) damageSource.getImmediateSource(), attacker, evt.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingUpdateEvent evt)
    {
        if (evt.getEntity().getEntityWorld().isRemote || evt.getEntity().isDead) return;
        int tick = Math.max(PokecubeMod.core.getConfig().attackCooldown, 1);
        if (evt.getEntityLiving().ticksExisted % tick == 0)
        {
            IOngoingAffected affected = CapabilityAffected.getAffected(evt.getEntityLiving());
            if (affected != null) affected.tick();
        }

        if (evt.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) evt.getEntityLiving();
            BlockPos here;
            BlockPos old;
            here = new BlockPos(MathHelper.floor(player.chasingPosX) >> 4, MathHelper.floor(player.chasingPosY) >> 4,
                    MathHelper.floor(player.chasingPosZ) >> 4);
            old = new BlockPos(MathHelper.floor(player.prevChasingPosX) >> 4,
                    MathHelper.floor(player.prevChasingPosY) >> 4, MathHelper.floor(player.prevChasingPosZ) >> 4);
            if (!here.equals(old)) SpawnHandler.refreshTerrain(Vector3.getNewVector().set(evt.getEntityLiving()),
                    evt.getEntity().getEntityWorld());
        }
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event)
    {
        event.addCapability(new ResourceLocation("pokecube:megawearable"), new MegaCapability(event.getObject()));
        UsableItemEffects.registerCapabilities(event);
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        Entity mob = event.getEntity();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) { return; }
        pokemob.setEntity((EntityLiving) mob);
        pokemob.initAI();
        IAIMob ai = mob.getCapability(IAIMob.THUTMOBAI, null);
        if (ai instanceof AICapWrapper)
        {
            ((AICapWrapper) ai).init();
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityLivingBase && !event.getCapabilities().containsKey(AFFECTEDCAP))
        {
            DefaultAffected affected = new DefaultAffected((EntityLivingBase) event.getObject());
            event.addCapability(AFFECTEDCAP, affected);
        }
        boolean isPokemob = false;
        if (PokemobGenes.isRegistered((Class<? extends EntityLiving>) event.getObject().getClass())
                && !event.getCapabilities().containsKey(POKEMOBCAP))
        {
            DefaultPokemob pokemob = new DefaultPokemob();
            GeneticsProvider genes = new GeneticsProvider();
            AICapWrapper aiCap = new AICapWrapper(pokemob);
            pokemob.setEntity((EntityLiving) event.getObject());
            pokemob.genes = genes.getCapability(IMobGenetics.GENETICS_CAP, null);
            event.addCapability(GeneticsManager.POKECUBEGENETICS, genes);
            event.addCapability(POKEMOBCAP, pokemob);
            event.addCapability(AICapWrapper.AICAP, aiCap);
            isPokemob = true;
        }

        if (isPokemob || event.getObject() instanceof EntityProfessor)
        {
            class Provider extends GuardAICapability implements ICapabilitySerializable<NBTTagCompound>
            {
                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    storage.readNBT(GUARDAI_CAP, this, null, nbt);
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (GUARDAI_CAP != null && capability == GUARDAI_CAP) return GUARDAI_CAP.cast(this);
                    return null;
                }

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                {
                    return GUARDAI_CAP != null && capability == GUARDAI_CAP;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    return (NBTTagCompound) storage.writeNBT(GUARDAI_CAP, this, null);
                }
            }
            event.addCapability(new ResourceLocation("pokecube:guardai"), new Provider());
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;
        if (!evt.player.getEntityWorld().isRemote)
        {
            PacketDataSync.sendInitHandshake(entityPlayer);
            PacketDataSync.sendInitPacket(entityPlayer, "pokecube-data");
            PacketDataSync.sendInitPacket(entityPlayer, "pokecube-stats");
        }

        if (evt.player != null)
        {
            if (!evt.player.getEntityWorld().isRemote)
            {
                if (PokecubeMod.core.getConfig().guiOnLogin)
                {
                    new ChooseFirst(evt.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void TickEvent(WorldTickEvent evt)
    {
        if (evt.phase == Phase.END && evt.side != Side.CLIENT && !Database.spawnables.isEmpty())
        {
            PokecubeCore.instance.spawner.tick(evt.world);
        }
    }

    @SubscribeEvent
    public void travelToDimension(EntityTravelToDimensionEvent evt)
    {
        Entity entity = evt.getEntity();
        if (entity.getEntityWorld().isRemote) return;

        ArrayList<Entity> list = new ArrayList<Entity>(entity.getEntityWorld().loadedEntityList);
        for (Entity o : list)
        {
            // Check to see if the mob has recenlty unloaded, or isn't added to
            // chunk for some reason. This is to hopefully prevent dupes when
            // the player has died far from the loaded area.
            if (entity.getEntityWorld().unloadedEntityList.contains(o)) continue;
            if (!o.addedToChunk) continue;
            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null)
            {
                boolean stay = mob.getGeneralState(GeneralStates.STAYING);
                if (mob.getGeneralState(GeneralStates.TAMED) && (mob.getPokemonOwner() == entity) && !stay)
                    mob.returnToPokecube();
            }
        }
    }

    @SubscribeEvent
    public void worldLoadEvent(Load evt)
    {
        if (evt.getWorld().isRemote) { return; }
        PokecubeMod.getFakePlayer(evt.getWorld());
    }

    @SubscribeEvent
    public void WorldSave(WorldEvent.Save evt)
    {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER && evt.getWorld().provider.getDimension() == 0)
        {
            long time = System.nanoTime();
            PokecubeSerializer.getInstance().save();
            double dt = (System.nanoTime() - time) / 1000000d;
            if (dt > 20) System.err.println("Took " + dt + "ms to save pokecube data");
        }
    }

    @SubscribeEvent
    public void PokecubeWatchEvent(StartTracking event)
    {
        if (event.getTarget() instanceof EntityPokecube && event.getEntityPlayer() instanceof EntityPlayerMP)
        {
            EntityPokecube pokecube = (EntityPokecube) event.getTarget();
            if (pokecube.isLoot && pokecube.cannotCollect(event.getEntityPlayer()))
            {
                PacketPokecube.sendMessage(event.getEntityPlayer(), pokecube.getEntityId(),
                        pokecube.world.getTotalWorldTime() + pokecube.resetTime);
            }
        }
    }

    private void handleExp(EntityLiving pokemob, IPokemob attacker, EntityLivingBase attacked)
    {
        IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if (PokecubeCore.core.getConfig().nonPokemobExp && attackedMob == null)
        {
            JEP parser = new JEP();
            parser.initFunTab(); // clear the contents of the function table
            parser.addStandardFunctions();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addStandardConstants();
            parser.addComplex();
            parser.addVariable("h", 0);
            parser.addVariable("a", 0);
            parser.parseExpression(PokecubeCore.core.getConfig().nonPokemobExpFunction);
            parser.setVarValue("h", attacked.getMaxHealth());
            parser.setVarValue("a", attacked.getTotalArmorValue());
            int exp = (int) parser.getValue();
            if (parser.hasError()) exp = 0;
            attacker.setExp(attacker.getExp() + exp, true);
            return;
        }
        if (attackedMob != null && attacked.getHealth() <= 0)
        {
            boolean giveExp = !attackedMob.isShadow();
            boolean pvp = attackedMob.getGeneralState(GeneralStates.TAMED)
                    && (attackedMob.getPokemonOwner() instanceof EntityPlayer);
            if (pvp && !PokecubeMod.core.getConfig().pvpExp)
            {
                giveExp = false;
            }
            if ((attackedMob.getGeneralState(GeneralStates.TAMED) && !PokecubeMod.core.getConfig().trainerExp))
            {
                giveExp = false;
            }
            KillEvent event = new KillEvent(attacker, attackedMob, giveExp);
            MinecraftForge.EVENT_BUS.post(event);
            giveExp = event.giveExp;
            if (event.isCanceled())
            {

            }
            else if (giveExp)
            {
                attacker.setExp(attacker.getExp() + Tools.getExp(
                        (float) (pvp ? PokecubeMod.core.getConfig().pvpExpMultiplier
                                : PokecubeCore.core.getConfig().expScaleFactor),
                        attackedMob.getBaseXP(), attackedMob.getLevel()), true);
                byte[] evsToAdd = Pokedex.getInstance().getEntry(attackedMob.getPokedexNb()).getEVs();
                attacker.addEVs(evsToAdd);
            }
            Entity targetOwner = attackedMob.getPokemonOwner();
            attacker.displayMessageToOwner(
                    new TextComponentTranslation("pokemob.action.faint.enemy", attackedMob.getPokemonDisplayName()));
            if (targetOwner instanceof EntityPlayer && attacker.getPokemonOwner() != targetOwner)
            {
                pokemob.setAttackTarget((EntityLivingBase) targetOwner);
            }
            else
            {
                pokemob.setAttackTarget(null);
            }
            if (attacker.getPokedexEntry().isFood(attackedMob.getPokedexEntry())
                    && attacker.getCombatState(CombatStates.HUNTING))
            {
                attacker.eat(pokemob.getAttackTarget());
                attacker.setCombatState(CombatStates.HUNTING, false);
                pokemob.getNavigator().clearPath();
            }
        }
        else
        {
            pokemob.setAttackTarget(null);
        }
    }
}
