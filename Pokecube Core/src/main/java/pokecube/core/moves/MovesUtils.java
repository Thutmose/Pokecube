package pokecube.core.moves;

import static pokecube.core.utils.PokeType.getAttackEfficiency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.reflect.TypeUtils;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.MoveEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

@SuppressWarnings("deprecation")
public class MovesUtils implements IMoveConstants
{
    public static Random                     rand = new Random();

    public static HashMap<String, Move_Base> moves;

    static
    {
        moves = new HashMap<String, Move_Base>();
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

    public static int attack(MovePacket packet)
    {
        return attack(packet, true);
    }

    /** Computes the statistics. Displays the messages to the owners, Damages
     * the attacked {@link Entity} by calling
     * {@link Entity#attackEntityFrom(DamageSource, int)}.
     *
     * @param attacker
     *            the Pokemob which attacks
     * @param attacked
     *            the {@link Entity} attacked, can be a Pokemob or not
     * @param attack
     *            the {@link String} English name of the attack
     * @param type
     *            the type of the attack, see {@link TypeUtils}
     * @param PWR
     *            the power of the attack
     * @param criticalLevel
     *            It's 1 for most moves. But some can have from to 2 to 5. The
     *            critical level used to calculate the critical ratio
     * @param statusChange
     *            if the status should change, specify here the status or
     *            STATUS_NON if none
     * @param changeAddition
     *            if a change should be added, specify here the change or
     *            CHANGE_NONE if none
     * @return the number of HPs the attack takes from target */
    public static int attack(MovePacket packet, boolean message)
    {
        IPokemob attacker = packet.attacker;
        Entity attacked = packet.attacked;

        if (!(packet.attacked instanceof EntityLivingBase) || packet.getMove() == null) return 0;

        Move_Base atk = packet.getMove();
        MoveEntry move = atk.move;
        attacker.onMoveUse(packet);
        if (attacked instanceof IPokemob)
        {
            ((IPokemob) attacked).onMoveUse(packet);
        }
        String attack = packet.attack;
        PokeType type = packet.attackType;
        int PWR = packet.PWR;
        int criticalLevel = packet.criticalLevel;
        byte statusChange = packet.statusChange;
        byte changeAddition = packet.changeAddition;
        float stabFactor = packet.stabFactor;
        if (!packet.stab)
        {
            packet.stab = packet.attacker.isType(packet.attackType);
        }
        if (!packet.stab)
        {
            stabFactor = 1;
        }

        TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(attacked);

        if (terrain != null)
        {
            PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
            if (effect == null)
            {
                terrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
            }
            effect.doEffect((EntityLivingBase) attacker);
        }

        if (packet.canceled)
        {
            displayEfficiencyMessages(attacker, attacked, -2, 0);
            return 0;
        }
        if (packet.failed)
        {
            displayEfficiencyMessages(attacker, attacked, -2, 0);
            return 0;
        }

        if (packet.infatuate[0] && attacked instanceof IPokemob)
        {
            ((IPokemob) attacked).getMoveStats().infatuateTarget = (Entity) attacker;
        }

        if (packet.infatuate[1] && attacker instanceof IPokemob)
        {
            attacker.getMoveStats().infatuateTarget = attacked;
        }

        attacker = packet.attacker;
        attacked = packet.attacked;
        attack = packet.attack;
        type = packet.attackType;
        PWR = packet.PWR;
        criticalLevel = packet.criticalLevel;
        statusChange = packet.statusChange;
        changeAddition = packet.changeAddition;
        boolean toSurvive = packet.noFaint;

        if (attacked == null) return 0;

        float efficiency = 1;

        if (attacked instanceof IPokemob)
        {
            efficiency = getAttackEfficiency(type, ((IPokemob) attacked).getType1(), ((IPokemob) attacked).getType2());
        }

        float criticalRatio = 1;

        if (attacker.getMoveStats().SPECIALTYPE == IPokemob.TYPE_CRIT)
        {
            criticalLevel += 1;
            attacker.getMoveStats().SPECIALTYPE = 0;
        }

        int critcalRate = 16;

        if (criticalLevel == 2)
        {
            critcalRate = 8;
        }

        if (criticalLevel == 3)
        {
            critcalRate = 4;
        }

        if (criticalLevel == 4)
        {
            critcalRate = 3;
        }

        if (criticalLevel == 5)
        {
            critcalRate = 2;
        }

        if (criticalLevel > 0 && rand.nextInt(critcalRate) == 0)
        {
            criticalRatio = 1.5f;
        }

        float attackStrength = attacker.getAttackStrength() * PWR / 150;

        if (attacked instanceof IPokemob)
        {
            attackStrength = getAttackStrength(attacker, (IPokemob) attacked, move.category, PWR, packet);

            int moveAcc = move.accuracy;
            if (moveAcc > 0)
            {
                double accuracy = Tools.modifierToRatio(attacker.getModifiers()[6], true);
                double evasion = Tools.modifierToRatio(((IPokemob) attacked).getModifiers()[7], true);
                double moveAccuracy = (moveAcc) / 100d;

                double hitModifier = moveAccuracy * accuracy / evasion;

                if (hitModifier < Math.random())
                {
                    efficiency = -1;
                }

            }
            if (moveAcc == -3)
            {
                double moveAccuracy = ((attacker.getLevel() - ((IPokemob) attacked).getLevel()) + 30) / 100d;

                double hitModifier = attacker.getLevel() < ((IPokemob) attacked).getLevel() ? -1 : moveAccuracy;

                if (hitModifier < Math.random())
                {
                    efficiency = -1;
                }
            }
        }

        if (attacked != attacker && attacked instanceof IPokemob && attacker instanceof EntityLiving)
        {
            if (((EntityLiving) attacked).getAttackTarget() != attacker)
                ((EntityLiving) attacked).setAttackTarget((EntityLivingBase) attacker);
            ((IPokemob) attacked).setPokemonAIState(IMoveConstants.ANGRY, true);
        }
        if (efficiency > 0)
        {
            Move_Ongoing ongoing;
            if (getMoveFromName(attack) instanceof Move_Ongoing && attacked instanceof IPokemob)
            {
                ongoing = (Move_Ongoing) getMoveFromName(attack);
                if (ongoing.onTarget()) ((IPokemob) attacked).addOngoingEffect(ongoing);
                if (ongoing.onSource()) attacker.addOngoingEffect(ongoing);
            }
        }

        float terrainDamageModifier = getTerrainDamageModifier(type, (Entity) attacker, terrain);

        int finalAttackStrength = Math.max(0, Math.round(attackStrength * efficiency * criticalRatio
                * terrainDamageModifier * stabFactor * packet.superEffectMult));

        float healRatio;
        float damageRatio;

        int beforeHealth = (int) ((EntityLivingBase) attacked).getHealth();// getHealth()

        if (efficiency > 0 && MoveEntry.oneHitKos.contains(attack))
        {
            finalAttackStrength = beforeHealth;
        }

        if (toSurvive)
        {
            finalAttackStrength = Math.min(finalAttackStrength, beforeHealth - 1);
        }

        if (PokecubeMod.core.getConfig().maxPlayerDamage > 0 && attacked instanceof EntityPlayer)
        {
            finalAttackStrength = Math.min(PokecubeMod.core.getConfig().maxPlayerDamage, finalAttackStrength);
        }

        if (attacked instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) attacked;
            if (mob.getAbility() != null)
            {
                finalAttackStrength = mob.getAbility().beforeDamage(mob, packet, finalAttackStrength);
            }
        }

        if (!(move.attackCategory == CATEGORY_SELF && PWR == 0) && finalAttackStrength > 0)
        {
            DamageSource source = new PokemobDamageSource("mob", (EntityLivingBase) attacker, getMoveFromName(attack));
            attacked.attackEntityFrom(source, finalAttackStrength);

            if (attacked instanceof IPokemob)
            {
                if (move.category == SPECIAL)
                    ((IPokemob) attacked).getMoveStats().SPECIALDAMAGETAKENCOUNTER += finalAttackStrength;
                if (move.category == PHYSICAL)
                    ((IPokemob) attacked).getMoveStats().PHYSICALDAMAGETAKENCOUNTER += finalAttackStrength;
            }
        }

        if ((efficiency > 0 || move.attackCategory == CATEGORY_SELF) && statusChange != STATUS_NON)
        {
            setStatus(attacked, statusChange);
        }
        if (efficiency > 0 && changeAddition != CHANGE_NONE) addChange(attacked, changeAddition);

        if (message && atk.getPWR(attacker, attacked) > 0)
            displayEfficiencyMessages(attacker, attacked, efficiency, criticalRatio);

        int afterHealth = (int) Math.max(0, ((EntityLivingBase) attacked).getHealth());

        int damageDealt = beforeHealth - afterHealth;

        healRatio = (move.damageHealRatio) / 100;
        damageRatio = move.selfDamage;

        if (damageRatio > 0)
        {
            if ((move.selfDamageType & MoveEntry.TOTALHP) != 0)
            {
                float max = ((EntityLiving) attacker).getMaxHealth();
                float diff = max * damageRatio / 100f;
                ((EntityLiving) attacker).attackEntityFrom(DamageSource.fall, diff);
            }
            if (((move.selfDamageType & MoveEntry.MISS) != 0 && efficiency <= 0))
            {
                float max = ((EntityLiving) attacker).getMaxHealth();
                float diff = max * damageRatio / 100f;
                ((EntityLiving) attacker).attackEntityFrom(DamageSource.fall, diff);
            }
            if (((move.selfDamageType & MoveEntry.DAMAGEDEALT) != 0))
            {
                float diff = damageDealt * damageRatio / 100f;
                ((EntityLiving) attacker).attackEntityFrom(DamageSource.fall, diff);
            }
            if (((move.selfDamageType & MoveEntry.RELATIVEHP) != 0))
            {
                float current = ((EntityLiving) attacker).getHealth();
                float diff = current * damageRatio / 100f;
                ((EntityLiving) attacker).attackEntityFrom(DamageSource.fall, diff);
            }
        }

        if (healRatio > 0)
        {
            float toHeal = Math.max(1, (damageDealt * healRatio));
            ((EntityLiving) attacker).setHealth(
                    Math.min(((EntityLiving) attacker).getMaxHealth(), ((EntityLiving) attacker).getHealth() + toHeal));
        }
        if (attacked instanceof IPokemob && atk.hasStatModTarget && efficiency > 0)
        {
            if ((!handleStats((IPokemob) attacked, (Entity) attacker, packet, true))) if (message && (move.power == 0))
                displayStatsMessage((IPokemob) attacked, (Entity) attacker, -2, (byte) 0, (byte) 0);
            attacker.getMoveStats().TARGETLOWERCOUNTER = 80;
        }
        if (atk.hasStatModSelf)
        {
            boolean goodEffect = false;
            for (int i = 0; i < atk.move.attackerStatModification.length; i++)
                if (atk.move.attackerStatModification[i] > 0) goodEffect = true;

            if ((!handleStats(attacker, attacked, packet, false)))
                if (message && goodEffect) displayStatsMessage(attacker, (Entity) attacker, -2, (byte) 0, (byte) 0);
            attacker.getMoveStats().SELFRAISECOUNTER = 80;
        }

        healRatio = (move.selfHealRatio) / 100;
        boolean canHeal = ((EntityLiving) attacker).getHealth() < ((EntityLiving) attacker).getMaxHealth();
        if (healRatio > 0 && canHeal && attacker.getMoveStats().SELFRAISECOUNTER == 0)
        {
            ((EntityLiving) attacker).setHealth(Math.min(((EntityLiving) attacker).getMaxHealth(),
                    ((EntityLiving) attacker).getHealth() + (((EntityLiving) attacker).getMaxHealth() * healRatio)));
            attacker.getMoveStats().SELFRAISECOUNTER = 80;
        }

        packet = new MovePacket(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                false);
        packet.hit = efficiency >= 0;
        packet.didCrit = criticalRatio > 1;

        attacker.onMoveUse(packet);
        if (attacked instanceof IPokemob)
        {
            ((IPokemob) attacked).onMoveUse(packet);
        }

        return beforeHealth - afterHealth;
    }

    /** For contact moves like tackle. The mob gets close to its target and
     * hits.
     *
     * @return whether the mob must attack */
    public static boolean contactAttack(IPokemob attacker, Entity attacked, float f)
    {
        EntityLivingBase entityAttacker = (EntityLivingBase) attacker;
        if (attacked == null || attacker == null) return false;

        if (f < 4 && entityAttacker.onGround || attacker.getPokedexEntry().flys()
                || attacker.getPokedexEntry().floats())
        {
            double d0 = attacked.posX - entityAttacker.posX;
            double d1 = attacked.posZ - entityAttacker.posZ;
            float f1 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
            f1 = Math.max(f1, 0.25f);
            entityAttacker.motionX += d0 / f1 * 0.25D * 0.8D + entityAttacker.motionX * 0.2D;
            entityAttacker.motionZ += d1 / f1 * 0.25D * 0.8D + entityAttacker.motionZ * 0.2D;
            entityAttacker.motionY = 0.2d;
        }
        return true;
    }

    public static void displayEfficiencyMessages(IPokemob attacker, Entity attacked, float efficiency,
            float criticalRatio)
    {
        ITextComponent text;
        if (efficiency == -1)
        {
            String message = "pokemob.move.missed";
            if (attacked instanceof IPokemob)
            {
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                ((IPokemob) attacked).displayMessageToOwner(text);
                return;
            }
            else if (attacked == null)
            {
                if (((EntityLiving) attacker).getAttackTarget() != null)
                {
                    attacked = ((EntityLiving) attacker).getAttackTarget();
                    String name = attacked.getName();
                    text = CommandTools.makeTranslatedMessage(message, "red", name);
                    ((IPokemob) attacked).displayMessageToOwner(text);
                    attacker.displayMessageToOwner(text);
                }
                else if (attacker.getPokemonAIState(IMoveConstants.ANGRY))
                {
                    message = "pokemob.move.missed";
                    text = CommandTools.makeTranslatedMessage(message, "red");
                    attacker.displayMessageToOwner(text);
                }
            }
        }
        if (efficiency == -2)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.failed";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
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
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                ((IPokemob) attacked).displayMessageToOwner(text);
                return;
            }
        }
        else if (efficiency < 1)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.not.very.effective";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
        }
        else if (efficiency > 1)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.super.effective";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
        }

        if (criticalRatio > 1)
        {
            if (attacked instanceof IPokemob)
            {
                text = CommandTools.makeTranslatedMessage("pokemob.move.critical.hit", "green",
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage("pokemob.move.critical.hit", "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
        }
    }

    public static void displayMoveMessages(IPokemob attacker, Entity attacked, String attack)
    {
        ITextComponent text;

        if (attack.equals("pokemob.status.confusion"))
        {
            text = CommandTools.makeTranslatedMessage("pokemob.status.confusion", "red",
                    ((IPokemob) attacked).getPokemonDisplayName());
            ((IPokemob) attacked).displayMessageToOwner(text);
            return;
        }
        String attackName = getUnlocalizedMove(attack);
        text = CommandTools.makeTranslatedMessage("pokemob.move.used", "green",
                ((IPokemob) attacker).getPokemonDisplayName(), attackName);
        ((IPokemob) attacker).displayMessageToOwner(text);
        if (attacker == attacked) return;

        if (attacked instanceof IPokemob)
        {
            text = CommandTools.makeTranslatedMessage("pokemob.move.enemyUsed", "red",
                    ((IPokemob) attacker).getPokemonDisplayName(), attackName);
            ((IPokemob) attacked).displayMessageToOwner(text);
        }
        else if (attacked instanceof EntityPlayer && !attacked.worldObj.isRemote)
        {
            text = CommandTools.makeTranslatedMessage("pokemob.move.enemyUsed", "red",
                    ((IPokemob) attacked).getPokemonDisplayName(), attackName);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(10));
            buffer.writeByte(PokecubeClientPacket.MOVEMESSAGE);
            buffer.writeInt(attacked.getEntityId());

            buffer.writeTextComponent(text);

            PokecubeClientPacket mess = new PokecubeClientPacket(buffer);
            PokecubePacketHandler.sendToClient(mess, (EntityPlayer) attacked);
        }
    }

    protected static void displayStatsMessage(IPokemob attacker, Entity attacked, float efficiency, byte stat,
            byte amount)
    {
        ITextComponent text;
        if (efficiency == -2)
        {
            if (attacked instanceof IPokemob)
            {
                String message = "pokemob.move.stat.fail";
                text = CommandTools.makeTranslatedMessage(message, "green",
                        ((IPokemob) attacked).getPokemonDisplayName());
                if (attacked != attacker) attacker.displayMessageToOwner(text);
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
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
            if (attacked instanceof IPokemob && attacker != null)
            {
                if (attacker == attacked)
                {
                    String colour = fell ? "red" : "green";
                    text = CommandTools.makeTranslatedMessage(message, colour,
                            ((IPokemob) attacked).getPokemonDisplayName(), statName);
                    attacker.displayMessageToOwner(text);
                }
                else
                {
                    text = CommandTools.makeTranslatedMessage(message, "red",
                            ((IPokemob) attacked).getPokemonDisplayName(), statName);
                    ((IPokemob) attacked).displayMessageToOwner(text);
                }
            }
            else if (attacker == null && (attacked instanceof IPokemob))
            {
                text = CommandTools.makeTranslatedMessage(message, "red", ((IPokemob) attacked).getPokemonDisplayName(),
                        statName);
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
            else if (attacker instanceof IPokemob)
            {
                String colour = fell ? "green" : "red";
                text = CommandTools.makeTranslatedMessage(message, colour,
                        ((IPokemob) attacked).getPokemonDisplayName(), statName);
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
                        ((IPokemob) attacked).getPokemonDisplayName());
                attacker.displayMessageToOwner(text);
            }
            if (attacked instanceof IPokemob)
            {
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                ((IPokemob) attacked).displayMessageToOwner(text);
            }
            else if (attacked instanceof EntityPlayer)
            {
                text = CommandTools.makeTranslatedMessage(message, "red",
                        ((IPokemob) attacked).getPokemonDisplayName());
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(10));
                buffer.writeByte(PokecubeClientPacket.MOVEMESSAGE);
                buffer.writeInt(attacked.getEntityId());
                buffer.writeTextComponent(text);
                PokecubeClientPacket mess = new PokecubeClientPacket(buffer);
                PokecubePacketHandler.sendToClient(mess, (EntityPlayer) attacked);
            }
        }
    }

    public static void doAttack(String attackName, IPokemob attacker, Entity attacked, float f)
    {
        Move_Base move = moves.get(attackName);
        if (move != null)
        {
            move.attack(attacker, attacked, f);
        }
        else
        {
            if (attackName != null)
            {
                System.err.println("The Move \"" + attackName + "\" does not exist.");
            }
            doAttack(DEFAULT_MOVE, attacker, attacked, f);
        }
    }

    public static void doAttack(String attackName, IPokemob attacker, Vector3 attacked, float f)
    {
        Move_Base move = moves.get(attackName);

        if (move != null)
        {
            move.attack(attacker, attacked, f);
        }
        else
        {
            if (attackName != null)
            {
                System.err.println("The Move \"" + attackName + "\" does not exist.");
            }
            doAttack(DEFAULT_MOVE, attacker, attacked, f);
        }
    }

    public static float getAttackStrength(IPokemob attacker, IPokemob attacked, byte type, int PWR,
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

        if (type == SPECIAL)
        {
            ATT = (int) (Tools.getStats(attacker)[3] * movePacket.statMults[3]);
            DEF = Tools.getStats(attacked)[4];
        }
        else
        {
            ATT = (int) (Tools.getStats(attacker)[1] * movePacket.statMults[1]);
            DEF = Tools.getStats(attacked)[2];
        }

        ATT = (int) (statusMultiplier * ATT);

        return (((level * 0.4F + 2F) * ATT * PWR) / (DEF * 50F) + 2);
    }

    /** Computes the delay between two moves in a fight from speed stat.
     *
     * @return the time to wait before reattack */
    public static int getDelayBetweenAttacks(IPokemob attacker, String moveName)
    {
        float statusMultiplier = 1F;
        if (attacker.getStatus() == STATUS_PAR) statusMultiplier = 0.25F;

        Move_Base move = getMoveFromName(moveName);

        if (moveName == MOVE_NONE)
        {
            move = getMoveFromName(MOVE_TACKLE);
        }
        else if (move == null)
        {
            move = getMoveFromName(MOVE_TACKLE);
        }
        float pp = move.getPP();

        float ppFactor = pp / 40f;

        int VIT = (int) Math.max(1, ppFactor * statusMultiplier * Tools.getStats(attacker)[5]);

        int ret = (int) (100 / Math.sqrt(VIT));
        return ret; // should be between around 10 and 80
    }

    public static Move_Base getMoveFromName(String moveName)
    {
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
        if (effect == null)
        {
            terrain.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
        }

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

    public static String getLocalizedMove(String attack)
    {
        String PREFIX = "pokemob.move.";
        String translatedAttack = I18n.translateToLocal(PREFIX + attack);
        if (translatedAttack == null || translatedAttack.startsWith(PREFIX))
        {
            translatedAttack = attack;
        }
        return translatedAttack;
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
        byte[] modifiers = mob.getModifiers();
        byte[] old = modifiers.clone();

        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + stats[1]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + stats[2]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + stats[3]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + stats[4]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + stats[5]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + stats[6]));
        if (attacked ? atk.attackedStatModProb / 100f > Math.random() : atk.attackerStatModProb / 100f > Math.random())
            modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + stats[7]));
        mob.setModifiers(modifiers);

        boolean ret = false;
        byte[] diff = new byte[old.length];
        for (int i = 0; i < old.length; i++)
        {
            diff[i] = (byte) (old[i] - modifiers[i]);
            if (old[i] != modifiers[i])
            {
                ret = true;
            }
        }
        if (ret) for (byte i = 0; i < diff.length; i++)
        {
            if (diff[i] != 0)
            {
                if (!attacked) displayStatsMessage(mob, target, 0, i, diff[i]);
                else if (target instanceof IPokemob)
                    displayStatsMessage((IPokemob) target, (Entity) mob, 0, i, diff[i]);
            }
        }
        return ret;
    }

    public static boolean handleStats2(IPokemob mob, Entity attacker, int statEffect, int statEffectAmount)
    {
        byte[] modifiers = mob.getModifiers();
        byte[] old = modifiers.clone();
        modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + statEffectAmount * (statEffect & 1)));
        modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + statEffectAmount * (statEffect & 2) / 2));
        modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + statEffectAmount * (statEffect & 4) / 4));
        modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + statEffectAmount * (statEffect & 8) / 8));
        modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + statEffectAmount * (statEffect & 16) / 16));
        modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + statEffectAmount * (statEffect & 32) / 32));
        modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + statEffectAmount * (statEffect & 64) / 64));
        mob.setModifiers(modifiers);
        boolean ret = false;
        byte[] diff = new byte[old.length];
        for (int i = 0; i < old.length; i++)
        {
            diff[i] = (byte) (old[i] - modifiers[i]);
            if (old[i] != modifiers[i])
            {
                ret = true;
            }
        }
        if (ret) for (byte i = 0; i < diff.length; i++)
        {
            if (diff[i] != 0 && attacker instanceof IPokemob)
            {
                displayStatsMessage((IPokemob) attacker, (Entity) mob, 0, i, diff[i]);
            }
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
                if (s.toLowerCase().contentEquals(attackName.toLowerCase()))
                {
                    attackName = s;
                    return true;
                }
            }
        }
        if (move != null) { return true; }
        return false;
    }

    /** returns a new explosion. Does initiation (at time of writing Explosion
     * is not finished) */
    public static Explosion newExplosion(Entity entity, double par2, double par4, double par6, float par8, boolean par9,
            boolean par10)
    {
        ExplosionCustom var11 = new ExplosionCustom(entity.worldObj, entity, par2, par4, par6, par8);

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
            ((IPokemob) attacked).setStatus(status);
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

    public static Entity targetHit(Entity attacker, Vector3 dest)
    {
        Vector3 source = Vector3.getNewVector().set(attacker, true);
        source.y += attacker.height / 4;
        return targetHit(source, dest.subtract(source), 16, attacker.worldObj, attacker, false);
    }

    @SuppressWarnings("rawtypes")
    public static Entity targetHit(Vector3 source, Vector3 dir, int distance, World worldObj, Entity attacker,
            boolean ignoreAllies, Class... ignored)
    {
        // Vector3 dest = Vector3.getVector().set(target, true);
        Entity target = null;

        List<Entity> targets = source.allEntityLocationExcluding(distance, 0.5, dir, source, worldObj, attacker);
        double closest = 16;

        if (targets != null) for (Entity e : targets)
        {
            if (e instanceof EntityLivingBase && attacker.getDistanceToEntity(e) < closest
                    && (PokecubeMod.pokemobsDamagePlayers || !(e instanceof EntityPlayer))
                    && e != attacker.getRidingEntity())
            {
                if (ignoreAllies && e instanceof IPokemob)
                {
                    if (attacker instanceof IPokemob)
                    {
                        if (((IPokemob) attacker).getPokemonOwner() == ((IPokemob) e).getPokemonOwner()) continue;
                    }
                    else if (e instanceof EntityPlayer)
                    {
                        if (((IPokemob) attacker).getPokemonOwner() == e) continue;
                    }
                }
                if (!PokecubeMod.pokemobsDamageOwner && ((IPokemob) attacker).getPokemonOwner() == e)
                {
                    continue;
                }

                for (Class c : ignored)
                {
                    if (c.isInstance(e)) continue;
                }

                closest = attacker.getDistanceToEntity(e);
                target = e;
            }
        }
        else target = null;

        return target;
    }

    public static List<EntityLivingBase> targetsHit(Entity attacker, Vector3 dest)
    {
        Vector3 source = Vector3.getNewVector().set(attacker, true);

        source.y += attacker.height / 4;
        List<Entity> targets = source.allEntityLocationExcluding(16, 0.1, dest.subtract(source), source,
                attacker.worldObj, attacker);
        List<EntityLivingBase> ret = new ArrayList<EntityLivingBase>();
        if (targets != null) for (Entity e : targets)
        {
            if (e instanceof EntityLivingBase && (PokecubeMod.pokemobsDamagePlayers || !(e instanceof EntityPlayer)))
            {
                if (((IPokemob) attacker).getPokemonOwner() == e && !PokecubeMod.pokemobsDamageOwner) continue;
                ret.add((EntityLivingBase) e);
            }
        }
        return ret;
    }

    public static List<EntityLivingBase> targetsHit(Entity attacker, Vector3 dest, int range, double area)
    {
        Vector3 source = Vector3.getNewVector().set(attacker);

        List<Entity> targets = source.allEntityLocationExcluding(range, area, dest.subtract(source), source,
                attacker.worldObj, attacker);
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

}
