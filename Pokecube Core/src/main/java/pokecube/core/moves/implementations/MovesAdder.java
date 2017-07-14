package pokecube.core.moves.implementations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.StatModifiers.DefaultModifiers;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.Move_Transform;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.teleport.Move_Teleport;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.moves.templates.Move_Explode;
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
//        presetMap.put("terrain", Move_Terrain.class);
    }

    public static void postInitMoves()
    {
        for (Move_Base move : MovesUtils.moves.values())
        {
            if (move.getAnimation() == null)
            {
                if (move.move.baseEntry != null && move.move.baseEntry.animations != null
                        && !move.move.baseEntry.animations.isEmpty())
                {
                    move.setAnimation(new AnimationMultiAnimations(move.move));
                    continue;
                }
                String anim = move.move.animDefault;
                if (anim == null) continue;
                IMoveAnimation animation = MoveAnimationHelper.getAnimationPreset(anim);
                if (animation != null) move.setAnimation(animation);
            }
        }
    }

    /** Moves like low kick(weight based), dragon rage (fixed damage), etc */
    static void registerFixedOrCustomDamageMoves()
    {
        registerMove(new Move_Basic(MOVE_SEISMICTOSS)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                return user.getLevel();
            }
        }.setFixedDamage());
        registerMove(new Move_Basic(MOVE_NIGHTSHADE)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                return user.getLevel();
            }
        }.setFixedDamage());

        registerMove(new Move_Basic(MOVE_LOWKICK)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                int pwr = 120;
                if (!(target instanceof IPokemob)) return pwr;
                double mass = ((IPokemob) target).getWeight();
                if (mass < 10) return 20;
                if (mass < 25) return 40;
                if (mass < 50) return 60;
                if (mass < 100) return 80;
                if (mass < 200) return 100;

                return pwr;
            }
        });

        registerMove(new Move_Basic(MOVE_GYROBALL)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                if (!(target instanceof IPokemob)) return 50;
                int targetSpeed = ((IPokemob) target).getStat(Stats.VIT, true);
                int userSpeed = user.getStat(Stats.VIT, true);
                int pwr = 25 * targetSpeed / userSpeed;
                return pwr;
            }
        });

        registerMove(new Move_Basic(MOVE_ELECTROBALL)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                if (!(target instanceof IPokemob)) return 50;
                int targetSpeed = ((IPokemob) target).getStat(Stats.VIT, true);
                int userSpeed = user.getStat(Stats.VIT, true);
                int pwr = 60;
                double var = (double) targetSpeed / (double) userSpeed;

                if (var < 0.25) pwr = 150;
                else if (var < 0.33) pwr = 120;
                else if (var < 0.5) pwr = 80;
                else pwr = 60;

                return pwr;
            }
        });

        registerMove(new Move_Basic(MOVE_GRASSKNOT)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                int pwr = 120;
                if (!(target instanceof IPokemob)) return pwr;
                double mass = ((IPokemob) target).getWeight();
                if (mass < 10) return 20;
                if (mass < 25) return 40;
                if (mass < 50) return 60;
                if (mass < 100) return 80;
                if (mass < 200) return 100;

                return pwr;
            }
        });

    }

    private static void registerMove(Move_Base move_Base)
    {
        MovesUtils.registerMove(move_Base);
    }

    public static void registerMoves()
    {
        // HM like moves
        registerMove(new Move_Teleport(MOVE_TELEPORT));

        // Ongoing moves
        registerOngoingMoves();
        registerStandardPsychicMoves();
        registerFixedOrCustomDamageMoves();
        registerTerrainMoves();
        registerAutodetect();
        MovesUtils.registerMove(new Move_Transform(IMoveNames.MOVE_TRANSFORM));
        registerRemainder();

        MoveEntry.protectionMoves.add(MOVE_PROTECT);
        MoveEntry.protectionMoves.add(MOVE_DETECT);
        MoveEntry.protectionMoves.add(MOVE_ENDURE);
        MoveEntry.oneHitKos.add(MOVE_FISSURE);
        MoveEntry.oneHitKos.add(MOVE_HORNDRILL);
        MoveEntry.oneHitKos.add(MOVE_GUILLOTINE);
        MoveEntry.oneHitKos.add(MOVE_SHEERCOLD);
    }

    // Finds all Move_Basics inside this package and registers them.
    static void registerAutodetect()
    {
        List<Class<?>> foundClasses;
        // Register moves.
        try
        {
            foundClasses = ClassFinder.find(MovesAdder.class.getPackage().getName());
            for (Class<?> candidateClass : foundClasses)
            {
                if (Move_Basic.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null)
                {
                    Move_Basic move = (Move_Basic) candidateClass.getConstructor().newInstance();
                    if (MovesUtils.isMoveImplemented(move.name))
                    {
                        PokecubeMod.log("Error, Double registration of " + move.name);
                    }
                    registerMove(move);
                }
            }
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

    /** Moves that continue to affect the pokemob for a few turns later */
    static void registerOngoingMoves()
    {
        registerMove(new Move_Ongoing(MOVE_FIRESPIN)
        {
            @Override
            public void doOngoingEffect(EntityLiving mob)
            {
                if (((IPokemob) mob).isType(ghost)) return;
                super.doOngoingEffect(mob);
            }
        });
    }

    /** Only registers contact and self, as distances moves usually should have
     * some effect. */
    public static void registerRemainder()
    {
        for (MoveEntry e : MoveEntry.values())
        {
            if (!MovesUtils.isMoveImplemented(e.name))
            {

                boolean doesSomething = false;

                doesSomething |= e.change != 0;
                doesSomething |= e.power != -2;
                doesSomething |= e.statusChange != 0;
                doesSomething |= e.selfDamage != 0;
                doesSomething |= e.selfHealRatio != 0;
                for (int i = 0; i < e.attackedStatModification.length; i++)
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
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    static void registerStandardPsychicMoves()
    {
        registerMove(new Move_Basic(MOVE_PSYWAVE)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                int lvl = user.getLevel();
                int pwr = (int) Math.max(1, lvl * (Math.random() + 0.5));

                return pwr;
            }
        });
        registerMove(new Move_Basic(MOVE_STOREDPOWER)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                int pwr = 20;
                DefaultModifiers mods = user.getModifiers().getDefaultMods();
                for (Stats stat : Stats.values())
                {
                    float b = mods.getModifierRaw(stat);
                    if (b > 0)
                    {
                        pwr += 20 * b;
                    }
                }
                return pwr;
            }
        });
    }

    /** Any move that affects the terrain or weather. */
    static void registerTerrainMoves()
    {
        registerMove(new Move_Terrain(MOVE_SANDSTORM, PokemobTerrainEffects.EFFECT_WEATHER_SAND));
        registerMove(new Move_Terrain(MOVE_RAINDANCE, PokemobTerrainEffects.EFFECT_WEATHER_RAIN));
        registerMove(new Move_Terrain(MOVE_SUNNYDAY, PokemobTerrainEffects.EFFECT_WEATHER_SUN));
        registerMove(new Move_Terrain(MOVE_HAIL, PokemobTerrainEffects.EFFECT_WEATHER_HAIL));
        registerMove(new Move_Terrain(MOVE_MUDSPORT, PokemobTerrainEffects.EFFECT_SPORT_MUD));
        registerMove(new Move_Terrain(MOVE_WATERSPORT, PokemobTerrainEffects.EFFECT_SPORT_WATER));

        registerMove(new Move_Terrain(MOVE_SPIKES, PokemobTerrainEffects.EFFECT_SPIKES));
        registerMove(new Move_Terrain(MOVE_TOXICSPIKES, PokemobTerrainEffects.EFFECT_POISON));
        registerMove(new Move_Terrain(MOVE_STEALTHROCK, PokemobTerrainEffects.EFFECT_ROCKS));
        registerMove(new Move_Terrain(MOVE_STICKYWEB, PokemobTerrainEffects.EFFECT_WEBS));
    }
}