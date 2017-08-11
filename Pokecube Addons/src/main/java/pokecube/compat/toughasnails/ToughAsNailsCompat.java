package pokecube.compat.toughasnails;

import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.SpawnCheckEvent;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import toughasnails.api.season.Season;
import toughasnails.api.season.Season.SubSeason;
import toughasnails.api.season.SeasonHelper;

public class ToughAsNailsCompat
{
    public static final QName SEASON    = new QName("season");
    public static final QName SUBSEASON = new QName("subseason");

    @Optional.Method(modid = "lostcities")
    @CompatClass(takesEvent = false, phase = Phase.CONSTRUCT)
    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new ToughAsNailsCompat());
    }

    @SubscribeEvent
    public void initMatcher(SpawnCheckEvent.Init event)
    {
        String seasons = event.matcher.spawnRule.values.get(SEASON);
        if (seasons != null)
        {
            String[] args = seasons.split(",");
            List<Season> toAdd = Lists.newArrayList();
            for (String s : args)
            {
                Season temp = Season.valueOf(s);
                if (temp != null)
                {
                    toAdd.add(temp);
                }
            }
            if (!toAdd.isEmpty())
                event.matcher.additionalConditions.add(new SeasonChecker(toAdd.toArray(new Season[toAdd.size()])));
        }
        String subseasons = event.matcher.spawnRule.values.get(SUBSEASON);
        if (subseasons != null)
        {
            String[] args = subseasons.split(",");
            List<SubSeason> toAdd = Lists.newArrayList();
            for (String s : args)
            {
                SubSeason temp = SubSeason.valueOf(s);
                if (temp != null)
                {
                    toAdd.add(temp);
                }
            }
            if (!toAdd.isEmpty()) event.matcher.additionalConditions
                    .add(new SubSeasonChecker(toAdd.toArray(new SubSeason[toAdd.size()])));
        }

    }

    public static class SeasonChecker implements Predicate<SpawnCheck>
    {
        final Season[] seasons;

        public SeasonChecker(Season... seasons)
        {
            this.seasons = seasons;
        }

        @Override
        public boolean apply(SpawnCheck input)
        {
            Season season = SeasonHelper.getSeasonData(input.world).getSeason();
            for (Season valid : seasons)
            {
                if (valid == season) return true;
            }
            return false;
        }
    }

    public static class SubSeasonChecker implements Predicate<SpawnCheck>
    {
        final SubSeason[] seasons;

        public SubSeasonChecker(SubSeason... seasons)
        {
            this.seasons = seasons;
        }

        @Override
        public boolean apply(SpawnCheck input)
        {
            SubSeason season = SeasonHelper.getSeasonData(input.world).getSubSeason();
            for (SubSeason valid : seasons)
            {
                if (valid == season) return true;
            }
            return false;
        }
    }
}
