package pokecube.core.moves.implementations;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.database.abilities.AbilityManager.ClassFinder;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.Move_Transform;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.AnimationPowder;
import pokecube.core.moves.animations.MoveAnimationBase;
import pokecube.core.moves.animations.ParticleBeam;
import pokecube.core.moves.animations.ParticleFlow;
import pokecube.core.moves.animations.ParticlesOnSource;
import pokecube.core.moves.animations.ParticlesOnTarget;
import pokecube.core.moves.animations.ThrowParticle;
import pokecube.core.moves.animations.Thunder;
import pokecube.core.moves.teleport.Move_Teleport;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.moves.templates.Move_Terrain;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class MovesAdder implements IMoveConstants
{

    public static void postInitMoves()
    {
        for (Move_Base move : MovesUtils.moves.values())
        {
            if (move.getAnimation() == null)
            {
                String anim = move.move.animDefault;
                if (anim == null) continue;
                if (anim.contains("beam"))
                {
                    move.setAnimation(new ParticleBeam(anim));
                }
                if (anim.contains("flow"))
                {
                    move.setAnimation(new ParticleFlow(anim));
                }
                if (anim.contains("pont"))
                {
                    move.setAnimation(new ParticlesOnTarget(anim));
                }
                if (anim.contains("pons"))
                {
                    move.setAnimation(new ParticlesOnSource(anim));
                }
                if (anim.contains("powder"))
                {
                    move.setAnimation(new AnimationPowder(anim));
                }
                if (anim.contains("throw"))
                {
                    move.setAnimation(new ThrowParticle(anim));
                }
                else
                {
                    if ((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0)
                    {
                        anim += "d:0.1";
                        move.setAnimation(new ParticlesOnSource(anim));
                    }
                }
            }
        }
    }

    static void registerDrainMoves()
    {
        registerMove(new Move_Basic(MOVE_ABSORB));
        registerMove(new Move_Basic(MOVE_MEGADRAIN));
        registerMove(new Move_Basic(MOVE_GIGADRAIN));
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
        registerMove(new Move_Basic(MOVE_SONICBOOM)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                return 20;
            }
        });// setAnimtion(new ParticleFlow("note")));
        registerMove(new Move_Basic(MOVE_DRAGONRAGE)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                return 40;
            }
        });// setAnimtion(new ParticleFlow("flame")));

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
                int targetSpeed = Tools.getStats(((IPokemob) target))[5];
                int userSpeed = Tools.getStats(user)[5];
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
                int targetSpeed = Tools.getStats(((IPokemob) target))[5];
                int userSpeed = Tools.getStats(user)[5];
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
        // Moves Cflame13 added

        // End of Moves Cflame13 added

        // HM like moves
        // Register world actions for some moves.

        registerMove(new Move_Teleport(MOVE_TELEPORT));

        // Ongoing moves
        registerOngoingMoves();

        registerDrainMoves();

        registerRecoilMoves();

        registerSelfStatReducingMoves();

        registerStatMoves();
        registerStatusMoves();

        registerStandardDragonMoves();
        registerStandardElectricMoves();
        registerStandardFairyMoves();
        registerStandardFightingMoves();
        registerStandardFireMoves();
        registerStandardFlyingMoves();
        registerStandardGhostMoves();
        registerStandardGrassMoves();
        registerStandardIceMoves();
        registerStandardNormalMoves();
        registerStandardPoisonMoves();
        registerStandardPsychicMoves();
        registerStandardSteelMoves();
        registerStandardWaterMoves();

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
                        PokecubeCore.log("Error, Double registration of " + move.name);
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

        registerMove(new Move_Ongoing(MOVE_WRAP));

        registerMove(new Move_Ongoing(MOVE_WHIRLPOOL));

        registerMove(new Move_Ongoing(MOVE_MAGMASTORM));

        registerMove(new Move_Ongoing(MOVE_BIND));

        registerMove(new Move_Ongoing(MOVE_CLAMP));

        registerMove(new Move_Ongoing(MOVE_SANDTOMB));
    }

    static void registerRecoilMoves()
    {

        registerMove(new Move_Basic(MOVE_TAKEDOWN).setSound("game.neutral.hurt.fall.big"));

        registerMove(new Move_Basic(MOVE_DOUBLEEDGE).setSound("game.neutral.hurt.fall.big"));
        registerMove(new Move_Basic(MOVE_FLAREBLITZ).setSound("game.neutral.hurt.fall.big"));
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
                    Move_Basic toAdd = new Move_Basic(e.name);
                    registerMove(toAdd);
                }
            }
        }
    }

    /** Moves that reduce the user's stats after dealing damage note: they use a
     * different method for the stats effects, as they are 100% chance of
     * occuring IF the attack did damage. */
    static void registerSelfStatReducingMoves()
    {
        registerMove(new Move_Basic(MOVE_LEAFSTORM)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, int
            // pwr, int finalAttackStrength)
            // {
            // System.out.println("Test " + finalAttackStrength);
            // if (finalAttackStrength > 0)
            // {
            // MovesUtils.handleStats2(attacker, attacked, SPATACK, HARSH);
            // }
            // }//TODO
        });// );//setAnimtion(new ParticleFlow("leavesBig")));

        registerMove(new Move_Basic(MOVE_OVERHEAT)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, int
            // pwr, int finalAttackStrength)
            // {
            // if (finalAttackStrength > 0) MovesUtils.handleStats2(attacker,
            // attacked, SPATACK, HARSH);
            // }//TODO
        });// );//setAnimtion(new ParticleFlow("flame")));

        registerMove(new Move_Basic(MOVE_DRACOMETEOR)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, int
            // pwr, int finalAttackStrength)
            // {
            // if (finalAttackStrength > 0) MovesUtils.handleStats2(attacker,
            // attacked, SPATACK, HARSH);
            // }//TODO
        });// );//setAnimtion(new ParticlesOnTarget("largeexplode")));

        registerMove(new Move_Basic(MOVE_PSYCHOBOOST)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, int
            // pwr, int finalAttackStrength)
            // {
            // if (finalAttackStrength > 0) MovesUtils.handleStats2(attacker,
            // attacked, SPATACK, HARSH);
            // }//TODO
        }.setNotInterceptable());// setAnimtion(new
                                 // ParticlesOnTarget("aurora")));
    }

    static void registerStandardDragonMoves()
    {
        registerMove(new Move_Basic(MOVE_TWISTER).setSound("mob.bat.loop").setMultiTarget());

    }

    static void registerStandardElectricMoves()
    {
        registerMove(new Move_Basic(MOVE_THUNDERSHOCK).setSound("ambient.weather.thunder").setAnimation(new Thunder()));
        registerMove(new Move_Basic(MOVE_THUNDERBOLT).setSound("ambient.weather.thunder").setAnimation(new Thunder()));
        registerMove(new Move_Basic(MOVE_THUNDER).setSound("ambient.weather.thunder").setAnimation(new Thunder()));
    }

    static void registerStandardFairyMoves()
    {
        registerMove(new Move_Basic(MOVE_DAZZLINGGLEAM));// setAnimtion(new
                                                         // ParticleBeam("white")).setMultiTarget());
    }

    static void registerStandardFightingMoves()
    {

    }

    static void registerStandardFireMoves()
    {

        registerMove(new Move_Basic(MOVE_EMBER));// setAnimtion(new
                                                 // ParticleFlow("flame")));
        registerMove(new Move_Basic(MOVE_FLAMETHROWER).setMultiTarget());
        registerMove(new Move_Basic(MOVE_FIREBLAST).setAnimation(new MoveAnimationBase()
        {

            @Override
            public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
            {
                Vector3 source = info.source;
                Vector3 target = info.target;
                ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
                double dist = source.distanceTo(target);
                Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);

                GlStateManager.translate(temp.x, temp.y, temp.z);
                double factor = (info.currentTick + partialTick) / (double) getDuration();
                factor = Math.min(1, factor);
                temp.set(temp.normalize());
                temp.scalarMultBy(-dist * factor);
                Vector3 temp2 = temp.copy();

                initColour(info.currentTick * 300, partialTick, info.move);

                renderPart(temp, temp2, factor);

                temp.set(source).subtractFrom(target).add(0, 0.5, 0);
                temp2.set(temp);

                renderPart(temp, temp2, factor);

                temp.set(source).subtractFrom(target).add(0.5, 0.5, 0.5);
                temp2.set(temp);

                renderPart(temp, temp2, factor);

                temp.set(source).subtractFrom(target).add(-0.5, 0.5, -0.5);
                temp2.set(temp);

                renderPart(temp, temp2, factor);
            }

            @Override
            public void initColour(long time, float partialTicks, Move_Base move)
            {
                rgba = getColourFromMove(move, 255);

            }

            private void renderPart(Vector3 temp, Vector3 temp2, double factor)
            {
                PTezzelator tez = PTezzelator.instance;

                GL11.glPushMatrix();
                int alpha = ((rgba >> 24) / 1 & 255);
                int red = ((rgba >> 16) / 1 & 255);
                int green = ((rgba >> 8) / 1 & 255);
                int blue = (rgba / 1 & 255);

                long hash = (long) (temp.x * 1000000l + temp.z * 1000000000000l);
                Random rand = new Random(hash);
                tez.begin(6);
                GL11.glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
                for (int i = 0; i < 500; i++)
                {
                    temp.set(rand.nextGaussian() * factor, rand.nextGaussian() * factor, rand.nextGaussian() * factor);
                    temp.scalarMult(0.001);
                    temp.addTo(temp2);
                    double size = 0.01;

                    tez.vertex(temp.x, temp.y + size, temp.z);
                    tez.vertex(temp.x - size, temp.y - size, temp.z - size);
                    tez.vertex(temp.x - size, temp.y + size, temp.z - size);
                    tez.vertex(temp.x, temp.y - size, temp.z);
                }
                tez.end();

                GL11.glPopMatrix();
            }
        }).setSound("mob.wither.shoot").setMultiTarget());

    }

    static void registerStandardFlyingMoves()
    {
        registerMove(new Move_Basic(MOVE_PECK));
        registerMove(new Move_Basic(MOVE_DRILLPECK));

        registerMove(new Move_Basic(MOVE_WINGATTACK).setSound("mob.enderdragon.wings"));

        registerMove(new Move_Basic(MOVE_AERIALACE).setSound("mob.enderdragon.wings"));

        registerMove(new Move_Basic(MOVE_GUST).setSound("mob.bat.loop"));
    }

    static void registerStandardGhostMoves()
    {

        registerMove(new Move_Basic(MOVE_LICK).setSound("mob.silverfish.step"));
    }

    static void registerStandardGrassMoves()
    {

        registerMove(new Move_Basic(MOVE_VINEWHIP)
        {
            // @Override TODO make an IMoveAnimation for this.
            // @SideOnly(Side.CLIENT)
            // public void clientAnimation(Entity attacker, Vector3 target,
            // Entity attacked,
            // IWorldEventListener world) {
            // attacked.worldObj.spawnEntityInWorld(new
            // EntityWhip(attacked.worldObj, (EntityPokemob) attacker, 1));
            // attacked.worldObj.spawnEntityInWorld(new
            // EntityWhip(attacked.worldObj, (EntityPokemob) attacker, 2));
            // }
        }.setSound("random.bow"));

        registerMove(new Move_Basic(MOVE_RAZORLEAF).setMultiTarget());
    }

    static void registerStandardIceMoves()
    {
        registerMove(new Move_Basic(MOVE_AURORABEAM).setAnimation(new ParticleBeam("aurora")).setMultiTarget());

        registerMove(new Move_Basic(MOVE_ICEBEAM).setMultiTarget());
    }

    static void registerStandardNormalMoves()
    {
        registerMove(new Move_Explode(MOVE_SELFDESTRUCT));
        registerMove(new Move_Explode(MOVE_EXPLOSION));
    }

    static void registerStandardPoisonMoves()
    {

        registerMove(new Move_Basic(MOVE_ACID));// setAnimtion(new
                                                // ParticleFlow("poison")));

        registerMove(new Move_Basic(MOVE_SLUDGE));// setAnimtion(new
                                                  // ParticleFlow("poison")));

        registerMove(new Move_Basic(MOVE_SLUDGEBOMB));// setAnimtion(new
                                                      // ParticleFlow("poison")));

        registerMove(new Move_Basic(MOVE_SMOG));// setAnimtion(new
                                                // ParticleFlow("largesmoke")));

        registerMove(new Move_Basic(MOVE_POISONJAB));// setAnimtion(new
                                                     // ParticlesOnTarget("poison")));

        registerMove(new Move_Basic(MOVE_POISONSTING));// setAnimtion(new
                                                       // ThrowParticle("sting")));
    }

    static void registerStandardPsychicMoves()
    {
        registerMove(new Move_Basic(MOVE_CONFUSION).setSound("mob.guardian.curse").setNotInterceptable());

        registerMove(new Move_Basic(MOVE_PSYBEAM).setSound("mob.guardian.curse"));

        registerMove(new Move_Basic(MOVE_PSYWAVE)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                int lvl = user.getLevel();
                int pwr = (int) Math.max(1, lvl * (Math.random() + 0.5));

                return pwr;
            }
        }.setSound("mob.guardian.curse"));

        registerMove(new Move_Basic(MOVE_STOREDPOWER)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                int pwr = 20;
                byte[] mods = user.getModifiers();
                for (byte b : mods)
                {
                    if (b > 0)
                    {
                        pwr += 20 * b;
                    }
                }
                return pwr;
            }
        }.setSound("mob.guardian.curse"));

        registerMove(new Move_Basic(MOVE_PSYCHIC).setSound("mob.guardian.curse").setNotInterceptable());

        registerMove(new Move_Basic(MOVE_PSYCHOCUT));

        registerMove(new Move_Basic(MOVE_ZENHEADBUTT));
    }

    static void registerStandardSteelMoves()
    {

    }

    static void registerStandardWaterMoves()
    {
        registerMove(new Move_Basic(MOVE_BUBBLEBEAM).setMultiTarget());
    }

    /** Moves that reduce or raise stats, but nothing else */
    static void registerStatMoves()
    {
        registerMove(new Move_Basic(MOVE_SANDATTACK).setMultiTarget());
        registerMove(new Move_Basic(MOVE_DOUBLETEAM)); // TODO make this make
                                                       // multiple fake images

        registerMove(new Move_Basic(MOVE_SMOKESCREEN).setMultiTarget());
        registerMove(new Move_Basic(MOVE_SWEETSCENT).setMultiTarget());

    }

    /** Moves that cause status problems, but nothing else */
    static void registerStatusMoves()
    {
        registerMove(new Move_Basic(MOVE_POISONPOWDER).setMultiTarget());
        registerMove(new Move_Basic(MOVE_SLEEPPOWDER).setMultiTarget());
        registerMove(new Move_Basic(MOVE_STUNSPORE).setMultiTarget());
        registerMove(new Move_Basic(MOVE_SPORE).setMultiTarget());
        registerMove(new Move_Basic(MOVE_THUNDERWAVE).setAnimation(new Thunder()).setSound("ambient.weather.thunder"));
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
        registerMove(new Move_Terrain(MOVE_STICKYWEB, PokemobTerrainEffects.EFFECT_WEBS)); // Slows
                                                                                           // entrants
    }
}