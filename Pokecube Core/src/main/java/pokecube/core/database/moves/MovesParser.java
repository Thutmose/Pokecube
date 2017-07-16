package pokecube.core.database.moves;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import pokecube.core.database.Database;
import pokecube.core.database.moves.MoveEntry.Category;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

public class MovesParser
{
    public static void load(File file) throws IOException
    {
        JsonMoves.loadMoves(file);
        MovesJson moves = JsonMoves.getMoves(file);
        for (int i = 0; i < moves.moves.size(); i++)
        {
            MoveJsonEntry entry = moves.moves.get(i);
            try
            {
                initMoveEntry(entry, i + 1);
            }
            catch (Exception e)
            {
                System.out.println("Error in move " + entry.readableName);
                e.printStackTrace();
                throw new IllegalAccessError();
            }
        }
    }

    public static void initMoveEntry(MoveJsonEntry entry, int index)
    {
        String name = Database.convertMoveName(entry.name);
        int power;
        int pp;
        int accuracy;
        try
        {
            power = Integer.parseInt(entry.pwr);
            pp = Integer.parseInt(entry.pp);
            accuracy = Integer.parseInt(entry.acc);
        }
        catch (NumberFormatException e)
        {
            PokecubeMod.log("Error with " + entry.readableName);
            return;
        }
        String yes = "Yes";
        MoveEntry move = MoveEntry.get(name);
        move = move == null ? new MoveEntry(name, index) : move;
        move.attackCategory = 0;
        move.power = power;
        move.pp = pp;
        move.accuracy = accuracy;
        move.baseEntry = entry;
        boolean contact = yes.equals(entry.contact);
        boolean sound = yes.equals(entry.soundType);
        boolean punch = yes.equals(entry.punchType);
        boolean snatch = yes.equals(entry.snatchable);
        boolean magiccoat = yes.equals(entry.magiccoat);
        boolean defrosts = yes.equals(entry.defrosts);
        boolean protect = yes.equals(entry.protect);
        boolean mirror = yes.equals(entry.mirrormove);
        // boolean wideArea = yes.equals(entry.wideArea);//TODO decide what to
        // do with these.
        // boolean zMove = yes.equals(entry.zMove);
        move.defrosts = defrosts;
        move.mirrorcoated = mirror;
        move.attackCategory += contact ? IMoveConstants.CATEGORY_CONTACT : IMoveConstants.CATEGORY_DISTANCE;
        move.soundType = sound;
        move.isPunch = punch;
        move.snatch = snatch;
        move.magiccoat = magiccoat;
        move.protect = protect;
        move.type = parseType(entry.type);
        if (entry.defaultanimation != null) move.animDefault = entry.defaultanimation;
        if (entry.effectRate == null) entry.effectRate = "100";
        parseCrit(entry.secondaryEffect, move);
        parseCategory(entry.category, move);
        parseTarget(entry, move);
        parseStatusEffects(entry, move);
        parseStatModifiers(entry, move);
        parseFixedDamage(entry, move);
        parseHealing(entry, move);
        parseSelfDamage(entry, move);
        parsePreset(entry);
    }

    static void parseCategory(String category, MoveEntry move)
    {
        String other = "Other";
        String special = "Special";
        String physical = "Physical";
        if (other.equals(category)) move.category = (byte) Category.OTHER.ordinal();
        if (special.equals(category)) move.category = (byte) Category.SPECIAL.ordinal();
        if (physical.equals(category)) move.category = (byte) Category.PHYSICAL.ordinal();
    }

    private static PokeType parseType(String type)
    {
        return PokeType.getType(type);
    }

    private static void parseCrit(String details, MoveEntry move)
    {
        boolean crit = details != null && details.contains("Has an increased Critical Hit ratio");
        if (crit)
        {
            move.crit = 2;
        }
        else
        {
            move.crit = 1;
        }
    }

    private static void parseFixedDamage(MoveJsonEntry entry, MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        if (entry.secondaryEffect.equalsIgnoreCase("May cause one-hit KO.")) entry.ohko = true;
        String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        boolean fixed = var.contains("inflicts") && var.contains("hp damage.");
        if (fixed)
        {
            var = var.replace("inflicts", "").replace("hp damage.", "").trim();
            move.fixed = true;
            try
            {
                move.power = Integer.parseInt(var);
                System.out.println(entry.readableName + " " + var);
            }
            catch (NumberFormatException e)
            {
                PokecubeMod.log("Error parsing fixed damage for " + entry.readableName + " " + entry.secondaryEffect
                        + " " + var);
            }
        }
    }

    private static void parseSelfDamage(MoveJsonEntry entry, MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        boolean recoils = var.contains("user takes recoil damage equal to ")
                && var.contains(" of the damage inflicted.");
        if (recoils)
        {
            var = var.replace("inflicts", "").replace("hp damage.", "");
            var = var.replace("%", "");
            float damage;
            try
            {
                damage = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
                damage = var.contains("third") ? 100 / 3f : 0;
            }
            move.selfDamage = damage;
            move.selfDamageType = MoveEntry.DAMAGEDEALT;
            return;
        }
        boolean userFaint = var.contains("user faints");
        if (userFaint)
        {
            move.selfDamage = 10000;
            move.selfDamageType = MoveEntry.TOTALHP;
            return;
        }
    }

    private static void parseHealing(MoveJsonEntry entry, MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        boolean ratioHeal = var.contains("user recovers") && var.contains(" the damage inflicted.");
        boolean healRatio = var.contains("user recovers") && var.contains(" the maximum hp.");
        if (ratioHeal)
        {
            var = var.replace("user recovers ", "");
            var = var.replace(" the damage inflicted.", "");
            var = var.trim();
            if (var.contains("half")) move.damageHealRatio = 50;
            if (var.contains("75%")) move.damageHealRatio = 75;
            return;
        }
        else if (healRatio)
        {
            var = var.replace("user recovers ", "");
            var = var.replace(" the damage inflicted.", "");
            var = var.trim();
            if (var.contains("half")) move.selfHealRatio = 50;
            if (var.contains("75%")) move.selfHealRatio = 75;
            return;
        }
        if (var.contains("user restores health"))
        {
            move.selfHealRatio = 100;
            return;
        }
        // TODO heal other moves as well.
    }

    private static void parseStatModifiers(MoveJsonEntry entry, MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        String[] effects = entry.secondaryEffect.split("\\.");
        for (String s : effects)
        {
            String effect = s.toLowerCase(Locale.ENGLISH).trim();
            if (s.isEmpty()) continue;
            boolean lower = effect.contains("lower");
            boolean raise = effect.contains("raise") || effect.contains("boost");
            boolean user = effect.contains("user");
            boolean atk = effect.contains("attack");
            boolean spatk = effect.contains("special attack");
            boolean def = effect.contains("defense");
            boolean spdef = effect.contains("special defense");
            boolean speed = effect.contains("speed");
            boolean acc = effect.contains("accuracy");
            boolean evas = effect.contains("evasion");
            int stages = 1;
            if (effect.contains("two stage") || effect.contains("2 stage")) stages = 2;
            if (effect.contains("three stage") || effect.contains("3 stage")) stages = 3;
            if (lower) stages *= -1;
            else if (!raise) stages = 0;
            if (!(raise || lower)) continue;

            if (atk && spatk)
            {
                // check to ensure is both;
                atk = effect.replaceFirst("attack", "").contains("attack");
            }
            if (def && spdef)
            {
                // check to ensure is both;
                def = effect.replaceFirst("defense", "").contains("defense");
            }
            int rate = getRate(entry.effectRate);
            int[] amounts = null;
            if (user)
            {
                move.attackerStatModProb = rate;
                amounts = move.attackerStatModification;
            }
            else
            {
                move.attackedStatModProb = rate;
                amounts = move.attackedStatModification;
            }
            if (atk) amounts[Stats.ATTACK.ordinal()] = stages;
            if (def) amounts[Stats.DEFENSE.ordinal()] = stages;
            if (spatk) amounts[Stats.SPATTACK.ordinal()] = stages;
            if (spdef) amounts[Stats.SPDEFENSE.ordinal()] = stages;
            if (speed) amounts[Stats.VIT.ordinal()] = stages;
            if (acc) amounts[Stats.ACCURACY.ordinal()] = stages;
            if (evas) amounts[Stats.EVASION.ordinal()] = stages;
        }

    }

    private static void parseStatusEffects(MoveJsonEntry entry, MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        String effect = entry.secondaryEffect.toLowerCase(Locale.ENGLISH);
        boolean burn = effect.contains("induce burn") || effect.contains("induces burn") || effect.contains("may burn");
        boolean par = effect.contains("induce paralysis") || effect.contains("induces paralysis")
                || effect.contains("may paralyze");
        boolean poison = effect.contains("induce poison") || effect.contains("induces poison")
                || effect.contains("may poison");
        boolean frz = effect.contains("induce freeze") || effect.contains("induces freeze")
                || effect.contains("may freeze");
        boolean slp = effect.contains("induce sleep") || effect.contains("induces sleep")
                || effect.contains("may sleep");
        boolean poison2 = effect.contains("induce severe poison") || effect.contains("induces severe poison");
        if (burn) move.statusChange += IMoveConstants.STATUS_BRN;
        if (par) move.statusChange += IMoveConstants.STATUS_PAR;
        if (frz) move.statusChange += IMoveConstants.STATUS_FRZ;
        if (slp) move.statusChange += IMoveConstants.STATUS_SLP;
        if (poison) move.statusChange += poison2 ? IMoveConstants.STATUS_PSN2 : IMoveConstants.STATUS_BRN;
        String chance = entry.effectRate.replace(" %", "");
        int rate = getRate(chance);
        move.statusChance = rate / 100f;
    }

    private static int getRate(String chance)
    {
        if (chance == null) chance = "100";
        int rate;
        try
        {
            rate = Integer.parseInt(chance);
            if (rate > 100)
            {
                rate = Integer.parseInt(chance.substring(0, chance.length() / 2));
            }
        }
        catch (NumberFormatException e)
        {
            rate = 100;
        }
        return rate;
    }

    private static void parseTarget(MoveJsonEntry entry, MoveEntry move)
    {
        String target = entry.target;
        String self = "Self";
        if (self.equals(target))
        {
            move.attackCategory += IMoveConstants.CATEGORY_SELF;
        }
    }

    private static void parsePreset(MoveJsonEntry entry)
    {
        if (entry.secondaryEffect != null && entry.secondaryEffect.startsWith("Traps")) entry.preset = "ongoing";
    }

}
