package pokecube.core.interfaces;

import thut.api.maths.Vector3;

public interface IMoveAction
{
    boolean applyEffect(IPokemob user, Vector3 location);
    
    String getMoveName();
}
