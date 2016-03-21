package pokecube.pokeplayer.client.gui;

import java.util.List;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.StatCollector;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Tools;
import pokecube.pokeplayer.PokePlayer;
import pokecube.pokeplayer.network.PacketPokePlayer.MessageServer;
import thut.api.maths.Vector3;

public class GuiAsPokemob extends GuiDisplayPokecubeInfo
{
    public GuiAsPokemob()
    {
        super();
    }

    public IPokemob[] getPokemobsToDisplay()
    {
        IPokemob pokemob = PokePlayer.proxy.getPokemob(minecraft.thePlayer);
        if (pokemob != null) return new IPokemob[] { pokemob };
        return super.getPokemobsToDisplay();
    }

    @Override
    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = PokePlayer.proxy.getPokemob(minecraft.thePlayer);
        return pokemob != null ? pokemob : super.getCurrentPokemob();
    }

    public void pokemobAttack()
    {
        IPokemob pokemob = PokePlayer.proxy.getPokemob(minecraft.thePlayer);
        if (pokemob == null)
        {
            super.pokemobAttack();
            return;
        }

        EntityPlayer player = minecraft.thePlayer;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(11));
        buffer.writeByte(MessageServer.MOVEUSE);
        // buffer.writeInt(((Entity) getCurrentPokemob()).getEntityId());

        Entity target = Tools.getPointedEntity(player, 32);

        buffer.writeInt(target != null ? target.getEntityId() : 0);

        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null) { return; }
            Vector3 look = Vector3.getNewVector().set(player.getLookVec());
            Vector3 pos = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            Vector3 v = pos.findNextSolidBlock(player.worldObj, look, 32);
            boolean attack = false;

            if (target != null)
            {
                if (v == null) v = Vector3.getNewVector();
                v.set(target);
            }
            else if (v == null)
            {
                v = pos.add(look.scalarMultBy(16));
            }

            attack = true;
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiTeleport.instance().getState())
                {
                    GuiTeleport.instance().setState(true);
                    return;
                }
                else
                {
                    GuiTeleport.instance().setState(false);

                    Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
                    List<TeleDest> locations = PokecubeSerializer.getInstance()
                            .getTeleports(minecraft.thePlayer.getUniqueID().toString());

                    if (locations.size() > 0)
                    {
                        buffer.writeBoolean(true);
                    }
                    else
                    {
                        buffer.writeBoolean(false);
                    }
                }
            }
            else if (!attack)
            {
                Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(pokemob.getMoveIndex()));
                if (move != null && (target != null || v != null))
                {
                    String mess = StatCollector.translateToLocalFormatted("pokemob.action.usemove",
                            pokemob.getPokemonDisplayName(), MovesUtils.getTranslatedMove(move.getName()));
                    pokemob.displayMessageToOwner(mess);
                }
                buffer.writeBoolean(false);
            }
            else buffer.writeBoolean(false);

            if (v != null)
            {
                v.writeToBuff(buffer);
            }
        }

        MessageServer packet = new MessageServer(buffer);
        PokecubePacketHandler.sendToServer(packet);
    }

    public void pokemobBack()
    {
        if (!isPokemob()) super.pokemobBack();
        else
        {

        }
    }

    public void nextMove(int i)
    {
        if (!isPokemob()) super.nextMove(i);
        else
        {
            IPokemob pokemob = PokePlayer.proxy.getPokemob(minecraft.thePlayer);
            setMove(pokemob.getMoveIndex() + i);
        }
    }

    public void previousMove(int j)
    {
        if (!isPokemob()) super.previousMove(j);
        else
        {
            IPokemob pokemob = PokePlayer.proxy.getPokemob(minecraft.thePlayer);
            setMove(pokemob.getMoveIndex() - j);
        }
    }

    public void setMove(int num)
    {
        if (!isPokemob()) super.setMove(num);
        else
        {
            if (num < 0)
            {
                num = 5;
            }
            if (num > 5)
            {
                num = 0;
            }

            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(6));
            buffer.writeByte(MessageServer.MOVEINDEX);
            buffer.writeByte((byte) num);
            MessageServer packet = new MessageServer(buffer);
            PokecubePacketHandler.sendToServer(packet);
        }
    }

    boolean isPokemob()
    {
        IPokemob pokemob = PokePlayer.proxy.getPokemob(minecraft.thePlayer);
        return pokemob != null;
    }
}
