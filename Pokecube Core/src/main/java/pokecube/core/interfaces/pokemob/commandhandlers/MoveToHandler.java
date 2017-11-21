package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import thut.api.maths.Vector3;

public class MoveToHandler implements IMobCommandHandler
{
    Vector3 location;
    float   speed;

    public MoveToHandler()
    {
    }

    public MoveToHandler(Vector3 location, float speed)
    {
        this.location = location.copy();
        this.speed = Math.max(speed, 0);
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
        speed = Math.min(speed, pokemob.getEntity().getAIMoveSpeed());
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
