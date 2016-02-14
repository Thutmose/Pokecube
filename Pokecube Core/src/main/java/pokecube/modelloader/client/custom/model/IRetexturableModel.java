package pokecube.modelloader.client.custom.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRetexturableModel
{
    @SideOnly(Side.CLIENT)
    void setTexturer(IPartTexturer texturer);
}
