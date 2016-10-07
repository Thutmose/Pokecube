package pokecube.adventures.achievements;

import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;

public class AchievementDefeatTrainer extends Achievement
{

    public AchievementDefeatTrainer(String statIdIn, String unlocalizedName, int column, int row, Item itemIn,
            Achievement parent)
    {
        super(statIdIn, unlocalizedName, column, row, itemIn, parent);
    }

}
