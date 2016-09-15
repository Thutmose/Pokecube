package pokecube.core.moves.implementations.actions;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionDig implements IMoveAction
{
    public ActionDig()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getPokemonAIState(IMoveConstants.ANGRY)) return false;
        IHungrymob mob = (IHungrymob) user;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;

        EntityLivingBase owner = user.getPokemonOwner();
        boolean repel = SpawnHandler.checkNoSpawnerInArea(((Entity) user).getEntityWorld(), location.intX(),
                location.intY(), location.intZ());
        if (owner != null && owner instanceof EntityPlayer)
        {
            if (!repel)
            {
                CommandTools.sendError(owner, "pokemob.action.denyrepel");
                return false;
            }
            EntityPlayer player = (EntityPlayer) owner;
            BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(),
                    location.getBlockState(player.getEntityWorld()), player);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return false;
        }

        count = (int) Math.max(1, Math.ceil(digHole(user, location, true) * Math.pow((100 - level) / 100d, 3)))
                * hungerValue;
        if (count > 0)
        {
            digHole(user, location, false);
            used = true;
            mob.setHungerTime(mob.getHungerTime() + count);
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

        ArrayList<Block> list = new ArrayList<Block>();
        for (Block l : PokecubeMod.core.getConfig().getCaveBlocks())
            list.add(l);
        for (Block l : PokecubeMod.core.getConfig().getSurfaceBlocks())
            list.add(l);
        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                for (int k = -1; k <= 1; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(((Entity) digger).getEntityWorld());
                    Block block = state.getBlock();
                    if (list.contains(block))
                    {
                        boolean drop = true;
                        if (PokecubeMod.core.getConfig().getTerrain().contains(block) && !dropAll && !silky
                                && uselessDrop < Math.random())
                            drop = false;

                        if (!count)
                        {
                            if (!silky) temp.breakBlock(((Entity) digger).getEntityWorld(), drop);
                            else
                            {
                                if (block.canSilkHarvest(player.getEntityWorld(), temp.getPos(), state, player))
                                {
                                    Move_Basic.silkHarvest(state, temp.getPos(), player.getEntityWorld(), player);
                                    temp.breakBlock(((Entity) digger).getEntityWorld(), drop);
                                }
                                else
                                {
                                    temp.breakBlock(((Entity) digger).getEntityWorld(), drop);
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
