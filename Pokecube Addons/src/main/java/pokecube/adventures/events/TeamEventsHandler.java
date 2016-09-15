package pokecube.adventures.events;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.handlers.PASaveHandler;
import pokecube.adventures.handlers.TeamManager;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.block.IOwnableTE;
import thut.api.maths.Vector3;

public class TeamEventsHandler
{
    public static boolean shouldRenderVolume = false;
    Vector3               v                  = Vector3.getNewVector(), v1 = Vector3.getNewVector();

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        EntityPlayer player = evt.getPlayer();
        TileEntity tile = evt.getWorld().getTileEntity(evt.getPos());
        if (tile instanceof IOwnableTE)
        {
            IOwnableTE te = (IOwnableTE) tile;
            NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
            if (tag.hasKey("admin") && tag.getBoolean("admin") && !te.canEdit(player))
            {
                evt.setCanceled(true);
                return;
            }
        }
        if (PokecubeAdv.conf.teamsEnabled && player != null && player.getTeam() != null)
        {
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.getPos(), player.dimension);
            if (!TeamManager.getInstance().isOwned(c)) return;
            if (!player.getEntityWorld().isRemote)
            {
                UserListOpsEntry userentry = ((EntityPlayerMP) player).mcServer.getPlayerList().getOppedPlayers()
                        .getEntry(player.getGameProfile());

                if (userentry != null
                        || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return; }
            }
            if (TeamManager.getInstance().isOwned(c)
                    && !TeamManager.getInstance().isTeamLand(c, player.getTeam().getRegisteredName()))
            {
                player.addChatMessage(new TextComponentString("You may not remove blocks from land owned by Team "
                        + TeamManager.getInstance().getLandOwner(c)));
                evt.setCanceled(true);
                return;
            }
            ChunkCoordinate block = new ChunkCoordinate(evt.getPos(), evt.getWorld().provider.getDimension());
            TeamManager.getInstance().unsetPublic(block);
        }
    }

    @SubscribeEvent
    public void EntityUpdate(LivingUpdateEvent evt)
    {
        if (evt.getEntityLiving() instanceof EntityPlayer
                && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            EntityPlayer player = (EntityPlayer) evt.getEntityLiving();
            BlockPos here;
            BlockPos old;
            here = new BlockPos(player.chasingPosX, player.chasingPosY, player.chasingPosZ);
            old = new BlockPos(player.prevChasingPosX, player.prevChasingPosY, player.prevChasingPosZ);
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(here, player.dimension);
            ChunkCoordinate c1 = ChunkCoordinate.getChunkCoordFromWorldCoord(old, player.dimension);
            if (c.equals(c1) || !PokecubeAdv.conf.teamsEnabled) return;
            SpawnHandler.refreshTerrain(v.set(player), player.worldObj);
            if (TeamManager.getInstance().isOwned(c) || TeamManager.getInstance().isOwned(c1))
            {
                String team = TeamManager.getInstance().getLandOwner(c);
                String team1 = TeamManager.getInstance().getLandOwner(c1);
                if (team != null)
                {
                    if (team.equals(team1)) return;
                    if (team1 != null)
                    {
                        evt.getEntity().addChatMessage(new TextComponentTranslation("msg.team.exitLand", team1));
                    }
                    evt.getEntity().addChatMessage(new TextComponentTranslation("msg.team.enterLand", team));
                }
                else
                {
                    evt.getEntity().addChatMessage(new TextComponentTranslation("msg.team.exitLand", team1));
                }
            }
        }
    }

    @SubscribeEvent
    public void ExplosionEvent(ExplosionEvent.Detonate evt)
    {
        List<BlockPos> toRemove = Lists.newArrayList();
        for (BlockPos pos : evt.getAffectedBlocks())
        {
            TileEntity tile = evt.getWorld().getTileEntity(pos);
            if (tile instanceof IOwnableTE)
            {
                NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
                if (tag.hasKey("admin") && tag.getBoolean("admin"))
                {
                    toRemove.add(pos);
                }
            }
        }
        if (TeamManager.denyBlasts && PokecubeAdv.conf.teamsEnabled)
        {
            int dimension = evt.getWorld().provider.getDimension();
            for (BlockPos pos : evt.getAffectedBlocks())
            {
                ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(pos, dimension);
                String owner = TeamManager.getInstance().getLandOwner(c);
                if (evt.getExplosion().getExplosivePlacedBy() instanceof EntityPlayer)
                {
                    ScorePlayerTeam playerTeam = evt.getWorld().getScoreboard()
                            .getPlayersTeam(evt.getExplosion().getExplosivePlacedBy().getName());
                    if (playerTeam != null)
                    {
                        String team = playerTeam.getRegisteredName();
                        if (owner.equals(team))
                        {
                            owner = null;
                        }
                    }
                }
                if (owner == null) continue;
                toRemove.add(pos);
            }
        }
        evt.getAffectedBlocks().removeAll(toRemove);
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactLeftClickBlock(PlayerInteractEvent.LeftClickBlock evt)
    {
        ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = TeamManager.getInstance().getLandOwner(c);
        if (owner == null || !PokecubeAdv.conf.teamsEnabled) return;
        String team = evt.getWorld().getScoreboard().getPlayersTeam(evt.getEntityPlayer().getName())
                .getRegisteredName();
        if (owner.equals(team)) { return; }
        ChunkCoordinate blockLoc = new ChunkCoordinate(evt.getPos(), evt.getEntityPlayer().dimension);
        TeamManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!TeamManager.getInstance().isPublic(blockLoc))
            {
                evt.setUseBlock(Result.DENY);
                evt.setCanceled(true);
                if (!evt.getWorld().isRemote)
                    evt.getEntity().addChatMessage(new TextComponentTranslation("msg.team.deny", owner));
            }
            evt.setUseItem(Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactRightClickItem(PlayerInteractEvent.RightClickItem evt)
    {
        ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = TeamManager.getInstance().getLandOwner(c);
        if (owner == null || evt.getItemStack().getItem() instanceof ItemFood || !PokecubeAdv.conf.teamsEnabled) return;

        String team = evt.getWorld().getScoreboard().getPlayersTeam(evt.getEntityPlayer().getName())
                .getRegisteredName();

        if (owner.equals(team))
        {
            return;
        }
        else if (evt.getItemStack() != null && evt.getItemStack().getItem() instanceof IPokecube) { return; }
        if (evt.getItemStack() == null) return;

        ChunkCoordinate blockLoc = new ChunkCoordinate(evt.getPos(), evt.getEntityPlayer().dimension);
        TeamManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!TeamManager.getInstance().isPublic(blockLoc))
            {
                evt.setResult(Result.DENY);
                evt.setCanceled(true);
            }
            evt.setResult(Result.DENY);
        }
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = TeamManager.getInstance().getLandOwner(c);
        if (owner == null || !PokecubeAdv.conf.teamsEnabled) return;

        Block block = null;
        IBlockState state = evt.getWorld().getBlockState(evt.getPos());
        block = evt.getWorld().getBlockState(evt.getPos()).getBlock();
        boolean b = true;
        String team = evt.getWorld().getScoreboard().getPlayersTeam(evt.getEntityPlayer().getName())
                .getRegisteredName();

        if (owner.equals(team))
        {
            return;
        }
        else if (block != null && !(block.hasTileEntity(state)) && evt.getWorld().isRemote)
        {
            b = block.onBlockActivated(evt.getWorld(), evt.getPos(), state, evt.getEntityPlayer(), evt.getHand(), null,
                    evt.getFace(), (float) evt.getHitVec().xCoord, (float) evt.getHitVec().yCoord,
                    (float) evt.getHitVec().zCoord);
        }
        if (!b && (evt.getItemStack() == null || evt.getItemStack().getItem() instanceof IPokecube)) return;

        ChunkCoordinate blockLoc = new ChunkCoordinate(evt.getPos(), evt.getEntityPlayer().dimension);
        TeamManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!TeamManager.getInstance().isPublic(blockLoc))
            {
                evt.setUseBlock(Result.DENY);
                evt.setCanceled(true);
                if (!evt.getWorld().isRemote && evt.getHand() == EnumHand.MAIN_HAND)
                {
                    evt.getEntity().addChatMessage(new TextComponentTranslation("msg.team.deny", owner));
                }
            }
            evt.setUseItem(Result.DENY);
        }
    }

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        if (evt.getWorld().provider.getDimension() == 0 && !evt.getWorld().isRemote)
        {
            if (evt.getWorld().getScoreboard().getTeam("Pokecube") == null)
            {
                evt.getWorld().getScoreboard().createTeam("Pokecube");
            }
            if (evt.getWorld().getScoreboard().getTeam("Pokecube").getMembershipCollection().isEmpty())
            {
                evt.getWorld().getScoreboard().addPlayerToTeam("PokecubePlayer", "Pokecube");
            }
            PASaveHandler.getInstance().loadTeams();
        }
    }
}
