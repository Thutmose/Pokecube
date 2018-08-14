package pokecube.core.moves.implementations.actions;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionDig implements IMoveAction
{
    public ActionDig()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getCombatState(CombatStates.ANGRY)) return false;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        count = (int) Math.max(1, Math.ceil(digHole(user, location, true) * Math.pow((100 - level) / 100d, 3)))
                * hungerValue;
        if (count > 0)
        {
            digHole(user, location, false);
            used = true;
            user.setHungerTime(user.getHungerTime() + count);
        }
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "dig";
    }

    private int digHole(IPokemob digger, Vector3 v, boolean count)
    {
        int ret = 0;

        EntityLivingBase owner = digger.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer)
        {
            player = (EntityPlayer) owner;

            BreakEvent evt = new BreakEvent(player.getEntityWorld(), v.getPos(),
                    v.getBlockState(player.getEntityWorld()), player);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return 0;
        }

        boolean silky = Move_Basic.shouldSilk(digger) && player != null;
        boolean dropAll = shouldDropAll(digger);
        double uselessDrop = Math.pow((100 - digger.getLevel()) / 100d, 3);
        Vector3 temp = Vector3.getNewVector();
        World world = digger.getEntity().getEntityWorld();
        temp.set(v);
        int range = 1;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(world);
                    Block block = state.getBlock();
                    if (PokecubeTerrainChecker.isTerrain(state))
                    {
                        boolean drop = true;
                        if (!dropAll && !silky && uselessDrop < Math.random()) drop = false;
                        if (!count)
                        {
                            if (!silky) temp.breakBlock(world, drop);
                            else
                            {
                                if (block.canSilkHarvest(world, temp.getPos(), state, player))
                                {
                                    Move_Basic.silkHarvest(state, temp.getPos(), world, player);
                                    temp.breakBlock(world, drop);
                                }
                                else
                                {
                                    temp.breakBlock(world, drop);
                                }
                            }
                        }
                        ret++;
                    }
                }
        return ret;
    }

    private boolean shouldDropAll(IPokemob pokemob)
    {
        if (pokemob.getAbility() == null) return false;
        Ability ability = pokemob.getAbility();
        return ability.toString().equalsIgnoreCase("arenatrap");
    }
}
