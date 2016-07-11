/**
 *
 */
package pokecube.core.moves.templates;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.Thunder;
import pokecube.core.utils.PokeType;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

/** @author Manchou */
public class Move_Basic extends Move_Base implements IMoveConstants
{
    protected static ItemStack createStackedBlock(IBlockState state)
    {
        int i = 0;
        Item item = Item.getItemFromBlock(state.getBlock());

        if (item != null && item.getHasSubtypes())
        {
            i = state.getBlock().getMetaFromState(state);
        }
        return new ItemStack(item, 1, i);
    }

    protected static boolean shouldSilk(IPokemob pokemob)
    {
        if (pokemob.getAbility() == null) return false;
        Ability ability = pokemob.getAbility();
        return pokemob.getLevel() > 90 && ability.toString().equalsIgnoreCase("hypercutter");
    }

    protected static void silkHarvest(IBlockState state, BlockPos pos, World worldIn, EntityPlayer player)
    {
        java.util.ArrayList<ItemStack> items = new java.util.ArrayList<ItemStack>();
        ItemStack itemstack = createStackedBlock(state);

        if (itemstack != null)
        {
            items.add(itemstack);
        }

        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, worldIn.getBlockState(pos),
                0, 1.0f, true, player);
        for (ItemStack stack : items)
        {
            Block.spawnAsEntity(worldIn, pos, stack);
        }
    }

    Vector3 v  = Vector3.getNewVector();

    Vector3 v1 = Vector3.getNewVector();

    /** Constructor for a Pokemob move. <br/>
     * The attack category defines the way the mob will move in order to make
     * its attack.
     *
     * @param name
     *            the English name of the attack, used as identifier and
     *            translation key
     * @param attackCategory
     *            can be either {@link MovesUtils#CATEGORY_CONTACT} or
     *            {@link MovesUtils#CATEGORY_DISTANCE} */
    public Move_Basic(String name)
    {
        super(name);
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked)
    {
        if (attacker.getStatus() == STATUS_SLP)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, STATUS_SLP, false);
            return;
        }

        if (attacker.getStatus() == STATUS_FRZ)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, STATUS_FRZ, false);
            return;
        }

        if (attacker.getStatus() == STATUS_PAR && Math.random() > 0.75)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, STATUS_PAR, false);
            return;
        }

        if ((getAttackCategory() & CATEGORY_CONTACT) != 0)
        {
            if (MovesUtils.contactAttack(attacker, attacked))
            {
                finalAttack(attacker, attacked);
            }
        }
        else if ((getAttackCategory() & CATEGORY_DISTANCE) != 0)
        {
            finalAttack(attacker, attacked);
        }
        else if ((getAttackCategory() & CATEGORY_SELF) != 0)
        {
            doSelfAttack(attacker);
        }
    }

    @Override
    public void attack(IPokemob attacker, Vector3 location)
    {
        List<Entity> targets = new ArrayList<Entity>();

        Entity entity = (Entity) attacker;

        if (!move.notIntercepable)
        {
            Vec3d loc1 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            Vec3d loc2 = new Vec3d(location.x, location.y, location.z);
            RayTraceResult pos = entity.getEntityWorld().rayTraceBlocks(loc1, loc2, false);
            if (pos != null)
            {
                location.set(pos.hitVec);
            }

        }
        if (move.multiTarget)
        {
            targets.addAll(MovesUtils.targetsHit(((Entity) attacker), location));
        }
        else if (!move.notIntercepable)
        {
            targets.add(MovesUtils.targetHit(entity, location));
        }
        else
        {
            List<Entity> subTargets = new ArrayList<Entity>();
            if (subTargets.contains(attacker)) subTargets.remove(attacker);

            targets.addAll(subTargets);
        }

        if ((getAttackCategory() & CATEGORY_SELF) == 0)
        {
            int n = targets.size();
            if (n > 0)
            {
                for (Entity e : targets)
                {
                    if (e != null) finalAttack(attacker, e, true);
                }
            }
            else
            {
                MovesUtils.displayEfficiencyMessages(attacker, null, -1, 0);
            }

        }
        else if ((getAttackCategory() & CATEGORY_SELF) != 0)
        {
            doSelfAttack(attacker);
        }
        doWorldAction(attacker, location);
    }

    @Override
    public boolean doAttack(IPokemob attacker, Entity attacked)
    {
        if (attacked == null && getAttackCategory() != CATEGORY_SELF) return false;

        if (getAttackCategory() == CATEGORY_SELF && this.hasStatModSelf)
        {
            return attacker.getMoveStats().SELFRAISECOUNTER == 0;
        }
        else if (this.hasStatModTarget && getPWR(attacker, attacked) == 0)
            return attacker.getMoveStats().TARGETLOWERCOUNTER == 0;

        return true;
    }

    /** Do anything special for self attacks, usually raising/lowering of stats.
     * 
     * @param mob */
    @Override
    public void doSelfAttack(IPokemob mob)
    {
        if (doAttack(mob, (Entity) mob))
        {
            if (sound != null)
            {
                ((Entity) mob).playSound(sound, 0.5F, 1F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
            }
            MovesUtils.attack(
                    new MovePacket(mob, (Entity) mob, name, move.type, getPWR(), move.crit, (byte) 0, (byte) 0, false));
            postAttack(mob, (Entity) mob, 0, 0);
        }
    }

    @Override
    public void doWorldAction(IPokemob attacker, Vector3 location)
    {
        if (!PokecubeMod.pokemobsDamageBlocks) return;
        World world = ((Entity) attacker).getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        if (getType(attacker) == PokeType.ice && (move.attackCategory & CATEGORY_DISTANCE) > 0 && move.power > 0)
        {
            if (block.isAir(state, world, location.getPos()))
            {
                if (location.offset(EnumFacing.DOWN).getBlockState(world).isNormalCube())
                {
                    try
                    {
                        world.setBlockState(location.getPos(), Blocks.SNOW_LAYER.getDefaultState(), 2);
                    }
                    catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            else if (block == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0)
            {
                location.setBlock(world, Blocks.ICE.getDefaultState());
            }
            else if (block.isReplaceable(world, location.getPos()))
            {
                if (location.offset(EnumFacing.DOWN).getBlockState(world).isNormalCube())
                    location.setBlock(world, Blocks.SNOW_LAYER.getDefaultState());
            }
        }
        int strong = 100;
        if (getType(attacker) == PokeType.water)
        {
            Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                    .addTo(location);
            IBlockState nextState = nextBlock.getBlockState(world);
            if (getPWR() >= strong)
            {
                if (block == Blocks.LAVA)
                {
                    location.setBlock(world, Blocks.OBSIDIAN);
                }
                else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.LAVA)
                {
                    nextBlock.setBlock(world, Blocks.OBSIDIAN);
                }
            }
            if (nextState.getProperties().containsKey(BlockFarmland.MOISTURE))
            {
                nextBlock.setBlock(world, nextState.withProperty(BlockFarmland.MOISTURE, 7));
            }
            if (state.getProperties().containsKey(BlockFarmland.MOISTURE))
            {
                location.setBlock(world, state.withProperty(BlockFarmland.MOISTURE, 7));
            }
        }
        if (getType(attacker) == PokeType.electric && getPWR() >= strong)
        {
            Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                    .addTo(location);
            IBlockState nextState = nextBlock.getBlockState(world);
            if (block == Blocks.SAND)
            {
                location.setBlock(world, Blocks.GLASS);
            }
            else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.SAND)
            {
                nextBlock.setBlock(world, Blocks.GLASS);
            }
        }
        if (getType(attacker) == PokeType.fire && getPWR() >= strong)
        {
            Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                    .addTo(location);
            IBlockState nextState = nextBlock.getBlockState(world);
            if (block == Blocks.OBSIDIAN)
            {
                location.setBlock(world, Blocks.LAVA);
            }
            else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.OBSIDIAN)
            {
                nextBlock.setBlock(world, Blocks.LAVA);
            }
        }
    }

    @Override
    protected void finalAttack(IPokemob attacker, Entity attacked)
    {
        finalAttack(attacker, attacked, true);
    }

    @Override
    protected void finalAttack(IPokemob attacker, Entity attacked, boolean message)
    {
        if (doAttack(attacker, attacked))
        {
            if (getAnimation() instanceof Thunder && attacked != null)
            {
                EntityLightningBolt lightning = new EntityLightningBolt(attacked.getEntityWorld(), 0, 0, 0, false);
                attacked.onStruckByLightning(lightning);
            }
            if (attacked instanceof EntityCreeper)
            {
                EntityCreeper creeper = (EntityCreeper) attacked;
                if (move.type == PokeType.psychic && creeper.getHealth() > 0)
                {
                    creeper.explode();
                }
            }
            if (move.multiTarget && (getAttackCategory() & CATEGORY_SELF) == 0)
            {
                if (sound != null)
                {
                    ((Entity) attacker).playSound(sound, 0.25F, 1f);
                }
                List<EntityLivingBase> hit = MovesUtils.targetsHit(((Entity) attacker),
                        v.set(attacked).addTo(0, attacked.height / 3, 0));
                // notifyClient((Entity) attacker, v.set(attacked), attacked);
                for (Entity e : hit)
                {
                    attacked = e;
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                    {
                        statusChange = move.statusChange;
                    }
                    if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                    {
                        changeAddition = move.change;
                    }
                    int pwr;
                    int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type,
                            pwr = getPWR(attacker, attacked), move.crit, statusChange, changeAddition));

                    postAttack(attacker, attacked, pwr, finalAttackStrength);
                }
            }
            else if ((getAttackCategory() & CATEGORY_SELF) == 0)
            {
                Entity temp = attacked;
                if (!move.notIntercepable)
                {
                    attacked = MovesUtils.targetHit(((Entity) attacker),
                            v.set(attacked).addTo(0, attacked.height / 3, 0));
                }
                if (sound != null)
                {
                    ((Entity) attacker).playSound(sound, 0.5F, 0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
                }
                if (attacked == null) attacked = temp;
                byte statusChange = STATUS_NON;
                byte changeAddition = CHANGE_NONE;
                if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
                {
                    statusChange = move.statusChange;
                }
                if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
                {
                    changeAddition = move.change;
                }
                int pwr;
                int finalAttackStrength = MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type,
                        pwr = getPWR(attacker, attacked), move.crit, statusChange, changeAddition));
                postAttack(attacker, attacked, pwr, finalAttackStrength);
            }
        }
    }

    @Override
    public void applyHungerCost(IPokemob attacker)
    {
        int pp = getPP();
        float relative = (50 - pp) / 30;
        relative = relative * relative;
        IHungrymob mob = (IHungrymob) attacker;
        mob.setHungerTime(mob.getHungerTime() + (int) (relative * 100));
    }

    @Override
    public Move_Base getMove(String name)
    {
        return MovesUtils.getMoveFromName(name);
    }

    @Override
    public boolean isMoveImplemented(String s)
    {
        return MovesUtils.isMoveImplemented(s);
    }

    /** Called after the attack for special post attack treatment.
     * 
     * @param attacker
     * @param attacked
     * @param f
     * @param finalAttackStrength
     *            the number of HPs the attack takes from target */
    @Override
    public void postAttack(IPokemob attacker, Entity attacked, int power, int finalAttackStrength)
    {
    }

    /** Specify the sound this move should play when executed.
     * 
     * @param sound
     *            the string id of the sound to play
     * @return the move */
    @Override
    public Move_Basic setSound(String sound)
    {
        this.sound = new SoundEvent(new ResourceLocation(sound));

        return this;
    }
}
