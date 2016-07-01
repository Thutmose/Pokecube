/**
 *
 */
package pokecube.core.blocks.healtable;

import net.minecraft.inventory.InventoryBasic;

/**
 * @author Manchou
 *
 */
public class InventoryHealTable extends InventoryBasic
{
    public InventoryHealTable(ContainerHealTable container, String par2Str)
    {
        super(par2Str, false, 6);
    }

}
