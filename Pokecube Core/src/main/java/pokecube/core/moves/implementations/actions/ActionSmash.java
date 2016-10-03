package pokecube.core.moves.implementations.actions;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.commands.CommandTools;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionSmash implements IMoveAction
{
    public ActionSmash()
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

        count = (int) Math.max(1, Math.ceil(smashRock(user, location, true) * Math.pow((100 - level) / 100d, 3)))
                * hungerValue;
        if (count > 0)
        {
            smashRock(user, location, false);
            used = true;
            mob.setHungerTime(mob.getHungerTime() + count);
        }
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "rocksmash";
    }

    private int smashRock(IPokemob digger, Vector3 v, boolean count)
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
        int fortune = digger.getLevel() / 30;
        boolean silky = Move_Basic.shouldSilk(digger) && player != null;
        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        int range = 0;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(((Entity) digger).getEntityWorld());
                    if (PokecubeTerrainChecker.isRock(state))
                    {
                        if (!count)
                        {
                            if (!silky) doFortuneDrop(temp, ((Entity) digger).getEntityWorld(), fortune);
                            else
                            {
                                Block block = state.getBlock();
                                if (block.canSilkHarvest(player.getEntityWorld(), temp.getPos(), state, player))
                                {
                                    Move_Basic.silkHarvest(state, temp.getPos(), player.getEntityWorld(), player);
                                    temp.breakBlock(((Entity) digger).getEntityWorld(), false);
                                }
                                else
                                {
                                    doFortuneDrop(temp, ((Entity) digger).getEntityWorld(), fortune);
                                }
                            }
                        }
                        ret++;
                    }
                }
        return ret;
    }

    private void doFortuneDrop(Vector3 location, World world, int fortune)
    {
        location.getBlock(world).dropBlockAsItem(world, location.getPos(), location.getBlockState(world), fortune);
        location.breakBlock(world, false);
    }
}
