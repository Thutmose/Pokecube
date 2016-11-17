package pokecube.core.events.handlers;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.MoveUse;
import pokecube.core.events.MoveUse.MoveWorldAction;
import pokecube.core.handlers.HeldItemHandler;
import pokecube.core.events.StatusEffectEvent;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class MoveEventsHandler
{
    public static int WATERSTRONG    = 100;
    public static int FIRESTRONG     = 100;
    public static int ELECTRICSTRONG = 100;

    public static boolean attemptSmelt(IPokemob attacker, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, location.getAABB().expandXyz(1));
        if (!items.isEmpty())
        {
            boolean smelt = false;
            for (int i = 0; i < items.size(); i++)
            {
                EntityItem item = items.get(i);
                ItemStack stack = item.getEntityItem();
                int num = stack.stackSize;
                ItemStack newstack = FurnaceRecipes.instance().getSmeltingResult(stack);
                if (newstack != null)
                {
                    newstack = newstack.copy();
                    newstack.stackSize = num;
                    int i1 = num;
                    float f = FurnaceRecipes.instance().getSmeltingExperience(newstack);
                    if (f == 0.0F)
                    {
                        i1 = 0;
                    }
                    else if (f < 1.0F)
                    {
                        int j = MathHelper.floor_float(i1 * f);
                        if (j < MathHelper.ceiling_float_int(i1 * f) && Math.random() < i1 * f - j)
                        {
                            ++j;
                        }

                        i1 = j;
                    }
                    f = i1;
                    while (i1 > 0)
                    {
                        int k = EntityXPOrb.getXPSplit(i1);
                        i1 -= k;
                        world.spawnEntityInWorld(
                                new EntityXPOrb(world, location.x, location.y + 1.5D, location.z + 0.5D, k));
                    }
                    int hunger = PokecubeCore.core.getConfig().baseSmeltingHunger * num;
                    hunger = (int) Math.max(1, hunger / (float) attacker.getLevel());
                    if (f > 0) hunger *= f;
                    ((IHungrymob) attacker).setHungerTime(((IHungrymob) attacker).getHungerTime() + hunger);
                    item.setEntityItemStack(newstack);
                    smelt = true;
                }
            }
            return smelt;
        }
        return false;
    }

    public static boolean doDefaultFire(IPokemob attacker, Move_Base move, Vector3 location)
    {
        if (move.getPWR() <= 0) return false;
        World world = ((Entity) attacker).getEntityWorld();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        IBlockState state = location.getBlockState(world);
        Vector3 prevBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).norm().addTo(location);
        IBlockState prevState = prevBlock.getBlockState(world);
        int flamNext = nextState.getBlock().getFlammability(world, nextBlock.getPos(), EnumFacing.UP);// TODO
        if (state.getMaterial().isReplaceable() && flamNext != 0)
        {
            location.setBlock(world, Blocks.FIRE);
        }
        else if (prevState.getMaterial().isReplaceable()
                && state.getBlock().getFlammability(world, location.getPos(), EnumFacing.UP) != 0)
        {
            prevBlock.setBlock(world, Blocks.FIRE);
        }
        if (move.getPWR() < FIRESTRONG) { return attemptSmelt(attacker, location); }
        Block block = state.getBlock();
        if (block == Blocks.OBSIDIAN)
        {
            location.setBlock(world, Blocks.LAVA);
            return true;
        }
        else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.OBSIDIAN)
        {
            nextBlock.setBlock(world, Blocks.LAVA);
            return true;
        }
        return attemptSmelt(attacker, location);
    }

    public static boolean doDefaultElectric(IPokemob attacker, Move_Base move, Vector3 location)
    {
        if (move.getPWR() < ELECTRICSTRONG) { return false; }

        World world = ((Entity) attacker).getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        if (block == Blocks.SAND)
        {
            location.setBlock(world, Blocks.GLASS);
            return true;
        }
        else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.SAND)
        {
            nextBlock.setBlock(world, Blocks.GLASS);
            return true;
        }
        return false;
    }

    public static boolean doDefaultIce(IPokemob attacker, Move_Base move, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        BlockPos pos = location.getPos();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        if (block.isAir(state, world, location.getPos()))
        {
            if (location.offset(EnumFacing.DOWN).getBlockState(world).isNormalCube())
            {
                try
                {
                    world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState(), 2);
                    return true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (block == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0)
        {
            location.setBlock(world, Blocks.ICE.getDefaultState());
            return true;
        }
        else if (block.isReplaceable(world, pos))
        {
            if (location.offset(EnumFacing.DOWN).getBlockState(world).isNormalCube())
                location.setBlock(world, Blocks.SNOW_LAYER.getDefaultState());
            return true;
        }
        else if (world.isAirBlock(pos.up()) && state.isNormalCube())
        {
            world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState());
        }
        return false;
    }

    public static boolean doDefaultWater(IPokemob attacker, Move_Base move, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Vector3 prevBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).norm().addTo(location);
        IBlockState prevState = prevBlock.getBlockState(world);
        if (state.getBlock() == Blocks.FIRE)
        {
            location.setAir(world);
        }
        else if (prevState.getBlock() == Blocks.FIRE)
        {
            prevBlock.setAir(world);
        }
        if (move.getPWR() < WATERSTRONG) { return false; }
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        if (move.getPWR() >= WATERSTRONG)
        {
            if (block == Blocks.LAVA)
            {
                location.setBlock(world, Blocks.OBSIDIAN);
                return true;
            }
            else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.LAVA)
            {
                nextBlock.setBlock(world, Blocks.OBSIDIAN);
                return true;
            }
        }
        boolean done = false;
        if (nextState.getProperties().containsKey(BlockFarmland.MOISTURE))
        {
            nextBlock.setBlock(world, nextState.withProperty(BlockFarmland.MOISTURE, 7));
            done = true;
        }
        if (state.getProperties().containsKey(BlockFarmland.MOISTURE))
        {
            location.setBlock(world, state.withProperty(BlockFarmland.MOISTURE, 7));
            done = true;
        }
        return done;
    }

    private static class DefaultAction implements IMoveAction
    {
        Move_Base move;

        public DefaultAction(Move_Base move)
        {
            this.move = move;
        }

        @Override
        public boolean applyEffect(IPokemob attacker, Vector3 location)
        {
            if (attacker.getPokemonOwner() instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) attacker.getPokemonOwner();
                BreakEvent evt2 = new BreakEvent(player.getEntityWorld(), location.getPos(),
                        location.getBlockState(player.getEntityWorld()), player);
                MinecraftForge.EVENT_BUS.post(evt2);
                if (evt2.isCanceled()) return false;
            }
            if (move.getType(attacker) == PokeType.water) return doDefaultWater(attacker, move, location);
            if (move.getType(attacker) == PokeType.ice
                    && (move.move.attackCategory & IMoveConstants.CATEGORY_DISTANCE) > 0
                    && move.move.power > 0) { return doDefaultIce(attacker, move, location); }
            if (move.getType(attacker) == PokeType.electric)
            {
                doDefaultElectric(attacker, move, location);
            }
            if (move.getType(attacker) == PokeType.fire) { return doDefaultFire(attacker, move, location); }
            return false;
        }

        @Override
        public String getMoveName()
        {
            return move.name;
        }
    }

    private static MoveEventsHandler INSTANCE;

    public static MoveEventsHandler getInstance()
    {
        return INSTANCE == null ? INSTANCE = new MoveEventsHandler() : INSTANCE;
    }

    public static void register(IMoveAction move)
    {
        getInstance().actionMap.put(move.getMoveName(), move);
    }

    Map<String, IMoveAction> actionMap = Maps.newHashMap();

    private MoveEventsHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(MoveWorldAction.OnAction evt)
    {
        IPokemob attacker = evt.getUser();
        Vector3 location = evt.getLocation();
        Move_Base move = evt.getMove();
        IMoveAction action = actionMap.get(move.name);
        if (action == null)
        {
            register(action = new DefaultAction(move));
        }
        action.applyEffect(attacker, location);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(StatusEffectEvent evt)
    {
        byte status = evt.getStatus();
        EntityLiving entity = (EntityLiving) evt.getEntity();
        IPokemob pokemob = evt.getPokemob();
        short timer = pokemob.getStatusTimer();
        if (status == IMoveConstants.STATUS_BRN)
        {
            entity.setFire(1);
            entity.attackEntityFrom(DamageSource.causeMobDamage(entity), entity.getMaxHealth() / 16f);
        }
        if (status == IMoveConstants.STATUS_FRZ)
        {
            if (Math.random() > 0.9)
            {
                pokemob.healStatus();
            }
        }
        if (status == IMoveConstants.STATUS_PSN)
        {
            entity.attackEntityFrom(DamageSource.causeMobDamage(entity), entity.getMaxHealth() / 8f);
            spawnPoisonParticle(entity);

        }
        if (status == IMoveConstants.STATUS_PSN2)
        {
            entity.attackEntityFrom(DamageSource.causeMobDamage(entity),
                    (pokemob.getMoveStats().TOXIC_COUNTER + 1) * entity.getMaxHealth() / 16f);
            spawnPoisonParticle(entity);
            spawnPoisonParticle(entity);
            pokemob.getMoveStats().TOXIC_COUNTER++;
        }
        else
        {
            pokemob.getMoveStats().TOXIC_COUNTER = 0;
        }
        if (status == IMoveConstants.STATUS_SLP)
        {
            if (Math.random() > 0.9 || timer <= 0)
            {
                pokemob.healStatus();
            }
            else
            {
                spawnSleepParticle(entity);
            }

        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(MoveUse.DuringUse.Pre evt)
    {
        MovePacket move = evt.getPacket();
        Move_Base attack = move.getMove();
        boolean user = evt.isFromUser();
        IPokemob attacker = move.attacker;
        Entity attacked = move.attacked;
        IPokemob target = null;
        if (attacked instanceof IPokemob) target = (IPokemob) attacked;
        IPokemob applied = user ? attacker : target;
        if (applied == null) return;
        if (!user)
        {
            ((Entity) applied).getEntityData().setString("lastMoveHitBy", move.attack);
            applied.setPokemonAIState(IMoveConstants.NOITEMUSE, false);
        }
        if (target != null && target.getMoveStats().substituteHP > 0 && !user)
        {
            float damage = MovesUtils.getAttackStrength(attacker, (IPokemob) attacked, move.getMove().getCategory(attacker),
                    move.PWR, move);
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.substitute.absorb", "green");
            target.displayMessageToOwner(mess);
            mess = CommandTools.makeTranslatedMessage("pokemob.substitute.absorb", "red");
            attacker.displayMessageToOwner(mess);
            target.getMoveStats().substituteHP -= damage;
            if (target.getMoveStats().substituteHP < 0)
            {
                mess = CommandTools.makeTranslatedMessage("pokemob.substitute.break", "red");
                target.displayMessageToOwner(mess);
                mess = CommandTools.makeTranslatedMessage("pokemob.substitute.break", "green");
                attacker.displayMessageToOwner(mess);
            }
            move.failed = true;
            move.PWR = 0;
            move.changeAddition = 0;
            move.statusChange = 0;
        }

        if (user && attack.getName().equals(IMoveNames.MOVE_SUBSTITUTE))
        {
            applied.getMoveStats().substituteHP = ((EntityLivingBase) applied).getMaxHealth() / 4;
        }

        if (((EntityLivingBase) applied).getHeldItemMainhand() != null)
        {
            HeldItemHandler.processHeldItemUse(move, applied, ((EntityLivingBase) applied).getHeldItemMainhand());
        }

        if (applied.getAbility() != null)
        {
            applied.getAbility().onMoveUse(applied, move);
        }

        if (attack.getName().equals(IMoveNames.MOVE_FALSESWIPE))
        {
            move.noFaint = true;
        }

        if (attack.getName().equals(IMoveNames.MOVE_PROTECT)
                || attack.getName().equals(IMoveNames.MOVE_DETECT) && !applied.getMoveStats().blocked)
        {
            applied.getMoveStats().blockTimer = 30;
            applied.getMoveStats().blocked = true;
            applied.getMoveStats().BLOCKCOUNTER++;
        }
        boolean blockMove = false;

        for (String s : MoveEntry.protectionMoves)
            if (s.equals(move.attack))
            {
                blockMove = true;
                break;
            }

        if (move.attacker == this && !blockMove && applied.getMoveStats().blocked)
        {
            applied.getMoveStats().blocked = false;
            applied.getMoveStats().blockTimer = 0;
            applied.getMoveStats().BLOCKCOUNTER = 0;
        }

        boolean unblockable = false;
        for (String s : MoveEntry.unBlockableMoves)
            if (s.equals(move.attack))
            {
                unblockable = true;
                System.out.println("Unblockable");
                break;
            }

        if (applied.getMoveStats().blocked && move.attacked != move.attacker && !unblockable)
        {
            float count = Math.min(0, applied.getMoveStats().BLOCKCOUNTER - 1);
            float chance = count != 0 ? Math.max(0.125f, ((1 / (count * 2)))) : 1;
            if (chance > Math.random())
            {
                move.canceled = true;
            }
            else
            {
                move.failed = true;
            }
        }
        if (applied.getMoveStats().BLOCKCOUNTER > 0) applied.getMoveStats().BLOCKCOUNTER--;
    }

    protected void spawnSleepParticle(Entity entity)
    {
        Random rand = new Random();
        Vector3 particleLoc = Vector3.getNewVector();
        Vector3 vel = Vector3.getNewVector();
        for (int i = 0; i < 3; ++i)
        {
            particleLoc.set(entity.posX, entity.posY + 0.5D + rand.nextFloat() * entity.height, entity.posZ);
            PokecubeMod.core.spawnParticle(entity.worldObj, "mobSpell", particleLoc, vel);
        }
    }

    protected void spawnPoisonParticle(Entity entity)
    {
        Random rand = new Random();
        Vector3 particleLoc = Vector3.getNewVector();
        int i = 0xFFFF00FF;
        double d0 = (i >> 16 & 255) / 255.0D;
        double d1 = (i >> 8 & 255) / 255.0D;
        double d2 = (i >> 0 & 255) / 255.0D;
        Vector3 vel = Vector3.getNewVector().set(d0, d1, d2);
        for (i = 0; i < 3; ++i)
        {
            particleLoc.set(entity.posX, entity.posY + 0.5D + rand.nextFloat() * entity.height, entity.posZ);
            PokecubeMod.core.spawnParticle(entity.worldObj, "mobSpell", particleLoc, vel);
        }
    }
}
