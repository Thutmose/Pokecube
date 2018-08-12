package pokecube.compat.tecompat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.TeamManager.ITeamProvider;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.essentials.ThutEssentials;
import thut.essentials.events.DenyItemUseEvent;
import thut.essentials.events.DenyItemUseEvent.UseType;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class EssentialsCompat
{

    @Optional.Method(modid = "thutessentials")
    @CompatClass(phase = Phase.POST)
    public static void thutEssentialsCompat()
    {
        new pokecube.compat.tecompat.EssentialsCompat();
    }

    public static class TeamProvider implements ITeamProvider
    {
        final ITeamProvider defaults;

        public TeamProvider(ITeamProvider defaults)
        {
            this.defaults = defaults;
        }

        @Override
        public boolean areAllied(String team, Entity target)
        {
            LandTeam teamA = LandManager.getInstance().getTeam(team, false);
            LandTeam teamB = LandManager.getInstance().getTeam(getTeam(target), false);
            // Both teams need to consider each other allies.
            if (teamA != null && teamB != null) { return teamA.isAlly(teamB) && teamB.isAlly(teamA); }
            return false;
        }

        @Override
        public String getTeam(Entity entityIn)
        {
            if (entityIn.world.isRemote) return defaults.getTeam(entityIn);
            if (entityIn instanceof EntityPlayer)
            {
                LandTeam team = LandManager.getTeam(entityIn);
                return team.teamName;
            }
            IPokemob pokemob;
            if (entityIn instanceof IEntityOwnable)
            {
                IEntityOwnable mob = (IEntityOwnable) entityIn;
                Entity owner = mob.getOwner();
                if (mob.getOwnerId() != null)
                {
                    LandTeam team = LandManager.getTeam(mob.getOwnerId());
                    return team.teamName;
                }
                if (owner != null && !(owner instanceof IEntityOwnable)) return getTeam(mob.getOwner());
            }
            else if ((pokemob = CapabilityPokemob.getPokemobFor(entityIn)) != null)
            {
                Entity owner = pokemob.getOwner();
                if (owner != null && !(owner instanceof IEntityOwnable)) return getTeam(pokemob.getOwner());
            }
            return defaults.getTeam(entityIn);
        }

    }

    public EssentialsCompat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        if (ThutEssentials.instance.config.landEnabled) TeamManager.provider = new TeamProvider(TeamManager.provider);
    }

    @SubscribeEvent
    public void onItemUse(DenyItemUseEvent evt)
    {
        if (evt.getType() == UseType.RIGHTCLICKITEM || evt.getType() == UseType.RIGHTCLICKBLOCK)
        {
            if (evt.getItem() == null || evt.getItem().getItem() instanceof IPokecube)
            {
                evt.setCanceled(true);
            }
        }
    }
}
