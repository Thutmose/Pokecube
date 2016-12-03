/**
 *
 */
package pokecube.core.items.pokemobeggs;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** @author Manchou */
public class ItemPokemobEgg extends Item
{
    public static double                   PLAYERDIST         = 2;
    public static double                   MOBDIST            = 4;
    public static double                   HACHANCE           = 0.1;
    public static double                   SHINYRATE          = 4096;
    public static double                   SHINYMULTI         = 0.5;
    public static String                   epigeneticFunction = "(((m + f + 256) * 31) / 512)";
    private static JEP                     epigeneticParser   = new JEP();
    static HashMap<PokedexEntry, IPokemob> fakeMobs           = new HashMap<PokedexEntry, IPokemob>();

    public static void initJEP()
    {
        epigeneticParser = new JEP();
        epigeneticParser.initFunTab();
        epigeneticParser.addStandardFunctions();
        epigeneticParser.initSymTab(); // clear the contents of the symbol table
        epigeneticParser.addStandardConstants();
        epigeneticParser.addComplex();
        // table
        epigeneticParser.addVariable("f", 0);
        epigeneticParser.addVariable("m", 0);
        epigeneticParser.parseExpression(epigeneticFunction);
    }

    static
    {
        initJEP();
    }

    public static byte[] getColour(int[] fatherColours, int[] motherColours)
    {
        byte[] ret = new byte[] { 127, 127, 127, 127 };
        if (fatherColours.length < 3 && motherColours.length < 3) return ret;
        for (int i = 0; i < 3; i++)
        {
            ret[i] = (byte) (((fatherColours[i] + motherColours[i]) / 2) - 128);
        }
        return ret;
    }

    public static ItemStack getEggStack(int pokedexNb)
    {
        return getEggStack(Database.getEntry(pokedexNb));
    }

    public static ItemStack getEggStack(PokedexEntry entry)
    {
        ItemStack eggItemStack = new ItemStack(PokecubeItems.pokemobEgg);
        eggItemStack.setTagCompound(new NBTTagCompound());
        eggItemStack.getTagCompound().setString("pokemob", entry.getName());
        return eggItemStack;
    }

    public static ItemStack getEggStack(IPokemob pokemob)
    {
        return getEggStack(pokemob.getPokedexEntry());
    }

    public static IPokemob getFakePokemob(World world, Vector3 location, ItemStack stack)
    {
        if (stack == null || stack.getTagCompound() == null
                || !stack.getTagCompound().hasKey("pokemobNumber")) { return null; }
        int number = stack.getTagCompound().getInteger("pokemobNumber");

        PokedexEntry entry = Database.getEntry(number);
        IPokemob pokemob = fakeMobs.get(entry);
        if (pokemob == null)
        {
            pokemob = (IPokemob) PokecubeMod.core.createPokemob(Database.getEntry(number), world);
            if (pokemob == null) return null;
            fakeMobs.put(entry, pokemob);
        }
        location.moveEntity((Entity) pokemob);
        return pokemob;
    }

    private static void getGenetics(IPokemob mother, IPokemob father, NBTTagCompound nbt)
    {
        boolean oldType = false;

        if (!oldType)
        {// TODO
            IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
            IMobGenetics mothers = ((Entity) mother).getCapability(IMobGenetics.GENETICS_CAP, null);
            IMobGenetics fathers = ((Entity) father).getCapability(IMobGenetics.GENETICS_CAP, null);
            GeneticsManager.initEgg(eggs, mothers, fathers);
            NBTBase tag = IMobGenetics.GENETICS_CAP.getStorage().writeNBT(IMobGenetics.GENETICS_CAP, eggs, null);
            nbt.setTag("Genes", tag);
            nbt.setString("motherId", ((Entity) mother).getCachedUniqueIdString());
            return;
        }

        byte[] ivs = getIVs(father.getIVs(), mother.getIVs(), father.getEVs(), mother.getEVs());
        long ivsL = PokecubeSerializer.byteArrayAsLong(ivs);
        nbt.setLong("ivs", ivsL);
        nbt.setByteArray("colour", getColour(((IMobColourable) father).getRGBA(), ((IMobColourable) mother).getRGBA()));
        nbt.setFloat("size", getSize(father.getSize(), mother.getSize()));
        nbt.setByte("nature", getNature(mother.getNature(), father.getNature()));
        nbt.setString("motherId", ((Entity) mother).getCachedUniqueIdString());

        int chance = (int) SHINYRATE;
        if (mother.isShiny()) chance *= SHINYMULTI;
        if (father.isShiny()) chance *= SHINYMULTI;
        if (mother.getOriginalOwnerUUID() != father.getOriginalOwnerUUID()) chance *= SHINYMULTI;

        nbt.setBoolean("shiny", new Random().nextInt(Math.max(1, chance)) == 0);

        String motherMoves = "";
        String fatherMoves = "";
        for (int i = 0; i < 4; i++)
        {
            if (mother.getMove(i) != null)
            {
                motherMoves += mother.getMove(i) + ":";
            }
            if (father.getMove(i) != null)
            {
                fatherMoves += father.getMove(i) + ":";
            }
        }
        String[] move = getMoves(motherMoves, fatherMoves);
        String moveString = "";
        int n = 0;
        for (String s : move)
        {
            if (s != null)
            {
                if (n != 0)
                {
                    moveString += ";";
                }
                moveString += s;
            }
            n++;
        }
        nbt.setString("moves", moveString);
        nbt.setInteger("abilityIndex", getAbility(mother.getAbilityIndex(), father.getAbilityIndex()));
    }

    private static byte[] getIVs(byte[] fatherIVs, byte[] motherIVs, byte[] fatherEVs, byte[] motherEVs)
    {
        byte[] ret = new byte[6];
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            byte mi = motherIVs[i];
            byte fi = fatherIVs[i];
            byte me = motherEVs[i];
            byte fe = fatherEVs[i];
            epigeneticParser.setVarValue("f", fe);
            epigeneticParser.setVarValue("m", me);
            double value = epigeneticParser.getValue();
            if (Double.isNaN(value) || Double.isInfinite(value)) value = 0;
            byte aE = (byte) Math.abs(value);
            byte iv = (byte) ((mi + fi) / 2);
            iv = (byte) Math.min((rand.nextInt(iv + 1) + rand.nextInt(iv + 1) + rand.nextInt(aE + 1)), 31);
            ret[i] = iv;
        }

        return ret;
    }

    private static String[] getMoves(String motherMoves, String fatherMoves)
    {
        String[] ma = motherMoves.split(":");
        String[] fa = fatherMoves.split(":");
        String[] ret = new String[4];
        int index = 0;
        ma:
        for (String s : ma)
        {
            if (s != null && !s.isEmpty())
            {
                for (String s1 : fa)
                {
                    if (s.equals(s1))
                    {
                        ret[index] = s;
                        index++;
                        continue ma;
                    }
                }
            }
        }
        return ret;
    }

    private static byte getNature(Nature nature, Nature nature2)
    {
        byte ret = 0;
        Random rand = new Random();

        byte[] motherMods = nature.getStatsMod();
        byte[] fatherMods = nature2.getStatsMod();

        byte[] sum = new byte[6];
        for (int i = 0; i < 6; i++)
        {
            sum[i] = (byte) (motherMods[i] + fatherMods[i]);
        }
        int pos = 0;
        int start = rand.nextInt(100);
        for (int i = 0; i < 6; i++)
        {
            if (sum[(i + start) % 6] > 0)
            {
                pos = (i + start) % 6;
                break;
            }
        }
        int neg = 0;
        start = rand.nextInt(100);
        for (int i = 0; i < 6; i++)
        {
            if (sum[(i + start) % 6] < 0)
            {
                neg = (i + start) % 6;
                break;
            }
        }
        if (pos != 0 && neg != 0)
        {
            for (byte i = 0; i < 25; i++)
            {
                if (Nature.values()[i].getStatsMod()[pos] > 0 && Nature.values()[i].getStatsMod()[neg] < 0)
                {
                    ret = i;
                    break;
                }
            }
        }
        else if (pos != 0)
        {
            start = rand.nextInt(1000);
            for (byte i = 0; i < 25; i++)
            {
                if (Nature.values()[(byte) ((i + start) % 25)].getStatsMod()[pos] > 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
            }
        }
        else if (neg != 0)
        {
            start = rand.nextInt(1000);
            for (byte i = 0; i < 25; i++)
            {
                if (Nature.values()[(byte) ((i + start) % 25)].getStatsMod()[neg] < 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
            }
        }
        else
        {
            int num = rand.nextInt(5);
            ret = (byte) (num * 6);
        }

        return ret;

    }

    private static int getAbility(int motherIndex, int fatherIndex)
    {
        if (motherIndex == fatherIndex) return Math.random() > HACHANCE ? motherIndex : 2;
        int index = Math.random() > 0.5 ? 0 : 1;
        return Math.random() > HACHANCE ? index : 2;
    }

    public static PokedexEntry getEntry(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || stack.getTagCompound() == null) return null;
        if (stack.getTagCompound().hasKey("pokemobNumber"))
            return Database.getEntry(stack.getTagCompound().getInteger("pokemobNumber"));
        return Database.getEntry(stack.getTagCompound().getString("pokemob"));
    }

    public static IPokemob getPokemob(World world, ItemStack stack)
    {
        PokedexEntry entry = getEntry(stack);
        if (entry == null) return null;
        IPokemob ret = (IPokemob) PokecubeMod.core.createPokemob(entry, world);
        return ret;
    }

    public static float getSize(float fatherSize, float motherSize)
    {
        float ret = 1;
        ret = (fatherSize + motherSize) * 0.5f * (1 + 0.075f * (float) (new Random()).nextGaussian());
        ret = Math.min(Math.max(0.1f, ret), 2);
        return ret;
    }

    public static void initPokemobGenetics(IPokemob mob, NBTTagCompound nbt)
    {
        boolean oldType = false;
        old:
        if (!oldType)
        {// TODO
            if (!nbt.hasKey("Genes")) break old;
            NBTBase genes = nbt.getTag("Genes");
            IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
            IMobGenetics.GENETICS_CAP.getStorage().readNBT(IMobGenetics.GENETICS_CAP, eggs, null, genes);
            GeneticsManager.initFromGenes(eggs, mob);
            return;
        }

        boolean fixedShiny = nbt.getBoolean("shiny");

        String moveString = nbt.getString("moves");
        String[] moves = moveString.split(";");
        long ivs = nbt.getLong("ivs");

        mob.setShiny(fixedShiny);
        if (!nbt.hasKey("ivs"))
        {

        }
        else
        {
            if (moves.length > 0)
            {
                for (int i = 1; i < Math.max(moves.length + 1, 5); i++)
                {
                    mob.setMove(4 - i, null);
                }
            }
            // IsDead is set to prevent it notifiying owner of move learning.
            ((Entity) mob).isDead = true;
            for (String s : moves)
            {
                if (s != null && !s.isEmpty()) mob.learn(s);
            }
            ((Entity) mob).isDead = false;
            byte[] rgba = new byte[4];
            if (nbt.hasKey("colour", 7))
            {
                rgba = nbt.getByteArray("colour");
                if (rgba.length == 4)
                {
                    ((IMobColourable) mob).setRGBA(rgba[0] + 128, rgba[1] + 128, rgba[2] + 128, rgba[3] + 128);
                }
                else if (rgba.length == 3)
                {
                    ((IMobColourable) mob).setRGBA(rgba[0] + 128, rgba[1] + 128, rgba[2] + 128);
                }
            }
            mob.setIVs(PokecubeSerializer.longAsByteArray(ivs));
            mob.setNature(Nature.values()[nbt.getByte("nature")]);
            mob.setSize(nbt.getFloat("size"));
            if (nbt.hasKey("abilityIndex"))
            {
                int index = nbt.getInteger("abilityIndex");
                if (index < 2)
                {
                    mob.setAbilityIndex(index);
                }
                else
                {
                    mob.setToHiddenAbility();
                }
            }
        }

        if (nbt.hasKey("gender"))
        {
            mob.setSexe(nbt.getByte("gender"));
        }

        Vector3 location = Vector3.getNewVector().set(mob);
        EntityPlayer player = ((Entity) mob).getEntityWorld().getClosestPlayer(location.x, location.y, location.z,
                PLAYERDIST, false);
        EntityLivingBase owner = player;
        AxisAlignedBB box = location.getAABB().expand(MOBDIST, MOBDIST, MOBDIST);
        if (owner == null)
        {
            List<EntityLivingBase> list = ((Entity) mob).getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                    box, new Predicate<EntityLivingBase>()
                    {
                        @Override
                        public boolean apply(EntityLivingBase input)
                        {
                            return !(input instanceof EntityPokemobEgg) && !(input instanceof IEntityOwnable)
                                    && !(input instanceof EntityPlayer);
                        }
                    });
            EntityLivingBase closestTo = (EntityLivingBase) mob;
            EntityLivingBase t = null;
            double d0 = Double.MAX_VALUE;

            for (int i = 0; i < list.size(); ++i)
            {
                EntityLivingBase t1 = list.get(i);

                if (t1 != closestTo && EntitySelectors.NOT_SPECTATING.apply(t1))
                {
                    double d1 = closestTo.getDistanceSqToEntity(t1);

                    if (d1 <= d0)
                    {
                        t = t1;
                        d0 = d1;
                    }
                }
            }
            owner = t;
        }
        if (owner == null)
        {
            IPokemob pokemob = (IPokemob) ((Entity) mob).getEntityWorld()
                    .findNearestEntityWithinAABB(EntityPokemob.class, box, (Entity) mob);
            if (pokemob != null && pokemob.getPokemonOwner() instanceof EntityPlayer)
                player = (EntityPlayer) pokemob.getPokemonOwner();
            owner = player;
        }

        if (owner != null)
        {
            mob.setPokemonOwner(owner);
            mob.setPokemonAIState(IMoveConstants.TAMED, true);
            mob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(0)));
            mob.setHeldItem(CompatWrapper.nullStack);
        }
    }

    public static void initStack(Entity mother, IPokemob father, ItemStack stack)
    {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        if (mother instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) mother;
            if (father != null)
            {
                getGenetics(mob, father, stack.getTagCompound());
            }
        }
        return;
    }

    public static boolean spawn(World world, ItemStack stack, double par2, double par4, double par6)
    {
        PokedexEntry entry = getEntry(stack);
        if (!PokecubeMod.pokemobEggs.containsKey(entry.getPokedexNb())) { return false; }

        EntityLiving entity = (EntityLiving) PokecubeMod.core.createPokemob(entry, world);

        if (entity != null)
        {
            IPokemob mob = ((IPokemob) entity);
            mob.setPokemonAIState(IMoveConstants.EXITINGCUBE, true);
            entity.setHealth(entity.getMaxHealth());
            int exp = Tools.levelToXp(mob.getExperienceMode(), 1);
            exp = Math.max(1, exp);
            mob.setForSpawn(exp);
            entity.setLocationAndAngles(par2, par4, par6, world.rand.nextFloat() * 360F, 0.0F);
            if (stack.hasTagCompound())
            {
                if (stack.getTagCompound().hasKey("nestLocation"))
                {
                    int[] nest = stack.getTagCompound().getIntArray("nestLocation");
                    mob.setHome(nest[0], nest[1], nest[2], 16);
                    mob.setPokemonAIState(IMoveConstants.EXITINGCUBE, false);
                }
                else
                {
                    initPokemobGenetics(mob, stack.getTagCompound());
                }
            }
            mob.specificSpawnInit();
            world.spawnEntityInWorld(entity);
            if (mob.getPokemonOwner() != null)
            {
                EntityLivingBase owner = mob.getPokemonOwner();
                owner.addChatMessage(
                        new TextComponentTranslation("pokemob.hatch", mob.getPokemonDisplayName().getFormattedText()));
                if (world.getGameRules().getBoolean("doMobLoot"))
                {
                    world.spawnEntityInWorld(new EntityXPOrb(world, entity.posX, entity.posY, entity.posZ,
                            entity.getRNG().nextInt(7) + 1));
                }
            }
            entity.setHeldItem(EnumHand.MAIN_HAND, CompatWrapper.nullStack);
            entity.playLivingSound();
        }

        return entity != null;
    }

    /** @param par1 */
    public ItemPokemobEgg()
    {
        this.setCreativeTab(null);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        PokedexEntry entry = getEntry(stack);
        if (entry != null) tooltip.add(1, I18n.format("pokemobEggnamed.name", I18n.format(entry.getUnlocalizedName())));
    }

    public boolean dropEgg(World world, ItemStack stack, Vector3 location, Entity placer)
    {
        if (!PokecubeMod.pokemobEggs.containsKey(getEntry(stack).getPokedexNb())) { return false; }
        ItemStack eggItemStack = new ItemStack(PokecubeItems.pokemobEgg, 1, stack.getItemDamage());
        if (stack.hasTagCompound()) eggItemStack.setTagCompound(stack.getTagCompound());
        else eggItemStack.setTagCompound(new NBTTagCompound());
        Entity entity = new EntityPokemobEgg(world, location.x, location.y, location.z, eggItemStack, placer);
        EggEvent.Place event = new EggEvent.Place(entity);
        MinecraftForge.EVENT_BUS.post(event);
        world.spawnEntityInWorld(entity);
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String s = "pokemobEgg";
        return s;
    }

    // 1.11
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }

    // 1.10
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote) { return EnumActionResult.SUCCESS; }
        Block i = worldIn.getBlockState(pos).getBlock();
        BlockPos newPos = pos.offset(side);
        double d = 0.0D;

        if (side == EnumFacing.UP && i instanceof BlockFence)
        {
            d = 0.5D;
        }
        Vector3 loc = Vector3.getNewVector().set(newPos).addTo(hitX, d, hitZ);

        if (dropEgg(worldIn, stack, loc, playerIn) && !playerIn.capabilities.isCreativeMode)
        {
            CompatWrapper.increment(stack, -1);
        }
        return EnumActionResult.SUCCESS;
    }

}
