package pokecube.adventures.achievements;

import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

public class AchievementGetBadge extends Achievement
{
    public AchievementGetBadge(String statIdIn, String unlocalizedName, int column, int row, ItemStack stack,
            Achievement parent)
    {
        super(statIdIn, unlocalizedName, column, row, stack, parent);
        this.setSpecial();
    }

}
