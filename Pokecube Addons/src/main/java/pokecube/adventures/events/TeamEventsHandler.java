package pokecube.adventures.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
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
    Vector3 v = Vector3.getNewVector(), v1 = Vector3.getNewVector();

    public static boolean shouldRenderVolume = false;

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent
    public void placeEvent(PlayerInteractEvent evt)
    {
        if (evt.action == Action.RIGHT_CLICK_BLOCK)
        {
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.pos, evt.entityPlayer.dimension);
            String owner = TeamManager.getInstance().getLandOwner(c);
            if (owner == null) return;

            Block block = null;
            IBlockState state = evt.world.getBlockState(evt.pos);
            block = evt.world.getBlockState(evt.pos).getBlock();
            boolean b = true;
            String team = evt.world.getScoreboard().getPlayersTeam(evt.entityPlayer.getName()).getRegisteredName();

            if (owner.equals(team))
            {
                // return;
            }
            else if (block != null && evt.entityPlayer.getHeldItem() != null
                    && evt.entityPlayer.getHeldItem().getItem() instanceof IPokecube)
            {
                b = block.onBlockActivated(evt.world, evt.pos, state, evt.entityPlayer, evt.face, 0, 0, 0);
                if (!b) { return; }
            }
            if (!b && evt.entityPlayer.getHeldItem() == null) return;

            ChunkCoordinate blockLoc = new ChunkCoordinate(evt.pos, evt.entityPlayer.dimension);
            TeamManager.getInstance().isPublic(blockLoc);
            if (!team.equals(owner))
            {
                if (!TeamManager.getInstance().isPublic(blockLoc))
                {
                    evt.entityPlayer.addChatMessage(
                            new ChatComponentText("You must be a member of Team " + owner + " to do that."));
                    evt.useBlock = Result.DENY;
                    evt.setCanceled(true);
                }
                evt.useItem = Result.DENY;
            }
        }
    }

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        if (evt.world.provider.getDimensionId() == 0 && !evt.world.isRemote)
        {
            if (evt.world.getScoreboard().getTeam("Pokecube") == null)
            {
                evt.world.getScoreboard().createTeam("Pokecube");
            }
            if (evt.world.getScoreboard().getTeam("Pokecube").getMembershipCollection().isEmpty())
            {
                evt.world.getScoreboard().addPlayerToTeam("PokecubePlayer", "Pokecube");
            }
            PASaveHandler.getInstance().loadBag();
            PASaveHandler.getInstance().loadTeams();
        }
    }

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        EntityPlayer player = evt.getPlayer();
        if (player != null && player.getTeam() != null) // TODO interface with forge permissions API here as well
        {
            ChunkCoordinate c = ChunkCoordinate.getChunkCoordFromWorldCoord(evt.pos, player.dimension);
            if (!TeamManager.getInstance().isOwned(c)) return;
            if (!player.worldObj.isRemote)
            {
                UserListOpsEntry userentry = (UserListOpsEntry) ((EntityPlayerMP) player).mcServer
                        .getConfigurationManager().getOppedPlayers().getEntry(player.getGameProfile());

                if (userentry != null
                        || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return; }
            }
            if (TeamManager.getInstance().isOwned(c)
                    && !TeamManager.getInstance().isTeamLand(c, player.getTeam().getRegisteredName()))
            {
                player.addChatMessage(new ChatComponentText("You may not remove blocks from land owned by Team "
                        + TeamManager.getInstance().getLandOwner(c)));
                evt.setCanceled(true);
                return;
            }
            ChunkCoordinate block = new ChunkCoordinate(evt.pos, evt.world.provider.getDimensionId());
            TeamManager.getInstance().unsetPublic(block);
        }
    }

}
