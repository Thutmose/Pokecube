package pokecube.core.items.pokecubes;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.lib.CompatWrapper;

public class Pokecube extends Item implements IPokecube
{
    public static final Set<Class<? extends EntityLivingBase>> snagblacklist = Sets.newHashSet();

    private static final Predicate<EntityLivingBase>           capturable    = new Predicate<EntityLivingBase>()
                                                                             {
                                                                                 @Override
                                                                                 public boolean test(EntityLivingBase t)
                                                                                 {
                                                                                     if (snagblacklist
                                                                                             .contains(t.getClass()))
                                                                                         return false;
                                                                                     for (Class<? extends EntityLivingBase> claz : snagblacklist)
                                                                                     {
                                                                                         if (claz.isInstance(t))
                                                                                             return false;
                                                                                     }
                                                                                     return true;
                                                                                 }
                                                                             };

    @SideOnly(Side.CLIENT)
    public static void displayInformation(NBTTagCompound nbt, List<String> list)
    {
        boolean flag2 = nbt.getBoolean("Flames");

        if (flag2)
        {
            list.add(I18n.format("item.pokecube.flames"));
        }

        boolean flag3 = nbt.getBoolean("Bubbles");

        if (flag3)
        {
            list.add(I18n.format("item.pokecube.bubbles"));
        }

        boolean flag4 = nbt.getBoolean("Leaves");

        if (flag4)
        {
            list.add(I18n.format("item.pokecube.leaves"));
        }

        boolean flag5 = nbt.hasKey("dye");

        if (flag5)
        {
            // TODO better tooltip for dyes?
            list.add(I18n.format(EnumDyeColor.byDyeDamage(nbt.getInteger("dye")).getUnlocalizedName()));
        }
    }

    public Pokecube()
    {
        super();
        this.setHasSubtypes(false);
        this.setNoRepair();
        setMaxDamage(PokecubeMod.MAX_DAMAGE);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, @Nullable World world, List<String> list, ITooltipFlag advanced)
    {
        if (PokecubeManager.isFilled(item))
        {
            IPokemob pokemob = PokecubeManager.itemToPokemob(item, world);
            if (pokemob == null)
            {
                list.add("ERROR");
                return;
            }

            NBTTagCompound pokeTag = item.getTagCompound().getCompoundTag(TagNames.POKEMOB);

            float health = pokeTag.getFloat("Health");
            float maxHealth = pokemob.getStat(Stats.HP, false);
            int lvlexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
            int exp = pokemob.getExp() - lvlexp;
            int neededexp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1) - lvlexp;
            list.add(I18n.format("pokecube.tooltip.level", pokemob.getLevel()));
            list.add(I18n.format("pokecube.tooltip.health", health, maxHealth));
            list.add(I18n.format("pokecube.tooltip.xp", exp, neededexp));

            if (GuiScreen.isShiftKeyDown())
            {
                String arg = "";
                for (String s : pokemob.getMoves())
                {
                    if (s != null)
                    {
                        arg += I18n.format(MovesUtils.getUnlocalizedMove(s)) + ", ";
                    }
                }
                if (arg.endsWith(", "))
                {
                    arg = arg.substring(0, arg.length() - 2);
                }
                list.add(I18n.format("pokecube.tooltip.moves", arg));
                arg = "";
                for (Byte b : pokemob.getIVs())
                {
                    arg += b + ", ";
                }
                if (arg.endsWith(", "))
                {
                    arg = arg.substring(0, arg.length() - 2);
                }
                list.add(I18n.format("pokecube.tooltip.ivs", arg));
                arg = "";
                for (Byte b : pokemob.getEVs())
                {
                    int n = b + 128;
                    arg += n + ", ";
                }
                if (arg.endsWith(", "))
                {
                    arg = arg.substring(0, arg.length() - 2);
                }
                list.add(I18n.format("pokecube.tooltip.evs", arg));
                list.add(I18n.format("pokecube.tooltip.nature", pokemob.getNature()));
                list.add(I18n.format("pokecube.tooltip.ability", pokemob.getAbility()));
            }
            else list.add(I18n.format("pokecube.tooltip.advanced"));
        }

        if (item.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = PokecubeManager.getSealTag(item);
            displayInformation(nbttagcompound, list);
        }
    }

    @Override
    public double getCaptureModifier(IPokemob mob, ResourceLocation id)
    {
        if (IPokecube.BEHAVIORS.containsKey(id)) return IPokecube.BEHAVIORS.getValue(id).getCaptureModifier(mob);
        return 0;
    }

    @Override
    public double getCaptureModifier(EntityLivingBase mob, ResourceLocation pokecubeId)
    {
        if (pokecubeId.getResourcePath().equals("snag")) return 1;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        return (pokemob != null) ? getCaptureModifier(pokemob, pokecubeId) : 0;
    }

    @Override
    /** returns the action that specifies what animation to play when the items
     * is being used */
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BOW;
    }

    @Override
    /** How long it takes to use or consume an item */
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 2000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        player.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    /** Called when the player stops using an Item (stops holding the right
     * mouse button). */
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
        if (entityLiving instanceof EntityPlayer && !worldIn.isRemote)
        {
            EntityPlayer player = (EntityPlayer) entityLiving;
            com.google.common.base.Predicate<Entity> selector = new com.google.common.base.Predicate<Entity>()
            {
                @Override
                public boolean apply(Entity input)
                {
                    IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
                    if (pokemob == null) return true;
                    return pokemob.getOwner() != player;
                }
            };
            Entity target = Tools.getPointedEntity(player, 32, selector);
            Vector3 direction = Vector3.getNewVector().set(player.getLook(0));
            Vector3 targetLocation = Tools.getPointedLocation(player, 32);

            if (target instanceof EntityPokecube) target = null;
            IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null)
            {
                if (targetMob.getPokemonOwner() == entityLiving) target = null;
            }

            boolean filled = PokecubeManager.isFilled(stack);
            if (!filled && targetMob == null) target = null;
            boolean used = false;
            boolean filledOrSneak = filled || player.isSneaking();
            if (target != null && EntityPokecube.SEEKING)
            {
                used = throwPokecubeAt(worldIn, player, stack, targetLocation, target);
            }
            else if (filledOrSneak || !EntityPokecube.SEEKING)
            {
                float power = (getMaxItemUseDuration(stack) - timeLeft) / (float) 100;
                power = Math.min(1, power);
                used = throwPokecube(worldIn, player, stack, direction, power);
            }
            else
            {
                CommandTools.sendError(player, "pokecube.badaim");
            }
            if (used)
            {
                stack.splitStack(1);
                if (!CompatWrapper.isValid(stack))
                {
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++)
                    {
                        if (player.inventory.getStackInSlot(i) == stack)
                        {
                            player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    // Pokeseal stuff

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    public boolean throwPokecube(World world, EntityLivingBase thrower, ItemStack cube, Vector3 direction, float power)
    {
        EntityPokecube entity = null;
        ResourceLocation id = PokecubeItems.getCubeId(cube.getItem());
        if (id == null || !IPokecube.BEHAVIORS.containsKey(id)) return false;
        ItemStack stack = cube.copy();
        boolean hasMob = PokecubeManager.hasMob(stack);
        Config config = PokecubeCore.core.getConfig();
        // Check permissions
        if (hasMob && (config.permsSendOut || config.permsSendOutSpecific) && thrower instanceof EntityPlayer)
        {
            PokedexEntry entry = PokecubeManager.getPokedexEntry(stack);
            EntityPlayer player = (EntityPlayer) thrower;
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            if (config.permsSendOut
                    && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB, context))
                return false;
            if (config.permsSendOutSpecific
                    && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTSPECIFIC.get(entry), context))
                return false;
        }
        stack.setCount(1);
        entity = new EntityPokecube(world, thrower, stack);
        Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
        Vector3 temp1 = Vector3.getNewVector().set(thrower.getLookVec()).scalarMultBy(1.5);
        temp.addTo(temp1).moveEntity(entity);
        temp.set(direction.scalarMultBy(power * 10)).setVelocities(entity);
        entity.targetEntity = null;
        entity.targetLocation.clear();
        entity.forceSpawn = true;
        if (hasMob && !thrower.isSneaking())
        {
            entity.targetLocation.y = -1;
        }

        if (!world.isRemote)
        {
            thrower.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            world.spawnEntity(entity);
        }
        return true;
    }

    @Override
    public boolean throwPokecubeAt(World world, EntityLivingBase thrower, ItemStack cube, Vector3 targetLocation,
            Entity target)
    {
        EntityPokecube entity = null;
        ResourceLocation id = PokecubeItems.getCubeId(cube.getItem());
        if (id == null || !IPokecube.BEHAVIORS.containsKey(id)) return false;
        ItemStack stack = cube.copy();
        stack.setCount(1);
        entity = new EntityPokecube(world, thrower, stack);
        boolean rightclick = target == thrower;
        if (rightclick) target = null;

        if (target instanceof EntityLivingBase || PokecubeManager.hasMob(cube) || thrower.isSneaking()
                || (thrower instanceof FakePlayer))
        {
            if (target instanceof EntityLivingBase) entity.targetEntity = (EntityLivingBase) target;
            if (target == null && targetLocation == null && PokecubeManager.hasMob(cube))
            {
                targetLocation = Vector3.secondAxisNeg;
            }
            entity.targetLocation.set(targetLocation);
            if (thrower.isSneaking())
            {
                Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
                Vector3 temp1 = Vector3.getNewVector().set(thrower.getLookVec()).scalarMultBy(1.5);

                temp.addTo(temp1).moveEntity(entity);
                temp.clear().setVelocities(entity);
                entity.targetEntity = null;
                entity.targetLocation.clear();
            }

            if (!world.isRemote)
            {
                thrower.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                world.spawnEntity(entity);
            }
        }
        else if (!rightclick) { return false; }
        return true;
    }

    @Override
    public boolean canCapture(EntityLiving hit, ItemStack cube)
    {
        ResourceLocation id = PokecubeItems.getCubeId(cube);
        if (id != null && id.getResourcePath().equals("snag")) { return capturable.test(hit); }
        return CapabilityPokemob.getPokemobFor(hit) != null;
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
        return PokecubeManager.hasMob(stack);
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
            FakePlayer player = PokecubeMod.getFakePlayer(world);
            EntityPokecube cube = new EntityPokecube(world, player, itemstack);
            cube.motionX = cube.motionY = cube.motionZ = 0;
            cube.shootingEntity = null;
            cube.shooter = null;
            Vector3.getNewVector().set(oldItem).moveEntity(cube);
            cube.tilt = -2;
            cube.targetLocation.clear();
            return cube;
        }
        return null;
    }
}
