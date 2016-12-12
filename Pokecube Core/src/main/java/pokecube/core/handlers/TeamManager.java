package pokecube.core.handlers;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;

public class TeamManager
{
    public static interface ITeamProvider
    {
        @Nonnull
        String getTeam(Entity entityIn);
    }

    public static class DefaultProvider implements ITeamProvider
    {
        @Override
        @Nonnull
        public String getTeam(Entity entityIn)
        {
            Team team = entityIn.getTeam();
            return team == null ? "" : team.getRegisteredName();
        }
    }

    public static ITeamProvider provider = new DefaultProvider();

    @Nonnull
    public static String getTeam(Entity entityIn)
    {
        return provider.getTeam(entityIn);
    }

    public static boolean sameTeam(Entity entityA, Entity entityB)
    {
        return getTeam(entityA).equals(getTeam(entityB));
    }
}
