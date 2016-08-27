package pokecube.core.database.moves;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.google.common.collect.Lists;

import pokecube.core.database.Database;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.utils.PokeType;
import thut.core.client.render.tabula.json.JsonFactory;

public class MoveEntryLoader implements IMoveConstants
{

    public static class MoveJsonEntry
    {
        int     num;
        String  name;
        String  type;
        String  category;
        int     pp;
        String  pwr;
        String  acc;
        String  status;
        String  stats;
        String  changes;
        String  heal;
        String  multi;
        boolean protect;
        boolean magiccoat;
        boolean snatch;
        boolean kingsrock;
        int     crit;
        String  selfDamage;
        String  defaultAnimation;
    }

    public static class MovesJson
    {
        List<MoveJsonEntry> moves = Lists.newArrayList();
    }

    public static void loadMoves(String path)
    {
        File file = new File(path);
        try
        {
            MovesJson json = JsonFactory.getGson().fromJson(new FileReader(file), MovesJson.class);
            for (MoveJsonEntry entry : json.moves)
            {
                initMove(entry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void initMove(MoveJsonEntry entry)
    {
        try
        {
            int index = entry.num;
            String name = Database.convertMoveName(entry.name);

            MoveEntry move = new MoveEntry(name, index);

            move.type = PokeType.getType(entry.type);

            String cat = entry.category.trim().toLowerCase(java.util.Locale.ENGLISH);
            if (cat.contains("spec") || cat.contains("status")) move.category = SPECIAL;
            else move.category = PHYSICAL;
            if (cat.contains("distance"))
            {
                move.attackCategory = CATEGORY_DISTANCE;
            }
            if (cat.contains("contact"))
            {
                move.attackCategory = CATEGORY_CONTACT;
            }
            if (cat.equals("self"))
            {
                move.attackCategory = CATEGORY_SELF;
            }
            else if (cat.contains("self"))
            {
                move.attackCategory += CATEGORY_SELF_EFFECT;
            }

            if (move.attackCategory == 0)
            {
                if (move.category == SPECIAL)
                {
                    move.attackCategory = CATEGORY_DISTANCE;
                }
                else
                {
                    move.attackCategory = CATEGORY_CONTACT;
                }
            }

            if (move.category == -1)
            {
                move.category = (move.attackCategory & CATEGORY_DISTANCE) > 0 ? SPECIAL : PHYSICAL;
            }

            move.pp = entry.pp;

            try
            {
                move.power = Integer.parseInt(entry.pwr);
            }
            catch (NumberFormatException e)
            {
                if (entry.pwr.equalsIgnoreCase("level"))
                {
                    move.power = MoveEntry.LEVEL;
                }
                else
                {
                    move.power = MoveEntry.NODAMAGE;
                }
            }

            try
            {
                move.accuracy = (int) Float.parseFloat(entry.acc.replace("%", ""));
            }
            catch (NumberFormatException e)
            {
                move.accuracy = -1;
            }

            String[] statusEffects = entry.status.toLowerCase(java.util.Locale.ENGLISH).split(";");
            int chance = 0;
            byte effect = 0;
            if (statusEffects.length == 2)
            {
                String status = statusEffects[0];
                chance = Integer.parseInt(statusEffects[1]);
                effect = status.equals("par") ? STATUS_PAR
                        : status.equals("brn") ? STATUS_BRN
                                : status.equals("frz") ? STATUS_FRZ
                                        : status.equals("slp") ? STATUS_SLP
                                                : status.equals("psn") ? STATUS_PSN
                                                        : status.equals("psn2") ? STATUS_PSN2 : STATUS_NON;
            }
            else
            {
                // TODO finish this for tri-attack
            }
            move.statusChange = effect;
            move.statusChance = chance;

            int[][] statmods = getModifiers(entry.stats.trim().toLowerCase(java.util.Locale.ENGLISH),
                    (move.attackCategory & CATEGORY_SELF) > 0 || (move.attackCategory & CATEGORY_SELF_EFFECT) > 0);

            move.attackerStatModification = statmods[2];
            move.attackerStatModProb = statmods[3][0];
            move.attackedStatModification = statmods[0];
            move.attackedStatModProb = statmods[1][0];

            String[] changes = entry.changes.split(";");
            if (!changes[0].equals("none"))
            {
                move.change = changes[0].equalsIgnoreCase("flinch") ? CHANGE_FLINCH : CHANGE_CONFUSED;
                move.chanceChance = Integer.parseInt(changes[1].trim());
            }
            changes = entry.heal.split(";");
            move.damageHealRatio = Integer.parseInt(changes[0].trim().replace("%", ""));
            move.selfHealRatio = Integer.parseInt(changes[1].trim().replace("%", ""));

            changes = entry.multi.split(";");
            move.multiTarget = Boolean.parseBoolean(changes[0].trim());
            move.notIntercepable = Boolean.parseBoolean(changes[1].trim());

            move.protect = entry.protect;
            move.magiccoat = entry.magiccoat;
            move.snatch = entry.snatch;
            move.kingsrock = entry.kingsrock;

            move.crit = entry.crit;

            changes = entry.selfDamage.split(";");
            if (changes.length > 1)
            {
                String amt = changes[0].replace("%", "");
                String cond = changes[1];
                move.selfDamage = Float.parseFloat(amt);
                move.selfDamageType = cond.contains("miss") ? MoveEntry.MISS
                        : cond.contains("hp") ? MoveEntry.RELATIVEHP : MoveEntry.DAMAGEDEALT;
            }
            String anim = entry.defaultAnimation;
            move.animDefault = anim;
        }
        catch (Exception e)
        {
            System.err.println(entry.name);
        }
    }

    /** Index 1 = attacked modifiers, index 2 = attacked modifier chance.<br>
     * Index 3 = attacker modifiers, index 4 = attacker modifier chance.<br>
     * 
     * @param input
     * @return */
    private static int[][] getModifiers(String input, boolean selfMove)
    {
        int[][] ret = new int[4][];

        byte effect = 0;
        byte amount = 0;
        int chance = 100;
        int[] amounts = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

        String[] stats = input.split("`");

        String[] statEffects = stats[0].split(";");
        if (statEffects.length == 3)
        {
            String stat = statEffects[0];
            effect = stat.equals("atk") ? ATTACK
                    : stat.equals("def") ? DEFENSE
                            : stat.equals("spd") ? VIT
                                    : stat.equals("spatk") ? SPATACK
                                            : stat.equals("spdef") ? SPDEFENSE
                                                    : stat.equals("acc") ? ACCURACY
                                                            : stat.equals("eva") ? EVASION : STATUS_NON;
            if (stat.equalsIgnoreCase("all"))
            {
                effect = ATTACK + DEFENSE + VIT + SPATACK + SPDEFENSE + ACCURACY + EVASION;
            }

            amount = Byte.parseByte(statEffects[1]);
            chance = Integer.parseInt(statEffects[2]);
        }
        else
        {
            effect = 0;
            chance = 100;
            for (int i = 0; i < statEffects.length; i++)
            {
                String stat = statEffects[i];
                byte effec = stat.equals("atk") ? ATTACK
                        : stat.equals("def") ? DEFENSE
                                : stat.equals("spd") ? VIT
                                        : stat.equals("spatk") ? SPATACK
                                                : stat.equals("spdef") ? SPDEFENSE
                                                        : stat.equals("acc") ? ACCURACY
                                                                : stat.equals("eva") ? EVASION : STATUS_NON;
                effect += effec;
                int j = (int) Math.round(Math.log(effec) / Math.log(2)) + 1;

                amounts[j] = Byte.parseByte(statEffects[i + 1]);

                i++;
            }

        }
        if (amount != 0)
        {
            for (int i = 0; i < amounts.length; i++)
            {
                amounts[i] = amount;
            }
        }

        int[] modifiers = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + amounts[1] * (effect & 1)));
        modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + amounts[2] * ((effect & 2) / 2)));
        modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + amounts[3] * ((effect & 4) / 4)));
        modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + amounts[4] * ((effect & 8) / 8)));
        modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + amounts[5] * ((effect & 16) / 16)));
        modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + amounts[6] * ((effect & 32) / 32)));
        modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + amounts[7] * ((effect & 64) / 64)));

        if (selfMove)
        {
            ret[0] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
            ret[1] = new int[] { 0 };
            ret[2] = modifiers.clone();
            ret[3] = new int[] { chance };
            return ret;
        }
        else if (stats.length == 1)
        {
            ret[2] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
            ret[3] = new int[] { 0 };
            ret[0] = modifiers.clone();
            ret[1] = new int[] { chance };
            return ret;
        }
        ret[0] = modifiers.clone();
        ret[1] = new int[] { chance };

        effect = 0;
        chance = 100;

        statEffects = stats[1].split(";");
        if (statEffects.length == 3)
        {
            String stat = statEffects[0];
            effect = stat.equals("atk") ? ATTACK
                    : stat.equals("def") ? DEFENSE
                            : stat.equals("spd") ? VIT
                                    : stat.equals("spatk") ? SPATACK
                                            : stat.equals("spdef") ? SPDEFENSE
                                                    : stat.equals("acc") ? ACCURACY
                                                            : stat.equals("eva") ? EVASION : STATUS_NON;
            if (stat.equalsIgnoreCase("all"))
            {
                effect = ATTACK + DEFENSE + VIT + SPATACK + SPDEFENSE + ACCURACY + EVASION;
            }

            amount = Byte.parseByte(statEffects[1]);
            chance = Integer.parseInt(statEffects[2]);
        }
        else
        {
            effect = 0;
            chance = 100;
            for (int i = 0; i < statEffects.length; i++)
            {
                String stat = statEffects[i];
                byte effec = stat.equals("atk") ? ATTACK
                        : stat.equals("def") ? DEFENSE
                                : stat.equals("spd") ? VIT
                                        : stat.equals("spatk") ? SPATACK
                                                : stat.equals("spdef") ? SPDEFENSE
                                                        : stat.equals("acc") ? ACCURACY
                                                                : stat.equals("eva") ? EVASION : STATUS_NON;
                effect += effec;
                int j = (int) Math.round(Math.log(effec) / Math.log(2)) + 1;

                amounts[j] = Byte.parseByte(statEffects[i + 1]);

                i++;
            }

        }
        if (amount != 0)
        {
            for (int i = 0; i < amounts.length; i++)
            {
                amounts[i] = amount;
            }
        }

        modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + amounts[1] * (effect & 1)));
        modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + amounts[2] * ((effect & 2) / 2)));
        modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + amounts[3] * ((effect & 4) / 4)));
        modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + amounts[4] * ((effect & 8) / 8)));
        modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + amounts[5] * ((effect & 16) / 16)));
        modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + amounts[6] * ((effect & 32) / 32)));
        modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + amounts[7] * ((effect & 64) / 64)));

        ret[2] = modifiers.clone();
        ret[3] = new int[] { chance };

        return ret;
    }

}
