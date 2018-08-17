package pokecube.core.events.handlers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.MoveUse;
import pokecube.core.events.MoveUse.MoveWorldAction;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.NonPersistantStatusEffect.Effect;
import pokecube.core.interfaces.entity.impl.OngoingMoveEffect;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect.Status;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

public class MoveEventsHandler
{
    public static int WATERSTRONG    = 100;
    public static int FIRESTRONG     = 100;
    public static int ELECTRICSTRONG = 100;

    /** This method should be called before any block setting by any move
     * effects.
     * 
     * @param user
     * @param location
     * @return */
    public static boolean canEffectBlock(IPokemob user, Vector3 location)
    {
        EntityLivingBase owner = user.getPokemonOwner();
        boolean repel = SpawnHandler.checkNoSpawnerInArea(user.getEntity().getEntityWorld(), location.intX(),
                location.intY(), location.intZ());
        if (!(owner instanceof EntityPlayer))
        {
            owner = PokecubeMod.getFakePlayer(user.getEntity().getEntityWorld());
        }
        if (!repel)
        {
            if (!user.getCombatState(CombatStates.ANGRY)) CommandTools.sendError(owner, "pokemob.action.denyrepel");
            return false;
        }
        EntityPlayer player = (EntityPlayer) owner;
        BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(),
                location.getBlockState(player.getEntityWorld()), player);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled())
        {
            TextComponentTranslation message = new TextComponentTranslation("pokemob.createbase.deny.noperms");
            if (!user.getCombatState(CombatStates.ANGRY)) owner.sendMessage(message);
            return false;
        }
        return true;
    }

    public static boolean attemptSmelt(IPokemob attacker, Vector3 location)
    {
        World world = attacker.getEntity().getEntityWorld();
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, location.getAABB().grow(1));
        if (!items.isEmpty())
        {
            boolean smelt = false;
            for (int i = 0; i < items.size(); i++)
            {
                EntityItem item = items.get(i);
                ItemStack stack = item.getItem();
                int num = stack.getCount();
                ItemStack newstack = FurnaceRecipes.instance().getSmeltingResult(stack);
                if (newstack != null)
                {
                    newstack = newstack.copy();
                    newstack.setCount(num);
                    int i1 = num;
                    float f = FurnaceRecipes.instance().getSmeltingExperience(newstack);
                    if (f == 0.0F)
                    {
                        i1 = 0;
                    }
                    else if (f < 1.0F)
                    {
                        int j = MathHelper.floor(i1 * f);
                        if (j < MathHelper.ceil(i1 * f) && Math.random() < i1 * f - j)
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
                        world.spawnEntity(new EntityXPOrb(world, location.x, location.y + 1.5D, location.z + 0.5D, k));
                    }
                    int hunger = PokecubeCore.core.getConfig().baseSmeltingHunger * num;
                    hunger = (int) Math.max(1, hunger / (float) attacker.getLevel());
                    if (f > 0) hunger *= f;
                    attacker.setHungerTime(attacker.getHungerTime() + hunger);
                    item.setItem(newstack);
                    item.lifespan += 6000;
                    smelt = true;
                }
            }
            return smelt;
        }
        return false;
    }

    public static boolean doDefaultFire(IPokemob attacker, Move_Base move, Vector3 location)
    {
        if (move.getPWR() <= 0 || !PokecubeMod.core.getConfig().defaultFireActions) return false;
        World world = attacker.getEntity().getEntityWorld();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        IBlockState state = location.getBlockState(world);
        Vector3 prevBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).norm()
                .addTo(location);
        IBlockState prevState = prevBlock.getBlockState(world);
        int flamNext = nextState.getBlock().getFlammability(world, nextBlock.getPos(), EnumFacing.UP);
        if (state.getMaterial().isReplaceable() && flamNext != 0)
        {
            location.setBlock(world, Blocks.FIRE);
            return true;
        }
        else if (prevState.getMaterial().isReplaceable()
                && state.getBlock().getFlammability(world, location.getPos(), EnumFacing.UP) != 0)
        {
            prevBlock.setBlock(world, Blocks.FIRE);
            return true;
        }
        else if (location.getBlock(world) == Blocks.SNOW_LAYER)
        {
            location.setAir(world);
            return true;
        }
        else if (prevBlock.getBlock(world) == Blocks.SNOW_LAYER)
        {
            prevBlock.setAir(world);
            return true;
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
        if (move.getPWR() < ELECTRICSTRONG || !PokecubeMod.core.getConfig().defaultElectricActions) { return false; }

        World world = attacker.getEntity().getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).reverse().norm()
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
        if (!PokecubeMod.core.getConfig().defaultIceActions) return false;
        World world = attacker.getEntity().getEntityWorld();
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
            return true;
        }
        return false;
    }

    public static boolean doDefaultWater(IPokemob attacker, Move_Base move, Vector3 location)
    {
        if (!PokecubeMod.core.getConfig().defaultWaterActions) return false;
        World world = attacker.getEntity().getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Vector3 prevBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).norm()
                .addTo(location);
        IBlockState prevState = prevBlock.getBlockState(world);
        if (state.getBlock() == Blocks.FIRE)
        {
            location.setAir(world);
            return true;
        }
        else if (prevState.getBlock() == Blocks.FIRE)
        {
            prevBlock.setAir(world);
            return true;
        }
        if (move.getPWR() < WATERSTRONG) { return false; }
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker.getEntity()).subtractFrom(location).reverse().norm()
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
            if (!MoveEventsHandler.canEffectBlock(attacker, location)) return false;
            if (move.getType(attacker) == PokeType.getType("water")) return doDefaultWater(attacker, move, location);
            if (move.getType(attacker) == PokeType.getType("ice")
                    && (move.move.attackCategory & IMoveConstants.CATEGORY_DISTANCE) > 0
                    && move.move.power > 0) { return doDefaultIce(attacker, move, location); }
            if (move.getType(attacker) == PokeType.getType("electric"))
            {
                doDefaultElectric(attacker, move, location);
            }
            if (move.getType(attacker) == PokeType.getType("fire")) { return doDefaultFire(attacker, move, location); }
            return false;
        }

        @Override
        public String getMoveName()
        {
            return move.name;
        }
    }

    public static class ActionWrapper implements IMoveAction
    {
        final IMoveAction   wrapped;
        private IMoveAction custom;
        private boolean     checked = false;

        public ActionWrapper(IMoveAction wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public boolean applyEffect(IPokemob user, Vector3 location)
        {
            if (!checked)
            {
                checked = true;
                custom = customActions.get(getMoveName());
            }
            boolean customApplied = custom != null && custom.applyEffect(user, location);
            return wrapped.applyEffect(user, location) || customApplied;
        }

        @Override
        public String getMoveName()
        {
            return wrapped.getMoveName();
        }

        @Override
        public void init()
        {
            wrapped.init();
            if (custom != null) custom.init();
        }
    }

    public static final Map<String, IMoveAction> customActions = Maps.newHashMap();

    private static MoveEventsHandler             INSTANCE;

    public static MoveEventsHandler getInstance()
    {
        return INSTANCE == null ? INSTANCE = new MoveEventsHandler() : INSTANCE;
    }

    public static void register(IMoveAction move)
    {
        if (!(move instanceof ActionWrapper))
        {
            move = new ActionWrapper(move);
        }
        getInstance().actionMap.put(move.getMoveName(), move);
    }

    public Map<String, IMoveAction> actionMap = Maps.newHashMap();

    private MoveEventsHandler()
    {
        PokecubeMod.MOVE_BUS.register(this);
        IOngoingAffected.EFFECTS.put(NonPersistantStatusEffect.ID, NonPersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(PersistantStatusEffect.ID, PersistantStatusEffect.class);
        IOngoingAffected.EFFECTS.put(OngoingMoveEffect.ID, OngoingMoveEffect.class);
        Status.initDefaults();
        Effect.initDefaults();
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
            action.init();
        }
        if (PokecubeCore.core.getConfig().permsMoveAction && attacker.getOwner() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) attacker.getOwner();
            IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            PlayerContext context = new PlayerContext(player);
            if (!handler.hasPermission(player.getGameProfile(), Permissions.MOVEWORLDACTION.get(move.name), context))
            {
                if (PokecubeMod.debug) PokecubeMod.log("Denied use of " + move.name + " for " + player);
                return;
            }
        }
        action.applyEffect(attacker, location);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEvent(MoveUse.DuringUse.Post evt)
    {
        MovePacket move = evt.getPacket();
        IPokemob attacker = move.attacker;
        Entity attacked = move.attacked;
        IPokemob target = CapabilityPokemob.getPokemobFor(attacked);

        IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            ActionResult<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(), move);
            if (result.getType() == EnumActionResult.SUCCESS)
            {
                attacker.setHeldItem(result.getResult());
            }
        }
        if (target != null)
        {
            IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                ActionResult<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(), move);
                if (result.getType() == EnumActionResult.SUCCESS)
                {
                    target.setHeldItem(result.getResult());
                }
            }
        }

        boolean user = evt.isFromUser();
        IPokemob applied = user ? attacker : target;
        if (applied != null && applied.getHeldItem() != null)
        {
            ItemGenerator.processHeldItemUse(move, applied, applied.getHeldItem());
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
        IPokemob target = CapabilityPokemob.getPokemobFor(attacked);
        IPokemob applied = user ? attacker : target;
        IPokemob other = user ? target : attacker;

        IPokemobUseable attackerheld = IPokemobUseable.getUsableFor(attacker.getHeldItem());
        if (attackerheld != null)
        {
            ActionResult<ItemStack> result = attackerheld.onMoveTick(attacker, attacker.getHeldItem(), move);
            if (result.getType() == EnumActionResult.SUCCESS)
            {
                attacker.setHeldItem(result.getResult());
            }
        }
        if (target != null)
        {
            IPokemobUseable targetheld = IPokemobUseable.getUsableFor(target.getHeldItem());
            if (targetheld != null)
            {
                ActionResult<ItemStack> result = targetheld.onMoveTick(attacker, target.getHeldItem(), move);
                if (result.getType() == EnumActionResult.SUCCESS)
                {
                    target.setHeldItem(result.getResult());
                }
            }
        }

        if (applied == null) return;
        if (!user)
        {
            applied.getEntity().getEntityData().setString("lastMoveHitBy", move.attack);
        }
        if (MoveEntry.oneHitKos.contains(attack.name) && target != null && target.getLevel() < attacker.getLevel())
        {
            move.failed = true;
        }
        if (target != null && target.getMoveStats().substituteHP > 0 && !user)
        {
            float damage = MovesUtils.getAttackStrength(attacker, target, move.getMove().getCategory(attacker),
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
            applied.getMoveStats().substituteHP = applied.getEntity().getMaxHealth() / 4;
        }

        if (applied.getHeldItem() != null)
        {
            ItemGenerator.processHeldItemUse(move, applied, applied.getHeldItem());
        }

        if (applied.getAbility() != null)
        {
            applied.getAbility().onMoveUse(applied, move);
        }

        if (attack.getName().equals(IMoveNames.MOVE_FALSESWIPE))
        {
            move.noFaint = true;
        }
        boolean blockMove = false;
        for (String s : MoveEntry.protectionMoves)
            if (s.equals(move.attack))
            {
                blockMove = true;
                break;
            }

        if (user && !blockMove && applied.getMoveStats().blocked && applied.getMoveStats().blockTimer-- <= 0)
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
                break;
            }
        if (move.attacked != move.attacker && !unblockable && other != null && other.getMoveStats().BLOCKCOUNTER > 0)
        {
            float count = Math.max(0, other.getMoveStats().BLOCKCOUNTER - 2);
            float chance = count != 0 ? Math.max(0.125f, ((1 / (count)))) : 1;
            if (chance > Math.random())
            {
                move.failed = true;
            }
        }
        if ((attack.getName().equals(IMoveNames.MOVE_PROTECT) || attack.getName().equals(IMoveNames.MOVE_DETECT)))
        {
            applied.getMoveStats().blockTimer = PokecubeMod.core.getConfig().attackCooldown * 2;
            applied.getMoveStats().blocked = true;
            applied.getMoveStats().BLOCKCOUNTER += 2;
        }
        if (applied.getMoveStats().BLOCKCOUNTER > 0) applied.getMoveStats().BLOCKCOUNTER--;
    }
}
