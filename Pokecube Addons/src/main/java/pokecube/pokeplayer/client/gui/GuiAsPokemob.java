package pokecube.pokeplayer.client.gui;

import java.util.List;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Tools;
import pokecube.pokeplayer.PokePlayer;
import pokecube.pokeplayer.network.PacketDoActions;
import thut.api.maths.Vector3;

public class GuiAsPokemob extends GuiDisplayPokecubeInfo
{
    public static int     moveIndex = 0;
    public static boolean useMove   = false;

    public GuiAsPokemob()
    {
        super();
    }

    @Override
    public IPokemob[] getPokemobsToDisplay()
    {
        IPokemob pokemob = PokePlayer.PROXY.getPokemob(minecraft.thePlayer);
        if (pokemob != null) return new IPokemob[] { pokemob };
        return super.getPokemobsToDisplay();
    }

    @Override
    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = PokePlayer.PROXY.getPokemob(minecraft.thePlayer);
        if (pokemob == null) return super.getCurrentPokemob();
        return pokemob;
    }

    @Override
    public void pokemobAttack()
    {
        IPokemob pokemob = PokePlayer.PROXY.getPokemob(minecraft.thePlayer);
        if (pokemob == null)
        {
            super.pokemobAttack();
            return;
        }
        if (!useMove || pokemob.getAttackCooldown() > 0) return;
        useMove = false;
        float range = 16;
        float contactRange = Math.max(1.5f, pokemob.getSize() * pokemob.getPokedexEntry().length);
        moveIndex = pokemob.getMoveIndex();
        Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(moveIndex));
        if (move == null)
        {
            moveIndex = 0;
            move = MovesUtils.getMoveFromName(pokemob.getMove(moveIndex));
        }
        if (move == null) return;
        EntityPlayer player = minecraft.thePlayer;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(11));
        buffer.writeByte(PacketDoActions.MOVEUSE);
        if ((move.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0) range = contactRange;
        Entity target = Tools.getPointedEntity(player, range);
        buffer.writeInt(target != null ? target.getEntityId() : -1);
        if (pokemob.getMove(pokemob.getMoveIndex()) == null) { return; }
        Vector3 look = Vector3.getNewVector().set(player.getLookVec());
        Vector3 pos = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
        Vector3 v = pos.findNextSolidBlock(player.getEntityWorld(), look, range);
        if (target != null)
        {
            if (v == null) v = Vector3.getNewVector();
            v.set(target);
        }
        else if (v == null)
        {
            v = pos.add(look.scalarMultBy(range));
        }
        if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
        {
            if (!GuiTeleport.instance().getState())
            {
                GuiTeleport.instance().setState(true);
                return;
            }
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
        else buffer.writeBoolean(false);

        if (v != null)
        {
            v.writeToBuff(buffer);
        }
        if (range == contactRange && target == null) return;
        PacketDoActions packet = new PacketDoActions(buffer);
        PokecubePacketHandler.sendToServer(packet);
    }

    @Override
    public void pokemobBack()
    {
        if (!isPokemob()) super.pokemobBack();
        else
        {

        }
    }

    @Override
    public void nextMove(int i)
    {
        super.nextMove(i);
    }

    @Override
    public void previousMove(int j)
    {
        super.previousMove(j);
    }

    @Override
    public void setMove(int num)
    {
        super.setMove(num);
    }

    boolean isPokemob()
    {
        IPokemob pokemob = PokePlayer.PROXY.getPokemob(minecraft.thePlayer);
        return pokemob != null;
    }
}
