package pokecube.core.moves.templates;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.TreeRemover;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class Move_Utility extends Move_Basic
{

    public static ArrayList<String> moves = new ArrayList<String>();

    public static boolean isUtilityMove(String move)
    {
        return moves.contains(move);
    }

    public Move_Utility(String name)
    {
        super(name);
        moves.add(name);
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked)
    {
        if (attacked != null && attacked != attacker)
        {
            super.attack(attacker, attacked);
            return;
        }
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

        boolean silky = shouldSilk(digger) && player != null;
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
                                    silkHarvest(state, temp.getPos(), player.getEntityWorld(), player);
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

    private void doFortuneDrop(Vector3 location, World world, int fortune)
    {
        location.getBlock(world).dropBlockAsItem(world, location.getPos(), location.getBlockState(world), fortune);
        location.breakBlock(world, false);
    }

    @Override
    public void doWorldAction(IPokemob user, Vector3 location)
    {
        if (!(PokecubeMod.pokemobsDamageBlocks || PokecubeMod.debug))
        {
            EntityLivingBase owner;
            if ((owner = user.getPokemonOwner()) != null)
            {
                CommandTools.sendError(owner, "pokemob.action.denydamageblock");
            }
            return;
        }
        if (user.getPokemonAIState(IMoveConstants.ANGRY)) return;
        IHungrymob mob = (IHungrymob) user;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;
        if (this.name == MOVE_FLASH)
        {
            count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
            used = mob.getHungerTime() + count < 0;
            EntityLivingBase owner = user.getPokemonOwner();
            if (used)
            {
                owner.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 5000));
                mob.setHungerTime(mob.getHungerTime() + count);
            }
            return;
        }
        boolean repel = SpawnHandler.checkNoSpawnerInArea(((Entity) user).getEntityWorld(), location.intX(),
                location.intY(), location.intZ());

        EntityLivingBase owner = user.getPokemonOwner();
        if (owner != null && owner instanceof EntityPlayer)
        {
            if (!repel)
            {
                CommandTools.sendError(owner, "pokemob.action.denyrepel");
                return;
            }
            EntityPlayer player = (EntityPlayer) owner;
            BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(),
                    location.getBlockState(player.getEntityWorld()), player);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return;
        }

        if (this.name == MOVE_DIG)
        {
            count = (int) Math.max(1, Math.ceil(digHole(user, location, true) * Math.pow((100 - level) / 100d, 3)))
                    * hungerValue;
            System.out.println(location);
            if (count > 0)
            {
                digHole(user, location, false);
                used = true;
            }
        }
        else if (this.name == MOVE_ROCKSMASH)
        {
            count = (int) Math.max(1, Math.ceil(smashRock(user, location, true) * Math.pow((100 - level) / 100d, 3)))
                    * hungerValue;
            if (count > 0)
            {
                smashRock(user, location, false);
                used = true;
            }
        }
        else if (this.name == MOVE_CUT)
        {
            TreeRemover remover = new TreeRemover(((Entity) user).getEntityWorld(), location);
            int cut = remover.cut(true);

            if (cut == 0)
            {
                int index = new Random().nextInt(6);
                for (int i = 0; i < 6; i++)
                {
                    EnumFacing dir = EnumFacing.VALUES[(i + index) % 6];
                    remover = new TreeRemover(((Entity) user).getEntityWorld(), location.offset(dir));
                    cut = remover.cut(true);
                    if (cut != 0) break;
                }
            }
            count = (int) Math.max(1, Math.ceil(cut * Math.pow((100 - level) / 100d, 3))) * hungerValue;
            if (count > 0)
            {
                remover.cut(false);
                used = true;
            }
            remover.clear();
        }
        if (used)
        {
            mob.setHungerTime(mob.getHungerTime() + count);
        }
    }

    private boolean shouldDropAll(IPokemob pokemob)
    {
        if (pokemob.getAbility() == null) return false;
        Ability ability = pokemob.getAbility();
        return ability.toString().equalsIgnoreCase("arenatrap");
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

        ArrayList<Block> list = new ArrayList<Block>();
        for (Block l : PokecubeMod.core.getConfig().getRocks())
            list.add(l);

        boolean silky = shouldSilk(digger) && player != null;

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
                        if (!count)
                        {
                            if (!silky) doFortuneDrop(temp, ((Entity) digger).getEntityWorld(), fortune);
                            else
                            {
                                if (block.canSilkHarvest(player.getEntityWorld(), temp.getPos(), state, player))
                                {
                                    silkHarvest(state, temp.getPos(), player.getEntityWorld(), player);
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
}
