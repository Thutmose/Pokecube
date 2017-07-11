package pokecube.compat.ftblib;

import com.feed_the_beast.ftbl.api.EnumTeamStatus;
import com.feed_the_beast.ftbl.api.FTBLibAPI;
import com.feed_the_beast.ftbl.api.FTBLibPlugin;
import com.feed_the_beast.ftbl.api.IFTBLibPlugin;
import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.api.IForgeTeam;
import com.feed_the_beast.ftbl.api.IUniverse;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.TeamManager.ITeamProvider;
import pokecube.core.interfaces.PokecubeMod;

public class FTBLibCompat implements IFTBLibPlugin
{
    public static class TeamProvider implements ITeamProvider
    {
        final ITeamProvider defaults;
        final FTBLibAPI     api;

        public TeamProvider(ITeamProvider defaults, FTBLibAPI api)
        {
            this.defaults = defaults;
            this.api = api;
        }

        private String getPlayerTeam(EntityPlayer player)
        {
            IUniverse universe = api.getUniverse();
            if (universe == null) return "";
            IForgePlayer iplayer = universe.getPlayer(player);
            if (iplayer == null) return "";
            IForgeTeam team = iplayer.getTeam();
            if (team == null) return "";
            return team.getName();
        }

        @Override
        public String getTeam(Entity entityIn)
        {
            if (entityIn instanceof EntityPlayer) return getPlayerTeam((EntityPlayer) entityIn);
            if (entityIn instanceof IEntityOwnable)
            {
                IEntityOwnable pokemob = (IEntityOwnable) entityIn;
                Entity owner = pokemob.getOwner();
                if (owner != null && !(owner instanceof IEntityOwnable)) return getTeam(pokemob.getOwner());
            }
            return defaults.getTeam(entityIn);
        }

        @Override
        public boolean areAllied(String team, Entity target)
        {
            IUniverse universe = api.getUniverse();
            if (universe == null) return false;
            IForgeTeam aTeam = universe.getTeam(team);
            if (aTeam == null) return false;
            EntityPlayer player = null;
            if (target instanceof EntityPlayer) player = (EntityPlayer) target;
            else if (target instanceof IEntityOwnable && ((IEntityOwnable) target).getOwner() instanceof EntityPlayer)
            {
                player = (EntityPlayer) ((IEntityOwnable) target).getOwner();
            }
            if (player != null)
            {
                IForgePlayer iplayer = universe.getPlayer(player);
                return aTeam.getHighestStatus(iplayer).isEqualOrGreaterThan(EnumTeamStatus.ALLY);
            }
            return false;
        }
    }

    @FTBLibPlugin
    public static FTBLibCompat INSTANCE = new FTBLibCompat();

    public FTBLibCompat()
    {
    }

    @Override
    public void init(FTBLibAPI api)
    {
        PokecubeMod.log("Registering Team Manager for FTBL");
        TeamManager.provider = new TeamProvider(TeamManager.provider, api);
    }

}
