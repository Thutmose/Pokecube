package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.SharedMonsterAttributes;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import thut.api.maths.Vector3;

public class MoveToHandler implements IMobCommandHandler
{
    Vector3 location;
    float   speed;

    public MoveToHandler()
    {
    }

    public MoveToHandler(Vector3 location, Float speed)
    {
        this.location = location.copy();
        this.speed = Math.abs(speed);
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
        speed = (float) Math.min(speed,
                pokemob.getEntity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        pokemob.getEntity().getNavigator()
                .setPath(pokemob.getEntity().getNavigator().getPathToXYZ(location.x, location.y, location.z), speed);
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        location.writeToBuff(buf);
        buf.writeFloat(speed);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        location = Vector3.readFromBuff(buf);
        speed = buf.readFloat();
    }

}
