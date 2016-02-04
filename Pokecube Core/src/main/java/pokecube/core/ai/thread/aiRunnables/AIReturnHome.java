package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.PathEntity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class AIReturnHome extends AIBase
{
    final private EntityLiving entity;
    final IPokemob             mob;
    final PokedexEntry         entry;
    private double             speed;

    Vector3 v  = Vector3.getNewVectorFromPool();
    Vector3 v1 = Vector3.getNewVectorFromPool();

    public AIReturnHome(EntityLiving entity)
    {
        this.entity = entity;
        this.setMutex(2);
        mob = (IPokemob) entity;
        entry = mob.getPokedexEntry();
        this.speed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.4;
    }

    @Override
    public boolean shouldRun()
    {
        // TODO make this run if the pokemob should go home at the end of the
        // "day"
        // TODO Auto-generated method stub
        
        PokedexEntry entry = mob.getPokedexEntry();
        for(TimePeriod active: entry.activeTimes())
        {
            
        }
        return false;
    }

    @Override
    public void run()
    {
        PathEntity path = entity.getNavigator().getPathToPos(mob.getHome());
        if(path!=null) addEntityPath(entity.getEntityId(), entity.dimension, path, speed);
    }

    @Override
    public void reset()
    {
        // TODO Auto-generated method stub

    }

}
