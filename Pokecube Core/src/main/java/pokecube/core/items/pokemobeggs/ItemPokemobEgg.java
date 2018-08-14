package pokecube.core.items.pokemobeggs;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** @author Manchou */
public class ItemPokemobEgg extends Item
{
    public static double                   PLAYERDIST = 2;
    public static double                   MOBDIST    = 4;
    static HashMap<PokedexEntry, IPokemob> fakeMobs   = new HashMap<PokedexEntry, IPokemob>();

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
        ItemStack stack = getEggStack(pokemob.getPokedexEntry());
        initStack(pokemob.getEntity(), pokemob, stack);
        return stack;
    }

    public static IPokemob getFakePokemob(World world, Vector3 location, ItemStack stack)
    {
        PokedexEntry entry = getEntry(stack);
        IPokemob pokemob = fakeMobs.get(entry);
        if (pokemob == null)
        {
            pokemob = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, world));
            if (pokemob == null) return null;
            fakeMobs.put(entry, pokemob);
        }
        location.moveEntity(pokemob.getEntity());
        return pokemob;
    }

    private static void getGenetics(IPokemob mother, IPokemob father, NBTTagCompound nbt)
    {
        IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
        IMobGenetics mothers = mother.getEntity().getCapability(IMobGenetics.GENETICS_CAP, null);
        IMobGenetics fathers = father.getEntity().getCapability(IMobGenetics.GENETICS_CAP, null);
        GeneticsManager.initEgg(eggs, mothers, fathers);
        NBTBase tag = IMobGenetics.GENETICS_CAP.getStorage().writeNBT(IMobGenetics.GENETICS_CAP, eggs, null);
        nbt.setTag(GeneticsManager.GENES, tag);
        try
        {
            SpeciesGene gene = eggs.getAlleles().get(GeneticsManager.SPECIESGENE).getExpressed();
            SpeciesInfo info = gene.getValue();
            nbt.setString("pokemob", info.entry.getName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        nbt.setString("motherId", mother.getEntity().getCachedUniqueIdString());
        return;
    }

    public static PokedexEntry getEntry(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || stack.getTagCompound() == null) return null;
        if (stack.getTagCompound().hasKey("pokemobNumber"))
            return Database.getEntry(stack.getTagCompound().getInteger("pokemobNumber"));
        genes:
        if (stack.getTagCompound().hasKey(GeneticsManager.GENES))
        {
            NBTBase genes = stack.getTagCompound().getTag(GeneticsManager.GENES);
            IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
            IMobGenetics.GENETICS_CAP.getStorage().readNBT(IMobGenetics.GENETICS_CAP, eggs, null, genes);
            Alleles gene = eggs.getAlleles().get(GeneticsManager.SPECIESGENE);
            if (gene == null) break genes;
            SpeciesInfo info = gene.getExpressed().getValue();
            return info.entry;
        }
        return Database.getEntry(stack.getTagCompound().getString("pokemob"));
    }

    public static IPokemob getPokemob(World world, ItemStack stack)
    {
        PokedexEntry entry = getEntry(stack);
        if (entry == null) return null;
        IPokemob ret = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, world));
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
        mob.setForSpawn(10);
        if (nbt.hasKey(GeneticsManager.GENES))
        {
            NBTBase genes = nbt.getTag(GeneticsManager.GENES);
            IMobGenetics eggs = IMobGenetics.GENETICS_CAP.getDefaultInstance();
            IMobGenetics.GENETICS_CAP.getStorage().readNBT(IMobGenetics.GENETICS_CAP, eggs, null, genes);
            GeneticsManager.initFromGenes(eggs, mob);
        }
        EntityLivingBase owner = imprintOwner(mob);
        Config config = PokecubeCore.core.getConfig();
        // Check permissions
        if (owner instanceof EntityPlayer && (config.permsHatch || config.permsHatchSpecific))
        {
            PokedexEntry entry = mob.getPokedexEntry();
            EntityPlayer player = (EntityPlayer) owner;
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            if (config.permsHatch
                    && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB, context))
                return;
            if (config.permsHatchSpecific
                    && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTSPECIFIC.get(entry), context))
                return;
        }
        if (owner != null)
        {
            mob.setPokemonOwner(owner);
            mob.setGeneralState(GeneralStates.TAMED, true);
            mob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            mob.setHeldItem(ItemStack.EMPTY);
        }
    }

    public static EntityLivingBase imprintOwner(IPokemob mob)
    {
        Vector3 location = Vector3.getNewVector().set(mob.getEntity());
        EntityPlayer player = mob.getEntity().getEntityWorld().getClosestPlayer(location.x, location.y, location.z,
                PLAYERDIST, false);
        EntityLivingBase owner = player;
        AxisAlignedBB box = location.getAABB().grow(MOBDIST, MOBDIST, MOBDIST);
        if (owner == null)
        {
            List<EntityLivingBase> list = mob.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                    box, new Predicate<EntityLivingBase>()
                    {
                        @Override
                        public boolean apply(EntityLivingBase input)
                        {
                            return !(input instanceof EntityPokemobEgg);
                        }
                    });
            EntityLivingBase closestTo = mob.getEntity();
            EntityLivingBase t = null;
            double d0 = Double.MAX_VALUE;

            for (int i = 0; i < list.size(); ++i)
            {
                EntityLivingBase t1 = list.get(i);

                if (t1 != closestTo && EntitySelectors.NOT_SPECTATING.apply(t1))
                {
                    double d1 = closestTo.getDistanceSq(t1);

                    if (d1 <= d0)
                    {
                        t = t1;
                        d0 = d1;
                    }
                }
            }
            owner = t;
        }
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(owner);
        if (owner == null || pokemob != null || owner instanceof IEntityOwnable)
        {
            if (pokemob != null && pokemob.getPokemonOwner() instanceof EntityPlayer)
                player = (EntityPlayer) pokemob.getPokemonOwner();
            else if (owner instanceof IEntityOwnable && ((IEntityOwnable) owner).getOwner() instanceof EntityPlayer)
                player = (EntityPlayer) ((IEntityOwnable) owner).getOwner();
            owner = player;
        }
        return owner;
    }

    public static void initStack(Entity mother, IPokemob father, ItemStack stack)
    {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        IPokemob mob = CapabilityPokemob.getPokemobFor(mother);
        if (mob != null && father != null)
        {
            getGenetics(mob, father, stack.getTagCompound());
        }
    }

    public static boolean spawn(World world, ItemStack stack, double par2, double par4, double par6)
    {
        PokedexEntry entry = getEntry(stack);
        if (!PokecubeMod.pokemobEggs.containsKey(entry.getPokedexNb())) { return false; }

        EntityLiving entity = (EntityLiving) PokecubeMod.core.createPokemob(entry, world);

        if (entity != null)
        {
            IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
            mob.setGeneralState(GeneralStates.EXITINGCUBE, true);
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
                    mob.setGeneralState(GeneralStates.EXITINGCUBE, false);
                }
                else
                {
                    initPokemobGenetics(mob, stack.getTagCompound());
                }
            }
            mob.specificSpawnInit();
            world.spawnEntity(entity);
            if (mob.getPokemonOwner() != null)
            {
                EntityLivingBase owner = mob.getPokemonOwner();
                owner.sendMessage(
                        new TextComponentTranslation("pokemob.hatch", mob.getPokemonDisplayName().getFormattedText()));
                if (world.getGameRules().getBoolean("doMobLoot"))
                {
                    world.spawnEntity(new EntityXPOrb(world, entity.posX, entity.posY, entity.posZ,
                            entity.getRNG().nextInt(7) + 1));
                }
            }
            entity.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            entity.playLivingSound();
        }

        return entity != null;
    }

    /** @param par1 */
    public ItemPokemobEgg()
    {
        this.setCreativeTab(null);
    }

    @Override
    public boolean getShareTag()
    {
        return true;
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced)
    {
        PokedexEntry entry = getEntry(stack);
        if (entry != null)
            tooltip.add(1, I18n.format("item.pokemobegg.named.name", I18n.format(entry.getUnlocalizedName())));
    }

    public boolean dropEgg(World world, ItemStack stack, Vector3 location, Entity placer)
    {
        PokedexEntry entry = getEntry(stack);
        if (entry == null || !PokecubeMod.pokemobEggs.containsKey(entry.getPokedexNb())) { return false; }
        ItemStack eggItemStack = new ItemStack(PokecubeItems.pokemobEgg, 1, stack.getItemDamage());
        if (stack.hasTagCompound()) eggItemStack.setTagCompound(stack.getTagCompound());
        else eggItemStack.setTagCompound(new NBTTagCompound());
        Entity entity = new EntityPokemobEgg(world, location.x, location.y, location.z, eggItemStack, placer);
        EggEvent.Place event = new EggEvent.Place(entity);
        MinecraftForge.EVENT_BUS.post(event);
        world.spawnEntity(entity);
        return true;
    }

    /** This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the EntityItem and will leave it to
     * function normally. Called when the item it placed in a world.
     *
     * @param world
     *            The world object
     * @param location
     *            The EntityItem object, useful for getting the position of the
     *            entity
     * @param itemstack
     *            The current item stack
     * @return A new Entity object to spawn or null */
    @Override
    public Entity createEntity(World world, Entity oldItem, ItemStack itemstack)
    {
        if (hasCustomEntity(itemstack))
        {
            EntityPokemobEgg egg = new EntityPokemobEgg(world, oldItem.posX, oldItem.posY, oldItem.posZ, itemstack,
                    null);
            egg.motionX = oldItem.motionX;
            egg.motionY = oldItem.motionY;
            egg.motionZ = oldItem.motionZ;
            return egg;
        }
        return null;
    }

    /** Determines if this Item has a special entity for when they are in the
     * world. Is called when a EntityItem is spawned in the world, if true and
     * Item#createCustomEntity returns non null, the EntityItem will be
     * destroyed and the new Entity will be added to the world.
     *
     * @param stack
     *            The current item stack
     * @return True of the item has a custom entity, If true,
     *         Item#createCustomEntity will be called */
    @Override
    public boolean hasCustomEntity(ItemStack stack)
    {
        return getEntry(stack) != null;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
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
        ItemStack stack = playerIn.getHeldItem(hand);
        if (dropEgg(worldIn, stack, loc, playerIn) && !playerIn.capabilities.isCreativeMode)
        {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

}