package pokecube.core.moves.animations;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.IWorldEventListener;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.database.moves.json.JsonMoves.AnimationJson;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.implementations.MovesAdder;

public class AnimationMultiAnimations extends MoveAnimationBase
{
    public static class WrappedAnimation
    {
        IMoveAnimation wrapped;
        int            start;
    }

    List<WrappedAnimation> components = Lists.newArrayList();

    public AnimationMultiAnimations(MoveEntry move)
    {
        List<AnimationJson> animations = move.baseEntry.animations;
        duration = 0;
        if (animations == null || animations.isEmpty()) return;
        for (AnimationJson anim : animations)
        {
            IMoveAnimation animation = MovesAdder.getAnimationPreset(anim.preset);
            if (animation == null) continue;
            int start = Integer.parseInt(anim.starttick);
            int dur = Integer.parseInt(anim.duration);
            duration = Math.max(duration, start + dur);
            WrappedAnimation wrapped = new WrappedAnimation();
            wrapped.wrapped = animation;
            wrapped.start = start;
            components.add(wrapped);
        }
        components.sort(new Comparator<WrappedAnimation>()
        {
            @Override
            public int compare(WrappedAnimation arg0, WrappedAnimation arg1)
            {
                return arg0.start - arg1.start;
            }
        });
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        int tick = info.currentTick;
        for (int i = 0; i < components.size(); i++)
        {
            info.currentTick = tick;
            WrappedAnimation toRun = components.get(i);
            if (tick > toRun.start + toRun.wrapped.getDuration()) continue;
            if (toRun.start > tick) continue;
            info.currentTick = tick - toRun.start;
            toRun.wrapped.spawnClientEntities(info);
        }
    }

}
