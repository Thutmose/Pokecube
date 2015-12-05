package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityItem;
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
import net.minecraft.util.Vec3;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.ai.properties.GuardAIProperties;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
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
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.gen.WorldGenStartBuilding;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class EventsHandler
{
    public EventsHandler()
    {
        MinecraftForge.EVENT_BUS.register(new StatsHandler());
        PokemobAIThread aiTicker = new PokemobAIThread();
        MinecraftForge.EVENT_BUS.register(aiTicker);
        new UpdateNotifier();
    }

    static double max        = 0;
    static int    count      = 0;
    static int    countAbove = 0;
    static double mean       = 0;
    static long   starttime  = 0;

    private HashSet<Integer> pendingStarters = new HashSet<Integer>();

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        if (evt.world.isRemote) { return; }
        PokecubeMod.getFakePlayer(evt.world);

        if (evt.world.provider.getDimensionId() == 0 && !evt.world.isRemote)
        {
            AISaveHandler.instance();
        }

        if (Mod_Pokecube_Helper.deactivateMonsters && evt.world.getChunkProvider() instanceof ChunkProviderHell)
        {
            // ChunkProviderHell provider = (ChunkProviderHell)
            // evt.world.getChunkProvider();

            // provider.genNetherBridge.getSpawnList().clear();//TODO remove
            // nether bridge mobs via reflection
        }
    }

    @SubscribeEvent
    public void placeEvent(PlaceEvent event)
    {
        TileEntity te = event.world.getTileEntity(event.pos);
        if (te != null && te instanceof TileEntityOwnable)
        {
            TileEntityOwnable ownable = (TileEntityOwnable) te;
            ownable.setPlacer(event.player);
        }
    }

    @SubscribeEvent
    public void ExplosionEvents(ExplosionEvent evt)
    {
        if (evt.explosion instanceof ExplosionCustom && evt instanceof ExplosionEvent.Detonate)
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
    public void ClientLogOffEvent(Unload evt)
    {
        WorldGenStartBuilding.building = false;
    }

    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event)
    {
        if (event.entity instanceof EntityLiving)
        {
            if (GuardAIProperties.get((EntityLiving) event.entity) == null)
            {
                GuardAIProperties.register((EntityLiving) event.entity);
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

            Vector3 look = Vector3.getNewVectorFromPool().set(evt.entityPlayer.getLook(1));
            Vector3 temp = Vector3.getNewVectorFromPool().set(evt.entityPlayer).addTo(0,
                    evt.entityPlayer.getEyeHeight(), 0);

            Entity target = temp.firstEntityExcluding(32, look, evt.entityPlayer.worldObj, false, evt.entityPlayer);

            look.freeVectorFromPool();
            temp.freeVectorFromPool();
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(5));
            buffer.writeByte(PokecubeServerPacket.POKECUBEUSE);
            if (target != null)
            {
                buffer.writeInt(target.getEntityId());
            }
            else
            {
                buffer.writeInt(-1);
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
                    Vec3 hitVec = FMLClientHandler.instance().getClient().objectMouseOver.hitVec;
                    float f = (float) hitVec.xCoord - evt.pos.getX();
                    float f1 = (float) hitVec.yCoord - evt.pos.getY();
                    float f2 = (float) hitVec.zCoord - evt.pos.getZ();
                    IBlockState state = evt.world.getBlockState(evt.pos);
                    boolean b = block.onBlockActivated(evt.world, evt.pos, state, evt.entityPlayer, evt.face, f, f1,
                            f2);
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

    @SubscribeEvent
    public void TickEvent(WorldTickEvent evt)
    {
        if (evt.phase == Phase.END && evt.side != Side.CLIENT)
        {
            mod_Pokecube.instance.spawner.tick(evt.world);
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
            entityPlayer.worldObj.getScoreboard().addPlayerToTeam(entityPlayer.getCommandSenderName(), "Trainers");
        }

        if (!evt.player.worldObj.isRemote)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            StatsCollector.writeToNBT(nbt);

            PokecubeSerializer.getInstance().writeToNBT2(nbt);
            nbt.setBoolean("hasSerializer", true);
            boolean offline = !FMLCommonHandler.instance().getMinecraftServerInstance().isServerInOnlineMode();
            nbt.setBoolean("serveroffline", offline);
            PokecubeClientPacket packet = new PokecubeClientPacket(PokecubeClientPacket.STATS, nbt);
            PokecubePacketHandler.sendToClient(packet, entityPlayer);
        }

        if (!evt.player.worldObj.isRemote && evt.player instanceof EntityPlayer)
        {
            if (Mod_Pokecube_Helper.guiOnLogin && !PokecubeSerializer.getInstance().hasStarter(entityPlayer))
            {
                pendingStarters.add(evt.player.getEntityId());
            }
        }
    }

    @SubscribeEvent
    public void PlayerLoggout(PlayerLoggedOutEvent evt)
    {
        System.out.println("LoggedOut Event " + " " + FMLCommonHandler.instance().getEffectiveSide());
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT && !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
        {
            System.out.println("Clearing Server instance");
            PokecubeSerializer.getInstance().clearInstance();
            AISaveHandler.clearInstance();
            pendingStarters.clear();
        }
    }

    static boolean notified = false;

    @SubscribeEvent
    public void EntityJoinWorld(EntityJoinWorldEvent evt)
    {
        if (Mod_Pokecube_Helper.disableMonsters && !(evt.entity instanceof IPokemob) && evt.entity instanceof IMob
                && !(evt.entity instanceof EntityDragon || evt.entity instanceof EntityDragonPart))
        {
            evt.entity.setDead();
            Vector3 location = Vector3.getNewVectorFromPool().set(evt.entity);
            int num = getShadowPokemonNb(evt.entity);
            Entity shadow = PokecubeMod.core.createEntityByPokedexNb(num, evt.world);
            if (shadow == null)
            {
                System.err.println(num);
                location.freeVectorFromPool();
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
            location.freeVectorFromPool();
        }
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

    // 4 = 1 per 10mins, 2 = 1 per 10s, 5 = 1 per 48 hours
    public static double candyChance = 4.5;
    public static double juiceChance = 3.5;

    @SubscribeEvent
    public void livingUpdate(LivingUpdateEvent evt)
    {
        if (evt.entity.worldObj.isRemote || evt.entity.isDead) return;

        if (evt.entityLiving instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) evt.entityLiving;
            if (player.getTeam() == null)
            {
                player.worldObj.getScoreboard().addPlayerToTeam(player.getCommandSenderName(), "Trainers");
            }
            if (pendingStarters.contains(player.getEntityId()))
            {
                PokecubeClientPacket packet2 = new PokecubeClientPacket(
                        new byte[] { PokecubeClientPacket.CHOOSE1ST });
                System.out.println("Sending Packet to " + player);
                PokecubePacketHandler.sendToClient(packet2, player);
                pendingStarters.remove(player.getEntityId());
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
        if (evt.target instanceof EntityPlayer && evt.entityLiving instanceof EntityLiving)
        {
            List<IPokemob> pokemon = getPokemobs((EntityPlayer) evt.target, 32);
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
                    ((IPokemob) evt.entityLiving).setPokemonAIState(IPokemob.ANGRY, true);
                    ((IPokemob) evt.entityLiving).setPokemonAIState(IPokemob.SITTING, false);
                }
                ((EntityLiving) newtarget).setAttackTarget(evt.entityLiving);
                newtarget.setPokemonAIState(IPokemob.ANGRY, true);
            }
        }
    }

    public static List<IPokemob> getPokemobs(EntityPlayer owner, double distance)
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

    public static void recallAllPokemobsExcluding(EntityPlayer player, IPokemob excluded)
    {
        List<?> pokemobs = new ArrayList<Object>(player.worldObj.loadedEntityList);
        for (Object o : pokemobs)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
                if (mob != excluded && mob.getPokemonOwner() == player && !mob.getPokemonAIState(IPokemob.GUARDING)
                        && !mob.getPokemonAIState(IPokemob.STAYING))
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
                    if (name != null && (name.equalsIgnoreCase(player.getCommandSenderName())
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

    public static int getShadowPokemonNb(Entity hostile)
    {
        String temp = hostile.getCommandSenderName().toLowerCase().trim().replace(" ", "");

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

    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                Object o = Loader.instance().getIndexedModList().get(PokecubeMod.ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    String mess = "Current Listed Release Version of Pokecube Core is " + result.target
                            + ", but you have " + PokecubeMod.VERSION + ".";
                    mess += "\nIf you find bugs, please update and check if they still occur before reporting them.";
                    (event.player).addChatMessage(new ChatComponentText(mess));
                }
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }
}
