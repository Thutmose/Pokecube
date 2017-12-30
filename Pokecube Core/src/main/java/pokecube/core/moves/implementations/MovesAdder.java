package pokecube.core.moves.implementations;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.templates.Move_AOE;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.moves.templates.Move_Doublehit;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.templates.Move_MultiHit;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.moves.templates.Move_Terrain;
import thut.lib.CompatParser.ClassFinder;

public class MovesAdder implements IMoveConstants
{
    public static Map<String, Class<? extends Move_Base>> presetMap = Maps.newHashMap();

    static
    {
        presetMap.put("ongoing", Move_Ongoing.class);
        presetMap.put("explode", Move_Explode.class);
        presetMap.put("terrain", Move_Terrain.class);
        presetMap.put("aoe", Move_AOE.class);
        presetMap.put("multihit", Move_MultiHit.class);
        presetMap.put("doublehit", Move_Doublehit.class);
    }

    public static void postInitMoves()
    {
        for (Move_Base move : MovesUtils.moves.values())
        {
            if (move.move.baseEntry != null && move.move.baseEntry.animations != null
                    && !move.move.baseEntry.animations.isEmpty())
            {
                if (PokecubeMod.debug)
                    PokecubeMod.log(move.move.name + ": animations: " + move.move.baseEntry.animations);
                move.setAnimation(new AnimationMultiAnimations(move.move));
                continue;
            }
            String anim = move.move.animDefault;
            if (anim == null || anim.equals("none")) continue;
            if (PokecubeMod.debug) PokecubeMod.log(move.move.name + ": preset animation: " + move.move.animDefault);
            IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim);
            if (animation != null) move.setAnimation(animation);
        }
    }

    private static void registerMove(Move_Base move_Base)
    {
        MovesUtils.registerMove(move_Base);
    }

    public static void registerMoves()
    {
        registerAutodetect();
        registerRemainder();
        // Reload the moves databases to apply the animations to the newly added
        // moves.
        for (String s : Database.configDatabases.get(EnumDatabase.MOVES.ordinal()))
        {
            try
            {
                File moves = new File(Database.CONFIGLOC + s);
                File anims = new File(Database.CONFIGLOC + "animations.json");
                JsonMoves.merge(anims, moves);
            }
            catch (Exception e1)
            {
                PokecubeMod.log(Level.SEVERE, "Error with " + Database.CONFIGLOC + s, e1);
            }
        }
    }

    // Finds all Move_Basics inside this package and registers them.
    static void registerAutodetect()
    {
        List<Class<?>> foundClasses;
        // Register moves.
        if (PokecubeMod.debug) PokecubeMod.log("Autodecting Moves...");
        try
        {
            foundClasses = ClassFinder.find(MovesAdder.class.getPackage().getName());
            int num = 0;
            for (Class<?> candidateClass : foundClasses)
            {
                if (Move_Basic.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null)
                {
                    Move_Basic move = (Move_Basic) candidateClass.getConstructor().newInstance();
                    if (MovesUtils.isMoveImplemented(move.name))
                    {
                        PokecubeMod.log(
                                "Error, Double registration of " + move.name + " Replacing old entry with new one.");
                        num--;
                    }
                    num++;
                    registerMove(move);
                }
            }
            if (PokecubeMod.debug) PokecubeMod.log("Registered " + num + " Custom Moves");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // Register Move Actions.
        try
        {
            foundClasses = ClassFinder.find(MovesAdder.class.getPackage().getName());
            for (Class<?> candidateClass : foundClasses)
            {
                if (IMoveAction.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null)
                {
                    IMoveAction move = (IMoveAction) candidateClass.getConstructor().newInstance();
                    MoveEventsHandler.register(move);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Only registers contact and self, as distances moves usually should have
     * some effect. */
    public static void registerRemainder()
    {
        int num = 0;
        for (MoveEntry e : MoveEntry.values())
        {
            if (!MovesUtils.isMoveImplemented(e.name))
            {

                boolean doesSomething = false;

                doesSomething |= e.baseEntry.preset != null;
                doesSomething |= e.change != 0;
                doesSomething |= e.power != -2;
                doesSomething |= e.statusChange != 0;
                doesSomething |= e.selfDamage != 0;
                doesSomething |= e.selfHealRatio != 0;
                doesSomething |= e.baseEntry.extraInfo != -1;
                if (!doesSomething) for (int i = 0; i < e.attackedStatModification.length; i++)
                {
                    doesSomething |= e.attackedStatModification[i] != 0;
                    doesSomething |= e.attackerStatModification[i] != 0;
                }

                if (doesSomething)
                {
                    Class<? extends Move_Base> moveClass = e.baseEntry.preset != null
                            ? presetMap.get(e.baseEntry.preset) : Move_Basic.class;
                    if (moveClass == null) moveClass = Move_Basic.class;

                    Move_Base toAdd;
                    try
                    {
                        toAdd = moveClass.getConstructor(String.class).newInstance(e.name);
                        registerMove(toAdd);
                        num++;
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        }
        if (PokecubeMod.debug) PokecubeMod.log("Registered " + num + " Database Moves");
    }
}