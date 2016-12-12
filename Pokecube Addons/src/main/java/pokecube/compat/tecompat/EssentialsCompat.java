package pokecube.compat.tecompat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.TeamManager.ITeamProvider;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
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
        public String getTeam(Entity entityIn)
        {
            if (entityIn instanceof EntityPlayer)
            {
                LandTeam team = LandManager.getTeam(entityIn);
                return team.teamName;
            }
            if (entityIn instanceof IPokemob)
            {
                IPokemob pokemob = (IPokemob) entityIn;
                Entity owner = pokemob.getPokemonOwner();
                if (owner != null && !(owner instanceof IPokemob)) return getTeam(pokemob.getPokemonOwner());
            }
            return defaults.getTeam(entityIn);
        }

    }

    public EssentialsCompat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        TeamManager.provider = new TeamProvider(TeamManager.provider);
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
