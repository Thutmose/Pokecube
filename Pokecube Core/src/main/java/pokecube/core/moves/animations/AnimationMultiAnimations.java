package pokecube.core.moves.animations;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.IWorldEventListener;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.database.moves.json.JsonMoves.AnimationJson;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.animations.presets.Thunder;

public class AnimationMultiAnimations extends MoveAnimationBase
{
    public static boolean isThunderAnimation(IMoveAnimation input)
    {
        if (input == null) return false;
        if (!(input instanceof AnimationMultiAnimations)) return input instanceof Thunder;
        AnimationMultiAnimations anim = (AnimationMultiAnimations) input;
        for (WrappedAnimation a : anim.components)
            if (a.wrapped instanceof Thunder) return true;
        return false;
    }

    public static class WrappedAnimation
    {
        IMoveAnimation wrapped;
        int            start;
    }

    List<WrappedAnimation> components      = Lists.newArrayList();

    private int            applicationTick = 0;

    public AnimationMultiAnimations(MoveEntry move)
    {
        List<AnimationJson> animations = move.baseEntry.animations;
        duration = 0;
        if (animations == null || animations.isEmpty()) return;
        for (AnimationJson anim : animations)
        {
            IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim.preset);
            if (animation == null) continue;
            int start = Integer.parseInt(anim.starttick);
            int dur = Integer.parseInt(anim.duration);
            if (anim.applyAfter)
            {
                applicationTick = Math.max(start + dur, applicationTick);
            }
            duration = Math.max(duration, start + dur);
            WrappedAnimation wrapped = new WrappedAnimation();
            wrapped.wrapped = animation;
            wrapped.start = start;
            components.add(wrapped);
        }
        if (applicationTick == 0) applicationTick = duration;
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
        int tick = info.currentTick;
        for (int i = 0; i < components.size(); i++)
        {
            info.currentTick = tick;
            WrappedAnimation toRun = components.get(i);
            if (tick > toRun.start + toRun.wrapped.getDuration()) continue;
            if (toRun.start > tick) continue;
            info.currentTick = tick - toRun.start;
            toRun.wrapped.clientAnimation(info, world, partialTick);
        }
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
    }

    @Override
    public int getApplicationTick()
    {
        return applicationTick;
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
