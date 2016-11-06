package pokecube.compat.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import thut.api.maths.Vector3;

@Cancelable
public class TransferDimension extends EntityEvent
{
    private int           destDimension;
    private final Vector3 destinationLoc;

    public TransferDimension(Entity entity, Vector3 destinationLoc, int destDim)
    {
        super(entity);
        destDimension = destDim;
        this.destinationLoc = destinationLoc;
    }

    public int getDestinationDim()
    {
        return destDimension;
    }

    public Vector3 getDesination()
    {
        return destinationLoc;
    }

    public void setDestination(Vector3 loc)
    {
        destinationLoc.set(loc);
    }

    public void setDestination(int dest)
    {
        destDimension = dest;
    }
}
