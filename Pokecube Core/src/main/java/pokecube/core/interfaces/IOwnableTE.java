package pokecube.core.interfaces;

import net.minecraft.entity.Entity;

public interface IOwnableTE
{
    boolean canEdit(Entity editor);

    void setPlacer(Entity placer);
}
