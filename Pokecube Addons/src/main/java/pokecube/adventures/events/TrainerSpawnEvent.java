package pokecube.adventures.events;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.adventures.entity.trainers.TypeTrainer;

@Cancelable
public class TrainerSpawnEvent extends Event
{
    private final Entity      trainer;
    private final TypeTrainer type;
    private final BlockPos    location;
    private final World       world;

    public TrainerSpawnEvent(TypeTrainer type, Entity trainer, BlockPos location, World world)
    {
        this.type = type;
        this.location = location;
        this.world = world;
        this.trainer = trainer;
    }

    public TypeTrainer getType()
    {
        return type;
    }

    public BlockPos getLocation()
    {
        return location;
    }

    public World getWorld()
    {
        return world;
    }

    public Entity getTrainer()
    {
        return trainer;
    }

}
