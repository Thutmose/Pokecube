package pokecube.modelloader.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;

public class ExtraDatabase
{
    static class AddedXML
    {
        final String name;
        final String modId;
        final String xml;

        AddedXML(String modId, String name, String xml)
        {
            this.modId = modId;
            this.name = name;
            this.xml = xml;
        }
    }

    @XmlRootElement(name = "details")
    public static class XMLDetails
    {
        @XmlElement(name = "RIDDENOFFSET")
        float  offset = -1;
        @XmlElement(name = "PARTICLEEFFECTS")
        String particles;
    }

    @XmlRootElement(name = "ModelAnimator")
    public static class XMLFile
    {
        @XmlElement(name = "details")
        XMLDetails            details;
        @XmlElement(name = "Pokemon")
        List<XMLPokedexEntry> entries = Lists.newArrayList();
    }

    static HashMap<String, AddedXML>        xmls    = Maps.newHashMap();

    static Set<AddedXML>                    toAdd   = Sets.newHashSet();

    static HashMap<String, XMLPokedexEntry> entries = Maps.newHashMap();

    public static void addXMLEntry(String modId, String mobName, ArrayList<String> xml)
    {
        String val = "";
        for (String s : xml)
        {
            val += s + "\n";
        }
        toAdd.add(new AddedXML(modId, mobName, val));
    }

    public static void apply()
    {
        apply(null);
    }

    public static void apply(String toApply)
    {
        if (toAdd.isEmpty()) return;
        boolean bar = toApply == null;

        xmls.clear();
        for (AddedXML xml : toAdd)
        {
            boolean add = true;
            AddedXML old = xmls.get(xml.name);
            if (old != null)
            {
                try
                {
                    add = Config.instance.modIdComparator.compare(old.modId, xml.modId) < 0;
                }
                catch (Exception e)
                {
                    System.out.println(old + " " + xml);
                    add = false;
                }
            }
            if (add)
            {
                xmls.put(xml.name, xml);
            }
        }
        ProgressBar loading = null;
        if (bar)
        {
            loading = ProgressManager.push("XML Overrides", xmls.size());
        }
        for (String s : xmls.keySet())
        {
            if (toApply != null && !toApply.equalsIgnoreCase(s)) continue;
            PokedexEntry entry = Database.getEntry(s);
            entry = apply(xmls.get(s).xml, entry);
            if (bar) loading.step(entry.getName());
        }
        if (bar)
        {
            ProgressManager.pop(loading);
        }
    }

    public static PokedexEntry apply(String xml, PokedexEntry entry)
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLFile.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            XMLFile file = (XMLFile) unmarshaller.unmarshal(new StringReader(xml));
            for (XMLPokedexEntry fileEntry : file.entries)
            {
                if (entry == null && fileEntry != null)
                {
                    String name = fileEntry.name;
                    int number = fileEntry.number;
                    entry = new PokedexEntry(number, name);
                    if (fileEntry.base)
                    {
                        Database.baseFormes.put(number, entry);
                        Database.addEntry(entry);
                    }
                    PokedexEntryLoader.updateEntry(fileEntry, true);
                }
                else if (entry != null && fileEntry != null && !entry.getName().equals(fileEntry.name))
                {
                    if (Database.getEntry(fileEntry.name) != null)
                    {
                        entry = Database.getEntry(fileEntry.name);
                    }
                    else
                    {
                        String name = fileEntry.name;
                        int number = fileEntry.number;
                        entry = new PokedexEntry(number, name);
                        if (fileEntry.base)
                        {
                            Database.baseFormes.put(number, entry);
                            Database.addEntry(entry);
                        }
                        PokedexEntryLoader.updateEntry(fileEntry, true);
                    }
                }
                if (fileEntry != null) PokedexEntryLoader.addOverrideEntry(fileEntry, true);

                if (entry != null && file.details != null)
                {
                    if (file.details.offset != -1) entry.mountedOffset = file.details.offset;
                    if (file.details.particles != null) entry.particleData = file.details.particles.split(":");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return entry;
    }

    public static void cleanup()
    {
        xmls = null;
        entries = null;
    }

    public static XMLPokedexEntry getEntry(String name)
    {
        return entries.get(name);
    }
}
