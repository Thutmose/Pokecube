package pokecube.core.moves.implementations.actions;

import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class ActionNaturePower implements IMoveAction
{
    /** Implementers of this interface must have a public constructor that takes
     * no arguments. */
    public abstract interface BiomeChanger
    {
        /** This method should check whether it should apply a biome change, and
         * if it should, it should do so, then return true. It should return
         * false if it does not change anything. Only the first of these to
         * return true will be used, so if you need to re-order things, reorder
         * ActionNaturePower.changer_classes accordingly. */
        public boolean apply(BlockPos pos, World world);
    }

    public static final List<Class<? extends BiomeChanger>> changer_classes = Lists.newArrayList();

    public static class ForestChanger implements BiomeChanger
    {
        public ForestChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {/*
          * TODO This should check for grass floor with some trees, if it finds
          * that, set surrounding biome to vanilla forest.
          */
            return false;
        }

    }

    public static class PlainsChanger implements BiomeChanger
    {
        public PlainsChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {/*
          * TODO This should check for grass floor with no trees, if it finds
          * that, set surrounding biome to vanilla plains.
          */
            return false;
        }

    }

    public static class DesertChanger implements BiomeChanger
    {
        public DesertChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {/*
          * TODO This should check for sand floor and some cactus, if found, set
          * to vanilla desert biome.
          */
            return false;
        }

    }

    public static class HillsChanger implements BiomeChanger
    {
        public HillsChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {/*
          * TODO This should check for stone floor, not inside, and above y=100,
          * if it finds that, set surrounding biome to extreme hills.
          */
            return false;
        }

    }

    static
    {
        changer_classes.add(ForestChanger.class);
        changer_classes.add(PlainsChanger.class);
        changer_classes.add(DesertChanger.class);
        changer_classes.add(HillsChanger.class);
    }

    /** This is filled with new instances of whatever is in changer_classes. It
     * will have same ordering as changer_classes, and the first of these to
     * return true for a location is the only one that will be used. */
    private final List<BiomeChanger> changers = Lists.newArrayList();

    public ActionNaturePower()
    {
    }

    @Override
    public boolean applyEffect(IPokemob attacker, Vector3 location)
    {
        if (attacker.getPokemonAIState(IMoveConstants.ANGRY)) return false;
        if (!(attacker.getPokemonOwner() instanceof EntityPlayerMP)) return false;
        if (!MoveEventsHandler.canEffectBlock(attacker, location)) return false;
        long time = attacker.getEntity().getEntityData().getLong("lastAttackTick");
        if (time + (20 * 3) > attacker.getEntity().getEntityWorld().getTotalWorldTime()) return false;
        BlockPos pos = location.getPos();
        World world = attacker.getEntity().getEntityWorld();
        for (BiomeChanger changer : changers)
        {
            if (changer.apply(pos, world)) return true;
        }
        return false;
    }

    @Override
    public String getMoveName()
    {
        return "naturepower";
    }

    @Override
    public void init()
    {
        for (Class<? extends BiomeChanger> clazz : changer_classes)
        {
            try
            {
                changers.add(clazz.newInstance());
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.SEVERE, "error with changer " + clazz, e);
            }
        }
    }
}
