package pokecube.core.blocks;

import net.minecraft.entity.Entity;

public interface IOwnableTE
{
    void setPlacer(Entity placer);

    boolean canEdit(Entity editor);
}
