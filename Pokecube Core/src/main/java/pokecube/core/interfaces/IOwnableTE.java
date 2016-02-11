package pokecube.core.interfaces;

import net.minecraft.entity.Entity;

public interface IOwnableTE
{
    void setPlacer(Entity placer);

    boolean canEdit(Entity editor);
}
