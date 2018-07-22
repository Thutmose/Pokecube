package pokecube.core.database.rewards;

import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;

public interface IRewardParser
{
    void process(XMLReward reward) throws NullPointerException;

    default String serialize(XMLReward recipe)
    {
        return PokedexEntryLoader.gson.toJson(recipe);
    }

    default XMLReward deserialize(String recipe)
    {
        return PokedexEntryLoader.gson.fromJson(recipe, XMLReward.class);
    }
}
