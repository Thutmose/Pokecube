package pokecube.core.utils;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class Tools
{
    /** This is an array of what lvl has what exp for the varying exp modes.
     * This array came from:
     * http://bulbapedia.bulbagarden.net/wiki/Experience */
    private static final int[][] expMap = {
            //@formatter:off
            { 0, 0, 0, 0, 0, 0, 1 }, 
            { 15, 6, 8, 9, 10, 4, 2 },
            { 52, 21, 27, 57, 33, 13, 3 }, 
            { 122, 51, 64, 96, 80, 32, 4 }, 
            { 237, 100, 125, 135, 156, 65, 5 },
            { 406, 172, 216, 179, 270, 112, 6 }, 
            { 637, 274, 343, 236, 428, 178, 7 },
            { 942, 409, 512, 314, 640, 276, 8 }, 
            { 1326, 583, 729, 419, 911, 393, 9 },
            { 1800, 800, 1000, 560, 1250, 540, 10 }, 
            { 2369, 1064, 1331, 742, 1663, 745, 11 },
            { 3041, 1382, 1728, 973, 2160, 967, 12 }, 
            { 3822, 1757, 2197, 1261, 2746, 1230, 13 },
            { 4719, 2195, 2744, 1612, 3430, 1591, 14 }, 
            { 5737, 2700, 3375, 2035, 4218, 1957, 15 },
            { 6881, 3276, 4096, 2535, 5120, 2457, 16 }, 
            { 8155, 3930, 4913, 3120, 6141, 3046, 17 },
            { 9564, 4665, 5832, 3798, 7290, 3732, 18 }, 
            { 11111, 5487, 6859, 4575, 8573, 4526, 19 },
            { 12800, 6400, 8000, 5460, 10000, 5440, 20 }, 
            { 14632, 7408, 9261, 6458, 11576, 6482, 21 },
            { 16610, 8518, 10648, 7577, 13310, 7666, 22 }, 
            { 18737, 9733, 12167, 8825, 15208, 9003, 23 },
            { 21012, 11059, 13824, 10208, 17280, 10506, 24 }, 
            { 23437, 12500, 15625, 11735, 19531, 12187, 25 },
            { 26012, 14060, 17576, 13411, 21970, 14060, 26 }, 
            { 28737, 15746, 19683, 15244, 24603, 16140, 27 },
            { 31610, 17561, 21952, 17242, 27440, 18439, 28 }, 
            { 34632, 19511, 24389, 19411, 30486, 20974, 29 },
            { 37800, 21600, 27000, 21760, 33750, 23760, 30 }, 
            { 41111, 23832, 29791, 24294, 37238, 26811, 31 },
            { 44564, 26214, 32768, 27021, 40960, 30146, 32 }, 
            { 48155, 28749, 35937, 29949, 44921, 33780, 33 },
            { 51881, 31443, 39304, 33084, 49130, 37731, 34 }, 
            { 55737, 34300, 42875, 36435, 53593, 42017, 35 },
            { 59719, 37324, 46656, 40007, 58320, 46656, 36 }, 
            { 63822, 40522, 50653, 43808, 63316, 50653, 37 },
            { 68041, 43897, 54872, 47846, 68590, 55969, 38 }, 
            { 72369, 47455, 59319, 52127, 74148, 60505, 39 },
            { 76800, 51200, 64000, 56660, 80000, 66560, 40 }, 
            { 81326, 55136, 68921, 61450, 86151, 71677, 41 },
            { 85942, 59270, 74088, 66505, 92610, 78533, 42 }, 
            { 90637, 63605, 79507, 71833, 99383, 84277, 43 },
            { 95406, 68147, 85184, 77440, 106480, 91998, 44 }, 
            { 100237, 72900, 91125, 83335, 113906, 98415, 45 },
            { 105122, 77868, 97336, 89523, 121670, 107069, 46 }, 
            { 110052, 83058, 103823, 96012, 129778, 114205, 47 },
            { 115015, 88473, 110592, 102810, 138240, 123863, 48 },
            { 120001, 94119, 117649, 109923, 147061, 131766, 49 },
            { 125000, 100000, 125000, 117360, 156250, 142500, 50 },
            { 131324, 106120, 132651, 125126, 165813, 151222, 51 },
            { 137795, 112486, 140608, 133229, 175760, 163105, 52 },
            { 144410, 119101, 148877, 141677, 186096, 172697, 53 },
            { 151165, 125971, 157464, 150476, 196830, 185807, 54 },
            { 158056, 133100, 166375, 159635, 207968, 196322, 55 },
            { 165079, 140492, 175616, 169159, 219520, 210739, 56 },
            { 172229, 148154, 185193, 179056, 231491, 222231, 57 },
            { 179503, 156089, 195112, 189334, 243890, 238036, 58 },
            { 186894, 164303, 205379, 199999, 256723, 250562, 59 },
            { 194400, 172800, 216000, 211060, 270000, 267840, 60 },
            { 202013, 181584, 226981, 222522, 283726, 281456, 61 },
            { 209728, 190662, 238328, 234393, 297910, 300293, 62 },
            { 217540, 200037, 250047, 246681, 312558, 315059, 63 },
            { 225443, 209715, 262144, 259392, 327680, 335544, 64 },
            { 233431, 219700, 274625, 272535, 343281, 351520, 65 },
            { 241496, 229996, 287496, 286115, 359370, 373744, 66 },
            { 249633, 240610, 300763, 300140, 375953, 390991, 67 },
            { 257834, 251545, 314432, 314618, 393040, 415050, 68 },
            { 267406, 262807, 328509, 329555, 410636, 433631, 69 },
            { 276458, 274400, 343000, 344960, 428750, 459620, 70 },
            { 286328, 286328, 357911, 360838, 447388, 479600, 71 },
            { 296358, 298598, 373248, 377197, 466560, 507617, 72 },
            { 305767, 311213, 389017, 394045, 486271, 529063, 73 },
            { 316074, 324179, 405224, 411388, 506530, 559209, 74 },
            { 326531, 337500, 421875, 429235, 527343, 582187, 75 },
            { 336255, 351180, 438976, 447591, 548720, 614566, 76 },
            { 346965, 365226, 456533, 466464, 570666, 639146, 77 },
            { 357812, 379641, 474552, 485862, 593190, 673863, 78 },
            { 367807, 394431, 493039, 505791, 616298, 700115, 79 },
            { 378880, 409600, 512000, 526260, 640000, 737280, 80 },
            { 390077, 425152, 531441, 547274, 664301, 765275, 81 },
            { 400293, 441094, 551368, 568841, 689210, 804997, 82 },
            { 411686, 457429, 571787, 590969, 714733, 834809, 83 },
            { 423190, 474163, 592704, 613664, 740880, 877201, 84 },
            { 433572, 491300, 614125, 636935, 767656, 908905, 85 },
            { 445239, 508844, 636056, 660787, 795070, 954084, 86 },
            { 457001, 526802, 658503, 685228, 823128, 987754, 87 },
            { 467489, 545177, 681472, 710266, 851840, 1035837, 88 },
            { 479378, 563975, 704969, 735907, 881211, 1071552, 89 },
            { 491346, 583200, 729000, 762160, 911250, 1122660, 90 },
            { 501878, 602856, 753571, 789030, 941963, 1160499, 91 },
            { 513934, 622950, 778688, 816525, 973360, 1214753, 92 },
            { 526049, 643485, 804357, 844653, 1005446, 1254796, 93 },
            { 536557, 664467, 830584, 873420, 1038230, 1312322, 94 },
            { 548720, 685900, 857375, 902835, 1071718, 1354652, 95 },
            { 560922, 707788, 884736, 932903, 1105920, 1415577, 96 },
            { 571333, 730138, 912673, 963632, 1140841, 1460276, 97 },
            { 583539, 752953, 941192, 995030, 1176490, 1524731, 98 },
            { 591882, 776239, 970299, 1027103, 1212873, 1571884, 99 },
            { 600000, 800000, 1000000, 1059860, 1250000, 1640000, 100 } };
    //@formatter:on
    // cache these in tables, for easier lookup.
    public static int[]          maxXPs = { 800000, 1000000, 1059860, 1250000, 600000, 1640000 };

    public static int computeCatchRate(IPokemob pokemob, double cubeBonus)
    {
        return computeCatchRate(pokemob, cubeBonus, 0);
    }

    public static int computeCatchRate(IPokemob pokemob, double cubeBonus, int cubeBonus2)
    {
        float HPmax = pokemob.getEntity().getMaxHealth();
        Random rand = new Random();
        float HP = pokemob.getEntity().getHealth();
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

        double a = getCatchRate(HPmax, HP, catchRate, cubeBonus, statusBonus) + cubeBonus2;

        if (a > 255) { return 5; }
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

    public static int computeCatchRate(IPokemob pokemob, ResourceLocation pokecubeId)
    {
        double cubeBonus = 0;
        int additionalBonus = 0;
        Item cube = PokecubeItems.getFilledCube(pokecubeId);
        if (cube instanceof IPokecube)
        {
            cubeBonus = ((IPokecube) cube).getCaptureModifier(pokemob, pokecubeId);
        }
        if (IPokecube.BEHAVIORS.containsKey(pokecubeId))
            additionalBonus = IPokecube.BEHAVIORS.getValue(pokecubeId).getAdditionalBonus(pokemob);
        return computeCatchRate(pokemob, cubeBonus, additionalBonus);
    }

    public static int countPokemon(Vector3 location, World world, double distance, PokedexEntry entry)
    {
        int ret = 0;
        List<EntityLiving> list = world.getEntitiesWithinAABB(EntityLiving.class,
                location.getAABB().grow(distance, distance, distance));
        for (EntityLiving o : list)
        {
            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null)
            {
                if (mob.getPokedexEntry() == entry)
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
                location.getAABB().grow(distance, distance, distance));
        for (EntityLiving o : list)
        {
            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null)
            {
                if (mob.getPokedexEntry().isType(type))
                {
                    ret++;
                }
            }
        }
        return ret;
    }

    public static int countPokemon(World world, Vector3 location, double radius)
    {
        AxisAlignedBB box = location.getAABB();
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class,
                box.grow(radius, radius, radius));
        int num = 0;
        for (EntityLivingBase o : list)
        {
            IPokemob mob = CapabilityPokemob.getPokemobFor(o);
            if (mob != null) num++;
        }
        return num;
    }

    public static double getCatchRate(float hPmax, float hP, float catchRate, double cubeBonus, double statusBonus)
    {
        return ((3D * hPmax - 2D * hP) * catchRate * cubeBonus * statusBonus) / (3D * hPmax);
    }

    public static int getExp(float coef, int baseXP, int level)
    {
        return MathHelper.floor(coef * baseXP * level / 7F);
    }

    public static int getHealedPokemobSerialization()
    {
        return PokecubeMod.MAX_DAMAGE - PokecubeMod.FULL_HEALTH;
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

    private static int getLevelFromTable(int index, int exp)
    {
        int level = 100;
        for (int i = 0; i < 99; i++)
        {
            if (expMap[i][index] <= exp && expMap[i + 1][index] > exp)
            {
                level = expMap[i][6];
                break;
            }
        }
        return level;
    }

    public static Entity getPointedEntity(Entity entity, double distance, Predicate<Entity> selector)
    {
        Vec3d vec3 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        Vector3 loc = getPointedLocation(entity, distance);
        if (loc != null)
        {
            distance = loc.distanceTo(Vector3.getNewVector().set(vec3));
        }
        double d0 = distance;
        Vec3d vec31 = entity.getLook(0);
        Vec3d vec32 = vec3.addVector(vec31.x * d0, vec31.y * d0, vec31.z * d0);
        Entity pointedEntity = null;

        Predicate<Entity> predicate = Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            @Override
            public boolean apply(Entity entity)
            {
                return entity.canBeCollidedWith();
            }
        });
        if (selector != null) predicate = Predicates.and(predicate, selector);
        float f = 0.5F;
        List<Entity> list = entity.getEntityWorld().getEntitiesInAABBexcluding(entity,
                entity.getEntityBoundingBox().expand(vec31.x * d0, vec31.y * d0, vec31.z * d0).grow(f, f, f),
                predicate);
        double d2 = distance;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = list.get(j);
            float f1 = 1f;
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
            RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

            if (axisalignedbb.contains(vec3))
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
                    if (entity1 == entity.getRidingEntity() && !entity.canRiderInteract())
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

    public static Entity getPointedEntity(Entity entity, double distance)
    {
        return getPointedEntity(entity, distance, null);
    }

    public static Vector3 getPointedLocation(Entity entity, double distance)
    {
        Vec3d vec3 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        double d0 = distance;
        Vec3d vec31 = entity.getLook(0);
        Vec3d vec32 = vec3.addVector(vec31.x * d0, vec31.y * d0, vec31.z * d0);
        RayTraceResult result = entity.getEntityWorld().rayTraceBlocks(vec3, vec32, false, true, false);
        if (result == null || result.hitVec == null) return null;
        Vector3 vec = Vector3.getNewVector().set(result.getBlockPos()).add(0.5, 0.5, 0.5);
        return vec;
    }

    public static int getPower(String move, IPokemob user, Entity target)
    {
        Move_Base attack = MovesUtils.getMoveFromName(move);
        int pwr = attack.getPWR(user, target);
        IPokemob mob = CapabilityPokemob.getPokemobFor(target);
        if (mob != null)
        {
            pwr *= PokeType.getAttackEfficiency(attack.getType(user), mob.getType1(), mob.getType2());
        }
        return pwr;
    }

    public static byte getRandomIV(Random random)
    {
        return (byte) random.nextInt(32);
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
        if (random.nextInt(255) >= baseValue) { return IPokemob.MALE; }
        return IPokemob.FEMALE;
    }

    public static int getType(String name)
    {
        name = name.toLowerCase(java.util.Locale.ENGLISH).trim();
        if (name.equalsIgnoreCase("erratic")) return 4;
        if (name.equalsIgnoreCase("fast")) return 0;
        if (name.equalsIgnoreCase("medium fast")) return 1;
        if (name.equalsIgnoreCase("medium slow")) return 2;
        if (name.equalsIgnoreCase("slow")) return 3;
        if (name.equalsIgnoreCase("fluctuating")) return 5;
        throw new IllegalArgumentException("Error parsing " + name);
    }

    public static boolean hasMove(String move, IPokemob mob)
    {
        for (String s : mob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(move)) return true;
        }
        return false;
    }

    public static boolean isAnyPlayerInRange(double rangeHorizontal, double rangeVertical, Entity entity)
    {
        return isAnyPlayerInRange(rangeHorizontal, rangeVertical, entity.getEntityWorld(),
                Vector3.getNewVector().set(entity));
    }

    public static boolean isAnyPlayerInRange(double rangeHorizontal, double rangeVertical, World world,
            Vector3 location)
    {
        double dhm = rangeHorizontal * rangeHorizontal;
        double dvm = rangeVertical * rangeVertical;
        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            EntityPlayer entityplayer = world.playerEntities.get(i);
            if (EntitySelectors.NOT_SPECTATING.apply(entityplayer))
            {
                double d0 = entityplayer.posX - location.x;
                double d1 = entityplayer.posZ - location.z;
                double d2 = entityplayer.posY - location.y;
                double dh = d0 * d0 + d1 * d1;
                double dv = d2 * d2;
                if (dh < dhm && dv < dvm) { return true; }
            }
        }
        return false;
    }

    public static boolean isAnyPlayerInRange(double range, Entity entity)
    {
        return entity.getEntityWorld().isAnyPlayerWithinRangeAt(entity.posX, entity.posY, entity.posZ, range);
    }

    public static boolean isStack(ItemStack stack, String oredict)
    {
        List<ItemStack> ores = OreDictionary.getOres(oredict);
        for (ItemStack ore : ores)
            if (isSameStack(stack, ore, true)) return true;
        return false;
    }

    public static boolean isSameStack(ItemStack a, ItemStack b)
    {
        return isSameStack(a, b, false);
    }

    public static boolean isSameStack(ItemStack a, ItemStack b, boolean strict)
    {
        if (!CompatWrapper.isValid(a) || !CompatWrapper.isValid(b)) return false;
        int[] aID = OreDictionary.getOreIDs(a);
        int[] bID = OreDictionary.getOreIDs(b);
        boolean check = a.getItem() == b.getItem();
        if (!check && !strict)
        {
            outer:
            for (int i : aID)
            {
                for (int i1 : bID)
                {
                    if (i == i1)
                    {
                        check = true;
                        break outer;
                    }
                }
            }
        }
        if (!check) { return false; }
        check = (!a.isItemStackDamageable() && a.getItemDamage() != b.getItemDamage());
        if (!a.isItemStackDamageable() && (a.getItemDamage() == OreDictionary.WILDCARD_VALUE
                || b.getItemDamage() == OreDictionary.WILDCARD_VALUE))
            check = false;
        if (check) return false;
        NBTBase tag;
        if (a.hasTagCompound() && ((tag = a.getTagCompound().getTag("ForgeCaps")) != null) && tag.hasNoTags())
        {
            a.getTagCompound().removeTag("ForgeCaps");
        }
        if (b.hasTagCompound() && ((tag = b.getTagCompound().getTag("ForgeCaps")) != null) && tag.hasNoTags())
        {
            b.getTagCompound().removeTag("ForgeCaps");
        }
        return ItemStack.areItemStackTagsEqual(a, b);
    }

    public static int levelToXp(int type, int level)
    {
        level = Math.min(100, level);
        level = Math.max(1, level);
        int index = type;
        switch (type)
        {
        case 4:
            index = 0;
            break;
        case 5:
            index = 5;
            break;
        default:
            index++;
        }
        return expMap[level - 1][index];
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

    public static int xpToLevel(int type, int exp)
    {
        int index = type;
        switch (type)
        {
        case 4:
            index = 0;
            break;
        case 5:
            index = 5;
            break;
        default:
            index++;
        }
        return getLevelFromTable(index, exp);
    }

    public Tools()
    {
    }

    public static ItemStack getStack(Map<QName, String> values)
    {
        int meta = -1;
        String id = "";
        int size = 1;
        boolean resource = false;
        String tag = "";

        for (QName key : values.keySet())
        {
            if (key.toString().equals("id"))
            {
                id = values.get(key);
            }
            else if (key.toString().equals("n"))
            {
                size = Integer.parseInt(values.get(key));
            }
            else if (key.toString().equals("d"))
            {
                meta = Integer.parseInt(values.get(key));
            }
            else if (key.toString().equals("tag"))
            {
                tag = values.get(key);
            }
        }
        if (id.isEmpty()) return ItemStack.EMPTY;
        resource = id.contains(":");
        ItemStack stack = ItemStack.EMPTY;
        Item item = null;
        if (resource)
        {
            item = Item.REGISTRY.getObject(new ResourceLocation(id));
        }
        else stack = PokecubeItems.getStack(id, false);
        if (!stack.isEmpty()) item = stack.getItem();
        if (item == null) return ItemStack.EMPTY;
        if (meta == -1) meta = 0;
        if (stack.isEmpty()) stack = new ItemStack(item, 1, meta);
        stack.setCount(size);
        if (!tag.isEmpty())
        {
            try
            {
                stack.setTagCompound(JsonToNBT.getTagFromJson(tag));
            }
            catch (NBTException e)
            {
                e.printStackTrace();
            }
        }
        return stack;
    }

    public static void giveItem(EntityPlayer entityplayer, ItemStack itemstack)
    {
        boolean flag = entityplayer.inventory.addItemStackToInventory(itemstack);
        if (flag)
        {
            entityplayer.getEntityWorld().playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY,
                    entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((entityplayer.getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.inventoryContainer.detectAndSendChanges();
        }
        if (!flag)
        {
            EntityItem entityitem = entityplayer.dropItem(itemstack, false);
            if (entityitem != null)
            {
                entityitem.setNoPickupDelay();
                entityitem.setOwner(entityplayer.getName());
            }
        }
    }
}
