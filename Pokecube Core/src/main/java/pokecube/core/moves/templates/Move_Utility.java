package pokecube.core.moves.templates;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.database.abilities.Ability;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.moves.TreeRemover;
import thut.api.maths.Vector3;

public class Move_Utility extends Move_Basic
{

    public static ArrayList<String> moves = new ArrayList<String>();

    public Move_Utility(String name)
    {
        super(name);
        moves.add(name);
    }

    public static boolean isUtilityMove(String move)
    {
        return moves.contains(move);
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked, float f)
    {
        if (attacked != null && attacked != attacker)
        {
            super.attack(attacker, attacked, f);
            return;
        }
        if ((attacker instanceof IPokemob && (PokecubeMod.semiHardMode || PokecubeMod.debug)
                && attacker.getPokemonAIState(IPokemob.TAMED)))
        {
            IPokemob a = ((IPokemob) attacker);

            boolean used = false;

            EntityLivingBase owner = a.getPokemonOwner();

            int number = countBerries(a, (EntityPlayer) owner);

            int count = 1;

            if (this.name == MOVE_FLASH)
            {
                System.out.println("Flashing");
                owner.addPotionEffect(new PotionEffect(Potion.nightVision.getId(), 5000));
                used = true;
                int level = a.getLevel();
                count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3)));
                consumeBerries(a, count);
                return;
            }

            if (number <= 0) return;

            if (used)
            {
                int level = a.getLevel();
                count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3)));
                consumeBerries(a, count);
            }
            else
            {
                ((EntityPlayer) owner)
                        .addChatMessage(new ChatComponentText("Your pokemon needs to eat more Berries to do that"));
            }
        }
    }

    @Override
    public void doWorldAction(IPokemob user, Vector3 location)
    {
        if (!(PokecubeMod.semiHardMode || PokecubeMod.debug)) return;

        boolean used = false;
        boolean repel = SpawnHandler.checkNoSpawnerInArea(((Entity) user).worldObj, location.intX(), location.intY(),
                location.intZ());

        EntityLivingBase owner = user.getPokemonOwner();

        int number = 0;

        if (owner != null && owner instanceof EntityPlayer)
        {
            number = countBerries(user, (EntityPlayer) owner);
            if (!repel)
            {
                ((EntityPlayer) owner)
                        .addChatMessage(new ChatComponentText("Your pokemon is too disgusted by the repel to do that"));
                return;
            }
            EntityPlayer player = (EntityPlayer) owner;

            BreakEvent evt = new BreakEvent(player.worldObj, location.getPos(), location.getBlockState(player.worldObj),
                    player);

            MinecraftForge.EVENT_BUS.post(evt);

            if (evt.isCanceled()) return;
        }
        else
        {
            number = 10;
        }

        int count = 1;
        int level = user.getLevel();

        if (this.name == MOVE_DIG)
        {
            count = (int) Math.max(1, Math.ceil(digHole(user, location, true) * Math.pow((100 - level) / 100d, 3)));
            if (count <= number && count > 0)
            {
                digHole(user, location, false);
                used = true;
            }
        }
        else if (this.name == MOVE_ROCKSMASH)
        {
            count = (int) Math.max(1, Math.ceil(smashRock(user, location, true) * Math.pow((100 - level) / 100d, 3)));
            if (count <= number && count > 0)
            {
                smashRock(user, location, false);
                used = true;
            }
        }
        else if (this.name == MOVE_CUT)
        {
            TreeRemover remover = new TreeRemover(((Entity) user).worldObj, location);
            int cut = remover.cut(true);
            count = (int) Math.max(1, Math.ceil(cut * Math.pow((100 - level) / 100d, 3)));
            if (count <= number && count > 0)
            {
                remover.cut(false);
                used = true;
            }
            remover.clear();
        }
        if (used)
        {
            consumeBerries(user, count);
        }
        else
        {
            ((EntityPlayer) owner)
                    .addChatMessage(new ChatComponentText("Your pokemon needs to eat more Berries to do that"));
        }
    }

    private int digHole(IPokemob digger, Vector3 v, boolean count)
    {
        int ret = 0;

        EntityLivingBase owner = digger.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer)
        {
            player = (EntityPlayer) owner;

            BreakEvent evt = new BreakEvent(player.worldObj, v.getPos(), v.getBlockState(player.worldObj), player);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return 0;
        }

        boolean silky = shouldSilk(digger) && player != null;
        boolean dropAll = shouldDropAll(digger);
        double uselessDrop = Math.pow((100 - digger.getLevel()) / 100d, 3);

        ArrayList<Block> list = new ArrayList<Block>();
        for (Block l : Mod_Pokecube_Helper.getCaveBlocks())
            list.add(l);
        for (Block l : Mod_Pokecube_Helper.getSurfaceBlocks())
            list.add(l);
        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                for (int k = -1; k <= 1; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(((Entity) digger).worldObj);
                    Block block = state.getBlock();
                    if (list.contains(block))
                    {
                        boolean drop = true;
                        if (Mod_Pokecube_Helper.getTerrain().contains(block) && !dropAll && !silky
                                && uselessDrop < Math.random())
                            drop = false;

                        if (!count)
                        {
                            if (!silky) temp.breakBlock(((Entity) digger).worldObj, drop);
                            else
                            {
                                if (block.canSilkHarvest(player.worldObj, temp.getPos(), state, player))
                                {
                                    silkHarvest(state, temp.getPos(), player.worldObj, player);
                                    temp.breakBlock(((Entity) digger).worldObj, drop);
                                }
                                else
                                {
                                    temp.breakBlock(((Entity) digger).worldObj, drop);
                                }
                            }
                        }
                        ret++;
                    }
                }
        return ret;
    }

    private boolean shouldDropAll(IPokemob pokemob)
    {
        if (pokemob.getAbility() == null) return false;
        Ability ability = pokemob.getAbility();
        return ability.toString().equalsIgnoreCase("arenatrap");
    }

    private int smashRock(IPokemob digger, Vector3 v, boolean count)
    {
        int ret = 0;

        EntityLivingBase owner = digger.getPokemonOwner();
        EntityPlayer player = null;
        if (owner instanceof EntityPlayer)
        {
            player = (EntityPlayer) owner;
            BreakEvent evt = new BreakEvent(player.worldObj, v.getPos(), v.getBlockState(player.worldObj), player);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return 0;
        }
        int fortune = digger.getLevel() / 30;

        ArrayList<Block> list = new ArrayList<Block>();
        for (Block l : Mod_Pokecube_Helper.getRocks())
            list.add(l);

        boolean silky = shouldSilk(digger) && player != null;

        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                for (int k = -1; k <= 1; k++)
                {
                    temp.set(v);
                    IBlockState state = temp.addTo(i, j, k).getBlockState(((Entity) digger).worldObj);
                    Block block = state.getBlock();
                    if (list.contains(block))
                    {
                        if (!count)
                        {
                            if (!silky) temp.breakBlock(((Entity) digger).worldObj, fortune, true);
                            else
                            {
                                if (block.canSilkHarvest(player.worldObj, temp.getPos(), state, player))
                                {
                                    silkHarvest(state, temp.getPos(), player.worldObj, player);
                                    temp.breakBlock(((Entity) digger).worldObj, false);
                                }
                                else
                                {
                                    temp.breakBlock(((Entity) digger).worldObj, fortune, true);
                                }
                            }
                        }
                        ret++;
                    }
                }
        return ret;
    }

    public static int countBerries(IPokemob mob, EntityPlayer player)
    {
        int ret = 0;

        if (player.capabilities.isCreativeMode) return Integer.MAX_VALUE;

        for (int i = 2; i < 7; i++)
        {
            ItemStack item = mob.getPokemobInventory().getStackInSlot(i);
            if (item != null && item.getItem() instanceof ItemBerry)
            {
                ret += item.stackSize;
            }
        }
        return ret;
    }

    public static void consumeBerries(IPokemob mob, int number)
    {
        for (int n = 2; n < 7; n++)
        {
            ItemStack i = mob.getPokemobInventory().getStackInSlot(n);

            if (i != null && i.getItem() instanceof ItemBerry)
            {
                if (i.stackSize >= number)
                {
                    i.splitStack(number);
                    if (i.stackSize <= 0)
                    {
                        i = null;
                        mob.getPokemobInventory().setInventorySlotContents(n, null);
                    }
                    return;
                }
                else
                {
                    number -= i.stackSize;
                    i = null;
                }
            }
            if (number <= 0) return;
        }
    }
}
