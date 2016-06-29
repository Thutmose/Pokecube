package pokecube.adventures.events;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.handlers.PASaveHandler;
import pokecube.adventures.handlers.TeamManager;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public class TeamEventsHandler
{
    public static boolean shouldRenderVolume = false;

    Vector3               v                  = Vector3.getNewVector(), v1 = Vector3.getNewVector();

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        EntityPlayer player = evt.getPlayer();
        // TODO interface with
        // forge permissions API
        // here as well
        if (player != null && player.getTeam() != null)
        {
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.getPos(), player.dimension);
            if (!TeamManager.getInstance().isOwned(c)) return;
            if (!player.worldObj.isRemote)
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
    public void ExplosionEvent(ExplosionEvent.Detonate evt)
    {
        if (!TeamManager.denyBlasts) return;
        int dimension = evt.getWorld().provider.getDimension();
        List<BlockPos> toRemove = Lists.newArrayList();
        for (BlockPos pos : evt.getAffectedBlocks())
        {
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(pos, dimension);
            String owner = TeamManager.getInstance().getLandOwner(c);
            if (evt.getExplosion().getExplosivePlacedBy() instanceof EntityPlayer)
            {
                String team = evt.getWorld().getScoreboard()
                        .getPlayersTeam(evt.getExplosion().getExplosivePlacedBy().getName()).getRegisteredName();
                if (owner.equals(team))
                {
                    owner = null;
                }
            }
            if (owner != null) continue;
            toRemove.add(pos);
        }
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent
    public void placeEvent(PlayerInteractEvent.RightClickBlock evt)
    {
        ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = TeamManager.getInstance().getLandOwner(c);
        if (owner == null) return;

        Block block = null;
        IBlockState state = evt.getWorld().getBlockState(evt.getPos());
        block = evt.getWorld().getBlockState(evt.getPos()).getBlock();
        boolean b = true;
        String team = evt.getWorld().getScoreboard().getPlayersTeam(evt.getEntityPlayer().getName())
                .getRegisteredName();

        if (owner.equals(team))
        {
            // return;
        }
        else if (block != null && evt.getEntityPlayer().getHeldItemMainhand() != null
                && evt.getEntityPlayer().getHeldItemMainhand().getItem() instanceof IPokecube)
        {
            b = block.onBlockActivated(evt.getWorld(), evt.getPos(), state, evt.getEntityPlayer(), EnumHand.MAIN_HAND,
                    null, evt.getFace(), (float) evt.getHitVec().xCoord, (float) evt.getHitVec().yCoord,
                    (float) evt.getHitVec().zCoord);
            if (!b) { return; }
        }
        if (!b && evt.getEntityPlayer().getHeldItemMainhand() == null) return;

        ChunkCoordinate blockLoc = new ChunkCoordinate(evt.getPos(), evt.getEntityPlayer().dimension);
        TeamManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!TeamManager.getInstance().isPublic(blockLoc))
            {
                evt.getEntityPlayer().addChatMessage(
                        new TextComponentString("You must be a member of Team " + owner + " to do that."));
                evt.setUseBlock(Result.DENY);
                evt.setCanceled(true);
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
            PASaveHandler.getInstance().loadBag();
            PASaveHandler.getInstance().loadTeams();
        }
    }

}
