package pokecube.core.blocks.pokecubeTable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.obj.OBJModel;

public class TileEntityPokecubeTable extends TileEntity{
    private int counter = 1;
    private int max = 2;
    public List<String> visible = new ArrayList<String>();
    
    public TileEntityPokecubeTable()
    {
        this.visible.add(OBJModel.Group.ALL);
    }
    
    
}
