package pokecube.adventures.achievements;

import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;

public class AchievementDefeatLeader extends Achievement
{

    public AchievementDefeatLeader(String statIdIn, String unlocalizedName, int column, int row, Item blockIn,
            Achievement parent)
    {
        super(statIdIn, unlocalizedName, column, row, blockIn, parent);
        this.setSpecial();
    }
}
