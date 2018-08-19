package pokecube.core.interfaces.pokemob.commandhandlers;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.common.commands.CommandTools;
import thut.lib.CompatWrapper;

public class TeleportHandler extends DefaultHandler
{
    public static float                MINDIST        = 5;
    public static final Set<Integer>   invalidDests   = Sets.newHashSet();

    public static Predicate<ItemStack> VALIDTELEITEMS = new Predicate<ItemStack>()
                                                      {
                                                          @Override
                                                          public boolean test(ItemStack t)
                                                          {
                                                              return t.getItem() == Items.ENDER_PEARL;
                                                          }
                                                      };

    public static void initTeleportRestrictions()
    {
        invalidDests.clear();
        for (int i : PokecubeMod.core.getConfig().teleDimBlackList)
        {
            invalidDests.add(new Integer(i));
        }
    }

    public static void swapTeleports(String uuid, int index1, int index2)
    {
        List<TeleDest> teleports = getTeleports(uuid);
        if (index1 < 0 || index1 >= teleports.size() || index2 < 0 || index2 >= teleports.size()) return;
        TeleDest dest1 = teleports.get(index1);
        TeleDest dest2 = teleports.get(index2);
        dest1.index = index2;
        dest2.index = index1;
        teleports.set(index1, dest2);
        teleports.set(index2, dest1);
        for (int i = 0; i < teleports.size(); i++)
        {
            teleports.get(i).index = i;
        }
    }

    public static void unsetTeleport(int index, String uuid)
    {
        TeleDest dest = getTeleport(uuid, index);
        List<TeleDest> list = getTeleports(uuid);
        if (dest != null)
        {
            list.remove(dest);
        }
        for (int i = 0; i < list.size(); i++)
        {
            list.get(i).index = i;
        }
    }

    public static int getTeleIndex(String uuid)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class)
                .getTeleIndex();
    }

    public static TeleDest getTeleport(String uuid, int teleIndex)
    {
        List<TeleDest> list = getTeleports(uuid);
        for (TeleDest dest : list)
        {
            if (dest.index == teleIndex) return dest;
        }
        return null;
    }

    public static TeleDest getTeleport(String uuid)
    {
        return getTeleport(uuid, getTeleIndex(uuid));
    }

    public static List<TeleDest> getTeleports(String uuid)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class)
                .getTeleDests();
    }

    public static void setTeleIndex(String uuid, int index)
    {
        List<?> list = getTeleports(uuid);
        if (index < 0) index = list.size() - 1;
        if (index < 0 || index > list.size() - 1) index = 0;
        PokecubePlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class)
                .setTeleIndex(index);
    }

    public static void setTeleport(String uuid, TeleDest teleport)
    {
        boolean set = false;
        List<TeleDest> list = getTeleports(uuid);
        ListIterator<TeleDest> dests = list.listIterator();
        while (dests.hasNext())
        {
            TeleDest dest = dests.next();
            if (dest.loc.withinDistance(MINDIST, teleport.loc))
            {
                if (set) dests.remove();
                else
                {
                    set = true;
                    teleport.index = dest.index;
                    dests.set(teleport);
                }
            }
        }
        if (!set)
        {
            list.add(teleport);
            teleport.index = list.size() - 1;
        }
        for (int i = 0; i < list.size(); i++)
        {
            list.get(i).index = i;
        }
    }

    public static void setTeleport(Vector4 v, String uuid)
    {
        TeleDest d = new TeleDest(v);
        setTeleport(uuid, d);
    }

    public static void renameTeleport(String uuid, int index, String customName)
    {
        getTeleport(uuid, index).setName(customName);
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
        EntityPlayer player = (EntityPlayer) pokemob.getOwner();
        TeleDest d = getTeleport(player.getCachedUniqueIdString());
        if (d == null) return;
        Vector3 loc = d.getLoc();
        Integer dim = d.getDim();
        Integer oldDim = player.dimension;
        int needed = PokecubeMod.core.getConfig().telePearlsCostSameDim;
        if (dim != oldDim)
        {
            needed = PokecubeMod.core.getConfig().telePearlsCostOtherDim;
            if (invalidDests.contains(dim) || invalidDests.contains(oldDim))
            {
                ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.teleport.invalid", "red");
                if (fromOwner()) pokemob.displayMessageToOwner(text);
                return;
            }
        }
        int count = 0;
        for (int i = 2; i < pokemob.getPokemobInventory().getSizeInventory(); i++)
        {
            ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
            if (!stack.isEmpty())
            {
                if (VALIDTELEITEMS.test(stack)) count += stack.getCount();
            }
        }
        if (needed > count)
        {
            ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.teleport.noitems", "red", needed);
            if (fromOwner()) pokemob.displayMessageToOwner(text);
            return;
        }
        if (needed > 0)
        {
            for (int i = 2; i < pokemob.getPokemobInventory().getSizeInventory(); i++)
            {
                ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
                if (CompatWrapper.isValid(stack))
                {
                    if (VALIDTELEITEMS.test(stack))
                    {
                        int toRemove = Math.min(needed, stack.getCount());
                        stack.splitStack(toRemove);
                        needed -= toRemove;
                        if (!stack.isEmpty()) pokemob.getPokemobInventory().setInventorySlotContents(i, stack);
                        else pokemob.getPokemobInventory().setInventorySlotContents(i, ItemStack.EMPTY);
                    }
                    if (needed <= 0) break;
                }
            }
        }
        if (needed > 0)
        {
            ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.teleport.noitems", "red", needed);
            if (fromOwner()) pokemob.displayMessageToOwner(text);
            return;
        }
        ITextComponent attackName = new TextComponentTranslation(
                MovesUtils.getUnlocalizedMove(IMoveNames.MOVE_TELEPORT));
        ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.move.used", "green",
                pokemob.getPokemonDisplayName(), attackName);
        if (fromOwner()) pokemob.displayMessageToOwner(text);
        EventsHandler.recallAllPokemobsExcluding(player, (IPokemob) null);
        Transporter.teleportEntity(player, loc, dim, false);
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
    }
}
