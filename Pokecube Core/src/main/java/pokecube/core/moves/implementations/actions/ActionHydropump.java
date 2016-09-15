package pokecube.core.moves.implementations.actions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class ActionHydropump implements IMoveAction
{
    public ActionHydropump()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getPokemonAIState(IMoveConstants.ANGRY)) return false;
        if (user.getPokemonOwner() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) user.getPokemonOwner();
            BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(),
                    location.getBlockState(player.getEntityWorld()), player);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return false;
        }
        MoveEventsHandler.doDefaultWater(user, MovesUtils.getMoveFromName(getMoveName()), location);
        location.setBlock(((Entity) user).getEntityWorld(), Blocks.FLOWING_WATER.getStateFromMeta(1));
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "hydropump";
    }
}
