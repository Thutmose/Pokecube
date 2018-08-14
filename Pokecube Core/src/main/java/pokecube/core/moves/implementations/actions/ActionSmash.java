package pokecube.core.moves.implementations.actions;

import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionSmash implements IMoveAction
{
    public ActionSmash()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getCombatState(CombatStates.ANGRY)) return false;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        level = Math.min(99, level);
        int rocks = smashRock(user, location, true);
        count = (int) Math.max(0, Math.ceil(rocks * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        if (rocks > 0)
        {
            smashRock(user, location, false);
            used = true;
            user.setHungerTime(user.getHungerTime() + count);
        }
        if (!used)
        {
            World world = user.getEntity().getEntityWorld();
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, location.getAABB().grow(1));
            if (!items.isEmpty())
            {
                // TODO instead of using reverse smelting, make an event that
                // can be used to allow pulverizer compatiblity.
                boolean smelt = false;
                for (int i = 0; i < items.size(); i++)
                {
                    EntityItem item = items.get(i);
                    ItemStack stack = item.getItem();
                    if (Block.getBlockFromItem(stack.getItem()) == null) continue;
                    int num = stack.getCount();
                    ItemStack newstack = null;
                    for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet())
                    {
                        ItemStack result = entry.getValue();
                        if (stack.getItem() == result.getItem() && stack.getMetadata() == result.getMetadata())
                        {
                            newstack = entry.getKey();
                            break;
                        }
                    }
                    if (newstack != null && newstack.getItem() instanceof ItemBlock)
                    {
                        newstack = newstack.copy();
                        if (newstack.getItemDamage() == 32767) newstack.setItemDamage(stack.getItemDamage());
                        newstack.setCount(num);
                        int hunger = PokecubeCore.core.getConfig().baseSmeltingHunger * num;
                        hunger = (int) Math.max(1, hunger / (float) user.getLevel());
                        user.setHungerTime(user.getHungerTime() + hunger);
                        item.setItem(newstack);
                        smelt = true;
                    }
                }
                return smelt;
            }
        }
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "rocksmash";
    }

    private int smashRock(IPokemob digger, Vector3 v, boolean count)
    {
        int ret = 0;
        EntityLivingBase owner = digger.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer)
        {
            player = (EntityPlayer) owner;
            BreakEvent evt = new BreakEvent(player.getEntityWorld(), v.getPos(),
                    v.getBlockState(player.getEntityWorld()), player);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return 0;
        }
        int fortune = digger.getLevel() / 30;
        boolean silky = Move_Basic.shouldSilk(digger) && player != null;
        World world = digger.getEntity().getEntityWorld();
        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        int range = 0;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(world);
                    if (PokecubeTerrainChecker.isRock(state))
                    {
                        if (!count)
                        {
                            if (!silky) doFortuneDrop(temp, world, fortune);
                            else
                            {
                                Block block = state.getBlock();
                                if (block.canSilkHarvest(world, temp.getPos(), state, player))
                                {
                                    Move_Basic.silkHarvest(state, temp.getPos(), world, player);
                                    temp.breakBlock(world, false);
                                }
                                else
                                {
                                    doFortuneDrop(temp, world, fortune);
                                }
                            }
                        }
                        ret++;
                    }
                }
        return ret;
    }

    private void doFortuneDrop(Vector3 location, World world, int fortune)
    {
        location.getBlock(world).dropBlockAsItem(world, location.getPos(), location.getBlockState(world), fortune);
        location.breakBlock(world, false);
    }
}
