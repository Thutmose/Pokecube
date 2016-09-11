package pokecube.core.blocks.nests;

import pokecube.core.blocks.TileEntityOwnable;

public class TileEntityBasePortal extends TileEntityOwnable
{
    @Override
    public boolean shouldBreak()
    {
        return false;
    }
}
