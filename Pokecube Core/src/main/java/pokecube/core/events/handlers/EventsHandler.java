package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.properties.GuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class EventsHandler
{
    public static class ChooseFirst
    {
        final EntityPlayer player;

        public ChooseFirst(EntityPlayer player)
        {
            this.player = player;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player == player)
            {
                PokecubeClientPacket packet2 = new PokecubeClientPacket(new byte[] { PokecubeClientPacket.CHOOSE1ST });
                PokecubePacketHandler.sendToClient(packet2, event.player);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        private IChatComponent getInfoMessage(CheckResult result, String name)
        {
            String linkName = "[" + EnumChatFormatting.GREEN + name + " " + PokecubeMod.VERSION
                    + EnumChatFormatting.WHITE;
            String link = "" + result.url;
            String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                    + link + "\"}}";

            String info = "\"" + EnumChatFormatting.GOLD + "Currently Running " + "\"";
            String mess = "[" + info + "," + linkComponent + ",\"]\"]";
            return IChatComponent.Serializer.jsonToComponent(mess);
        }

        @Deprecated // Use one from ThutCore whenever that is updated for a bit.
        private IChatComponent getOutdatedMessage(CheckResult result, String name)
        {
            String linkName = "[" + EnumChatFormatting.GREEN + name + " " + result.target + EnumChatFormatting.WHITE;
            String link = "" + result.url;
            String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                    + link + "\"}}";

            String info = "\"" + EnumChatFormatting.RED
                    + "New Pokecube Core version available, please update before reporting bugs.\nClick the green link for the page to download.\n"
                    + "\"";
            String mess = "[" + info + "," + linkComponent + ",\"]\"]";
            return IChatComponent.Serializer.jsonToComponent(mess);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeMod.ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    IChatComponent mess = getOutdatedMessage(result, "Pokecube Core");
                    (event.player).addChatMessage(mess);
                }
                else if (PokecubeMod.core.getConfig().loginmessage)
                {
                    IChatComponent mess = getInfoMessage(result, "Pokecube Core");
                    (event.player).addChatMessage(mess);
                }
            }
        }
    }

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
                .expand(distance, distance, distance);

        List<EntityLivingBase> pokemobs = owner.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (Object o : pokemobs)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
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
        String temp = hostile.getName().toLowerCase().trim().replace(" ", "");

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
        List<?> pokemobs = new ArrayList<Object>(player.worldObj.loadedEntityList);
        for (Object o : pokemobs)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
                if (mob != excluded && mob.getPokemonOwner() == player
                        && !mob.getPokemonAIState(IMoveConstants.GUARDING)
                        && !mob.getPokemonAIState(IMoveConstants.STAYING))
                {
                    mob.returnToPokecube();
                }
            }
            else if (o instanceof EntityPokecube)
            {
                EntityPokecube mob = (EntityPokecube) o;
                if (mob.getEntityItem() != null)
                {
                    String name = PokecubeManager.getOwner(mob.getEntityItem());
                    if (name != null && (name.equalsIgnoreCase(player.getName())
                            || name.equals(player.getUniqueID().toString())))
                    {
                        ItemStack cube = mob.getEntityItem();
                        ItemTossEvent evt = new ItemTossEvent(
                                new EntityItem(mob.worldObj, mob.posX, mob.posY, mob.posZ, cube), player);
                        MinecraftForge.EVENT_BUS.post(evt);
                    }
                }
            }
        }
    }

    public static void setFromNBT(IPokemob pokemob, NBTTagCompound tag)
    {
        float scale = tag.getFloat("scale");
        if (scale > 0)
        {
            pokemob.setSize(scale);
        }
        pokemob.setSexe((byte) tag.getInteger(PokecubeSerializer.SEXE));
        boolean shiny = tag.getBoolean("shiny");
        pokemob.setShiny(shiny);
        byte[] rgbaBytes = new byte[4];
        // TODO remove the legacy colour support eventually.
        if (tag.hasKey("colours", 7))
        {
            rgbaBytes = tag.getByteArray("colours");
        }
        else
        {
            rgbaBytes[0] = tag.getByte("red");
            rgbaBytes[1] = tag.getByte("green");
            rgbaBytes[2] = tag.getByte("blue");
            rgbaBytes[3] = 127;
        }
        if (pokemob instanceof IMobColourable)
        {
            ((IMobColourable) pokemob).setRGBA(rgbaBytes[0] + 128, rgbaBytes[1] + 128, rgbaBytes[2] + 128,
                    rgbaBytes[2] + 128);
        }
        String forme = tag.getString("forme");
        pokemob.changeForme(forme);
        pokemob.setSpecialInfo(tag.getInteger("specialInfo"));
    }

    public EventsHandler()
    {
        CapabilityManager.INSTANCE.register(IGuardAICapability.class, storage = new IGuardAICapability.Storage(),
                GuardAICapability.class);
        MinecraftForge.EVENT_BUS.register(new StatsHandler());
        PokemobAIThread aiTicker = new PokemobAIThread();
        MinecraftForge.EVENT_BUS.register(aiTicker);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) new UpdateNotifier();
    }

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        if (evt.state.getBlock() == Blocks.mob_spawner)
        {
            ItemStack stack = PokecubeItems.getRandomSpawnerDrop();
            if (stack == null) return;
            EntityItem item = new EntityItem(evt.world, evt.pos.getX() + 0.5, evt.pos.getY() + 0.5,
                    evt.pos.getZ() + 0.5, stack);
            evt.world.spawnEntityInWorld(item);
        }
        if (evt.state.getBlock() == PokecubeItems.pokecenter)
        {
            int meta = evt.state.getBlock().getMetaFromState(evt.state);
            if (meta == 1 && !evt.getPlayer().capabilities.isCreativeMode) evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void clearNetherBridge(InitMapGenEvent evt)
    {
        if (PokecubeMod.core.getConfig().deactivateMonsters && evt.type == InitMapGenEvent.EventType.NETHER_BRIDGE)
        {
            ((MapGenNetherBridge) evt.newGen).getSpawnList().clear();
        }
    }

    @SubscribeEvent
    public void EntityJoinWorld(EntityJoinWorldEvent evt)
    {
        if (PokecubeMod.core.getConfig().disableMonsters && !(evt.entity instanceof IPokemob)
                && evt.entity instanceof IMob
                && !(evt.entity instanceof EntityDragon || evt.entity instanceof EntityDragonPart))
        {
            evt.entity.setDead();
            Vector3 location = Vector3.getNewVector().set(evt.entity);
            int num = getShadowPokemonNb(evt.entity);
            Entity shadow = PokecubeMod.core.createEntityByPokedexNb(num, evt.world);
            if (shadow == null)
            {
                System.err.println(num);
                return;
            }

            location.moveEntity(shadow);
            ((IPokemob) shadow).setShadow(true);
            ((IPokemob) shadow).specificSpawnInit();

            int exp = (int) (SpawnHandler.getSpawnXp(evt.world, location, ((IPokemob) shadow).getPokedexEntry())
                    * 1.25);
            exp = Math.max(exp, 8000);

            ((IPokemob) shadow).setExp(exp, false, true);

            ((EntityLiving) shadow).setHealth(((EntityLiving) shadow).getMaxHealth());
            evt.world.spawnEntityInWorld(shadow);
            evt.setCanceled(true);
        }
        else if (evt.entity instanceof EntityCreeper)
        {
            EntityAIAvoidEntity<EntityPokemobBase> avoidAI;
            EntityCreeper creeper = (EntityCreeper) evt.entity;
            avoidAI = new EntityAIAvoidEntity<EntityPokemobBase>(creeper, EntityPokemobBase.class,
                    new Predicate<EntityPokemobBase>()
                    {
                        @Override
                        public boolean apply(EntityPokemobBase input)
                        {
                            return input.isType(PokeType.psychic);
                        }
                    }, 6.0F, 1.0D, 1.2D);
            creeper.tasks.addTask(3, avoidAI);
        }
    }

    @SubscribeEvent
    public void explosionEvents(ExplosionEvent.Detonate evt)
    {
        if (evt.explosion instanceof ExplosionCustom)
        {
            ExplosionCustom boom = (ExplosionCustom) evt.explosion;
            if (!boom.meteor) return;

            List<BlockPos> blocks = Lists.newArrayList(boom.affectedBlockPositions);

            for (BlockPos p : blocks)
            {
                TerrainManager.getInstance().getTerrain(evt.world, p).setBiome(p, BiomeType.METEOR.getType());
            }

        }
    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent evt)
    {
        if (evt.action == Action.LEFT_CLICK_BLOCK && evt.entityPlayer.getHeldItem() != null
                && evt.entityPlayer.getHeldItem().getItem() == Items.stick)
        {
            TileEntity te = evt.world.getTileEntity(evt.pos);
            if (te instanceof TileEntityOwnable)
            {
                IBlockState state = evt.world.getBlockState(evt.pos);
                TileEntityOwnable tile = (TileEntityOwnable) te;
                if (tile.canEdit(evt.entity))
                {
                    Block b = state.getBlock();
                    b.dropBlockAsItem(evt.world, evt.pos, state, 0);
                    evt.world.setBlockToAir(evt.pos);
                }
            }
        }
        if (evt.entityPlayer.worldObj.isRemote && evt.entityPlayer.getHeldItem() != null
                && evt.entityPlayer.getHeldItem().getItem() instanceof IPokecube)
        {
            Block block = null;
            if (evt.action == Action.RIGHT_CLICK_BLOCK)
            {
                block = evt.world.getBlockState(evt.pos).getBlock();
            }

            Entity target = Tools.getPointedEntity(evt.entityPlayer, 32);

            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(5));
            buffer.writeByte(PokecubeServerPacket.POKECUBEUSE);
            if (target != null)
            {
                buffer.writeInt(target.getEntityId());
            }
            else
            {
                Vector3 temp = Vector3.getNewVector(), look = Vector3.getNewVector();
                temp.set(evt.entityPlayer).addTo(0, evt.entityPlayer.getEyeHeight(), 0);
                look.set(evt.entityPlayer.getLook(1));
                Vector3 hit = Vector3.getNextSurfacePoint(evt.world, temp, look, 32);
                if (hit != null) hit.writeToBuff(buffer);
            }
            PokecubeServerPacket packet = new PokecubeServerPacket(buffer);
            if (evt.action == Action.RIGHT_CLICK_AIR) PokecubePacketHandler.sendToServer(packet);

            if (evt.action != Action.RIGHT_CLICK_BLOCK)
            {
                evt.setCanceled(true);
                evt.setResult(Result.DENY);
            }
            else
            {
                StackTraceElement[] trace = Thread.currentThread().getStackTrace();
                boolean loop = false;
                for (int i = 2; i < trace.length; i++)
                {
                    if (trace[i].getClassName().equals("pokecube.core.events.handlers.EventsHandler"))
                    {
                        loop = true;
                        break;
                    }
                }

                if (block != null && !loop)
                {
                    IBlockState state = evt.world.getBlockState(evt.pos);
                    boolean b = block.onBlockActivated(evt.world, evt.pos, state, evt.entityPlayer, evt.face,
                            (float) evt.localPos.xCoord, (float) evt.localPos.yCoord, (float) evt.localPos.zCoord);
                    if (!b && !evt.entityPlayer.isSneaking())
                    {
                        PokecubePacketHandler.sendToServer(packet);
                    }
                }
                else
                {
                    PokecubePacketHandler.sendToServer(packet);
                }
            }
            if (evt.entityPlayer.getHeldItem() == null || evt.entityPlayer.getHeldItem().stackSize <= 0)
            {
                int current = evt.entityPlayer.inventory.currentItem;
                evt.entityPlayer.inventory.mainInventory[current] = null;
                evt.entityPlayer.inventory.markDirty();
            }
        }
        if (evt.entityPlayer.worldObj.isRemote || evt.entityPlayer.worldObj.rand.nextInt(10) != 0) return;

        TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(evt.entityPlayer);
        t.checkIndustrial(evt.entityPlayer.worldObj);
    }

    /** Applies the exp from lucky egg and exp share. TODO move this out of
     * PCEventsHandler.
     * 
     * @param evt */
    @SubscribeEvent
    public void KillEvent(pokecube.core.events.KillEvent evt)
    {
        IPokemob killer = evt.killer;
        IPokemob killed = evt.killed;

        if (killer != null)
        {
            EntityLivingBase owner = killer.getPokemonOwner();

            ItemStack stack = ((EntityLivingBase) killer).getHeldItem();
            if (stack != null && PokecubeItems.getStack("luckyegg").isItemEqual(stack))
            {
                int exp = killer.getExp() + Tools.getExp(1, killed.getBaseXP(), killed.getLevel());

                killer.setExp(exp, true, false);
            }

            if (owner != null)
            {
                List<IPokemob> pokemobs = PCEventsHandler.getOutMobs(owner);
                for (IPokemob mob : pokemobs)
                {
                    if (mob instanceof IPokemob)
                    {
                        IPokemob poke = mob;
                        if (((EntityLiving) poke).getHeldItem() != null)
                            if (((EntityLiving) poke).getHeldItem().isItemEqual(PokecubeItems.getStack("exp_share")))
                            {
                            int exp = poke.getExp() + Tools.getExp(1, killed.getBaseXP(), killed.getLevel());

                            poke.setExp(exp, true, false);
                            }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void livingHurtEvent(LivingHurtEvent evt)
    {
        if (evt.entityLiving instanceof EntityPlayer && evt.source == DamageSource.inWall)
        {
            if (evt.entityLiving.ridingEntity instanceof IPokemob) evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingSetTargetEvent(LivingSetAttackTargetEvent evt)
    {
        if (evt.target instanceof EntityLivingBase && evt.entityLiving instanceof EntityLiving)
        {
            List<IPokemob> pokemon = getPokemobs(evt.target, 32);
            if (pokemon.isEmpty()) return;
            double closest = 1000;
            IPokemob newtarget = null;
            for (IPokemob e : pokemon)
            {
                double dist = ((Entity) e).getDistanceSqToEntity(evt.entityLiving);
                if (dist < closest && !(e.getPokemonAIState(IMoveConstants.STAYING)
                        && e.getPokemonAIState(IMoveConstants.SITTING)))
                {
                    closest = dist;
                    newtarget = e;
                }
            }
            if (newtarget != null)
            {
                ((EntityLiving) evt.entityLiving).setAttackTarget((EntityLivingBase) newtarget);
                if (evt.entityLiving instanceof IPokemob)
                {
                    ((IPokemob) evt.entityLiving).setPokemonAIState(IMoveConstants.ANGRY, true);
                    ((IPokemob) evt.entityLiving).setPokemonAIState(IMoveConstants.SITTING, false);
                }
                ((EntityLiving) newtarget).setAttackTarget(evt.entityLiving);
                newtarget.setPokemonAIState(IMoveConstants.ANGRY, true);
            }
        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingUpdateEvent evt)
    {
        if (evt.entity.worldObj.isRemote || evt.entity.isDead) return;

        if (evt.entityLiving instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) evt.entityLiving;
            if (player.getTeam() == null)
            {
                player.worldObj.getScoreboard().addPlayerToTeam(player.getName(), "Trainers");
            }
        }

        if (evt.entityLiving.worldObj.getTotalWorldTime() % 40 == 0)
        {
            TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(evt.entityLiving);
            PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
            if (effect == null)
            {
                terrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
            }
            effect.doEffect(evt.entityLiving, false);
        }

        if (evt.entityLiving instanceof IPokemob && ((IPokemob) evt.entityLiving).getPokedexNb() == 213)
        {
            IPokemob shuckle = (IPokemob) evt.entityLiving;

            if (evt.entityLiving.worldObj.isRemote) return;

            ItemStack item = evt.entityLiving.getHeldItem();
            if (item == null) return;
            Item itemId = item.getItem();
            boolean berry = item.isItemEqual(BerryManager.getBerryItem("oran"));
            Random r = new Random();
            if (berry && r.nextGaussian() > juiceChance)
            {
                if (shuckle.getPokemonOwner() != null)
                {
                    String message = "A sweet smell is coming from " + shuckle.getPokemonDisplayName();
                    ((EntityPlayer) shuckle.getPokemonOwner()).addChatMessage(new ChatComponentText(message));
                }
                shuckle.setHeldItem(new ItemStack(PokecubeItems.berryJuice));
                return;
            }
            berry = itemId == PokecubeItems.berryJuice;
            if (berry && (r.nextGaussian() > candyChance))
            {
                ItemStack candy = PokecubeItems.makeCandyStack();
                if (candy == null) return;

                if (shuckle.getPokemonOwner() != null)
                {
                    String message = "The smell coming from " + shuckle.getPokemonDisplayName() + " has changed";
                    ((EntityPlayer) shuckle.getPokemonOwner()).addChatMessage(new ChatComponentText(message));
                }
                shuckle.setHeldItem(candy);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent.Entity event)
    {
        if (event.getEntity() instanceof IPokemob)
        {
            class Provider extends GuardAICapability implements ICapabilitySerializable<NBTTagCompound>
            {
                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    storage.readNBT(GUARDAI_CAP, this, null, nbt);
                }

                @SuppressWarnings("unchecked") // There isnt anything sane we
                                               // can do about this.
                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (GUARDAI_CAP != null && capability == GUARDAI_CAP) return (T) this;
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
            event.addCapability(new ResourceLocation("pokecube:GuardAI"), new Provider());
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;

        if (entityPlayer.getTeam() == null)
        {
            if (entityPlayer.worldObj.getScoreboard().getTeam("Trainers") == null)
            {
                entityPlayer.worldObj.getScoreboard().createTeam("Trainers");
            }
            entityPlayer.worldObj.getScoreboard().addPlayerToTeam(entityPlayer.getName(), "Trainers");
        }

        if (!evt.player.worldObj.isRemote)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            StatsCollector.writeToNBT(nbt);
            nbt.setBoolean("playerhasstarter", PokecubeSerializer.getInstance().hasStarter(entityPlayer));
            PokecubeSerializer.getInstance().writeToNBT2(nbt);
            nbt.setBoolean("hasSerializer", true);
            boolean offline = !FMLCommonHandler.instance().getMinecraftServerInstance().isServerInOnlineMode();
            nbt.setBoolean("serveroffline", offline);
            PokecubeClientPacket packet = new PokecubeClientPacket(PokecubeClientPacket.STATS, nbt);
            PokecubePacketHandler.sendToClient(packet, entityPlayer);
        }

        if (!evt.player.worldObj.isRemote && evt.player instanceof EntityPlayer)
        {
            if (PokecubeMod.core.getConfig().guiOnLogin && !PokecubeSerializer.getInstance().hasStarter(entityPlayer))
            {
                new ChooseFirst(evt.player);
            }
        }
    }

    @SubscribeEvent
    public void TickEvent(WorldTickEvent evt)
    {
        if (evt.phase == Phase.END && evt.side != Side.CLIENT)
        {
            PokecubeCore.instance.spawner.tick(evt.world);
        }
    }

    @SubscribeEvent
    public void worldLoadEvent(Load evt)
    {
        if (evt.world.isRemote) { return; }
        PokecubeMod.getFakePlayer(evt.world);
    }

    @SubscribeEvent
    public void WorldSave(WorldEvent.Save evt)
    {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER && evt.world.provider.getDimensionId() == 0)
        {
            long time = System.nanoTime();
            PokecubeSerializer.getInstance().save();
            double dt = (System.nanoTime() - time) / 1000000d;
            if (dt > 20) System.err.println("Took " + dt + "ms to save pokecube data");
        }
    }
}
