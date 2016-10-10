package pokecube.core.moves.implementations.actions;

import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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
import pokecube.core.commands.CommandTools;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionSmash implements IMoveAction
{
    public ActionSmash()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getPokemonAIState(IMoveConstants.ANGRY)) return false;
        IHungrymob mob = (IHungrymob) user;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;
        EntityLivingBase owner = user.getPokemonOwner();
        boolean repel = SpawnHandler.checkNoSpawnerInArea(((Entity) user).getEntityWorld(), location.intX(),
                location.intY(), location.intZ());
        if (owner != null && owner instanceof EntityPlayer)
        {
            if (!repel)
            {
                CommandTools.sendError(owner, "pokemob.action.denyrepel");
                return false;
            }
            EntityPlayer player = (EntityPlayer) owner;
            BreakEvent evt = new BreakEvent(player.getEntityWorld(), location.getPos(),
                    location.getBlockState(player.getEntityWorld()), player);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return false;
        }
        count = (int) Math.max(0, Math.ceil(smashRock(user, location, true) * Math.pow((100 - level) / 100d, 3)))
                * hungerValue;
        if (count > 0)
        {
            smashRock(user, location, false);
            used = true;
            mob.setHungerTime(mob.getHungerTime() + count);
        }
        System.out.println("test ");
        if (!used)
        {
            World world = ((Entity) user).getEntityWorld();
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, location.getAABB().expandXyz(1));
            if (!items.isEmpty())
            {
                // TODO instead of using reverse smelting, make an event that
                // can be used to allow pulverizer compatiblity.
                boolean smelt = false;
                for (int i = 0; i < items.size(); i++)
                {
                    EntityItem item = items.get(i);
                    ItemStack stack = item.getEntityItem();
                    if (Block.getBlockFromItem(stack.getItem()) == null) continue;
                    int num = stack.stackSize;
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
                        newstack.stackSize = num;
                        int hunger = PokecubeCore.core.getConfig().baseSmeltingHunger * num;
                        hunger = (int) Math.max(1, hunger / (float) user.getLevel());
                        ((IHungrymob) user).setHungerTime(((IHungrymob) user).getHungerTime() + hunger);
                        item.setEntityItemStack(newstack);
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
        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        int range = 0;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(((Entity) digger).getEntityWorld());
                    if (PokecubeTerrainChecker.isRock(state))
                    {
                        if (!count)
                        {
                            if (!silky) doFortuneDrop(temp, ((Entity) digger).getEntityWorld(), fortune);
                            else
                            {
                                Block block = state.getBlock();
                                if (block.canSilkHarvest(player.getEntityWorld(), temp.getPos(), state, player))
                                {
                                    Move_Basic.silkHarvest(state, temp.getPos(), player.getEntityWorld(), player);
                                    temp.breakBlock(((Entity) digger).getEntityWorld(), false);
                                }
                                else
                                {
                                    doFortuneDrop(temp, ((Entity) digger).getEntityWorld(), fortune);
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
