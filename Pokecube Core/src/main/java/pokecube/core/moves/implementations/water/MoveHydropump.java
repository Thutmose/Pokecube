package pokecube.core.moves.implementations.water;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.maths.Vector3;

public class MoveHydropump extends Move_Basic
{
    public MoveHydropump()
    {
        super("hydropump");
    }

    @Override
    public void doWorldAction(IPokemob attacker, Vector3 location)
    {
        if (!PokecubeMod.pokemobsDamageBlocks) return;
        super.doWorldAction(attacker, location);
        if (attacker.getPokemonOwner() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) attacker.getPokemonOwner();
            BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(),
                    location.getBlockState(player.getEntityWorld()), player);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return;
        }
        location.setBlock(((Entity) attacker).getEntityWorld(), Blocks.FLOWING_WATER.getStateFromMeta(1));
    }
}
