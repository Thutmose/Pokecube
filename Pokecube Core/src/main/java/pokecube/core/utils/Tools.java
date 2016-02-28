package pokecube.core.utils;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class Tools
{
    public Tools()
    {
        initTables();
    }

    public static int serialize(float f, float g)
    {
        float toSet = g;
        if (toSet > f)
        {
            toSet = f;
        }

        if (toSet < 0)
        {
            toSet = 0;
        }

        float maxHealthFloat = f;

        float value = (PokecubeMod.FULL_HEALTH * toSet) / maxHealthFloat;
        return (int) (PokecubeMod.MAX_DAMAGE - value);
    }

    public static int getHealth(int maxHealth, int serialization)
    {
        float value = (PokecubeMod.MAX_DAMAGE - serialization);
        int health = (int) (value * maxHealth / PokecubeMod.FULL_HEALTH);

        if (health > maxHealth)
        {
            health = maxHealth;
        }

        if (health < 0)
        {
            health = 0;
        }

        return health;
    }

    public static int getHealedPokemobSerialization()
    {
        return PokecubeMod.MAX_DAMAGE - PokecubeMod.FULL_HEALTH;
    }

    // ADDED + 128 for java usbytes
    public static int getHP(int BS, int IV, int EV, int level)
    {
        if (BS == 1) return 1;

        int EP = MathHelper.floor_double((EV + 128) / 4);
        return 10 + (MathHelper.floor_double((2 * BS) + IV + EP + 100) * level / 100);
    }

    public static int getStat(int BS, int IV, int EV, int level, int nature)
    {
        return getStat(BS, IV, EV, level, 0, nature);
    }

    // This is for getting stats after effects of attack modifiers.
    public static int getStat(int BS, int IV, int EV, int level, int mod, int nature)
    {
        int EP = MathHelper.floor_double((EV + 128) / 4);
        double natModifier = (((double) 10 * nature) + 100) / 100d;
        double modifier = modifierToRatio((byte) mod, false);
        return Math.max(
                (int) ((((MathHelper.floor_double((2 * BS) + IV + EP) * level / 100) + 5) * modifier * natModifier)),
                5);
    }

    public static int[] getStats(IPokemob mob)
    {
        int[] ret = new int[6];
        ret[0] = getHP(mob.getBaseStats()[0], mob.getIVs()[0], mob.getEVs()[0], mob.getLevel());
        ret[1] = getStat(mob.getBaseStats()[1], mob.getIVs()[1], mob.getEVs()[1], mob.getLevel(), mob.getModifiers()[1],
                mob.getNature().getStatsMod()[1]);
        ret[2] = getStat(mob.getBaseStats()[2], mob.getIVs()[2], mob.getEVs()[2], mob.getLevel(), mob.getModifiers()[2],
                mob.getNature().getStatsMod()[2]);
        ret[3] = getStat(mob.getBaseStats()[3], mob.getIVs()[3], mob.getEVs()[3], mob.getLevel(), mob.getModifiers()[3],
                mob.getNature().getStatsMod()[3]);
        ret[4] = getStat(mob.getBaseStats()[4], mob.getIVs()[4], mob.getEVs()[4], mob.getLevel(), mob.getModifiers()[4],
                mob.getNature().getStatsMod()[4]);
        ret[5] = getStat(mob.getBaseStats()[5], mob.getIVs()[5], mob.getEVs()[5], mob.getLevel(), mob.getModifiers()[5],
                mob.getNature().getStatsMod()[5]);
        return ret;
    }

    public static int[] getStatsUnMod(IPokemob mob)
    {
        int[] ret = new int[6];
        ret[0] = getHP(mob.getPokedexEntry().getStatHP(), mob.getIVs()[0], mob.getEVs()[0], mob.getLevel());
        ret[1] = getStat(mob.getPokedexEntry().getStatATT(), mob.getIVs()[1], mob.getEVs()[1], mob.getLevel(),
                mob.getNature().getStatsMod()[1]);
        ret[2] = getStat(mob.getPokedexEntry().getStatDEF(), mob.getIVs()[2], mob.getEVs()[2], mob.getLevel(),
                mob.getNature().getStatsMod()[2]);
        ret[3] = getStat(mob.getPokedexEntry().getStatATTSPE(), mob.getIVs()[3], mob.getEVs()[3], mob.getLevel(),
                mob.getNature().getStatsMod()[3]);
        ret[4] = getStat(mob.getPokedexEntry().getStatDEFSPE(), mob.getIVs()[4], mob.getEVs()[4], mob.getLevel(),
                mob.getNature().getStatsMod()[4]);
        ret[5] = getStat(mob.getPokedexEntry().getStatVIT(), mob.getIVs()[5], mob.getEVs()[5], mob.getLevel(),
                mob.getNature().getStatsMod()[5]);
        return ret;
    }

    public static int getStat(int oldStat, int mod)
    {
        int ret = (int) (oldStat * modifierToRatio((byte) mod, false));

        return Math.max(5, ret);
    }

    public static double modifierToRatio(byte mod, boolean accuracy)
    {
        double modifier = 1;
        if (mod == 0) modifier = 1;
        else if (mod == 1) modifier = !accuracy ? 1.5 : 4 / 3d;
        else if (mod == 2) modifier = !accuracy ? 2 : 5 / 3d;
        else if (mod == 3) modifier = !accuracy ? 2.5 : 2;
        else if (mod == 4) modifier = !accuracy ? 3 : 7 / 3d;
        else if (mod == 5) modifier = !accuracy ? 3.5 : 8 / 3d;
        else if (mod == 6) modifier = !accuracy ? 4 : 3;
        else if (mod == -1) modifier = !accuracy ? 2 / 3d : 3 / 4d;
        else if (mod == -2) modifier = !accuracy ? 1 / 2d : 3 / 5d;
        else if (mod == -3) modifier = !accuracy ? 2 / 5d : 3 / 6d;
        else if (mod == -4) modifier = !accuracy ? 1 / 3d : 3 / 7d;
        else if (mod == -5) modifier = !accuracy ? 2 / 7d : 3 / 8d;
        else if (mod == -6) modifier = !accuracy ? 1 / 4d : 3 / 9d;
        return modifier;
    }

    public static int levelToXp(int type, int level)
    {
        switch (type)
        {
        case 0:// 800 000
            return MathHelper.floor_double(0.8D * Math.pow(level, 3)) + 1;

        case 1:// 1 000 000
            return MathHelper.floor_double(Math.pow(level, 3)) + 1;

        case 2:// 1 059 860
            return MathHelper.floor_double(1.05D * Math.pow(level, 3)) + 1; // it
                                                                            // should
                                                                            // be
                                                                            // the
                                                                            // parabollic

        case 3:// 1 250 000
            return MathHelper.floor_double(1.25D * Math.pow(level, 3)) + 1;

        case 4:// 600 000
        {
            int ret = 0;
            if (level > 0 && level <= 50)
            {
                ret = (int) (Math.pow(level, 3) * (100 - level) / 50) + 1;
            }
            if (level > 50 && level <= 68)
            {
                ret = (int) (Math.pow(level, 3) * (150 - level) / 100);
            }
            if (level > 68 && level <= 98)
            {
                ret = (int) (Math.pow(level, 3) * (1911 - 10 * level) / 1500);
            }
            if (level > 98)
            {
                ret = (int) (Math.pow(level, 3) * (160 - level) / 100);
            }

            return ret;
        }
        case 5:// 1 640 000
        {
            int ret = 0;
            if (level > 0 && level <= 15)
            {
                int numA = level * level * level;
                double numB = (((level + 1) / 3 + 24) / 50d);
                ret = (int) (numA * numB) + 1;
            }
            if (level > 15 && level <= 36)
            {
                int numA = level * level * level;
                double numB = ((level + 14) / 50d);
                ret = (int) (numA * numB);
            }
            if (level > 36)
            {
                ret = (int) (Math.pow(level, 3) * ((level + 64) / 100d));
            }

            return ret;
        }

        default:
            return -1;
        }
    }

    // cache these in tables, for easier lookup.
    private static int[] erraticXp     = new int[101];
    private static int[] fluctuatingXp = new int[101];

    public static int getType(String name)
    {
        name = name.toLowerCase().trim();
        if (name.equalsIgnoreCase("erratic")) return 4;
        if (name.equalsIgnoreCase("fast")) return 0;
        if (name.equalsIgnoreCase("medium fast")) return 1;
        if (name.equalsIgnoreCase("medium slow")) return 2;
        if (name.equalsIgnoreCase("slow")) return 3;
        if (name.equalsIgnoreCase("fluctuating")) return 5;
        return -1;
    }

    public static int[] maxXPs = { 800000, 1000000, 1059860, 1250000, 600000, 1640000 };

    public static int xpToLevel(int type, int exp)
    {
        int level = 0;

        switch (type)
        {
        case 0:
            level = MathHelper.floor_double(Math.pow(exp / 0.8D, 0.333333333333333333333F));
            break;

        case 1:
            level = MathHelper.floor_double(Math.pow(exp, 0.333333333333333333333F));
            break;

        case 2:
            level = MathHelper.floor_double(Math.pow(exp / 1.05D, 0.333333333333333333333F));
            break;

        case 3:
            level = MathHelper.floor_double(Math.pow(exp / 1.25D, 0.333333333333333333333F));
            break;

        case 4:
            level = getLevelFromTable(erraticXp, exp);
            break;

        case 5:
            level = getLevelFromTable(fluctuatingXp, exp);
            break;

        default:
            level = -1;
        }

        return Math.min(level, 100);
    }

    private static int getLevelFromTable(int[] table, int exp)
    {
        int level = 0;
        for (int i = 0; i < 100; i++)
        {
            if (table[i] <= exp && table[i + 1] > exp)
            {
                level = i;
                break;
            }
        }

        return level;
    }

    private static void initTables()
    {
        for (int i = 0; i < 101; i++)
        {
            erraticXp[i] = levelToXp(4, i);
            fluctuatingXp[i] = levelToXp(5, i);
        }
    }

    public static int getExp(float coef, int baseXP, int level)
    {
        // Increase exp gain if the density is set lower.
        float multiplier = (float) (1.0F / Math.sqrt(PokecubeMod.MAX_DENSITY));
        return MathHelper.floor_float(multiplier * coef * baseXP * level / 7F);
    }

    public static int computeCatchRate(IPokemob pokemob, int pokecubeId)
    {
        double cubeBonus = 0;
        Item cube = PokecubeItems.getFilledCube(pokecubeId);
        if (cube instanceof IPokecube)
        {
            cubeBonus = ((IPokecube) cube).getCaptureModifier(pokemob, pokecubeId);
        }
        return computeCatchRate(pokemob, cubeBonus);
    }

    public static int computeCatchRate(IPokemob pokemob, double cubeBonus)
    {
        float HPmax = ((EntityLivingBase) pokemob).getMaxHealth();
        Random rand = new Random();
        float HP = ((EntityLivingBase) pokemob).getHealth();
        float statusBonus = 1F;
        byte status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_FRZ || status == IMoveConstants.STATUS_SLP)
        {
            statusBonus = 2F;
        }
        else if (status != IMoveConstants.STATUS_NON)
        {
            statusBonus = 1.5F;
        }
        int catchRate = pokemob.getCatchRate();

        double a = getCatchRate(HPmax, HP, catchRate, cubeBonus, statusBonus);

        if (a > 255)
        {
            return 5;
        }
        else
        {
            double b = 1048560 / Math.sqrt(Math.sqrt(16711680 / a));
            int n = 0;

            if (rand.nextInt(65535) <= b)
            {
                n++;
            }

            if (rand.nextInt(65535) <= b)
            {
                n++;
            }

            if (rand.nextInt(65535) <= b)
            {
                n++;
            }

            if (rand.nextInt(65535) <= b)
            {
                n++;
            }

            return n;
        }
    }

    public static double getCatchRate(float hPmax, float hP, float catchRate, double cubeBonus, double statusBonus)
    {
        return ((3D * hPmax - 2D * hP) * catchRate * cubeBonus * statusBonus) / (3D * hPmax);
    }

    /** Can be {@link IPokemob#MALE}, {@link IPokemob#FEMALE} or
     * {@link IPokemob#NOSEXE}
     *
     * @param baseValue
     *            the sexe ratio of the Pokemon, 254=Only female, 255=no sexe,
     *            0=Only male
     * @param random
     * @return the int gender */
    public static byte getSexe(int baseValue, Random random)
    {
        if (baseValue == 255) { return IPokemob.NOSEXE; }

        if (random.nextInt(255) >= baseValue)
        {
            return IPokemob.MALE;
        }
        else
        {
            return IPokemob.FEMALE;
        }
    }

    public static byte getRandomIV(Random random)
    {
        return (byte) random.nextInt(32);
    }

    public static int countPokemon(Vector3 location, World world, double distance, PokedexEntry pokemon)
    {
        int ret = 0;
        List<EntityLiving> list = world.getEntitiesWithinAABB(EntityLiving.class,
                location.getAABB().expand(distance, distance, distance));
        for (Object o : list)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
                if (mob.getPokedexEntry() == pokemon)
                {
                    ret++;
                }
            }
        }

        return ret;
    }

    public static int countPokemon(Vector3 location, World world, double distance, PokeType type)
    {
        int ret = 0;
        List<EntityLiving> list = world.getEntitiesWithinAABB(EntityLiving.class,
                location.getAABB().expand(distance, distance, distance));
        for (Object o : list)
        {
            if (o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;
                if (mob.getPokedexEntry().isType(type))
                {
                    ret++;
                }
            }
        }
        return ret;
    }

    public static Entity getPointedEntity(Entity entity, double distance)
    {
        Vec3 vec3 = new Vec3(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);
        double d0 = distance;
        Vec3 vec31 = entity.getLook(0);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
        Entity pointedEntity = null;
        float f = 1.0F;
        List<Entity> list = entity.worldObj.getEntitiesInAABBexcluding(entity,
                entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0)
                        .expand((double) f, (double) f, (double) f),
                Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
                {
                    public boolean apply(Entity p_apply_1_)
                    {
                        return p_apply_1_.canBeCollidedWith();
                    }
                }));
        double d2 = distance;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity) list.get(j);
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double) f1, (double) f1, (double) f1);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

            if (axisalignedbb.isVecInside(vec3))
            {
                if (d2 >= 0.0D)
                {
                    pointedEntity = entity1;
                    d2 = 0.0D;
                }
            }
            else if (movingobjectposition != null)
            {
                double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                if (d3 < d2 || d2 == 0.0D)
                {
                    if (entity1 == entity.ridingEntity && !entity.canRiderInteract())
                    {
                        if (d2 == 0.0D)
                        {
                            pointedEntity = entity1;
                        }
                    }
                    else
                    {
                        pointedEntity = entity1;
                        d2 = d3;
                    }
                }
            }
        }
        return pointedEntity;
    }
}
