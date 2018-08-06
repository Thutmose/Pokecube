package pokecube.core.database.moves;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import pokecube.core.database.Database;
import pokecube.core.database.moves.MoveEntry.Category;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

public class MovesParser
{
    static final Pattern NUMBER = Pattern.compile("([0-9])+");
    static final Pattern HALF   = Pattern.compile("half");
    static final Pattern THIRD  = Pattern.compile("third");

    static final Pattern PSNA   = Pattern.compile("(induce).*(poison)");
    static final Pattern PSNB   = Pattern.compile("(may).*(poison)");
    static final Pattern PSNC   = Pattern.compile("(induce).*(severe).*(poison)");

    static final Pattern PARA   = Pattern.compile("(induce).*(paralysis)");
    static final Pattern PARB   = Pattern.compile("(may).*(paralyze)");

    static final Pattern BRNA   = Pattern.compile("(induce).*(burn)");
    static final Pattern BRNB   = Pattern.compile("(may).*(burn)");

    static final Pattern FRZA   = Pattern.compile("(induce).*(freeze)");
    static final Pattern FRZB   = Pattern.compile("(may).*(freeze)");

    static final Pattern SLPA   = Pattern.compile("(induce).*(sleep)");
    static final Pattern SLPB   = Pattern.compile("(may).*(sleep)");

    @Nullable
    static String getMatch(String input, Pattern pattern)
    {
        Matcher match = pattern.matcher(input);
        if (match.find()) return match.group();
        return null;
    }

    static boolean matches(String input, Pattern... patterns)
    {
        for (Pattern pattern : patterns)
        {
            Matcher match = pattern.matcher(input);
            if (match.find()) return true;
        }
        return false;
    }

    public static void load(MovesJson moves) throws IOException
    {
        for (int i = 0; i < moves.moves.size(); i++)
        {
            MoveJsonEntry entry = moves.moves.get(i);
            try
            {
                initMoveEntry(entry, i + 1);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.SEVERE, "Error in move " + entry.readableName, e);
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
            PokecubeMod.log(Level.WARNING, "Error with " + entry.readableName, e);
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
        addCategory(contact ? IMoveConstants.CATEGORY_CONTACT : IMoveConstants.CATEGORY_DISTANCE, move);
        move.soundType = sound;
        move.isPunch = punch;
        move.snatch = snatch;
        move.magiccoat = magiccoat;
        move.protect = protect;
        move.type = parseType(entry.type);
        if (entry.defaultanimation != null) move.animDefault = entry.defaultanimation;
        if (entry.effectRate == null) entry.effectRate = "100";
        if (entry.secondaryEffect != null)
        {
            parseNoMove(entry.secondaryEffect, move);
            parseCrit(entry.secondaryEffect.toLowerCase(Locale.ENGLISH), move);
        }
        parseCategory(entry.category, move);
        parseTarget(entry, move);
        parseStatusEffects(entry, move);
        parseStatModifiers(entry, move);
        parseFixedDamage(entry, move);
        parseHealing(entry, move);
        parseSelfDamage(entry, move);
        parsePreset(entry);
    }

    static void parseNoMove(String secondaryEffect, MoveEntry move)
    {
        if (secondaryEffect.equals("User cannot Attack on the next turn."))
        {
            move.delayAfter = true;
            if (PokecubeMod.debug) PokecubeMod.log(move.name + " set as long cooldown move.");
        }
    }

    static void parseCategory(String category, MoveEntry move)
    {
        String other = "Other";
        String special = "Special";
        String physical = "Physical";
        move.category = 0;
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
        boolean crit = details != null && (details.contains("has an increased critical hit ratio")
                || details.contains("has high critical hit ratio"));
        boolean alwaysCrit = details != null && details.contains("always inflicts a critical hit");

        if (alwaysCrit)
        {
            move.crit = 255;
            if (PokecubeMod.debug) PokecubeMod.log(move.name + " set to always crit.");
        }
        else if (crit)
        {
            move.crit = 2;
            if (PokecubeMod.debug) PokecubeMod.log(move.name + " set to twice crit rate.");
        }
        else
        {
            move.crit = 1;
        }
    }

    private static void parseFixedDamage(MoveJsonEntry entry, MoveEntry move)
    {
        if (entry.secondaryEffect == null) return;
        if (entry.secondaryEffect.toLowerCase(Locale.ENGLISH).equalsIgnoreCase("may cause one-hit ko."))
            entry.ohko = true;
        String var = entry.secondaryEffect.toLowerCase(Locale.ENGLISH).trim();
        boolean fixed = var.contains("inflicts") && var.contains("hp damage.");
        if (fixed)
        {
            var = getMatch(var, NUMBER);
            move.fixed = true;
            try
            {
                move.power = Integer.parseInt(var);
                if (PokecubeMod.debug) PokecubeMod.log(entry.readableName + " set to fixed damage of " + var);
            }
            catch (NumberFormatException e)
            {
                PokecubeMod.log(Level.WARNING, "Error parsing fixed damage for " + entry.readableName + " "
                        + entry.secondaryEffect + " " + var, e);
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
            Matcher number = NUMBER.matcher(var);
            Matcher third = THIRD.matcher(var);
            Matcher half = HALF.matcher(var);
            float damage = 0;
            if (number.find()) damage = Integer.parseInt(number.group()) / 100f;
            else if (third.find()) damage = 1 / 3f;
            else if (half.find()) damage = 1 / 2f;
            move.selfDamage = damage;
            move.selfDamageType = MoveEntry.DAMAGEDEALT;
            if (PokecubeMod.debug) PokecubeMod.log(move.name + " set to recoil factor of " + damage);
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
            Matcher number = NUMBER.matcher(var);
            Matcher third = THIRD.matcher(var);
            Matcher half = HALF.matcher(var);
            if (number.find()) move.damageHeal = Integer.parseInt(number.group()) / 100f;
            else if (half.find()) move.damageHeal = 0.5f;
            else if (third.find()) move.damageHeal = 1 / 3f;
            if (PokecubeMod.debug) PokecubeMod.log(move.name + " set to damage heal of " + move.damageHeal);
            return;
        }
        else if (healRatio)
        {
            Matcher number = NUMBER.matcher(var);
            Matcher third = THIRD.matcher(var);
            Matcher half = HALF.matcher(var);
            if (number.find()) move.selfHealRatio = Integer.parseInt(number.group()) / 100f;
            else if (half.find()) move.selfHealRatio = 0.5f;
            else if (third.find()) move.selfHealRatio = 1 / 3f;
            if (PokecubeMod.debug) PokecubeMod.log(move.name + " set to self heal of " + move.damageHeal);
            return;
        }
        if (var.contains("user restores health"))
        {
            move.selfHealRatio = 1;
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
                move.attackerStatModProb = rate / 100f;
                amounts = move.attackerStatModification;
            }
            else
            {
                move.attackedStatModProb = rate / 100f;
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
        boolean burn = matches(effect, BRNA, BRNB);
        boolean par = matches(effect, PARA, PARB);
        boolean poison = matches(effect, PSNA, PSNB);
        boolean frz = matches(effect, FRZA, FRZB);
        boolean slp = matches(effect, SLPA, SLPB);
        boolean poison2 = matches(effect, PSNC);
        boolean confuse = effect.contains("confus");
        boolean flinch = effect.contains("flinch");
        if (burn) addStatus(IMoveConstants.STATUS_BRN, move);
        if (par) addStatus(IMoveConstants.STATUS_PAR, move);
        if (frz) addStatus(IMoveConstants.STATUS_FRZ, move);
        if (slp) addStatus(IMoveConstants.STATUS_SLP, move);
        if (poison) addStatus(poison2 ? IMoveConstants.STATUS_PSN2 : IMoveConstants.STATUS_PSN, move);
        if (confuse) addChange(IMoveConstants.CHANGE_CONFUSED, move);
        if (flinch) addChange(IMoveConstants.CHANGE_FLINCH, move);
        int rate = getRate(entry.effectRate);
        if (confuse || flinch) move.chanceChance = rate / 100f;
        move.statusChance = rate / 100f;
        if (slp || burn || par || poison || frz || slp)
        {
            if (PokecubeMod.debug)
                PokecubeMod.log(move.name + " Has Status Effects: " + move.statusChange + " " + move.statusChance);
        }
    }

    private static void addStatus(byte mask, MoveEntry move)
    {
        if ((move.statusChange & mask) == 0) move.statusChange += mask;
    }

    private static void addChange(byte mask, MoveEntry move)
    {
        if ((move.change & mask) == 0) move.change += mask;
    }

    private static void addCategory(byte mask, MoveEntry move)
    {
        if ((move.attackCategory & mask) == 0) move.attackCategory += mask;
    }

    private static int getRate(String chance)
    {
        if (chance == null) chance = "100";
        int rate;
        try
        {
            rate = Integer.parseInt(getMatch(chance, NUMBER));
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
        if (self.equals(target) && (move.attackCategory & IMoveConstants.CATEGORY_SELF) == 0)
        {
            move.attackCategory += IMoveConstants.CATEGORY_SELF;
        }
    }

    private static void parsePreset(MoveJsonEntry entry)
    {
        if (entry.secondaryEffect != null && entry.secondaryEffect.startsWith("Traps")) entry.preset = "ongoing";
    }

}
