/**
 *
 */
package pokecube.core.items;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

/** @author Manchou */
public class ItemPokemobEgg extends ItemMonsterPlacer
{
    static HashMap<PokedexEntry, IPokemob> fakeMobs = new HashMap<PokedexEntry, IPokemob>();

    /** @param par1 */
    public ItemPokemobEgg()
    {
        this.setCreativeTab(null);
    }

    /** Callback for item usage. If the item does something special on right
     * clicking, he will have one of those. Return True if something happen and
     * false if it don't. This is for ITEMS, not BLOCKS */
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote) { return true; }
        Block i = worldIn.getBlockState(pos).getBlock();
        BlockPos newPos = pos.offset(side);
        double d = 0.0D;

        if (side == EnumFacing.UP && i instanceof BlockFence)
        {
            d = 0.5D;
        }
        Vector3 loc = Vector3.getNewVectorFromPool().set(newPos).addTo(hitX, d, hitZ);

        if (dropEgg(worldIn, stack, loc, playerIn) && !playerIn.capabilities.isCreativeMode)
        {
            stack.stackSize--;
        }
        loc.freeVectorFromPool();

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
    {
        int damage = getNumber(par1ItemStack);
        EntityEggInfo entityegginfo = (EntityEggInfo) PokecubeMod.pokemobEggs.get(Integer.valueOf(damage));

        PokedexEntry entry = Database.getEntry(damage);

        if (entry != null)
        {
            int colour = entry.getType1().colour;

            if (par2 == 0)
            {
                return colour;
            }
            else
            {
                colour = entry.getType2().colour;
                return colour;
            }
        }

        if (entityegginfo != null)
        {
            if (par2 == 0)
            {
                return entityegginfo.primaryColor;
            }
            else
            {
                return entityegginfo.secondaryColor;
            }
        }
        else
        {
            return 0xffffff;
        }
    }

    public boolean dropEgg(World world, ItemStack stack, Vector3 location, Entity placer)
    {
        if (!PokecubeMod.pokemobEggs.containsKey(getNumber(stack))) { return false; }

        ItemStack eggItemStack = new ItemStack(PokecubeItems.pokemobEgg, 1, stack.getItemDamage());
        if (stack.hasTagCompound()) eggItemStack.setTagCompound(stack.getTagCompound());
        else eggItemStack.setTagCompound(new NBTTagCompound());

        initStack(placer, eggItemStack);

        Entity entity = new EntityPokemobEgg(world, location.x, location.y, location.z, eggItemStack, placer);
        EggEvent.Place event = new EggEvent.Place(entity);
        MinecraftForge.EVENT_BUS.post(event);
        world.spawnEntityInWorld(entity);
        return entity != null;
    }

    public static void initStack(Entity placer, ItemStack stack)
    {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

        if (placer instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) placer;
            if (!mob.getPokemonOwnerName().equals("")) { return; }
        }
        return;
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

    private static void getGenetics(IPokemob mother, IPokemob father, NBTTagCompound nbt)
    {
        byte[] ivs = getIVs(father.getIVs(), mother.getIVs(), father.getEVs(), mother.getEVs());
        long ivsL = PokecubeSerializer.byteArrayAsLong(ivs);
        nbt.setLong("ivs", ivsL);
        nbt.setByteArray("colour", getColour(father.getColours(), mother.getColours()));
        nbt.setFloat("size", getSize(father.getSize(), mother.getSize()));
        nbt.setByte("nature", getNature(mother.getNature(), father.getNature()));

        int chance = 4096;
        if (mother.isShiny()) chance = chance / 2;
        if (father.isShiny()) chance = chance / 2;

        nbt.setBoolean("shiny", new Random().nextInt(chance) == 0);

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
    }

    private static void initPokemobGenetics(IPokemob mob, NBTTagCompound nbt)
    {

        boolean fixedShiny = nbt.getBoolean("shiny");

        String moveString = nbt.getString("moves");
        String[] moves = moveString.split(";");
        long ivs = nbt.getLong("ivs");

        if (fixedShiny)
        {
            mob.setShiny(true);
        }
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
            //IsDead is set to prevent it notifiying owner of move learning.
            ((Entity)mob).isDead = true;
            for (String s : moves)
            {
                if (s != null && !s.isEmpty()) mob.learn(s);
            }
            ((Entity)mob).isDead = false;
            mob.setColours(nbt.getByteArray("colour"));
            mob.setIVs(PokecubeSerializer.longAsByteArray(ivs));
            mob.setNature(nbt.getByte("nature"));
            mob.setSize(nbt.getFloat("size"));
        }
        

        Vector3 location = Vector3.getNewVectorFromPool().set(mob);
        EntityPlayer player = ((Entity) mob).worldObj.getClosestPlayer(location.x, location.y, location.z, 8);
        if (player == null)
        {
            IPokemob pokemob = (IPokemob) ((Entity) mob).worldObj.findNearestEntityWithinAABB(EntityPokemob.class,
                    location.getAABB().expand(8, 8, 8), (Entity) mob);
            if (pokemob != null && pokemob.getPokemonOwner() instanceof EntityPlayer)
                player = (EntityPlayer) pokemob.getPokemonOwner();
        }
        if (player != null)
        {
            mob.setPokemonOwner(player);
            mob.setPokemonAIState(IPokemob.TAMED, true);
            mob.setPokemonAIState(IPokemob.SITTING, true);
            mob.setPokecubeId(0);
            mob.setHeldItem(null);
        }
    }

    public static String[] getMoves(String motherMoves, String fatherMoves)
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

    public static byte[] getIVs(byte[] fatherIVs, byte[] motherIVs, byte[] fatherEVs, byte[] motherEVs)
    {
        byte[] ret = new byte[6];
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            byte mi = motherIVs[i];
            byte fi = fatherIVs[i];
            byte me = motherEVs[i];
            byte fe = fatherEVs[i];

            byte aE = (byte) (((me + fe + 256) * 31) / 512);

            byte iv = (byte) ((mi + fi) / 2);
            iv = (byte) Math.min((rand.nextInt(iv + 1) + rand.nextInt(iv + 1) + rand.nextInt(aE + 1)), 31);

            ret[i] = iv;
        }

        return ret;
    }

    public static byte[] getColour(byte[] fatherColours, byte[] motherColours)
    {
        byte[] ret = new byte[] { 127, 127, 127 };
        if (fatherColours.length < 3 && motherColours.length < 3) return ret;
        for (int i = 0; i < 3; i++)
        {
            ret[i] = (byte) ((fatherColours[i] + motherColours[i]) / 2);
        }

        return ret;
    }

    public static float getSize(float fatherSize, float motherSize)
    {
        float ret = 1;

        ret = (fatherSize + motherSize) * 0.5f * (1 + 0.075f * (float) (new Random()).nextGaussian());

        return ret;
    }

    public static byte getNature(byte motherNature, byte fatherNature)
    {
        byte ret = 0;
        Random rand = new Random();

        byte[] motherMods = PokeType.statsModFromNature(motherNature);
        byte[] fatherMods = PokeType.statsModFromNature(fatherNature);

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
                if (PokeType.statsModFromNature(i)[pos] > 0 && PokeType.statsModFromNature(i)[neg] < 0)
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
                if (PokeType.statsModFromNature((byte) ((i + start) % 25))[pos] > 0)
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
                if (PokeType.statsModFromNature((byte) ((i + start) % 25))[neg] < 0)
                {
                    ret = (byte) ((i + start) % 25);
                    break;
                }
            }
        }
        else
        {
            ret = rand.nextGaussian() > 0 ? motherNature : fatherNature;
        }

        return ret;

    }

    public static boolean spawn(World world, ItemStack stack, double par2, double par4, double par6)
    {
        int pokedexNb = getNumber(stack);
        if (!PokecubeMod.pokemobEggs.containsKey(Integer.valueOf(pokedexNb))) { return false; }

        EntityLiving entity = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(pokedexNb, world);

        if (entity != null)
        {
            IPokemob mob = ((IPokemob) entity);
            mob.setPokemonAIState(IPokemob.EXITINGCUBE, true);
            entity.setHealth(entity.getMaxHealth());
            int exp = Tools.levelToXp(mob.getExperienceMode(), 1);
            exp = Math.max(1, exp);
            mob.setExp(exp, true, true);
            entity.setLocationAndAngles(par2, par4, par6, world.rand.nextFloat() * 360F, 0.0F);
            if (stack.hasTagCompound())
            {
                if (stack.getTagCompound().hasKey("nestLocation"))
                {
                    int[] nest = stack.getTagCompound().getIntArray("nestLocation");
                    ((IPokemob) mob).setHome(nest[0], nest[1], nest[2], 16);
                    mob.setPokemonAIState(IMoveConstants.EXITINGCUBE, false);
                }
                else
                {
                    initPokemobGenetics(mob, stack.getTagCompound());
                }
            }
            mob.specificSpawnInit();
            world.spawnEntityInWorld(entity);
            entity.setCurrentItemOrArmor(0, null);
            entity.playLivingSound();
        }

        return entity != null;
    }

    public static IPokemob getPokemob(World world, ItemStack stack)
    {
        if (stack == null || stack.getTagCompound() == null
                || !stack.getTagCompound().hasKey("pokemobNumber")) { return null; }
        int number = stack.getTagCompound().getInteger("pokemobNumber");
        IPokemob ret = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(number, world);
        return ret;
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
            pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(number, world);
            fakeMobs.put(entry, pokemob);
            if (pokemob == null) return null;
        }
        location.moveEntity((Entity) pokemob);
        initPokemobGenetics(pokemob, stack.getTagCompound());
        return pokemob;
    }

    public static int getNumber(ItemStack stack)
    {
        if (stack == null || stack.getTagCompound() == null
                || !stack.getTagCompound().hasKey("pokemobNumber")) { return -1; }
        return stack.getTagCompound().getInteger("pokemobNumber");
    }

    public static ItemStack getEggStack(IPokemob pokemob)
    {
        ItemStack eggItemStack = new ItemStack(PokecubeItems.pokemobEgg);
        eggItemStack.setTagCompound(new NBTTagCompound());
        eggItemStack.getTagCompound().setInteger("pokemobNumber", pokemob.getPokedexNb());
        return eggItemStack;
    }

    public static ItemStack getEggStack(int pokedexNb)
    {
        ItemStack eggItemStack = new ItemStack(PokecubeItems.pokemobEgg);
        eggItemStack.setTagCompound(new NBTTagCompound());
        eggItemStack.getTagCompound().setInteger("pokemobNumber", pokedexNb);
        return eggItemStack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack)
    {
        int pokedexNb = getNumber(par1ItemStack);
        String s = null;
        String entityName = PokecubeMod.core.getTranslatedPokenameFromPokedexNumber(pokedexNb);
        PokedexEntry entry = Pokedex.getInstance().getEntry(pokedexNb);

        if (entry != null)
        {
            entityName = entry.getTranslatedName();
            s = StatCollector.translateToLocalFormatted("pokemobEgg.name", entityName).trim();
        }
        else
        {
            s = "Pokemob Egg";
        }

        return s;
    }

}
