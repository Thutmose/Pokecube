package pokecube.core.handlers;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.scoreboard.Team;

public class TeamManager
{
    public static interface ITeamProvider
    {
        @Nonnull
        String getTeam(Entity entityIn);

        default boolean areAllied(String team, Entity target)
        {
            return team.equals(getTeam(target));
        }
    }

    public static class DefaultProvider implements ITeamProvider
    {
        @Override
        @Nonnull
        public String getTeam(Entity entityIn)
        {
            Team team = entityIn.getTeam();
            String name = team == null ? "" : team.getName();
            if (entityIn instanceof IEntityOwnable && team == null)
            {
                UUID id = ((IEntityOwnable) entityIn).getOwnerId();
                if (id != null) name = id.toString();
            }
            return name;
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
        String teamA = getTeam(entityA);
        return !teamA.isEmpty() && provider.areAllied(teamA, entityB);
    }
}
