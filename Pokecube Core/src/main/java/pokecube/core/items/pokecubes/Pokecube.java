package pokecube.core.items.pokecubes;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.commands.CommandTools;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class Pokecube extends Item implements IPokecube
{

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
            list.add(I18n.format(EnumDyeColor.byDyeDamage(nbt.getInteger("dye")).getUnlocalizedName()));// TODO
        }
    }

    public Pokecube()
    {
        super();
        this.setHasSubtypes(false);
        setMaxDamage(PokecubeMod.MAX_DAMAGE);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean advanced)
    {
        if (PokecubeManager.isFilled(item))
        {
            NBTTagCompound poketag = item.getTagCompound().getCompoundTag("Pokemob");
            IPokemob pokemob = PokecubeManager.itemToPokemob(item, player.getEntityWorld());

            if (pokemob == null)
            {
                list.add("ERROR");
                return;
            }

            float health = poketag.getFloat("Health");
            float maxHealth = ((EntityLiving) pokemob).getMaxHealth();
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
        }

        if (item.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = PokecubeManager.getSealTag(item);
            displayInformation(nbttagcompound, list);
        }
    }

    @Override
    public double getCaptureModifier(IPokemob mob, int id)
    {
        if (IPokecube.map.containsKey(id)) return IPokecube.map.get(id).getCaptureModifier(mob);
        return 0;
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

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        player.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    /** Called when the player finishes using this Item (E.g. finishes eating.).
     * Not called when the player stops using the Item before the action is
     * complete. */
    @Override
    @Nullable
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        return stack;
    }

    @Override
    /** Called when the player stops using an Item (stops holding the right
     * mouse button). */
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
        if (entityLiving instanceof EntityPlayer && !worldIn.isRemote)
        {
            EntityPlayer player = (EntityPlayer) entityLiving;
            Entity target = Tools.getPointedEntity(player, 32);
            Vector3 direction = Vector3.getNewVector().set(player.getLook(0));
            Vector3 targetLocation = Tools.getPointedLocation(player, 32);

            if (target instanceof EntityPokecube) target = null;
            if (target instanceof IPokemob)
            {
                if (((IPokemob) target).getPokemonOwner() == entityLiving) target = null;
            }

            boolean filled = PokecubeManager.isFilled(stack);
            if (!filled && !(target instanceof IPokemob)) target = null;
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
                            player.inventory.setInventorySlotContents(i, CompatWrapper.nullStack);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count)
    {

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
        int id = PokecubeItems.getCubeId(cube.getItem());
        if (id < 0) return false;
        ItemStack stack = CompatWrapper.copy(cube);
        CompatWrapper.setStackSize(stack, 1);
        entity = new EntityPokecube(world, thrower, stack);
        Vector3 temp = Vector3.getNewVector().set(thrower).add(0, thrower.getEyeHeight(), 0);
        Vector3 temp1 = Vector3.getNewVector().set(thrower.getLookVec()).scalarMultBy(1.5);
        temp.addTo(temp1).moveEntity(entity);
        temp.set(direction.scalarMultBy(power * 10)).setVelocities(entity);
        entity.targetEntity = null;
        entity.targetLocation.clear();

        if (PokecubeManager.isFilled(stack) && !thrower.isSneaking())
        {
            entity.targetLocation.y = -1;
        }

        if (!world.isRemote)
        {
            thrower.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            world.spawnEntityInWorld(entity);
        }
        return true;
    }

    @Override
    public boolean throwPokecubeAt(World world, EntityLivingBase thrower, ItemStack cube, Vector3 targetLocation,
            Entity target)
    {
        EntityPokecube entity = null;
        int id = PokecubeItems.getCubeId(cube.getItem());
        if (id < 0) return false;
        ItemStack stack = CompatWrapper.copy(cube);
        CompatWrapper.setStackSize(stack, 1);
        entity = new EntityPokecube(world, thrower, stack);
        boolean rightclick = target == thrower;
        if (rightclick) target = null;

        if (target instanceof EntityLivingBase || PokecubeManager.isFilled(cube) || thrower.isSneaking()
                || (thrower instanceof FakePlayer))
        {
            if (target instanceof EntityLivingBase) entity.targetEntity = (EntityLivingBase) target;
            if (target == null && targetLocation == null && PokecubeManager.isFilled(cube))
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
                world.spawnEntityInWorld(entity);
            }
        }
        else if (!rightclick) { return false; }
        return true;
    }
}
