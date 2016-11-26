package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StructureEvent extends Event
{
    @Cancelable
    public static class SpawnEntity extends StructureEvent
    {
        private Entity       toSpawn;
        private final String structure;
        private final Entity original;

        public SpawnEntity(Entity entity, String structure)
        {
            this.structure = structure;
            this.original = entity;
            this.toSpawn = original;
        }

        public void setToSpawn(Entity entity)
        {
            this.toSpawn = entity;
        }

        public Entity getEntity()
        {
            return original;
        }

        public Entity getToSpawn()
        {
            return toSpawn;
        }

        public String getStructureName()
        {
            return structure;
        }
    }
}
