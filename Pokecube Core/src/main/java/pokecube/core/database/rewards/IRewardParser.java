package pokecube.core.database.rewards;

import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;

public interface IRewardParser
{
    void process(XMLReward reward) throws NullPointerException;
}
