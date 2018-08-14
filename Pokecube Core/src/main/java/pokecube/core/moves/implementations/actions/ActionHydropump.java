package pokecube.core.moves.implementations.actions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class ActionHydropump implements IMoveAction
{
    public ActionHydropump()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getCombatState(CombatStates.ANGRY)) return false;
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        MoveEventsHandler.doDefaultWater(user, MovesUtils.getMoveFromName(getMoveName()), location);
        Vector3 source = Vector3.getNewVector().set(user.getEntity());
        double dist = source.distanceTo(location);
        Vector3 dir = location.subtract(source).norm();
        Vector3 temp = Vector3.getNewVector();
        for (int i = 0; i < dist; i++)
        {
            Entity player = user.getEntity();
            temp.set(dir).scalarMultBy(i).addTo(source);
            IBlockState state = temp.getBlockState(player.getEntityWorld());
            if (!state.getMaterial().isReplaceable()) continue;
            if (user.getPokemonOwner() instanceof EntityPlayer)
            {
                BreakEvent evt = new BreakEvent(player.getEntityWorld(), temp.getPos(), state,
                        (EntityPlayer) user.getPokemonOwner());
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) continue;
            }
            TickHandler.addBlockChange(temp, player.dimension, Blocks.FLOWING_WATER, 1);
        }
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "hydropump";
    }
}
