package pokecube.compat;

import igwmod.gui.tabs.BaseWikiTab;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;

public class PokecubeWikiTab extends BaseWikiTab
{

	public PokecubeWikiTab()
	{
		pageEntries.add("Controls");
        pageEntries.add("HMs");
		pageEntries.add("item/pokedex");
		pageEntries.add("block/pc.top");
		pageEntries.add("item/pokecubebag");
		pageEntries.add("block/pc.base");
		pageEntries.add("block/pokecenter");
		pageEntries.add("block/tradingtable");
        pageEntries.add("Pokecubes");
        pageEntries.add("Stones");
	}

	@Override
	public String getName()
	{
		return "Pokecube";
	}

	@Override
	protected String getPageLocation(String pageEntry)
	{
		if (pageEntry.contains("item/") || pageEntry.contains("block/")) return pageEntry;
		return "pokecube:menu/" + pageEntry;
	}

	@Override
	protected String getPageName(String pageEntry)
	{
		if (pageEntry.startsWith("item") || pageEntry.startsWith("block"))
		{
			return I18n.format(pageEntry.replace("/", ".").replace("block", "tile") + ".name");
		}
		else
		{
			return I18n.format("igwtab.entry." + pageEntry);
		}
	}

	@Override
	public ItemStack renderTabIcon(igwmod.gui.GuiWiki gui)
	{
		return PokecubeItems.getStack("pokecube");
	}

}
