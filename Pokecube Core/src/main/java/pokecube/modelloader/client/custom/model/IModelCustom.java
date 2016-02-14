package pokecube.modelloader.client.custom.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModelCustom
{
    @SideOnly(Side.CLIENT)
    void renderAll();

    @SideOnly(Side.CLIENT)
    void renderOnly(String... groupNames);

    @SideOnly(Side.CLIENT)
    void renderPart(String partName);

    @SideOnly(Side.CLIENT)
    void renderAllExcept(String... excludedGroupNames);
}
