package pokecube.core.moves;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.database.MoveEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
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
import pokecube.core.moves.templates.Move_Utility;
import pokecube.core.utils.PokeType;
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
        registerMove(new Move_Teleport(MOVE_TELEPORT));
        registerMove(new Move_Utility(MOVE_CUT));
        registerMove(new Move_Utility(MOVE_FLASH));
        registerMove(new Move_Utility(MOVE_DIG));
        registerMove(new Move_Utility(MOVE_ROCKSMASH));

        // Ongoing moves
        registerOngoingMoves();

        registerDrainMoves();

        registerRecoilMoves();

        registerSelfStatReducingMoves();

        registerSpecialMoves();
        registerStatMoves();
        registerStatusMoves();

        registerMultiHitMoves();

        registerStandardBugMoves();
        registerStandardDarkMoves();
        registerStandardDragonMoves();
        registerStandardElectricMoves();
        registerStandardFairyMoves();
        registerStandardFightingMoves();
        registerStandardFireMoves();
        registerStandardFlyingMoves();
        registerStandardGhostMoves();
        registerStandardGrassMoves();
        registerStandardGroundMoves();
        registerStandardIceMoves();
        registerStandardNormalMoves();
        registerStandardPoisonMoves();
        registerStandardPsychicMoves();
        registerStandardRockMoves();
        registerStandardSteelMoves();
        registerStandardWaterMoves();

        registerFixedOrCustomDamageMoves();
        registerTerrainMoves();
        registerRemainder();

        MoveEntry.protectionMoves.add(MOVE_PROTECT);
        MoveEntry.protectionMoves.add(MOVE_DETECT);
        MoveEntry.protectionMoves.add(MOVE_ENDURE);

        MoveEntry.oneHitKos.add(MOVE_FISSURE);
        MoveEntry.oneHitKos.add(MOVE_HORNDRILL);
        MoveEntry.oneHitKos.add(MOVE_GUILLOTINE);
        MoveEntry.oneHitKos.add(MOVE_SHEERCOLD);

        MovesUtils.registerMove(new Move_Transform(IMoveNames.MOVE_TRANSFORM));
    }

    /** Moves that hit multiple times */
    static void registerMultiHitMoves()
    {
        // 2 hits
        registerMove(new Move_Basic(MOVE_BONEMERANG)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                super.finalAttack(attacker, attacked, f);
                super.finalAttack(attacker, attacked, f);
            };
        });// setAnimtion(new ThrowParticle("rock")));
        registerMove(new Move_Basic(MOVE_DOUBLEHIT)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                super.finalAttack(attacker, attacked, f);
                super.finalAttack(attacker, attacked, f);
            };
        });
        registerMove(new Move_Basic(MOVE_DOUBLEKICK)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                super.finalAttack(attacker, attacked, f);
                super.finalAttack(attacker, attacked, f);
            };
        });
        registerMove(new Move_Basic(MOVE_DUALCHOP)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                super.finalAttack(attacker, attacked, f);
                super.finalAttack(attacker, attacked, f);
            };
        });
        registerMove(new Move_Basic(MOVE_GEARGRIND)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                super.finalAttack(attacker, attacked, f);
                super.finalAttack(attacker, attacked, f);
            };
        });
        registerMove(new Move_Basic(MOVE_TWINEEDLE)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                super.finalAttack(attacker, attacked, f);
                super.finalAttack(attacker, attacked, f);
            };
        });
        // 2-5 hits
        registerMove(new Move_Basic(MOVE_WATERSHURIKEN)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }

                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f);
            };
        });// setAnimtion(new ThrowParticle("iceshard")));
        registerMove(new Move_Basic(MOVE_FURYATTACK)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                MovesUtils.displayMoveMessages(attacker, attacked, name);
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);

            };
        });
        registerMove(new Move_Basic(MOVE_FURYSWIPES)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        registerMove(new Move_Basic(MOVE_ARMTHRUST)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        registerMove(new Move_Basic(MOVE_BARRAGE)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        registerMove(new Move_Basic(MOVE_BONERUSH)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        registerMove(new Move_Basic(MOVE_BULLETSEED)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });// setAnimtion(new ThrowParticle("sting")));
        registerMove(new Move_Basic(MOVE_COMETPUNCH)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        registerMove(new Move_Basic(MOVE_DOUBLESLAP)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        registerMove(new Move_Basic(MOVE_ICICLESPEAR)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });// setAnimtion(new ThrowParticle("iceshard")));
        registerMove(new Move_Basic(MOVE_PINMISSILE)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });// setAnimtion(new ThrowParticle("sting")));
        registerMove(new Move_Basic(MOVE_ROCKBLAST)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });// setAnimtion(new ThrowParticle("rock")));
        registerMove(new Move_Basic(MOVE_SPIKECANNON)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });// setAnimtion(new ThrowParticle("sting")));
        registerMove(new Move_Basic(MOVE_TAILSLAP)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int count = 2;
                int random = (new Random()).nextInt(6);
                switch (random)
                {
                case 1:
                    count = 2;
                case 2:
                    count = 3;
                case 3:
                    count = 3;
                case 4:
                    count = 4;
                case 5:
                    count = 5;
                default:
                    count = 2;
                }
                for (int i = 0; i <= count; i++)
                    super.finalAttack(attacker, attacked, f, false);
            };
        });
        // 3 hits
        registerMove(new Move_Basic(MOVE_TRIPLEKICK)
        {

            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int PWR = this.getPWR();
                for (int i = 0; i < 3; i++)
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                    {
                        statusChange = move.statusChange;
                    }
                    if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                    {
                        changeAddition = move.change;
                    }
                    int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type, PWR,
                            move.crit, statusChange, changeAddition));
                    if (finalAttackStrength != 0) PWR += 10;
                    else break;
                }
            };
        });
    }

    /** Moves that continue to affect the pokemob for a few turns later */
    static void registerOngoingMoves()
    {
        registerMove(new Move_Ongoing(MOVE_LEECHSEED)
        {
            @Override
            public void doOngoingEffect(EntityLiving mob)
            {
                if (mob.getAttackTarget() instanceof EntityLivingBase)
                {
                    EntityLivingBase target = mob.getAttackTarget();
                    // TODO make this scale with toxic.
                    float thisMaxHP = mob.getMaxHealth();
                    int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                    mob.attackEntityFrom(DamageSource.generic, damage);
                    target.setHealth(Math.min(target.getHealth() + damage, target.getMaxHealth()));
                }
            }

            @Override
            public int getDuration()
            {
                return -1;
            }

        });

        registerMove(new Move_Ongoing(MOVE_YAWN)
        {
            @Override
            public void doOngoingEffect(EntityLiving mob)
            {
                Move_Ongoing move = null;
                for (Move_Ongoing m : ((IPokemob) mob).getOngoingEffects().keySet())
                {
                    if (m.name.equals(name))
                    {
                        move = m;
                        break;
                    }
                }

                int duration = ((IPokemob) mob).getOngoingEffects().get(move);

                if (duration == 0)
                {
                    MovesUtils.setStatus(mob, STATUS_SLP);
                }
            }

            @Override
            public int getDuration()
            {
                return 2;
            }

        });

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

        registerMove(new Move_Ongoing(MOVE_INFESTATION)
        {
            @Override
            public void doOngoingEffect(EntityLiving mob)
            {
                float thisMaxHP = mob.getMaxHealth();
                int damage = Math.max(1, (int) (0.125 * thisMaxHP));
                mob.attackEntityFrom(DamageSource.generic, damage);
            }
        });
    }

    static void registerRecoilMoves()
    {

        registerMove(new Move_Basic(MOVE_TAKEDOWN)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, float
            // f,
            // int finalAttackStrength) {
            // if (finalAttackStrength > 0){
            // int damage = finalAttackStrength / 4 ;
            // ((EntityLivingBase)
            // attacker).attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)
            // attacked), damage);
            // }
            // }
        }.setSound("game.neutral.hurt.fall.big"));

        registerMove(new Move_Basic(MOVE_DOUBLEEDGE)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, float
            // f,
            // int finalAttackStrength) {
            // if (finalAttackStrength > 0){
            // int damage = finalAttackStrength / 3 ;
            // ((EntityLivingBase)
            // attacker).attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)
            // attacked), damage);
            // }
            // }
        }.setSound("game.neutral.hurt.fall.big"));

        registerMove(new Move_Basic(MOVE_FLAREBLITZ)
        {
            // @Override
            // public void postAttack(IPokemob attacker, Entity attacked, float
            // f,
            // int finalAttackStrength) {
            // if (finalAttackStrength > 0){
            // int damage = finalAttackStrength / 3 ;
            // ((EntityLivingBase)
            // attacker).attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)
            // attacked), damage);
            // }
            // }
        }.setSound("game.neutral.hurt.fall.big"));// setAnimtion(new
                                                  // ParticlesOnTarget("flame")));
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
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                System.out.println("Test " + finalAttackStrength);
                if (finalAttackStrength > 0)
                {
                    MovesUtils.handleStats2(attacker, attacked, SPATACK, HARSH);
                }
            }
        });// );//setAnimtion(new ParticleFlow("leavesBig")));

        registerMove(new Move_Basic(MOVE_OVERHEAT)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (finalAttackStrength > 0) MovesUtils.handleStats2(attacker, attacked, SPATACK, HARSH);
            }
        });// );//setAnimtion(new ParticleFlow("flame")));

        registerMove(new Move_Basic(MOVE_DRACOMETEOR)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (finalAttackStrength > 0) MovesUtils.handleStats2(attacker, attacked, SPATACK, HARSH);
            }
        });// );//setAnimtion(new ParticlesOnTarget("largeexplode")));

        registerMove(new Move_Basic(MOVE_PSYCHOBOOST)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (finalAttackStrength > 0) MovesUtils.handleStats2(attacker, attacked, SPATACK, HARSH);
            }
        }.setNotInterceptable());// setAnimtion(new
                                 // ParticlesOnTarget("aurora")));
    }

    /** things like Acupressure, Power Split, Power Trick, etc */
    static void registerSpecialMoves()
    {
        registerMove(new Move_Basic(MOVE_JUDGMENT)
        {
            /** Type getter
             * 
             * @return the type of this move */
            @Override
            public PokeType getType(IPokemob user)
            {
                if(user==null)
                return move.type;
                else return user.getType1();
            }
        });
        
        registerMove(new Move_Basic(MOVE_ACUPRESSURE)
        {
            /** Do anything special for self attacks, usually raising/lowering
             * of stats.
             * 
             * @param mob */
            @Override
            public void doSelfAttack(IPokemob mob, float f)
            {
                if (mob.getMoveStats().SELFRAISECOUNTER == 0)
                {
                    if (sound != null)
                    {
                        ((Entity) mob).worldObj.playSoundAtEntity((Entity) mob, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    MovesUtils.attack(
                            new MovePacket(mob, (Entity) mob, name, move.type, getPWR(), move.crit, (byte) 0, (byte) 0),
                            false);
                    postAttack(mob, (Entity) mob, f, 0);
                }
            }

            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                Random r = new Random(attacked.worldObj.rand.nextLong());

                int rand = r.nextInt(7);
                attacker.getMoveStats().SELFRAISECOUNTER = 80;
                for (int i = 0; i < 8; i++)
                {
                    int stat = (rand);
                    if (MovesUtils.handleStats2(attacker, attacked, 1 << stat, SHARP))
                    {
                        return;
                    }
                    rand = (rand + 1) % 7;
                }
                MovesUtils.displayEfficiencyMessages(attacker, attacked, -2, 0);
            }
        });

        registerMove(new Move_Basic(MOVE_POWERSPLIT)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (attacked instanceof IPokemob)
                {
                    IPokemob target = (IPokemob) attacked;
                    int[] targetStats = target.getBaseStats();
                    int[] attackerStats = attacker.getBaseStats();
                    byte[] targetMods = target.getModifiers();
                    byte[] attackerMods = attacker.getModifiers();

                    targetStats[1] = attackerStats[1] = (targetStats[1] + attackerStats[1]) / 2;
                    targetStats[3] = attackerStats[3] = (targetStats[3] + attackerStats[3]) / 2;

                    targetMods[1] = attackerMods[1] = (byte) ((targetMods[1] + attackerMods[1]) / 2);
                    targetMods[3] = attackerMods[3] = (byte) ((targetMods[3] + attackerMods[3]) / 2);

                    target.setStats(targetStats);
                    attacker.setStats(attackerStats);
                    target.setModifiers(targetMods);
                    attacker.setModifiers(attackerMods);
                }
            }
        });

        registerMove(new Move_Basic(MOVE_GUARDSPLIT)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (attacked instanceof IPokemob)
                {
                    IPokemob target = (IPokemob) attacked;

                    int[] targetStats = target.getBaseStats();
                    int[] attackerStats = attacker.getBaseStats();
                    byte[] targetMods = target.getModifiers();
                    byte[] attackerMods = attacker.getModifiers();

                    targetStats[2] = attackerStats[2] = (targetStats[2] + attackerStats[2]) / 2;
                    targetStats[4] = attackerStats[4] = (targetStats[4] + attackerStats[4]) / 2;

                    targetMods[2] = attackerMods[2] = (byte) ((targetMods[2] + attackerMods[2]) / 2);
                    targetMods[4] = attackerMods[4] = (byte) ((targetMods[4] + attackerMods[4]) / 2);

                    target.setStats(targetStats);
                    attacker.setStats(attackerStats);
                    target.setModifiers(targetMods);
                    attacker.setModifiers(attackerMods);
                }
            }
        });

        registerMove(new Move_Basic(MOVE_POWERTRICK)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (attacked instanceof IPokemob)
                {
                    int[] attackerStats = attacker.getBaseStats();
                    byte[] attackerMods = attacker.getModifiers();

                    int def = attackerStats[2];
                    attackerStats[2] = attackerStats[1];
                    attackerStats[1] = def;

                    byte def2 = attackerMods[2];
                    attackerMods[2] = attackerMods[1];
                    attackerMods[1] = def2;

                    attacker.setModifiers(attackerMods);
                    attacker.setStats(attackerStats);
                }
            }
        });

        registerMove(new Move_Basic(MOVE_WHIRLWIND)
        {

            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                // ends the battle
                if (attacked instanceof EntityLiving)
                {
                    ((EntityLiving) attacked).setAttackTarget(null);
                }
                if (attacked instanceof EntityCreature)
                {
                    ((EntityCreature) attacker).setAttackTarget(null);
                }
                if (attacked instanceof IPokemob)
                {
                    ((IPokemob) attacked).setPokemonAIState(IMoveConstants.ANGRY, false);
                    if (((IPokemob) attacked).getPokemonAIState(IMoveConstants.TAMED))
                        ((IPokemob) attacked).returnToPokecube();
                }
                ((EntityCreature) attacker).setAttackTarget(null);
                attacker.setPokemonAIState(IMoveConstants.ANGRY, false);
            }
        }.setSound("mob.bat.loop"));

        registerMove(new Move_Basic(MOVE_REST)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                attacker.healStatus();
                attacker.setStatus(STATUS_SLP);
                attacker.setStatusTimer((short) 20);
            }
        });

        registerMove(new Move_Basic(MOVE_BIDE)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (!attacker.getMoveStats().biding)
                {
                    attacker.getMoveStats().SELFRAISECOUNTER = 100;
                    attacker.getMoveStats().biding = true;
                    attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
                    attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
                }
                else
                {
                    if (attacker.getMoveStats().SELFRAISECOUNTER == 0)
                    {
                        int damage = attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER
                                + attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER;
                        attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
                        attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
                        attacked.attackEntityFrom(new PokemobDamageSource("mob", (EntityLivingBase) attacker, this),
                                damage);
                        attacker.getMoveStats().biding = false;
                    }
                }
            }
        });

        registerMove(new Move_Basic(MOVE_MIRRORCOAT)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (!attacker.getMoveStats().biding)
                {
                    attacker.getMoveStats().SELFRAISECOUNTER = 30;
                    attacker.getMoveStats().biding = true;
                    attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
                }
                else
                {
                    if (attacker.getMoveStats().SELFRAISECOUNTER == 0)
                    {
                        int damage = 2 * attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER;
                        attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
                        if (attacked != null)
                            attacked.attackEntityFrom(new PokemobDamageSource("mob", (EntityLivingBase) attacker, this),
                                    damage);
                        attacker.getMoveStats().biding = false;
                    }
                }
            }
        });

        registerMove(new Move_Basic(MOVE_COUNTER)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (!attacker.getMoveStats().biding)
                {
                    attacker.getMoveStats().SELFRAISECOUNTER = 30;
                    attacker.getMoveStats().biding = true;
                    attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
                }
                else
                {
                    if (attacker.getMoveStats().SELFRAISECOUNTER == 0)
                    {
                        int damage = 2 * attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER;
                        attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
                        attacked.attackEntityFrom(new PokemobDamageSource("mob", (EntityLivingBase) attacker, this),
                                damage);
                        attacker.getMoveStats().biding = false;
                    }
                }
            }
        });

        registerMove(new Move_Basic(MOVE_FOCUSENERGY)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                attacker.getMoveStats().SPECIALTYPE = IPokemob.TYPE_CRIT;
            }
        });

        registerMove(new Move_Basic(MOVE_CURSE)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (attacker.isType(PokeType.ghost))
                {
                    if (attacked instanceof IPokemob)
                    {
                        IPokemob target = (IPokemob) attacked;
                        if ((target.getChanges() & CHANGE_CURSE) == 0)
                        {
                            MovePacket move = new MovePacket(attacker, attacked, MOVE_CURSE, ghost, 0, 0, (byte) 0,
                                    CHANGE_CURSE, true);
                            target.onMoveUse(move);
                            if (!move.canceled)
                            {
                                target.addChange(CHANGE_CURSE);
                                MovesUtils.displayMoveMessages(attacker, attacked, MOVE_CURSE);
                                ((EntityLivingBase) attacker).attackEntityFrom(DamageSource.magic,
                                        ((EntityLivingBase) attacker).getMaxHealth() / 2);
                            }
                        }
                    }
                }
                else if (attacked != attacker && attacked != null)
                {
                    MovePacket packet = new MovePacket(attacker, attacked, MovesUtils.getMoveFromName(MOVE_CURSE));
                    MovesUtils.handleStats(attacker, attacked, packet, true);
                }
            }
        });

        registerMove(new Move_Basic(MOVE_PRESENT)
        {
            @Override
            public int getPWR(IPokemob user, Entity target)
            {
                double rand = new Random().nextDouble();
                if (rand < 0.4) { return 40; }
                if (rand < 0.7) { return 80; }
                if (rand < 0.8) { return 120; }
                return 0;
            }

            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                if (f == 0 && attacked instanceof EntityLivingBase)
                {
                    EntityLivingBase toHeal = (EntityLivingBase) attacked;
                    float health = Math.min(toHeal.getHealth() + 80, toHeal.getMaxHealth());
                    toHeal.setHealth(health);
                }
            }
        });

        registerMove(new Move_Basic(MOVE_ATTRACT)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f, boolean message)
            {
                if (doAttack(attacker, attacked, f))
                {
                    if (message) MovesUtils.displayMoveMessages(attacker, attacked, name);
                    Vector3 v = Vector3.getNewVector();
                    Entity temp = attacked;
                    if (attacked == null) attacked = temp;
                    notifyClient((Entity) attacker, v.set(attacked), attacked);
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    MovePacket packet = new MovePacket(attacker, attacked, name, move.type, getPWR(attacker, attacked),
                            move.crit, statusChange, changeAddition);
                    packet.infatuate[0] = true;
                    int finalAttackStrength = MovesUtils.attack(packet);
                    postAttack(attacker, attacked, f, finalAttackStrength);
                }

            }
        });
    }

    static void registerStandardBugMoves()
    {
        registerMove(new Move_Basic(MOVE_BUGBITE));

        registerMove(new Move_Basic(MOVE_MEGAHORN));

        registerMove(new Move_Basic(MOVE_FURYCUTTER)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (doAttack(attacker, attacked, f))
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                    {
                        statusChange = move.statusChange;
                    }
                    if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                    {
                        changeAddition = move.change;
                    }
                    double rollOut = attacker.getMoveStats().FURYCUTTERCOUNTER;
                    int PWR = (int) Math.max(this.getPWR(), Math.min(160, (rollOut * 2) * this.getPWR()));
                    int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type, PWR,
                            move.crit, statusChange, changeAddition));

                    postAttack(attacker, attacked, f, finalAttackStrength);
                    if (finalAttackStrength == 0)
                    {
                        attacker.getMoveStats().FURYCUTTERCOUNTER = 0;
                    }
                    else attacker.getMoveStats().FURYCUTTERCOUNTER++;
                }
            }

        });

    }

    static void registerStandardDarkMoves()
    {
        registerMove(new Move_Basic(MOVE_BITE));
        registerMove(new Move_Basic(MOVE_NIGHTSLASH));
        registerMove(new Move_Basic(MOVE_PURSUIT));// should attack even if the
                                                   // mob has been called back.
    }

    static void registerStandardDragonMoves()
    {
        registerMove(new Move_Basic(MOVE_TWISTER).setSound("mob.bat.loop").setMultiTarget());

    }

    static void registerStandardElectricMoves()
    {
        registerMove(new Move_Basic(MOVE_DISCHARGE));// setAnimtion(new
                                                     // ParticleFlow("spark")));

        registerMove(new Move_Basic(MOVE_ZAPCANNON));

        registerMove(new Move_Basic(MOVE_THUNDERSHOCK).setSound("ambient.weather.thunder").setAnimation(new Thunder()));

        registerMove(new Move_Basic(MOVE_THUNDERBOLT).setSound("ambient.weather.thunder").setAnimation(new Thunder()));

        registerMove(new Move_Basic(MOVE_THUNDER).setSound("ambient.weather.thunder").setAnimation(new Thunder()));

        registerMove(new Move_Basic(MOVE_SPARK));// setAnimtion(new
                                                 // ParticlesOnTarget("spark")));

        registerMove(new Move_Basic(MOVE_ELECTROWEB));// setAnimtion(new
                                                      // ParticleFlow("yellow")));

        registerMove(new Move_Basic(MOVE_SHOCKWAVE)
        {// TODO better animation
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (doAttack(attacker, attacked, f))
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);

                    List<EntityLivingBase> hit = MovesUtils.targetsHit(((Entity) attacker), v, 2, 8);
                    for (Entity e : hit)
                    {
                        if ((!PokecubeMod.pokemobsDamagePlayers) && e instanceof EntityPlayer) continue;
                        if ((!PokecubeMod.pokemobsDamageOwner) && e == attacker.getPokemonOwner()) continue;
                        attacked = e;
                        byte statusChange = STATUS_NON;
                        byte changeAddition = CHANGE_NONE;
                        if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                        {
                            statusChange = move.statusChange;
                        }
                        if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                        {
                            changeAddition = move.change;
                        }
                        int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type,
                                getPWR(), move.crit, statusChange, changeAddition));
                        postAttack(attacker, attacked, f, finalAttackStrength);

                    }
                }
            }

        }.setSound("ambient.weather.thunder"));
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
            public void clientAnimation(MovePacketInfo info, IWorldAccess world, float partialTick)
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
            // IWorldAccess world) {
            // attacked.worldObj.spawnEntityInWorld(new
            // EntityWhip(attacked.worldObj, (EntityPokemob) attacker, 1));
            // attacked.worldObj.spawnEntityInWorld(new
            // EntityWhip(attacked.worldObj, (EntityPokemob) attacker, 2));
            // }
        }.setSound("random.bow"));

        registerMove(new Move_Basic(MOVE_RAZORLEAF).setMultiTarget());
    }

    static void registerStandardGroundMoves()
    {
        registerMove(new Move_Basic(MOVE_EARTHQUAKE)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (doAttack(attacker, attacked, f))
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);

                    List<EntityLivingBase> hit = MovesUtils.targetsHit(((Entity) attacker), v, 2, 8);
                    for (Entity e : hit)
                    {
                        if (!(e.onGround || e.fallDistance < 0.5)) continue;
                        if (e instanceof IPokemob && ((IPokemob) e).isType(flying)
                                || (e instanceof EntityPlayer && !PokecubeMod.pokemobsDamagePlayers))
                            continue;
                        if ((!PokecubeMod.pokemobsDamageOwner) && e == attacker.getPokemonOwner()) continue;
                        attacked = e;
                        int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type,
                                getPWR(), move.crit, (byte) 0, (byte) 0));
                        postAttack(attacker, attacked, f, finalAttackStrength);

                    }
                }
            }
        }.setSound("ambient.weather.thunder"));

        registerMove(new Move_Basic(MOVE_MAGNITUDE)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                int PWR = 0;
                int rand = (new Random()).nextInt(20);
                if (rand == 0) PWR = 10;
                else if (rand <= 2) PWR = 30;
                else if (rand <= 6) PWR = 50;
                else if (rand <= 12) PWR = 70;
                else if (rand <= 16) PWR = 90;
                else if (rand <= 18) PWR = 110;
                else PWR = 150;

                if (doAttack(attacker, attacked, f))
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);

                    List<EntityLivingBase> hit = MovesUtils.targetsHit(((Entity) attacker), v, 2, 8);
                    for (Entity e : hit)
                    {
                        if (!(e.onGround || e.fallDistance < 0.5)) continue;
                        if (e instanceof IPokemob && ((IPokemob) e).isType(flying)
                                || (e instanceof EntityPlayer && !PokecubeMod.pokemobsDamagePlayers))
                            continue;
                        if ((!PokecubeMod.pokemobsDamageOwner) && e == attacker.getPokemonOwner()) continue;
                        attacked = e;
                        int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type,
                                PWR, move.crit, (byte) 0, (byte) 0));
                        postAttack(attacker, attacked, f, finalAttackStrength);

                    }
                }
            }
        }.setSound("ambient.weather.thunder"));

        registerMove(new Move_Basic(MOVE_MUDSLAP));// setAnimtion(new
                                                   // ParticleFlow("brown")));
    }

    static void registerStandardIceMoves()
    {
        registerMove(new Move_Basic(MOVE_ICESHARD)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                // entityAttacker.attackTime =
                // MovesUtils.getDelayBetweenAttacks(attacker) * 2 / 3; // will
                // reattack faster
            }
        });// setAnimtion(new ThrowParticle("iceshard")));

        registerMove(new Move_Basic(MOVE_AURORABEAM).setAnimation(new ParticleBeam("aurora")).setMultiTarget());

        registerMove(new Move_Basic(MOVE_ICEBEAM).setMultiTarget());

        registerMove(new Move_Basic(MOVE_ICEBALL)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (doAttack(attacker, attacked, f))
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                    {
                        statusChange = move.statusChange;
                    }
                    if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                    {
                        changeAddition = move.change;
                    }
                    double defCurl = attacker.getMoveStats().DEFENSECURLCOUNTER > 0 ? 2 : 1;
                    double rollOut = attacker.getMoveStats().ROLLOUTCOUNTER;
                    if (rollOut > 4)
                    {
                        rollOut = attacker.getMoveStats().ROLLOUTCOUNTER = 0;
                    }
                    int PWR = (int) Math.min(this.getPWR(), (rollOut * 1.5) * this.getPWR() * defCurl);
                    int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type, PWR,
                            move.crit, statusChange, changeAddition));
                    postAttack(attacker, attacked, f, finalAttackStrength);
                    attacker.getMoveStats().ROLLOUTCOUNTER++;
                }
            }

        });

    }

    static void registerStandardNormalMoves()
    {
        registerMove(new Move_Basic(MOVE_TACKLE));
        registerMove(new Move_Basic(MOVE_POUND));
        registerMove(new Move_Basic(MOVE_SCRATCH));
        registerMove(new Move_Basic(MOVE_SLASH));
        registerMove(new Move_Basic(MOVE_SLAM));
        registerMove(new Move_Basic(MOVE_BODYSLAM));
        registerMove(new Move_Basic(MOVE_STOMP));
        registerMove(new Move_Basic(MOVE_HYPERFANG));
        registerMove(new Move_Basic(MOVE_MEGAPUNCH));
        registerMove(new Move_Basic(MOVE_HEADBUTT));
        registerMove(new Move_Explode(MOVE_SELFDESTRUCT));
        registerMove(new Move_Explode(MOVE_EXPLOSION));

        registerMove(new Move_Basic(MOVE_QUICKATTACK)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                // entityAttacker.attackTime =
                // MovesUtils.getDelayBetweenAttacks(attacker) * 2 / 3; // will
                // reattack faster
            }
        });

        registerMove(new Move_Basic(MOVE_SUPERFANG)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                // the actual PWR is 0
                // the attack causes half the remaining life of the target
                // damage

                if (attacked == null) return;

                int damage = (int) (((EntityLivingBase) attacked).getHealth() / 2);
                if (damage <= 0) damage = 1;
                attacked.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) attacker), damage);
            }
        });

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

    static void registerStandardRockMoves()
    {
        registerMove(new Move_Basic(MOVE_ROCKTHROW));// setAnimtion(new
                                                     // ThrowParticle("rock")));

        registerMove(new Move_Basic(MOVE_ROLLOUT)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (doAttack(attacker, attacked, f))
                {
                    if (sound != null)
                    {
                        ((Entity) attacker).worldObj.playSoundAtEntity((Entity) attacker, sound, 0.5F,
                                0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                    }
                    Vector3 v = Vector3.getNewVector().set(attacked);
                    notifyClient((Entity) attacker, v, attacked);
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                    {
                        statusChange = move.statusChange;
                    }
                    if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                    {
                        changeAddition = move.change;
                    }
                    double defCurl = attacker.getMoveStats().DEFENSECURLCOUNTER > 0 ? 2 : 1;
                    double rollOut = attacker.getMoveStats().ROLLOUTCOUNTER;
                    if (rollOut > 4)
                    {
                        rollOut = attacker.getMoveStats().ROLLOUTCOUNTER = 0;
                    }
                    int PWR = (int) Math.max(getPWR(), (rollOut * 1.5) * this.getPWR() * defCurl);
                    int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type, PWR,
                            move.crit, statusChange, changeAddition));

                    postAttack(attacker, attacked, f, finalAttackStrength);
                    if (finalAttackStrength == 0)
                    {
                        attacker.getMoveStats().ROLLOUTCOUNTER = 0;
                    }
                    else attacker.getMoveStats().ROLLOUTCOUNTER++;
                }
            }

        });

        registerMove(new Move_Basic(MOVE_ANCIENTPOWER));// setAnimtion(new
                                                        // ParticleFlow("rock")));
    }

    static void registerStandardSteelMoves()
    {

    }

    static void registerStandardWaterMoves()
    {
        registerMove(new Move_Basic(MOVE_WATERGUN));// setAnimtion(new
                                                    // ParticleFlow("splash")));

        registerMove(new Move_Basic(MOVE_BUBBLE));// setAnimtion(new
                                                  // ParticleFlow("airbubble")));

        registerMove(new Move_Basic(MOVE_BUBBLEBEAM).setMultiTarget());

        registerMove(new Move_Basic(MOVE_HYDROPUMP)
        {
            @Override
            public boolean doAttack(IPokemob attacker, Entity attacked, float f)
            {
                if (!super.doAttack(attacker, attacked, f)) return false;
                attacked.worldObj.setBlockState(
                        new BlockPos((int) attacked.posX, (int) attacked.posY + 1, (int) attacked.posZ),
                        Blocks.flowing_water.getStateFromMeta(1), 3);
                return true;
            }
            // @Override TODO make an IMoveAnimation for this.
            // public void clientAnimation(Entity attacker, Vector3 target,
            // Entity attacked, IWorldAccess world)
            // {
            // double x = attacker.posX;
            // double z = attacker.posZ;
            // double y = attacker.posY - 0.5;
            // int i = 0;
            //
            // while (Math.abs(attacked.posX - x) > Math.abs(attacked.posX -
            // attacker.posX) / 10)
            // {
            // i++;
            // x = x + ((attacked.posX - attacker.posX) / 1000);
            // y = y + ((attacked.posY - attacker.posY) / 1000);
            // z = z + ((attacked.posZ - attacker.posZ) / 1000);
            // spawnParticle("splash", x + 0.5, y , z + 0.5, 0.0D, 0.0D, 0.0D);
            // spawnParticle("splash", x - 0.5, y , z - 0.5, 0.0D, 0.0D, 0.0D);
            // }
            // }
        }.setMultiTarget());

        registerMove(new Move_Basic(MOVE_SPLASH)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                super.postAttack(attacker, attacked, f, finalAttackStrength);
                if (attacked instanceof IPokemob)
                {
                    String doesntAffect = StatCollector.translateToLocalFormatted("pokemob.move.doesnt.affect",
                            ((IPokemob) attacked).getPokemonDisplayName());
                    attacker.displayMessageToOwner("\u00a7a" + doesntAffect);
                    ((IPokemob) attacked).displayMessageToOwner("\u00a7c" + doesntAffect);
                }
            }
        });// setAnimtion(new ParticlesOnSource("splash")));
    }

    /** Moves that reduce or raise stats, but nothing else */
    static void registerStatMoves()
    {
        registerMove(new Move_Basic(MOVE_DEFENSECURL)
        {
            @Override
            public void postAttack(IPokemob attacker, Entity attacked, float f, int finalAttackStrength)
            {
                IPokemob e = attacker;
                e.getMoveStats().DEFENSECURLCOUNTER = 200;
            }
        });
        registerMove(new Move_Basic(MOVE_LEER));
        registerMove(new Move_Basic(MOVE_TAILWHIP));

        registerMove(new Move_Basic(MOVE_GROWL)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                sound = attacker.getSound();
                super.finalAttack(attacker, attacked, f);
            }
        });
        registerMove(new Move_Basic(MOVE_SANDATTACK).setMultiTarget());
        registerMove(new Move_Basic(MOVE_STRINGSHOT));// setAnimtion(new
                                                      // ParticleFlow("string")));
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

        registerMove(new Move_Basic(MOVE_WILLOWISP));// setAnimtion(new
                                                     // ParticleFlow("flameblue")));
        registerMove(new Move_Basic(MOVE_THUNDERWAVE).setAnimation(new Thunder()).setSound("ambient.weather.thunder"));
        registerMove(new Move_Basic(MOVE_SING)
        {
            @Override
            protected void finalAttack(IPokemob attacker, Entity attacked, float f)
            {
                sound = attacker.getSound();
                super.finalAttack(attacker, attacked, f);
            }
        });// setAnimtion(new ParticlesOnSource("note")));
        registerMove(new Move_Basic(MOVE_GRASSWHISTLE));// setAnimtion(new
                                                        // ParticlesOnSource("note")));
        registerMove(new Move_Basic(MOVE_SUPERSONIC));// setAnimtion(new
                                                      // ParticleFlow("note")));

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
