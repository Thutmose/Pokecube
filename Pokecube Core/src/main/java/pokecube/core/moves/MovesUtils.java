package pokecube.core.moves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.moves.MoveEntry.Category;
import pokecube.core.events.MoveUse;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.StatModifiers;
import pokecube.core.interfaces.IPokemob.StatModifiers.DefaultModifiers;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import pokecube.core.utils.PokeType;
import thut.api.boom.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment;

public class MovesUtils implements IMoveConstants
{
    public static Random                     rand          = new Random();

    public static HashMap<String, Move_Base> moves         = Maps.newHashMap();
    public static Set<String>                rechargeMoves = Sets.newHashSet();

    static
    {
        rechargeMoves.add("hyperbeam");
        rechargeMoves.add("gigaimpact");
        rechargeMoves.add("solarbeam");
        rechargeMoves.add("rockwrecker");
        rechargeMoves.add("blastburn");
        rechargeMoves.add("frenzyplant");
        rechargeMoves.add("hydrocannon");
        rechargeMoves.add("roaroftime");
    }

    public static void addChange(Entity attacked, byte change)
    {
        if (attacked instanceof IPokemob)
        {
            ((IPokemob) attacked).addChange(change);
        }
        else if (attacked instanceof EntityLivingBase)
        {
            int duration = 250;

            if (change == IMoveConstants.CHANGE_CONFUSED)
            {
                ((EntityLivingBase) attacked)
                        .addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("nausea"), duration));
            }

        }
    }

    /** For contact moves like tackle. The mob gets close to its target and
     * hits.
     *
     * @return whether the mob must attack */
    public static boolean contactAttack(IPokemob attacker, Entity attacked)
    {
        if (attacked == null || attacker == null) return false;
        return true;
    }

    public static void displayEfficiencyMessages(IPokemob attacker, Entity attacked, float efficiency,
            float criticalRatio)
    {
        ITextComponent text;
        if (efficiency == -1)
        {
            if (attacked instanceof IPokemob)
            {
                text = new TextComponentTranslation("pokemob.move.missed.theirs",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = new TextComponentTranslation("pokemob.move.missed.ours",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
                return;
            }
            else if (attacked == null)
            {
                if (((EntityLiving) attacker).getAttackTarget() != null)
                {
                    attacked = ((EntityLiving) attacker).getAttackTarget();
                    String name = attacked.getName();
                    text = new TextComponentTranslation("pokemob.move.missed.ours", name);
                    attacker.displayMessageToOwner(text);
                }
            }
        }
        if (efficiency == -2)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.failed";
                text = new TextComponentTranslation(message + ".theirs",
                        ((IPokemob) attacker).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = new TextComponentTranslation(message + ".ours",
                        ((IPokemob) attacker).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
                return;
            }
        }
        if (efficiency == 0)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.doesnt.affect";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
                attacked.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, 1, 1);
                return;
            }
        }
        else if (efficiency < 1)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.not.very.effective";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
                attacked.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, 1, 1);
            }
        }
        else if (efficiency > 1)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.super.effective";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
                attacked.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
            }
        }

        if (criticalRatio > 1)
        {
            if (attacked instanceof IPokemob)
            {
                text = CommandTools.makeTranslatedMessage("pokemob.move.critical.hit", "green",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage("pokemob.move.critical.hit", "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
                attacked.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            }
        }
    }

    public static void displayMoveMessages(IPokemob attacker, Entity attacked, String attack)
    {
        ITextComponent text;

        if (attack.equals("pokemob.status.confusion"))
        {
            if (attacked instanceof IPokemob)
            {
                text = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
            return;
        }
        String attackName = getUnlocalizedMove(attack);
        text = CommandTools.makeTranslatedMessage("pokemob.move.used", "green",
                attacker.getPokemonDisplayName().getFormattedText(), attackName);
        attacker.displayMessageToOwner(text);
        if (attacker == attacked) return;

        if (attacked instanceof IPokemob)
        {
            text = CommandTools.makeTranslatedMessage("pokemob.move.enemyUsed", "red",
                    attacker.getPokemonDisplayName().getFormattedText(), attackName);
            ((IPokemob) attacked).displayMessageToOwner(text);
        }
        else if (attacked instanceof EntityPlayer && !attacked.getEntityWorld().isRemote)
        {
            text = CommandTools.makeTranslatedMessage("pokemob.move.enemyUsed", "red",
                    attacker.getPokemonDisplayName().getFormattedText(), attackName);
            PacketPokemobMessage.sendMessage((EntityPlayer) attacked, attacked.getEntityId(), text);
        }
    }

    public static void displayStatsMessage(IPokemob attacker, Entity attacked, float efficiency, byte stat, byte amount)
    {
        ITextComponent text;
        if (efficiency == -2)
        {
            if (attacked instanceof IPokemob)
            {
                if ((attacker == null || attacker == attacked) && attacked instanceof EntityLiving)
                {
                    if (((EntityLiving) attacked).getAttackTarget() instanceof IPokemob)
                    {
                        attacker = (IPokemob) ((EntityLiving) attacked).getAttackTarget();
                    }
                }
                String message = "pokemob.move.stat.fail";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
        }
        else
        {
            String message = "pokemob.move.stat";
            boolean fell = false;
            if (amount > 0)
            {
                message += ".fall" + (amount);
                fell = true;
            }
            else
            {
                message += ".rise" + (-amount);
            }
            String statName = "pokemob.move.stat" + stat;

            if ((attacker == null || attacker == attacked) && attacked instanceof EntityLiving)
            {
                if (((EntityLiving) attacked).getAttackTarget() instanceof IPokemob)
                {
                    attacker = (IPokemob) ((EntityLiving) attacked).getAttackTarget();
                }
            }
            if (attacked instanceof IPokemob && attacker != null)
            {
                if (attacker == attacked)
                {
                    String colour = fell ? "red" : "green";
                    text = CommandTools.makeTranslatedMessage(message, colour,
                            ((IPokemob) attacked).getPokemonDisplayName().getFormattedText(), statName);
                    ((IPokemob) attacked).displayMessageToOwner(text);
                }
                else
                {
                    String colour = fell ? "red" : "green";
                    text = CommandTools.makeTranslatedMessage(message, colour,
                            ((IPokemob) attacked).getPokemonDisplayName().getFormattedText(), statName);
                    ((IPokemob) attacked).displayMessageToOwner(text);
                    colour = fell ? "green" : "red";
                    text = CommandTools.makeTranslatedMessage(message, colour,
                            ((IPokemob) attacked).getPokemonDisplayName().getFormattedText(), statName);
                    attacker.displayMessageToOwner(text);
                }
            }
            else if (attacker == null && (attacked instanceof IPokemob))
            {
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText(), statName);
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
            else
            {
                String colour = fell ? "green" : "red";
                text = CommandTools.makeTranslatedMessage(message, colour,
                        attacker.getPokemonDisplayName().getFormattedText(), statName);
                attacker.displayMessageToOwner(text);
            }
        }
    }

    public static void displayStatusMessages(IPokemob attacker, Entity attacked, byte status, boolean onMove)
    {
        String message = getStatusMessage(status, onMove);
        ITextComponent text;
        if (message != null)
        {
            if (attacker != null)
            {
                text = CommandTools.makeTranslatedMessage(message, "green",
                        attacker.getPokemonDisplayName().getFormattedText());
                attacker.displayMessageToOwner(text);
            }
            if (attacked instanceof IPokemob)
            {
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName().getFormattedText());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
            else if (attacked instanceof EntityPlayer && attacker != null)
            {
                text = CommandTools.makeTranslatedMessage(message, "red",
                        attacker.getPokemonDisplayName().getFormattedText());
                PacketPokemobMessage.sendMessage((EntityPlayer) attacked, attacked.getEntityId(), text);
            }
        }
    }

    public static void doAttack(String attackName, IPokemob attacker, Entity attacked)
    {
        Move_Base move = moves.get(attackName);
        if (move != null)
        {
            move.attack(attacker, attacked);
        }
        else
        {
            if (attackName != null)
            {
                System.err.println("The Move \"" + attackName + "\" does not exist.");
            }
            doAttack(DEFAULT_MOVE, attacker, attacked);
        }
    }

    public static void doAttack(String attackName, IPokemob attacker, Vector3 attacked)
    {
        Move_Base move = moves.get(attackName);

        if (move != null)
        {
            move.attack(attacker, attacked);
        }
        else
        {
            if (attackName != null)
            {
                System.err.println("The Move \"" + attackName + "\" does not exist.");
            }
            doAttack(DEFAULT_MOVE, attacker, attacked);
        }
    }

    public static float getAttackStrength(IPokemob attacker, IPokemob attacked, Category type, int PWR,
            MovePacket movePacket)
    {
        Move_Base move = movePacket.getMove();
        if (move.fixedDamage) { return move.getPWR(attacker, (Entity) attacked); }

        if (PWR <= 0) return 0;

        float statusMultiplier = 1F;
        if (attacker.getStatus() == STATUS_PAR || attacker.getStatus() == STATUS_BRN) statusMultiplier = 0.5F;

        int level = attacker.getLevel();
        int ATT;
        int DEF;

        if (type == Category.SPECIAL)
        {
            ATT = (int) (attacker.getStat(Stats.SPATTACK, true) * movePacket.statMults[Stats.SPATTACK.ordinal()]);
            DEF = attacker.getStat(Stats.SPDEFENSE, true);
        }
        else
        {
            ATT = (int) (attacker.getStat(Stats.ATTACK, true) * movePacket.statMults[Stats.ATTACK.ordinal()]);
            DEF = attacker.getStat(Stats.DEFENSE, true);
        }

        ATT = (int) (statusMultiplier * ATT);

        return (((level * 0.4F + 2F) * ATT * PWR) / (DEF * 50F) + 2);
    }

    /** Computes the delay between two moves in a fight from move and status
     * effects.
     *
     * @return muliplier on attack delay */
    public static float getDelayMultiplier(IPokemob attacker, String moveName)
    {
        float statusMultiplier = 1F;
        if (attacker.getStatus() == STATUS_PAR) statusMultiplier = 4F;
        if (rechargeMoves.contains(moveName)) statusMultiplier = 4f;
        Move_Base move = getMoveFromName(moveName);
        if (move == null) return 0;
        if (moveName == MOVE_NONE)
        {
            move = getMoveFromName(MOVE_TACKLE);
        }
        float pp = move.getPP();
        float ppFactor = (float) Math.sqrt(pp / 40f);
        return ppFactor * statusMultiplier;
    }

    public static int getAttackDelay(IPokemob attacker, String moveName, boolean distanced, boolean playerTarget)
    {
        int cd = PokecubeMod.core.getConfig().attackCooldown;
        if (playerTarget) cd *= 2;
        double accuracyMod = attacker.getModifiers().getDefaultMods().getModifier(Stats.ACCURACY);
        double moveMod = MovesUtils.getDelayMultiplier(attacker, moveName);
        return (int) (cd * moveMod / accuracyMod);
    }

    public static ITextComponent getMoveName(String attack)
    {
        return new TextComponentTranslation("pokemob.move." + attack);
    }

    public static Move_Base getMoveFromName(String moveName)
    {
        if (moveName == null) return null;
        Move_Base ret = moves.get(moveName);
        return ret;
    }

    protected static String getStatusMessage(byte status, boolean onMove)
    {
        String message = null;
        if (status == STATUS_FRZ)
        {
            message = "pokemob.move.isfrozen";
        }
        if (status == STATUS_SLP)
        {
            message = "pokemob.move.issleeping";
        }
        if (status == STATUS_PAR && onMove)
        {
            message = "pokemob.move.paralyzed";
        }
        else if (status == STATUS_PAR)
        {
            message = "pokemob.move.isfullyparalyzed";
        }
        if (status == STATUS_BRN)
        {
            message = "pokemob.move.isburned";
        }
        if (status == STATUS_PSN)
        {
            message = "pokemob.move.ispoisoned";
        }
        if (status == STATUS_PSN2)
        {
            message = "pokemob.move.isbadlypoisoned";
        }
        return message;
    }

    public static float getTerrainDamageModifier(PokeType type, Entity attacker, TerrainSegment terrain)
    {
        float ret = 1;
        long terrainDuration = 0;
        PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
        if (type == dragon)
        {
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_TERRAIN_MISTY);
            if (terrainDuration > 0)
            {
                ret = 0.5f;
            }
        }
        if (type == electric && (attacker.onGround || attacker.fallDistance < 0.5))
        {
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC);
            if (terrainDuration > 0)
            {
                ret = 1.5f;
            }
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_SPORT_MUD);
            if (terrainDuration > 0)
            {
                ret *= 0.33f;
            }
        }
        if (type == grass && (attacker.onGround || attacker.fallDistance < 0.5))
        {
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_TERRAIN_GRASS);
            if (terrainDuration > 0)
            {
                ret = 1.5f;
            }
        }
        if (type == water)
        {
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_RAIN);
            if (terrainDuration > 0)
            {
                ret = 1.5f;
            }
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_SUN);
            if (terrainDuration > 0)
            {
                ret = 0.5f;
            }

        }
        if (type == fire)
        {
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_SUN);
            if (terrainDuration > 0)
            {
                ret = 1.5f;
            }
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_RAIN);
            if (terrainDuration > 0)
            {
                ret = 0.5f;
            }
            terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_SPORT_WATER);
            if (terrainDuration > 0)
            {
                ret *= 0.33f;
            }
        }
        return ret;
    }

    public static String getUnlocalizedMove(String attack)
    {
        return "pokemob.move." + attack;
    }

    /** Handles stats modifications of the move
     * 
     * @param mob
     *            the pokemob being affected
     * @param atk
     *            the move being used
     * @param attacked
     *            whether the mob is the attacked mob, or the attacker
     * @return */
    public static boolean handleStats(IPokemob mob, Entity target, MovePacket atk, boolean attacked)
    {
        int[] stats = attacked ? atk.attackedStatModification : atk.attackerStatModification;
        if (attacked && !(target instanceof IPokemob)) return false;
        IPokemob affected = (IPokemob) (attacked ? target : mob);
        DefaultModifiers modifiers = affected.getModifiers().getDefaultMods();
        float[] mods = modifiers.values;
        float[] old = mods.clone();
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[1] = (byte) Math.max(-6, Math.min(6, mods[1] + stats[1]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[2] = (byte) Math.max(-6, Math.min(6, mods[2] + stats[2]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[3] = (byte) Math.max(-6, Math.min(6, mods[3] + stats[3]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[4] = (byte) Math.max(-6, Math.min(6, mods[4] + stats[4]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[5] = (byte) Math.max(-6, Math.min(6, mods[5] + stats[5]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[6] = (byte) Math.max(-6, Math.min(6, mods[6] + stats[6]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            mods[7] = (byte) Math.max(-6, Math.min(6, mods[7] + stats[7]));
        boolean ret = false;
        byte[] diff = new byte[old.length];
        for (int i = 0; i < old.length; i++)
        {
            diff[i] = (byte) (old[i] - mods[i]);
            if (old[i] != mods[i])
            {
                ret = true;
            }
        }
        if (ret)
        {
            for (byte i = 0; i < diff.length; i++)
            {
                if (diff[i] != 0)
                {
                    if (!attacked) displayStatsMessage(mob, target, 0, i, diff[i]);
                    else if (target instanceof IPokemob)
                        displayStatsMessage((IPokemob) target, (Entity) mob, 0, i, diff[i]);
                }
            }
            PacketSyncModifier.sendUpdate(StatModifiers.DEFAULTMODIFIERS, affected);
        }
        return ret;
    }

    public static boolean handleStats2(IPokemob mob, Entity attacker, int statEffect, int statEffectAmount)
    {
        DefaultModifiers modifiers = mob.getModifiers().getDefaultMods();
        float[] mods = modifiers.values;
        float[] old = mods.clone();
        mods[1] = (byte) Math.max(-6, Math.min(6, mods[1] + statEffectAmount * (statEffect & 1)));
        mods[2] = (byte) Math.max(-6, Math.min(6, mods[2] + statEffectAmount * (statEffect & 2) / 2));
        mods[3] = (byte) Math.max(-6, Math.min(6, mods[3] + statEffectAmount * (statEffect & 4) / 4));
        mods[4] = (byte) Math.max(-6, Math.min(6, mods[4] + statEffectAmount * (statEffect & 8) / 8));
        mods[5] = (byte) Math.max(-6, Math.min(6, mods[5] + statEffectAmount * (statEffect & 16) / 16));
        mods[6] = (byte) Math.max(-6, Math.min(6, mods[6] + statEffectAmount * (statEffect & 32) / 32));
        mods[7] = (byte) Math.max(-6, Math.min(6, mods[7] + statEffectAmount * (statEffect & 64) / 64));
        boolean ret = false;
        byte[] diff = new byte[old.length];
        for (int i = 0; i < old.length; i++)
        {
            diff[i] = (byte) (old[i] - mods[i]);
            if (old[i] != mods[i])
            {
                ret = true;
            }
        }
        if (ret)
        {
            for (byte i = 0; i < diff.length; i++)
            {
                if (diff[i] != 0 && attacker instanceof IPokemob)
                {
                    displayStatsMessage((IPokemob) attacker, (Entity) mob, 0, i, diff[i]);
                }
            }
            PacketSyncModifier.sendUpdate(StatModifiers.DEFAULTMODIFIERS, mob);
        }
        return ret;
    }

    public static boolean isMoveImplemented(String attackName)
    {
        if (attackName == null) return false;
        Move_Base move = moves.get(attackName);
        if (move == null)
        {
            for (String s : moves.keySet())
            {
                if (s.toLowerCase(java.util.Locale.ENGLISH)
                        .contentEquals(attackName.toLowerCase(java.util.Locale.ENGLISH)))
                {
                    attackName = s;
                    return true;
                }
            }
        }
        if (move != null) { return true; }
        return false;
    }

    /** creats and ExplosionCustom */
    public static ExplosionCustom newExplosion(Entity entity, double par2, double par4, double par6, float par8,
            boolean par9, boolean par10)
    {
        ExplosionCustom var11 = new ExplosionCustom(entity.getEntityWorld(), entity, par2, par4, par6, par8);

        if (entity instanceof IPokemob)
        {
            IPokemob poke = (IPokemob) entity;
            if (poke.getPokemonOwner() instanceof EntityPlayer)
            {
                var11.owner = (EntityPlayer) poke.getPokemonOwner();
            }
            else
            {
                var11.owner = PokecubeMod.getFakePlayer();
            }
        }
        return var11;
    }

    public static void registerMove(Move_Base move_Base)
    {
        moves.put(move_Base.name, move_Base);
    }

    public static void setStatus(Entity attacked, byte status)
    {
        displayStatusMessages(null, attacked, status, true);
        if (attacked instanceof IPokemob)
        {
            boolean apply = ((IPokemob) attacked).setStatus(status);
            if (!apply)
            {
                // TODO message here about no status effect.
            }
        }
        else if (attacked instanceof EntityLivingBase)
        {
            int duration = 20;
            if (status == IMoveConstants.STATUS_BRN)
            {
                attacked.setFire(10);
            }
            if (status == IMoveConstants.STATUS_FRZ)
            {
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("weakness"), duration * 3, 100));
            }
            if (status == IMoveConstants.STATUS_PAR)
            {
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 1));
            }
            if (status == IMoveConstants.STATUS_PSN)
            {
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("poison"), duration, 10));
            }
            if (status == IMoveConstants.STATUS_PSN2)
            {
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("poison"), duration * 2, 10));
            }
            if (status == IMoveConstants.STATUS_SLP)
            {
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
                ((EntityLivingBase) attacked).addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("weakness"), duration * 2, 100));
            }

        }
    }

    public static Entity targetHit(final Entity attacker, Vector3 dest)
    {
        Vector3 source = Vector3.getNewVector().set(attacker, true);
        source.y += attacker.height / 4;
        final boolean ignoreAllies = false;
        Predicate<Entity> matcher = new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity e)
            {
                if (attacker == e.getRidingEntity()) return false;
                if (!PokecubeMod.pokemobsDamagePlayers && e instanceof EntityPlayer) return false;
                if (!PokecubeMod.pokemobsDamageOwner && e == ((IPokemob) attacker).getPokemonOwner()) return false;
                if (PokecubeMod.core.getEntityProvider().getEntity(attacker.getEntityWorld(), e.getEntityId(),
                        true) == attacker)
                    return false;
                return true;
            }
        };
        return targetHit(source, dest.subtract(source), 16, attacker.getEntityWorld(), attacker, ignoreAllies, matcher);
    }

    public static Entity targetHit(Vector3 source, Vector3 dir, int distance, World worldObj, Entity attacker,
            boolean ignoreAllies, Predicate<? super Entity> matcher)
    {
        // Vector3 dest = Vector3.getVector().set(target, true);
        Entity target = null;

        List<Entity> targets = source.allEntityLocationExcluding(distance, 0.5, dir, source, worldObj, attacker);
        double closest = 16;

        if (targets != null) for (Entity e : targets)
        {
            if (!matcher.apply(e)) continue;
            if (attacker.getDistanceToEntity(e) < closest)
            {
                closest = attacker.getDistanceToEntity(e);
                target = e;
            }
        }
        return target;
    }

    public static List<EntityLivingBase> targetsHit(final Entity attacker, Vector3 dest)
    {
        Vector3 source = Vector3.getNewVector().set(attacker, true);

        source.y += attacker.height / 4;
        List<Entity> targets = source.allEntityLocationExcluding(16, 0.5, dest.subtract(source), source,
                attacker.getEntityWorld(), attacker);
        List<EntityLivingBase> ret = new ArrayList<EntityLivingBase>();

        Predicate<Entity> matcher = new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity e)
            {
                if (attacker == e.getRidingEntity()) return false;
                if (!PokecubeMod.pokemobsDamagePlayers && e instanceof EntityPlayer) return false;
                if (!PokecubeMod.pokemobsDamageOwner && e == ((IPokemob) attacker).getPokemonOwner()) return false;
                if (PokecubeMod.core.getEntityProvider().getEntity(attacker.getEntityWorld(), e.getEntityId(),
                        true) == attacker)
                    return false;
                return true;
            }
        };

        if (targets != null) for (Entity e : targets)
        {
            if (e instanceof EntityLivingBase)
            {
                if (!matcher.apply(e)) continue;
                ret.add((EntityLivingBase) e);
            }
        }
        return ret;
    }

    public static List<EntityLivingBase> targetsHit(Entity attacker, Vector3 dest, int range, double area)
    {
        Vector3 source = Vector3.getNewVector().set(attacker);

        List<Entity> targets = source.allEntityLocationExcluding(range, area, dest.subtract(source), source,
                attacker.getEntityWorld(), attacker);
        List<EntityLivingBase> ret = new ArrayList<EntityLivingBase>();
        if (targets != null) for (Entity e : targets)
        {
            if (e instanceof EntityLivingBase)
            {
                ret.add((EntityLivingBase) e);
            }
        }

        return ret;
    }

    public static void useMove(@Nonnull Move_Base move, @Nonnull Entity user, @Nullable Entity target,
            @Nonnull Vector3 start, @Nonnull Vector3 end)
    {
        if (MinecraftForge.EVENT_BUS.post(new MoveUse.ActualMoveUse.Init((IPokemob) user, move, target)))
        {
            // Move Failed message here?
            return;
        }
        EntityMoveUse moveUse = new EntityMoveUse(user.getEntityWorld());
        moveUse.setUser(user).setMove(move).setTarget(target).setStart(start).setEnd(end);
        PokecubeCore.moveQueues.queueMove(moveUse);
    }

    @Deprecated
    public static void useMove(@Nonnull String move, @Nonnull Entity user, @Nullable Entity target,
            @Nonnull Vector3 start, @Nonnull Vector3 end)
    {
        useMove(getMoveFromName(move), user, target, start, end);
    }

    public static boolean canUseMove(IPokemob attacker)
    {
        if (attacker.getMoveStats().nextMoveTick <= ((Entity) attacker).ticksExisted) return true;
        return false;
    }
}
